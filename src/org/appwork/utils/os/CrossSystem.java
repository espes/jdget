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
import org.appwork.shutdown.ShutdownRequest;
import org.appwork.storage.JSonStorage;
import org.appwork.storage.StorageException;
import org.appwork.uio.UIOManager;
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

    private static String         JAVAINT             = null;
    private static DesktopSupport DESKTOP_SUPPORT     = null;

    /**
     * Cache to store the OS string in
     */
    private final static String   OS_STRING;

    /**
     * Cache to store the Mime Class in
     */
    private static final Mime     MIME;

    private static String[]       BROWSER_COMMANDLINE = null;
    private static String[]       FILE_COMMANDLINE    = null;
    private static Boolean        OS64BIT             = null;
    public static OperatingSystem OS;
    static {
        /* Init OS_ID */
        OS_STRING = System.getProperty("os.name");
        OS = CrossSystem.getOSByString(CrossSystem.OS_STRING);

        /* Init MIME */
        if (CrossSystem.isWindows()) {
            MIME = new MimeWindows();
            CrossSystem.DESKTOP_SUPPORT = new DesktopSupportWindows();
        } else if (CrossSystem.isLinux()) {
            CrossSystem.DESKTOP_SUPPORT = new DesktopSupportLinux();
            MIME = new MimeLinux();
        } else {
            CrossSystem.DESKTOP_SUPPORT = new DesktopSupportJavaDesktop();
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
            CrossSystem.DESKTOP_SUPPORT.openFile(file);
        }

    }

    public static enum OperatingSystem {
        WINDOWS_8(OSFamily.WINDOWS),
        WINDOWS_7(OSFamily.WINDOWS),
        WINDOWS_XP(OSFamily.WINDOWS),
        WINDOWS_VISTA(OSFamily.WINDOWS),
        WINDOWS_2000(OSFamily.WINDOWS),
        WINDOWS_2003(OSFamily.WINDOWS),
        WINDOWS_SERVER_2008(OSFamily.WINDOWS),
        WINDOWS_NT(OSFamily.WINDOWS),
        WINDOWS_OTHERS(OSFamily.WINDOWS),
        MAC(OSFamily.MAC),
        OS2(OSFamily.OS2),
        LINUX(OSFamily.LINUX);

        private OSFamily family;

        public OSFamily getFamily() {
            return family;
        }

        private OperatingSystem(final OSFamily family) {

            this.family = family;
        }
    }

    /**
     * @param osString
     * @return
     */
    private static OperatingSystem getOSByString(final String osString) {
        if (osString == null) {
            /* fallback to latest Windows */
            return OperatingSystem.WINDOWS_8;
        }
        final String os = osString.toLowerCase();
        if (os.contains("windows 8")) {
            return OperatingSystem.WINDOWS_8;
        } else if (os.contains("windows 7")) {
            return OperatingSystem.WINDOWS_7;
        } else if (os.contains("windows xp")) {
            return OperatingSystem.WINDOWS_XP;
        } else if (os.contains("windows vista")) {
            return OperatingSystem.WINDOWS_VISTA;
        } else if (os.contains("windows 2000")) {
            return OperatingSystem.WINDOWS_2000;
        } else if (os.contains("windows 2003")) {
            return OperatingSystem.WINDOWS_2003;
        } else if (os.contains("windows server 2008")) {
            return OperatingSystem.WINDOWS_SERVER_2008;
        } else if (os.contains("nt")) {
            return OperatingSystem.WINDOWS_NT;
        } else if (os.contains("windows")) {
            return OperatingSystem.WINDOWS_OTHERS;
        } else if (os.contains("mac")) {
            return OperatingSystem.MAC;
        } else if (os.contains("OS/2")) {
            return OperatingSystem.OS2;
        } else {
            return OperatingSystem.LINUX;
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
            CrossSystem.DESKTOP_SUPPORT.browseURL(new URL(_url));
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
        if (CrossSystem.isWindows() || CrossSystem.isOS2()) {
            if (new Regex(pathPart, "^(CON|PRN|AUX|NUL|COM\\d+|LPT\\d+|CLOCK\\$)$").matches()) {
                pathPart = "_" + pathPart;
            }
        }
        return pathPart.trim();
    }

    public static String fixPathSeperators(String path) {
        if (StringUtils.isEmpty(path)) {
            if (path != null) { return path; }
            return null;
        }
        if (CrossSystem.isWindows()) {
            /* windows uses \ as path seperator */
            path = path.replaceAll("[/]+", "\\\\");
            path = path.replaceAll("[\\\\]+", "\\\\");
        } else {
            /* mac/linux uses / as path seperator */
            path = path.replaceAll("[\\\\]+", "/");
            path = path.replaceAll("[/]+", "/");
        }
        return path;
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

    public static String getJavaBinary() {
        if (CrossSystem.JAVAINT != null) { return CrossSystem.JAVAINT; }
        String javaBinary = "java";
        if (CrossSystem.isWindows() || CrossSystem.isOS2()) {
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

    public static String getOSString() {
        return CrossSystem.OS_STRING;
    }

    public static double getSystemCPUUsage() {

        try {
            final java.lang.management.OperatingSystemMXBean operatingSystemMXBean = java.lang.management.ManagementFactory.getOperatingSystemMXBean();
            double sysload = operatingSystemMXBean.getSystemLoadAverage();
            if (sysload < 0) {
                final java.lang.reflect.Method method = operatingSystemMXBean.getClass().getDeclaredMethod("getSystemCpuLoad", new Class[] {});
                method.setAccessible(true);
                sysload = (Double) method.invoke(operatingSystemMXBean, new Object[] {});

            }
            return sysload;
        } catch (final Throwable e) {
            return -1;
        }
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
        if ((CrossSystem.isWindows() || CrossSystem.isOS2()) && path.matches("\\\\\\\\.+\\\\.+")) { return true; }
        if ((CrossSystem.isWindows() || CrossSystem.isOS2()) && path.matches(".:/.*")) { return true; }
        if ((CrossSystem.isWindows() || CrossSystem.isOS2()) && path.matches(".:\\\\.*")) { return true; }
        if (!CrossSystem.isWindows() && !CrossSystem.isOS2() && path.startsWith("/")) { return true; }

        return false;
    }

    /**
     * Returns true if the OS is a linux system
     * 
     * @return
     */
    public static OSFamily getOSFamily() {
        return OS.getFamily();
    }

    public static boolean isLinux() {
        return OS.getFamily() == OSFamily.LINUX;
    }

    /**
     * Returns true if the OS is a MAC System
     * 
     * @return
     */

    public static boolean isMac() {
        return OS.getFamily() == OSFamily.LINUX;
    }

    /**
     * returns true in case of "open an URL in a browser" is supported
     * 
     * @return
     */
    public static boolean isOpenBrowserSupported() {
        return CrossSystem.DESKTOP_SUPPORT.isBrowseURLSupported();
    }

    /**
     * returns true in case of "open a File" is supported
     * 
     * @return
     */
    public static boolean isOpenFileSupported() {
        return CrossSystem.DESKTOP_SUPPORT.isOpenFileSupported();
    }

    public static boolean isOS2() {
        return OS.getFamily() == OSFamily.OS2;
    }

    /**
     * Returns true if the OS is a Windows System
     * 
     * @return
     */
    public static boolean isWindows() {
        return OS.getFamily() == OSFamily.WINDOWS;
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
        // I noticed a bug: desktop.open freezes under win7 java 1.7u25 in some
        // cases... we should at least avoid a gui freeze in such cases..
        final Runnable runnable = new Runnable() {

            @Override
            public void run() {
                try {
                    CrossSystem._openFILE(file);
                } catch (final IOException e) {
                    Log.exception(e);
                }
            }
        };
        if (CrossSystem.isWindows()) {

            new Thread(runnable, "Open Folder").start();

        } else {
            runnable.run();
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
                Dialog.getInstance().showInputDialog(UIOManager.BUTTONS_HIDE_CANCEL, _AWU.T.crossSystem_open_url_failed_msg(), urlString);
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
                if (CrossSystem.isWindows() || CrossSystem.isOS2()) {
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
                    setHookPriority(Integer.MIN_VALUE);
                }

                @Override
                public void onShutdown(final ShutdownRequest shutdownRequest) {
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
     * @param saveTo
     */
    public static void showInExplorer(final File saveTo) {
        if (CrossSystem.isWindows() && saveTo.exists()) {
            try {
                // we need to go this cmd /c way, because explorer.exe seems to
                // do some strange parameter parsing.
                new ProcessBuilder("cmd", "/c", "explorer /select,\"" + saveTo.getAbsolutePath() + "\"").start();

                return;
            } catch (final IOException e) {
                e.printStackTrace();
            }
        }
        if (saveTo.isDirectory()) {
            CrossSystem.openFile(saveTo);
        } else {
            CrossSystem.openFile(saveTo.getParentFile());
        }

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

    public static enum OSFamily {
        WINDOWS,
        LINUX,
        MAC,
        OTHERS,
        OS2
    }

    /**
     * @return
     */
    public static OperatingSystem getOS() {
        return OS;
    }

}