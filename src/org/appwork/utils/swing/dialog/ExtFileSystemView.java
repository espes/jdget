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
import java.io.FileNotFoundException;
import java.io.IOException;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.ArrayList;
import java.util.List;

import javax.swing.Icon;
import javax.swing.filechooser.FileSystemView;

import org.appwork.utils.locale._AWU;
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
    public static boolean SAMBA_SCANNED = false;

    public static void runSambaScanner() {
        if (ExtFileSystemView.SAMBA_SCANNED) { return; }
        ExtFileSystemView.SAMBA_SCANNED = true;

        final long tt = System.currentTimeMillis();
        new Thread("Networkfolder Loader") {
            @Override
            public void run() {
                final ExtFileSystemView view = new ExtFileSystemView();
                view.getRoots();

                try {

                    if (view.networkFolder != null) {

                        view.networkFolder.listFiles();
                        Log.L.info("List Networkfolder done " + (System.currentTimeMillis() - tt));
                    }
                } catch (final Exception e) {
                    e.printStackTrace();
                }
            }
        }.start();

    }

    private final FileSystemView org;
    private File[]               roots;
    private NetWorkFolder        networkFolder;

    /**
     * 
     */
    public static final String   VIRTUAL_NETWORKFOLDER    = "::{F02C1A0D-BE21-4350-88B0-7367FC96EF3C}";
    public static final String   VIRTUAL_NETWORKFOLDER_XP = "::{208D2C60-3AEA-1069-A2D7-08002B30309D}";

    /**
     */
    public ExtFileSystemView() {
        this.org = FileSystemView.getFileSystemView();
        if (ExtFileSystemView.SAMBA_SCANNED) {
            new Exception("run ExtFileSystemView.runSambaScanner() as early as possible in your app!");
            ExtFileSystemView.runSambaScanner();
        }

    }

    @Override
    public File createFileObject(final File dir, final String filename) {

        return this.org.createFileObject(dir, filename);
    }

    @Override
    public File createFileObject(final String path) {

        return this.org.createFileObject(path);
    }

    /*
     * (non-Javadoc)
     * 
     * @see javax.swing.filechooser.FileSystemView#createNewFolder(java.io.File)
     */
    @Override
    public File createNewFolder(final File containingDir) throws IOException {
        return this.org.createNewFolder(containingDir);
    }

    @Override
    public File getChild(final File parent, final String fileName) {

        return this.org.getChild(parent, fileName);
    }

    @Override
    public File getDefaultDirectory() {

        return this.org.getDefaultDirectory();
    }

    @Override
    public File[] getFiles(File dir, final boolean useFileHiding) {
        final long t = System.currentTimeMillis();
        try {

            final File[] ret;

            if (dir == networkFolder) {

                ret = this.getFilesShellfolder((NetWorkFolder) dir, useFileHiding);
            } else {
                ret = this.org.getFiles(dir, useFileHiding);
            }

            final java.util.List<File> filtered = new ArrayList<File>();
            for (final File f : ret) {
                if (f.getName().equals(ExtFileSystemView.VIRTUAL_NETWORKFOLDER)) {
                    filtered.add(new NetWorkFolder(f));
                    continue;
                } else if (f.getName().equals(ExtFileSystemView.VIRTUAL_NETWORKFOLDER_XP)) {
                    filtered.add(new NetWorkFolder(f));
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

    public File[] getFilesShellfolder(NetWorkFolder network, boolean useFileHiding) {
        List<File> files = new ArrayList<File>();

        File[] names = network.listFiles(useFileHiding);
        if (names == null) { return new File[0]; }

        for (File f : names) {
            if (Thread.currentThread().isInterrupted()) {
                break;
            }

            if (!(f instanceof ShellFolder)) {
                if (isFileSystemRoot(f)) {
                    f = createFileSystemRoot(f);
                }
                try {
                    f = ShellFolder.getShellFolder(f);
                } catch (FileNotFoundException e) {
                    // Not a valid file (wouldn't show in native file chooser)
                    // Example: C:\pagefile.sys
                    continue;
                } catch (InternalError e) {
                    // Not a valid file (wouldn't show in native file chooser)
                    // Example C:\Winnt\Profiles\joe\history\History.IE5
                    continue;
                }
            }
            if (!useFileHiding || !isHiddenFile(f)) {
                files.add(f);
            }
        }

        return files.toArray(new File[files.size()]);
    }

    @Override
    public File getHomeDirectory() {

        return this.org.getHomeDirectory();
    }

    /**
     * @return
     */
    public NetWorkFolder getNetworkFolder() {
        // TODO Auto-generated method stub
        return networkFolder;
    }

    @Override
    public File getParentDirectory(final File dir) {

        return this.org.getParentDirectory(dir);
    }

    @Override
    public File[] getRoots() {
        final long t = System.currentTimeMillis();
        Log.L.info("Get Roots");
        if (this.roots != null) { return this.roots; }
        try {

            // this may take a long time on some systems.

            final File desktopPath = new File(System.getProperty("user.home") + "/Desktop");
            final File[] rootFiles = File.listRoots();
            Log.L.info("Listed roots " + (System.currentTimeMillis() - t));
            final File[] desktopList = desktopPath.listFiles();
            Log.L.info("Listed desktop " + (System.currentTimeMillis() - t));

            final File[] baseFolders = AccessController.doPrivileged(new PrivilegedAction<File[]>() {
                public File[] run() {
                    return (File[]) ShellFolder.get("fileChooserComboBoxFolders");
                }
            });

            Log.L.info("Listed Base folders " + (System.currentTimeMillis() - t));
            final java.util.List<File> newRoots = new ArrayList<File>();

            final File home = this.getHomeDirectory();
            for (final File f : baseFolders) {
                // Win32ShellFolder2.class
                if (f.getName().equals("Recent")) {
                    continue;
                }
                if (f.getParentFile() == null || !f.getParentFile().equals(home)) {
                    newRoots.add(f);
                } else if (f.getName().equals(ExtFileSystemView.VIRTUAL_NETWORKFOLDER)) {
                    this.networkFolder = new NetWorkFolder(f);
                } else if (f.getName().equals(ExtFileSystemView.VIRTUAL_NETWORKFOLDER_XP)) {
                    this.networkFolder = new NetWorkFolder(f);
                }
                Log.L.info("Basefolder: " + f.getName() + " - " + CrossSystem.getOSString());

            }

            final HomeFolder[] homeFolders = new HomeFolder[] { new HomeFolder(HomeFolder.DOCUMENTS, "documents"), new HomeFolder(HomeFolder.PICTURES, "images"), new HomeFolder(HomeFolder.VIDEOS, "videos"), new HomeFolder(HomeFolder.DOWNLOADS, "downloads"), new HomeFolder(HomeFolder.MUSIC, "music") };

            for (final HomeFolder hf : homeFolders) {
                if (hf.exists()) {
                    newRoots.add(hf);
                }
            }

            if (this.networkFolder != null) {
                newRoots.add(this.networkFolder);
            }
            this.roots = newRoots.toArray(new File[] {});
            return this.roots;
        } finally {
            Log.L.info("Roots: " + (System.currentTimeMillis() - t));

        }
    }

    @Override
    public String getSystemDisplayName(final File f) {
        if (f == networkFolder) { return _AWU.T.DIALOG_FILECHOOSER_networkfolder(); }
        return this.org.getSystemDisplayName(f);
    }

    @Override
    public Icon getSystemIcon(final File f) {

        return this.org.getSystemIcon(f);
    }

    @Override
    public String getSystemTypeDescription(final File f) {

        return this.org.getSystemTypeDescription(f);
    }

    @Override
    public boolean isComputerNode(final File dir) {

        return this.org.isComputerNode(dir);
    }

    @Override
    public boolean isDrive(final File dir) {

        return this.org.isDrive(dir);
    }

    @Override
    public boolean isFileSystem(final File f) {

        return this.org.isFileSystem(f);
    }

    @Override
    public boolean isFileSystemRoot(final File dir) {

        return this.org.isFileSystemRoot(dir);
    }

    @Override
    public boolean isFloppyDrive(final File dir) {

        return this.org.isFloppyDrive(dir);
    }

    @Override
    public boolean isHiddenFile(final File f) {

        return this.org.isHiddenFile(f);
    }

    @Override
    public boolean isParent(final File folder, final File file) {

        return this.org.isParent(folder, file);
    }

    @Override
    public boolean isRoot(final File f) {

        return this.org.isRoot(f);
    }

    @Override
    public Boolean isTraversable(final File f) {

        return this.org.isTraversable(f);
    }

}
