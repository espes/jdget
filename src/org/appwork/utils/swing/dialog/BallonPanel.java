/**
 * Copyright (c) 2009 - 2011 AppWork UG(haftungsbeschränkt) <e-mail@appwork.org>
 * 
 * This file is part of org.appwork.utils.swing.dialog
 * 
 * This software is licensed under the Artistic License 2.0,
 * see the LICENSE file or http://www.opensource.org/licenses/artistic-license-2.0.php
 * for details
 */
package org.appwork.utils.swing.dialog;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Polygon;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.geom.Area;
import java.awt.geom.RoundRectangle2D;
import java.awt.geom.RoundRectangle2D.Double;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JLabel;

import net.miginfocom.swing.MigLayout;

import org.appwork.app.gui.MigPanel;

/**
 * @author thomas
 * 
 */
public class BallonPanel extends MigPanel {

    private final int       shadowSize = 3;

    private int             topInset;
    private final int       leftInset;
    private int             bottomInset;
    private final int       rightInset;
    private final Dimension contentSize;

    private int             yposition;

    private int             xPosition;

    /**
     * @param component
     * @param dontshowagain
     * @param timerLbl
     * @param desiredLocation
     * @param bounds
     * @param constraints
     * @param columns
     * @param rows
     */
    public BallonPanel(final JComponent component, final JLabel timerLbl, final JCheckBox dontshowagain, final Rectangle bounds, final Point desiredLocation) {
        super("ins 10,wrap 1,debug", "[grow,fill]", "[][grow,fill][]");

        // this.setDoubleBuffered(false);
        this.setOpaque(false);

        this.add(this.getHeader());

        this.add(component, "pushx,growx");
        this.add(this.getBottom(timerLbl, dontshowagain));

        this.contentSize = this.getPreferredSize();
        System.out.println("Desired Contentsite: " + this.contentSize);

        final int x = desiredLocation.x - bounds.x;
        final int y = desiredLocation.y - bounds.y;

        final boolean right = bounds.width - x > bounds.width / 2;
        final boolean bottom = bounds.height - y > bounds.height / 2;

        this.topInset = 0;
        this.leftInset = 0;
        this.bottomInset = 0;
        this.rightInset = 0;
        this.xPosition = -this.contentSize.width / 2;
        if (!right) {
            this.xPosition -= this.contentSize.width / 6;
        } else {
            this.xPosition += this.contentSize.width / 6;
        }
        if (bottom) {
            this.topInset = this.contentSize.height / 6;
            this.yposition = 0;
        } else {
            this.bottomInset = this.contentSize.height / 6;
            this.yposition = -this.contentSize.height - this.bottomInset;
        }

        final int xp = desiredLocation.x - bounds.x + this.xPosition;
        if (xp < 0) {
            this.xPosition -= xp;
        }
        final int rightSpace = bounds.width - xp + this.xPosition;
        if (rightSpace < this.contentSize.width / 2) {
            this.xPosition -= this.contentSize.width / 2 - rightSpace;
        }
        // final int bottomSpace = bounds.y + bounds.height - (desiredLocation.y
        // + this.contentSize.height + this.topInset + this.bottomInset);

        // this.xPosition -= Math.min(spaceleft, 0);
        // this.xPosition -= Math.max(spaceRight, 0);
        System.out.println("ins " + this.topInset + " " + this.leftInset + " " + this.bottomInset + " " + this.rightInset);
        this.setLayout(new MigLayout("ins " + (10 + this.topInset) + " " + (10 + this.leftInset) + " " + (10 + this.bottomInset) + " " + (10 + this.rightInset) + ",wrap 1", "[grow,fill]", "[][grow,fill][]"));

    }

    /**
     * @param timerLbl
     * @param dontshowagain
     * @return
     */
    private Component getBottom(final JLabel timerLbl, final JCheckBox dontshowagain) {

        final MigPanel p = new MigPanel("ins 0", "[][]", "[]");
        p.setOpaque(false);
        p.add(timerLbl, "hidemode 3");
        if (dontshowagain != null) {
            p.add(dontshowagain, "hidemode 3");
        }
        return p;
    }

    /**
     * @return
     */
    private MigPanel getHeader() {
        final MigPanel p = new MigPanel("ins 0", "[grow,fill][]", "[]");
        p.setOpaque(false);
        p.add(new JLabel("Title"));
        p.add(new JButton("X"), "height 16!,width 16!");
        return p;
    }

    /**
     * @return
     */
    public int getXOffset() {
        // TODO Auto-generated method stub
        return this.xPosition;
    }

    /**
     * @return
     */
    public int getYOffset() {
        // TODO Auto-generated method stub
        return this.yposition;
    }

    @Override
    protected void paintComponent(final Graphics g) {

        final Graphics2D g2 = (Graphics2D) g;

        final int w = this.getWidth() - this.leftInset - this.shadowSize - this.rightInset;
        final int h = this.getHeight() - this.topInset - this.shadowSize - this.bottomInset;
        System.out.println(new Rectangle(this.leftInset, this.topInset, w, h));
        final Double shape = new RoundRectangle2D.Double(this.leftInset, this.topInset, w, h, 25, 25);
        final Area areaOne = new Area(shape);
        final Polygon dart = new Polygon();
        if (this.bottomInset > this.topInset) {
            dart.addPoint(-this.xPosition, this.getHeight() - this.shadowSize * 2);
            dart.addPoint(this.leftInset + 25, this.topInset);
            dart.addPoint(this.leftInset + w - 25, this.topInset);

        } else {
            dart.addPoint(-this.xPosition, -this.yposition);
            dart.addPoint(this.leftInset + 25, this.topInset + h);
            dart.addPoint(this.leftInset + w - 25, this.topInset + h);

        }
        areaOne.add(new Area(dart));

        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.translate(this.shadowSize, this.shadowSize);
        g2.setColor(new Color(0, 0, 0, 50));
        g2.fill(areaOne);
        g2.translate(-this.shadowSize, -this.shadowSize);

        g2.fill(areaOne);
        g2.setColor(Color.WHITE);
        g2.fill(areaOne);
        g2.setColor(Color.DARK_GRAY);

        g2.setStroke(new BasicStroke(1));
        // g2.translate(10, 10);
        g2.draw(areaOne);
        // g2.translate(0, dartHeight);

        System.out.println("TOTAL " + this.getPreferredSize() + "  - " + this.getSize());
        super.paintComponent(g);
    }

}
