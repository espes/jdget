package org.appwork.utils.images;

import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Transparency;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.URL;
import java.util.logging.Level;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;

import org.appwork.utils.ImageProvider.ImageProvider;
import org.appwork.utils.logging.Log;

public class IconIO {

    /**
     * @param resource
     * @return
     */
    public static Image getImage(final URL resource) {
        if (resource != null) {
            try {
                return ImageIO.read(resource);
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
        final ImageIcon ret = IconIO.getImageIcon(resource);
        if (size <= 0) { return ret; }
        return ImageProvider.scaleImageIcon(ret, size, size);
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

}
