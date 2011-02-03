package org.appwork.utils.swing;

import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.File;
import java.net.URL;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JLabel;

import org.appwork.utils.Application;
import org.appwork.utils.Files;
import org.appwork.utils.Hash;
import org.appwork.utils.ImageProvider.ImageProvider;
import org.appwork.utils.net.HTTP;

public class AsynchImage extends JLabel {

    public static class Updater extends Thread {

        private final File        cache;
        private final int         x;
        private final int         y;
        private final URL         url;
        private final AsynchImage asynchImage;
        public final static long  EXPIRETIME = 7 * 24 * 60 * 60 * 1000l;

        /**
         * @param asynchImage
         * @param prefX
         * @param prefY
         * @param cache
         * @param url
         */
        public Updater(final AsynchImage asynchImage, final int prefX, final int prefY, final File cache, final URL url) {
            x = prefX;
            y = prefY;
            this.cache = cache;
            this.url = url;
            this.asynchImage = asynchImage;
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
                    if (cache.exists() && age < Updater.EXPIRETIME) {
                        // seems like another thread updated the image in the
                        // meantime

                        final BufferedImage image = ImageIO.read(cache);
                        if (asynchImage != null) {
                            asynchImage.setDirectIcon(new ImageIcon(image));
                        }
                        return;

                    }

                    System.out.println("Update image " + cache);
                    cache.delete();
                    if (url == null) { return; }

                    System.out.println("Download image " + cache);
                    HTTP.download(url, cache, null);

                    BufferedImage image = ImageIO.read(cache);
                    System.out.println("Scale image " + cache);
                    image = ImageProvider.getScaledInstance(image, x, y, RenderingHints.VALUE_INTERPOLATION_BILINEAR, true);
                    System.out.println("Cachewrite image " + cache + " " + x + " - " + image.getWidth());
                    ImageIO.write(image, Files.getExtension(cache.getName()), cache);

                    if (asynchImage != null) {
                        asynchImage.setDirectIcon(new ImageIcon(image));
                    }
                } catch (final Throwable e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * 
     */
    private static final long serialVersionUID    = 1L;
    private File              cache;

    private final int         prefX;
    private final int         prefY;
    private URL               url;
    private boolean           setIconAfterLoading = true;

    private static Object     LOCK                = new Object();

    /**
     * @param i
     * @param j
     */
    public AsynchImage(final int x, final int y) {
        prefX = x;
        prefY = y;
    }

    /**
     * @param thumbURL
     * @param i
     * @param j
     */
    public AsynchImage(final String thumbURL, final String extension, final int x, final int y) {
        super();
        setBorder(new ShadowBorder(2));
        prefX = x;
        prefY = y;
        this.setIcon(thumbURL, extension);

    }

    /**
     * @return the setIconAfterLoading
     */
    public boolean isSetIconAfterLoading() {
        return setIconAfterLoading;
    }

    /**
     * @param imageIcon
     */
    protected void setDirectIcon(final ImageIcon imageIcon) {
        if (setIconAfterLoading) {

            new EDTRunner() {
                @Override
                protected void runInEDT() {
                    System.out.println("Set image " + cache);
                    AsynchImage.this.setIcon(imageIcon);
                }
            };
        }
    }

    /**
     * @param thumbURL
     * @param extension
     * @param x
     * @param y
     */
    public void setIcon(final String thumbURL, final String extension) {
        cache = Application.getResource("tmp/asynchimage/" + Hash.getMD5(thumbURL) + "_" + prefX + "x" + prefY + "." + extension);

        // if cache is older than 7 days. delete
        final long age = System.currentTimeMillis() - cache.lastModified();
        try {
            url = new URL(thumbURL);
            if (cache.exists() && age < Updater.EXPIRETIME) {
                BufferedImage image;

                image = ImageIO.read(cache);

                this.setIcon(new ImageIcon(image));

                if (!isSetIconAfterLoading()) {
                    if (image.getWidth() > 32) {
                        // System.out.println(this.cache);
                    }

                }
            } else if (cache.exists()) {
                BufferedImage image;
                image = ImageIO.read(cache);
                this.setIcon(new ImageIcon(image));
                new Updater(this, prefX, prefY, cache, url).start();

            } else {
                new Updater(this, prefX, prefY, cache, url).start();
                this.setIcon(ImageProvider.getImageIcon("imageLoader", prefX, prefY));
            }
        } catch (final Exception e) {
            new Updater(this, prefX, prefY, cache, url).start();
            this.setIcon(ImageProvider.getImageIcon("imageLoader", prefX, prefY));
        }
    }

    /**
     * @param setIconAfterLoading
     *            the setIconAfterLoading to set
     */
    public void setSetIconAfterLoading(final boolean setIconAfterLoading) {
        this.setIconAfterLoading = setIconAfterLoading;
    }

}
