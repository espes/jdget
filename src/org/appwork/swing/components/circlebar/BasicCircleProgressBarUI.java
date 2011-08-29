/**
 * Copyright (c) 2009 - 2011 AppWork UG(haftungsbeschränkt) <e-mail@appwork.org>
 * 
 * This file is part of org.appwork.swing.components
 * 
 * This software is licensed under the Artistic License 2.0,
 * see the LICENSE file or http://www.opensource.org/licenses/artistic-license-2.0.php
 * for details
 */
package org.appwork.swing.components.circlebar;

import java.awt.Color;
import java.awt.Component.BaselineResizeBehavior;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.geom.Arc2D;
import java.awt.geom.Area;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Rectangle2D;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.swing.BoundedRangeModel;
import javax.swing.JComponent;
import javax.swing.plaf.ComponentUI;

import org.appwork.utils.event.predefined.changeevent.ChangeEvent;
import org.appwork.utils.event.predefined.changeevent.ChangeListener;

/**
 * @author thomas
 * 
 */
public class BasicCircleProgressBarUI extends CircleProgressBarUI {
    class AnimationListener implements ActionListener {

        /*
         * (non-Javadoc)
         * 
         * @see
         * java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent
         * )
         */
        @Override
        public void actionPerformed(final ActionEvent e) {

            BasicCircleProgressBarUI.this.animatedProgress += BasicCircleProgressBarUI.this.animationStepSize;
            BasicCircleProgressBarUI.this.animatedProgress %= 2.f;
            BasicCircleProgressBarUI.this.circleBar.repaint();
        }
    }

    class Handler implements PropertyChangeListener, ChangeListener {

        /*
         * (non-Javadoc)
         * 
         * @see org.appwork.utils.event.predefined.changeevent.ChangeListener#
         * onChangeEvent
         * (org.appwork.utils.event.predefined.changeevent.ChangeEvent)
         */
        @Override
        public void onChangeEvent(final ChangeEvent event) {
            if (!BasicCircleProgressBarUI.this.circleBar.isIndeterminate()) {
                BasicCircleProgressBarUI.this.circleBar.repaint();
            }
        }

        /*
         * (non-Javadoc)
         * 
         * @see java.beans.PropertyChangeListener#propertyChange(java.beans.
         * PropertyChangeEvent)
         */
        @Override
        public void propertyChange(final PropertyChangeEvent e) {
            final String prop = e.getPropertyName();
            if ("indeterminate" == prop) {
                if (BasicCircleProgressBarUI.this.circleBar.isIndeterminate()) {
                    BasicCircleProgressBarUI.this.initIndeterminate();
                } else {
                    // clean up
                    BasicCircleProgressBarUI.this.cleanUpIndeterminateValues();
                }
                BasicCircleProgressBarUI.this.circleBar.repaint();
            }

            BasicCircleProgressBarUI.this.animationStepSize = 1.0f / (BasicCircleProgressBarUI.this.circleBar.getAnimationFPS() / BasicCircleProgressBarUI.this.circleBar.getCyclesPerSecond());
        }

    }

    public static ComponentUI createUI(final JComponent x) {
        return new BasicCircleProgressBarUI();
    }

    private float              animationStepSize = 0;

    private static final Color FOREGROUND        = Color.GREEN;

    private static double tan(final double degree) {
        return Math.tan(degree * Math.PI / 180.0);
    }

    private javax.swing.Timer  timer;
    private CircledProgressBar circleBar;

    private Handler            handler;
    private float              animatedProgress = 0.0f;

    /**
     * 
     */
    public void cleanUpIndeterminateValues() {
        this.timer.stop();
        this.timer = null;
    }

    /**
     * @param diameter
     * @param degree
     * @return
     */
    private Shape createClip(final int diameter, final double progress) {
        final Area a = new Area(new Arc2D.Float(-diameter / 2 + 1, -diameter / 2 + 1, diameter * 2, diameter * 2, 90, (float) (-progress * 360), Arc2D.PIE));
        a.intersect(new Area(new Rectangle2D.Float(0, 0, diameter, diameter)));
        return a;
    }

    @Override
    public int getBaseline(final JComponent c, final int width, final int height) {
        return super.getBaseline(c, width, height);
    }

    @Override
    public BaselineResizeBehavior getBaselineResizeBehavior(final JComponent c) {
        // TODO Auto-generated method stub
        return super.getBaselineResizeBehavior(c);
    }

    /**
     * @return
     */
    private Color getForeground() {
        final Color fg = this.circleBar.getForeground();

        return fg == null ? BasicCircleProgressBarUI.FOREGROUND : fg;
    }

    @Override
    public Dimension getMaximumSize(final JComponent c) {
        // TODO Auto-generated method stub
        return super.getMaximumSize(c);
    }

    @Override
    public Dimension getMinimumSize(final JComponent c) {
        // TODO Auto-generated method stub
        return super.getMinimumSize(c);
    }

    @Override
    public Dimension getPreferredSize(final JComponent c) {

        return new Dimension(32, 32);
    }

    /**
     * 
     */
    public void initIndeterminate() {
        if (this.timer != null) { return; }
        this.timer = new javax.swing.Timer(1000 / this.circleBar.getAnimationFPS(), new AnimationListener());
        this.timer.setInitialDelay(0);
        this.timer.setRepeats(true);
        this.timer.start();

    }

    /**
     * 
     */
    private void installListeners() {
        this.handler = new Handler();
        this.circleBar.addPropertyChangeListener(this.handler);
        this.circleBar.getEventSender().addListener(this.handler);
    }

    @Override
    public void installUI(final JComponent c) {
        this.circleBar = (CircledProgressBar) c;
        this.installListeners();
    }

    public void paint(final Graphics g, final double progress, final IconPainter bgi, final IconPainter clipIcon) {

        final Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);

        final Insets b = this.circleBar.getInsets(); // area for border
        final Dimension size = this.circleBar.getSize();
        final int diameter = Math.min(size.height - b.top - b.bottom, size.width - b.left - b.right) - 2;

        g2.translate(b.left + 1, b.top + 1);
        final Shape clip = this.createClip(diameter, progress);
        if (bgi != null) {
            final Area a = new Area(new Rectangle2D.Double(0, 0, diameter, diameter));
            a.subtract(new Area(clip));
            // g2.setClip(a);
            bgi.paint(this.circleBar, g2, a, diameter, progress);
        }

        // Create the Polygon for the "upper" Icon

        // g2.setClip(clip);

        if (clipIcon != null) {
            clipIcon.paint(this.circleBar, g2, clip, diameter, progress);
        }

        g2.translate(-b.left + 1, -b.top + 1);
        // g2.setColor(Color.BLACK);
        // g2.drawArc(0, 0, diameter, diameter, 0, 360);
    }

    @Override
    public void paint(final Graphics g, final JComponent c) {
        if (this.circleBar.isIndeterminate()) {
            this.paintIndeterminate(g);
        } else {
            this.paintDeterminate(g);
        }

    }

    /**
     * @param g
     */
    private void paintDeterminate(final Graphics g) {
        final BoundedRangeModel model = this.circleBar.getModel();
        final double progress = model.getValue() / (double) model.getMaximum();

        this.paint(g, progress, this.circleBar.getNonvalueClipPainter(), this.circleBar.getValueClipPainter());

    }

    /**
     * @param g
     */
    private void paintIndeterminate(final Graphics g) {
        if (this.animatedProgress > 1.0) {
            this.paint(g, this.animatedProgress - 1.0, this.circleBar.getValueClipPainter(), this.circleBar.getNonvalueClipPainter());

        } else {
            this.paint(g, this.animatedProgress, this.circleBar.getNonvalueClipPainter(), this.circleBar.getValueClipPainter());

        }
        // if (this.animatedProgress > 1.0) {
        // this.paintIndeterminate(g, 2.0 - this.animatedProgress,
        // this.circleBar.getNonvalueClipPainter(),
        // this.circleBar.getValueClipPainter());
        //
        // } else {
        // this.paintIndeterminate(g, this.animatedProgress,
        // this.circleBar.getNonvalueClipPainter(),
        // this.circleBar.getValueClipPainter());
        //
        // }

    }

    /**
     * @param g
     * @param d
     * @param valueClipPainter
     * @param nonvalueClipPainter
     */
    private void paintIndeterminate(final Graphics g, final double progress, final IconPainter bgi, final IconPainter clipIcon) {

        final Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);

        final Insets b = this.circleBar.getInsets(); // area for border
        final Dimension size = this.circleBar.getSize();
        final int diameter = Math.min(size.height - b.top - b.bottom, size.width - b.left - b.right);

        g2.translate(b.left, b.top);
        final float pDia = (float) (diameter * progress);
        final Shape clip = new Ellipse2D.Float((diameter - pDia) / 2, (diameter - pDia) / 2, pDia, pDia);
        if (bgi != null) {
            final Area a = new Area(new Rectangle2D.Double(0, 0, diameter, diameter));
            a.subtract(new Area(clip));
            g2.setClip(a);
            bgi.paint(this.circleBar, g2, null, diameter, progress);
        }

        // Create the Polygon for the "upper" Icon

        g2.setClip(clip);

        if (clipIcon != null) {
            clipIcon.paint(this.circleBar, g2, null, diameter, progress);
        }

        g2.translate(-b.left, -b.top);
        // g2.setColor(Color.BLACK);
        // g2.drawArc(0, 0, diameter, diameter, 0, 360);
    }

    /**
     * 
     */
    private void uninstallListeners() {
        this.circleBar.removePropertyChangeListener(this.handler);
        this.circleBar.getEventSender().removeListener(this.handler);
    }

    @Override
    public void uninstallUI(final JComponent c) {
        this.circleBar = null;
        this.uninstallListeners();

    }

    @Override
    public void update(final Graphics g, final JComponent c) {
        // TODO Auto-generated method stub
        super.update(g, c);
    }

}
