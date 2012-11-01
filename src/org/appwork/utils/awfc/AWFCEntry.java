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

    public AWFCEntry(String path, long size, byte[] hash) {
        if (path.endsWith("/")) {
            isFile = false;
            size = 0;
        } else {
            size = Math.max(0, size);
            isFile = true;
        }
        this.path = path;
        this.size = size;
        this.hash = hash;
    }

    public long getSize() {
        return size;
    }

    public String getPath() {
        return path;
    }

    public boolean isFile() {
        return isFile;
    }

    public byte[] getHash() {
        return hash;
    }

    @Override
    public String toString() {
        return "AWFCEntry [path=" + path + ", isFile=" + isFile + ", size=" + size + "]";
    }

}
