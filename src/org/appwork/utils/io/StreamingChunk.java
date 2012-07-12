/**
 * Copyright (c) 2009 - 2012 AppWork UG(haftungsbeschränkt) <e-mail@appwork.org>
 * 
 * This file is part of org.appwork.utils.io
 * 
 * This software is licensed under the Artistic License 2.0,
 * see the LICENSE file or http://www.opensource.org/licenses/artistic-license-2.0.php
 * for details
 */
package org.appwork.utils.io;

/**
 * @author daniel
 *
 */

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.concurrent.atomic.AtomicLong;

public class StreamingChunk {

    protected RandomAccessFile chunkFile = null;
    protected volatile boolean canGrow   = false;
    protected AtomicLong       writes    = new AtomicLong(0);

    public StreamingChunk(final File file) throws FileNotFoundException {
        this(file, false);
    }

    public StreamingChunk(final File file, final boolean canGrow) throws FileNotFoundException {
        if (canGrow) {
            this.chunkFile = new RandomAccessFile(file, "rw");
        } else {
            this.chunkFile = new RandomAccessFile(file, "r");
        }
        this.canGrow = canGrow;
    }

    private synchronized int _read(final byte[] b, final long position) throws IOException {
        if (position < this.chunkFile.length()) {
            this.chunkFile.seek(position);
            return this.chunkFile.read(b);
        } else if (this.canGrow == false) {
            return -1;
        } else {
            return 0;
        }
    }

    public boolean canGrow() {
        return this.canGrow;
    }

    public void close() {
        try {
            this.chunkFile.close();
        } catch (final Throwable e) {
        }
    }

    protected void disableCanGrow() {
        this.canGrow = false;
    }

    public int read(final byte[] b, final long position) throws IOException, InterruptedException {
        if (position < 0) { throw new IOException("invalid position " + position); }
        final long lastWrites = this.writes.get();
        int ret = this._read(b, position);
        if (ret > 0 || ret == -1) { return ret; }
        while (true) {
            Thread.sleep(50);
            if (lastWrites != this.writes.get()) {
                ret = this._read(b, position);
                if (ret > 0 || ret == -1) { return ret; }
            }
            if (this.canGrow == false) { return -1; }
        }
    }

    public synchronized void write(final byte[] b) throws IOException {
        if (this.canGrow == false) { throw new IOException("canGrow == false"); }
        if (this.chunkFile.getFilePointer() != this.chunkFile.length()) {
            this.chunkFile.seek(this.chunkFile.length());
        }
        this.chunkFile.write(b);
        this.writes.incrementAndGet();
    }

}
