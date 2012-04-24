package org.appwork.utils.images;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.GraphicsConfiguration;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.Image;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.Toolkit;
import java.awt.Transparency;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImage;
import java.awt.image.BufferedImageOp;
import java.awt.image.ConvolveOp;
import java.awt.image.FilteredImageSource;
import java.awt.image.ImageFilter;
import java.awt.image.ImageProducer;
import java.awt.image.Kernel;
import java.awt.image.RGBImageFilter;
import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.logging.Level;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;

import org.appwork.utils.ImageProvider.ImageProvider;
import org.appwork.utils.logging.Log;

public class IconIO {

    static {
        ImageIO.setUseCache(false);
    }

    /**
     * @param resource
     * @return
     */
    public static BufferedImage getImage(final URL resource) {
        if (resource != null) {
            try {
                return (ImageIO.read(resource));
            } catch (final IOException e) {
                Log.exception(Level.WARNING, e);
            }
        }

        return ImageProvider.createIcon("DUMMY", 48, 48);
    }

    /**
     * @param resource
     * @return
     */
    public static ImageIcon getImageIcon(final URL resource) {

        return new ImageIcon(IconIO.getImage(resource));
    }

    /**
     * @param resource
     * @param i
     * @return
     */
    public static ImageIcon getImageIcon(final URL resource, final int size) {

        if (size <= 0) {
            return new ImageIcon(IconIO.getImage(resource));
        } else {
            return new ImageIcon(IconIO.getScaledInstance(IconIO.getImage(resource), size, size, Interpolation.BICUBIC, true));
        }
    }

    public static BufferedImage colorRangeToTransparency(BufferedImage image, Color c1, Color c2) {

        final int r1 = c1.getRed();
        final int g1 = c1.getGreen();
        final int b1 = c1.getBlue();
        final int r2 = c2.getRed();
        final int g2 = c2.getGreen();
        final int b2 = c2.getBlue();
        ImageFilter filter = new RGBImageFilter() {
            public final int filterRGB(int x, int y, int rgb) {

                int r = (rgb & 0xFF0000) >> 16;
                int g = (rgb & 0xFF00) >> 8;
                int b = rgb & 0xFF;
                if (r >= r1 && r <= r2 && g >= g1 && g <= g2 && b >= b1 && b <= b2) {
                    // Set fully transparent but keep color
                    // calculate a alpha value based on the distance between the
                    // range borders and the pixel color
                    int dist = ((Math.abs(r - (r1 + r2) / 2) + Math.abs(g - (g1 + g2) / 2) + Math.abs(b - (b1 + b2) / 2))) * 2;

                    return new Color(r, g, b, Math.min(255, dist)).getRGB();
                }

                return rgb;
            }
        };

        ImageProducer ip = new FilteredImageSource(image.getSource(), filter);
        Image img = Toolkit.getDefaultToolkit().createImage(ip);
        return toBufferedImage(img);
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
     * @param higherQuality
     *            if true, this method will use a multi-step scaling technique
     *            that provides higher quality than the usual one-step technique
     *            (only useful in downscaling cases, where {@code targetWidth}
     *            or {@code targetHeight} is smaller than the original
     *            dimensions, and generally only when the {@code BILINEAR} hint
     *            is specified)
     * @return a scaled version of the original {@code BufferedImage}
     */
    public static BufferedImage getScaledInstance(final Image img, int width, int height, final Interpolation interpolation, final boolean higherQuality) {
        double faktor = Math.max((double) img.getWidth(null) / width, (double) img.getHeight(null) / height);
        width = Math.max((int) (img.getWidth(null) / faktor), 1);
        height = Math.max((int) (img.getHeight(null) / faktor), 1);
        if (faktor == 1.0 && img instanceof BufferedImage) { return (BufferedImage) img; }

        Image ret = img;
        int w, h;
        if (higherQuality) {
            // Use multi-step technique: start with original size, then
            // scale down in multiple passes with drawImage()
            // until the target size is reached
            w = Math.max(width, img.getWidth(null));
            h = Math.max(height, img.getHeight(null));
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
            int type = 6;
            if (ret instanceof BufferedImage) {
                type = ((BufferedImage) ret).getType();
                if (type == 0) {
                    type = 6;
                }
            }
            if (w == 0) {
                int o = 2;
            }
            final BufferedImage tmp = new BufferedImage(w, h, type);
            final Graphics2D g2 = tmp.createGraphics();
            g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, interpolation.getHint());
            g2.drawImage(ret, 0, 0, w, h, null);
            g2.dispose();

            ret = tmp;
        } while (w != width || h != height);

        return (BufferedImage) ret;
    }

    /**
     * @param drop
     * @param i
     * @return
     */
    public static BufferedImage rotate(final BufferedImage src, final int degree) {
        final int w = src.getWidth(null);
        final int h = src.getHeight(null);

        // final Graphics2D g = image.createGraphics();
        // g.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
        // RenderingHints.VALUE_ANTIALIAS_ON);

        final AffineTransform at = new AffineTransform();
        at.rotate(degree * Math.PI / 180.0);
        Point2D p2din, p2dout;

        p2din = new Point2D.Double(0.0, 0.0);
        p2dout = at.transform(p2din, null);
        double ytrans = p2dout.getY();
        double xtrans = p2dout.getX();
        p2din = new Point2D.Double(0, h);
        p2dout = at.transform(p2din, null);
        ytrans = Math.min(ytrans, p2dout.getY());
        xtrans = Math.min(xtrans, p2dout.getX());
        p2din = new Point2D.Double(w, h);
        p2dout = at.transform(p2din, null);
        ytrans = Math.min(ytrans, p2dout.getY());
        xtrans = Math.min(xtrans, p2dout.getX());
        p2din = new Point2D.Double(w, 0);
        p2dout = at.transform(p2din, null);
        ytrans = Math.min(ytrans, p2dout.getY());
        xtrans = Math.min(xtrans, p2dout.getX());

        final AffineTransform tat = new AffineTransform();
        tat.translate(-xtrans, -ytrans);

        at.preConcatenate(tat);
        final AffineTransformOp bio = new AffineTransformOp(at, AffineTransformOp.TYPE_BILINEAR);

        final Rectangle r = bio.getBounds2D(src).getBounds();

        BufferedImage image = new BufferedImage(r.width, r.height, BufferedImage.TYPE_INT_ARGB);

        image = bio.filter(src, image);
        // final Graphics g = image.getGraphics();
        // g.setColor(Color.RED);
        // g.drawRect(0, 0, image.getWidth() - 1, image.getHeight() - 1);
        // g.dispose();
        // try {
        // Dialog.getInstance().showConfirmDialog(0, "", "", new
        // ImageIcon(image), null, null);
        // } catch (final DialogClosedException e) {
        // // TODO Auto-generated catch block
        // e.printStackTrace();
        // } catch (final DialogCanceledException e) {
        // // TODO Auto-generated catch block
        // e.printStackTrace();
        // }
        return image;
    }

    /**
     * Converts any image to a BufferedImage
     * 
     * @param image
     * @return
     */
    public static BufferedImage toBufferedImage(final Image src) {
        final int w = src.getWidth(null);
        final int h = src.getHeight(null);
        final BufferedImage image = new BufferedImage(w, h, Transparency.TRANSLUCENT);
        final Graphics2D g = image.createGraphics();
        g.drawImage(src, 0, 0, null);
        g.dispose();
        return image;
    }

    /**
     * @param image
     * @return
     */
    public static BufferedImage blur(BufferedImage image) {
        float[] matrix = new float[400];
        for (int i = 0; i < 400; i++)
            matrix[i] = 1.0f / 400.0f;

        BufferedImageOp op = new ConvolveOp(new Kernel(20, 20, matrix), ConvolveOp.EDGE_NO_OP, null);
        return op.filter(image, null);

    }

    /**
     * @param image
     * @param i
     * @param j
     * @return
     */
    public static BufferedImage getScaledInstance(Image img, int width, int height) {
        // TODO Auto-generated method stub
        return getScaledInstance(img, width, height, Interpolation.BICUBIC, true);
    }

    /**
     * This function removes the major color of the image and replaces it with
     * transparency.
     * 
     * @param image
     * @return
     */
    public static BufferedImage removeBackground(BufferedImage image, double tollerance) {

        HashMap<Integer, Integer> map = new HashMap<Integer, Integer>();
        int biggestValue = 0;
        int color = -1;
        for (int rgb : image.getRGB(0, 0, image.getWidth() - 1, image.getHeight() - 1, null, 0, image.getWidth())) {
            Integer v = map.get(rgb);
            if (v == null) v = 0;
            v++;
            map.put(rgb, v);
            if (v > biggestValue) {
                biggestValue = v;
                color = rgb;
            }
        }
        Color col = new Color(color);

        int r = col.getRed();
        int g = col.getGreen();
        int b = col.getBlue();
        int a = col.getAlpha();
        return colorRangeToTransparency(image, new Color(Math.max((int) (r * (1d - tollerance)), 0), Math.max((int) (g * (1d - tollerance)), 0), Math.max((int) (b * (1d - tollerance)), 0), a), new Color(Math.min(255, (int) (r * (1d + tollerance))), Math.min(255, (int) (g * (1d + tollerance))), Math.min(255, (int) (b * (1d + tollerance))), a));
    }

    public static BufferedImage createEmptyImage(int w, int h) {

        final GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
        final GraphicsDevice gd = ge.getDefaultScreenDevice();
        final GraphicsConfiguration gc = gd.getDefaultConfiguration();
        final BufferedImage image = gc.createCompatibleImage(w, h, Transparency.BITMASK);
        return image;
    }

    public static BufferedImage debug(BufferedImage img) {
        Graphics2D g2 = img.createGraphics();
        g2.setColor(Color.RED);
        g2.drawRect(0, 0, img.getWidth() - 1, img.getHeight() - 1);
        return img;
    }

    /**
     * @param object
     * @param image
     * @param i
     * @param j
     * @return
     * @return
     */
    public static BufferedImage paint(BufferedImage paintTo, Image image, int xoffset, int yoffset) {

        Graphics2D g2 = paintTo.createGraphics();
        g2.drawImage(image, xoffset, yoffset, null);
        g2.dispose();
        debug(paintTo);
        return paintTo;

    }

 
}
