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
import java.awt.Color;
import java.awt.Composite;
import java.awt.Cursor;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.MouseInfo;
import java.awt.Point;
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
    private static final long serialVersionUID = 3184465232251321247L;
    private BufferedImage     image;

    private int               lastX;
    private int               lastY;

    private boolean           isDragging       = false;
    private Point             dragStart;
    private Point             dragEnd;
    // private VolatileImage volatileImg;
    private final Timer       timer;

    public Layover() {
        super();
        this.timer = new Timer(1000 / 50, this);
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
     * @param complete
     */
    public void setImage(final BufferedImage complete) {
        this.image = complete;
        this.setSize(complete.getWidth(), complete.getHeight());
        setLocation(0,0);
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
            final Graphics2D gb = (Graphics2D) bufferStrategy.getDrawGraphics();
            if (this.image != null) {
                gb.drawImage(this.image, 0, 0, null);
            }

            final Point l = MouseInfo.getPointerInfo().getLocation();
            if (this.dragStart != null) {
                gb.setColor(Color.BLACK);

                final int startX = Math.min(this.dragStart.x, l.x);
                final int startY = Math.min(this.dragStart.y, l.y);
                gb.drawRect(startX, startY, Math.abs(l.x - this.dragStart.x), Math.abs(l.y - this.dragStart.y));
                final Composite comp = gb.getComposite();
                gb.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.3f));
                gb.fillRect(0, 0, startX, this.image.getHeight());
                gb.fillRect(l.x, 0, this.image.getWidth() - l.x, this.image.getHeight());
                gb.fillRect(startX, 0, Math.abs(l.x - this.dragStart.x), startY);
                gb.fillRect(startX, l.y, Math.abs(l.x - this.dragStart.x), this.image.getHeight() - l.y);

                gb.setComposite(comp);
            }
            gb.setColor(Color.GRAY);
            gb.drawLine(0, l.y, this.image.getWidth(), l.y);
            gb.drawLine(l.x, 0, l.x, this.image.getHeight());
            gb.dispose();
            bufferStrategy.show();
        } catch (final Exception e) {

        }
    }
}
