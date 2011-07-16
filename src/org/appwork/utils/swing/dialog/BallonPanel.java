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
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
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

    private final int        shadowSize = 3;

    private int              topInset;
    private int              leftInset;
    private int              bottomInset;
    private int              rightInset;
    private Dimension        contentSize;

    private int              yposition;

    private int              xPosition;

    private Point            desiredLocation;

    private int              rounded;

    private static final int GAP        = 15;

    /**
     * @param component
     * @param dontshowagain
     * @param timerLbl
     * @param desiredLocation
     * @param bounds
     * @param constraints
     * @param columns
     * @param rows
     * @throws OffScreenException
     */
    public BallonPanel(final JComponent component, final JLabel timerLbl, final JCheckBox dontshowagain, final Point desiredLocation) throws OffScreenException {
        super("ins 10,wrap 1", "[grow,fill]", "[][grow,fill][]");

        // this.setDoubleBuffered(false);
        this.setOpaque(false);

        this.add(this.getHeader());

        this.add(component, "pushx,growx");
        this.add(this.getBottom(timerLbl, dontshowagain));

        this.relayout(desiredLocation);
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

        final Double shape = new RoundRectangle2D.Double(this.leftInset, this.topInset, w, h, this.rounded, this.rounded);

        final Area areaOne = new Area(shape);
        final Polygon dart = new Polygon();
        if (this.bottomInset > this.topInset) {
            // unten
            dart.addPoint(-this.xPosition, this.getHeight());
            dart.addPoint(this.leftInset + 25, this.topInset);
            dart.addPoint(this.leftInset + w - 25, this.topInset);

        } else {
            // top
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

        super.paintComponent(g);
    }

    /**
     * @param desiredLocation2
     * @throws OffScreenException
     * 
     */
    public void relayout(final Point desiredLocation) throws OffScreenException {
        this.desiredLocation = desiredLocation;
        this.setLayout(new MigLayout("ins 0,wrap 1", "[grow,fill]", "[][grow,fill][]"));

        final GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
        final GraphicsDevice[] screens = ge.getScreenDevices();
        // store screen device bounds to find current screen later easily

        Rectangle bounds = null;
        main: for (int i = 0; i < ge.getScreenDevices().length; i++) {
            final Rectangle r = screens[i].getDefaultConfiguration().getBounds();
            if (this.desiredLocation.x >= r.x && this.desiredLocation.x <= r.x + r.width) {
                // x correct
                if (this.desiredLocation.y >= r.y && this.desiredLocation.y <= r.y + r.height) {
                    // y correct
                    bounds = r;
                    break main;
                }

            }
        }
        if (bounds == null) { throw new OffScreenException("Point not on screen"); }
        this.contentSize = this.getPreferredSize();
        this.rounded = Math.min(this.contentSize.width, this.contentSize.height) / 2;
        final int x = this.desiredLocation.x - bounds.x;
        final int y = this.desiredLocation.y - bounds.y;

        final boolean bottom = bounds.height - y > bounds.height / 2;

        this.topInset = BallonPanel.GAP;
        this.leftInset = BallonPanel.GAP;
        this.bottomInset = BallonPanel.GAP;
        this.rightInset = BallonPanel.GAP;
        final int half = (this.contentSize.width + (BallonPanel.GAP + this.rounded / 2) * 2) / 2;

        final int dartHeight = this.contentSize.height / 2;
        if (bottom) {
            this.xPosition = -half;
            this.topInset = dartHeight;
            this.yposition = 0;
        } else {
            this.xPosition = -half;
            this.bottomInset = dartHeight;
            this.yposition = -(this.topInset + this.bottomInset + this.rounded + this.contentSize.height);
        }
        final int xp = this.desiredLocation.x - bounds.x + this.xPosition;
        if (xp < 0) {
            this.xPosition -= xp;
        }

        final int rightSpace = bounds.x + bounds.width - (desiredLocation.x + half * 2 + this.xPosition);

        this.xPosition += Math.min(rightSpace, 0);
        final String insets = "ins " + (this.rounded / 2 + this.topInset) + " " + (this.rounded / 2 + this.leftInset) + " " + (this.rounded / 2 + this.bottomInset) + " " + (this.rounded / 2 + this.rightInset) + ",wrap 1";

        this.setLayout(new MigLayout(insets, "[grow,fill]", "[][grow,fill][]"));

    }

}
