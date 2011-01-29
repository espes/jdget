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

    /**
     * @param thumbURL
     * @param i
     * @param j
     */
    public AsynchImage(final String thumbURL, final String extension, final int x, final int y) {
        super();

        this.prefX = x;
        this.prefY = y;
        this.cache = Application.getResource("tmp/asynchimage/" + Hash.getMD5(thumbURL) + "_" + x + "x" + y + "." + extension);

        // if cache is older than 7 days. delete
        final long age = System.currentTimeMillis() - this.cache.lastModified();
        try {
            this.url = new URL(thumbURL);
            if (this.cache.exists() && age < this.expireTime) {
                BufferedImage image;

                image = ImageIO.read(this.cache);

                this.setIcon(new ImageIcon(image));
            } else {
                new Thread(this).start();
                this.setIcon(ImageProvider.getImageIcon("imageLoader", x, y));
            }
        } catch (final Exception e) {
            new Thread(this).start();
            this.setIcon(ImageProvider.getImageIcon("imageLoader", x, y));
        }

    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Runnable#run()
     */
    @Override
    public void run() {
        this.cache.delete();
        if (this.url == null) { return; }
        try {
            HTTP.download(this.url, this.cache, null);

            BufferedImage image = ImageIO.read(this.cache);
            image = ImageProvider.getScaledInstance(image, this.prefX, this.prefY, RenderingHints.VALUE_INTERPOLATION_BILINEAR, true);
            ImageIO.write(image, Files.getExtension(this.cache.getName()), this.cache);
            final ImageIcon icon = new ImageIcon(image);
            new EDTRunner() {
                @Override
                protected void runInEDT() {
                    AsynchImage.this.setIcon(icon);
                }
            };
        } catch (final IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

    }

}
