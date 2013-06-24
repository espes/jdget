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
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;

import javax.swing.Icon;
import javax.swing.filechooser.FileSystemView;

import org.appwork.resources.AWUTheme;
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
    private HashMap<File, File>  specialsMap;

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
    public File[] getFiles(final File dir, final boolean useFileHiding) {
        final long t = System.currentTimeMillis();
        try {

            final File[] ret;

            if (dir == this.networkFolder) {

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
            Log.L.info("Return Files for " + dir + "(" + useFileHiding + "): " + filtered.size());
            return filtered.toArray(new File[] {});
        } finally {
            Log.L.info("getFiles: ms:" + (System.currentTimeMillis() - t) + " " + dir);

        }
    }

    public File[] getFilesShellfolder(final NetWorkFolder network, final boolean useFileHiding) {
        final List<File> files = new ArrayList<File>();

        final File[] names = network.listFiles(useFileHiding);
        if (names == null) { return new File[0]; }

        for (File f : names) {
            if (Thread.currentThread().isInterrupted()) {
                break;
            }

            if (!(f instanceof ShellFolder)) {
                if (this.isFileSystemRoot(f)) {
                    f = this.createFileSystemRoot(f);
                }
                try {
                    f = ShellFolder.getShellFolder(f);
                } catch (final FileNotFoundException e) {
                    // Not a valid file (wouldn't show in native file chooser)
                    // Example: C:\pagefile.sys
                    continue;
                } catch (final InternalError e) {
                    // Not a valid file (wouldn't show in native file chooser)
                    // Example C:\Winnt\Profiles\joe\history\History.IE5
                    continue;
                }
            }
            if (!useFileHiding || !this.isHiddenFile(f)) {
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
        return this.networkFolder;
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
            final File[] baseFolders = AccessController.doPrivileged(new PrivilegedAction<File[]>() {
                public File[] run() {
                    return (File[]) ShellFolder.get("fileChooserComboBoxFolders");
                }
            });

            Log.L.info("Listed Base folders " + (System.currentTimeMillis() - t));
            final LinkedHashSet<File> unique = new LinkedHashSet<File>() {
                /*
                 * (non-Javadoc)
                 * 
                 * @see java.util.HashSet#add(java.lang.Object)
                 */
                @Override
                public boolean add(final File e) {
                    if (this.contains(e)) { return false; }
                    return super.add(e);
                }
            };

            final File desktopPath = new File(System.getProperty("user.home") + "/Desktop");
            if (desktopPath.exists() && desktopPath.isDirectory()) {
                unique.add(desktopPath);
            }

            this.mount(new File("/Volumes"), unique);
            this.mount(new File("/media"), unique);

            final HomeFolder[] homeFolders = new HomeFolder[] { new HomeFolder(HomeFolder.DOCUMENTS, "documents"), new HomeFolder(HomeFolder.PICTURES, "images"), new HomeFolder(HomeFolder.VIDEOS, "videos"), new HomeFolder(HomeFolder.DOWNLOADS, "downloads"), new HomeFolder(HomeFolder.MUSIC, "music") };
            for (final HomeFolder hf : homeFolders) {
                if (hf.exists()) {
                    unique.add(hf);
                }
            }

            if (this.networkFolder != null) {
                unique.add(this.networkFolder);
            }
            final File home = this.getHomeDirectory();
            for (final File f : baseFolders) {
                // Win32ShellFolder2.class
                if (f.getName().equals("Recent")) {
                    continue;
                }
                if (f.getParentFile() == null || !f.getParentFile().equals(home)) {
                    unique.add(f);
                } else if (f.getName().equals(ExtFileSystemView.VIRTUAL_NETWORKFOLDER)) {
                    this.networkFolder = new NetWorkFolder(f);
                } else if (f.getName().equals(ExtFileSystemView.VIRTUAL_NETWORKFOLDER_XP)) {
                    this.networkFolder = new NetWorkFolder(f);
                }
                Log.L.info("Basefolder: " + f.getName() + " - " + f + " - " + CrossSystem.getOSString());
            }
            final File[] nroots = unique.toArray(new File[] {});
            final HashMap<File, File> nspecialsMap = new HashMap<File, File>();
            for (final File f : nroots) {
                nspecialsMap.put(f, f);
            }
            this.specialsMap = nspecialsMap;
            this.roots = nroots;
            return this.roots;
        } finally {
            Log.L.info("Roots: " + (System.currentTimeMillis() - t));

        }
    }

    @Override
    public String getSystemDisplayName(final File f) {
        if (f == this.networkFolder) { return _AWU.T.DIALOG_FILECHOOSER_networkfolder(); }
        if (f instanceof VirtualRoot) { return f.getName(); }
        return this.org.getSystemDisplayName(f);
    }

    @Override
    public Icon getSystemIcon(final File f) {
        try {
            if (f instanceof VirtualRoot) {

            return AWUTheme.I().getIcon("root", 18); }

            return this.org.getSystemIcon(f);

        } catch (final Exception e) {
            // seems like getSystemIcon can throw a FileNotFoundException or a
            // Nullpointerfor
            // 1.6 java
            return null;
        }
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

    /**
     * @param f
     * @return
     */
    public File mapSpecialFolders(final File f) {
        final File ret = this.specialsMap.get(f);
        return ret != null ? ret : f;
    }

    private void mount(final File path, final LinkedHashSet<File> files) {
        if (path.exists() && path.isDirectory()) {
            final File[] content = path.listFiles();
            if (content != null) {
                for (final File f : content) {
                    if (f.isHidden()) {
                        continue;
                    }
                    final VirtualRoot vFile = new VirtualRoot(f, f.getName());
                    files.add(vFile);
                }
            }
        }
    }

}
