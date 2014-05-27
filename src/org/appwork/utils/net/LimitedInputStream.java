/**
 * Copyright (c) 2009 - 2012 AppWork UG(haftungsbeschränkt) <e-mail@appwork.org>
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

/**
 * @author daniel
 * 
 */
public class LimitedInputStream extends CountingInputStream implements StreamValidEOF {

    private final long     limit;
    protected final byte[] skipBuffer = new byte[32767];

    public LimitedInputStream(final InputStream in, final long limit) {
        super(in);
        this.limit = limit;
    }

    public long getLimit() {
        return this.limit;
    }

    @Override
    public int read() throws IOException {
        if (this.transferedBytes() >= this.getLimit()) { return -1; }
        return super.read();
    }

    @Override
    public int read(final byte[] b, final int off, int len) throws IOException {
        final long left = this.getLimit() - this.transferedBytes();
        if (left == 0) { return -1; }
        if (left < len) {
            len = (int) left;
        }
        return super.read(b, off, len);
    }

    @Override
    public long skip(long n) throws IOException {
        if (n < this.skipBuffer.length) {
            return this.read(this.skipBuffer, 0, (int) n);
        } else {
            return this.read(this.skipBuffer);
        }
    }

    @Override
    public boolean isValidEOF() {
        return this.transferedBytes() == this.getLimit();
    }
}
