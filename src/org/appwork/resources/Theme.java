/**
 * Copyright (c) 2009 - 2011 AppWork UG(haftungsbeschränkt) <e-mail@appwork.org>
 * 
 * This file is part of org.appwork.resources
 * 
 * This software is licensed under the Artistic License 2.0,
 * see the LICENSE file or http://www.opensource.org/licenses/artistic-license-2.0.php
 * for details
 */
package org.appwork.resources;

import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Transparency;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.UIManager;

import org.appwork.storage.config.MinTimeWeakReference;
import org.appwork.storage.config.MinTimeWeakReferenceCleanup;
import org.appwork.utils.Application;
import org.appwork.utils.IO;
import org.appwork.utils.images.IconIO;
import org.appwork.utils.images.Interpolation;
import org.appwork.utils.logging.Log;

/**
 * 
 * @author thomas
 * 
 */
public class Theme implements MinTimeWeakReferenceCleanup {
    private String                                                   path;

    // private final HashMap<String, MinTimeWeakReference<BufferedImage>>
    // imageCache = new HashMap<String, MinTimeWeakReference<BufferedImage>>();

    protected final HashMap<String, MinTimeWeakReference<Icon>> imageIconCache = new HashMap<String, MinTimeWeakReference<Icon>>();

    private long                                                     cacheLifetime  = 20000l;

    private String                                                   theme;

    private String                                                   nameSpace;

    public Theme(final String namespace) {
        setNameSpace(namespace);
        setTheme("standard");
    }

    public void cache(final Icon ret, final String key) {
        synchronized (imageIconCache) {
            imageIconCache.put(key, new MinTimeWeakReference<Icon>(ret, getCacheLifetime(), key, this));
        }
    }

    /**
     * 
     */
    public void clearCache() {
        synchronized (imageIconCache) {
            imageIconCache.clear();
        }
    }

    public Icon getCached(final String key) {
        synchronized (imageIconCache) {
            final MinTimeWeakReference<Icon> cache = imageIconCache.get(key);
            if (cache != null) { return cache.get(); }
            return null;
        }
    }

    /**
     * @param relativePath
     * @param size
     * @return
     */
    protected String getCacheKey(final Object... objects) {
        if (objects.length == 1) { return objects[0].toString(); }
        final StringBuilder sb = new StringBuilder();
        for (final Object o : objects) {
            if (sb.length() > 0) {
                sb.append("_");
            }
            sb.append(o.toString());
        }
        return sb.toString();
    }

    public long getCacheLifetime() {
        return cacheLifetime;
    }

    private String getDefaultPath(final String pre, final String path, final String ext) {
        final StringBuilder sb = new StringBuilder();
        sb.append(this.path);
        sb.append(pre);
        sb.append(path);
        sb.append(ext);
        return sb.toString();
    }

    public Icon getDisabledIcon(final Icon _getIcon) {
        final String key = getCacheKey(_getIcon, "disabled");
        Icon ret = getCached(key);
        if (ret == null) {
            final Icon ico = UIManager.getLookAndFeel().getDisabledIcon(null, _getIcon);
            final BufferedImage dest = new BufferedImage(_getIcon.getIconWidth(), _getIcon.getIconHeight(), Transparency.TRANSLUCENT);
            final Graphics2D g2 = dest.createGraphics();

            ico.paintIcon(null, g2, 0, 0);
            g2.dispose();
            ret = new ImageIcon(dest);
            cache(ret, key);
        }
        return ret;

    }

    public ImageIcon getIcon(final String relativePath, final int size) {
        return this.getIcon(relativePath, size, true);

    }

    /**
     * @param relativePath
     * @param size
     * @param b
     * @return
     */
    public ImageIcon getIcon(final String relativePath, final int size, final boolean useCache) {
        Icon ret = null;
        String key = null;
        if (useCache) {
            key = getCacheKey(relativePath, size);
            ret = getCached(key);
        }
        if (ret == null) {
            final URL url = getURL("images/", relativePath, ".png");
            ret = IconIO.getImageIcon(url, size);
            if (url == null) {

                Log.exception(new Exception("Icon missing: " + this.getPath("images/", relativePath, ".png")));
                // try {
                // // Dialog.getInstance().showConfirmDialog(0, "Icon Missing",
                // // "Please add the\r\n" + this.getPath("images/",
                // // relativePath, ".png") + " to the classpath", ret, null,
                // // null);
                // } catch (final DialogClosedException e) {
                // // TODO Auto-generated catch block
                // e.printStackTrace();
                // } catch (final DialogCanceledException e) {
                // // TODO Auto-generated catch block
                // e.printStackTrace();
                // }

            }
            if (useCache) {
                cache(ret, key);
            }
        }
        return (ImageIcon) ret;
    }
//
//    public ImageIcon getIcon(final URL ressourceURL) {
//        final String key = getCacheKey(ressourceURL);
//        ImageIcon ret = getCached(key);
//        if (ret == null) {
//            ret = IconIO.getImageIcon(ressourceURL);
//            cache(ret, key);
//        }
//        return ret;
//    }

    public Image getImage(final String relativePath, final int size) {
        return this.getImage(relativePath, size, false);
    }

    public Image getImage(final String key, final int size, final boolean useCache) {
        return this.getIcon(key, size, useCache).getImage();
    }

    public URL getImageUrl(final String relativePath) {
        return getURL("images/", relativePath, ".png");
    }

    public String getNameSpace() {
        return nameSpace;
    }

    /**
     * @return
     */
    public String getPath() {

        return path;
    }

    private String getPath(final String pre, final String path, final String ext) {
        final StringBuilder sb = new StringBuilder();
        sb.append(this.path);
        sb.append(pre);
        sb.append(path);
        sb.append(ext);
        return sb.toString();
    }

    public Icon getScaledInstance(final Icon imageIcon, final int size) {
        final String key = getCacheKey(imageIcon, size);
        Icon ret = getCached(key);
        if (ret == null) {
            ret = IconIO.getScaledInstance(imageIcon, size, size, Interpolation.BILINEAR);
            cache(ret, key);
        }
        return ret;
    }

    public String getText(final String string) {
        final URL url = getURL("", string, "");
        if (url == null) { return null; }
        try {
            return IO.readURLToString(url);
        } catch (final IOException e) {
            Log.exception(e);
        }
        return null;
    }

    public String getTheme() {
        return theme;
    }

    /**
     * returns a valid resourceurl or null if no resource is available.
     * 
     * @param pre
     *            subfolder. for exmaple "images/"
     * @param relativePath
     *            relative resourcepath
     * @param ext
     *            resource extension
     * @return
     */
    public URL getURL(final String pre, final String relativePath, final String ext) {
        final String path = this.getPath(pre, relativePath, ext);
        try {

            // first lookup in home dir. .jd_home or installdirectory
            final File file = Application.getResource(path);
            if (file.exists()) { return file.toURI().toURL(); }
        } catch (final MalformedURLException e) {
            e.printStackTrace();
        }
        // afterwards, we lookup in classpath. jar or bin folders
        URL url = Theme.class.getResource(path);
        if (url == null) {
            url = Theme.class.getResource(getDefaultPath(pre, relativePath, ext));
        }
        return url;
    }

    public boolean hasIcon(final String string) {
        return getURL("images/", string, ".png") != null;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.appwork.storage.config.MinTimeWeakReferenceCleanup#
     * onMinTimeWeakReferenceCleanup
     * (org.appwork.storage.config.MinTimeWeakReference)
     */
    @Override
    public void onMinTimeWeakReferenceCleanup(final MinTimeWeakReference<?> minTimeWeakReference) {
        synchronized (imageIconCache) {
            imageIconCache.remove(minTimeWeakReference.getID());
        }
    }

    public void setCacheLifetime(final long cacheLifetime) {
        this.cacheLifetime = cacheLifetime;
    }

    public void setNameSpace(final String nameSpace) {
        this.nameSpace = nameSpace;
        path = "/themes/" + getTheme() + "/" + getNameSpace();
        clearCache();
    }

    public void setPath(final String path) {
        this.path = path;
        nameSpace = null;
        theme = null;
        clearCache();
    }

    /**
     * @param theme
     */
    public void setTheme(final String theme) {
        this.theme = theme;
        path = "/themes/" + getTheme() + "/" + getNameSpace();

        clearCache();
    }

}
