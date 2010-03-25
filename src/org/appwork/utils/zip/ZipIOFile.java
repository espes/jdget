/**
 * Copyright (c) 2009 - 2010 AppWork UG(haftungsbeschränkt) <e-mail@appwork.org>
 * 
 * This file is part of org.appwork.utils.zip
 * 
 * This software is licensed under the Artistic License 2.0,
 * see the LICENSE file or http://www.opensource.org/licenses/artistic-license-2.0.php
 * for details
 */
package org.appwork.utils.zip;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.zip.ZipEntry;

/**
 * @author daniel
 * 
 */
public class ZipIOFile {

    private final String name;
    private final ZipEntry file;
    private final ZipIOReader zipFile;
    private final ZipIOFile parent;
    private final boolean isFile;
    private ArrayList<ZipIOFile> files = new ArrayList<ZipIOFile>();

    protected ZipIOFile(String name, ZipEntry file, ZipIOReader zipFile, ZipIOFile parent) {
        this.name = name;
        this.zipFile = zipFile;
        this.file = file;
        this.parent = parent;
        if (file == null) {
            this.isFile = false;
        } else {
            this.isFile = !file.isDirectory();
        }
    }

    protected final ArrayList<ZipIOFile> getFilesInternal() {
        return files;
    }

    public final ZipIOFile[] getFiles() {
        return files.toArray(new ZipIOFile[files.size()]);
    }

    public final String getName() {
        return name;
    }

    public final boolean isDirectory() {
        return !isFile;
    }

    public final ZipIOFile getParent() {
        return parent;
    }

    public final boolean isFile() {
        return isFile;
    }

    public final String getAbsolutPath() {
        if (file == null) return (parent != null ? parent.getAbsolutPath() : "") + name + "/";
        return file.getName();
    }

    public final long getSize() {
        if (!isFile) return 0;
        return file.getSize();
    }

    public final long getCRC32() {
        if (!isFile) return 0;
        return file.getCrc();
    }

    public final InputStream getInputStream() throws IOException, ZipIOException {
        if (!isFile) return null;
        return zipFile.getInputStream(file);
    }

    @Override
    public String toString() {
        if (isFile) {
            return getAbsolutPath() + name;
        } else {
            return getAbsolutPath();
        }
    }

}
