/**
 * Copyright (c) 2009 - 2010 AppWork UG(haftungsbeschränkt) <e-mail@appwork.org>
 * 
 * This file is part of org.appwork.utils.os.mime
 * 
 * This software is licensed under the Artistic License 2.0,
 * see the LICENSE file or http://www.opensource.org/licenses/artistic-license-2.0.php
 * for details
 */
package org.appwork.utils.os.mime;

import java.io.IOException;
import java.util.HashMap;

import javax.swing.ImageIcon;

import org.appwork.utils.ImageProvider.ImageProvider;

public class MimeDefault implements Mime {
    /**
     * Cachemap for fileicons
     */
    private static HashMap<String, ImageIcon> IMAGE_ICON_CACHE = new HashMap<String, ImageIcon>();

    /**
     * Cache for the MIME descriptions
     */
    private static HashMap<String, String> DESCRIPTION_CACHE = new HashMap<String, String>();

    /**
     * Builds the icon key.
     * 
     * @param extension
     * @param width
     * @param height
     * @return
     */
    protected String getIconKey(String extension, int width, int height) {
        final StringBuilder sb = new StringBuilder();
        sb.append(extension);
        sb.append("_");
        sb.append(width);
        sb.append("x");
        sb.append(height);

        return sb.toString();
    }

    /**
     * Returns a icon from the cache.
     * 
     * @param iconKey
     * @return
     */
    protected ImageIcon getCacheIcon(String iconKey) {
        return IMAGE_ICON_CACHE.get(iconKey);
    }

    /**
     * Saves a icon in the cache.
     * 
     * @param iconKey
     * @param icon
     */
    protected void saveIconCache(String iconKey, ImageIcon icon) {
        IMAGE_ICON_CACHE.put(iconKey, icon);
    }

    /**
     * Returns the Mime dexcription from the cache
     * 
     * @param mimetype
     * @return
     */
    protected String getMimeDescriptionCache(String mimetype) {
        return DESCRIPTION_CACHE.get(mimetype);
    }

    /**
     * Saves a mime description in the cache
     * 
     * @param mimetype
     * @param description
     */
    protected void saveMimeDescriptionCache(String mimetype, String description) {
        DESCRIPTION_CACHE.put(mimetype, description);
    }

    public ImageIcon getFileIcon(String extension, int width, int height) throws IOException {
        return ImageProvider.getImageIcon("fileIcon", width, height, true);
    }

    public String getMimeDescription(String mimetype) {
        return mimetype;
    }
}