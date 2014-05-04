/**
 * Copyright (c) 2009 - 2013 AppWork UG(haftungsbeschränkt) <e-mail@appwork.org>
 * 
 * This file is part of org.appwork.utils.swing
 * 
 * This software is licensed under the Artistic License 2.0,
 * see the LICENSE file or http://www.opensource.org/licenses/artistic-license-2.0.php
 * for details
 */
package org.appwork.utils.swing.windowmanager;

import java.awt.Component;
import java.awt.Frame;
import java.awt.Point;
import java.awt.Window;
import java.awt.event.HierarchyEvent;
import java.awt.event.HierarchyListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;

import org.appwork.swing.ExtJFrame;
import org.appwork.swing.PropertyStateEventProviderInterface;
import org.appwork.swing.event.PropertySetListener;
import org.appwork.utils.swing.windowmanager.WindowManager.FrameState;

public class WindowResetListener implements PropertySetListener, HierarchyListener, WindowListener {

    private FrameState state;
    private Window     w;
    private boolean    oldFocusableWindowState;

    public boolean isOldFocusableWindowState() {
        return oldFocusableWindowState;
    }

    public boolean isOldFocusable() {
        return oldFocusable;
    }

    public boolean isOldAlwaysOnTop() {
        return oldAlwaysOnTop;
    }

    private boolean              oldFocusable;
    private WindowsWindowManager windowsWindowManager;
    private Point                location;
    private boolean              oldAlwaysOnTop;
    private int                  frameExtendedState;

    /**
     * @param windowsWindowManager
     * @param w
     * @param flags2
     */
    public WindowResetListener(final WindowsWindowManager windowsWindowManager, final Window w, final FrameState state) {
        this.w = w;
        this.state = state;
        this.windowsWindowManager = windowsWindowManager;
        oldFocusableWindowState = w.getFocusableWindowState();
        oldAlwaysOnTop = w.isAlwaysOnTop();
        oldFocusable = w.isFocusable();
        if (w instanceof Frame) {
            frameExtendedState = ((Frame) w).getExtendedState();
        }
        if (w instanceof PropertyStateEventProviderInterface) {
            ((PropertyStateEventProviderInterface) w).getPropertySetEventSender().addListener(this, true);
        }
        location = w.getLocation();

    }

    public FrameState getState() {
        return state;
    }

    /**
     * @param flags
     */
    public void setState(final FrameState flags) {
        state = flags;

    }

    /**
     * 
     */
    public void resetProperties() {
        removeListeners();

        switch (getState()) {
        case TO_FRONT:
        case TO_FRONT_FOCUSED:

            windowsWindowManager.setFocusableWindowState(w, oldFocusableWindowState);
            windowsWindowManager.setFocusable(w, oldFocusable);
            windowsWindowManager.setAlwaysOnTop(w, oldAlwaysOnTop);
            break;
        default:

            windowsWindowManager.setFocusableWindowState(w, oldFocusableWindowState);
            windowsWindowManager.setFocusable(w, oldFocusable);
            if (w instanceof Frame) {

                windowsWindowManager.setExtendedState(((Frame) w), frameExtendedState);
            }
        }

        w.setLocation(location);
    }

    protected void removeListeners() {
        w.removeHierarchyListener(this);
        w.removeWindowListener(this);
        if (w instanceof PropertyStateEventProviderInterface) {
            ((PropertyStateEventProviderInterface) w).getPropertySetEventSender().removeListener(this);
        }
    };

    /*
     * (non-Javadoc)
     * 
     * @see java.awt.event.HierarchyListener#hierarchyChanged(java.awt.event.
     * HierarchyEvent)
     */
    @Override
    public void hierarchyChanged(final HierarchyEvent hierarchyevent) {
        windowOpened(null);

    }

    public void windowOpened(final WindowEvent windowevent) {
        //System.out.println("Reset After Window Opened");
        removeListeners();

        switch (getState()) {
        case TO_FRONT:
        case TO_FRONT_FOCUSED:

            // it is important to reset focus states before calling
            // toFront
            windowsWindowManager.setFocusableWindowState(w, oldFocusableWindowState);
            windowsWindowManager.setFocusable(w, oldFocusable);
            windowsWindowManager.setAlwaysOnTop(w, oldAlwaysOnTop);
            windowsWindowManager.setZState(w, getState());

            break;
        default:
            if (w instanceof Frame) {

                windowsWindowManager.setExtendedState((Frame) w, frameExtendedState);
            }
            // it's important to call toBack first. else we see a flicker
            // (window appears and disappears)
            windowsWindowManager.toBack(w);

            windowsWindowManager.setFocusableWindowState(w, oldFocusableWindowState);
            windowsWindowManager.setFocusable(w, oldFocusable);

        }

        w.setLocation(location);

    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.appwork.swing.event.PropertySetListener#onPropertySet(java.awt.Component
     * , java.lang.String, java.lang.Object, java.lang.Object)
     */
    @Override
    public void onPropertySet(final Component caller, final String propertyName, final Object oldValue, final Object newValue) {
        if (propertyName == null || propertyName.equals(windowsWindowManager.getBlocker())) { return; }
        //System.out.println("Property Update: " + propertyName + " - " + newValue);
        if (ExtJFrame.PROPERTY_FOCUSABLE_WINDOW_STATE.equals(propertyName)) {
            oldFocusableWindowState = (Boolean) newValue;
        } else if (ExtJFrame.PROPERTY_FOCUSABLE.equals(propertyName)) {
            oldFocusable = (Boolean) newValue;
        } else if (ExtJFrame.PROPERTY_ALWAYS_ON_TOP.equals(propertyName)) {
            oldAlwaysOnTop = (Boolean) newValue;
        } else if (ExtJFrame.PROPERTY_LOCATION.equals(propertyName)) {
            location = (Point) newValue;
        } else if (ExtJFrame.PROPERTY_EXTENDED_STATE.equals(propertyName)) {
            frameExtendedState = (Integer) newValue;
        }

    }

    /**
     * 
     */
    public void add() {
         w.addHierarchyListener(this);
         // do not use the window opened listener. it is only called the FIRST time a window gets visible
//        w.addWindowListener(this);

    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * java.awt.event.WindowListener#windowActivated(java.awt.event.WindowEvent)
     */
    @Override
    public void windowActivated(final WindowEvent windowevent) {
        // TODO Auto-generated method stub

    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * java.awt.event.WindowListener#windowClosed(java.awt.event.WindowEvent)
     */
    @Override
    public void windowClosed(final WindowEvent windowevent) {
        // TODO Auto-generated method stub

    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * java.awt.event.WindowListener#windowClosing(java.awt.event.WindowEvent)
     */
    @Override
    public void windowClosing(final WindowEvent windowevent) {
        // TODO Auto-generated method stub

    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * java.awt.event.WindowListener#windowDeactivated(java.awt.event.WindowEvent
     * )
     */
    @Override
    public void windowDeactivated(final WindowEvent windowevent) {
        // TODO Auto-generated method stub

    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * java.awt.event.WindowListener#windowDeiconified(java.awt.event.WindowEvent
     * )
     */
    @Override
    public void windowDeiconified(final WindowEvent windowevent) {
        // TODO Auto-generated method stub

    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * java.awt.event.WindowListener#windowIconified(java.awt.event.WindowEvent)
     */
    @Override
    public void windowIconified(final WindowEvent windowevent) {
        // TODO Auto-generated method stub

    }

}