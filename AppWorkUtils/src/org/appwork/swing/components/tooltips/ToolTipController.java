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
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.GraphicsConfiguration;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.Insets;
import java.awt.MouseInfo;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.Window;
import java.awt.event.AWTEventListener;
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
        setDelay(2500);
        Toolkit.getDefaultToolkit().addAWTEventListener(this, AWTEvent.MOUSE_EVENT_MASK);

    }

    protected ExtTooltip getActiveToolTipPanel() {
        return activeToolTipPanel;
    }

    public int getChangeDelay() {
        return changeDelay;
    }

    public ToolTipPainter getHandler() {
        return handler;
    }

    /**
     * 
     */
    public void hideTooltip() {
        delayer.stop();
        if (handler != null) {
            handler.hideTooltip();
        }
        if (activePopup != null) {
            activeToolTipPanel.onHide();
            if (activeToolTipPanel.isLastHiddenEnabled()) {

                lastHidden = System.currentTimeMillis();
            }
            activeToolTipPanel = null;
            activePopup.hide();
            // this.activePopup.removeMouseListener(this);
            activePopup = null;

            if (activeComponent != null) {
                final Window ownerWindow = SwingUtilities.getWindowAncestor(activeComponent);
                if (ownerWindow != null) {
                    ownerWindow.removeWindowFocusListener(this);
                }
                if (((ToolTipHandler) activeComponent).isTooltipDisabledUntilNextRefocus()) {
                    activeComponent = null;
                }
            }

        }
    }

    public boolean isTooltipActive() {
        return activeToolTipPanel != null;
    }

    /**
     * @return
     */
    private boolean isTooltipVisible() {

        return activePopup != null;
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.appwork.swing.event.AWTEventListener#onAWTEventAfterDispatch(java
     * .awt.AWTEvent)
     */
    @Override
    public void eventDispatched(final AWTEvent event) {
        if (event instanceof MouseEvent) {
            switch (event.getID()) {
            case MouseEvent.MOUSE_PRESSED:
                if (event.getSource() instanceof Component) {
                    final Container parent = SwingUtilities.getAncestorOfClass(ExtTooltip.class, (Component) event.getSource());
                    if (parent == activeToolTipPanel && parent != null) {
                        // user clicked in the tooltip!
                        return;
                    }
                }
                hideTooltip();
                // reset last Hidden. if we clicked to remove a tooltip it
                // should
                // not
                // popup again immediatly after
                lastHidden = 0;
                break;
            }

        }
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
        mouseMoved(e);

    }

    @Override
    public void mouseEntered(final MouseEvent e) {

        if (e.getSource() instanceof ToolTipHandler) {
            if (e.getSource() == activeComponent) { return; }
            // just to be sure
            if (activeComponent instanceof JComponent) {
                ToolTipManager.sharedInstance().unregisterComponent(activeComponent);
            }

            activeComponent = (JComponent) e.getSource();
            if (System.currentTimeMillis() - lastHidden < getChangeDelay()) {
                mousePosition = e.getLocationOnScreen();
                showTooltip();
                return;
            }
            hideTooltip();
        } else {
            if (!mouseOverComponent(e.getLocationOnScreen()) && !mouseOverTooltip(e.getLocationOnScreen())) {

                hideTooltip();
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
        if (!mouseOverComponent(e.getLocationOnScreen()) && !mouseOverTooltip(e.getLocationOnScreen())) {
            // do not hide if we exit component and enter tooltip

            hideTooltip();
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

        final JComponent ac = activeComponent;
        if (!isTooltipVisible() && ac != null) {
            mousePosition = e.getLocationOnScreen();
            if (System.currentTimeMillis() - lastHidden < getChangeDelay()) {

                showTooltip();
            } else {
                restartDelayer(ac, mousePosition);

            }
        } else if (ac != null) {

            if (((ToolTipHandler) ac).updateTooltip(activeToolTipPanel, e)) {
                if (activeToolTipPanel == null || activeToolTipPanel.isLastHiddenEnabled()) {
                    mousePosition = e.getLocationOnScreen();
                    showTooltip();
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
    private void restartDelayer(final JComponent ac, final Point mousePosition2) {

        final int newDelayer = ((ToolTipHandler) ac).getTooltipDelay(new Point(mousePosition2.x, mousePosition2.y));
        if (newDelayer > 0 && newDelayer != delayer.getDelay()) {
            delayer.stop();
            delayer = new ToolTipDelayer(newDelayer);
        } else if (defaultDelayer != delayer && newDelayer <= 0) {
            delayer.stop();
            delayer = defaultDelayer;
        }
        delayer.resetAndStart();

    }

    /**
     * @param point
     * @return
     */
    private boolean mouseOverComponent(final Point point) {
        try {
            if (point != null && activeComponent != null && activeComponent instanceof JComponent && activeComponent.isShowing() && activeComponent.getParent() != null) {

                final Rectangle bounds = activeComponent.getBounds();

                SwingUtilities.convertPointFromScreen(point, activeComponent.getParent());
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
            if (locationOnScreen != null && activeToolTipPanel != null && activeToolTipPanel.isShowing()) {

                final Dimension d = activeToolTipPanel.getSize();
                final Point loc = activeToolTipPanel.getLocationOnScreen();
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
        hideTooltip();

    }

    /**
     * @param circledProgressBar
     */
    public void register(final ToolTipHandler component) {
        unregister(component);
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

    public void setClassicToolstipsEnabled(final boolean classicToolstipsEnabled) {
        this.classicToolstipsEnabled = classicToolstipsEnabled;
    }

    /**
     * @param createExtTooltip
     */
    public void show(final ExtTooltip tt) {

        hideTooltip();
        // delayer.stop();
        if (tt != null) {
            if (handler != null) {
                if (handler.showToolTip(tt)) { return; }

            }
            if (isClassicToolstipsEnabled()) {
                final PopupFactory popupFactory = PopupFactory.getSharedInstance();
                GraphicsConfiguration gc = null;

                if (activeComponent != null) {
                    gc = activeComponent.getGraphicsConfiguration();
                } else {

                    for (final GraphicsDevice screen : GraphicsEnvironment.getLocalGraphicsEnvironment().getScreenDevices()) {
                        if (screen.getDefaultConfiguration().getBounds().contains(mousePosition)) {
                            gc = screen.getDefaultConfiguration();
                            break;
                        }
                    }
                }
                if (gc == null) { return; }
                final Rectangle screenBounds = gc.getBounds();
                final Insets screenInsets = Toolkit.getDefaultToolkit().getScreenInsets(gc);
                Point ttPosition = new Point(mousePosition.x, mousePosition.y);

                // if screen has insets, we have to deacrease the available
                // space
                screenBounds.x += screenInsets.left;
                screenBounds.y += screenInsets.top;
                screenBounds.width -= screenInsets.left + screenInsets.right;
                screenBounds.height -= screenInsets.top + screenInsets.bottom;

                if (ttPosition.x > screenBounds.x + screenBounds.width / 2) {
                    // move to left on right screen size
                    ttPosition.x = mousePosition.x - tt.getPreferredSize().width;

                } else {
                    ttPosition.x = mousePosition.x + 15;
                }

                if (ttPosition.y > screenBounds.y + screenBounds.height / 2) {
                    // move tt to top if we are in bottom part of screen
                    ttPosition.y = mousePosition.y - tt.getPreferredSize().height;
                } else {
                    ttPosition.y = mousePosition.y + 15;
                }

                // wtf...fu%&inaccessable methods!
                try {
                    final Method method = javax.swing.PopupFactory.class.getDeclaredMethod("setPopupType", new Class[] { Integer.TYPE });
                    method.setAccessible(true);
                    method.invoke(popupFactory, new Object[] { ToolTipController.MEDIUM_WEIGHT_POPUP });
                } catch (final Exception exception) {
                    throw new RuntimeException(exception);
                }

                screenBounds.x += screenInsets.left;
                screenBounds.y += screenInsets.top;
                screenBounds.width -= screenInsets.left + screenInsets.right;
                screenBounds.height -= screenInsets.top + screenInsets.bottom;

                final int maxWidth = screenBounds.x + screenBounds.width - ttPosition.x;
                final int maxHeight = ttPosition.y + tt.getPreferredSize().height - screenBounds.y;
                // final Point loc = getLocationOnScreen();
                tt.setMaximumSize(new Dimension(maxWidth, maxHeight));
                ToolTipController.this.activeToolTipPanel = tt;
                tt.addMouseListener(ToolTipController.this);
                ttPosition = activeToolTipPanel.getDesiredLocation(activeComponent, ttPosition);

                activePopup = popupFactory.getPopup(activeComponent, activeToolTipPanel, ttPosition.x, ttPosition.y);

                final Window ownerWindow = SwingUtilities.getWindowAncestor(activeComponent);
                // if the components window is not the active any more, for
                // exmaple
                // because we opened a dialog, don't show tooltip

                if (ownerWindow != null) {
                    ownerWindow.removeWindowFocusListener(this);
                    ownerWindow.addWindowFocusListener(this);
                }
                tt.onShow();
                activePopup.show();

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
        activeComponent = (JComponent) handler;

        mousePosition = MouseInfo.getPointerInfo().getLocation();
        showTooltip();

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
        if (activeComponent == circledProgressBar) {
            delayer.stop();
            activeComponent = null;
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
        hideTooltip();

    }

    /*
     * (non-Javadoc)
     * 
     * @see java.awt.event.MouseListener#mousePressed(java.awt.event.MouseEvent)
     */
    @Override
    public void mousePressed(final MouseEvent e) {
        // TODO Auto-generated method stub

    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * java.awt.event.MouseListener#mouseReleased(java.awt.event.MouseEvent)
     */
    @Override
    public void mouseReleased(final MouseEvent e) {
        // TODO Auto-generated method stub

    }

}
