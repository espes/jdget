/**
 * Copyright (c) 2009 - 2012 AppWork UG(haftungsbeschränkt) <e-mail@appwork.org>
 * 
 * This file is part of org.appwork.utils.awfc
 * 
 * This software is licensed under the Artistic License 2.0,
 * see the LICENSE file or http://www.opensource.org/licenses/artistic-license-2.0.php
 * for details
 */
package org.appwork.utils.awfc;

import java.io.IOException;
import java.io.OutputStream;
import java.security.MessageDigest;
import java.util.Arrays;

import org.appwork.utils.net.CountingOutputStream;

/**
 * @author Daniel Wilhelm
 * 
 */
public class AWFCOutputStream extends OutputStream {

    /**
     * Appwork FileContainer     * 
     
     */
    private final OutputStream   os;
    private AWFCEntry            currentEntry                = null;
    private CountingOutputStream currentCountingOutputStream = null;
    private byte                 writeBuffer[]               = new byte[10];
    private final MessageDigest  md;
    private boolean              headerWritten               = false;

    public AWFCOutputStream(OutputStream os, MessageDigest md) {
        this.os = os;
        this.md = md;
    }

    @Override
    public synchronized void write(int b) throws IOException {
        getCurrentOutputStream().write(b);
    }

    private synchronized void closeLastEntry() throws IOException {
        if (currentEntry != null) {
            /* verify if currentEntry is complete */
            long bytesWritten = currentCountingOutputStream.transferedBytes();
            if (currentEntry.getSize() != bytesWritten) throw new IOException("Wrong size for Entry: " + currentEntry + " != " + bytesWritten);
            if (md != null && !Arrays.equals(currentEntry.getHash(), md.digest())) throw new IOException("Wrong hash for Entry: " + currentEntry);
            /* we want to write on original OutputStream again */
            currentCountingOutputStream = null;
            currentEntry = null;
        } else {
            throw new IOException("No lastEntry to close!");
        }
    }

    public synchronized void putNextEntry(AWFCEntry e) throws IOException {
        if (currentEntry != null) {
            closeLastEntry();
        }
        currentEntry = e;
        if (headerWritten == false) writeAWFCHeader();
        writeAWFCEntry(e);
        if (md != null) md.reset();
        currentCountingOutputStream = new CountingOutputStream(os) {

            @Override
            public void write(byte[] b) throws IOException {
                super.write(b);
                if (md != null) md.update(b);
            }

            @Override
            public void write(byte[] b, int off, int len) throws IOException {
                super.write(b, off, len);
                if (md != null) md.update(b, off, len);
            }

            @Override
            public void write(int b) throws IOException {
                super.write(b);
                if (md != null) md.update((byte) b);
            }

            @Override
            public void close() throws IOException {
                try {
                    closeLastEntry();
                } finally {
                    super.close();
                }
            }

        };

    }

    private void writeAWFCEntry(AWFCEntry e) throws IOException {        
        writeString(e.getPath());
        if (e.isFile()) {
            writeLongOptimized(e.getSize());
            if (md != null) {
                /* only write Hash when MessageDigest is set */
                if (e.getHash() == null) throw new IOException("Hash is missing for Entry: " + currentEntry);
                if (e.getHash().length != md.getDigestLength()) throw new IOException("Hashlength does not match for Entry: " + currentEntry);
                write(e.getHash());
            }
        }
    }

    @Override
    public synchronized void write(byte b[]) throws IOException {
        getCurrentOutputStream().write(b);
    }

    @Override
    public synchronized void write(byte[] b, int off, int len) throws IOException {
        getCurrentOutputStream().write(b, off, len);
    }

    @Override
    public synchronized void flush() throws IOException {
        getCurrentOutputStream().flush();
    }

    @Override
    public synchronized void close() throws IOException {
        getCurrentOutputStream().close();
    }

    private void writeString(String string) throws IOException {
        if (string == null) throw new IOException("string == null");
        byte[] stringBytes = string.getBytes("UTF-8");
        writeShort(stringBytes.length);
        getCurrentOutputStream().write(stringBytes);
    }

    private synchronized void writeAWFCHeader() throws IOException {
        headerWritten = true;
        write(1);
        writeBoolean(md != null);
        if (md != null) {
            writeString(md.getAlgorithm());
            writeShort(md.getDigestLength());
        }
    }

    protected synchronized OutputStream getCurrentOutputStream() throws IOException {
        if (currentCountingOutputStream != null) { return currentCountingOutputStream; }
        if (currentEntry != null) return os;
        throw new IOException("No Entry added yet!");
    }

    private void writeLong(long l) throws IOException {
        writeBuffer[0] = (byte) (l >>> 56);
        writeBuffer[1] = (byte) (l >>> 48);
        writeBuffer[2] = (byte) (l >>> 40);
        writeBuffer[3] = (byte) (l >>> 32);
        writeBuffer[4] = (byte) (l >>> 24);
        writeBuffer[5] = (byte) (l >>> 16);
        writeBuffer[6] = (byte) (l >>> 8);
        writeBuffer[7] = (byte) (l >>> 0);
        getCurrentOutputStream().write(writeBuffer, 0, 8);
    }

    private void writeLongOptimized(long l) throws IOException {
        if (l >= 0) {
            if (l <= 127) {
                writeBuffer[0] = (byte) (1);
                writeBuffer[1] = (byte) ((l >>> 0) & 0xFF);
                getCurrentOutputStream().write(writeBuffer, 0, 2);
                return;
            }
            if (l <= 32.767) {
                writeBuffer[0] = (byte) (2);
                writeBuffer[1] = (byte) ((l >>> 8) & 0xFF);
                writeBuffer[2] = (byte) ((l >>> 0) & 0xFF);
                getCurrentOutputStream().write(writeBuffer, 0, 3);
                return;
            }
            if (l <= 8388607) {
                writeBuffer[0] = (byte) (3);
                writeBuffer[1] = (byte) (l >>> 16);
                writeBuffer[2] = (byte) (l >>> 8);
                writeBuffer[3] = (byte) (l >>> 0);
                getCurrentOutputStream().write(writeBuffer, 0, 4);
                return;
            }
            if (l <= 2147483647) {
                writeBuffer[0] = (byte) (4);
                writeBuffer[1] = (byte) (l >>> 24);
                writeBuffer[2] = (byte) (l >>> 16);
                writeBuffer[3] = (byte) (l >>> 8);
                writeBuffer[4] = (byte) (l >>> 0);
                getCurrentOutputStream().write(writeBuffer, 0, 5);
                return;
            }
            if (l <= 549755813887l) {
                writeBuffer[0] = (byte) (5);
                writeBuffer[1] = (byte) (l >>> 32);
                writeBuffer[2] = (byte) (l >>> 24);
                writeBuffer[3] = (byte) (l >>> 16);
                writeBuffer[4] = (byte) (l >>> 8);
                writeBuffer[5] = (byte) (l >>> 0);
                getCurrentOutputStream().write(writeBuffer, 0, 6);
                return;
            }
        }
        write(8);
        writeLong(l);
    }

    private void writeShort(int v) throws IOException {
        writeBuffer[0] = (byte) ((v >>> 8) & 0xFF);
        writeBuffer[1] = (byte) ((v >>> 0) & 0xFF);
        getCurrentOutputStream().write(writeBuffer, 0, 2);
    }

    private void writeBoolean(boolean b) throws IOException {
        getCurrentOutputStream().write(b ? 1 : 0);
    }

}
