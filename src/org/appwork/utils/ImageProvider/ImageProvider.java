/**
 * Copyright (c) 2009 - 2010 AppWork UG(haftungsbeschränkt) <e-mail@appwork.org>
 * 
 * This file is part of org.appwork.utils.ImageProvider
 * 
 * This software is licensed under the Artistic License 2.0,
 * see the LICENSE file or http://www.opensource.org/licenses/artistic-license-2.0.php
 * for details
 */
package org.appwork.utils.ImageProvider;

import java.awt.Color;
import java.awt.Composite;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GraphicsConfiguration;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.Image;
import java.awt.RenderingHints;
import java.awt.Transparency;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.HashMap;
import java.util.Locale;
import java.util.WeakHashMap;
import java.util.logging.Level;

import javax.imageio.IIOException;
import javax.imageio.ImageIO;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.UIManager;

import org.appwork.storage.config.MinTimeWeakReference;
import org.appwork.storage.config.MinTimeWeakReferenceCleanup;
import org.appwork.utils.Application;
import org.appwork.utils.logging.Log;

import sun.awt.image.ToolkitImage;

/**
 * This class grants easy access to images stored in APPROOT/images/**.png
 * 
 * @author $Author: unknown$
 * 
 */
public class ImageProvider {
    private static final long                                           MIN_LIFETIME            = 20000l;
    /**
     * Hashcashmap to cache images.
     */
    private static HashMap<String, MinTimeWeakReference<BufferedImage>> IMAGE_CACHE             = new HashMap<String, MinTimeWeakReference<BufferedImage>>();
    private static MinTimeWeakReferenceCleanup                          IMAGE_CACHE_CLEANUP     = new MinTimeWeakReferenceCleanup() {

                                                                                                    @Override
                                                                                                    public void onMinTimeWeakReferenceCleanup(final MinTimeWeakReference<?> minTimeWeakReference) {
                                                                                                        synchronized (ImageProvider.LOCK) {
                                                                                                            ImageProvider.IMAGE_CACHE.remove(minTimeWeakReference.getID());
                                                                                                        }
                                                                                                    }
                                                                                                };
    private static HashMap<String, MinTimeWeakReference<ImageIcon>>     IMAGEICON_CACHE         = new HashMap<String, MinTimeWeakReference<ImageIcon>>();
    private static MinTimeWeakReferenceCleanup                          IMAGEICON_CACHE_CLEANUP = new MinTimeWeakReferenceCleanup() {

                                                                                                    @Override
                                                                                                    public void onMinTimeWeakReferenceCleanup(final MinTimeWeakReference<?> minTimeWeakReference) {
                                                                                                        synchronized (ImageProvider.LOCK) {
                                                                                                            ImageProvider.IMAGEICON_CACHE.remove(minTimeWeakReference.getID());
                                                                                                        }
                                                                                                    }
                                                                                                };
    private static WeakHashMap<Icon, MinTimeWeakReference<Icon>>        DISABLED_ICON_CACHE     = new WeakHashMap<Icon, MinTimeWeakReference<Icon>>();

    private static Object                                               LOCK                    = new Object();
    // stringbuilder die concat strings fast

    static {
        /* we dont want images to get cached on disk */
        ImageIO.setUseCache(false);
    }

    /* Thx to flubshi */
    public static BufferedImage convertToGrayScale(final BufferedImage bufferedImage) {
        final BufferedImage dest = new BufferedImage(bufferedImage.getWidth(), bufferedImage.getHeight(), BufferedImage.TYPE_INT_ARGB);
        Color tmp;
        int val, alpha;
        for (int y = 0; y < dest.getHeight(); y++) {
            for (int x = 0; x < dest.getWidth(); x++) {
                alpha = bufferedImage.getRGB(x, y) & 0xFF000000;
                tmp = new Color(bufferedImage.getRGB(x, y));
                // val = (int) (tmp.getRed()+tmp.getGreen()+tmp.getBlue())/3;
                // val =
                // Math.max(tmp.getRed(),Math.max(tmp.getGreen(),tmp.getBlue()));
                val = (int) (tmp.getRed() * 0.3 + tmp.getGreen() * 0.59 + tmp.getBlue() * 0.11);
                dest.setRGB(x, y, alpha | val | val << 8 & 0x0000FF00 | val << 16 & 0x00FF0000);
            }
        }
        return dest;
    }

    /**
     * Creates a dummy Icon
     * 
     * @param string
     * @param i
     * @param j
     * @return
     */
    public static BufferedImage createIcon(final String string, final int width, final int height) {
        final int w = width;
        final int h = height;

        final GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
        final GraphicsDevice gd = ge.getDefaultScreenDevice();
        final GraphicsConfiguration gc = gd.getDefaultConfiguration();
        final BufferedImage image = gc.createCompatibleImage(w, h, Transparency.BITMASK);

        final Graphics2D g = image.createGraphics();
        int size = 1 + width / string.length();
        // find max font size
        int ww = 0;
        int hh = 0;
        while (size > 0) {
            size--;
            g.setFont(new Font("Arial", Font.BOLD, size));
            ww = g.getFontMetrics().stringWidth(string);
            hh = g.getFontMetrics().getAscent();
            if (ww < w - 4 && hh < h - 2) {
                break;
            }
        }
        g.setColor(Color.WHITE);
        g.fillRect(0, 0, w - 1, h - 1);
        g.draw3DRect(0, 0, w - 1, h - 1, true);
        g.setColor(Color.BLACK);
        g.drawString(string, (w - ww) / 2, hh + (h - hh) / 2);
        g.dispose();
        return image;

    }

    /**
     * this creates a new BufferedImage from an existing Image. Is used for
     * dereferencing the sourceImage in scaled Images created with
     * image.getScaledInstance, which always keeps a reference to its original
     * image
     * 
     * @param image
     * @return
     * @throws IOException
     */
    public static BufferedImage dereferenceImage(final Image image) throws IOException {
        final BufferedImage bu = new BufferedImage(image.getWidth(null), image.getHeight(null), BufferedImage.TYPE_INT_ARGB);
        final Graphics g = bu.getGraphics();
        g.drawImage(image, 0, 0, null);
        g.dispose();
        return bu;
    }

    /**
     * 
     * @param name
     *            to the png file
     * @param createDummy
     *            TODO
     * @return
     * @throws IOException
     */
    public static BufferedImage getBufferedImage(final String name, final boolean createDummy) throws IOException {
        return ImageProvider.getBufferedImage(name, createDummy, true);
    }

    public static BufferedImage getBufferedImage(final String name, final boolean createDummy, final boolean putIntoCache) throws IOException {
        synchronized (ImageProvider.LOCK) {
            if (ImageProvider.IMAGE_CACHE.containsKey(name)) {

                final MinTimeWeakReference<BufferedImage> cache = ImageProvider.IMAGE_CACHE.get(name);
                if (cache.get() != null) { return cache.get(); }
            }

            final URL absolutePath = Application.getRessourceURL("images/" + name + ".png");
            try {
                Log.L.info("Init Image: " + name + ": " + absolutePath);
                final BufferedImage image = ImageProvider.read(absolutePath);
                if (putIntoCache) {
                    if (image.getHeight() * image.getWidth() > 100 * 100) {
                        // Log.exception(new Throwable("BIG IMAGE IN CACHE: " +
                        // name));
                    }
                    ImageProvider.IMAGE_CACHE.put(name, new MinTimeWeakReference<BufferedImage>(image, ImageProvider.MIN_LIFETIME, name, ImageProvider.IMAGE_CACHE_CLEANUP));
                }
                return image;
            } catch (final IOException e) {
                Log.L.severe("Could not Init Image: " + absolutePath);
                if (createDummy) {
                    Log.exception(Level.WARNING, e);
                    return ImageProvider.createIcon(name.toUpperCase(Locale.ENGLISH), 48, 48);
                } else {
                    throw e;
                }
            } catch (final Throwable e) {
                Log.L.severe("Could not Init Image: " + absolutePath);
                Log.exception(Level.WARNING, e);
                return ImageProvider.createIcon(name.toUpperCase(Locale.ENGLISH), 48, 48);
            }
        }
    }

    /**
     * Uses the uimanager to get a grayscaled disabled Icon
     * 
     * @param icon
     * @return
     */
    public static Icon getDisabledIcon(Icon icon) {
        if (icon == null) { return null; }
        synchronized (ImageProvider.LOCK) {
            final MinTimeWeakReference<Icon> cache = ImageProvider.DISABLED_ICON_CACHE.get(icon);
            Icon ret = cache == null ? null : cache.get();
            if (ret != null) { return ret; }
            if (!(icon instanceof ImageIcon)) {
                // getDisabledIcon only works for imageicons
                icon = ImageProvider.toImageIcon(icon);
            }
            ret = UIManager.getLookAndFeel().getDisabledIcon(null, icon);
            ImageProvider.DISABLED_ICON_CACHE.put(icon, new MinTimeWeakReference<Icon>(ret, ImageProvider.MIN_LIFETIME, "disabled icon"));
            return ret;
        }
    }

    /**
     * @param string
     * @param i
     * @param j
     * @return
     */
    public static ImageIcon getImageIcon(final String name, final int x, final int y) {
        // TODO Auto-generated method stub
        try {
            return ImageProvider.getImageIcon(name, x, y, true);
        } catch (final IOException e) {
            // can not happen. true creates a dummyicon in case of io errors
            Log.exception(e);
            return null;
        }
    }

    /**
     * Loads the image, scales it to the desired size and returns it as an
     * imageicon
     * 
     * @param name
     * @param width
     * @param height
     * @param createDummy
     *            TODO
     * @return
     * @throws IOException
     */
    public static ImageIcon getImageIcon(final String name, final int width, final int height, final boolean createDummy) throws IOException {
        return ImageProvider.getImageIcon(name, width, height, createDummy, true);
    }

    public static ImageIcon getImageIcon(final String name, int width, int height, final boolean createDummy, final boolean putIntoCache) throws IOException {
        synchronized (ImageProvider.LOCK) {
            final StringBuilder SB = new StringBuilder();
            SB.append(name);
            SB.append('_');
            SB.append(width);
            SB.append('_');
            SB.append(height);
            String key;
            if (ImageProvider.IMAGEICON_CACHE.containsKey(key = SB.toString())) {
                final MinTimeWeakReference<ImageIcon> cache = ImageProvider.IMAGEICON_CACHE.get(key);
                if (cache.get() != null) { return cache.get(); }
            }
            final BufferedImage image = ImageProvider.getBufferedImage(name, createDummy, putIntoCache);
            final double faktor = Math.max((double) image.getWidth(null) / width, (double) image.getHeight(null) / height);
            width = (int) (image.getWidth(null) / faktor);
            height = (int) (image.getHeight(null) / faktor);
            /**
             * WARNING: getScaledInstance will return a scaled image, BUT keeps
             * a reference to original unscaled image
             */
            final Image scaledWithFuckingReference = image.getScaledInstance(width, height, Image.SCALE_SMOOTH);
            final BufferedImage referencelessVersion = ImageProvider.dereferenceImage(scaledWithFuckingReference);
            final ImageIcon imageicon = new ImageIcon(referencelessVersion);
            if (putIntoCache) {
                ImageProvider.IMAGEICON_CACHE.put(key, new MinTimeWeakReference<ImageIcon>(imageicon, ImageProvider.MIN_LIFETIME, key, ImageProvider.IMAGEICON_CACHE_CLEANUP));
            }
            return imageicon;
        }
    }

    public static ImageIcon getImageIconUnCached(final String name, final int x, final int y) {
        // TODO Auto-generated method stub
        try {
            return ImageProvider.getImageIcon(name, x, y, true, false);
        } catch (final IOException e) {
            // can not happen. true creates a dummyicon in case of io errors
            Log.exception(e);
            return null;
        }
    }

    /**
     * Taken from http://today.java.net/pub/a/today/2007/04/03/perils-of-image-
     * getscaledinstance.html License: unknown Convenience method that returns a
     * scaled instance of the provided {@code BufferedImage}.
     * 
     * @param img
     *            the original image to be scaled
     * @param targetWidth
     *            the desired width of the scaled instance, in pixels
     * @param targetHeight
     *            the desired height of the scaled instance, in pixels
     * @param hint
     *            one of the rendering hints that corresponds to
     *            {@code RenderingHints.KEY_INTERPOLATION} (e.g.
     *            {@code RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR},
     *            {@code RenderingHints.VALUE_INTERPOLATION_BILINEAR},
     *            {@code RenderingHints.VALUE_INTERPOLATION_BICUBIC})
     * @param higherQuality
     *            if true, this method will use a multi-step scaling technique
     *            that provides higher quality than the usual one-step technique
     *            (only useful in downscaling cases, where {@code targetWidth}
     *            or {@code targetHeight} is smaller than the original
     *            dimensions, and generally only when the {@code BILINEAR} hint
     *            is specified)
     * @return a scaled version of the original {@code BufferedImage}
     */
    public static BufferedImage getScaledInstance(final BufferedImage img, int width, int height, final Object hint, final boolean higherQuality) {
        final double faktor = Math.max((double) img.getWidth() / width, (double) img.getHeight() / height);
        width = (int) (img.getWidth() / faktor);
        height = (int) (img.getHeight() / faktor);
        if (faktor == 1.0) { return img; }

        BufferedImage ret = img;
        int w, h;
        if (higherQuality) {
            // Use multi-step technique: start with original size, then
            // scale down in multiple passes with drawImage()
            // until the target size is reached
            w = Math.max(width, img.getWidth());
            h = Math.max(height, img.getHeight());
        } else {
            // Use one-step technique: scale directly from original
            // size to target size with a single drawImage() call
            w = width;
            h = height;
        }

        do {
            if (higherQuality && w > width) {
                w /= 2;
                if (w < width) {
                    w = width;
                }
            }

            if (higherQuality && h > height) {
                h /= 2;
                if (h < height) {
                    h = height;
                }
            }
            // use 6 as default image type. java versions <16 u17 return type 0
            // for loaded pngs
            final BufferedImage tmp = new BufferedImage(w, h, ret.getType() == 0 ? 6 : ret.getType());
            final Graphics2D g2 = tmp.createGraphics();
            g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, hint);
            g2.drawImage(ret, 0, 0, w, h, null);
            g2.dispose();

            ret = tmp;
        } while (w != width || h != height);

        return ret;
    }

    /**
     * @param back
     * @param imageIcon
     * @param i
     * @param j
     */
    public static BufferedImage merge(final Image back, final Image front, final int xoffset, final int yoffset) {
        int xoffsetTop, yoffsetTop, xoffsetBottom, yoffsetBottom;

        if (xoffset >= 0) {
            xoffsetTop = 0;
            xoffsetBottom = xoffset;
        } else {
            xoffsetTop = -xoffset;
            xoffsetBottom = 0;
        }
        if (yoffset >= 0) {
            yoffsetTop = 0;
            yoffsetBottom = yoffset;
        } else {
            yoffsetTop = -yoffset;
            yoffsetBottom = 0;
        }
        return ImageProvider.merge(back, front, xoffsetTop, yoffsetTop, xoffsetBottom, yoffsetBottom);
    }

    public static BufferedImage merge(final Icon back, final Icon front, final int xoffsetBack, final int yoffsetBack, final int xoffsetFront, final int yoffsetFront, final Composite backComposite, final Composite frontComposite) {
        final int width = Math.max(xoffsetBack + back.getIconWidth(), xoffsetFront + front.getIconWidth());
        final int height = Math.max(yoffsetBack + back.getIconHeight(), yoffsetFront + front.getIconHeight());
        final BufferedImage dest = new BufferedImage(width, height, Transparency.TRANSLUCENT);
        final Graphics2D g2 = dest.createGraphics();

        if (backComposite != null) {
            final Composite old = g2.getComposite();
            g2.setComposite(backComposite);
            back.paintIcon(null, g2, xoffsetBack, yoffsetBack);
            g2.setComposite(old);
        } else {

            back.paintIcon(null, g2, xoffsetBack, yoffsetBack);
        }
        if (frontComposite != null) {
            final Composite old = g2.getComposite();
            g2.setComposite(frontComposite);
            front.paintIcon(null, g2, xoffsetFront, yoffsetFront);
            g2.setComposite(old);
        } else {

            front.paintIcon(null, g2, xoffsetFront, yoffsetFront);
        }

        g2.dispose();

        return dest;

    }

    public static BufferedImage merge(final Icon back, final Icon front, final int xoffsetBack, final int yoffsetBack, final int xoffsetFront, final int yoffsetFront) {

        return merge(back, front, xoffsetBack, yoffsetBack, xoffsetFront, yoffsetFront, null, null);
    }

    public static BufferedImage merge(final Image back, final Image front, final int xoffsetBack, final int yoffsetBack, final int xoffsetFront, final int yoffsetFront) {
        return merge(new ImageIcon(back), new ImageIcon(front), xoffsetBack, yoffsetBack, xoffsetFront, yoffsetFront);
    }

    /* copied from ImageIO, to close the inputStream */
    public static BufferedImage read(final File input) throws IOException {
        if (!input.canRead()) { throw new IIOException("Can't read input file!"); }
        FileInputStream is = null;
        try {
            is = new FileInputStream(input);
            return ImageIO.read(is);
        } finally {
            try {
                is.close();
            } catch (final Throwable e) {
            }
        }
    }

    /**
     * @param absolutePath
     * @return
     * @throws IOException
     */
    private static BufferedImage read(final URL absolutePath) throws IOException {
        if (absolutePath == null) { throw new IllegalArgumentException("input == null!"); }
        InputStream is = null;
        BufferedImage bi = null;
        try {
            is = absolutePath.openStream();
            if (is == null) { throw new IIOException("Can't create an ImageInputStream!"); }
            bi = ImageIO.read(is);
        } finally {
            try {
                is.close();
            } catch (final Throwable e) {
            }
        }
        return bi;
    }

    /**
     * @param scaleBufferedImage
     * @param width
     * @param height
     * @return
     */
    public static BufferedImage resizeWorkSpace(final Image scaleBufferedImage, final int width, final int height) {
        // final GraphicsEnvironment ge =
        // GraphicsEnvironment.getLocalGraphicsEnvironment();
        // final GraphicsDevice gd = ge.getDefaultScreenDevice();
        // final GraphicsConfiguration gc = gd.getDefaultConfiguration();
        // final BufferedImage image = gc.createCompatibleImage(width, height,
        // Transparency.BITMASK);
        final BufferedImage image = new BufferedImage(width, height, Transparency.TRANSLUCENT);
        final Graphics2D g = image.createGraphics();
        g.drawImage(scaleBufferedImage, (width - scaleBufferedImage.getWidth(null)) / 2, (height - scaleBufferedImage.getHeight(null)) / 2, null);

        g.dispose();
        return image;
    }

    /**
     * Scales a buffered Image to the given size. This method is NOT cached. so
     * take care to cache it externally if you use it frequently
     * 
     * @param img
     * @param width
     * @param height
     * @return
     */
    public static Image scaleBufferedImage(final BufferedImage img, int width, int height) {
        if (img == null) { return null; }
        final double faktor = Math.max((double) img.getWidth() / width, (double) img.getHeight() / height);
        width = (int) (img.getWidth() / faktor);
        height = (int) (img.getHeight() / faktor);
        if (faktor == 1.0) { return img; }
        final Image image = img.getScaledInstance(width, height, Image.SCALE_SMOOTH);
        try {
            return ImageProvider.dereferenceImage(image);
        } catch (final IOException e) {
            Log.exception(e);
            return null;
        }

    }

    /**
     * Scales an imageicon to w x h.<br>
     * like {@link #scaleBufferedImage(BufferedImage, int, int)}, this Function
     * is NOT cached. USe an external cache if you use it frequently
     * 
     * @param img
     * @param w
     * @param h
     * @return
     */
    public static ImageIcon scaleImageIcon(final ImageIcon img, final int w, final int h) {
        // already has the desired size?
        if (img.getIconHeight() == h && img.getIconWidth() == w) { return img; }

        BufferedImage dest;

        if (img.getImage() instanceof ToolkitImage) {
            dest = new BufferedImage(w, h, Transparency.TRANSLUCENT);
            final Graphics2D g2 = dest.createGraphics();
            g2.drawImage(img.getImage(), 0, 0, null);
            g2.dispose();
        } else {
            dest = (BufferedImage) img.getImage();
        }

        return new ImageIcon(ImageProvider.scaleBufferedImage(dest, w, h));
    }

    /**
     * Converts an Icon to an Imageicon.
     * 
     * @param icon
     * @return
     */
    public static ImageIcon toImageIcon(final Icon icon) {

        if (icon == null) { return null; }
        if (icon instanceof ImageIcon) {
            return (ImageIcon) icon;
        } else {
            final int w = icon.getIconWidth();
            final int h = icon.getIconHeight();
            final GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
            final GraphicsDevice gd = ge.getDefaultScreenDevice();
            final GraphicsConfiguration gc = gd.getDefaultConfiguration();
            final BufferedImage image = gc.createCompatibleImage(w, h, Transparency.TRANSLUCENT);
          
            final Graphics2D g = image.createGraphics();
            g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
            // g.setColor(Color.RED);
            // g.fillRect(0, 0, w, h);
            icon.paintIcon(null, g, 0, 0);
            g.dispose();
            return new ImageIcon(image);

        }

    }
}
