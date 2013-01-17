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
     * Appwork FileContainer *
     */
    private final OutputStream   os;
    private AWFCEntryOptions     currentEntry                = null;
    private CountingOutputStream currentCountingOutputStream = null;
    private final MessageDigest  md;
    private boolean              headerWritten               = false;
    private final AWFCUtils      utils;
    private boolean              closing                     = false;

    public AWFCOutputStream(final OutputStream os, final MessageDigest md) {
        this.os = os;
        this.md = md;
        this.utils = new AWFCUtils() {

            @Override
            public OutputStream getCurrentOutputStream() throws IOException {
                return AWFCOutputStream.this.getCurrentOutputStream();
            }

        };
    }

    @Override
    public synchronized void close() throws IOException {
        if (this.headerWritten == false) {
            this.closing = true;
            this.writeAWFCHeader();
        }
        this.getCurrentOutputStream().close();
    }

    private synchronized void closeLastEntry() throws IOException {
        if (this.currentEntry != null) {
            /* verify if currentEntry is complete */
            final long bytesWritten = this.currentCountingOutputStream.transferedBytes();
            final AWFCEntry entry = this.currentEntry.getEntry();
            if (this.currentEntry.hasPayLoad()) {
                if (entry.getSize() != bytesWritten) { throw new IOException("Wrong size for Entry: " + entry + " != " + bytesWritten); }
                if (entry.isFile() && this.md != null && !Arrays.equals(entry.getHash(), this.md.digest())) { throw new IOException("Wrong hash for Entry: " + entry); }
            } else {
                if (bytesWritten > 0) { throw new IOException("Entry must not have payLoad: " + entry + " != " + bytesWritten); }
            }
            /* we want to write on original OutputStream again */
            this.currentCountingOutputStream = null;
            this.currentEntry = null;
        } else {
            throw new IOException("No lastEntry to close!");
        }
    }

    @Override
    public synchronized void flush() throws IOException {
        this.getCurrentOutputStream().flush();
    }

    protected synchronized OutputStream getCurrentOutputStream() throws IOException {
        if (this.currentCountingOutputStream != null) { return this.currentCountingOutputStream; }
        if (this.currentEntry != null) { return this.os; }
        if (this.closing) { return this.os; }
        throw new IOException("No Entry added yet!");
    }

    public synchronized void putNextEntry(final AWFCEntry e) throws IOException {
        this.putNextEntry(e, false);
    }

    public synchronized void putNextEntry(final AWFCEntry entry, boolean noPayLoad) throws IOException {
        if (this.currentEntry != null) {
            this.closeLastEntry();
        }
        if (entry.isFile() == false) {
            /* folders do not have any payload */
            noPayLoad = true;
        }
        /* wrap AWFCEntry into AWFCEntryOptions */
        this.currentEntry = new AWFCEntryOptions(entry, noPayLoad);
        if (this.headerWritten == false) {
            this.writeAWFCHeader();
        }
        /* write AWFCHeader */
        this.writeAWFCEntry(this.currentEntry);
        if (this.md != null) {
            this.md.reset();
        }
        this.currentCountingOutputStream = new CountingOutputStream(this.os) {

            @Override
            public void close() throws IOException {
                try {
                    AWFCOutputStream.this.closeLastEntry();
                } finally {
                    super.close();
                }
            }

            @Override
            public void write(final byte[] b) throws IOException {
                super.write(b);
                if (AWFCOutputStream.this.md != null) {
                    AWFCOutputStream.this.md.update(b);
                }
            }

            @Override
            public void write(final byte[] b, final int off, final int len) throws IOException {
                super.write(b, off, len);
                if (AWFCOutputStream.this.md != null) {
                    AWFCOutputStream.this.md.update(b, off, len);
                }
            }

            @Override
            public void write(final int b) throws IOException {
                super.write(b);
                if (AWFCOutputStream.this.md != null) {
                    AWFCOutputStream.this.md.update((byte) b);
                }
            }

        };

    }

    @Override
    public synchronized void write(final byte b[]) throws IOException {
        this.getCurrentOutputStream().write(b);
    }

    @Override
    public synchronized void write(final byte[] b, final int off, final int len) throws IOException {
        this.getCurrentOutputStream().write(b, off, len);
    }

    @Override
    public synchronized void write(final int b) throws IOException {
        this.getCurrentOutputStream().write(b);
    }

    private void writeAWFCEntry(final AWFCEntryOptions awfcEntryOptions) throws IOException {
        final AWFCEntry entry = awfcEntryOptions.getEntry();
        /* write filePath */
        this.utils.writeString(entry.getPath());
        int entryOptions = 0;
        if (entry.isFile()) {
            /* entry is a File */
            entryOptions = entryOptions | 1;
        }
        if (awfcEntryOptions.hasPayLoad() == false) {
            /* entry has no payLoad */
            entryOptions = entryOptions | 2;
        }
        /* write entryOptions */
        this.write(entryOptions);
        if (entry.isFile()) {
            /* write entrySize */
            this.utils.writeLongOptimized(entry.getSize());
            if (this.md != null) {
                /* only write Hash when MessageDigest is set */
                if (entry.getHash() == null) { throw new IOException("Hash is missing for Entry: " + entry); }
                if (entry.getHash().length != this.md.getDigestLength()) { throw new IOException("Hashlength does not match for Entry: " + entry); }
                this.write(entry.getHash());
            }
        }
    }

    private synchronized void writeAWFCHeader() throws IOException {
        this.headerWritten = true;
        /* write AWFC Version 1 */
        this.write(1);
        /* write MessageDigest set */
        this.utils.writeBoolean(this.md != null);
        if (this.md != null) {
            /* write MessageDigest Type */
            this.utils.writeString(this.md.getAlgorithm());
            /* write MessageDigest Length */
            this.utils.writeShort(this.md.getDigestLength());
        }
    }

}
