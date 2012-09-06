/**
 * Copyright (c) 2009 - 2012 AppWork UG(haftungsbeschränkt) <e-mail@appwork.org>
 * 
 * This file is part of org.appwork.utils.io
 * 
 * This software is licensed under the Artistic License 2.0,
 * see the LICENSE file or http://www.opensource.org/licenses/artistic-license-2.0.php
 * for details
 */
package org.appwork.utils.io.streamingio;

/**
 * @author daniel
 *
 */

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.util.concurrent.atomic.AtomicLong;

public class StreamingChunk {

    protected RandomAccessFile chunkFile        = null;
    protected volatile boolean canGrow          = false;
    protected AtomicLong       writes           = new AtomicLong(0);
    protected AtomicLong       currentChunkSize = new AtomicLong(0);
    protected final long       chunkStartPosition;

    public StreamingChunk(final File file, final long chunkStartPosition) throws FileNotFoundException {
        this.chunkFile = new RandomAccessFile(file, "rw");
        this.currentChunkSize.set(file.length());
        this.chunkStartPosition = chunkStartPosition;
    }

    private synchronized int _read(final byte b[], final int off, final int len, long position) throws IOException {
        position = position - this.chunkStartPosition;
        if (position < this.currentChunkSize.get()) {
            this.chunkFile.seek(position);
            return this.chunkFile.read(b, off, (int) Math.min(len, this.currentChunkSize.get() - position));
        } else if (this.canGrow == false) {
            return -1;
        } else {
            return 0;
        }
    }

    /**
     * can this StreamingChunk grow, that means is there someone writing to it
     * 
     * @return
     */
    public boolean canGrow() {
        return this.canGrow;
    }

    /**
     * close this StreamingChunk (its RandomAccessFile)
     */
    public void close() {
        try {
            this.chunkFile.close();
        } catch (final Throwable e) {
        }
    }

    /**
     * how many bytes does this chunk contain
     * 
     * @return
     */
    public long getAvailableChunkSize() {
        return this.currentChunkSize.get();
    }

    /**
     * return the startPosition of this chunk
     * 
     * @return
     */
    public long getChunkStartPosition() {
        return this.chunkStartPosition;
    }

    /**
     * read bytes from this chunk at given position
     * 
     * @param b
     * @param off
     * @param len
     * @param position
     * @return
     * @throws IOException
     * @throws InterruptedException
     */
    public int read(final byte b[], final int off, final int len, final long position) throws IOException, InterruptedException {
        if (position < 0) { throw new IOException("invalid position " + position); }
        final long lastWrites = this.writes.get();
        int ret = this._read(b, off, len, position);
        if (ret > 0 || ret == -1) {

            //
            return ret;

        }
        while (true) {
            Thread.sleep(50);

            if (lastWrites != this.writes.get()) {
                ret = this._read(b, off, len, position);
                if (ret > 0 || ret == -1) { return ret; }
            }
            if (this.canGrow == false) {
                //
                return -1;
            }
        }
    }

    /**
     * changes canGrow on this chunk
     * 
     * @param b
     */
    public void setCanGrow(final boolean b) {
        this.canGrow = b;
    }

    /**
     * flush the buffer to disk!
     */
    public synchronized void sync() {
        try {
            this.chunkFile.getFD().sync();
        } catch (final Throwable e) {
        }
    }

    /**
     * write bytes to this chunk at given position
     * 
     * @param b
     * @param off
     * @param len
     * @throws IOException
     */
    public synchronized void write(final byte b[], final int off, final int len) throws IOException {
        if (this.chunkFile.getFilePointer() != this.chunkFile.length()) {
            this.chunkFile.seek(this.chunkFile.length());
        }
        this.chunkFile.write(b, off, len);
        this.currentChunkSize.addAndGet(len);
        this.writes.incrementAndGet();
    }

    /**
     * @param start
     * @param end
     * @return
     */
    public InputStream getInputStream(final long startPosition, long end) {

        return new InputStream() {
            long   currentPosition = startPosition;
            byte[] bufferByte      = new byte[1];

            @Override
            public int read() throws IOException {

                if (currentPosition != 0 && currentPosition > getAvailableChunkSize()) return -1;
                int ret = read(bufferByte, 0, 1);
                if (ret == 1) return bufferByte[1];
                return -1;
            }

            @Override
            public void close() throws IOException {
                super.close();
            }

            @Override
            public int read(byte[] b, int off, int len) throws IOException {

                if (currentPosition != 0 && currentPosition > StreamingChunk.this.getAvailableChunkSize()) {
                    System.out.println("-1 answer");
                    return -1;
                }
                try {
                    int ret;

                    ret = StreamingChunk.this.read(b, off, len, currentPosition);
                    if (ret >= 0) currentPosition += ret;

                    return ret;
                } catch (InterruptedException e) {
                    e.printStackTrace();
                    return -1;
                }

            }

            @Override
            public boolean markSupported() {
                return false;
            }
        };
    }

}
