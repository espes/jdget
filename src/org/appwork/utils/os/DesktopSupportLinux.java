/**
 * Copyright (c) 2009 - 2012 AppWork UG(haftungsbeschränkt) <e-mail@appwork.org>
 * 
 * This file is part of org.appwork.utils.os
 * 
 * This software is licensed under the Artistic License 2.0,
 * see the LICENSE file or http://www.opensource.org/licenses/artistic-license-2.0.php
 * for details
 */
package org.appwork.utils.os;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;

/**
 * @author daniel
 * 
 */
public class DesktopSupportLinux implements DesktopSupport {

    private final DesktopSupportJavaDesktop fallBack = new DesktopSupportJavaDesktop();
    private final String[]                  customFile;
    private final String[]                  customBrowse;

    public DesktopSupportLinux() {
        /* see http://standards.freedesktop.org/menu-spec/latest/apb.html */
        final String desktopManager = System.getenv("XDG_CURRENT_DESKTOP");
        /* returns true in case we have running KDE */
        final String kdeFullSession = System.getenv("KDE_FULL_SESSION");
        final String desktopSession = System.getenv("DESKTOP_SESSION");
        if ("Unity".equals(desktopManager) || "GNOME".equals(desktopManager) || "ubuntu-2d".equals(desktopManager) || "gnome".equals(desktopManager)) {
            System.out.println("Gnome or Unity Desktop detected");
            this.customFile = new String[] { "gnome-open", "%s" };
            this.customBrowse = new String[] { "gnome-open", "%s" };
        } else if ("true".equals(kdeFullSession) || "kde-plasma".equals(desktopSession)) {
            System.out.println("KDE detected");
            this.customFile = new String[] { "kde-open", "%s" };
            this.customBrowse = new String[] { "kde-open", "%s" };
        } else {
            this.customFile = null;
            this.customBrowse = null;
        }
    }

    @Override
    public void browseURL(final URL url) throws IOException, URISyntaxException {
        if (this.openCustom(this.customBrowse, url.toExternalForm())) { return; }
        this.fallBack.browseURL(url);
    }

    @Override
    public boolean isBrowseURLSupported() {
        if (this.customBrowse != null && this.customFile.length >= 2 || this.fallBack.isBrowseURLSupported()) { return true; }
        return false;
    }

    @Override
    public boolean isOpenFileSupported() {
        if (this.customFile != null && this.customFile.length >= 2 || this.fallBack.isOpenFileSupported()) { return true; }
        return false;
    }

    private boolean openCustom(final String[] custom, final String what) throws IOException {
        if (custom == null || custom.length < 1) { return false; }
        boolean added = false;
        final ArrayList<String> commands = new ArrayList<String>();
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

    @Override
    public void openFile(final File file) throws IOException {
        if (this.openCustom(this.customFile, file.getAbsolutePath())) { return; }
        this.fallBack.openFile(file);
    }

}
