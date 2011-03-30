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
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.GraphicsConfiguration;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.Image;
import java.awt.RenderingHints;
import java.awt.Transparency;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.logging.Level;

import javax.imageio.IIOException;
import javax.imageio.ImageIO;
import javax.imageio.stream.ImageInputStream;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.UIManager;

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
    /**
     * Hashcashmap to cache images.
     */
    private static HashMap<String, BufferedImage> IMAGE_CACHE         = new HashMap<String, BufferedImage>();
    private static HashMap<String, ImageIcon>     IMAGEICON_CACHE     = new HashMap<String, ImageIcon>();
    private static HashMap<Icon, Icon>            DISABLED_ICON_CACHE = new HashMap<Icon, Icon>();

    private static Object                         LOCK                = new Object();
    // stringbuilder die concat strings fast
    private static StringBuilder                  SB                  = new StringBuilder();
    static {
        /* we dont want images to get cached on disk */
        ImageIO.setUseCache(false);
    }

    /**
     * @param bufferedImage
     * @return
     */
    public static BufferedImage convertToGrayScale(final BufferedImage bufferedImage) {
        final BufferedImage dest = new BufferedImage(bufferedImage.getWidth(), bufferedImage.getHeight(), BufferedImage.TYPE_BYTE_GRAY);
        final Graphics2D g2 = dest.createGraphics();
        g2.drawImage(bufferedImage, 0, 0, null);
        g2.dispose();
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
    public static Image createIcon(final String string, final int width, final int height) {
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
     * 
     * @param name
     *            to the png file
     * @param createDummy
     *            TODO
     * @return
     * @throws IOException
     */
    public static Image getBufferedImage(final String name, final boolean createDummy) throws IOException {
        synchronized (ImageProvider.LOCK) {
            if (ImageProvider.IMAGE_CACHE.containsKey(name)) { return ImageProvider.IMAGE_CACHE.get(name); }

            final URL absolutePath = Application.getRessourceURL("images/" + name + ".png");
            try {
                Log.L.info("Init Image: " + name + ": " + absolutePath);
                final BufferedImage image = ImageProvider.read(absolutePath);
                ImageProvider.IMAGE_CACHE.put(name, image);
                return image;
            } catch (final IOException e) {
                Log.L.severe("Could not Init Image: " + absolutePath);
                if (createDummy) {
                    Log.exception(Level.WARNING, e);
                    return ImageProvider.createIcon(name.toUpperCase(), 48, 48);
                } else {
                    throw e;
                }

            } catch (final IllegalArgumentException e) {
                Log.L.severe("Could not Init Image: " + absolutePath);
                if (createDummy) {
                    Log.exception(Level.WARNING, e);
                    return ImageProvider.createIcon(name.toUpperCase(), 48, 48);
                } else {
                    throw e;
                }

            }
        }
    }

    /**
     * Uses the uimanager to get a grayscaled disabled Icon
     * 
     * @param icon
     * @return
     */
    public static Icon getDisabledIcon(final Icon icon) {
        if (icon == null) { return null; }
        Icon ret = ImageProvider.DISABLED_ICON_CACHE.get(icon);
        if (ret != null) { return ret; }
        ret = UIManager.getLookAndFeel().getDisabledIcon(null, icon);
        ImageProvider.DISABLED_ICON_CACHE.put(icon, ret);
        return ret;
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
    public static ImageIcon getImageIcon(final String name, int width, int height, final boolean createDummy) throws IOException {
        synchronized (ImageProvider.LOCK) {
            ImageProvider.SB.delete(0, ImageProvider.SB.capacity());
            ImageProvider.SB.append(name);
            ImageProvider.SB.append('_');
            ImageProvider.SB.append(width);
            ImageProvider.SB.append('_');
            ImageProvider.SB.append(height);
            String key;
            if (ImageProvider.IMAGEICON_CACHE.containsKey(key = ImageProvider.SB.toString())) { return ImageProvider.IMAGEICON_CACHE.get(key); }
            final Image image = ImageProvider.getBufferedImage(name, createDummy);
            final double faktor = Math.max((double) image.getWidth(null) / width, (double) image.getHeight(null) / height);
            width = (int) (image.getWidth(null) / faktor);
            height = (int) (image.getHeight(null) / faktor);
            final ImageIcon imageicon = new ImageIcon(image.getScaledInstance(width, height, Image.SCALE_SMOOTH));
            ImageProvider.IMAGEICON_CACHE.put(key, imageicon);
            return imageicon;
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

            final BufferedImage tmp = new BufferedImage(w, h, ret.getType());
            final Graphics2D g2 = tmp.createGraphics();
            g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, hint);
            g2.drawImage(ret, 0, 0, w, h, null);
            g2.dispose();

            ret = tmp;
        } while (w != width || h != height);

        return ret;
    }

    /**
     * @param image
     * @param imageIcon
     * @param i
     * @param j
     */
    public static BufferedImage merge(final Image image, final Image b, final int xoffset, final int yoffset) {
        final int width = Math.max(image.getWidth(null), xoffset + b.getWidth(null));
        final int height = Math.max(image.getHeight(null), yoffset + b.getHeight(null));
        final BufferedImage dest = new BufferedImage(width, height, Transparency.TRANSLUCENT);
        final Graphics2D g2 = dest.createGraphics();
        g2.drawImage(image, 0, 0, null);
        g2.drawImage(b, xoffset, yoffset, null);
        g2.dispose();
        return dest;
    }

    /* copied from ImageIO, to close the inputStream */
    public static BufferedImage read(final File input) throws IOException {
        if (!input.canRead()) { throw new IIOException("Can't read input file!"); }
        return ImageProvider.read(input.toURI().toURL());
    }

    /**
     * @param absolutePath
     * @return
     * @throws IOException
     */
    private static BufferedImage read(final URL absolutePath) throws IOException {
        if (absolutePath == null) { throw new IllegalArgumentException("input == null!"); }
        final ImageInputStream stream = ImageIO.createImageInputStream(absolutePath.openStream());
        BufferedImage bi = null;
        try {
            if (stream == null) { throw new IIOException("Can't create an ImageInputStream!"); }
            bi = ImageIO.read(stream);
        } finally {
            try {
                stream.close();
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
        return img.getScaledInstance(width, height, Image.SCALE_SMOOTH);

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
            final BufferedImage image = gc.createCompatibleImage(w, h, Transparency.BITMASK);
            final Graphics2D g = image.createGraphics();
            icon.paintIcon(null, g, 0, 0);
            g.dispose();
            return new ImageIcon(image);

        }

    }
}
