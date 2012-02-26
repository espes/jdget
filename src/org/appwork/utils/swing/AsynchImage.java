package org.appwork.utils.swing;

import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.File;
import java.net.HttpURLConnection;
import java.net.URL;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JLabel;

import org.appwork.resources.AWUTheme;
import org.appwork.utils.Application;
import org.appwork.utils.Files;
import org.appwork.utils.Hash;
import org.appwork.utils.ImageProvider.ImageProvider;
import org.appwork.utils.logging.Log;
import org.appwork.utils.net.SimpleHTTP;

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
            try {
                synchronized (AsynchImage.LOCK) {
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
                }
                BufferedImage image = null;
                synchronized (AsynchImage.LOCK2) {
                    synchronized (AsynchImage.LOCK) {
                        final long age = System.currentTimeMillis() - this.cache.lastModified();
                        if (this.cache.exists() && age < Updater.EXPIRETIME) {
                            // seems like another thread updated the image in
                            // the
                            // meantime
                            image = ImageIO.read(this.cache);
                            if (this.asynchImage != null) {
                                this.asynchImage.setDirectIcon(new ImageIcon(image));
                            }
                            return;
                        }
                    }
                    Log.L.finest("Update image " + this.cache);
                    if (this.url == null) {
                        Log.L.finest("no url given");
                        return;
                    }
                    final SimpleHTTP simple = new SimpleHTTP();
                    HttpURLConnection ret = null;
                    try {
                        Log.L.finest("Call " + url);
                        ret = simple.openGetConnection(this.url, 30 * 1000);
                        Log.L.finest("DONE");
                        image = ImageIO.read(ret.getInputStream());
                    } finally {
                        try {
                            ret.disconnect();
                        } catch (final Throwable e) {
                        }
                    }
                    Log.L.finest("Scale image " + this.cache);
                    image = ImageProvider.getScaledInstance(image, this.x, this.y, RenderingHints.VALUE_INTERPOLATION_BILINEAR, true);
                }
                synchronized (AsynchImage.LOCK) {
                    final long age = System.currentTimeMillis() - this.cache.lastModified();
                    if (this.cache.exists() && age < Updater.EXPIRETIME) {
                        // seems like another thread updated the image in
                        // the
                        // meantime
                        image = ImageIO.read(this.cache);
                        if (this.asynchImage != null) {
                            this.asynchImage.setDirectIcon(new ImageIcon(image));
                        }
                        return;
                    }
                    Log.L.finest("Cachewrite image " + this.cache + " " + this.x + " - " + image.getWidth());
                    this.cache.getParentFile().mkdirs();
                    ImageIO.write(image, Files.getExtension(this.cache.getName()), this.cache);
                    if (this.asynchImage != null) {
                        this.asynchImage.setDirectIcon(new ImageIcon(image));
                    }
                }
            } catch (final Throwable e) {
                Log.exception(e);
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
    private boolean           setIconAfterLoading = true;

    public static Object      LOCK                = new Object();
    private static Object     LOCK2               = new Object();

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
        this.setBorder(new ShadowBorder(2));
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
        if (this.setIconAfterLoading) {

            new EDTRunner() {
                @Override
                protected void runInEDT() {
                    Log.L.finest("Set image " + AsynchImage.this.cache);
                    AsynchImage.this.setIcon(imageIcon);
                    AsynchImage.this.repaint();
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
        /* cacheFile for resized image */
        this.cache = Application.getResource("tmp/asynchimage/" + Hash.getMD5(thumbURL) + "_" + this.prefX + "x" + this.prefY + "." + extension);
        // if cache is older than 7 days. delete
        boolean refresh = true;
        try {
            synchronized (AsynchImage.LOCK) {
                final long age = System.currentTimeMillis() - this.cache.lastModified();
                if (this.cache.exists() && age < Updater.EXPIRETIME) {
                    refresh = false;
                    BufferedImage image;
                    image = ImageIO.read(this.cache);
                    this.setIcon(new ImageIcon(image));
                    if (!this.isSetIconAfterLoading()) {
                        if (image.getWidth() > 32) {
                            // Log.L.finest(this.cache);
                        }
                    }
                    return;
                } else if (this.cache.exists()) {
                    BufferedImage image;
                    image = ImageIO.read(this.cache);
                    this.setIcon(new ImageIcon(image));
                    return;
                }
            }
            this.setIcon(AWUTheme.getInstance().getIcon("imageLoader", this.prefX));
        } catch (final Throwable e) {
            this.setIcon(AWUTheme.getInstance().getIcon("imageLoader", this.prefX));
        } finally {
            if (refresh) {
                try {
                    new Updater(this, this.prefX, this.prefY, this.cache, new URL(thumbURL)).start();
                } catch (final Throwable e) {
                }
            }
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
