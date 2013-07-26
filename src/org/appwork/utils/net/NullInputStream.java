/**
 * Copyright (c) 2009 - 2013 AppWork UG(haftungsbeschränkt) <e-mail@appwork.org>
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
public class NullInputStream extends InputStream {

    @Override
    public int read() throws IOException {
        return 0;
    }

    @Override
    public int read(final byte[] b) throws IOException {
        return b.length;
    }

    @Override
    public int read(final byte[] b, final int off, final int len) throws IOException {
        return len;
    }

}
