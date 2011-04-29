/**
 * Copyright (c) 2009 - 2011 AppWork UG(haftungsbeschränkt) <e-mail@appwork.org>
 * 
 * This file is part of org.appwork.screenshot
 * 
 * This software is licensed under the Artistic License 2.0,
 * see the LICENSE file or http://www.opensource.org/licenses/artistic-license-2.0.php
 * for details
 */
package org.appwork.screenshot;

import java.awt.AWTException;
import java.awt.AlphaComposite;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Composite;
import java.awt.Cursor;
import java.awt.DisplayMode;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.Image;
import java.awt.MouseInfo;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Robot;
import java.awt.Toolkit;
import java.awt.Transparency;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.image.BufferStrategy;
import java.awt.image.BufferedImage;
import java.awt.image.MemoryImageSource;

import javax.swing.JWindow;
import javax.swing.Timer;

import org.appwork.utils.locale.APPWORKUTILS;
import org.appwork.utils.os.CrossSystem;

/**
 * @author thomas
 * 
 */
public class Layover extends JWindow implements ActionListener, MouseListener {
    private static final long   serialVersionUID = 3184465232251321247L;
    private static final int    SIZE             = 150;
    private static final double FACTOR           = 4.0;
    private static final int    SCALED_SIZE      = (int) (Layover.SIZE / Layover.FACTOR);

    /**
     * @return
     * @throws AWTException
     */
    public static Layover create() throws AWTException {
        final GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
        final GraphicsDevice[] screens = ge.getScreenDevices();

        // Get size of each screen
        final Robot robot;

        robot = new Robot();

        // for (final GraphicsDevice screen : screens) {
        int xMax = 0;
        int xMin = 0;
        int yMax = 0;
        int yMin = 0;
        for (final GraphicsDevice screen : screens) {
            final Rectangle bounds = screen.getDefaultConfiguration().getBounds();
            xMax = Math.max(xMax, bounds.x + bounds.width);
            yMax = Math.max(bounds.y + bounds.height, yMax);
            yMin = Math.min(yMin, bounds.y);
            xMin = Math.min(xMin, bounds.x);
        }
        final BufferedImage complete = new BufferedImage(xMax - xMin, yMax - yMin, Transparency.TRANSLUCENT);
        final BufferedImage completeGrayed = new BufferedImage(xMax - xMin, yMax - yMin, Transparency.TRANSLUCENT);
        final Graphics2D g2gray = completeGrayed.createGraphics();
        final Graphics2D g2 = complete.createGraphics();

        for (final GraphicsDevice screen : screens) {
            final DisplayMode dm = screen.getDisplayMode();
            System.out.println(screen.getDefaultConfiguration().getBounds());
            final Rectangle bounds = screen.getDefaultConfiguration().getBounds();
            final int screenWidth = dm.getWidth();
            final int screenHeight = dm.getHeight();

            System.out.println(screen + " : " + screenWidth + "x" + screenHeight);
            final Rectangle rect = new Rectangle(screenWidth, screenHeight);
            rect.setLocation(bounds.x, bounds.y);

            final BufferedImage image = robot.createScreenCapture(rect);
            g2.drawImage(image, bounds.x - xMin, bounds.y - yMin, null);
            g2gray.drawImage(image, bounds.x - xMin, bounds.y - yMin, null);
            final Composite comp = g2gray.getComposite();
            g2gray.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.3f));
            g2gray.setColor(Color.BLACK);
            g2gray.fillRect(bounds.x - xMin, bounds.y - yMin, screenWidth, screenHeight);
            g2gray.drawImage(image, bounds.x - xMin, bounds.y - yMin, null);
            g2gray.setComposite(comp);
        }
        g2.dispose();
        g2gray.dispose();
        final Layover layover = new Layover();
        layover.setImage(complete, completeGrayed);
        return layover;
    }

    private BufferedImage     image;
    private int               lastX;

    private int               lastY;
    private boolean           isDragging = false;
    private Point             dragStart;
    private Point             dragEnd;
    // private VolatileImage volatileImg;
    private final Timer       timer;
    private BufferedImage     grayedImage;

    private final Rectangle[] bounds;

    public Layover() {
        super();
        this.timer = new Timer(1000 / 250, this);
        this.timer.setRepeats(true);

        this.setVisible(true);
        final int[] pixels = new int[16 * 16];
        final Image image = Toolkit.getDefaultToolkit().createImage(new MemoryImageSource(16, 16, pixels, 0, 16));
        final Cursor transparentCursor = Toolkit.getDefaultToolkit().createCustomCursor(image, new Point(0, 0), "invisibleCursor");
        this.setCursor(transparentCursor);
        this.addMouseListener(this);
        this.createBufferStrategy(2);
        final GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
        final GraphicsDevice[] screens = ge.getScreenDevices();
        this.bounds = new Rectangle[screens.length];
        for (int i = 0; i < this.bounds.length; i++) {
            this.bounds[i] = screens[i].getDefaultConfiguration().getBounds();
        }

    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
     */
    @Override
    public void actionPerformed(final ActionEvent e) {
        final Point l = MouseInfo.getPointerInfo().getLocation();
        if (this.lastX != l.x || this.lastY != l.y) {
            this.updateGUI(this.getBufferStrategy());
        }
        this.lastX = l.x;
        this.lastY = l.y;
    }

    /**
     * 
     */
    private void cancel() {
        if (this.isDragging) {
            this.stopDrag();
            this.dragStart = null;
            this.dragEnd = null;
        } else {

            this.setVisible(false);
            this.dispose();

        }
    }

    private Rectangle getDeviceBounds(final Point l) {
        for (final Rectangle r : this.bounds) {
            if (l.x >= r.x && l.x <= r.x + r.width) {
                // x correct
                if (l.y >= r.y && l.y <= r.y + r.height) {
                    // y correct
                    return r;
                }

            }
        }
        return null;
    }

    /**
     * @param l
     * @return
     */
    private Point getMagPosition(final Point l) {

        final Rectangle bounds = this.getDeviceBounds(l);
        if (bounds == null) { return null; }
        int x = l.x + 20;
        if (x + Layover.SIZE > bounds.x + bounds.width) {
            x = l.x - Layover.SIZE - 20;
        }
        int y = l.y - Layover.SIZE - 20;
        if (y < bounds.y) {
            y = l.y + 20;
        }
        return new Point(x, y);
    }

    /**
     * 
     */
    // private void createBackBuffer() {
    // final GraphicsConfiguration gc = this.getGraphicsConfiguration();
    // this.volatileImg = gc.createCompatibleVolatileImage(this.getWidth(),
    // this.getHeight());
    //
    // }

    /*
     * (non-Javadoc)
     * 
     * @see java.awt.event.MouseListener#mouseClicked(java.awt.event.MouseEvent)
     */
    @Override
    public void mouseClicked(final MouseEvent e) {
        if (e.isPopupTrigger() || e.getButton() == MouseEvent.BUTTON3) {

            this.cancel();
        }

    }

    /*
     * (non-Javadoc)
     * 
     * @see java.awt.event.MouseListener#mouseEntered(java.awt.event.MouseEvent)
     */
    @Override
    public void mouseEntered(final MouseEvent e) {
        // TODO Auto-generated method stub

    }

    /*
     * (non-Javadoc)
     * 
     * @see java.awt.event.MouseListener#mouseExited(java.awt.event.MouseEvent)
     */
    @Override
    public void mouseExited(final MouseEvent e) {
        // TODO Auto-generated method stub

    }

    /*
     * (non-Javadoc)
     * 
     * @see java.awt.event.MouseListener#mousePressed(java.awt.event.MouseEvent)
     */
    @Override
    public void mousePressed(final MouseEvent e) {
        if (!this.isDragging) {

            this.startDrag();
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * java.awt.event.MouseListener#mouseReleased(java.awt.event.MouseEvent)
     */
    @Override
    public void mouseReleased(final MouseEvent e) {
        if (this.isDragging) {
            this.stopDrag();
        }
    }

    @Override
    public void paint(final Graphics g) {
        // this.createBackBuffer();
        // do {
        // final GraphicsConfiguration gc = this.getGraphicsConfiguration();
        //
        // switch (this.volatileImg.validate(gc)) {
        // // This means the device doesn't match up to this hardware
        // // accelerated image.
        // case VolatileImage.IMAGE_INCOMPATIBLE:
        // // recreate the hardware accelerated image.
        // this.createBackBuffer();
        // break;
        // }
        //
        // final Graphics2D gb = (Graphics2D) this.volatileImg.getGraphics();
        //
        // this.doPaint(gb);
        // g.drawImage(this.volatileImg, 0, 0, null);
        // } while (this.volatileImg.contentsLost());
    }

    /**
     * @param gb
     * @param l
     */
    private void paintMag(final Graphics2D gb, final Point l) {
        final Point pos = this.getMagPosition(l);

        gb.drawImage(this.image, pos.x, pos.y, pos.x + Layover.SIZE, pos.y + Layover.SIZE, l.x - Layover.SCALED_SIZE / 2, l.y - Layover.SCALED_SIZE / 2, l.x + Layover.SCALED_SIZE / 2, l.y + Layover.SCALED_SIZE / 2, Color.BLACK, null);
        gb.setColor(Color.BLACK);
        gb.setStroke(new BasicStroke(1));
        gb.drawRect(pos.x, pos.y, Layover.SIZE, Layover.SIZE);

        final Composite comp = gb.getComposite();
        gb.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.3f));

        gb.setColor(Color.BLACK);

        gb.setStroke(new BasicStroke(4));
        gb.drawLine(pos.x + 2, pos.y + Layover.SIZE / 2, pos.x + Layover.SIZE - 2, pos.y + Layover.SIZE / 2);
        // gb.setColor(Color.RED);
        gb.drawLine(pos.x + Layover.SIZE / 2, pos.y + 2, pos.x + Layover.SIZE / 2, pos.y + Layover.SIZE / 2 - 4);
        gb.drawLine(pos.x + Layover.SIZE / 2, pos.y + Layover.SIZE / 2 + 4, pos.x + Layover.SIZE / 2, pos.y + Layover.SIZE - 2);
        gb.setComposite(comp);

        //
        if (this.isDragging) {
            gb.setStroke(new BasicStroke(1));
            gb.setColor(Color.WHITE);
            gb.fillRect(pos.x + 1, pos.y + Layover.SIZE - gb.getFontMetrics().getHeight(), Layover.SIZE - 1, gb.getFontMetrics().getHeight());

            gb.setColor(Color.GRAY);
            gb.drawLine(pos.x, pos.y + Layover.SIZE - gb.getFontMetrics().getHeight(), pos.x + Layover.SIZE, pos.y + Layover.SIZE - gb.getFontMetrics().getHeight());
            final String dimension = APPWORKUTILS.T.Layover_size(Math.abs(l.x - this.dragStart.x), Math.abs(l.y - this.dragStart.y));
            gb.getFontMetrics().stringWidth(dimension);
            gb.drawString(dimension, pos.x + 5, pos.y + Layover.SIZE - 3);
        }
        //
        String str = l.y + " px";
        final Rectangle db = this.getDeviceBounds(l);
        int width = gb.getFontMetrics().stringWidth(str) + 10;
        int height = gb.getFontMetrics().getHeight() + 5;
        gb.setStroke(new BasicStroke(1));
        gb.setColor(Color.white);
        int y = l.y - height / 2;
        if (y < db.y) {
            y = db.y;
        }
        if (y + height + 5 > db.height + db.y) {
            y = db.height + db.y - height - 5;
        }
        gb.fillRect(db.x + db.width - width - 10, y, width, height);
        gb.setColor(Color.GRAY);
        gb.drawRect(db.x + db.width - width - 10, y, width, height);
        gb.drawString(str, db.x + db.width - width - 5, y + height - 5);

        //
        str = l.x + " px";

        width = gb.getFontMetrics().stringWidth(str) + 10;
        height = gb.getFontMetrics().getHeight() + 5;
        gb.setStroke(new BasicStroke(1));
        gb.setColor(Color.white);
        int x = l.x - width / 2;
        if (x < db.x + 5) {
            x = db.x + 5;
        }
        if (x + width + 5 > db.x + db.width) {
            x = db.x + db.width - width - 5;
        }
        if (db.y + db.height - height - 5 - y <= height) {
            // on mac, we cannot override the topbar which is 22 px height
            gb.fillRect(x, db.y + (CrossSystem.isMac() ? 22 + 5 : 5), width, height);
            gb.setColor(Color.GRAY);
            gb.drawRect(x, +(CrossSystem.isMac() ? 22 + 5 : 5), width, height);
            gb.drawString(str, x + 5, (CrossSystem.isMac() ? 22 + height : height));
        } else {
            gb.fillRect(x, db.y + db.height - height - 5, width, height);
            gb.setColor(Color.GRAY);
            gb.drawRect(x, db.y + db.height - height - 5, width, height);
            gb.drawString(str, x + 5, db.y + db.height - 10);
        }
        //

    }

    /**
     * @param complete
     * @param completeGrayed
     */
    public void setImage(final BufferedImage complete, final BufferedImage completeGrayed) {
        this.image = complete;
        this.grayedImage = completeGrayed;
        this.setSize(complete.getWidth(), complete.getHeight());
        this.setLocation(0, 0);

    }

    /**
     * 
     */
    public void start() {
        this.timer.start();
        this.setVisible(true);
    }

    /**
     * 
     */
    private void startDrag() {
        this.isDragging = true;
        this.dragStart = MouseInfo.getPointerInfo().getLocation();
        System.out.println("Start Drag " + this.dragStart);
    }

    /**
     * 
     */
    private void stopDrag() {
        this.isDragging = false;
        this.dragEnd = MouseInfo.getPointerInfo().getLocation();
        System.out.println("StopDrag");
    }

    /**
     * @param bufferStrategy
     */
    private void updateGUI(final BufferStrategy bufferStrategy) {
        try {
            final Point l = MouseInfo.getPointerInfo().getLocation();
            final Graphics2D gb = (Graphics2D) bufferStrategy.getDrawGraphics();

            if (this.dragStart != null) {
                final int startX = Math.min(this.dragStart.x, l.x);
                final int startY = Math.min(this.dragStart.y, l.y);
                gb.drawImage(this.grayedImage, 0, 0, null);
                final int endX = Math.max(l.x, this.dragStart.x);
                final int endY = Math.max(l.y, this.dragStart.y);
                gb.drawImage(this.image, startX, startY, endX, endY, startX, startY, endX, endY, null);
                gb.setColor(Color.GRAY);
                final float dash[] = { 10.0f };
                gb.setStroke(new BasicStroke(1.0f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 10.0f, dash, 0.0f));
                gb.drawLine(0, l.y, this.image.getWidth(), l.y);
                gb.drawLine(l.x, 0, l.x, this.image.getHeight());
                gb.setColor(Color.BLACK);
                gb.setStroke(new BasicStroke(1));
                gb.drawRect(startX, startY, Math.abs(l.x - this.dragStart.x), Math.abs(l.y - this.dragStart.y));
                gb.setStroke(new BasicStroke(1));
            } else {
                gb.drawImage(this.image, 0, 0, null);
                gb.setStroke(new BasicStroke(1));
                gb.setColor(Color.GRAY);
                final float dash[] = { 10.0f };
                gb.setStroke(new BasicStroke(1.0f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 10.0f, dash, 0.0f));
                gb.drawLine(0, l.y, this.image.getWidth(), l.y);
                gb.drawLine(l.x, 0, l.x, this.image.getHeight());
            }
            gb.setStroke(new BasicStroke(1));

            gb.drawLine(l.x - 10, l.y, l.x + 10, l.y);
            gb.drawLine(l.x, l.y - 10, l.x, l.y + 10);
            this.paintMag(gb, l);
            gb.dispose();
            bufferStrategy.show();
        } catch (final Exception e) {

        }
    }

}
