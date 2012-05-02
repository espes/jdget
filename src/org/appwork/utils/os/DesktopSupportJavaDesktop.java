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

import java.awt.Desktop;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;

/**
 * @author daniel
 * 
 */
public class DesktopSupportJavaDesktop implements DesktopSupport {

    private Boolean openFileSupported  = null;
    private Boolean browseURLSupported = null;

    @Override
    public void browseURL(final URL url) throws IOException, URISyntaxException {
        final Desktop desktop = Desktop.getDesktop();
        desktop.browse(url.toURI());
    }

    @Override
    public boolean isBrowseURLSupported() {
        if (this.browseURLSupported != null) { return this.browseURLSupported; }
        if (!Desktop.isDesktopSupported()) {
            this.browseURLSupported = false;
        }
        final Desktop desktop = Desktop.getDesktop();
        if (!desktop.isSupported(Desktop.Action.BROWSE)) {
            this.browseURLSupported = false;
        }
        this.browseURLSupported = true;
        return this.browseURLSupported;
    }

    @Override
    public boolean isOpenFileSupported() {
        if (this.openFileSupported != null) { return this.openFileSupported; }
        if (!Desktop.isDesktopSupported()) {
            this.openFileSupported = false;
        }
        final Desktop desktop = Desktop.getDesktop();
        if (!desktop.isSupported(Desktop.Action.OPEN)) {
            this.openFileSupported = false;
        }
        this.openFileSupported = true;
        return this.openFileSupported;
    }

    @Override
    public void openFile(final File file) throws IOException {
        final Desktop desktop = Desktop.getDesktop();
        final URI uri = file.getCanonicalFile().toURI();
        desktop.open(new File(uri));
    }

}
