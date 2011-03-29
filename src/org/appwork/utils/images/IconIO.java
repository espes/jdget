package org.appwork.utils.images;

import java.awt.Image;
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
        return ImageProvider.scaleImageIcon(IconIO.getImageIcon(resource), size, size);
    }

}
