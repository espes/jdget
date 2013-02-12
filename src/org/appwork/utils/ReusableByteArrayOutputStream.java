/**
 * Copyright (c) 2009 - 2013 AppWork UG(haftungsbeschränkt) <e-mail@appwork.org>
 * 
 * This file is part of org.appwork.utils
 * 
 * This software is licensed under the Artistic License 2.0,
 * see the LICENSE file or http://www.opensource.org/licenses/artistic-license-2.0.php
 * for details
 */
package org.appwork.utils;

import java.io.ByteArrayOutputStream;

/**
 * @author daniel
 * 
 */
public class ReusableByteArrayOutputStream extends ByteArrayOutputStream {

    public ReusableByteArrayOutputStream(final int size) {
        super(size);
    }

    public int bufferSize() {
        return this.buf.length;
    }

    public synchronized int free() {
        return this.buf.length - this.count;
    }

    public byte[] getInternalBuffer() {
        return this.buf;
    }

    public synchronized void increaseUsed(final int increase) {
        this.count = this.count + increase;
    }

    public synchronized void setUsed(final int used) {
        this.count = used;
    }

}
