/**
 * Copyright (c) 2009 - 2010 AppWork UG(haftungsbeschränkt) <e-mail@appwork.org>
 * 
 * This file is part of org.appwork.utils.os
 * 
 * This software is licensed under the Artistic License 2.0,
 * see the LICENSE file or http://www.opensource.org/licenses/artistic-license-2.0.php
 * for details
 */
package org.appwork.utils.os;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.logging.Level;

import javax.swing.filechooser.FileFilter;

import org.appwork.exceptions.WTFException;
import org.appwork.shutdown.ShutdownController;
import org.appwork.shutdown.ShutdownEvent;
import org.appwork.storage.JSonStorage;
import org.appwork.storage.StorageException;
import org.appwork.utils.Regex;
import org.appwork.utils.StringUtils;
import org.appwork.utils.locale._AWU;
import org.appwork.utils.logging.Log;
import org.appwork.utils.os.mime.Mime;
import org.appwork.utils.os.mime.MimeDefault;
import org.appwork.utils.os.mime.MimeLinux;
import org.appwork.utils.os.mime.MimeWindows;
import org.appwork.utils.processes.ProcessBuilderFactory;
import org.appwork.utils.swing.dialog.Dialog;
import org.appwork.utils.swing.dialog.DialogCanceledException;
import org.appwork.utils.swing.dialog.DialogClosedException;
import org.appwork.utils.swing.dialog.DialogNoAnswerException;
import org.appwork.utils.swing.dialog.ExtFileChooserDialog;
import org.appwork.utils.swing.dialog.FileChooserSelectionMode;
import org.appwork.utils.swing.dialog.FileChooserType;

/**
 * This class provides a few native features.
 * 
 * @author $Author: unknown$
 */

public class CrossSystem {

    public static final byte      OS_LINUX_OTHER         = 6;
    public static final byte      OS_MAC_OTHER           = 5;
    public static final byte      OS_WINDOWS_OTHER       = 4;
    public static final byte      OS_WINDOWS_NT          = 3;
    public static final byte      OS_WINDOWS_2000        = 2;
    public static final byte      OS_WINDOWS_XP          = 0;
    public static final byte      OS_WINDOWS_2003        = 7;
    public static final byte      OS_WINDOWS_VISTA       = 1;
    public static final byte      OS_WINDOWS_7           = 8;
    public static final byte      OS_WINDOWS_SERVER_2008 = 9;
    public static final byte      OS_WINDOWS_8           = 10;

    private static String         JAVAINT                = null;
    private static DesktopSupport desktopSupport         = null;

    /**
     * Cache to store the OS string in
     */
    private final static String   OS_STRING;

    /**
     * Cache to store the OS ID in
     */
    public final static byte      OS_ID;

    /**
     * Cache to store the Mime Class in
     */
    private static final Mime     MIME;
    private static String[]       BROWSER_COMMANDLINE    = null;
    private static String[]       FILE_COMMANDLINE       = null;
    private static Boolean        OS64BIT                = null;
    static {
        /* Init OS_ID */
        OS_STRING = System.getProperty("os.name");
        OS_ID = CrossSystem.getOSID(CrossSystem.OS_STRING);
        /* Init MIME */
        if (CrossSystem.isWindows()) {
            MIME = new MimeWindows();
            CrossSystem.desktopSupport = new DesktopSupportWindows();
        } else if (CrossSystem.isLinux()) {
            CrossSystem.desktopSupport = new DesktopSupportLinux();
            MIME = new MimeLinux();
        } else {
            CrossSystem.desktopSupport = new DesktopSupportJavaDesktop();
            MIME = new MimeDefault();
        }
    }

    /**
     * internal function to open a file/folder
     * 
     * @param file
     * @throws IOException
     */
    private static void _openFILE(final File file) throws IOException {
        if (!CrossSystem.openCustom(CrossSystem.FILE_COMMANDLINE, file.getAbsolutePath())) {
            CrossSystem.desktopSupport.openFile(file);
        }
    }

    /**
     * internal function to open an URL in a browser
     * 
     * @param _url
     * @throws IOException
     * @throws URISyntaxException
     */
    private static void _openURL(final String _url) throws IOException, URISyntaxException {
        if (!CrossSystem.openCustom(CrossSystem.BROWSER_COMMANDLINE, _url)) {
            CrossSystem.desktopSupport.browseURL(new URL(_url));
        }
    }

    /**
     * use this method to make pathPart safe to use in a full absoluePath.
     * 
     * it will remove driveletters/path seperators and all known chars that are
     * forbidden in a path
     * 
     * @param pathPart
     * @return
     */
    public static String alleviatePathParts(String pathPart) {
        if (StringUtils.isEmpty(pathPart)) {
            if (pathPart != null) { return pathPart; }
            return null;
        }
        /* remove invalid chars */
        pathPart = pathPart.replaceAll("([\\\\|<|>|\\||\"|:|\\*|\\?|/|\\x00])+", "_");
        /*
         * remove ending points, not allowed under windows and others os maybe
         * too
         */
        pathPart = pathPart.replaceFirst("\\.+$", "");
        if (CrossSystem.isWindows()) {
            if (new Regex(pathPart, "^(CON|PRN|AUX|NUL|COM\\d+|LPT\\d+|CLOCK\\$)$").matches()) {
                pathPart = "_" + pathPart;
            }
        }
        return pathPart.trim();
    }

    public static String[] getBrowserCommandLine() {
        return CrossSystem.BROWSER_COMMANDLINE;
    }

    public static String[] getEditor(final String extension) throws DialogCanceledException, DialogClosedException, StorageException {

        final ExtFileChooserDialog d = new ExtFileChooserDialog(0, _AWU.T.fileditcontroller_geteditor_for(extension), null, null);
        d.setStorageID("FILE_EDIT_CONTROLLER_" + extension);
        d.setFileSelectionMode(FileChooserSelectionMode.FILES_ONLY);
        d.setFileFilter(new FileFilter() {

            @Override
            public boolean accept(final File f) {
                if (f.isDirectory()) { return true; }
                if (CrossSystem.isWindows()) {
                    return f.getName().endsWith(".exe");
                } else {
                    return f.canExecute();
                }

            }

            @Override
            public String getDescription() {

                return _AWU.T.fileeditcontroller_exechooser_description(extension);

            }

        });
        d.setType(FileChooserType.OPEN_DIALOG_WITH_PRESELECTION);
        d.setMultiSelection(false);
        d.setPreSelection(new File(JSonStorage.getPlainStorage("EDITORS").get(extension, "")));
        try {
            Dialog.I().showDialog(d);
        } catch (final DialogClosedException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (final DialogCanceledException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        final File ret = d.getSelectedFile();
        if (ret != null && ret.exists()) {
            JSonStorage.getPlainStorage("EDITORS").put(extension, ret.toString());
            return new String[] { ret.toString() };
        } else {
            return null;
        }

    }

    public static String[] getFileCommandLine() {
        return CrossSystem.FILE_COMMANDLINE;
    }

    public static byte getID() {
        return CrossSystem.OS_ID;
    }

    public static double getSystemCPUUsage() {

        try {
            java.lang.management.OperatingSystemMXBean operatingSystemMXBean = java.lang.management.ManagementFactory.getOperatingSystemMXBean();
            double sysload = operatingSystemMXBean.getSystemLoadAverage();
            if (sysload < 0) {
                java.lang.reflect.Method method = operatingSystemMXBean.getClass().getDeclaredMethod("getSystemCpuLoad", new Class[] {});
                method.setAccessible(true);
                sysload = (Double) method.invoke(operatingSystemMXBean, new Object[] {});

            }
            return sysload;
        } catch (Throwable e) {
            return -1;
        }
    }

    public static String getJavaBinary() {
        if (CrossSystem.JAVAINT != null) { return CrossSystem.JAVAINT; }
        String javaBinary = "java";
        if (CrossSystem.isWindows()) {
            javaBinary = "javaw.exe";
        }
        final String javaHome = System.getProperty("java.home");
        if (javaHome != null) {
            /* get path from system property */
            final File java = new File(new File(javaHome), "/bin/" + javaBinary);
            if (java.exists() && java.isFile()) {
                CrossSystem.JAVAINT = java.getAbsolutePath();

            }
        } else {
            CrossSystem.JAVAINT = javaBinary;
        }
        return CrossSystem.JAVAINT;
    }

    /**
     * Returns the Mime Class for the current OS
     * 
     * @return
     * @see Mime
     */
    public static Mime getMime() {
        return CrossSystem.MIME;
    }

    /**
     * @param osString
     * @return
     */
    public static byte getOSID(final String osString) {
        if (osString == null) {
            /* fallback to latest Windows */
            return CrossSystem.OS_WINDOWS_7;
        }
        final String OS = osString.toLowerCase();
        if (OS.contains("windows 8")) {
            return CrossSystem.OS_WINDOWS_8;
        } else if (OS.contains("windows 7")) {
            return CrossSystem.OS_WINDOWS_7;
        } else if (OS.contains("windows xp")) {
            return CrossSystem.OS_WINDOWS_XP;
        } else if (OS.contains("windows vista")) {
            return CrossSystem.OS_WINDOWS_VISTA;
        } else if (OS.contains("windows 2000")) {
            return CrossSystem.OS_WINDOWS_2000;
        } else if (OS.contains("windows 2003")) {
            return CrossSystem.OS_WINDOWS_2003;
        } else if (OS.contains("windows server 2008")) {
            return CrossSystem.OS_WINDOWS_SERVER_2008;
        } else if (OS.contains("nt")) {
            return CrossSystem.OS_WINDOWS_NT;
        } else if (OS.contains("windows")) {
            return CrossSystem.OS_WINDOWS_OTHER;
        } else if (OS.contains("mac")) {
            return CrossSystem.OS_MAC_OTHER;
        } else {
            return CrossSystem.OS_LINUX_OTHER;
        }

    }

    public static String getOSString() {
        return CrossSystem.OS_STRING;
    }

    public static boolean is64BitOperatingSystem() {
        if (CrossSystem.OS64BIT != null) { return CrossSystem.OS64BIT; }
        boolean ret = false;
        if (org.appwork.utils.Application.is64BitJvm()) {
            /*
             * we are running a 64bit jvm, so the underlying os must be 64bit
             * too
             */
            ret = true;
        } else if (CrossSystem.isMac()) {
            /* mac is always 64bit os */
            ret = true;
        } else if (CrossSystem.isLinux()) {
            Process p = null;
            Boolean ret2 = null;
            try {
                final Runtime r = Runtime.getRuntime();
                p = r.exec("uname -m");
                p.waitFor();
                final BufferedReader b = new BufferedReader(new InputStreamReader(p.getInputStream()));
                final String arch = b.readLine();
                if (arch != null && arch.contains("x86_64")) {
                    ret2 = true;
                }
            } catch (final Throwable e) {
            } finally {
                try {
                    p.destroy();
                } catch (final Throwable e2) {
                }
            }
            if (ret2 == null) {
                final String hostType = System.getenv("HOSTTYPE");
                if (hostType != null && hostType.contains("x86_64")) {
                    ret2 = true;
                }
            }
            if (ret2 != null) {
                ret = ret2;
            }
        } else if (CrossSystem.isWindows()) {
            if (System.getenv("ProgramFiles(x86)") != null) {
                ret = true;
            }
            if (System.getenv("ProgramW6432") != null) {
                ret = true;
            }
        }
        CrossSystem.OS64BIT = ret;
        return ret;
    }

    /**
     * checks if given path is absolute or relative
     * 
     * @param path
     * @return
     */
    public static boolean isAbsolutePath(final String path) {
        if (StringUtils.isEmpty(path)) { return false; }
        if (CrossSystem.isWindows() && path.matches("\\\\\\\\.+\\\\.+")) { return true; }
        if (CrossSystem.isWindows() && path.matches(".:/.*")) { return true; }
        if (CrossSystem.isWindows() && path.matches(".:\\\\.*")) { return true; }
        if (!CrossSystem.isWindows() && path.startsWith("/")) { return true; }

        return false;
    }

    /**
     * Returns true if the OS is a linux system
     * 
     * @return
     */
    public static boolean isLinux() {
        return CrossSystem.isLinux(CrossSystem.OS_ID);
    }

    public static boolean isLinux(final byte osID) {
        return osID == CrossSystem.OS_LINUX_OTHER;
    }

    /**
     * Returns true if the OS is a MAC System
     * 
     * @return
     */
    public static boolean isMac() {
        return CrossSystem.isMac(CrossSystem.OS_ID);
    }

    public static boolean isMac(final byte osID) {
        return osID == CrossSystem.OS_MAC_OTHER;
    }

    /**
     * returns true in case of "open an URL in a browser" is supported
     * 
     * @return
     */
    public static boolean isOpenBrowserSupported() {
        return CrossSystem.desktopSupport.isBrowseURLSupported();
    }

    /**
     * returns true in case of "open a File" is supported
     * 
     * @return
     */
    public static boolean isOpenFileSupported() {
        return CrossSystem.desktopSupport.isOpenFileSupported();
    }

    /**
     * Returns true if the OS is a Windows System
     * 
     * @return
     */
    public static boolean isWindows() {
        return CrossSystem.isWindows(CrossSystem.OS_ID);
    }

    /**
     * @param osId
     * @return
     */
    public static boolean isWindows(final byte osId) {
        switch (osId) {
        case OS_WINDOWS_8:
        case OS_WINDOWS_XP:
        case OS_WINDOWS_VISTA:
        case OS_WINDOWS_2000:
        case OS_WINDOWS_2003:
        case OS_WINDOWS_NT:
        case OS_WINDOWS_OTHER:
        case OS_WINDOWS_7:
        case OS_WINDOWS_SERVER_2008:
            return true;
        }
        return false;
    }

    private static boolean openCustom(final String[] custom, final String what) throws IOException {
        if (custom == null || custom.length < 1) { return false; }
        boolean added = false;
        final java.util.List<String> commands = new ArrayList<String>();
        for (final String s : custom) {
            final String add = s.replace("%s", what);
            if (!add.equals(s)) {
                added = true;
            }
            commands.add(add);
        }
        if (added == false) {
            commands.add(what);
        }
        Runtime.getRuntime().exec(commands.toArray(new String[] {}));
        return true;
    }

    /**
     * Opens a file or directory
     * 
     * @see java.awt.Desktop#open(File)
     * @param file
     * @throws IOException
     */
    public static void openFile(final File file) {

        try {
            CrossSystem._openFILE(file);
        } catch (final IOException e) {
            Log.exception(e);
        }

    }

    /**
     * Open an url in the systems default browser
     * 
     * @param url
     */
    public static void openURL(final String url) {
        try {
            CrossSystem._openURL(url);
        } catch (final Throwable e) {
            Log.exception(Level.WARNING, e);
        }
    }

    public static void openURL(final URL url) {
        CrossSystem.openURL(url.toString());
    }

    /**
     * @param update_dialog_news_button_url
     */
    public static void openURLOrShowMessage(final String urlString) {
        try {
            CrossSystem._openURL(urlString);
        } catch (final Throwable e) {
            Log.exception(Level.WARNING, e);
            try {
                Dialog.getInstance().showInputDialog(Dialog.BUTTONS_HIDE_CANCEL, _AWU.T.crossSystem_open_url_failed_msg(), urlString);
            } catch (final DialogNoAnswerException donothing) {
            }
        }
    }

    /**
     * @param class1
     */
    public static void restartApplication(final File jar, final String... parameters) {

        try {
            Log.L.info("restartApplication " + jar + " " + parameters.length);
            final java.util.List<String> nativeParameters = new ArrayList<String>();
            File runin = null;
            if (CrossSystem.isMac()) {

                // find .app
                File rootpath = jar;
                final HashSet<File> loopMap = new HashSet<File>();
                while (rootpath != null && loopMap.add(rootpath)) {
                    if (rootpath.getName().endsWith(".app")) {
                        break;

                    }
                    rootpath = rootpath.getParentFile();

                }
                if (rootpath.getName().endsWith(".app")) {

                    // found app.- restart it.

                    nativeParameters.add("open");
                    nativeParameters.add("-n");
                    nativeParameters.add(rootpath.getAbsolutePath());
                    runin = rootpath.getParentFile();

                }

            }
            if (nativeParameters.isEmpty()) {
                Log.L.info("Find Jarfile");
                final File jarFile = jar;
                Log.L.info("Find Jarfile " + jarFile);
                runin = jarFile.getParentFile();
                if (CrossSystem.isWindows()) {
                    final File exeFile = new File(jarFile.getParentFile(), jarFile.getName().substring(0, jarFile.getName().length() - 4) + ".exe");
                    if (exeFile.exists()) {
                        nativeParameters.add(exeFile.getAbsolutePath());
                    } else {
                        nativeParameters.add(CrossSystem.getJavaBinary());
                        nativeParameters.add("-jar");
                        nativeParameters.add(jarFile.getAbsolutePath());
                    }
                } else {
                    nativeParameters.add(CrossSystem.getJavaBinary());
                    nativeParameters.add("-jar");
                    nativeParameters.add(jarFile.getAbsolutePath());
                }

            }

            if (parameters != null) {
                for (final String s : parameters) {
                    nativeParameters.add(s);
                }
            }
            Log.L.info("Start " + nativeParameters);
            final ProcessBuilder pb = ProcessBuilderFactory.create(nativeParameters.toArray(new String[] {}));
            /*
             * needed because the root is different for jre/class version
             */

            Log.L.info("Root: " + runin);
            if (runin != null) {
                pb.directory(runin);
            }

            ShutdownController.getInstance().addShutdownEvent(new ShutdownEvent() {
                {
                    this.setHookPriority(Integer.MIN_VALUE);
                }

                @Override
                public void run() {
                    try {
                        pb.start();
                    } catch (final IOException e) {
                        Log.exception(e);
                    }

                }
            });

            Log.L.info("Start " + ShutdownController.getInstance().requestShutdown(true));

        } catch (final Throwable e) {
            throw new WTFException(e);
        }

    }

    /**
     * Set commandline to open the browser use %s as wildcard for the url
     * 
     * @param commands
     */
    public static void setBrowserCommandLine(final String[] commands) {
        CrossSystem.BROWSER_COMMANDLINE = commands;
    }

    public static void setFileCommandLine(final String[] fILE_COMMANDLINE) {
        CrossSystem.FILE_COMMANDLINE = fILE_COMMANDLINE;
    }

    /**
     * splits filename into name,extension
     * 
     * @param filename
     * @return
     */
    public static String[] splitFileName(final String filename) {
        final String extension = new Regex(filename, "\\.+([^\\.]*$)").getMatch(0);
        final String name = new Regex(filename, "(.*?)(\\.+[^\\.]*$|$)").getMatch(0);
        return new String[] { name, extension };
    }

    public static void main(String[] args) {
        showInExplorer(new File("C:\\Users\\Thomas\\.jd_home\\tmp\\hosts.json"));
    }

    /**
     * @param saveTo
     */
    public static void showInExplorer(File saveTo) {
        if (CrossSystem.isWindows()) {
            try {
                ProcessBuilderFactory.create("explorer.exe", "/select," + saveTo.getAbsolutePath()).start();
                return;
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        } 
            if (saveTo.isDirectory()) {
                openFile(saveTo);
            } else {
                openFile(saveTo.getParentFile());
            }

        

    }
}