/**
 * Copyright (c) 2009 - 2014 AppWork UG(haftungsbeschränkt) <e-mail@appwork.org>
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
public final class EmptyInputStream extends InputStream {

    @Override
    public final int read() throws IOException {
        return -1;
    }

    @Override
    public final int read(final byte[] b) throws IOException {
        return -1;
    }

    @Override
    public final int read(final byte[] b, final int off, final int len) throws IOException {
        return -1;
    };
}
