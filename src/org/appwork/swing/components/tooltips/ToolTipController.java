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
import java.awt.GraphicsConfiguration;
import java.awt.Insets;
import java.awt.MouseInfo;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.Window;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowFocusListener;
import java.lang.reflect.Method;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

import javax.swing.JComponent;
import javax.swing.Popup;
import javax.swing.PopupFactory;
import javax.swing.SwingUtilities;
import javax.swing.ToolTipManager;

import org.appwork.scheduler.DelayedRunnable;
import org.appwork.utils.logging.Log;
import org.appwork.utils.swing.EDTRunner;

/**
 * @author thomas
 * 
 */
public class ToolTipController implements MouseListener, MouseMotionListener, WindowFocusListener {

    private static final ScheduledExecutorService EXECUTER            = Executors.newSingleThreadScheduledExecutor();
    // order is important. EXECUTER has to be available
    private static final ToolTipController        INSTANCE            = new ToolTipController();
    private static final int                      MEDIUM_WEIGHT_POPUP = 1;

    /**
     * get the only existing instance of ToolTipManager. This is a singleton
     * 
     * @return
     */
    public static ToolTipController getInstance() {
        return ToolTipController.INSTANCE;
    }

    private DelayedRunnable delayer;

    private JComponent      activeComponent;

    private Point           mousePosition;
    private long            lastHidden  = 0;

    /*
     * (non-Javadoc)
     * 
     * @see java.awt.event.MouseListener#mouseEntered(java.awt.event.MouseEvent)
     */
    private int             changeDelay = 500;
    private Popup           activePopup;
    private ExtTooltip      activeToolTipPanel;

    // private Window parentWindow;

    /**
     * Create a new instance of ToolTipManager. This is a singleton class.
     * Access the only existing instance by using {@link #getInstance()}.
     */
    private ToolTipController() {
        this.setDelay(2500);
    }

    public int getChangeDelay() {
        return this.changeDelay;
    }

    public boolean isTooltipActive() {
        return activeToolTipPanel != null;
    }

    /**
     * 
     */
    public void hideTooltip() {
        if (this.activePopup != null) {
            activeToolTipPanel = null;
            this.activePopup.hide();
            // this.activePopup.removeMouseListener(this);
            this.activePopup = null;
            this.lastHidden = System.currentTimeMillis();
            if (this.activeComponent != null) {
                final Window ownerWindow = SwingUtilities.getWindowAncestor(this.activeComponent);
                if (ownerWindow != null) {
                    ownerWindow.removeWindowFocusListener(this);
                }
                if (((ToolTipHandler) this.activeComponent).isTooltipDisabledUntilNextRefocus()) {
                    this.activeComponent = null;
                }
            }

        }
    }

    /**
     * @return
     */
    private boolean isTooltipVisible() {

        return this.activePopup != null;
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
        this.hideTooltip();

    }

    @Override
    public void mouseEntered(final MouseEvent e) {
        if (e.getSource() instanceof ToolTipHandler) {
            if (e.getSource() == this.activeComponent) { return; }
            // just to be sure
            if (this.activeComponent instanceof JComponent) {
                ToolTipManager.sharedInstance().unregisterComponent(this.activeComponent);
            }

            this.activeComponent = (JComponent) e.getSource();
            if (System.currentTimeMillis() - this.lastHidden < this.getChangeDelay()) {
                this.mousePosition = e.getLocationOnScreen();
                this.showTooltip();
                return;
            }
            this.hideTooltip();
        } else {
            if (!this.mouseOverComponent(e.getLocationOnScreen()) && !this.mouseOverTooltip(e.getLocationOnScreen())) {

                this.hideTooltip();
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
            if (System.currentTimeMillis() - this.lastHidden < this.getChangeDelay()) {
                this.showTooltip();
            } else {
                this.delayer.resetAndStart();
            }
        } else if (this.activeComponent != null) {

            if (((ToolTipHandler) this.activeComponent).updateTooltip(this.activeToolTipPanel, e)) {
                this.mousePosition = e.getLocationOnScreen();
                this.showTooltip();

            }
        }
    }

    /**
     * @param point
     * @return
     */
    private boolean mouseOverComponent(final Point point) {
        try {
            if (point != null && this.activeComponent != null && this.activeComponent instanceof JComponent && this.activeComponent.isShowing() && this.activeComponent.getParent() != null) {

                final Rectangle bounds = this.activeComponent.getBounds();

                SwingUtilities.convertPointFromScreen(point, this.activeComponent.getParent());
                return bounds.contains(point);

            }
        } catch (final Exception e) {
            Log.exception(e);
        }
        return false;
    }

    /**
     * @param locationOnScreen
     * @return
     */
    private boolean mouseOverTooltip(final Point locationOnScreen) {
        try {
            if (locationOnScreen != null && this.activeToolTipPanel != null && this.activeToolTipPanel.isShowing()) {

                final Dimension d = this.activeToolTipPanel.getSize();
                final Point loc = this.activeToolTipPanel.getLocationOnScreen();
                final Rectangle bounds = new Rectangle(loc.x, loc.y, d.width, d.height);
                return bounds.contains(locationOnScreen);
            }
        } catch (final Exception e) {
            Log.exception(e);
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
                new EDTRunner() {

                    @Override
                    protected void runInEDT() {
                        ToolTipController.this.showTooltip();

                    }
                };

            }

        };
    }

    /**
     * @param createExtTooltip
     */
    private void show(final ExtTooltip tt) {
       
        this.hideTooltip();
//        delayer.stop();
        if (tt != null) {
            final PopupFactory popupFactory = PopupFactory.getSharedInstance();
            final GraphicsConfiguration gc = this.activeComponent.getGraphicsConfiguration();
            final Rectangle screenBounds = gc.getBounds();
            final Insets screenInsets = Toolkit.getDefaultToolkit().getScreenInsets(gc);
            final Point ttPosition = new Point(this.mousePosition.x,mousePosition.y);

            // if screen has insets, we have to deacrease the available space
            screenBounds.x += screenInsets.left;
            screenBounds.y += screenInsets.top;
            screenBounds.width -= screenInsets.left + screenInsets.right;
            screenBounds.height -= screenInsets.top + screenInsets.bottom;

            if (ttPosition.x > screenBounds.x + screenBounds.width / 2) {
                // move to left on right screen size
                ttPosition.x = this.mousePosition.x - tt.getPreferredSize().width;

            } else {
                ttPosition.x = this.mousePosition.x + 15;
            }

            if (ttPosition.y > screenBounds.y + screenBounds.height / 2) {
                // move tt to top if we are in bottom part of screen
                ttPosition.y = this.mousePosition.y - tt.getPreferredSize().height;
            } else {
                ttPosition.y = this.mousePosition.y + 15;
            }

            // wtf...fu%&inaccessable methods!
            try {
                final Method method = javax.swing.PopupFactory.class.getDeclaredMethod("setPopupType", new Class[] { Integer.TYPE });
                method.setAccessible(true);
                method.invoke(this, new Object[] { ToolTipController.MEDIUM_WEIGHT_POPUP });
            } catch (final Exception exception) {
                new RuntimeException(exception);
            }

            ToolTipController.this.activeToolTipPanel = tt;
            tt.addMouseListener(ToolTipController.this);
            this.activePopup = popupFactory.getPopup(this.activeComponent, this.activeToolTipPanel, ttPosition.x, ttPosition.y);
            final Window ownerWindow = SwingUtilities.getWindowAncestor(this.activeComponent);
            // if the components window is not the active any more, for exmaple
            // because we opened a dialog, don't show tooltip

            if (ownerWindow != null) {
                ownerWindow.removeWindowFocusListener(this);
                ownerWindow.addWindowFocusListener(this);
            }

            this.activePopup.show();

            // parentWindow =
            // SwingUtilities.windowForComponent(this.activeToolTipPanel);
            //
            // parentWindow.addMouseListener(ToolTipController.this);

        }
    }

    protected ExtTooltip getActiveToolTipPanel() {
        return activeToolTipPanel;
    }

    /**
     * 
     */
    protected void showTooltip() {

        ToolTipController.this.hideTooltip();
        final JComponent aC = ToolTipController.this.activeComponent;
        if (aC != null && (!aC.isFocusable() || aC.hasFocus() || ((ToolTipHandler) aC).isTooltipWithoutFocusEnabled()) && !ToolTipController.this.isTooltipVisible() && ToolTipController.this.mouseOverComponent(MouseInfo.getPointerInfo().getLocation())) {

            final Window ownerWindow = SwingUtilities.getWindowAncestor(aC);
            // if the components window is not the active any more, for exmaple
            // because we opened a dialog, don't show tooltip

            if (ownerWindow.isActive()) {
                final Point p = new Point(ToolTipController.this.mousePosition);
                SwingUtilities.convertPointFromScreen(p, aC);
                this.show(((ToolTipHandler) aC).createExtTooltip(p));
            }

        }

    }

    /**
     * @param circledProgressBar
     */
    public void unregister(final ToolTipHandler circledProgressBar) {
        if(activeComponent==circledProgressBar){
            delayer.stop();
            activeComponent=null;
        }
        circledProgressBar.removeMouseListener(this);
        circledProgressBar.removeMouseMotionListener(this);

    }

    /*
     * (non-Javadoc)
     * 
     * @see java.awt.event.WindowFocusListener#windowGainedFocus(java.awt.event.
     * WindowEvent)
     */
    @Override
    public void windowGainedFocus(final WindowEvent e) {
        // TODO Auto-generated method stub

    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * java.awt.event.WindowFocusListener#windowLostFocus(java.awt.event.WindowEvent
     * )
     */
    @Override
    public void windowLostFocus(final WindowEvent e) {
        this.hideTooltip();

    }

    /**
     * @param iconedProcessIndicator
     */
    public void show(ToolTipHandler handler) {
        this.activeComponent = (JComponent) handler;

        this.mousePosition = MouseInfo.getPointerInfo().getLocation();
        this.showTooltip();

    }
}
