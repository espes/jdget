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
        utils.writeLongOptimized(10);
        utils.writeLongOptimized(127);
        utils.writeLongOptimized(128);
        utils.writeLongOptimized(32769);
        utils.writeLongOptimized(8388607);
        utils.writeLongOptimized(8388608);
        utils.writeLongOptimized(2147483647);
        utils.writeLongOptimized(2147483648l);
        utils.writeLongOptimized(-2147483648l);
        final ByteArrayInputStream bis = new ByteArrayInputStream(bos.toByteArray());
        utils = new AWFCUtils(bis);
        int read = 0;
        if (utils.readBoolean() != true) { throw new WTFException(); }
        if (utils.readShort() != 10) { throw new WTFException(); }
        if ((read = utils.readShort()) != 32765) { throw new WTFException("" + read); }
        if (!"HALLO DU".equals(utils.readString())) { throw new WTFException(); }
        if (utils.readLongOptimized() != 10) { throw new WTFException(); }
        if (utils.readLongOptimized() != 127) { throw new WTFException(); }
        if (utils.readLongOptimized() != 128) { throw new WTFException(); }
        if (utils.readLongOptimized() != 32769) { throw new WTFException(); }
        if (utils.readLongOptimized() != 8388607) { throw new WTFException(); }
        if (utils.readLongOptimized() != 8388608) { throw new WTFException(); }
        if (utils.readLongOptimized() != 2147483647) { throw new WTFException(); }
        if (utils.readLongOptimized() != 2147483648l) { throw new WTFException(); }
        if (utils.readLongOptimized() != -2147483648l) { throw new WTFException(); }

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

    public long readLongOptimized() throws IOException {
        final int read = this.ensureRead();
        switch (read) {
        case 1:
            return (this.ensureRead() & 255) << 0;
        case 2:
            this.ensureRead(2, this.buffer);
            return ((this.buffer[0] & 255) << 8) + ((this.buffer[1] & 255) << 0);
        case 3:
            this.ensureRead(3, this.buffer);
            return ((this.buffer[0] & 255) << 16) + ((this.buffer[1] & 255) << 8) + ((this.buffer[2] & 255) << 0);
        case 4:
            this.ensureRead(4, this.buffer);
            return ((long) (this.buffer[0] & 255) << 24) + ((this.buffer[1] & 255) << 16) + ((this.buffer[2] & 255) << 8) + ((this.buffer[3] & 255) << 0);
        case 5:
            this.ensureRead(5, this.buffer);
            return ((long) (this.buffer[0] & 255) << 32) + ((long) (this.buffer[1] & 255) << 24) + ((this.buffer[2] & 255) << 16) + ((this.buffer[3] & 255) << 8) + ((this.buffer[4] & 255) << 0);
        case 8:
            return this.readLong();
        default:
            throw new IOException("Invalid optimizedLong " + read);
        }
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

    public void writeLongOptimized(final long l) throws IOException {
        if (l >= 0) {
            if (l <= 127) {
                this.buffer[0] = (byte) 1;
                this.buffer[1] = (byte) (l >>> 0 & 0xFF);
                this.getCurrentOutputStream().write(this.buffer, 0, 2);
                return;
            }
            if (l <= 32.767) {
                this.buffer[0] = (byte) 2;
                this.buffer[1] = (byte) (l >>> 8 & 0xFF);
                this.buffer[2] = (byte) (l >>> 0 & 0xFF);
                this.getCurrentOutputStream().write(this.buffer, 0, 3);
                return;
            }
            if (l <= 8388607) {
                this.buffer[0] = (byte) 3;
                this.buffer[1] = (byte) (l >>> 16);
                this.buffer[2] = (byte) (l >>> 8);
                this.buffer[3] = (byte) (l >>> 0);
                this.getCurrentOutputStream().write(this.buffer, 0, 4);
                return;
            }
            if (l <= 2147483647) {
                this.buffer[0] = (byte) 4;
                this.buffer[1] = (byte) (l >>> 24);
                this.buffer[2] = (byte) (l >>> 16);
                this.buffer[3] = (byte) (l >>> 8);
                this.buffer[4] = (byte) (l >>> 0);
                this.getCurrentOutputStream().write(this.buffer, 0, 5);
                return;
            }
            if (l <= 549755813887l) {
                this.buffer[0] = (byte) 5;
                this.buffer[1] = (byte) (l >>> 32);
                this.buffer[2] = (byte) (l >>> 24);
                this.buffer[3] = (byte) (l >>> 16);
                this.buffer[4] = (byte) (l >>> 8);
                this.buffer[5] = (byte) (l >>> 0);
                this.getCurrentOutputStream().write(this.buffer, 0, 6);
                return;
            }
        }
        this.getCurrentOutputStream().write(8);
        this.writeLong(l);
    }

    public void writeShort(final int v) throws IOException {
        this.buffer[0] = (byte) (v >>> 8 & 0xFF);
        this.buffer[1] = (byte) (v >>> 0 & 0xFF);
        this.getCurrentOutputStream().write(this.buffer, 0, 2);
    }

    public void writeString(final String string) throws IOException {
        if (string == null) { throw new IOException("string == null"); }
        final byte[] stringBytes = string.getBytes("UTF-8");
        this.writeShort(stringBytes.length);
        this.getCurrentOutputStream().write(stringBytes);
    }
}
