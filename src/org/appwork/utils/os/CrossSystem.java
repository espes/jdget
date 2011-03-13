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

import java.awt.Desktop;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URL;
import java.util.logging.Level;

import org.appwork.utils.locale.APPWORKUTILS;
import org.appwork.utils.logging.Log;
import org.appwork.utils.os.mime.Mime;
import org.appwork.utils.os.mime.MimeDefault;
import org.appwork.utils.os.mime.MimeLinux;
import org.appwork.utils.os.mime.MimeWindows;
import org.appwork.utils.swing.dialog.Dialog;
import org.appwork.utils.swing.dialog.DialogCanceledException;
import org.appwork.utils.swing.dialog.DialogClosedException;

/**
 * This class provides a few native features.
 * 
 * @author $Author: unknown$
 */
public class CrossSystem {
    public static final byte    OS_LINUX_OTHER   = 6;
    public static final byte    OS_MAC_OTHER     = 5;
    public static final byte    OS_WINDOWS_OTHER = 4;
    public static final byte    OS_WINDOWS_NT    = 3;
    public static final byte    OS_WINDOWS_2000  = 2;
    public static final byte    OS_WINDOWS_XP    = 0;
    public static final byte    OS_WINDOWS_2003  = 7;
    public static final byte    OS_WINDOWS_VISTA = 1;
    public static final byte    OS_WINDOWS_7     = 8;

    /**
     * Cache to store the OS string in
     */
    private final static String OS_STRING;

    /**
     * Cache to store the OS ID in
     */
    private final static byte   OS_ID;

    /**
     * Cache to store the Mime Class in
     */
    private static final Mime   MIME;
    static {
        OS_STRING = System.getProperty("os.name");
        final String OS = CrossSystem.OS_STRING.toLowerCase();
        if (OS.contains("windows 7")) {
            OS_ID = CrossSystem.OS_WINDOWS_7;
        } else if (OS.contains("windows xp")) {
            OS_ID = CrossSystem.OS_WINDOWS_XP;
        } else if (OS.contains("windows vista")) {
            OS_ID = CrossSystem.OS_WINDOWS_VISTA;
        } else if (OS.contains("windows 2000")) {
            OS_ID = CrossSystem.OS_WINDOWS_2000;
        } else if (OS.contains("windows 2003")) {
            OS_ID = CrossSystem.OS_WINDOWS_2003;
        } else if (OS.contains("nt")) {
            OS_ID = CrossSystem.OS_WINDOWS_NT;
        } else if (OS.contains("windows")) {
            OS_ID = CrossSystem.OS_WINDOWS_OTHER;
        } else if (OS.contains("mac")) {
            OS_ID = CrossSystem.OS_MAC_OTHER;
        } else {
            OS_ID = CrossSystem.OS_LINUX_OTHER;
        }
        if (CrossSystem.isWindows()) {
            MIME = new MimeWindows();
        } else if (CrossSystem.isLinux()) {
            MIME = new MimeLinux();
        } else {
            MIME = new MimeDefault();
        }
    }

    public static byte getID() {
        return CrossSystem.OS_ID;
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

    /**
     * Returns true if the OS is a linux system
     * 
     * @return
     */
    public static boolean isLinux() {
        return CrossSystem.OS_ID == CrossSystem.OS_LINUX_OTHER;
    }

    /**
     * Returns true if the OS is a MAC System
     * 
     * @return
     */
    public static boolean isMac() {
        return CrossSystem.OS_ID == CrossSystem.OS_MAC_OTHER;
    }

    /**
     * @return
     */
    public static boolean isOpenBrowserSupported() {
        if (CrossSystem.isWindows()) { return true; }
        try {
            final Desktop desktop = Desktop.getDesktop();
            if (!desktop.isSupported(Desktop.Action.BROWSE)) { return false; }
            return true;
        } catch (final Throwable e) {
            Log.exception(Level.WARNING, e);
        }
        return false;
    }

    public static boolean isOpenFileSupported() {
        if (CrossSystem.isWindows()) { return true; }
        try {
            final Desktop desktop = Desktop.getDesktop();
            if (!desktop.isSupported(Desktop.Action.OPEN)) { return false; }
            return true;
        } catch (final Throwable e) {
            Log.exception(Level.WARNING, e);
        }
        return false;
    }

    /**
     * Returns true if the OS is a Windows System
     * 
     * @return
     */
    public static boolean isWindows() {
        switch (CrossSystem.OS_ID) {
        case OS_WINDOWS_XP:
        case OS_WINDOWS_VISTA:
        case OS_WINDOWS_2000:
        case OS_WINDOWS_2003:
        case OS_WINDOWS_NT:
        case OS_WINDOWS_OTHER:
        case OS_WINDOWS_7:
            return true;
        }
        return false;
    }

    /**
     * Opens a file or directory
     * 
     * @see java.awt.Desktop#open(File)
     * @param file
     */
    public static void openFile(final File file) {
        if (file == null || !file.exists()) { return; }
        if (CrossSystem.isWindows()) {
            // workaround for windows
            // see http://bugs.sun.com/view_bug.do?bug_id=6599987
            try {
                Runtime.getRuntime().exec(new String[] { "rundll32.exe", "url.dll,FileProtocolHandler", file.getAbsolutePath() });
                return;
            } catch (final IOException e) {
                Log.exception(Level.WARNING, e);
            }
        }
        if (!Desktop.isDesktopSupported()) {
            Log.L.warning("Desktop is not supported (fatal)");
            return;
        }
        final Desktop desktop = Desktop.getDesktop();
        if (!desktop.isSupported(Desktop.Action.OPEN)) {
            Log.L.severe("Desktop doesn't support the OPEN action (fatal)");
            return;
        }
        try {
            final URI uri = file.getCanonicalFile().toURI();
            desktop.open(new File(uri));
        } catch (final Exception e) {
            try {
                Log.L.warning(file.getCanonicalFile().toURI().toString());
            } catch (final Exception e1) {
            }
            Log.exception(Level.WARNING, e);
        }
    }

    /**
     * Open an url in the systems default browser
     * 
     * @param url
     */
    public static void openURL(final URL url) {
        if (url == null) { return; }
        if (CrossSystem.isWindows()) {
            try {
                Runtime.getRuntime().exec(new String[] { "rundll32.exe", "url.dll,FileProtocolHandler", url.toString() });
                return;
            } catch (final IOException e) {
                Log.exception(Level.WARNING, e);
            }
        }
        if (!Desktop.isDesktopSupported()) {
            Log.L.severe("Desktop is not supported (fatal)");
            return;
        }
        final Desktop desktop = Desktop.getDesktop();
        if (!desktop.isSupported(Desktop.Action.BROWSE)) {
            Log.L.warning("Desktop doesn't support the browse action (fatal)");
            return;
        }
        try {
            desktop.browse(url.toURI());
        } catch (final Exception e) {
            try {
                Log.L.warning(url.toURI().toString());
            } catch (final Exception e1) {
            }
            Log.exception(Level.WARNING, e);
        }
    }

    /**
     * @param update_dialog_news_button_url
     */
    public static void openURLOrShowMessage(final String urlString) {
        URL url;
        try {
            url = new URL(urlString);

            if (CrossSystem.isWindows()) {

                Runtime.getRuntime().exec(new String[] { "rundll32.exe", "url.dll,FileProtocolHandler", url.toString() });
                return;

            }
            if (!Desktop.isDesktopSupported()) { throw new Exception("Desktop not supported"); }
            final Desktop desktop = Desktop.getDesktop();
            if (!desktop.isSupported(Desktop.Action.BROWSE)) { throw new Exception("Desktop doesn't support the browse action (fatal)");

            }

            desktop.browse(url.toURI());

        } catch (final Exception e) {
            Log.exception(Level.WARNING, e);
            try {
                Dialog.getInstance().showInputDialog(Dialog.BUTTONS_HIDE_CANCEL, APPWORKUTILS.T.crossSystem_open_url_failed_msg(), urlString);
            } catch (final DialogClosedException e1) {
                // nothing
            } catch (final DialogCanceledException e1) {
                // nothing
            }
        }

    }
}