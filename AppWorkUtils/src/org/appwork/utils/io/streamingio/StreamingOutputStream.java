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
import java.io.OutputStream;

/**
 * @author daniel
 * 
 */
public class StreamingOutputStream extends OutputStream {

    protected Streaming      streaming;
    protected byte[]         singleWriteBuffer = new byte[1];
    protected StreamingChunk currentChunk      = null;

    protected StreamingOutputStream(final Streaming streaming) {
        this.streaming = streaming;
    }

    @Override
    public void close() {
        this.streaming.closeOutputStream(this);
    }

    @Override
    public void flush() throws IOException {
        final StreamingChunk lcurrentChunk = this.currentChunk;
        if (lcurrentChunk == null) { throw new IOException("outputstream is closed!"); }
        lcurrentChunk.sync();
    }

    protected StreamingChunk getCurrentChunk() {
        return this.currentChunk;
    }

    public void setCurrentChunk(final StreamingChunk currentChunk) {
        this.currentChunk = currentChunk;
    }

    @Override
    public void write(final byte[] b, final int off, final int len) throws IOException {
        if (this.currentChunk == null) {
            //
            throw new IOException("outputstream is closed!"); }
        this.streaming.writeChunkData(this, b, off, len);
    }

    @Override
    public void write(final int b) throws IOException {
        if (this.currentChunk == null) { throw new IOException("outputstream is closed!"); }
        this.singleWriteBuffer[0] = (byte) b;
        this.streaming.writeChunkData(this, this.singleWriteBuffer, 0, 1);
    }

}
