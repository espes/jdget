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
import org.appwork.utils.os.CrossSystem;

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
    private static boolean SAMBA_SCANNED = false;

    public static void runSambaScanner() {
        if (SAMBA_SCANNED) return;
        SAMBA_SCANNED = true;

        final long tt = System.currentTimeMillis();
        new Thread("Networkfolder Loader") {
            public void run() {
                ExtFileSystemView view = new ExtFileSystemView();
                view.getRoots();

                try {

                    if (view.networkFolder != null) {

                        SAMBA_FOLDERS = view.networkFolder.listFiles();
                        Log.L.info("List Networkfolder done " + (System.currentTimeMillis() - tt));
                    }
                } catch (Exception e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
        }.start();

    }

    private FileSystemView     org;
    private File[]             roots;
    private File               networkFolder;
    private static File[]      SAMBA_FOLDERS            = null;
    /**
     * 
     */
    public static final String VIRTUAL_NETWORKFOLDER    = "::{F02C1A0D-BE21-4350-88B0-7367FC96EF3C}";
    public static final String VIRTUAL_NETWORKFOLDER_XP = "::{208D2C60-3AEA-1069-A2D7-08002B30309D}";

    /**
     */
    public ExtFileSystemView() {
        org = FileSystemView.getFileSystemView();
        if (SAMBA_SCANNED) {
            new Exception("run ExtFileSystemView.runSambaScanner() as early as possible in your app!");
            runSambaScanner();
        }

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
        Log.L.info("Get Roots");
        if (roots != null) return roots;
        try {

            // this may take a long time on some systems.

            File desktopPath = new File(System.getProperty("user.home") + "/Desktop");
            File[] rootFiles = File.listRoots();
            Log.L.info("Listed roots " + (System.currentTimeMillis() - t));
            File[] desktopList = desktopPath.listFiles();
            Log.L.info("Listed desktop " + (System.currentTimeMillis() - t));

            File[] baseFolders = AccessController.doPrivileged(new PrivilegedAction<File[]>() {
                public File[] run() {
                    return (File[]) ShellFolder.get("fileChooserComboBoxFolders");
                }
            });

            Log.L.info("Listed Base folders " + (System.currentTimeMillis() - t));
            java.util.List<File> newRoots = new ArrayList<File>();

            File home = getHomeDirectory();
            for (File f : baseFolders) {
                // Win32ShellFolder2.class
                if (f.getName().equals("Recent")) continue;
                if (f.getParentFile() == null || !f.getParentFile().equals(home)) {
                    newRoots.add(f);
                } else if (f.getName().equals(VIRTUAL_NETWORKFOLDER)) {
                    networkFolder = f;
                } else if (f.getName().equals(VIRTUAL_NETWORKFOLDER_XP)) {
                    networkFolder = f;
                }
                Log.L.info("Basefolder: " + f.getName() + " - " + CrossSystem.getOSString());

            }

            HomeFolder[] homeFolders = new HomeFolder[] { new HomeFolder(HomeFolder.DOCUMENTS, "documents"), new HomeFolder(HomeFolder.PICTURES, "images"), new HomeFolder(HomeFolder.VIDEOS, "videos"), new HomeFolder(HomeFolder.DOWNLOADS, "downloads"), new HomeFolder(HomeFolder.MUSIC, "music") };

            for (HomeFolder hf : homeFolders) {
                if (hf.exists()) newRoots.add(hf);
            }
            if (SAMBA_FOLDERS == null) {
                Log.L.warning("Did not run SAMBA_ SCANNER YET");
            }

            if (networkFolder != null && (SAMBA_FOLDERS != null && SAMBA_FOLDERS.length > 0)) {
                newRoots.add(networkFolder);
            }
            roots = newRoots.toArray(new File[] {});
            return roots;
        } finally {
            Log.L.info("Roots: " + (System.currentTimeMillis() - t));

        }
    }

    public File[] getSAMBA_FOLDERS() {
        return SAMBA_FOLDERS;
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
            java.util.List<File> filtered = new ArrayList<File>();
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

    /**
     * @return
     */
    public File getNetworkFolder() {
        // TODO Auto-generated method stub
        return null;
    }

}
