/**
 * Copyright (c) 2009 - 2011 AppWork UG(haftungsbeschränkt) <e-mail@appwork.org>
 * 
 * This file is part of org.appwork.swing.components.tooltips
 * 
 * This software is licensed under the Artistic License 2.0,
 * see the LICENSE file or http://www.opensource.org/licenses/artistic-license-2.0.php
 * for details
 */
package org.appwork.swing.components.tooltips;

import java.awt.Dimension;
import java.awt.MouseInfo;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

import javax.swing.JComponent;
import javax.swing.SwingUtilities;
import javax.swing.ToolTipManager;

import org.appwork.scheduler.DelayedRunnable;
import org.appwork.utils.swing.EDTHelper;

/**
 * @author thomas
 * 
 */
public class ToolTipController implements MouseListener, MouseMotionListener {

    private static final ScheduledExecutorService EXECUTER = Executors.newSingleThreadScheduledExecutor();
    // order is important. EXECUTER has to be available
    private static final ToolTipController        INSTANCE = new ToolTipController();

    /**
     * get the only existing instance of ToolTipManager. This is a singleton
     * 
     * @return
     */
    public static ToolTipController getInstance() {
        return ToolTipController.INSTANCE;
    }

    private DelayedRunnable delayer;

    private ToolTipHandler  activeComponent;

    private ExtTooltip      activeToolTip;

    private Point           mousePosition;
    private long            lastHidden  = 0;

    /*
     * (non-Javadoc)
     * 
     * @see java.awt.event.MouseListener#mouseEntered(java.awt.event.MouseEvent)
     */
    private int             changeDelay = 500;

    /**
     * Create a new instance of ToolTipManager. This is a singleton class.
     * Access the only existing instance by using {@link #getInstance()}.
     */
    private ToolTipController() {
        this.setDelay(1000);
    }

    public int getChangeDelay() {
        return this.changeDelay;
    }

    /**
     * 
     */
    private synchronized void hideTooltip() {
        if (this.activeToolTip != null) {
            System.out.println("hide");
            this.activeToolTip.dispose();
            this.activeToolTip.removeMouseListener(this);
            this.activeToolTip = null;
            this.lastHidden = System.currentTimeMillis();
            this.activeComponent = null;
        }
    }

    /**
     * @return
     */
    private boolean isTooltipVisible() {
        return this.activeToolTip != null;
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.awt.event.MouseListener#mouseClicked(java.awt.event.MouseEvent)
     */
    @Override
    public void mouseClicked(final MouseEvent e) {
        this.hideTooltip();

    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * java.awt.event.MouseMotionListener#mouseDragged(java.awt.event.MouseEvent
     * )
     */
    @Override
    public void mouseDragged(final MouseEvent e) {
        // TODO Auto-generated method stub

    }

    @Override
    public void mouseEntered(final MouseEvent e) {
        if (e.getSource() instanceof ToolTipHandler) {
            if (e.getSource() == this.activeComponent) { return; }
            // just to be sure
            if (this.activeComponent instanceof JComponent) {
                ToolTipManager.sharedInstance().unregisterComponent((JComponent) this.activeComponent);
            }
            this.hideTooltip();
            this.activeComponent = (ToolTipHandler) e.getSource();
            if (System.currentTimeMillis() - this.lastHidden < this.getChangeDelay()) {
                this.mousePosition = e.getLocationOnScreen();
                this.showTooltip();
            }
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.awt.event.MouseListener#mouseExited(java.awt.event.MouseEvent)
     */
    @Override
    public void mouseExited(final MouseEvent e) {
        if (!this.mouseOverComponent(e.getLocationOnScreen()) && !this.mouseOverTooltip(e.getLocationOnScreen())) {
            // do not hide if we exit component and enter tooltip
            this.hideTooltip();
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * java.awt.event.MouseMotionListener#mouseMoved(java.awt.event.MouseEvent)
     */
    @Override
    public void mouseMoved(final MouseEvent e) {
        if (!this.isTooltipVisible() && this.activeComponent != null) {
            this.mousePosition = e.getLocationOnScreen();
            this.delayer.resetAndStart();
        }
    }

    /**
     * @param point
     * @return
     */
    private boolean mouseOverComponent(final Point point) {
        if (this.activeComponent != null && this.activeComponent instanceof JComponent) {

            final Rectangle bounds = ((JComponent) this.activeComponent).getBounds();

            SwingUtilities.convertPointFromScreen(point, ((JComponent) this.activeComponent).getParent());
            return bounds.contains(point);

        }
        return false;
    }

    /**
     * @param locationOnScreen
     * @return
     */
    private boolean mouseOverTooltip(final Point locationOnScreen) {
        if (this.activeToolTip != null) {
            final Dimension d = this.activeToolTip.getSize();
            final Point loc = this.activeToolTip.getLocationOnScreen();
            final Rectangle bounds = new Rectangle(loc.x, loc.y, d.width, d.height);
            return bounds.contains(locationOnScreen);
        }
        return false;
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.awt.event.MouseListener#mousePressed(java.awt.event.MouseEvent)
     */
    @Override
    public void mousePressed(final MouseEvent e) {
        this.hideTooltip();

    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * java.awt.event.MouseListener#mouseReleased(java.awt.event.MouseEvent)
     */
    @Override
    public void mouseReleased(final MouseEvent e) {

        this.hideTooltip();

    }

    /**
     * @param circledProgressBar
     */
    public void register(final ToolTipHandler component) {
        this.unregister(component);
        component.addMouseListener(this);
        component.addMouseMotionListener(this);
        if (component instanceof JComponent) {
            ToolTipManager.sharedInstance().unregisterComponent((JComponent) component);
        }
    }

    public void setChangeDelay(final int changeDelay) {
        this.changeDelay = changeDelay;
    }

    /**
     * @param i
     */
    public synchronized void setDelay(final int delay) {
        if (this.delayer != null) {
            this.delayer.stop();
        }
        this.delayer = new DelayedRunnable(ToolTipController.EXECUTER, delay) {

            @Override
            public void delayedrun() {
                ToolTipController.this.showTooltip();

            }

        };
    }

    /**
     * 
     */
    protected synchronized void showTooltip() {

        new EDTHelper<Object>() {

            @Override
            public Object edtRun() {
                synchronized (ToolTipController.this) {
                    if (ToolTipController.this.activeComponent != null && !ToolTipController.this.isTooltipVisible() && ToolTipController.this.mouseOverComponent(MouseInfo.getPointerInfo().getLocation())) {
                        final ExtTooltip tt = ToolTipController.this.activeComponent.createExtTooltip();
                        tt.addMouseListener(ToolTipController.this);
                        if (tt != null) {
                            tt.show(ToolTipController.this.mousePosition);

                            ToolTipController.this.activeToolTip = tt;
                        }
                    }
                }
                return null;
            }
        }.start();

    }

    /**
     * @param circledProgressBar
     */
    public void unregister(final ToolTipHandler circledProgressBar) {
        circledProgressBar.removeMouseListener(this);
        circledProgressBar.removeMouseMotionListener(this);

    }
}
