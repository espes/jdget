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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.appwork.exceptions.WTFException;
import org.appwork.utils.ReusableByteArrayOutputStreamPool;
import org.appwork.utils.ReusableByteArrayOutputStreamPool.ReusableByteArrayOutputStream;

/**
 * @author daniel
 * 
 */
public class AWFCUtils {
    public static void main(final String[] args) throws IOException {
        final ByteArrayOutputStream bos = new ByteArrayOutputStream();
        AWFCUtils utils = new AWFCUtils(bos);

        utils.writeBoolean(true);
        utils.writeShort(10);
        utils.writeShort(32765);
        utils.writeString("HALLO DU");

        long[] testValues = new long[] { Long.MAX_VALUE };
        for (long value : testValues) {
            utils.writeLongOptimized(value);
        }
        final ByteArrayInputStream bis = new ByteArrayInputStream(bos.toByteArray());
        utils = new AWFCUtils(bis);
        int read = 0;
        if (utils.readBoolean() != true) { throw new WTFException(); }
        if (utils.readShort() != 10) { throw new WTFException(); }
        if ((read = utils.readShort()) != 32765) { throw new WTFException("" + read); }
        if (!"HALLO DU".equals(utils.readString())) { throw new WTFException(); }
        for (long value : testValues) {
            long ret = utils.readLongOptimized();
            if (value != ret) { throw new WTFException(); }
        }
        final ReusableByteArrayOutputStream rbos = ReusableByteArrayOutputStreamPool.getReusableByteArrayOutputStream();
        final InputStream is = new InputStream() {

            protected int pos;

            public synchronized int read() {
                return (pos < rbos.size()) ? (rbos.getInternalBuffer()[pos++] & 0xff) : -1;
            }

            @Override
            public synchronized int read(byte b[], int off, int len) {
                if (b == null) {
                    throw new NullPointerException();
                } else if (off < 0 || len < 0 || len > b.length - off) { throw new IndexOutOfBoundsException(); }

                if (pos >= rbos.size()) { return -1; }

                int avail = rbos.size() - pos;
                if (len > avail) {
                    len = avail;
                }
                if (len <= 0) { return 0; }
                System.arraycopy(rbos.getInternalBuffer(), pos, b, off, len);
                pos += len;
                return len;
            }

            /*
             * (non-Javadoc)
             * 
             * @see java.io.InputStream#reset()
             */
            @Override
            public synchronized void reset() throws IOException {
                pos = 0;
            }

        };

        AWFCUtils utils2 = new AWFCUtils() {

            @Override
            public InputStream getCurrentInputStream() throws IOException {
                // TODO Auto-generated method stub
                return is;
            }

            @Override
            public OutputStream getCurrentOutputStream() throws IOException {
                return rbos;
            }

        };
        long size = 0;
        long l = 0;
        for (l = 0; l < Integer.MAX_VALUE; l++) {
            rbos.reset();
            is.reset();
            utils2.writeLongOptimized(l);
            if (utils2.readLongOptimized() != l) {
                break;
            }
            size += rbos.size();
            System.out.println(l + " " + size);
        }

    }

    private final byte[]       buffer = new byte[16];
    private final OutputStream os;

    private final InputStream  is;

    public AWFCUtils() {
        this.is = null;
        this.os = null;
    }

    public AWFCUtils(final InputStream is) {
        this.is = is;
        this.os = null;
    }

    public AWFCUtils(final OutputStream os) {
        this.os = os;
        this.is = null;
    }

    public int ensureRead() throws IOException {
        final int read = this.getCurrentInputStream().read();
        if (read == -1) { throw new EOFException(); }
        return read;
    }

    public byte[] ensureRead(final int size, final byte[] buffer) throws IOException {
        byte[] stringBytes = buffer;
        if (stringBytes == null) {
            stringBytes = new byte[size];
        }
        if (size > stringBytes.length) { throw new IOException("buffer too small"); }
        int done = 0;
        int read = 0;
        while (done < size && (read = this.getCurrentInputStream().read(stringBytes, done, size - done)) != -1) {
            done += read;
        }
        if (done != size) { throw new EOFException(); }
        return stringBytes;
    }

    public InputStream getCurrentInputStream() throws IOException {
        if (this.is != null) { return this.is; }
        throw new IOException("no InputStream available");
    }

    public OutputStream getCurrentOutputStream() throws IOException {
        if (this.os != null) { return this.os; }
        throw new IOException("no OutputStream available");
    }

    public boolean readBoolean() throws IOException {
        final int read = this.ensureRead();
        if (read == 1) { return true; }
        if (read == 0) { return false; }
        throw new IOException("Invalid boolean value!");
    }

    public long readLong() throws IOException {
        this.ensureRead(8, this.buffer);
        return ((long) this.buffer[0] << 56) + ((long) (this.buffer[1] & 255) << 48) + ((long) (this.buffer[2] & 255) << 40) + ((long) (this.buffer[3] & 255) << 32) + ((long) (this.buffer[4] & 255) << 24) + ((this.buffer[5] & 255) << 16) + ((this.buffer[6] & 255) << 8) + ((this.buffer[7] & 255) << 0);
    }

    public int readShort() throws IOException {
        this.ensureRead(2, this.buffer);
        return ((this.buffer[0] & 255) << 8) + ((this.buffer[1] & 255) << 0);
    }

    public String readString() throws IOException {
        return this.readString(this.readShort());
    }

    public String readString(final int size) throws IOException {
        return new String(this.ensureRead(size, null), "UTF-8");
    }

    public void writeBoolean(final boolean b) throws IOException {
        this.getCurrentOutputStream().write(b ? 1 : 0);
    }

    public void writeLong(final long l) throws IOException {
        this.buffer[0] = (byte) (l >>> 56);
        this.buffer[1] = (byte) (l >>> 48);
        this.buffer[2] = (byte) (l >>> 40);
        this.buffer[3] = (byte) (l >>> 32);
        this.buffer[4] = (byte) (l >>> 24);
        this.buffer[5] = (byte) (l >>> 16);
        this.buffer[6] = (byte) (l >>> 8);
        this.buffer[7] = (byte) (l >>> 0);
        this.getCurrentOutputStream().write(this.buffer, 0, 8);
    }

    public long readLongOptimized() throws IOException {
        long ret = 0;
        long read = 0;
        int position = 0;
        while (true) {
            read = ensureRead();
            ret = ret + (read >>> 1 << (position * 7));
            if ((read & 1) == 0) return ret;
            position++;
        }
    }

    public void writeLongOptimized(final long value) throws IOException {
        if (value < 0) throw new NumberFormatException("value must be >=0");
        long rest = value;
        int bufferPosition = 0;
        while (true) {
            int write = (int) (((rest & 127) << 1) & 0xFF);
            this.buffer[bufferPosition] = (byte) write;
            rest = rest >>> 7;
            if (rest == 0) {
                this.getCurrentOutputStream().write(this.buffer, 0, bufferPosition + 1);
                return;
            }
            this.buffer[bufferPosition] = (byte) (this.buffer[bufferPosition] | 1);
            bufferPosition++;
        }
    }

    public void writeShort(final int v) throws IOException {
        this.buffer[0] = (byte) (v >>> 8 & 0xFF);
        this.buffer[1] = (byte) (v >>> 0 & 0xFF);
        this.getCurrentOutputStream().write(this.buffer, 0, 2);
    }

    public void writeString(final String string) throws IOException {
        if (string == null) { throw new IOException("string == null"); }
        final byte[] stringBytes = string.getBytes("UTF-8");
        if (stringBytes.length > 32767) throw new IllegalArgumentException("StringSize must not be greater than 32767 bytes");
        this.writeShort(stringBytes.length);
        this.getCurrentOutputStream().write(stringBytes);
    }
}
