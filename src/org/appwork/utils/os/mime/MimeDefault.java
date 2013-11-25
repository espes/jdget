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

import javax.swing.Icon;

import org.appwork.resources.AWUTheme;

public class MimeDefault implements Mime {

    /**
     * Cache for the MIME descriptions
     */
    private static HashMap<String, String> DESCRIPTION_CACHE = new HashMap<String, String>();

    /**
     * Returns a icon from the cache.
     * 
     * @param iconKey
     * @return
     */
    protected Icon getCacheIcon(final String iconKey) {
        return AWUTheme.I().getCached(iconKey);
    }

    public Icon getFileIcon(final String extension, final int width, final int height) throws IOException {
        return AWUTheme.I().getIcon("fileIcon", width);
    }

    /**
     * Builds the icon key.
     * 
     * @param extension
     * @param width
     * @param height
     * @return
     */
    protected String getIconKey(final String extension, final int width, final int height) {
        final StringBuilder sb = new StringBuilder();
        sb.append(extension);
        sb.append("_");
        sb.append(width);
        sb.append("x");
        sb.append(height);

        return sb.toString();
    }

    public String getMimeDescription(final String mimetype) {
        return mimetype;
    }

    /**
     * Returns the Mime dexcription from the cache
     * 
     * @param mimetype
     * @return
     */
    protected String getMimeDescriptionCache(final String mimetype) {
        return MimeDefault.DESCRIPTION_CACHE.get(mimetype);
    }

    /**
     * Saves a icon in the cache.
     * 
     * @param iconKey
     * @param icon
     */
    protected void saveIconCache(final String iconKey, final Icon icon) {
        AWUTheme.I().cache(icon, iconKey);
    }

    /**
     * Saves a mime description in the cache
     * 
     * @param mimetype
     * @param description
     */
    protected void saveMimeDescriptionCache(final String mimetype, final String description) {
        MimeDefault.DESCRIPTION_CACHE.put(mimetype, description);
    }
}