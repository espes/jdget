package org.appwork.utils.swing;

import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileOutputStream;
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
import org.appwork.utils.event.queue.Queue;
import org.appwork.utils.event.queue.QueueAction;
import org.appwork.utils.logging.Log;
import org.appwork.utils.net.SimpleHTTP;

public class AsynchImage extends JLabel {

    public static class Updater extends QueueAction<Void, RuntimeException> {

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
        public Void run() {
            try {
                synchronized (AsynchImage.LOCK) {
                    // check again.
                    final long age = System.currentTimeMillis() - cache.lastModified();
                    if (cache.exists() && age < Updater.EXPIRETIME) {
                        // seems like another thread updated the image in the
                        // meantime
                        final BufferedImage image = ImageProvider.read(cache);
                        if (asynchImage != null) {
                            asynchImage.setDirectIcon(new ImageIcon(image));
                        }
                        return null;
                    }
                }
                BufferedImage image = null;
                synchronized (AsynchImage.LOCK2) {
                    synchronized (AsynchImage.LOCK) {
                        final long age = System.currentTimeMillis() - cache.lastModified();
                        if (cache.exists() && age < Updater.EXPIRETIME) {
                            // seems like another thread updated the image in
                            // the
                            // meantime
                            image = ImageProvider.read(cache);
                            if (asynchImage != null) {
                                asynchImage.setDirectIcon(new ImageIcon(image));
                            }
                            return null;
                        }
                    }
                    Log.L.finest("Update image " + cache);
                    if (url == null) {
                        Log.L.finest("no url given");
                        return null;
                    }
                    final SimpleHTTP simple = new SimpleHTTP();
                    HttpURLConnection ret = null;
                    try {
                        Log.L.finest("Call " + url);
                        ret = simple.openGetConnection(url, 30 * 1000);
                        Log.L.finest("DONE");
                        image = ImageIO.read(ret.getInputStream());
                    } finally {
                        try {
                            ret.disconnect();
                        } catch (final Throwable e) {
                        }
                        try {
                            simple.getConnection().disconnect();
                        } catch (final Throwable e) {
                        }
                    }
                    Log.L.finest("Scale image " + cache);
                    image = ImageProvider.getScaledInstance(image, x, y, RenderingHints.VALUE_INTERPOLATION_BILINEAR, true);
                }
                synchronized (AsynchImage.LOCK) {
                    final long age = System.currentTimeMillis() - cache.lastModified();
                    if (cache.exists() && age < Updater.EXPIRETIME) {
                        // seems like another thread updated the image in
                        // the
                        // meantime
                        image = ImageProvider.read(cache);
                        if (asynchImage != null) {
                            asynchImage.setDirectIcon(new ImageIcon(image));
                        }
                        return null;
                    }
                    Log.L.finest("Cachewrite image " + cache + " " + x + " - " + image.getWidth());
                    cache.getParentFile().mkdirs();
                    FileOutputStream fos = null;
                    try {
                        fos = new FileOutputStream(cache);
                        ImageIO.write(image, Files.getExtension(cache.getName()), fos);
                    } finally {
                        try {
                            fos.close();
                        } catch (final Throwable e) {
                        }
                    }
                    if (asynchImage != null) {
                        asynchImage.setDirectIcon(new ImageIcon(image));
                    }
                }
            } catch (final Throwable e) {
                Log.L.severe("Error loading Url:"+url);
      
                Log.exception(e);
            
            }
            return null;
        }

        public void start() {
            AsynchImage.QUEUE.add(this);
        }
    }

    private static final Queue QUEUE               = new Queue(AsynchImage.class.getName() + "-Queue") {

                                                   };

    /**
     * 
     */
    private static final long  serialVersionUID    = 1L;
    private File               cache;

    private final int          prefX;
    private final int          prefY;
    private boolean            setIconAfterLoading = true;

    public static Object       LOCK                = new Object();
    private static Object      LOCK2               = new Object();

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
                    Log.L.finest("Set image " + cache);
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
        cache = Application.getTempResource("asynchimage/" + Hash.getMD5(thumbURL) + "_" + prefX + "x" + prefY + "." + extension);
        // if cache is older than 7 days. delete
        boolean refresh = true;
        try {
            synchronized (AsynchImage.LOCK) {
                final long age = System.currentTimeMillis() - cache.lastModified();
                if (cache.exists() && age < Updater.EXPIRETIME) {
                    refresh = false;
                    BufferedImage image;
                    image = ImageProvider.read(cache);
                    this.setIcon(new ImageIcon(image));
                    if (!isSetIconAfterLoading()) {
                        if (image.getWidth() > 32) {
                            // Log.L.finest(this.cache);
                        }
                    }
                    return;
                } else if (cache.exists()) {
                    BufferedImage image;
                    image = ImageProvider.read(cache);
                    this.setIcon(new ImageIcon(image));
                    return;
                }
            }
            this.setIcon(AWUTheme.getInstance().getIcon("imageLoader", prefX));
        } catch (final Throwable e) {
            this.setIcon(AWUTheme.getInstance().getIcon("imageLoader", prefX));
        } finally {
            if (refresh) {
                try {
                    new Updater(this, prefX, prefY, cache, new URL(thumbURL)).start();
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
