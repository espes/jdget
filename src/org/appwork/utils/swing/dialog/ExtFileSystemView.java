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
import java.io.IOException;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.ArrayList;

import javax.swing.Icon;
import javax.swing.filechooser.FileSystemView;

import org.appwork.utils.logging.Log;

import sun.awt.shell.ShellFolder;

/**
 * 
 * 
 * This ExtFileSystemView is a workaround. The standard FileSystemView maps all
 * contents of the Windows Desktop into the save in combobox. The virtual
 * WIndows Folders are a pain in the a%/&
 * 
 * @author Thomas
 * 
 */
public class ExtFileSystemView extends FileSystemView {

    private FileSystemView     org;
    private File[]             roots;
    /**
     * 
     */
    public static final String VIRTUAL_NETWORKFOLDER    = "::{F02C1A0D-BE21-4350-88B0-7367FC96EF3C}";
    public static final String VIRTUAL_NETWORKFOLDER_XP = "::{208D2C60-3AEA-1069-A2D7-08002B30309D}";

    /**
     */
    public ExtFileSystemView() {
        org = FileSystemView.getFileSystemView();

    }

    @Override
    public boolean isRoot(File f) {

        return org.isRoot(f);
    }

    @Override
    public Boolean isTraversable(File f) {

        return org.isTraversable(f);
    }

    @Override
    public String getSystemDisplayName(File f) {

        return org.getSystemDisplayName(f);
    }

    @Override
    public String getSystemTypeDescription(File f) {

        return org.getSystemTypeDescription(f);
    }

    @Override
    public Icon getSystemIcon(File f) {

        return org.getSystemIcon(f);
    }

    @Override
    public boolean isParent(File folder, File file) {

        return org.isParent(folder, file);
    }

    @Override
    public File getChild(File parent, String fileName) {

        return org.getChild(parent, fileName);
    }

    @Override
    public boolean isFileSystem(File f) {

        return org.isFileSystem(f);
    }

    @Override
    public boolean isHiddenFile(File f) {

        return org.isHiddenFile(f);
    }

    @Override
    public boolean isFileSystemRoot(File dir) {

        return org.isFileSystemRoot(dir);
    }

    @Override
    public boolean isDrive(File dir) {

        return org.isDrive(dir);
    }

    @Override
    public boolean isFloppyDrive(File dir) {

        return org.isFloppyDrive(dir);
    }

    @Override
    public boolean isComputerNode(File dir) {

        return org.isComputerNode(dir);
    }

    @Override
    public File[] getRoots() {
        long t = System.currentTimeMillis();
        if (roots != null) return roots;
        try {
            File[] baseFolders = AccessController.doPrivileged(new PrivilegedAction<File[]>() {
                public File[] run() {
                    return (File[]) ShellFolder.get("fileChooserComboBoxFolders");
                }
            });
            ArrayList<File> newRoots = new ArrayList<File>();
            File net = null;
            File home = getHomeDirectory();
            for (File f : baseFolders) {
                // Win32ShellFolder2.class
                if (f.getName().equals("Recent")) continue;
                if (f.getParentFile() == null || !f.getParentFile().equals(home)) {
                    newRoots.add(f);
                } else if (f.getName().equals(VIRTUAL_NETWORKFOLDER)) {
                    net = f;
                } else if (f.getName().equals(VIRTUAL_NETWORKFOLDER_XP)) {
                    net = f;
                }
            }

            HomeFolder[] homeFolders = new HomeFolder[] { new HomeFolder(HomeFolder.PICTURES, "images"), new HomeFolder(HomeFolder.VIDEOS, "images"), new HomeFolder(HomeFolder.DOWNLOADS, "downloads"), new HomeFolder(HomeFolder.MUSIC, "music") };

            for (HomeFolder hf : homeFolders) {
                if (hf.exists()) newRoots.add(hf);
            }
            if (net != null) {
                File[] netList = net.listFiles();
                if (netList != null && netList.length > 0) {
                    newRoots.add(net);
                }
            }
            roots = newRoots.toArray(new File[] {});
            return roots;
        } finally {
            Log.L.info("Roots: " + (System.currentTimeMillis() - t));

        }
    }

    @Override
    public File getHomeDirectory() {

        return org.getHomeDirectory();
    }

    @Override
    public File getDefaultDirectory() {

        return org.getDefaultDirectory();
    }

    @Override
    public File createFileObject(File dir, String filename) {

        return org.createFileObject(dir, filename);
    }

    @Override
    public File createFileObject(String path) {

        return org.createFileObject(path);
    }

    @Override
    public File[] getFiles(File dir, boolean useFileHiding) {
        long t = System.currentTimeMillis();
        try {
            File[] ret = org.getFiles(dir, useFileHiding);
            ArrayList<File> filtered = new ArrayList<File>();
            for (File f : ret) {
                if (f.getName().equals(VIRTUAL_NETWORKFOLDER)) {
                    filtered.add(f);
                    continue;
                } else if (f.getName().equals(VIRTUAL_NETWORKFOLDER_XP)) {
                    filtered.add(f);
                    continue;
                } else if (f.getName().startsWith("::{")) {
                    continue;
                }
                filtered.add(f);
            }
            return filtered.toArray(new File[] {});
        } finally {
            Log.L.info("getFiles: " + (System.currentTimeMillis() - t) + " " + dir);

        }
    }

    @Override
    public File getParentDirectory(File dir) {

        return org.getParentDirectory(dir);
    }

    /*
     * (non-Javadoc)
     * 
     * @see javax.swing.filechooser.FileSystemView#createNewFolder(java.io.File)
     */
    @Override
    public File createNewFolder(File containingDir) throws IOException {
        return org.createNewFolder(containingDir);
    }

}
