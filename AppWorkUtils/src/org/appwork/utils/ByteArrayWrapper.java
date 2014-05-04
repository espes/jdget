/**
 * Copyright (c) 2009 - 2014 AppWork UG(haftungsbeschränkt) <e-mail@appwork.org>
 * 
 * This file is part of org.appwork.utils
 * 
 * This software is licensed under the Artistic License 2.0,
 * see the LICENSE file or http://www.opensource.org/licenses/artistic-license-2.0.php
 * for details
 */
package org.appwork.utils;

import java.io.IOException;

/**
 * @author daniel
 * 
 */
public final class ByteArrayWrapper {

    private final byte[] byteArray;
    private final int    offset;
    private final int    length;
    private final int    hashCode;

    public ByteArrayWrapper(final byte[] byteArray, final boolean clone) {
        this(byteArray, 0, byteArray.length, clone);
    }

    public ByteArrayWrapper(final byte[] byteArray, final int offset, final int length, final boolean clone) {
        if (clone) {
            this.byteArray = new byte[length];
            System.arraycopy(byteArray, offset, this.byteArray, 0, length);
            this.offset = 0;
            this.length = byteArray.length;
        } else {
            this.byteArray = byteArray;
            this.offset = offset;
            this.length = length;
        }
        this.hashCode = this.calcHashCode(this.byteArray, this.offset, this.length);
    }

    private final int calcHashCode(final byte[] byteArray, final int offset, final int length) {
        int result = 1;
        for (int index = offset; index < length; index++) {
            result = 31 * result + byteArray[index];
        }

        return result;
    }

    @Override
    public final boolean equals(final Object obj) {
        if (obj == null || !(obj instanceof ByteArrayWrapper) || this.hashCode() != obj.hashCode()) { return false; }
        if (obj == this) { return true; }
        final ByteArrayWrapper other = (ByteArrayWrapper) obj;
        if (other.getLength() != this.getLength()) { return false; }
        int index2 = other.getOffset();
        for (int index = this.getOffset(); index < this.getLength(); index++) {
            if (this.getByteArray()[index] != other.getByteArray()[index2]) { return false; }
            index2++;
        }
        return true;
    }

    public final byte[] getByteArray() {
        return this.byteArray;
    }

    public final int getLength() {
        return this.length;
    }

    public final int getOffset() {
        return this.offset;
    }

    @Override
    public final int hashCode() {
        return this.hashCode;
    }

    @Override
    public String toString() {
        return new String(this.byteArray, this.offset, this.length);
    }

    public String toString(final String charset) throws IOException {
        return new String(this.byteArray, this.offset, this.length, charset);
    }
}
