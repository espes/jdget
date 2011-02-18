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

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.zip.CRC32;
import java.util.zip.CheckedInputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipException;
import java.util.zip.ZipFile;
import java.util.zip.ZipInputStream;

import org.appwork.utils.Files;
import org.appwork.utils.logging.Log;

/**
 * @author daniel
 * 
 */
public class ZipIOReader {

    private File       zipFile               = null;
    private ZipFile    zip                   = null;
    private ZipIOFile  rootFS                = null;
    private boolean    autoCreateExtractPath = true;
    private boolean    overwrite             = false;
    private boolean    autoCreateSubDirs     = true;
    private byte[]     byteArray             = null;
    private int        zipEntriesSize        = -1;
    private ZipEntry[] zipEntries            = null;

    public ZipIOReader(final byte[] byteArray) {
        this.byteArray = byteArray;
    }

    /**
     * open the zipFile for this ZipIOReader
     * 
     * @param zipFile
     *            the zipFile we want to open
     * @throws ZipIOException
     * @throws ZipException
     * @throws IOException
     */
    public ZipIOReader(final File zipFile) throws ZipIOException, ZipException, IOException {
        this.zipFile = zipFile;
        openZip();
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
            byteArray = null;
            zip = null;
        }
    }

    /**
     * extract given ZipEntry to output File
     * 
     * @param entry
     *            ZipEntry to extract
     * @param output
     *            File to extract to
     * @return
     * @throws ZipIOException
     * @throws IOException
     */
    public synchronized ArrayList<File> extract(final ZipEntry entry, final File output) throws ZipIOException, IOException {
        if (entry.isDirectory()) { throw new ZipIOException("Cannot extract a directory", entry); }
        final ArrayList<File> ret = new ArrayList<File>();
        if (output.exists() && output.isDirectory()) {
            if (isOverwrite()) {
                Files.deleteRecursiv(output);
                if (output.exists()) { throw new IOException("Cannot extract File to Directory " + output); }
            }
            if (output.exists() && output.isDirectory()) {
                Log.L.finer("Skipped extraction: directory exists: " + output);
                return ret;

            }
        }
        if (output.exists()) {
            if (isOverwrite()) {
                output.delete();
                if (output.exists()) { throw new IOException("Cannot overwrite File " + output); }
            }
            if (output.exists()) {
                Log.L.finer("Skipped extraction: file exists: " + output);
                return ret;
            }
        }
        if (!output.getParentFile().exists()) {

            if (isAutoCreateSubDirs()) {
                output.getParentFile().mkdirs();
                ret.add(output.getParentFile());
                if (!output.getParentFile().exists()) { throw new IOException("Cannot create folder for File " + output); }
            }
            if (!output.getParentFile().exists()) {
                Log.L.finer("Skipped extraction: cannot create dir: " + output);
                return ret;
            }
        }
        FileOutputStream stream = null;
        CheckedInputStream in = null;
        try {
            stream = new FileOutputStream(output);
            final InputStream is = getInputStream(entry);
            in = new CheckedInputStream(is, new CRC32());
            final byte[] buffer = new byte[32767];
            int len = 0;
            while ((len = in.read(buffer)) != -1) {
                stream.write(buffer, 0, len);
            }
            if (entry.getCrc() != -1 && entry.getCrc() != in.getChecksum().getValue()) { throw new ZipIOException("CRC32 Failed", entry); }
            ret.add(output);
        } finally {
            try {
                in.close();
            } catch (final Throwable e) {
            }
            try {
                stream.close();
            } catch (final Throwable e) {
            }
        }
        return ret;
    }

    public synchronized ArrayList<File> extractTo(final File outputDirectory) throws ZipIOException, IOException {
        if (outputDirectory.exists() && outputDirectory.isFile()) { throw new IOException("cannot extract to a file " + outputDirectory); }
        if (!outputDirectory.exists() && !(autoCreateExtractPath && outputDirectory.mkdirs())) { throw new IOException("could not create outputDirectory " + outputDirectory); }

        final ArrayList<File> ret = new ArrayList<File>();

        for (final ZipEntry entry : getZipFiles()) {
            final File out = new File(outputDirectory, entry.getName());
            if (entry.isDirectory()) {
                if (!out.exists()) {
                    if (isAutoCreateSubDirs()) {
                        if (!out.mkdir()) { throw new IOException("could not create outputDirectory " + out); }
                        ret.add(out);
                    } else {
                        Log.L.finer("SKipped creatzion of: " + out);
                    }
                }

            } else {
                ret.addAll(extract(entry, out));
            }
        }
        return ret;
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
    private ZipIOFile getFolder(final String path, final ZipIOFile currentRoot) {
        if (path == null || currentRoot == null || !currentRoot.isDirectory()) { return null; }
        if (currentRoot.getAbsolutePath().equalsIgnoreCase(path)) { return currentRoot; }
        for (final ZipIOFile tmp : currentRoot.getFiles()) {
            if (tmp.isDirectory() && tmp.getAbsolutePath().equalsIgnoreCase(path)) {
                return tmp;
            } else if (tmp.isDirectory()) {
                final ZipIOFile ret = getFolder(path, tmp);
                if (ret != null) { return ret; }
            }
        }
        return null;
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
    public synchronized InputStream getInputStream(final ZipEntry entry) throws ZipIOException, IOException {
        if (entry == null) { throw new ZipIOException("invalid zipEntry"); }
        if (zip != null) {
            return zip.getInputStream(entry);
        } else {
            ZipInputStream zis = null;
            boolean close = true;
            try {
                zis = new ZipInputStream(new ByteArrayInputStream(byteArray));
                ZipEntry ze = null;

                while ((ze = zis.getNextEntry()) != null) {
                    /* find the entry that matches */
                    final String name = ze.getName();
                    if (name.equals(entry.getName())) {
                        final ZipInputStream zis2 = zis;
                        close = false;
                        return new InputStream() {

                            @Override
                            public void close() throws IOException {
                                zis2.close();
                            }

                            @Override
                            public int read() throws IOException {
                                return zis2.read();
                            }

                        };
                    }
                }
            } catch (final IOException e) {
                throw new ZipIOException(e.getMessage(), e);
            } finally {
                try {
                    if (close) {
                        zis.close();
                    }
                } catch (final Throwable e) {
                }
            }
            return null;
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
    public synchronized ZipEntry getZipFile(final String fileName) throws ZipIOException {
        if (fileName == null) { throw new ZipIOException("invalid fileName"); }
        if (zip != null) {
            return zip.getEntry(fileName);
        } else {
            ZipInputStream zis = null;
            try {
                zis = new ZipInputStream(new ByteArrayInputStream(byteArray));
                ZipEntry ze = null;
                while ((ze = zis.getNextEntry()) != null) {
                    if (ze.getName().equals(fileName)) { return ze; }
                }
                return null;
            } catch (final IOException e) {
                throw new ZipIOException(e.getMessage(), e);
            } finally {
                try {
                    zis.close();
                } catch (final Throwable e) {
                }
            }
        }
    }

    /**
     * returns a list of all ZipEntries in this ZipFile
     * 
     * @return ZipEntry[] of all files in the ZipFile
     * @throws ZipIOException
     */
    public synchronized ZipEntry[] getZipFiles() throws ZipIOException {
        if (zipEntries != null) { return zipEntries; }
        final ArrayList<ZipEntry> ret = new ArrayList<ZipEntry>();
        if (zip != null) {
            final Enumeration<? extends ZipEntry> zipIter = zip.entries();
            while (zipIter.hasMoreElements()) {
                ret.add(zipIter.nextElement());
            }
        } else {
            ZipInputStream zis = null;
            try {
                zis = new ZipInputStream(new ByteArrayInputStream(byteArray));
                ZipEntry ze = null;
                while ((ze = zis.getNextEntry()) != null) {
                    ret.add(ze);
                }
            } catch (final IOException e) {
                throw new ZipIOException(e.getMessage(), e);
            } finally {
                try {
                    zis.close();
                } catch (final Throwable e) {
                }
            }
        }
        zipEntries = ret.toArray(new ZipEntry[ret.size()]);
        return zipEntries;
    }

    /**
     * returns a ZipIOFile Filesystem for this ZipFile
     * 
     * @return ZipIOFile that represents ROOT of the Filesystem
     * @throws ZipIOException
     */
    public synchronized ZipIOFile getZipIOFileSystem() throws ZipIOException {
        if (rootFS != null) { return rootFS; }
        final ZipEntry[] content = getZipFiles();
        final ArrayList<ZipIOFile> root = new ArrayList<ZipIOFile>();
        for (final ZipEntry file : content) {
            if (!file.isDirectory() && !file.getName().contains("/")) {
                /* file is in root */
                final ZipIOFile tmp = new ZipIOFile(file.getName(), file, this, null);
                root.add(tmp);
            } else if (!file.isDirectory()) {
                /* file is not in root */
                final String parts[] = file.getName().split("/");
                /* we begin at root */
                ZipIOFile currentParent = null;
                String path = "";
                for (int i = 0; i < parts.length; i++) {
                    if (i == parts.length - 1) {
                        /* the file */
                        final ZipIOFile tmp = new ZipIOFile(parts[i], file, this, currentParent);
                        currentParent.getFilesInternal().add(tmp);
                    } else {
                        path = path + parts[i] + "/";
                        ZipIOFile found = null;
                        for (final ZipIOFile tmp : root) {
                            found = getFolder(path, tmp);
                            if (found != null) {
                                break;
                            }
                        }
                        if (found != null) {
                            currentParent = found;
                        } else {
                            final ZipIOFile newFolder = new ZipIOFile(parts[i], null, this, currentParent);
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

    public boolean isAutoCreateExtractPath() {
        return autoCreateExtractPath;
    }

    /**
     * @return
     */
    private boolean isAutoCreateSubDirs() {
        // TODO Auto-generated method stub
        return autoCreateSubDirs;
    }

    /**
     * @return
     */
    private boolean isOverwrite() {
        // TODO Auto-generated method stub
        return overwrite;
    }

    /**
     * opens the ZipFile for further use
     * 
     * @throws ZipIOException
     * @throws ZipException
     * @throws IOException
     */
    private synchronized void openZip() throws ZipIOException, ZipException, IOException {
        if (zip != null) { return; }
        if (zipFile == null || zipFile.isDirectory() || !zipFile.exists()) { throw new ZipIOException("invalid zipFile"); }
        zip = new ZipFile(zipFile);
    }

    public void setAutoCreateExtractPath(final boolean autoCreateExtractPath) {
        this.autoCreateExtractPath = autoCreateExtractPath;
    }

    public void setAutoCreateSubDirs(final boolean autoCreateSubDirs) {
        this.autoCreateSubDirs = autoCreateSubDirs;
    }

    public void setOverwrite(final boolean overwrite) {
        this.overwrite = overwrite;
    }

    /**
     * how many ZipEntries does this ZipFile have
     * 
     * @return
     * @throws ZipIOException
     * @throws IOException
     */
    public synchronized int size() throws ZipIOException {
        if (zipEntriesSize != -1) { return zipEntriesSize; }
        if (zip != null) {
            zipEntriesSize = zip.size();
        } else {
            ZipInputStream zis = null;
            try {
                zipEntriesSize = 0;
                zis = new ZipInputStream(new ByteArrayInputStream(byteArray));
                while (zis.getNextEntry() != null) {
                    zipEntriesSize++;
                }
            } catch (final IOException e) {
                throw new ZipIOException(e.getMessage(), e);
            } finally {
                try {
                    zis.close();
                } catch (final Throwable e) {
                }
            }
        }
        return zipEntriesSize;
    }

    /**
     * trims the ZipIOFiles(reduces memory)
     * 
     * @param root
     *            ZipIOFile we want to start
     */
    private void trimZipIOFiles(final ZipIOFile root) {
        if (root == null) { return; }
        for (final ZipIOFile tmp : root.getFiles()) {
            if (tmp.isDirectory()) {
                trimZipIOFiles(tmp);
            }
        }
        root.getFilesInternal().trimToSize();
    }

}
