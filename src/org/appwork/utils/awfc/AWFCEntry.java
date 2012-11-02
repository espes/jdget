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

    private byte[]        hash = null;
    private final String  path;
    private final boolean isFile;
    private long          size = -1;

    public AWFCEntry(final String path, long size, final byte[] hash) {
        if (path.endsWith("/")) {
            this.isFile = false;
            size = 0;
        } else {
            size = Math.max(0, size);
            this.isFile = true;
        }
        this.path = path;
        this.size = size;
        this.hash = hash;
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
