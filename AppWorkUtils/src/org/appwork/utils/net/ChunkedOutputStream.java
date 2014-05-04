/**
 * Copyright (c) 2009 - 2011 AppWork UG(haftungsbeschränkt) <e-mail@appwork.org>
 * 
 * This file is part of org.appwork.utils.net
 * 
 * This software is licensed under the Artistic License 2.0,
 * see the LICENSE file or http://www.opensource.org/licenses/artistic-license-2.0.php
 * for details
 */
package org.appwork.utils.net;

import java.io.IOException;
import java.io.OutputStream;

/**
 * @author daniel,ChunkedOutputStream, see rfc2616#section-3.6
 * 
 */
public class ChunkedOutputStream extends OutputStream {

    private final OutputStream os;
    private final byte[]       buffer;
    private int                bufUsed = 0;
    boolean                    closed  = false;

    public ChunkedOutputStream(final OutputStream os) {
        this(os, 4096);
    }

    public ChunkedOutputStream(final OutputStream os, final byte[] buffer) {
        this.os = os;
        this.buffer = buffer;
    }

    public ChunkedOutputStream(final OutputStream os, final int bufSize) {
        this.os = os;
        this.buffer = new byte[bufSize];
    }

    private void _flush(final boolean emptyFlush) throws IOException {
        if (this.closed == false) {
            if (this.bufUsed > 0 || emptyFlush) {
                final byte[] bytes = Integer.toHexString(this.bufUsed).getBytes();
                /* send chunk size */
                this.os.write(bytes);
                this.os.write((byte) '\r');
                this.os.write((byte) '\n');
                /* send chunk data */
                if (this.bufUsed > 0 || emptyFlush) {
                    /* flush buffered data if any available */
                    if (this.bufUsed > 0) {
                        this.os.write(this.buffer, 0, this.bufUsed);
                    }
                    this.os.write((byte) '\r');
                    this.os.write((byte) '\n');
                }
                this.bufUsed = 0;
            }
        }
    }

    @Override
    public synchronized void close() throws IOException {
        if (this.closed == false) {
            this.sendEOF();
            this.closed = true;
        }
        this.os.close();
    }

    @Override
    public synchronized void flush() throws IOException {
        this._flush(false);
        this.os.flush();
    }

    public synchronized void sendEOF() throws IOException {
        /* flush rest available chunk data */
        this._flush(false);
        /* send empty chunk = EOF */
        this._flush(true);
        this.os.flush();
        this.closed = true;
    }

    @Override
    public synchronized void write(final byte b[], final int off, final int len) throws IOException {
        if (len == 0) { return; }
        if (this.bufUsed + len < this.buffer.length) {
            /* buffer has enough space for len bytes */
            /* fill buffer */
            System.arraycopy(b, off, this.buffer, this.bufUsed, len);
            this.bufUsed += len;
        } else {
            /* buffer has not enough space for len bytes, send as chunk */
            final byte[] bytes = Integer.toHexString(this.bufUsed + len).getBytes();
            /* send chunk size */
            this.os.write(bytes);
            this.os.write((byte) '\r');
            this.os.write((byte) '\n');
            /* send chunk data */
            if (this.bufUsed > 0) {
                /* flush buffered data if any available */
                this.os.write(this.buffer, 0, this.bufUsed);
                this.bufUsed = 0;
            }
            this.os.write(b, off, len);
            this.os.write((byte) '\r');
            this.os.write((byte) '\n');
        }
    }

    @Override
    public synchronized void write(final int b) throws IOException {
        if (this.bufUsed == this.buffer.length) {
            /* buffer full,send as chunked data */
            final byte[] bytes = Integer.toHexString(this.buffer.length).getBytes();
            /* send chunk size */
            this.os.write(bytes);
            this.os.write((byte) '\r');
            this.os.write((byte) '\n');
            /* send buffer as chunk data */
            this.os.write(this.buffer);
            this.os.write((byte) '\r');
            this.os.write((byte) '\n');
            this.bufUsed = 0;
        }
        /* fill buffer */
        this.buffer[this.bufUsed++] = (byte) b;
    }
}
