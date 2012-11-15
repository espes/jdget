/**
 * Copyright (c) 2009 - 2012 AppWork UG(haftungsbeschränkt) <e-mail@appwork.org>
 * 
 * This file is part of org.appwork.utils.awf
 * 
 * This software is licensed under the Artistic License 2.0,
 * see the LICENSE file or http://www.opensource.org/licenses/artistic-license-2.0.php
 * for details
 */
package org.appwork.utils.awfc;

/**
 * @author Daniel Wilhelm
 * 
 */
public class AWFCEntry {

    private final byte[]  hash;
    private final String  path;
    private final boolean isFile;
    private final long    size;

    public AWFCEntry(final String path, final long size, final byte[] hash) {
        if (path.endsWith("/")) {
            this.path = path.substring(0, path.length() - 1);
            this.isFile = false;
            this.size = 0;
            this.hash = null;
        } else {
            if (size < 0) { throw new IllegalArgumentException("Size must be >=0"); }
            this.size = size;
            this.isFile = true;
            this.path = path;
            this.hash = hash;
        }
    }

    public byte[] getHash() {
        return this.hash;
    }

    public String getPath() {
        return this.path;
    }

    public long getSize() {
        return this.size;
    }

    public boolean isFile() {
        return this.isFile;
    }

    @Override
    public String toString() {
        return "AWFCEntry [path=" + this.path + ", isFile=" + this.isFile + ", size=" + this.size + "]";
    }

}
