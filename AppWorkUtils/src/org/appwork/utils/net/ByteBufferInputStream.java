/**
 * Copyright (c) 2009 - 2011 AppWork UG(haftungsbeschränkt) <e-mail@appwork.org>
 * 
 * This file is part of org.appwork.utils.net
 * 
 * This software is licensed under the Artistic License 2.0,
 * see the LICENSE file or http://www.opensource.org/licenses/artistic-license-2.0.php
 * for details
 */
package org.appwork.utils.net;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;

/**
 * @author daniel
 * 
 */
public class ByteBufferInputStream extends InputStream {

    private final ByteBuffer buffer;

    public ByteBufferInputStream(final ByteBuffer buffer) {
        this.buffer = buffer;
    }

    @Override
    public int available() throws IOException {
        return this.buffer.remaining();
    }

    @Override
    public synchronized int read() throws IOException {
        if (!this.buffer.hasRemaining()) { return -1; }
        return this.buffer.get();
    }

    @Override
    public synchronized int read(final byte[] bytes, final int off, int len) throws IOException {
        if (!this.buffer.hasRemaining()) { return -1; }
        len = Math.min(len, this.buffer.remaining());
        this.buffer.get(bytes, off, len);
        return len;
    }

}
