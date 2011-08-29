/**
 * Copyright (c) 2009 - 2011 AppWork UG(haftungsbeschränkt) <e-mail@appwork.org>
 * 
 * This file is part of org.appwork.swing.components.circlebar
 * 
 * This software is licensed under the Artistic License 2.0,
 * see the LICENSE file or http://www.opensource.org/licenses/artistic-license-2.0.php
 * for details
 */
package org.appwork.swing.components.circlebar;

import java.awt.AlphaComposite;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Composite;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.geom.Area;
import java.awt.geom.Ellipse2D;

/**
 * @author thomas
 * 
 */
public class ImagePainter implements IconPainter {

    private final Image          image;
    private final AlphaComposite composite;
    private int                  xOffset = 0;
    private int                  yOffset = 0;

    private Color                foreground;

    private Color                background;

    /**
     * @param image
     */
    public ImagePainter(final Image image) {
        this(image, 1.0f);
    }

    /**
     * @param image2
     * @param instance
     */
    public ImagePainter(final Image image2, final AlphaComposite instance) {
        this.image = image2;
        this.composite = instance;
    }

    /**
     * @param image2
     * @param f
     */
    public ImagePainter(final Image image2, final float alpha) {
        this(image2, AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha));
    }

    public Color getBackground() {
        return this.background;
    }

    public AlphaComposite getComposite() {
        return this.composite;
    }

    public Color getForeground() {
        return this.foreground;
    }

    public Image getImage() {
        return this.image;
    }

    public int getxOffset() {
        return this.xOffset;
    }

    public int getyOffset() {
        return this.yOffset;
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.appwork.swing.components.circlebar.IconPainter#paint(org.appwork.
     * swing.components.circlebar.CircledProgressBar, java.awt.Graphics2D, int,
     * double)
     */
    @Override
    public void paint(final CircledProgressBar bar, final Graphics2D g2, final Shape shape, final int diameter, final double progress) {
        final Composite comp = g2.getComposite();
        if (this.composite != null) {
            g2.setComposite(this.composite);
        }
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        final Shape circle = new Ellipse2D.Float(0, 0, diameter, diameter);
        final Area a = new Area(circle);
        a.intersect(new Area(shape));
        if (this.getBackground() != null) {
            g2.setColor(this.getBackground());
            g2.fill(a);
        }
        g2.setClip(shape);

        g2.drawImage(this.image, this.xOffset, this.yOffset, bar.getWidth(), bar.getHeight(), 0, 0, this.image.getWidth(null), this.image.getHeight(null), null);
        g2.setClip(null);
        if (this.getForeground() != null) {
            // g2.draw(shape);
            g2.setStroke(new BasicStroke(1, BasicStroke.CAP_ROUND, BasicStroke.CAP_ROUND));
            g2.setColor(this.getForeground());
            g2.draw(a);
        }
        if (this.composite != null) {
            g2.setComposite(comp);
        }
    }

    public void setBackground(final Color background) {
        this.background = background;
    }

    public void setForeground(final Color foreground) {
        this.foreground = foreground;
    }

    public void setxOffset(final int xOffset) {
        this.xOffset = xOffset;
    }

    public void setyOffset(final int yOffset) {
        this.yOffset = yOffset;
    }

}
