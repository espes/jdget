/**
 * Copyright (c) 2009 - 2010 AppWork UG(haftungsbeschränkt) <e-mail@appwork.org>
 * 
 * This file is part of org.appwork.utils.net
 * 
 * This software is licensed under the Artistic License 2.0,
 * see the LICENSE file or http://www.opensource.org/licenses/artistic-license-2.0.php
 * for details
 */
package org.appwork.utils.net;

import java.io.IOException;
import java.io.OutputStream;

/**
 * @author daniel
 * 
 */
public class CountingOutputStream extends OutputStream {

    private OutputStream os      = null;
    private long         written = 0;

    public CountingOutputStream(OutputStream os) {
        this.os = os;
    }

    @Override
    public void write(int b) throws IOException {
        os.write(b);
        written++;
    }

    @Override
    public void write(byte b[]) throws IOException {
        os.write(b);
        written += b.length;
    }

    @Override
    public void write(byte b[], int off, int len) throws IOException {
        os.write(b, off, len);
        written += len;
    }

    public long bytesWritten() {
        return written;
    }

}
