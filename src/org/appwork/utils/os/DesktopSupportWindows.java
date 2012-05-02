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

/**
 * @author daniel
 * 
 */
public class DesktopSupportWindows implements DesktopSupport {

    @Override
    public void browseURL(final URL url) throws IOException, URISyntaxException {
        Runtime.getRuntime().exec(new String[] { "rundll32.exe", "url.dll,FileProtocolHandler", url.toExternalForm() });
    }

    @Override
    public boolean isBrowseURLSupported() {
        return true;
    }

    @Override
    public boolean isOpenFileSupported() {
        return true;
    }

    @Override
    public void openFile(final File file) throws IOException {
        // workaround for windows
        // see http://bugs.sun.com/view_bug.do?bug_id=6599987
        if (!file.exists()) { throw new IOException("File does not exist " + file.getAbsolutePath()); }
        Runtime.getRuntime().exec(new String[] { "rundll32.exe", "url.dll,FileProtocolHandler", file.getAbsolutePath() });
    }

}
