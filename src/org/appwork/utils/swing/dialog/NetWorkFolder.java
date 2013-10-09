/**
 * Copyright (c) 2009 - 2012 AppWork UG(haftungsbeschränkt) <e-mail@appwork.org>
 * 
 * This file is part of org.appwork.utils.swing.dialog
 * 
 * This software is licensed under the Artistic License 2.0,
 * see the LICENSE file or http://www.opensource.org/licenses/artistic-license-2.0.php
 * for details
 */
package org.appwork.utils.swing.dialog;

import java.io.File;
import java.io.FileFilter;
import java.io.FilenameFilter;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.nio.file.Path;
import java.util.HashMap;

import sun.awt.shell.ShellFolder;

/**
 * @author Thomas
 * 
 */
public class NetWorkFolder extends File {

    private File                  networkFolder;
    private File[]                fileList;
    private HashMap<String, File> map;

    /**
     * @param networkFolder
     */
    public NetWorkFolder(final File networkFolder) {
        super(networkFolder, "");
        this.networkFolder = networkFolder;

    }

    @Override
    public String getName() {

        return networkFolder.getName();
    }

    @Override
    public String getParent() {

        return networkFolder.getParent();
    }

    @Override
    public File getParentFile() {

        return networkFolder.getParentFile();
    }

    @Override
    public String getPath() {

        return networkFolder.getPath();
    }

    @Override
    public boolean isAbsolute() {

        return networkFolder.isAbsolute();
    }

    @Override
    public String getAbsolutePath() {

        return networkFolder.getAbsolutePath();
    }

    @Override
    public File getAbsoluteFile() {

        return networkFolder.getAbsoluteFile();
    }

    @Override
    public String getCanonicalPath() throws IOException {

        return networkFolder.getCanonicalPath();
    }

    @Override
    public File getCanonicalFile() throws IOException {

        return networkFolder.getCanonicalFile();
    }

    @Override
    public URL toURL() throws MalformedURLException {

        return networkFolder.toURL();
    }

    @Override
    public URI toURI() {

        return networkFolder.toURI();
    }

    @Override
    public boolean canRead() {

        return networkFolder.canRead();
    }

    @Override
    public boolean canWrite() {

        return networkFolder.canWrite();
    }

    @Override
    public boolean exists() {

        return networkFolder.exists();
    }

    @Override
    public boolean isDirectory() {

        return networkFolder.isDirectory();
    }

    @Override
    public boolean isFile() {

        return networkFolder.isFile();
    }

    @Override
    public boolean isHidden() {

        return networkFolder.isHidden();
    }

    @Override
    public long lastModified() {

        return networkFolder.lastModified();
    }

    @Override
    public long length() {

        return networkFolder.length();
    }

    @Override
    public boolean createNewFile() throws IOException {

        return networkFolder.createNewFile();
    }

    @Override
    public boolean delete() {

        return networkFolder.delete();
    }

    @Override
    public void deleteOnExit() {

        networkFolder.deleteOnExit();
    }

    @Override
    public String[] list() {

        return networkFolder.list();
    }

    @Override
    public String[] list(final FilenameFilter filter) {

        return networkFolder.list(filter);
    }

    @Override
    public File[] listFiles() {

        return listFiles(true);
    }

    @Override
    public File[] listFiles(final FilenameFilter filter) {

        return networkFolder.listFiles(filter);
    }

    @Override
    public File[] listFiles(final FileFilter filter) {

        return networkFolder.listFiles(filter);
    }

    @Override
    public boolean mkdir() {

        return networkFolder.mkdir();
    }

    @Override
    public boolean mkdirs() {

        return networkFolder.mkdirs();
    }

    @Override
    public boolean renameTo(final File dest) {

        return networkFolder.renameTo(dest);
    }

    @Override
    public boolean setLastModified(final long time) {

        return networkFolder.setLastModified(time);
    }

    @Override
    public boolean setReadOnly() {

        return networkFolder.setReadOnly();
    }

    @Override
    public boolean setWritable(final boolean writable, final boolean ownerOnly) {

        return networkFolder.setWritable(writable, ownerOnly);
    }

    @Override
    public boolean setWritable(final boolean writable) {

        return networkFolder.setWritable(writable);
    }

    @Override
    public boolean setReadable(final boolean readable, final boolean ownerOnly) {

        return networkFolder.setReadable(readable, ownerOnly);
    }

    @Override
    public boolean setReadable(final boolean readable) {

        return networkFolder.setReadable(readable);
    }

    @Override
    public boolean setExecutable(final boolean executable, final boolean ownerOnly) {

        return networkFolder.setExecutable(executable, ownerOnly);
    }

    @Override
    public boolean setExecutable(final boolean executable) {

        return networkFolder.setExecutable(executable);
    }

    @Override
    public boolean canExecute() {

        return networkFolder.canExecute();
    }

    @Override
    public long getTotalSpace() {

        return networkFolder.getTotalSpace();
    }

    @Override
    public long getFreeSpace() {

        return networkFolder.getFreeSpace();
    }

    @Override
    public long getUsableSpace() {

        return networkFolder.getUsableSpace();
    }

    @Override
    public int compareTo(final File pathname) {

        return networkFolder.compareTo(pathname);
    }

    @Override
    public boolean equals(final Object obj) {

        return networkFolder.equals(obj);
    }

    @Override
    public int hashCode() {

        return networkFolder.hashCode();
    }

    @Override
    public String toString() {

        return networkFolder.toString();
    }

    @Override
    public Path toPath() {

        return networkFolder.toPath();
    }

    /**
     * @param useFileHiding
     * @return
     */
    public File[] listFiles(final boolean useFileHiding) {
       
        fileList = ((ShellFolder) networkFolder).listFiles(!useFileHiding);

        if (fileList != null) {
            final HashMap<String, File> map = new HashMap<String, File>();
            for (final File f : fileList) {
                map.put(f.getAbsolutePath(), f);
            }
            this.map = map;
        }

        return fileList;
    }

    /**
     * @return
     */
    public File[] listFilesAsynch() {

        return fileList;
    }

    /**
     * @param absolutePath
     * @return
     */
    public File get(final String absolutePath) {
        if (map != null) {
            return map.get(absolutePath);
        }

        return null;
    }

}
