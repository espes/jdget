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

import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * @author daniel
 * 
 */
public class CountingInputStream extends FilterInputStream implements CountingConnection {
    private long count = 0;

    public CountingInputStream(final InputStream in) {
        super(in);
    }

    @Override
    public int read() throws IOException {
        final int ret = super.read();
        if (ret != -1) {
            this.count++;
        }
        return ret;
    }

    @Override
    public int read(final byte[] b, final int off, final int len) throws IOException {
        final int ret = super.read(b, off, len);
        if (ret != -1) {
            this.count += ret;
        }
        return ret;
    }

    @Override
    public long transferedBytes() {
        return this.count;
    }

}
