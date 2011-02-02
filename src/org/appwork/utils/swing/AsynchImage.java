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
            this.x = prefX;
            this.y = prefY;
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
                    final long age = System.currentTimeMillis() - this.cache.lastModified();
                    if (this.cache.exists() && age < Updater.EXPIRETIME) {
                        // seems like another thread updated the image in the
                        // meantime

                        final BufferedImage image = ImageIO.read(this.cache);
                        if (this.asynchImage != null) {
                            this.asynchImage.setDirectIcon(new ImageIcon(image));
                        }
                        return;

                    }

                    System.out.println("Update image " + this.cache);
                    this.cache.delete();
                    if (this.url == null) { return; }

                    System.out.println("Download image " + this.cache);
                    HTTP.download(this.url, this.cache, null);

                    BufferedImage image = ImageIO.read(this.cache);
                    System.out.println("Scale image " + this.cache);
                    image = ImageProvider.getScaledInstance(image, this.x, this.y, RenderingHints.VALUE_INTERPOLATION_BILINEAR, true);
                    System.out.println("Cachewrite image " + this.cache + " " + this.x + " - " + image.getWidth());
                    ImageIO.write(image, Files.getExtension(this.cache.getName()), this.cache);

                    if (this.asynchImage != null) {
                        this.asynchImage.setDirectIcon(new ImageIcon(image));
                    }
                } catch (final IOException e) {
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
        this.prefX = x;
        this.prefY = y;
    }

    /**
     * @param thumbURL
     * @param i
     * @param j
     */
    public AsynchImage(final String thumbURL, final String extension, final int x, final int y) {
        super();

        this.prefX = x;
        this.prefY = y;
        this.setIcon(thumbURL, extension);

    }

    /**
     * @return the setIconAfterLoading
     */
    public boolean isSetIconAfterLoading() {
        return this.setIconAfterLoading;
    }

    /**
     * @param imageIcon
     */
    protected void setDirectIcon(final ImageIcon imageIcon) {
        if (AsynchImage.this.setIconAfterLoading) {

            new EDTRunner() {
                @Override
                protected void runInEDT() {
                    System.out.println("Set image " + AsynchImage.this.cache);
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
        this.cache = Application.getResource("tmp/asynchimage/" + Hash.getMD5(thumbURL) + "_" + this.prefX + "x" + this.prefY + "." + extension);

        // if cache is older than 7 days. delete
        final long age = System.currentTimeMillis() - this.cache.lastModified();
        try {
            this.url = new URL(thumbURL);
            if (this.cache.exists() && age < Updater.EXPIRETIME) {
                BufferedImage image;

                image = ImageIO.read(this.cache);

                this.setIcon(new ImageIcon(image));

                if (!this.isSetIconAfterLoading()) {
                    if (image.getWidth() > 32) {
                        // System.out.println(this.cache);
                    }

                }
            } else if (this.cache.exists()) {
                BufferedImage image;
                image = ImageIO.read(this.cache);
                this.setIcon(new ImageIcon(image));
                new Updater(this, this.prefX, this.prefY, this.cache, this.url).start();

            } else {
                new Updater(this, this.prefX, this.prefY, this.cache, this.url).start();
                this.setIcon(ImageProvider.getImageIcon("imageLoader", this.prefX, this.prefY));
            }
        } catch (final Exception e) {
            new Updater(this, this.prefX, this.prefY, this.cache, this.url).start();
            this.setIcon(ImageProvider.getImageIcon("imageLoader", this.prefX, this.prefY));
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
