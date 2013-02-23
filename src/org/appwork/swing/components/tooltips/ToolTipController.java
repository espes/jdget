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

import java.awt.AWTEvent;
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
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.lang.reflect.Method;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

import javax.swing.JComponent;
import javax.swing.JTable;
import javax.swing.Popup;
import javax.swing.PopupFactory;
import javax.swing.SwingUtilities;
import javax.swing.ToolTipManager;

import org.appwork.swing.event.AWTEventListener;
import org.appwork.swing.event.AWTEventQueueLinker;
import org.appwork.utils.logging.Log;
import org.appwork.utils.swing.EDTRunner;

/**
 * @author thomas
 * 
 */
public class ToolTipController implements MouseListener, MouseMotionListener, WindowFocusListener, PropertyChangeListener, AWTEventListener {

    public static final ScheduledExecutorService EXECUTER            = Executors.newSingleThreadScheduledExecutor();
    // order is important. EXECUTER has to be available
    private static final ToolTipController       INSTANCE            = new ToolTipController();
    private static final int                     MEDIUM_WEIGHT_POPUP = 1;

    /**
     * get the only existing instance of ToolTipManager. This is a singleton
     * 
     * @return
     */
    public static ToolTipController getInstance() {
        return ToolTipController.INSTANCE;
    }

    private ToolTipDelayer   delayer;

    private JComponent       activeComponent;

    private Point            mousePosition;
    private long             lastHidden  = 0;

    /*
     * (non-Javadoc)
     * 
     * @see java.awt.event.MouseListener#mouseEntered(java.awt.event.MouseEvent)
     */
    private int              changeDelay = 500;
    private Popup            activePopup;
    private ExtTooltip       activeToolTipPanel;
    private ToolTipPainter   handler;
    protected ToolTipDelayer defaultDelayer;

    // private Window parentWindow;

    /**
     * Create a new instance of ToolTipManager. This is a singleton class.
     * Access the only existing instance by using {@link #getInstance()}.
     */
    private ToolTipController() {
        this.setDelay(2500);
        AWTEventQueueLinker.link();

        AWTEventQueueLinker.getInstance().getEventSender().addListener(this);

    }

    protected ExtTooltip getActiveToolTipPanel() {
        return this.activeToolTipPanel;
    }

    public int getChangeDelay() {
        return this.changeDelay;
    }

    public ToolTipPainter getHandler() {
        return this.handler;
    }

    /**
     * 
     */
    public void hideTooltip() {
        this.delayer.stop();
        if (this.handler != null) {
            this.handler.hideTooltip();
        }
        if (this.activePopup != null) {
            this.activeToolTipPanel.onHide();
            if (activeToolTipPanel.isLastHiddenEnabled()) {

                this.lastHidden = System.currentTimeMillis();
            }
            this.activeToolTipPanel = null;
            this.activePopup.hide();
            // this.activePopup.removeMouseListener(this);
            this.activePopup = null;

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

    public boolean isTooltipActive() {
        return this.activeToolTipPanel != null;
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
     * @see
     * org.appwork.swing.event.AWTEventListener#onAWTEventAfterDispatch(java
     * .awt.AWTEvent)
     */
    @Override
    public void onAWTEventAfterDispatch(AWTEvent parameter) {
        if (parameter instanceof MouseEvent) {
            switch (parameter.getID()) {
            case MouseEvent.MOUSE_PRESSED:
                this.hideTooltip();
                // reset last Hidden. if we clicked to remove a tooltip it
                // should
                // not
                // popup again immediatly after
                this.lastHidden = 0;
                break;
            }

        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.appwork.swing.event.AWTEventListener#onAWTEventBeforeDispatch(java
     * .awt.AWTEvent)
     */
    @Override
    public void onAWTEventBeforeDispatch(AWTEvent parameter) {

    }

    /*
     * (non-Javadoc)
     * 
     * @see java.awt.event.MouseListener#mouseClicked(java.awt.event.MouseEvent)
     */
    @Override
    public void mouseClicked(final MouseEvent e) {

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
        this.mouseMoved(e);

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

        JComponent ac = activeComponent;
        if (!this.isTooltipVisible() && ac != null) {
            this.mousePosition = e.getLocationOnScreen();
            if (System.currentTimeMillis() - this.lastHidden < this.getChangeDelay()) {

                this.showTooltip();
            } else {
                restartDelayer(ac,mousePosition);

            }
        } else if (ac != null) {

            if (((ToolTipHandler) ac).updateTooltip(this.activeToolTipPanel, e)) {
                if (activeToolTipPanel == null || activeToolTipPanel.isLastHiddenEnabled()) {
                    this.mousePosition = e.getLocationOnScreen();
                    this.showTooltip();
                } else {
                    hideTooltip();
                }

            }
        }
    }

    /**
     * @param ac
     * @param mousePosition2 
     */
    private void restartDelayer(JComponent ac, Point mousePosition2) {

        int newDelayer = ((ToolTipHandler) ac).getTooltipDelay(new Point(mousePosition2.x,mousePosition2.y));
        if (newDelayer > 0 && newDelayer != delayer.getDelay()) {
            delayer.stop();
            delayer = new ToolTipDelayer(newDelayer);
        } else if (defaultDelayer != delayer && newDelayer <= 0) {
            delayer.stop();
            delayer = defaultDelayer;
        }
        this.delayer.resetAndStart();

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
     * @see java.beans.PropertyChangeListener#propertyChange(java.beans.
     * PropertyChangeEvent)
     */
    @Override
    public void propertyChange(final PropertyChangeEvent evt) {
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
            final JComponent jC = (JComponent) component;
            ToolTipManager.sharedInstance().unregisterComponent(jC);
            jC.addPropertyChangeListener("dropLocation", this);
        }
    }

    public void setChangeDelay(final int changeDelay) {
        this.changeDelay = changeDelay;
    }

    /**
     * @param i
     */
    public synchronized void setDelay(final int delay) {
        new EDTRunner() {

            @Override
            protected void runInEDT() {
                if (delayer != null) {
                    delayer.stop();
                }
                delayer = new ToolTipDelayer(delay);
                defaultDelayer = delayer;
            }
        }.waitForEDT();

    }

    public void setHandler(final ToolTipPainter handler) {
        this.handler = handler;
    }

    private boolean classicToolstipsEnabled = true;

    public boolean isClassicToolstipsEnabled() {
        return classicToolstipsEnabled;
    }

    public void setClassicToolstipsEnabled(boolean classicToolstipsEnabled) {
        this.classicToolstipsEnabled = classicToolstipsEnabled;
    }

    /**
     * @param createExtTooltip
     */
    public void show(final ExtTooltip tt) {

        this.hideTooltip();
        // delayer.stop();
        if (tt != null) {
            if (this.handler != null) {
                if (this.handler.showToolTip(tt)) { return; }

            }
            if (isClassicToolstipsEnabled()) {
                final PopupFactory popupFactory = PopupFactory.getSharedInstance();
                final GraphicsConfiguration gc = this.activeComponent.getGraphicsConfiguration();
                final Rectangle screenBounds = gc.getBounds();
                final Insets screenInsets = Toolkit.getDefaultToolkit().getScreenInsets(gc);
                final Point ttPosition = new Point(this.mousePosition.x, this.mousePosition.y);

                // if screen has insets, we have to deacrease the available
                // space
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
                    method.invoke(popupFactory, new Object[] { ToolTipController.MEDIUM_WEIGHT_POPUP });
                } catch (final Exception exception) {
                    throw new RuntimeException(exception);
                }

                ToolTipController.this.activeToolTipPanel = tt;
                tt.addMouseListener(ToolTipController.this);
                this.activePopup = popupFactory.getPopup(this.activeComponent, this.activeToolTipPanel, ttPosition.x, ttPosition.y);

                final Window ownerWindow = SwingUtilities.getWindowAncestor(this.activeComponent);
                // if the components window is not the active any more, for
                // exmaple
                // because we opened a dialog, don't show tooltip

                if (ownerWindow != null) {
                    ownerWindow.removeWindowFocusListener(this);
                    ownerWindow.addWindowFocusListener(this);
                }
                tt.onShow();
                this.activePopup.show();
            }
            // parentWindow =
            // SwingUtilities.windowForComponent(this.activeToolTipPanel);
            //
            // parentWindow.addMouseListener(ToolTipController.this);

        }
    }

    /**
     * @param iconedProcessIndicator
     */
    public void show(final ToolTipHandler handler) {
        this.activeComponent = (JComponent) handler;

        this.mousePosition = MouseInfo.getPointerInfo().getLocation();
        this.showTooltip();

    }

    /**
     * 
     */
    protected void showTooltip() {
        ToolTipController.this.hideTooltip();
        final JComponent aC = ToolTipController.this.activeComponent;
        if (aC != null && (!aC.isFocusable() || aC.hasFocus() || ((ToolTipHandler) aC).isTooltipWithoutFocusEnabled()) && !ToolTipController.this.isTooltipVisible() && ToolTipController.this.mouseOverComponent(MouseInfo.getPointerInfo().getLocation())) {
            if (aC instanceof JTable && ((JTable) aC).getDropLocation() != null) {
                System.out.println("drop is going on");
                return;
            }
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
        if (this.activeComponent == circledProgressBar) {
            this.delayer.stop();
            this.activeComponent = null;
        }
        circledProgressBar.removeMouseListener(this);
        circledProgressBar.removeMouseMotionListener(this);
        if (circledProgressBar instanceof JComponent) {
            final JComponent jC = (JComponent) circledProgressBar;
            jC.removePropertyChangeListener("dropLocation", this);
        }
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

    /*
     * (non-Javadoc)
     * 
     * @see java.awt.event.MouseListener#mousePressed(java.awt.event.MouseEvent)
     */
    @Override
    public void mousePressed(MouseEvent e) {
        // TODO Auto-generated method stub

    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * java.awt.event.MouseListener#mouseReleased(java.awt.event.MouseEvent)
     */
    @Override
    public void mouseReleased(MouseEvent e) {
        // TODO Auto-generated method stub

    }

}
