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

/**
 * @author Thomas
 * 
 */
public class HomeFolder extends File {

    private String iconKey;
    /**
     * 
     */
    public static final String PICTURES = "Pictures";
    /**
     * 
     */
    public static final String VIDEOS = "Videos";
    /**
     * 
     */
    public static final String DOWNLOADS = "Downloads";
    /**
     * 
     */
    public static final String MUSIC = "Music";
    
    public static final String DOCUMENTS = "Documents";
    public static final String HOME_ROOT = "";
    public static final String DROPBOX = "Dropbox";

    /**
     * @param string
     * @param string2
     */
    public HomeFolder(final String relPath, final String iconKey) {
        super(System.getProperty("user.home") + System.getProperty("file.separator") + relPath);
        this.iconKey = iconKey;
    }

    public String getIconKey() {
        return iconKey;
    }

    public void setIconKey(final String iconKey) {
        this.iconKey = iconKey;
    }

}
