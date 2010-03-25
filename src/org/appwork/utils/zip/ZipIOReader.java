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
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipException;
import java.util.zip.ZipFile;

/**
 * @author daniel
 * 
 */
public class ZipIOReader {

    private File zipFile = null;
    private ZipFile zip = null;
    private ArrayList<ZipIOFile> root = null;

    public ZipIOReader(File zipFile) throws ZipIOException, ZipException, IOException {
        this.zipFile = zipFile;
        openZip();
    }

    private void openZip() throws ZipIOException, ZipException, IOException {
        if (zip != null) return;
        if (zipFile == null || zipFile.isDirectory() || !zipFile.exists()) throw new ZipIOException("invalid zipFile");
        this.zip = new ZipFile(zipFile);
    }

    public synchronized void close() throws IOException {
        try {
            if (zip != null) {
                zip.close();
            }
        } finally {
            zip = null;
        }
    }

    public synchronized ZipEntry[] getZipFiles() throws ZipIOException {
        ArrayList<ZipEntry> ret = new ArrayList<ZipEntry>();
        Enumeration<? extends ZipEntry> zipIter = zip.entries();
        while (zipIter.hasMoreElements()) {
            ret.add(zipIter.nextElement());
        }
        return ret.toArray(new ZipEntry[ret.size()]);
    }

    public synchronized int size() throws ZipIOException {
        return zip.size();
    }

    public synchronized InputStream getInputStream(ZipEntry entry) throws ZipIOException, IOException {
        if (entry == null) throw new ZipIOException("invalid zipEntry");
        return zip.getInputStream(entry);
    }

    public synchronized ZipEntry getZipFile(String fileName) throws ZipIOException {
        if (fileName == null) throw new ZipIOException("invalid fileName");
        return zip.getEntry(fileName);
    }

    protected void finalize() throws IOException {
        close();
    }

    public synchronized ZipIOFile[] getZipIOFileSystem() throws ZipIOException {
        if (root != null) return root.toArray(new ZipIOFile[root.size()]);
        ZipEntry[] content = getZipFiles();
        root = new ArrayList<ZipIOFile>();
        for (ZipEntry file : content) {
            if (!file.isDirectory() && !file.getName().contains("/")) {
                /* file is in root */
                ZipIOFile tmp = new ZipIOFile(file.getName(), file, this, null);
                root.add(tmp);
            } else if (!file.isDirectory()) {
                /* file is not in root */
                String parts[] = file.getName().split("/");
                /* we begin at root */
                ZipIOFile currentParent = null;
                String path = "";
                for (int i = 0; i < parts.length; i++) {
                    if (i == parts.length - 1) {
                        /* the file */
                        ZipIOFile tmp = new ZipIOFile(parts[i], file, this, currentParent);
                        currentParent.getFilesInternal().add(tmp);
                    } else {
                        path = path + parts[i] + "/";
                        ZipIOFile found = null;
                        for (ZipIOFile tmp : root) {
                            found = getFolder(path, tmp);
                            if (found != null) break;
                        }
                        if (found != null) {
                            currentParent = found;
                        } else {
                            ZipIOFile newFolder = new ZipIOFile(parts[i], null, this, currentParent);
                            if (currentParent != null) {
                                currentParent.getFilesInternal().add(newFolder);
                            } else {
                                root.add(newFolder);
                            }
                            currentParent = newFolder;

                        }
                    }
                }
            }
        }
        return root.toArray(new ZipIOFile[root.size()]);
    }

    private ZipIOFile getFolder(String path, ZipIOFile currentRoot) {
        if (!currentRoot.isDirectory()) return null;
        if (currentRoot.getAbsolutPath().equalsIgnoreCase(path)) return currentRoot;
        for (ZipIOFile tmp : currentRoot.getFiles()) {
            if (tmp.isDirectory() && tmp.getAbsolutPath().equalsIgnoreCase(path)) {
                return tmp;
            } else if (tmp.isDirectory()) {
                ZipIOFile ret = getFolder(path, tmp);
                if (ret != null) return ret;
            }
        }
        return null;
    }

    public static void main(String[] args) throws ZipIOException, IOException {
        ZipIOReader zip = new ZipIOReader(new File("/home/daniel/test.zip"));
        for (ZipEntry file : zip.getZipFiles()) {
            System.out.println(file.getName());
        }
        ZipIOFile[] dd = zip.getZipIOFileSystem();
        int i = 1;
    }
}
