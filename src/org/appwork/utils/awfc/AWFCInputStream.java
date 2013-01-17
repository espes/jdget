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

import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;

import org.appwork.utils.net.LimitedInputStream;

/**
 * @author Daniel Wilhelm
 * 
 */
public class AWFCInputStream extends InputStream {

    private final InputStream  is;
    private LimitedInputStream lis              = null;
    private MessageDigest      md               = null;
    private boolean            headerRead       = false;
    private AWFCEntryOptions   currentEntry     = null;
    private byte[]             currentEntryHash = null;
    private final byte[]       skipBuffer       = new byte[32767];
    private AWFCUtils          utils;

    public AWFCInputStream(final InputStream is) {
        this.is = is;
        this.utils = new AWFCUtils() {

            @Override
            public InputStream getCurrentInputStream() throws IOException {
                return AWFCInputStream.this.getCurrentInputStream();
            }

        };
    }

    @Override
    public int available() throws IOException {
        return this.getCurrentInputStream().available();
    }

    @Override
    public void close() throws IOException {
        this.getCurrentInputStream().close();
    }

    private synchronized InputStream getCurrentInputStream() throws IOException {
        if (this.lis != null) { return this.lis; }
        return this.is;
    }

    public synchronized AWFCEntry getNextEntry() throws IOException {
        if (this.headerRead == false) {
            this.readAWFCHeader();
        }
        if (this.currentEntry != null) {
            while (this.lis.available() > 0) {
                this.lis.skip(this.lis.available());
            }
            this.lis = null;
            this.currentEntry = null;
            this.currentEntryHash = null;
        }
        this.currentEntry = this.readAWFCEntry();
        if (this.currentEntry == null) { return null; }
        if (this.md != null) {
            this.md.reset();
        }
        long inputLimit = this.currentEntry.getEntry().getSize();
        final boolean hasPayLoad = this.currentEntry.hasPayLoad();
        if (hasPayLoad == false) {
            inputLimit = 0;
        }
        this.lis = new LimitedInputStream(this.is, inputLimit) {

            @Override
            public int available() throws IOException {
                final long ret = this.getLimit() - this.transferedBytes();
                if (ret > Integer.MAX_VALUE) { return Integer.MAX_VALUE; }
                return (int) ret;
            }

            @Override
            public int read() throws IOException {
                final int ret = super.read();
                if (hasPayLoad && AWFCInputStream.this.md != null) {
                    if (ret != -1) {
                        AWFCInputStream.this.md.update((byte) ret);
                    } else if (ret == -1) {
                        if (AWFCInputStream.this.currentEntryHash == null) {
                            AWFCInputStream.this.currentEntryHash = AWFCInputStream.this.md.digest();
                        }
                        if (!Arrays.equals(AWFCInputStream.this.currentEntryHash, AWFCInputStream.this.currentEntry.getEntry().getHash())) { throw new IOException("Wrong hash for Entry: " + AWFCInputStream.this.currentEntry.getEntry()); }
                    }
                }
                return ret;
            }

            @Override
            public int read(final byte[] b, final int off, final int len) throws IOException {
                final int ret = super.read(b, off, len);
                if (hasPayLoad && AWFCInputStream.this.md != null) {
                    if (ret > 0) {
                        AWFCInputStream.this.md.update(b, off, ret);
                    } else if (ret == -1) {
                        if (AWFCInputStream.this.currentEntryHash == null) {
                            AWFCInputStream.this.currentEntryHash = AWFCInputStream.this.md.digest();
                        }
                        if (!Arrays.equals(AWFCInputStream.this.currentEntryHash, AWFCInputStream.this.currentEntry.getEntry().getHash())) { throw new IOException("Wrong hash for Entry: " + AWFCInputStream.this.currentEntry.getEntry()); }
                    }
                }
                return ret;
            }

            @Override
            public long skip(final long n) throws IOException {
                if (n < AWFCInputStream.this.skipBuffer.length) {
                    return this.read(AWFCInputStream.this.skipBuffer, 0, (int) n);
                } else {
                    return this.read(AWFCInputStream.this.skipBuffer, 0, this.skipBuffer.length);
                }
            }

        };
        return this.currentEntry.getEntry();
    }

    @Override
    public synchronized void mark(final int readlimit) {
    }

    @Override
    public boolean markSupported() {
        return false;
    }

    @Override
    public int read() throws IOException {
        return this.getCurrentInputStream().read();
    }

    @Override
    public int read(final byte[] b) throws IOException {
        return this.getCurrentInputStream().read(b);
    }

    @Override
    public int read(final byte[] b, final int off, final int len) throws IOException {
        return this.getCurrentInputStream().read(b, off, len);
    }

    private AWFCEntryOptions readAWFCEntry() throws IOException {
        int stringSize = 0;
        try {
            stringSize = this.utils.readShort();
        } catch (final EOFException e) {
            return null;
        }
        final String path = this.utils.readString(stringSize);
        final int entryOptions = this.utils.ensureRead();
        final boolean isFolder = (entryOptions & 1) == 0;
        final boolean hasPayLoad = (entryOptions & 2) == 0;
        AWFCEntry entry = null;
        if (isFolder) {
            entry = new AWFCEntry(path + "/", 0, null);
        } else {
            final long size = this.utils.readLongOptimized();
            byte[] hash = null;
            if (this.md != null) {
                hash = this.utils.ensureRead(this.md.getDigestLength(), null);
            }
            entry = new AWFCEntry(path, size, hash);
        }
        return new AWFCEntryOptions(entry, !hasPayLoad);
    }

    private synchronized void readAWFCHeader() throws IOException {
        final int version = this.utils.ensureRead();
        if (version != 1) { throw new IOException("Unknown AWFC Version " + version); }
        if (this.utils.readBoolean()) {
            final int stringSize = this.utils.readShort();
            final String mdAlgo = this.utils.readString(stringSize);
            try {
                this.md = MessageDigest.getInstance(mdAlgo);
            } catch (final NoSuchAlgorithmException e) {
                throw new IOException(e);
            }
            if (this.md.getDigestLength() != this.utils.readShort()) { throw new IOException("Hashlength does not match for given md: " + mdAlgo); }
        }
        this.headerRead = true;
    }

    @Override
    public synchronized void reset() throws IOException {
    }

    @Override
    public long skip(final long n) throws IOException {
        return this.getCurrentInputStream().skip(n);
    }

}
