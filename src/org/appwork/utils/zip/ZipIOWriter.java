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
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.appwork.utils.Hash;

/**
 * @author daniel
 * 
 */
public class ZipIOWriter {

    private ZipOutputStream zipStream = null;
    private FileOutputStream fileStream = null;
    private File zipFile = null;

    private byte[] buf = new byte[8192];

    /**
     * constructor for ZipIOWriter
     * 
     * @param zipFile
     *            zipFile we want create (does not overwrite existing files!)
     * @throws FileNotFoundException
     * @throws ZipIOException
     */
    public ZipIOWriter(File zipFile) throws FileNotFoundException, ZipIOException {
        this.zipFile = zipFile;
        openZip(false);
    }

    /**
     * constructor for ZipIOWriter
     * 
     * @param zipFile
     *            zipFile we want create
     * @param overwrite
     *            overwrite existing ziFiles?
     * @throws FileNotFoundException
     * @throws ZipIOException
     */
    public ZipIOWriter(File zipFile, boolean overwrite) throws FileNotFoundException, ZipIOException {
        this.zipFile = zipFile;
        openZip(overwrite);
    }

    /**
     * opens the zipFile for further use
     * 
     * @param overwrite
     *            overwrite existing zipFiles?
     * @throws ZipIOException
     * @throws FileNotFoundException
     */
    private void openZip(boolean overwrite) throws ZipIOException, FileNotFoundException {
        if (fileStream != null && zipStream != null) return;
        if (zipFile == null || zipFile.isDirectory()) throw new ZipIOException("invalid zipFile");
        if (zipFile.exists() && !overwrite) throw new ZipIOException("zipFile already exists");
        fileStream = new FileOutputStream(zipFile);
        zipStream = new ZipOutputStream(fileStream);
    }

    /**
     * closes the ZipFile
     * 
     * @throws IOException
     */
    public synchronized void close() throws IOException {
        try {
            if (zipStream != null) {
                zipStream.flush();
                zipStream.close();
            }
            if (fileStream != null) {
                fileStream.flush();
                fileStream.close();
            }
        } finally {
            zipStream = null;
            fileStream = null;
        }
    }

    protected void finalize() throws IOException {
        close();
    }

    /**
     * add given File (File or Directory) to this ZipFile
     * 
     * @param add
     *            File to add
     * @param compress
     *            compress or store
     * @param path
     *            customized path
     * @throws ZipIOException
     * @throws IOException
     */
    public synchronized void add(File add, boolean compress, String path) throws ZipIOException, IOException {
        if (add == null || !add.exists()) throw new ZipIOException("add " + add.getAbsolutePath() + " invalid");
        if (add.isFile()) {
            addFileInternal(add, compress, path);
        } else if (add.isDirectory()) {
            addDirectoryInternal(add, compress, path);
        } else {
            throw new ZipIOException("add " + add.getAbsolutePath() + " invalid");
        }
    }

    /**
     * add given File to this ZipFile
     * 
     * @param addFile
     *            File to add
     * @param compress
     *            compress or store
     * @param path
     *            customized path
     * @throws ZipIOException
     * @throws IOException
     */
    public synchronized void addFile(File addFile, boolean compress, String path) throws ZipIOException, IOException {
        addFileInternal(addFile, compress, path);
    }

    /**
     * add given Directory to this ZipFile
     * 
     * @param addDirectory
     *            Directory to add
     * @param compress
     *            compress or store
     * @param path
     *            customized path
     * @throws ZipIOException
     * @throws IOException
     */
    public synchronized void addDirectory(File addDirectory, boolean compress, String path) throws ZipIOException, IOException {
        addDirectoryInternal(addDirectory, compress, path);
    }

    private void addDirectoryInternal(File addDirectory, boolean compress, String path) throws ZipIOException, IOException {
        if (addDirectory == null || !addDirectory.isDirectory() || !addDirectory.exists()) throw new ZipIOException("addDirectory " + addDirectory.getAbsolutePath() + " invalid");
        for (File add : addDirectory.listFiles()) {
            if (add.isFile()) {
                addFileInternal(add, compress, (path != null ? path + "/" : "") + addDirectory.getName());
            } else if (add.isDirectory()) {
                addDirectoryInternal(add, compress, (path != null ? path + "/" : "") + addDirectory.getName());
            } else {
                throw new ZipIOException("addDirectory " + addDirectory.getAbsolutePath() + " invalid");
            }
        }
    }

    private void addFileInternal(File addFile, boolean compress, String path) throws ZipIOException, IOException {
        FileInputStream fin = null;
        boolean zipEntryAdded = false;
        try {
            if (addFile == null || !addFile.isFile() || !addFile.exists()) throw new ZipIOException("addFile " + addFile.getAbsolutePath() + " invalid");
            ZipEntry zipAdd = new ZipEntry((path != null ? path + "/" : "") + addFile.getName());
            zipAdd.setSize(addFile.length());
            if (compress) {
                zipAdd.setMethod(ZipEntry.DEFLATED);
            } else {
                zipAdd.setMethod(ZipEntry.STORED);
                zipAdd.setCompressedSize(addFile.length());
                /* STORED must have a CRC32! */
                zipAdd.setCrc(Hash.getCRC32(addFile));
            }
            fin = new FileInputStream(addFile);
            zipStream.putNextEntry(zipAdd);
            zipEntryAdded = true;
            int len;
            while ((len = fin.read(buf)) > 0) {
                zipStream.write(buf, 0, len);
            }
        } finally {
            if (zipEntryAdded) zipStream.closeEntry();
            if (fin != null) fin.close();
        }
    }

    /**
     * @param args
     * @throws IOException
     * @throws ZipIOException
     */
    public static void main(String[] args) throws ZipIOException, IOException {
        ZipIOWriter zip = new ZipIOWriter(new File("/home/daniel/test.zip"), true);
        zip.add(new File("/home/daniel/Rain-OST"), true, "home/uu");
        zip.close();
    }
}
