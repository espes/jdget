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
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.image.BufferStrategy;
import java.awt.image.BufferedImage;
import java.awt.image.MemoryImageSource;

import javax.swing.JFrame;

import org.appwork.utils.locale.APPWORKUTILS;
import org.appwork.utils.os.CrossSystem;

/**
 * @author thomas
 * 
 */
public class ScreenShooter extends JFrame implements MouseListener {
    private static final long   serialVersionUID = 3184465232251321247L;
    /**
     * Size of the Mag Glass
     */
    private static final int    SIZE             = 150;
    /**
     * Mag resize factor
     */
    private static final double FACTOR           = 5.0;

    private static final int    SCALED_SIZE      = (int) (ScreenShooter.SIZE / ScreenShooter.FACTOR);
    protected static final int  FPS              = 50;

    /**
     * Creates a screenshot of all available screens. and returns the
     * ScreenShooter
     * 
     * @return
     * @throws AWTException
     */
    public static ScreenShooter create() throws AWTException {
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
        // we create a normal screenshot and a grayed screenshot
        final BufferedImage completeGrayed = new BufferedImage(xMax - xMin, yMax - yMin, Transparency.TRANSLUCENT);
        final Graphics2D g2gray = completeGrayed.createGraphics();
        final Graphics2D g2 = complete.createGraphics();

        for (final GraphicsDevice screen : screens) {
            final DisplayMode dm = screen.getDisplayMode();
            // bounds are used to gete the position and size of this screen in
            // the complete screen configuration
            final Rectangle bounds = screen.getDefaultConfiguration().getBounds();
            final int screenWidth = dm.getWidth();
            final int screenHeight = dm.getHeight();
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
        final ScreenShooter layover = new ScreenShooter();
        layover.setImage(complete, completeGrayed);
        return layover;
    }

    private BufferedImage     image;

    private boolean           isDragging = false;
    private Point             dragStart;
    private Point             dragEnd;

    private BufferedImage     grayedImage;

    private final Rectangle[] bounds;
    private BufferedImage     screenshot;

    public ScreenShooter() {
        super();

        // we extends from a JFrame because JWindow cannot get focus and this
        // cannot listen on key events
        this.setUndecorated(true);

        // invisible cursor
        final int[] pixels = new int[16 * 16];
        final Image image = Toolkit.getDefaultToolkit().createImage(new MemoryImageSource(16, 16, pixels, 0, 16));
        final Cursor transparentCursor = Toolkit.getDefaultToolkit().createCustomCursor(image, new Point(0, 0), "invisibleCursor");
        this.setCursor(transparentCursor);
        this.addMouseListener(this);
        this.addKeyListener(new KeyListener() {

            @Override
            public void keyPressed(final KeyEvent e) {
                // TODO Auto-generated method stub

            }

            @Override
            public void keyReleased(final KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ESCAPE) {
                    ScreenShooter.this.cancel();
                }
            }

            @Override
            public void keyTyped(final KeyEvent e) {
                // TODO Auto-generated method stub

            }
        });
        // see
        // http://www.javalobby.org/forums/thread.jspa?threadID=16867&tstart=0

        final GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
        final GraphicsDevice[] screens = ge.getScreenDevices();
        // store screen device bounds to find current screen later easily
        this.bounds = new Rectangle[screens.length];
        for (int i = 0; i < this.bounds.length; i++) {
            this.bounds[i] = screens[i].getDefaultConfiguration().getBounds();
        }

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

    /**
     * Converts yourPoint to a coresponding point in the mag glass
     * 
     * @param mouselocation
     * @param mag
     *            glass location
     * @param yourPoint
     * @return
     */
    private Point convertMag(final Point l, final Point mag, final Point p) {

        final int tx = (int) ((p.x - l.x) * ScreenShooter.FACTOR + l.x + mag.x + ScreenShooter.SIZE / 2 - l.x);
        final int ty = (int) ((p.y - l.y) * ScreenShooter.FACTOR + l.y + mag.y + ScreenShooter.SIZE / 2 - l.y);
        return new Point(tx, ty);
    }

    /**
     * Cuts the given range from the image(screenshot)
     * 
     * @param x
     * @param y
     * @param x2
     * @param y2
     * @return
     */
    private BufferedImage cut(final int x1, final int y1, final int x2, final int y2) {
        final int width = Math.abs(x1 - x2);
        final int height = Math.abs(y1 - y2);
        final int sX = Math.min(x1, x2);
        final int sY = Math.min(y1, y2);
        if (width <= 0 || height <= 0) { return null; }
        final BufferedImage ret = new BufferedImage(width, height, Transparency.TRANSLUCENT);
        final Graphics2D gb = (Graphics2D) ret.getGraphics();
        gb.drawImage(this.image, 0, 0, width, height, sX, sY, sX + width, sY + height, null);
        gb.dispose();
        return ret;
    }

    /**
     * get the device bounds of the device l is in. Use to find the currently
     * used screen
     * 
     * @param l
     * @return
     */
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
     * calculates the position of the mag. mag position relative to the
     * mouseposition changes if we reach the screen devices bounds
     * 
     * @param l
     * @return
     */
    private Point getMagPosition(final Point l) {

        final Rectangle bounds = this.getDeviceBounds(l);
        if (bounds == null) { return null; }
        int x = l.x + 20;
        if (x + ScreenShooter.SIZE > bounds.x + bounds.width) {
            x = l.x - ScreenShooter.SIZE - 20;
        }
        int y = l.y - ScreenShooter.SIZE - 20;
        if (y < bounds.y) {
            y = l.y + 20;
        }
        return new Point(x, y);
    }

    /**
     * gets the selected Screenshot. Blocks until a screenshot is available, or
     * the user canceled
     * 
     * @return
     * @throws InterruptedException
     */
    public BufferedImage getScreenshot() throws InterruptedException {
        while (this.screenshot == null && this.isVisible()) {
            Thread.sleep(100);
        }
        return this.screenshot;
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.awt.event.MouseListener#mouseClicked(java.awt.event.MouseEvent)
     */
    @Override
    public void mouseClicked(final MouseEvent e) {

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
        if (e.isPopupTrigger() || e.getButton() == MouseEvent.BUTTON3) {

            this.cancel();
        } else if (!this.isDragging) {

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
        if (e.isPopupTrigger() || e.getButton() == MouseEvent.BUTTON3) { return; }
        if (this.isDragging) {
            this.stopDrag();
            this.screenshot = this.cut(this.dragStart.x, this.dragStart.y, this.dragEnd.x, this.dragEnd.y);
            this.setVisible(false);

        }
    }

    /**
     * Paints the mag, and the position values
     * 
     * @param gb
     * @param l
     */
    private void paintMag(final Graphics2D gb, final Point l) {
        final Point pos = this.getMagPosition(l);
        // draw and resize the mag image
        gb.drawImage(this.image, pos.x, pos.y, pos.x + ScreenShooter.SIZE, pos.y + ScreenShooter.SIZE, l.x - ScreenShooter.SCALED_SIZE / 2, l.y - ScreenShooter.SCALED_SIZE / 2, l.x + ScreenShooter.SCALED_SIZE / 2, l.y + ScreenShooter.SCALED_SIZE / 2, Color.BLACK, null);

        // Draws the black alpha cross
        Composite comp = gb.getComposite();
        gb.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.3f));
        gb.setColor(Color.BLACK);
        gb.setStroke(new BasicStroke(5));
        gb.drawLine(pos.x + 2, pos.y + ScreenShooter.SIZE / 2 + 2, pos.x + ScreenShooter.SIZE - 2, pos.y + ScreenShooter.SIZE / 2 + 2);
        // gb.setColor(Color.RED);
        gb.drawLine(pos.x + ScreenShooter.SIZE / 2 + 2, pos.y + 2, pos.x + ScreenShooter.SIZE / 2 + 2, pos.y + ScreenShooter.SIZE / 2 - 3);
        gb.drawLine(pos.x + ScreenShooter.SIZE / 2 + 2, pos.y + ScreenShooter.SIZE / 2 + 7, pos.x + ScreenShooter.SIZE / 2 + 2, pos.y + ScreenShooter.SIZE - 2);
        gb.setComposite(comp);

        //
        if (this.isDragging) {
            // if we are dragging we paint a white area to paint the selected
            // are size
            gb.setStroke(new BasicStroke(1));
            gb.setColor(Color.WHITE);
            gb.fillRect(pos.x + 1, pos.y + ScreenShooter.SIZE - gb.getFontMetrics().getHeight(), ScreenShooter.SIZE - 1, gb.getFontMetrics().getHeight());
            gb.setColor(Color.GRAY);
            gb.drawLine(pos.x, pos.y + ScreenShooter.SIZE - gb.getFontMetrics().getHeight(), pos.x + ScreenShooter.SIZE, pos.y + ScreenShooter.SIZE - gb.getFontMetrics().getHeight());
            final String dimension = APPWORKUTILS.T.Layover_size(Math.abs(l.x - this.dragStart.x), Math.abs(l.y - this.dragStart.y));
            gb.getFontMetrics().stringWidth(dimension);
            gb.drawString(dimension, pos.x + 5, pos.y + ScreenShooter.SIZE - 3);

            // Paint the blue selection rectangle in the mag.
            final int startX = Math.min(this.dragStart.x, l.x);
            final int startY = Math.min(this.dragStart.y, l.y);
            final int endX = Math.max(l.x, this.dragStart.x);
            final int endY = Math.max(l.y, this.dragStart.y);
            final Point start = this.convertMag(l, pos, new Point(startX, startY));
            final Point end = this.convertMag(l, pos, new Point(endX, endY));
            gb.setColor(Color.BLUE);
            gb.drawRect(Math.max(pos.x, start.x), Math.max(start.y, pos.y), Math.min(ScreenShooter.SIZE / 2, end.x - start.x), Math.min(ScreenShooter.SIZE / 2, end.y - start.y));

            comp = gb.getComposite();
            gb.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.1f));

            gb.fillRect(Math.max(pos.x, start.x), Math.max(start.y, pos.y), Math.min(ScreenShooter.SIZE / 2, end.x - start.x), Math.min(ScreenShooter.SIZE / 2, end.y - start.y));
            gb.setComposite(comp);
        }
        // paint the position marker
        String str = l.y + " px";
        final Rectangle db = this.getDeviceBounds(l);
        int width = gb.getFontMetrics().stringWidth(str) + 10;
        int height = gb.getFontMetrics().getHeight() + 5;
        gb.setStroke(new BasicStroke(1));
        gb.setColor(Color.white);
        int y = l.y - height / 2;
        if (y < db.y) {
            // dock marker on top
            y = db.y;
        }
        if (y + height + 5 > db.height + db.y) {
            y = db.height + db.y - height - 5;
            // dock marker on bottom
        }
        // paint marker
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
            // marker reached left margin we dock here
        }
        if (x + width + 5 > db.x + db.width) {
            // marker reached right margin. we dock here
            x = db.x + db.width - width - 5;
        }

        // avoid that marker overlap at the bottom right corner. set X marker to
        // top if x and y markers share the same y position
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
        // paint black border

        gb.setColor(Color.BLACK);
        gb.setStroke(new BasicStroke(1));
        gb.drawRect(pos.x, pos.y, ScreenShooter.SIZE, ScreenShooter.SIZE);

    }

    /**
     * @param complete
     * @param completeGrayed
     */
    private void setImage(final BufferedImage complete, final BufferedImage completeGrayed) {
        this.image = complete;
        this.grayedImage = completeGrayed;
        this.setSize(complete.getWidth(), complete.getHeight());
        this.setLocation(0, 0);

    }

    /**
     * 
     */
    public void start() {

        this.setVisible(true);
        this.createBufferStrategy(2);

        // this.timer.start();
        new Thread("Asynchpainter") {
            @Override
            public void run() {
                final long t = System.currentTimeMillis();
                final int frame = 1000 / ScreenShooter.FPS;
                while (ScreenShooter.this.isVisible()) {

                    ScreenShooter.this.updateGUI(ScreenShooter.this.getBufferStrategy());
                    try {
                        Thread.sleep(Math.max(0, frame - System.currentTimeMillis() - t));
                    } catch (final InterruptedException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                }
            }
        }.start();

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
        System.out.println("StopDrag ");
    }

    /**
     * Paints the complete screen
     * 
     * @param bufferStrategy
     */
    private void updateGUI(final BufferStrategy bufferStrategy) {
        try {

            final Point l = MouseInfo.getPointerInfo().getLocation();
            final Graphics2D gb = (Graphics2D) bufferStrategy.getDrawGraphics();

            if (this.isDragging) {
                final int startX = Math.min(this.dragStart.x, l.x);
                final int startY = Math.min(this.dragStart.y, l.y);
                // draw grayed image over full screen
                gb.drawImage(this.grayedImage, 0, 0, null);
                final int endX = Math.max(l.x, this.dragStart.x);
                final int endY = Math.max(l.y, this.dragStart.y);
                // draw ungrayed icon as selection
                gb.drawImage(this.image, startX, startY, endX, endY, startX, startY, endX, endY, null);
                gb.setColor(Color.GRAY);
                // draw BIG dashed hair cross
                final float dash[] = { 10.0f };
                gb.setStroke(new BasicStroke(1.0f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 10.0f, dash, 0.0f));
                gb.drawLine(0, l.y, this.image.getWidth(), l.y);
                gb.drawLine(l.x, 0, l.x, this.image.getHeight());
                // Draw selection Border
                gb.setColor(Color.BLACK);
                gb.setStroke(new BasicStroke(1));
                gb.drawRect(startX, startY, Math.abs(l.x - this.dragStart.x), Math.abs(l.y - this.dragStart.y));
                gb.setStroke(new BasicStroke(1));
            } else {
                // draw screenshot image
                gb.drawImage(this.image, 0, 0, null);
                // draw dashed cross
                gb.setStroke(new BasicStroke(1));
                gb.setColor(Color.GRAY);
                final float dash[] = { 10.0f };
                gb.setStroke(new BasicStroke(1.0f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 10.0f, dash, 0.0f));
                gb.drawLine(0, l.y, this.image.getWidth(), l.y);
                gb.drawLine(l.x, 0, l.x, this.image.getHeight());
            }
            // draw tiny cross at mouse location
            gb.setStroke(new BasicStroke(1));
            gb.drawLine(l.x - 10, l.y, l.x + 10, l.y);
            gb.drawLine(l.x, l.y - 10, l.x, l.y + 10);
            this.paintMag(gb, l);
            gb.dispose();
            // flip screen
            bufferStrategy.show();

        } catch (final Exception e) {
            e.printStackTrace();
        }
    }

}
