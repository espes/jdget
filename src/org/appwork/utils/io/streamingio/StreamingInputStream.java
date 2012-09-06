/**
 * Copyright (c) 2009 - 2012 AppWork UG(haftungsbeschränkt) <e-mail@appwork.org>
 * 
 * This file is part of org.appwork.utils.io.streamingio
 * 
 * This software is licensed under the Artistic License 2.0,
 * see the LICENSE file or http://www.opensource.org/licenses/artistic-license-2.0.php
 * for details
 */
package org.appwork.utils.io.streamingio;

import java.io.IOException;
import java.io.InputStream;

/**
 * @author daniel
 * 
 */
public class StreamingInputStream extends InputStream {

    /**
     * 
     */
    public static final String DOWNLOAD_STREAM_IS_CLOSED = "DownloadStream is closed";
    protected final Streaming streaming;
    protected final long      startPosition;
    protected final long      endPosition;
    protected StreamingChunk  currentChunk     = null;
    protected byte[]          singleReadBuffer = new byte[1];
    protected long            currentPosition;

    protected StreamingInputStream(final Streaming streaming, final long startPosition, final long endPosition) {
        this.streaming = streaming;
        this.startPosition = startPosition;
        this.endPosition = endPosition;
        this.currentPosition = startPosition;
    }

    protected int checkEOF(int wishedReadLength) {
        if (this.endPosition < 0) { return wishedReadLength; }
        wishedReadLength = Math.min(wishedReadLength, (int) (this.endPosition - this.currentPosition));
        if (wishedReadLength <= 0) { return -1; }
        return wishedReadLength;
    }

    @Override
    public void close() {
        this.streaming.closeInputStream(this);
    }

    protected StreamingChunk getCurrentChunk() {
        return this.currentChunk;
    }

    public long getCurrentPosition() {
        return this.currentPosition;
    }

    public long getEndPosition() {
        return this.endPosition;
    }

    public long getStartPosition() {
        return this.startPosition;
    }

    @Override
    public void mark(final int readlimit) {
    }

    @Override
    public boolean markSupported() {
        return false;
    }

    @Override
    public int read() throws IOException {
        if (this.currentChunk == null) { throw new IOException("inputstream is closed"); }
        final int len = this.checkEOF(1);
        if (len == -1) { return -1; }
        final int ret = this.streaming.readChunkData(this, this.singleReadBuffer, 0, len);
        if (ret == -1) { return -1; }
        if (ret == 1) {
            this.currentPosition += 1;
            return this.singleReadBuffer[0];
        }
        throw new IOException("unknown IOException, ret=" + ret);
    }

    @Override
    public int read(final byte[] b, final int off, int len) throws IOException {
        if (this.currentChunk == null) {
            //
            throw new IOException(DOWNLOAD_STREAM_IS_CLOSED);
        }
        len = this.checkEOF(len);
        if (len == -1) { return -1; }
        final int ret = this.streaming.readChunkData(this, b, off, len);
        if (ret == -1) { return -1; }
        this.currentPosition += ret;
        return ret;
    }

    @Override
    public void reset() throws IOException {
        throw new IOException("mark/reset not supported");
    }

    protected void setCurrentChunk(final StreamingChunk currentChunk) {
        this.currentChunk = currentChunk;
    }

}
