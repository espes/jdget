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
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.geom.Area;
import java.awt.geom.Ellipse2D;

import javax.swing.Icon;

/**
 * @author thomas
 * 
 */
public class ImagePainter implements IconPainter {

    private final Icon           image;
    private final AlphaComposite composite;

    private Color                foreground;

    private Color                background;
    private Dimension            preferredSize;

    /**
     * @param image
     */
    public ImagePainter(final Icon image) {
        this(image, 1.0f);
    }

    /**
     * @param image2
     * @param instance
     */
    public ImagePainter(final Icon image2, final AlphaComposite instance) {
        image = image2;
        composite = instance;
        preferredSize = new Dimension(image.getIconWidth(), image.getIconHeight());
    }

    /**
     * @param image2
     * @param f
     */
    public ImagePainter(final Icon image2, final float alpha) {
        this(image2, AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha));
    }

    public Color getBackground() {
        return background;
    }

    public AlphaComposite getComposite() {
        return composite;
    }

    public Color getForeground() {
        return foreground;
    }

    public Icon getImage() {
        return image;
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
        if (composite != null) {
            g2.setComposite(composite);
        }
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        Area a = null;

        if (getBackground() != null) {
            if (a == null) {
                final Shape circle = new Ellipse2D.Float(-(diameter - 2) / 2, -(diameter - 2) / 2, diameter - 2, diameter - 2);
                a = new Area(circle);
                a.intersect(new Area(shape));
            }
            g2.setColor(getBackground());
            g2.fill(a);
        }
        g2.setClip(shape);

        // g2.drawImage(image, (diameter - image.getWidth(null)) / 2, (diameter
        // - image.getHeight(null)) / 2, image.getWidth(null),
        // image.getHeight(null), null);
        final Dimension dim = bar.getSize();
        // System.out.println((bar.getWidth() - image.getWidth(null)) /
        // 2+" - "+((bar.getHeight() - image.getHeight(null)) / 2));
        image.paintIcon(bar, g2, -image.getIconWidth() / 2, -image.getIconHeight() / 2);
        g2.setClip(null);
        if (getForeground() != null) {
            if (a == null) {
                final Shape circle = new Ellipse2D.Float(-(diameter - 2) / 2, -(diameter - 2) / 2, diameter - 2, diameter - 2);
                a = new Area(circle);
                a.intersect(new Area(shape));
            }
            // g2.draw(shape);
            g2.setStroke(new BasicStroke(1, BasicStroke.CAP_ROUND, BasicStroke.CAP_ROUND));
            g2.setColor(getForeground());
            g2.draw(a);
        }
        if (composite != null) {
            g2.setComposite(comp);
        }
    }

    public void setBackground(final Color background) {
        this.background = background;
    }

    public void setForeground(final Color foreground) {
        this.foreground = foreground;
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.appwork.swing.components.circlebar.IconPainter#getPreferredSize()
     */
    @Override
    public Dimension getPreferredSize() {
        // TODO Auto-generated method stub
        return preferredSize;
    }

}
