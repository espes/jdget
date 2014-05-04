/**
 * Copyright (c) 2009 - 2013 AppWork UG(haftungsbeschränkt) <e-mail@appwork.org>
 * 
 * This file is part of org.appwork.utils.net
 * 
 * This software is licensed under the Artistic License 2.0,
 * see the LICENSE file or http://www.opensource.org/licenses/artistic-license-2.0.php
 * for details
 */
package org.appwork.utils.net;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;

/**
 * @author daniel
 * 
 */
public class DeChunkingOutputStream extends OutputStream {

    public static void main(final String[] args) throws Throwable {
        final ByteArrayOutputStream bos = new ByteArrayOutputStream();
        final ChunkedOutputStream cos = new ChunkedOutputStream(new DeChunkingOutputStream(bos), 0);
        cos.write("Hello ".getBytes("UTF-8"));
        cos.write("H".getBytes("UTF-8"));
        cos.write("ell".getBytes("UTF-8"));
        cos.write("o! ".getBytes("UTF-8"));
        cos.write("Hello This is a simple Test".getBytes("UTF-8"));
        cos.close();
        System.out.println("Output: " + new String(bos.toByteArray(), "UTF-8"));
    }

    protected final OutputStream os;
    protected int                nextExpectedChunkLeft = 0;
    protected byte[]             chunkSize             = new byte[4];
    protected int                chunkSizePosition     = 0;
    protected boolean            chunkedExtension      = false;
    protected boolean            chunkedTrailers       = false;
    private int                  lastWrite             = -1;

    public DeChunkingOutputStream(final OutputStream os) {
        this.os = os;
    }

    @Override
    public void close() throws IOException {
        if (this.nextExpectedChunkLeft > 0) { throw new IOException("malformed chunk, still " + this.nextExpectedChunkLeft + " bytes expected!"); }
        this.os.close();
    }

    @Override
    public void flush() throws IOException {
        this.os.flush();
    }

    protected int nextChunkInfoProcessed(final int b) throws IOException {
        if (this.nextExpectedChunkLeft > 0) { return this.nextExpectedChunkLeft; }
        if (this.lastWrite == 13 || this.lastWrite == 10) {
            /* finish of LF/CRLF */
            final boolean returnChunkSize = this.lastWrite == 10;
            if (this.lastWrite == 13 && b != 10) { throw new IOException("malformed chunk, bad/missing LF/CRLF"); }
            this.lastWrite = -1;
            this.chunkedExtension = false;
            if (this.chunkSizePosition > 0) {
                /* we can parse nextExpectedChunkLeft */
                final String size = new String(this.chunkSize, 0, this.chunkSizePosition, "UTF-8");
                this.nextExpectedChunkLeft = Integer.parseInt(size.toString().trim(), 16);
                this.chunkSizePosition = 0;
                // System.out.println("Next ChunkSize: " +
                // this.nextExpectedChunkLeft);
                if (this.nextExpectedChunkLeft == 0) {
                    this.chunkedTrailers = true;
                }
            }
            if (returnChunkSize) {
                return this.nextExpectedChunkLeft;
            } else {
                return 0;
            }
        }
        if (b != 10 && b != 13) {
            /* no LF/CRLF */
            if (b == 59) {
                /* chunkedExtension found */
                this.chunkedExtension = true;
            } else {
                if (this.chunkedExtension == false) {
                    /* no chunkedExtension byte, we can add it to our chunkSize */
                    this.chunkSize[this.chunkSizePosition++] = (byte) b;
                }
            }
        } else {
            this.lastWrite = b;
        }
        return this.nextExpectedChunkLeft;
    }

    @Override
    public void write(final byte b[], final int off, final int len) throws IOException {
        if (this.chunkedTrailers) {
            /* we no longer have data to write, only Trailers are following */
            return;
        }
        if (this.nextExpectedChunkLeft >= len) {
            /* we can still write len bytes because they belong to expectedChunk */
            this.os.write(b, off, len);
            /* reduce nextExpectedChunkLeft by len */
            this.nextExpectedChunkLeft -= len;
        } else {
            /* we have to write in multiple steps */
            int rest = len;
            int done = 0;
            int next = 0;
            while (rest > 0 && this.chunkedTrailers == false) {
                next = Math.min(this.nextExpectedChunkLeft, rest);
                if (next > 0) {
                    /* we can write up to next bytes */
                    this.os.write(b, off + done, next);
                    this.nextExpectedChunkLeft -= next;
                    rest -= next;
                    done += next;
                } else {
                    final byte temp = b[off + done];
                    rest -= 1;
                    done += 1;
                    this.nextChunkInfoProcessed(temp);
                }
            }
        }
    }

    @Override
    public void write(final int b) throws IOException {
        if (this.chunkedTrailers) {
            /* we no longer have data to write, only Trailers are following */
            return;
        }
        if (this.nextChunkInfoProcessed(b) > 0) {
            /* we can still write 1 byte because it belongs to expectedChunk */
            this.os.write(b);
            /* reduce nextExpectedChunkLeft by 1 */
            this.nextExpectedChunkLeft--;
        }
    }

}
