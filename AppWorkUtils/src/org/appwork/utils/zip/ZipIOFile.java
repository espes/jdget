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

import java.io.File;
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

    /**
     * constructor for ZipIOFile
     * 
     * @param name
     *            name of the ZipIOFile node
     * @param file
     *            ZipEntry that represents the given File
     * @param zipFile
     *            ZipIOReader for this ZipIOFile
     * @param parent
     *            ZipIOFile that represents the parent of this node or null if
     *            no parent available
     */
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

    /**
     * returns ZipIOFile list for all the filesif this ZipIOFile represents a
     * directory(internal use for ZipIOReader)
     * 
     * @return
     */
    protected final ArrayList<ZipIOFile> getFilesInternal() {
        return files;
    }

    /**
     * returns ZipIOFile list for all the filesif this ZipIOFile represents a
     * directory
     * 
     * @return
     */
    public final ZipIOFile[] getFiles() {
        return files.toArray(new ZipIOFile[files.size()]);
    }

    /**
     * returns the name of this ZipIOFile
     * 
     * @return
     */
    public final String getName() {
        return name;
    }

    /**
     * is this ZipIOFile a directory
     * 
     * @return
     */
    public final boolean isDirectory() {
        return !isFile;
    }

    /**
     * returns parent ZipIOFile
     * 
     * @return
     */
    public final ZipIOFile getParent() {
        return parent;
    }

    /**
     * is this ZipIOFile a file
     * 
     * @return
     */
    public final boolean isFile() {
        return isFile;
    }

    /**
     * returns the absolutepath of this ZipIOFile
     * 
     * @return
     */
    public final String getAbsolutePath() {
        if (file == null) return (parent != null ? parent.getAbsolutePath() : "") + name + "/";
        return file.getName();
    }

    /**
     * returns filesize for this ZipIOFile
     * 
     * @return filesize or 0 if this ZipIOFile is a directory
     */
    public final long getSize() {
        if (!isFile) return 0;
        return file.getSize();
    }

    /**
     * returns CRC32 for this ZipIOFile
     * 
     * @return CRC32 or 0 if this ZipIOFile is a directory
     */
    public final long getCRC32() {
        if (!isFile) return 0;
        return file.getCrc();
    }

    /**
     * returns InputStream for this ZipIOFile
     * 
     * @return InputStream or null if this ZipIOFile is a directory
     */
    public final InputStream getInputStream() throws IOException, ZipIOException {
        if (!isFile) return null;
        return zipFile.getInputStream(file);
    }

    @Override
    public String toString() {
        return getAbsolutePath();
    }

    /**
     * extracts this ZipIOFile to given output File
     * 
     * @param output
     *            File to extract this ZipIOFile to
     * @throws ZipIOException
     * @throws IOException
     */
    public void extract(File output) throws ZipIOException, IOException {
        if (isFile) {
            zipFile.extract(file, output);
        } else {
            throw new ZipIOException("Cannot extract a directory", file);
        }
    }

}
