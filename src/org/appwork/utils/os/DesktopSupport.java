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
public interface DesktopSupport {

    public void browseURL(URL url) throws IOException, URISyntaxException;

    boolean isBrowseURLSupported();

    boolean isOpenFileSupported();

    public void openFile(File file) throws IOException;

}
