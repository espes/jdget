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

import java.io.DataInputStream;
import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.zip.ZipInputStream;

import org.appwork.utils.net.LimitedInputStream;

/**
 * @author Daniel Wilhelm
 * 
 */
public class AWFCInputStream extends InputStream {

    private final InputStream  is;
    private LimitedInputStream lis          = null;
    private MessageDigest      md           = null;
    private boolean            headerRead   = false;
    private AWFCEntry          currentEntry = null;
    private byte[]             skipBuffer   = new byte[32767];

    public AWFCInputStream(InputStream is) {
        this.is = is;
    }

    @Override
    public int read() throws IOException {
        return getCurrentInputStream().read();
    }

    @Override
    public int read(byte[] b) throws IOException {
        return getCurrentInputStream().read(b);
    }

    @Override
    public int read(byte[] b, int off, int len) throws IOException {
        return getCurrentInputStream().read(b, off, len);
    }

    @Override
    public void close() throws IOException {
        getCurrentInputStream().close();
    }

    @Override
    public long skip(long n) throws IOException {
        return getCurrentInputStream().skip(n);
    }

    @Override
    public int available() throws IOException {
        return getCurrentInputStream().available();
    }

    @Override
    public synchronized void mark(int readlimit) {
    }

    @Override
    public synchronized void reset() throws IOException {
    }

    @Override
    public boolean markSupported() {
        return false;
    }

    public synchronized AWFCEntry getNextEntry() throws IOException {
        if (headerRead == false) readAWFCHeader();
        if (currentEntry != null) {
            while (lis.available() > 0) {
                lis.skip(lis.available());
            }
            lis = null;
            currentEntry = null;
        }
        currentEntry = readAWFCEntry();
        if (currentEntry == null) return null;
        if (md != null) md.reset();
        lis = new LimitedInputStream(is, currentEntry.getSize()) {

            @Override
            public int available() throws IOException {
                long ret = getLimit() - transferedBytes();
                if (ret > Integer.MAX_VALUE) return Integer.MAX_VALUE;
                return (int) ret;
            }

            @Override
            public int read() throws IOException {
                int ret = super.read();
                if (md != null) {
                    if (ret != -1) {
                        md.update((byte) ret);
                    } else if (ret == -1) {
                        if (!Arrays.equals(md.digest(), currentEntry.getHash())) throw new IOException("Wrong hash for Entry: " + currentEntry);
                    }
                }
                return ret;
            }

            @Override
            public int read(byte[] b, int off, int len) throws IOException {
                int ret = super.read(b, off, len);
                if (md != null) {
                    if (ret > 0) {
                        md.update(b, off, ret);
                    } else if (ret == -1) {
                        if (!Arrays.equals(md.digest(), currentEntry.getHash())) throw new IOException("Wrong hash for Entry: " + currentEntry);
                    }
                }
                return ret;
            }

            @Override
            public long skip(long n) throws IOException {
                if (n < AWFCInputStream.this.skipBuffer.length) {
                    return read(AWFCInputStream.this.skipBuffer, 0, (int) n);
                } else {
                    return read(AWFCInputStream.this.skipBuffer);
                }
            }

        };
        return currentEntry;
    }

    private AWFCEntry readAWFCEntry() throws IOException {
        int stringSize = 0;
        try {
            stringSize = readShort();
        } catch (EOFException e) {
            return null;
        }
        String path = readString(stringSize);
        if (path.endsWith("/")) {
            return new AWFCEntry(path, 0, null);
        } else {
            long size = readLongOptimized();
            byte[] hash = null;
            if (md != null) {
                hash = ensureRead(md.getDigestLength(), null);
            }
            return new AWFCEntry(path, size, hash);
        }
    }

    private synchronized void readAWFCHeader() throws IOException {
        int version = ensureRead();
        if (version != 1) throw new IOException("Unknown AWFC Version " + version);
        if (readBoolean()) {
            int stringSize = readShort();
            String mdAlgo = readString(stringSize);
            try {
                md = MessageDigest.getInstance(mdAlgo);
            } catch (NoSuchAlgorithmException e) {
                throw new IOException(e);
            }
            if (md.getDigestLength() != readShort()) { throw new IOException("Hashlength does not match for given md: " + mdAlgo); }
        }
        headerRead = true;
    }

    private String readString(int size) throws IOException {
        return new String(ensureRead(size, null), "UTF-8");
    }

    private byte[] ensureRead(int size, byte[] buffer) throws IOException {
        byte[] stringBytes = buffer;
        if (stringBytes == null) {
            stringBytes = new byte[size];
        }
        if (size > stringBytes.length) throw new IOException("buffer too small");
        int done = 0;
        int read = 0;
        while (done < size && (read = getCurrentInputStream().read(stringBytes, done, size - done)) != -1) {
            done += read;
        }
        if (done != size) throw new EOFException();
        return stringBytes;
    }

    private synchronized InputStream getCurrentInputStream() throws IOException {
        if (lis != null) { return lis; }
        return is;
    }

    private int ensureRead() throws IOException {
        int read = getCurrentInputStream().read();
        if (read == -1) throw new EOFException();
        return read;
    }

    private short readShort() throws IOException {
        ensureRead(2, skipBuffer);
        return (short) ((skipBuffer[0] << 8) + (skipBuffer[1] << 0));
    }

    private boolean readBoolean() throws IOException {
        int read = ensureRead();
        if (read == 1) return true;
        if (read == 0) return false;
        throw new IOException("Invalid boolean value!");
    }

    private long readLong() throws IOException {
        ensureRead(8, skipBuffer);
        return (((long) skipBuffer[0] << 56) + ((long) (skipBuffer[1] & 255) << 48) + ((long) (skipBuffer[2] & 255) << 40) + ((long) (skipBuffer[3] & 255) << 32) + ((long) (skipBuffer[4] & 255) << 24) + ((skipBuffer[5] & 255) << 16) + ((skipBuffer[6] & 255) << 8) + ((skipBuffer[7] & 255) << 0));
    }

    private long readLongOptimized() throws IOException {
        int read = ensureRead();
        switch (read) {
        case 1:
            return ((ensureRead() & 255) << 0);
        case 2:
            ensureRead(2, skipBuffer);
            return ((skipBuffer[0] & 255) << 8) + ((skipBuffer[1] & 255) << 0);
        case 3:
            ensureRead(3, skipBuffer);
            return ((skipBuffer[0] & 255) << 16) + ((skipBuffer[1] & 255) << 8) + ((skipBuffer[2] & 255) << 0);
        case 4:
            ensureRead(4, skipBuffer);
            return ((long) (skipBuffer[0] & 255) << 24) + ((skipBuffer[1] & 255) << 16) + ((skipBuffer[2] & 255) << 8) + ((skipBuffer[3] & 255) << 0);
        case 5:
            ensureRead(5, skipBuffer);
            return ((long) (skipBuffer[0] & 255) << 32) + ((long) (skipBuffer[1] & 255) << 24) + ((skipBuffer[2] & 255) << 16) + ((skipBuffer[3] & 255) << 8) + ((skipBuffer[4] & 255) << 0);
        case 8:
            return readLong();
        default:
            throw new IOException("Invalid optimizedLong " + read);
        }
    }

}
