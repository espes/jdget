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
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.MouseInfo;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Transparency;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;

import javax.swing.JWindow;
import javax.swing.Timer;

/**
 * @author thomas
 * 
 */
public class Magnifyer extends JWindow implements ActionListener {
    private static final long serialVersionUID = 2543289315175526228L;
    private static final int    SIZE        = 100;
    private static final double FACTOR      = 4.0;
    private static final int    SCALED_SIZE = (int) (Magnifyer.SIZE / Magnifyer.FACTOR);

    private final Timer         timer;

    private final ScreenShot    screenShot;
    private final Rectangle[]   bounds;
    private int                 lastX;
    private int                 lastY;
    private final BufferedImage buffer;

    public Magnifyer(final ScreenShot screenShot) throws AWTException {
        super();
        this.setAlwaysOnTop(true);
        this.screenShot = screenShot;
        this.buffer = new BufferedImage(Magnifyer.SIZE, Magnifyer.SIZE, Transparency.TRANSLUCENT);
        this.timer = new Timer(1000 / 150, this);
        this.timer.setRepeats(true);
        this.timer.start();
        this.setVisible(true);
        this.setSize(Magnifyer.SIZE, Magnifyer.SIZE);
        this.setCursor(Cursor.getPredefinedCursor(Cursor.CROSSHAIR_CURSOR));
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
        try {

            final Point l = MouseInfo.getPointerInfo().getLocation();
            if (this.lastX != l.x || this.lastY != l.y) {
                this.repaint();

                this.lastX = l.x;
                this.lastY = l.y;
                final Rectangle bounds = this.getDeviceBounds(l);
                if (bounds == null) { return; }
                int x = l.x + 20;
                if (x + Magnifyer.SIZE > bounds.x + bounds.width) {
                    x = l.x - Magnifyer.SIZE - 20;
                }
                int y = l.y - Magnifyer.SIZE - 20;
                if (y < bounds.y) {
                    y = l.y + 20;
                }

                this.setLocation(x, y);
                // this.setVisible(true);

                // this.repaint();
            }
        } catch (final Exception e2) {
            // this.setVisible(false);
            e2.printStackTrace();
        }
    }

    /**
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

    // @Override
    // public Dimension getSize() {
    // return new Dimension(Magnifyer.SIZE, Magnifyer.SIZE);
    // }
    //
    @Override
    public void paint(final Graphics g) {
        // this.paintComponents(g)
        // final Graphics2D gd2 = (Graphics2D) g;

        final Point l = MouseInfo.getPointerInfo().getLocation();
        // Rectangle b = getDeviceBounds(l);

        final Graphics2D gb = (Graphics2D) this.buffer.getGraphics();
        gb.clearRect(0, 0, Magnifyer.SIZE, Magnifyer.SIZE);
        gb.drawImage(this.screenShot.getComplete(), 0, 0, Magnifyer.SIZE, Magnifyer.SIZE, l.x - Magnifyer.SCALED_SIZE / 2, l.y - Magnifyer.SCALED_SIZE / 2, l.x + Magnifyer.SCALED_SIZE / 2, l.y + Magnifyer.SCALED_SIZE / 2, Color.BLACK, null);
        gb.setColor(Color.BLUE);
        gb.setStroke(new BasicStroke(1));
        gb.drawRect(0, 0, Magnifyer.SIZE - 1, Magnifyer.SIZE - 1);
        final Composite comp = gb.getComposite();
        gb.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.3f));

        gb.setColor(Color.BLUE);

        gb.setStroke(new BasicStroke(4));
        gb.drawLine(Magnifyer.SIZE / 2 - Magnifyer.SIZE / 2, Magnifyer.SIZE / 2, Magnifyer.SIZE / 2 + Magnifyer.SIZE / 2, Magnifyer.SIZE / 2);
        // gb.setColor(Color.RED);
        gb.drawLine(Magnifyer.SIZE / 2, Magnifyer.SIZE / 2 - Magnifyer.SIZE / 2, Magnifyer.SIZE / 2, Magnifyer.SIZE / 2 - 4);
        gb.drawLine(Magnifyer.SIZE / 2, Magnifyer.SIZE / 2 + 4, Magnifyer.SIZE / 2, Magnifyer.SIZE / 2 + Magnifyer.SIZE / 2);
        gb.setComposite(comp);

        g.drawImage(this.buffer, 0, 0, this);
    }
}
