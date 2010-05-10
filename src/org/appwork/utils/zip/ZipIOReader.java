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
import java.io.FileOutputStream;
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
    private ZipIOFile rootFS = null;

    /**
     * open the zipFile for this ZipIOReader
     * 
     * @param zipFile
     *            the zipFile we want to open
     * @throws ZipIOException
     * @throws ZipException
     * @throws IOException
     */
    public ZipIOReader(File zipFile) throws ZipIOException, ZipException, IOException {
        this.zipFile = zipFile;
        openZip();
    }

    /**
     * opens the ZipFile for further use
     * 
     * @throws ZipIOException
     * @throws ZipException
     * @throws IOException
     */
    private synchronized void openZip() throws ZipIOException, ZipException, IOException {
        if (zip != null) return;
        if (zipFile == null || zipFile.isDirectory() || !zipFile.exists()) throw new ZipIOException("invalid zipFile");
        this.zip = new ZipFile(zipFile);
    }

    /**
     * closes the ZipFile
     * 
     * @throws IOException
     */
    public synchronized void close() throws IOException {
        try {
            if (zip != null) {
                zip.close();
            }
        } finally {
            zip = null;
        }
    }

    /**
     * returns a list of all ZipEntries in this ZipFile
     * 
     * @return ZipEntry[] of all files in the ZipFile
     * @throws ZipIOException
     */
    public synchronized ZipEntry[] getZipFiles() throws ZipIOException {
        ArrayList<ZipEntry> ret = new ArrayList<ZipEntry>();
        Enumeration<? extends ZipEntry> zipIter = zip.entries();
        while (zipIter.hasMoreElements()) {
            ret.add(zipIter.nextElement());
        }
        return ret.toArray(new ZipEntry[ret.size()]);
    }

    /**
     * how many ZipEntries does this ZipFile have
     * 
     * @return
     * @throws ZipIOException
     */
    public synchronized int size() throws ZipIOException {
        return zip.size();
    }

    /**
     * returns an InputStream for given ZipEntry
     * 
     * @param entry
     *            ZipEntry we want an InputStream
     * @return InputStream for given ZipEntry
     * @throws ZipIOException
     * @throws IOException
     */
    public synchronized InputStream getInputStream(ZipEntry entry) throws ZipIOException, IOException {
        if (entry == null) throw new ZipIOException("invalid zipEntry");
        return zip.getInputStream(entry);
    }

    public synchronized void extract(ZipEntry entry, File output) throws ZipIOException, IOException {
        if (entry.getName().endsWith("/")) throw new ZipIOException("Cannot extract a directory");
        FileOutputStream stream = null;
        InputStream in = null;
        try {
            stream = new FileOutputStream(output);
            in = getInputStream(entry);
            byte[] buffer = new byte[32767];
            int len = 0;
            while ((len = in.read(buffer)) != -1) {
                stream.write(buffer, 0, len);
            }
        } finally {
            if (in != null) {
                try {
                    in.close();
                } catch (Exception e) {
                }
            }
            if (stream != null) {
                try {
                    stream.close();
                } catch (Exception e) {
                }
            }
        }
    }

    /**
     * returns the ZipEntry for the given name
     * 
     * @param fileName
     *            Filename we want a ZipEntry for
     * @return ZipEntry if filename is found or null if not found
     * @throws ZipIOException
     */
    public synchronized ZipEntry getZipFile(String fileName) throws ZipIOException {
        if (fileName == null) throw new ZipIOException("invalid fileName");
        return zip.getEntry(fileName);
    }

    protected void finalize() throws IOException {
        close();
    }

    /**
     * returns a ZipIOFile Filesystem for this ZipFile
     * 
     * @return ZipIOFile that represents ROOT of the Filesystem
     * @throws ZipIOException
     */
    public synchronized ZipIOFile getZipIOFileSystem() throws ZipIOException {
        if (rootFS != null) return rootFS;
        ZipEntry[] content = getZipFiles();
        ArrayList<ZipIOFile> root = new ArrayList<ZipIOFile>();
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
        rootFS = new ZipIOFile("", null, this, null);
        rootFS.getFilesInternal().addAll(root);
        rootFS.getFilesInternal().trimToSize();
        trimZipIOFiles(rootFS);
        return rootFS;
    }

    /**
     * trims the ZipIOFiles(reduces memory)
     * 
     * @param root
     *            ZipIOFile we want to start
     */
    private void trimZipIOFiles(ZipIOFile root) {
        if (root == null) return;
        for (ZipIOFile tmp : root.getFiles()) {
            if (tmp.isDirectory()) trimZipIOFiles(tmp);
        }
        root.getFilesInternal().trimToSize();
    }

    /**
     * find ZipIOFile that represents the Folder with given path
     * 
     * @param path
     *            the path we search a ZipIOFile for
     * @param currentRoot
     *            currentRoot for the search
     * @return ZipIOFile if path is found, else null
     */
    private ZipIOFile getFolder(String path, ZipIOFile currentRoot) {
        if (path == null || currentRoot == null || !currentRoot.isDirectory()) return null;
        if (currentRoot.getAbsolutePath().equalsIgnoreCase(path)) return currentRoot;
        for (ZipIOFile tmp : currentRoot.getFiles()) {
            if (tmp.isDirectory() && tmp.getAbsolutePath().equalsIgnoreCase(path)) {
                return tmp;
            } else if (tmp.isDirectory()) {
                ZipIOFile ret = getFolder(path, tmp);
                if (ret != null) return ret;
            }
        }
        return null;
    }

}
