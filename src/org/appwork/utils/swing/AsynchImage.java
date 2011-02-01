package org.appwork.utils.swing;

import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.URL;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JLabel;

import org.appwork.utils.Application;
import org.appwork.utils.Files;
import org.appwork.utils.Hash;
import org.appwork.utils.ImageProvider.ImageProvider;
import org.appwork.utils.net.HTTP;

public class AsynchImage extends JLabel implements Runnable {

    /**
     * 
     */
    private static final long serialVersionUID = 1L;
    private final File        cache;
    private final long        expireTime       = 7 * 24 * 60 * 60 * 1000l;
    private final int         prefX;
    private final int         prefY;
    private URL               url;
    private static Object     LOCK             = new Object();

    /**
     * @param thumbURL
     * @param i
     * @param j
     */
    public AsynchImage(final String thumbURL, final String extension, final int x, final int y) {
        super();

        prefX = x;
        prefY = y;
        cache = Application.getResource("tmp/asynchimage/" + Hash.getMD5(thumbURL) + "_" + x + "x" + y + "." + extension);

        // if cache is older than 7 days. delete
        final long age = System.currentTimeMillis() - cache.lastModified();
        try {
            url = new URL(thumbURL);
            if (cache.exists() && age < expireTime) {
                BufferedImage image;

                image = ImageIO.read(cache);

                setIcon(new ImageIcon(image));
            } else if (cache.exists()) {
                BufferedImage image;
                image = ImageIO.read(cache);
                setIcon(new ImageIcon(image));
                new Thread(this).start();

            } else {
                new Thread(this).start();
                setIcon(ImageProvider.getImageIcon("imageLoader", x, y));
            }
        } catch (final Exception e) {
            new Thread(this).start();
            setIcon(ImageProvider.getImageIcon("imageLoader", x, y));
        }

    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Runnable#run()
     */
    @Override
    public void run() {
        synchronized (AsynchImage.LOCK) {

            try {
                // check again.
                final long age = System.currentTimeMillis() - cache.lastModified();
                if (cache.exists() && age < expireTime) {
                    // seems like another thread updated the image in the
                    // meantime

                    final BufferedImage image = ImageIO.read(cache);
                    new EDTRunner() {

                        @Override
                        protected void runInEDT() {
                            setIcon(new ImageIcon(image));

                        }
                    };
                    return;

                }

                System.out.println("Update image " + url);
                cache.delete();
                if (url == null) { return; }

                System.out.println("Download image " + url);
                HTTP.download(url, cache, null);

                BufferedImage image = ImageIO.read(cache);
                System.out.println("Scale image " + url);
                image = ImageProvider.getScaledInstance(image, prefX, prefY, RenderingHints.VALUE_INTERPOLATION_BILINEAR, true);
                System.out.println("Cachewrite image " + url);
                ImageIO.write(image, Files.getExtension(cache.getName()), cache);
                final ImageIcon icon = new ImageIcon(image);
                new EDTRunner() {
                    @Override
                    protected void runInEDT() {
                        System.out.println("Set immage " + icon.getIconWidth());
                        AsynchImage.this.setIcon(icon);
                    }
                };
            } catch (final IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
    }

}
