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

import java.awt.Color;
import java.awt.Cursor;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.MouseInfo;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.Transparency;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.awt.image.MemoryImageSource;

import javax.swing.JWindow;
import javax.swing.Timer;

/**
 * @author thomas
 * 
 */
public class Layover extends JWindow implements ActionListener {
    private BufferedImage image;
    private final Timer   timer;
    private int           lastX;
    private int           lastY;
    private BufferedImage buffer;

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
            this.repaint();
        }
        this.lastX = l.x;
        this.lastY = l.y;
    }

    @Override
    public void paint(final Graphics g) {
        if (this.buffer != null) {
            final Graphics2D gb = (Graphics2D) this.buffer.getGraphics();
            gb.clearRect(0, 0, this.buffer.getWidth(), this.buffer.getHeight());
            if (this.image != null) {
                gb.drawImage(this.image, 0, 0, null);
                // g.setColor(Color.RED);
                // g.drawRect(0, 0, screenWidth, screenHeight);
            }
            final Point l = MouseInfo.getPointerInfo().getLocation();
            gb.setColor(Color.GRAY);
            gb.drawLine(0, l.y, this.image.getWidth(), l.y);
            gb.drawLine(l.x, 0, l.x, this.image.getHeight());
            g.drawImage(this.buffer, 0, 0, null);
        }
    }

    /**
     * @param complete
     */
    public void setImage(final BufferedImage complete) {
        this.image = complete;
        this.setSize(complete.getWidth(), complete.getHeight());
        this.buffer = new BufferedImage(this.image.getWidth(null), this.image.getHeight(null), Transparency.TRANSLUCENT);

        this.repaint();

        this.setVisible(true);
    }
}
