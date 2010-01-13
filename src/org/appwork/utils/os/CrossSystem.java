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
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;

import org.appwork.utils.logging.Log;
import org.appwork.utils.os.mime.Mime;
import org.appwork.utils.os.mime.MimeDefault;
import org.appwork.utils.os.mime.MimeLinux;
import org.appwork.utils.os.mime.MimeWindows;

/**
 * This class provides a few native features.
 * 
 * @author $Author: unknown$
 */
public class CrossSystem {
    public static final byte OS_LINUX_OTHER = 6;
    public static final byte OS_MAC_OTHER = 5;
    public static final byte OS_WINDOWS_OTHER = 4;
    public static final byte OS_WINDOWS_NT = 3;
    public static final byte OS_WINDOWS_2000 = 2;
    public static final byte OS_WINDOWS_XP = 0;
    public static final byte OS_WINDOWS_2003 = 7;
    public static final byte OS_WINDOWS_VISTA = 1;
    public static final byte OS_WINDOWS_7 = 8;

    /**
     * Cache to store the OSID in
     */
    private final static byte OS_ID;

    /**
     * Cache to store the Mime Class in
     */
    private static final Mime MIME;
    static {
        final String OS = System.getProperty("os.name").toLowerCase();
        if (OS.indexOf("windows 7") > -1) {
            OS_ID = OS_WINDOWS_7;
        } else if (OS.indexOf("windows xp") > -1) {
            OS_ID = OS_WINDOWS_XP;
        } else if (OS.indexOf("windows vista") > -1) {
            OS_ID = OS_WINDOWS_VISTA;
        } else if (OS.indexOf("windows 2000") > -1) {
            OS_ID = OS_WINDOWS_2000;
        } else if (OS.indexOf("windows 2003") > -1) {
            OS_ID = OS_WINDOWS_2003;
        } else if (OS.indexOf("nt") > -1) {
            OS_ID = OS_WINDOWS_NT;
        } else if (OS.indexOf("windows") > -1) {
            OS_ID = OS_WINDOWS_OTHER;
        } else if (OS.indexOf("mac") > -1) {
            OS_ID = OS_MAC_OTHER;
        } else {
            OS_ID = OS_LINUX_OTHER;
        }
        if (isWindows()) {
            MIME = new MimeWindows();
        } else if (isLinux()) {
            MIME = new MimeLinux();
        } else {
            MIME = new MimeDefault();
        }
    }

    /**
     * Open an url in the systems default browser
     * 
     * @param url
     */
    public static void openURL(URL url) {
        if (!Desktop.isDesktopSupported()) {
            Log.L.severe("Desktop is not supported (fatal)");
        }

        Desktop desktop = java.awt.Desktop.getDesktop();

        if (!desktop.isSupported(java.awt.Desktop.Action.BROWSE)) {
            Log.L.severe("Desktop doesn't support the browse action (fatal)");
        }

        try {
            desktop.browse(url.toURI());
        } catch (IOException e) {
            Log.exception(e);
        } catch (URISyntaxException e) {
            Log.exception(e);
        }
    }

    /**
     * Returns true if the OS is a linux system
     * 
     * @return
     */
    public static boolean isLinux() {
        return OS_ID == OS_LINUX_OTHER;
    }

    /**
     * Returns true if the OS is a MAC System
     * 
     * @return
     */
    public static boolean isMac() {
        return OS_ID == OS_MAC_OTHER;
    }

    /**
     * Returns true if the OS is a Windows System
     * 
     * @return
     */
    public static boolean isWindows() {
        switch (OS_ID) {
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
     * Returns the Mime Class for the current OS
     * 
     * @return
     * @see Mime
     */
    public static Mime getMime() {
        return MIME;
    }
}