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

    private final File            networkFolder;
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
    public boolean canExecute() {

        return this.networkFolder.canExecute();
    }

    @Override
    public boolean canRead() {

        return this.networkFolder.canRead();
    }

    @Override
    public boolean canWrite() {
        return this.networkFolder.canWrite();
    }

    @Override
    public int compareTo(final File pathname) {

        return this.networkFolder.compareTo(pathname);
    }

    @Override
    public boolean createNewFile() throws IOException {

        return this.networkFolder.createNewFile();
    }

    @Override
    public boolean delete() {

        return this.networkFolder.delete();
    }

    @Override
    public void deleteOnExit() {

        this.networkFolder.deleteOnExit();
    }

    @Override
    public boolean equals(final Object obj) {

        return this.networkFolder.equals(obj);
    }

    @Override
    public boolean exists() {

        return this.networkFolder.exists();
    }

    /**
     * @param absolutePath
     * @return
     */
    public File get(final String absolutePath) {
        if (this.map != null) { return this.map.get(absolutePath); }

        return null;
    }

    @Override
    public File getAbsoluteFile() {

        return this.networkFolder.getAbsoluteFile();
    }

    @Override
    public String getAbsolutePath() {

        return this.networkFolder.getAbsolutePath();
    }

    @Override
    public File getCanonicalFile() throws IOException {

        return this.networkFolder.getCanonicalFile();
    }

    @Override
    public String getCanonicalPath() throws IOException {

        return this.networkFolder.getCanonicalPath();
    }

    @Override
    public long getFreeSpace() {

        return this.networkFolder.getFreeSpace();
    }

    @Override
    public String getName() {

        return this.networkFolder.getName();
    }

    @Override
    public String getParent() {

        return this.networkFolder.getParent();
    }

    @Override
    public File getParentFile() {

        return this.networkFolder.getParentFile();
    }

    @Override
    public String getPath() {

        return this.networkFolder.getPath();
    }

    @Override
    public long getTotalSpace() {

        return this.networkFolder.getTotalSpace();
    }

    @Override
    public long getUsableSpace() {

        return this.networkFolder.getUsableSpace();
    }

    @Override
    public int hashCode() {

        return this.networkFolder.hashCode();
    }

    @Override
    public boolean isAbsolute() {

        return this.networkFolder.isAbsolute();
    }

    @Override
    public boolean isDirectory() {

        return this.networkFolder.isDirectory();
    }

    @Override
    public boolean isFile() {

        return this.networkFolder.isFile();
    }

    @Override
    public boolean isHidden() {

        return this.networkFolder.isHidden();
    }

    @Override
    public long lastModified() {

        return this.networkFolder.lastModified();
    }

    @Override
    public long length() {

        return this.networkFolder.length();
    }

    @Override
    public String[] list() {

        return this.networkFolder.list();
    }

    @Override
    public String[] list(final FilenameFilter filter) {

        return this.networkFolder.list(filter);
    }

    @Override
    public File[] listFiles() {

        return this.listFiles(true);
    }

    /**
     * @param useFileHiding
     * @return
     */
    public File[] listFiles(final boolean useFileHiding) {

        this.fileList = ((ShellFolder) this.networkFolder).listFiles(!useFileHiding);

        if (this.fileList != null) {
            final HashMap<String, File> map = new HashMap<String, File>();
            for (final File f : this.fileList) {
                map.put(f.getAbsolutePath(), f);
            }
            this.map = map;
        }

        return this.fileList;
    }

    @Override
    public File[] listFiles(final FileFilter filter) {

        return this.networkFolder.listFiles(filter);
    }

    @Override
    public File[] listFiles(final FilenameFilter filter) {

        return this.networkFolder.listFiles(filter);
    }

    /**
     * @return
     */
    public File[] listFilesAsynch() {

        return this.fileList;
    }

    @Override
    public boolean mkdir() {

        return this.networkFolder.mkdir();
    }

    @Override
    public boolean mkdirs() {

        return this.networkFolder.mkdirs();
    }

    @Override
    public boolean renameTo(final File dest) {

        return this.networkFolder.renameTo(dest);
    }

    @Override
    public boolean setExecutable(final boolean executable) {

        return this.networkFolder.setExecutable(executable);
    }

    @Override
    public boolean setExecutable(final boolean executable, final boolean ownerOnly) {

        return this.networkFolder.setExecutable(executable, ownerOnly);
    }

    @Override
    public boolean setLastModified(final long time) {

        return this.networkFolder.setLastModified(time);
    }

    @Override
    public boolean setReadable(final boolean readable) {

        return this.networkFolder.setReadable(readable);
    }

    @Override
    public boolean setReadable(final boolean readable, final boolean ownerOnly) {

        return this.networkFolder.setReadable(readable, ownerOnly);
    }

    @Override
    public boolean setReadOnly() {

        return this.networkFolder.setReadOnly();
    }

    @Override
    public boolean setWritable(final boolean writable) {

        return this.networkFolder.setWritable(writable);
    }

    @Override
    public boolean setWritable(final boolean writable, final boolean ownerOnly) {

        return this.networkFolder.setWritable(writable, ownerOnly);
    }

    @Override
    public Path toPath() {

        return this.networkFolder.toPath();
    }

    @Override
    public String toString() {

        return this.networkFolder.toString();
    }

    @Override
    public URI toURI() {

        return this.networkFolder.toURI();
    }

    @Override
    public URL toURL() throws MalformedURLException {

        return this.networkFolder.toURL();
    }

}
