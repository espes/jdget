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
import javax.swing.Timer;
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
            if (!circleBar.isDisplayable()) {
                cleanUpIndeterminateValues();
            }

            // if bar is showing, or the bar is part of a renderer
            if (circleBar.isShowing() || circleBar instanceof org.appwork.swing.exttable.columns.ExtCircleProgressColumn.IndeterminatedCircledProgressBar) {
                animatedProgress += animationStepSize;
                animatedProgress %= 2.f;
                circleBar.repaint();
            }

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
            if (!circleBar.isIndeterminate()) {
                circleBar.repaint();
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
                if (circleBar.isIndeterminate()) {
                    initIndeterminate();
                } else {
                    // clean up
                    cleanUpIndeterminateValues();
                }
                circleBar.repaint();
            }

            animationStepSize = 1.0f / (circleBar.getAnimationFPS() / circleBar.getCyclesPerSecond());
        }

    }

    public static ComponentUI createUI(final JComponent x) {
        return new BasicCircleProgressBarUI();
    }

    private float              animationStepSize = 0;

    private static final Color FOREGROUND        = Color.GREEN;

    private javax.swing.Timer  timer;
    private CircledProgressBar circleBar;

    private Handler            handler;
    private float              animatedProgress  = 0.0f;

    /**
     * 
     */
    public void cleanUpIndeterminateValues() {
        final Timer ltimer = timer;
        if (ltimer != null) {
            ltimer.stop();
        }
        timer = null;
        animatedProgress = 0.0f;
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
        final Color fg = circleBar.getForeground();

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

        return circleBar.getValueClipPainter().getPreferredSize();
    }

    /**
     * 
     */
    public void initIndeterminate() {
        if (timer != null) { return; }
        final javax.swing.Timer timer = new javax.swing.Timer(1000 / circleBar.getAnimationFPS(), new AnimationListener());
        timer.setInitialDelay(0);
        timer.setRepeats(true);
        timer.start();
        this.timer = timer;

    }

    /**
     * 
     */
    private void installListeners() {
        handler = new Handler();
        circleBar.addPropertyChangeListener(handler);
        circleBar.getEventSender().addListener(handler);
    }

    @Override
    public void installUI(final JComponent c) {
        circleBar = (CircledProgressBar) c;
        installListeners();
        if (circleBar.isIndeterminate()) {

            BasicCircleProgressBarUI.this.initIndeterminate();
        }
    }

    public void paint(final Graphics g, final double progress, final IconPainter bgi, final IconPainter clipIcon) {

        final Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);

        final Insets b = circleBar.getInsets(); // area for border
        final Dimension size = circleBar.getSize();
        final int diameter = Math.min(size.height - b.top - b.bottom, size.width - b.left - b.right);
        final int midy = b.top + ((size.height - b.top - b.bottom) / 2);
        final int midx = b.left + ((size.width - b.left - b.right) / 2);
        g2.translate(midx, midy);
     

        Area clip = null;
        if (progress == 1.0f) {
            clip = new Area(new Ellipse2D.Float(-diameter, -diameter, diameter * 2, diameter * 2));
        } else if (progress > 0d) {

            clip = new Area(new Arc2D.Float(-diameter, -diameter, diameter * 2, diameter * 2, 90, (float) (-progress * 360), Arc2D.PIE));

        }

        g2.setColor(Color.RED);
        if (clip != null) {
//            clip.intersect(new Area(new Rectangle2D.Float(0, 0, diameter, diameter)));
            // g2.fillRect(-10, -diameter/2, diameter, diameter);
//            g2.fill(clip);
//            return;
        }

        if (bgi != null) {
            final Area a = new Area(new Rectangle2D.Double(-diameter, -diameter, diameter*2, diameter*2));
            if (clip != null) {
                a.subtract(new Area(clip));
            }
            // g2.setClip(a);
            bgi.paint(circleBar, g2, a, diameter, progress);
            
        }
        ;

        // g2.setClip(clip);

        if (clipIcon != null && clip != null) {
            clipIcon.paint(circleBar, g2, clip, diameter, progress);
        }

        g2.translate(-midx, -midy);
        // g2.setColor(Color.BLACK);
        // g2.drawArc(0, 0, diameter, diameter, 0, 360);
    }

    @Override
    public void paint(final Graphics g, final JComponent c) {
        if (circleBar.isIndeterminate()) {
            this.paintIndeterminate(g);
        } else {
            paintDeterminate(g);
        }

    }

    /**
     * @param g
     */
    private void paintDeterminate(final Graphics g) {
        final BoundedRangeModel model = circleBar.getModel();
        final double progress = model.getValue() / (double) model.getMaximum();

        this.paint(g, progress, circleBar.getNonvalueClipPainter(), circleBar.getValueClipPainter());

    }

    /**
     * @param g
     */
    private void paintIndeterminate(final Graphics g) {
        if (animatedProgress > 1.0) {
            this.paint(g, animatedProgress - 1.0, circleBar.getValueClipPainter(), circleBar.getNonvalueClipPainter());

        } else {
            this.paint(g, animatedProgress, circleBar.getNonvalueClipPainter(), circleBar.getValueClipPainter());

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

        final Insets b = circleBar.getInsets(); // area for border
        final Dimension size = circleBar.getSize();
        final int diameter = Math.min(size.height - b.top - b.bottom, size.width - b.left - b.right);

        g2.translate(b.left, b.top);
        final float pDia = (float) (diameter * progress);
        final Shape clip = new Ellipse2D.Float((diameter - pDia) / 2, (diameter - pDia) / 2, pDia, pDia);
        if (bgi != null) {
            final Area a = new Area(new Rectangle2D.Double(0, 0, diameter, diameter));
            a.subtract(new Area(clip));
            g2.setClip(a);
            bgi.paint(circleBar, g2, null, diameter, progress);
        }

        // Create the Polygon for the "upper" Icon

        g2.setClip(clip);

        if (clipIcon != null) {
            clipIcon.paint(circleBar, g2, null, diameter, progress);
        }

        g2.translate(-b.left, -b.top);
        // g2.setColor(Color.BLACK);
        // g2.drawArc(0, 0, diameter, diameter, 0, 360);
    }

    /**
     * 
     */
    private void uninstallListeners() {
        circleBar.removePropertyChangeListener(handler);
        circleBar.getEventSender().removeListener(handler);
    }

    @Override
    public void uninstallUI(final JComponent c) {
        circleBar = null;
        uninstallListeners();

    }

    @Override
    public void update(final Graphics g, final JComponent c) {
        // TODO Auto-generated method stub
        super.update(g, c);
    }

}
