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
public class LimitedInputStream extends CountingInputStream {

    private final long limit;

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

}
