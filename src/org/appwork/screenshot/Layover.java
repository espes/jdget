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

import java.awt.AlphaComposite;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Composite;
import java.awt.Cursor;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.Image;
import java.awt.MouseInfo;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.image.BufferStrategy;
import java.awt.image.BufferedImage;
import java.awt.image.MemoryImageSource;

import javax.swing.JWindow;
import javax.swing.Timer;

/**
 * @author thomas
 * 
 */
public class Layover extends JWindow implements ActionListener, MouseListener {
    private static final long   serialVersionUID = 3184465232251321247L;
    private static final int    SIZE             = 150;
    private static final double FACTOR           = 4.0;
    private static final int    SCALED_SIZE      = (int) (Layover.SIZE / Layover.FACTOR);
    private BufferedImage       image;

    private int                 lastX;
    private int                 lastY;

    private boolean             isDragging       = false;
    private Point               dragStart;
    private Point               dragEnd;
    // private VolatileImage volatileImg;
    private final Timer         timer;
    private BufferedImage       grayedImage;
    private final Rectangle[]   bounds;

    public Layover() {
        super();
        this.timer = new Timer(1000 / 250, this);
        this.timer.setRepeats(true);
        this.timer.start();
        this.setVisible(true);
        final int[] pixels = new int[16 * 16];
        final Image image = Toolkit.getDefaultToolkit().createImage(new MemoryImageSource(16, 16, pixels, 0, 16));
        final Cursor transparentCursor = Toolkit.getDefaultToolkit().createCustomCursor(image, new Point(0, 0), "invisibleCursor");
        this.setCursor(transparentCursor);
        this.addMouseListener(this);
        this.createBufferStrategy(2);
        this.setAlwaysOnTop(true);
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
            final Color color = Color.BLACK;
            gb.drawLine(l.x - 10, l.y, l.x + 10, l.y);
            gb.drawLine(l.x, l.y - 10, l.x, l.y + 10);
            this.paintMag(gb, l);
            gb.dispose();
            bufferStrategy.show();
        } catch (final Exception e) {

        }
    }

}
