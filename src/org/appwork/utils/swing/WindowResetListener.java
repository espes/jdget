/**
 * Copyright (c) 2009 - 2013 AppWork UG(haftungsbeschränkt) <e-mail@appwork.org>
 * 
 * This file is part of org.appwork.utils.swing
 * 
 * This software is licensed under the Artistic License 2.0,
 * see the LICENSE file or http://www.opensource.org/licenses/artistic-license-2.0.php
 * for details
 */
package org.appwork.utils.swing;

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
import org.appwork.utils.swing.WindowManager.FrameState;

public class WindowResetListener implements PropertySetListener, HierarchyListener,WindowListener {

    private FrameState[] flags;
    private Window       w;
    private boolean      oldFocusableWindowState;

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
    public WindowResetListener(final WindowsWindowManager windowsWindowManager, final Window w, final FrameState[] flags2) {
        this.w = w;
        flags = flags2;
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

    public FrameState[] getFlags() {
        return flags;
    }

    /**
     * @param flags
     */
    public void setFlags(final FrameState[] flags) {
        this.flags = flags;

    }

    /**
     * 
     */
    public void resetProperties() {
        removeListeners();

        final boolean requestFocus = FrameState.FOCUS.containedBy(getFlags());
        final boolean forceToFront = requestFocus || FrameState.TO_FRONT.containedBy(getFlags());

        if (requestFocus || forceToFront) {

            windowsWindowManager.setFocusableWindowState(w, oldFocusableWindowState);
            windowsWindowManager.setFocusable(w, oldFocusable);
            windowsWindowManager.setAlwaysOnTop(w, oldAlwaysOnTop);

        } else {
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
        System.out.println("Reset After Window Opened");
        removeListeners();

        final boolean requestFocus = FrameState.FOCUS.containedBy(getFlags());
        final boolean forceToFront = requestFocus || FrameState.TO_FRONT.containedBy(getFlags());

        if (requestFocus || forceToFront) {
            // it is important to reset focus states before calling
            // toFront
            windowsWindowManager.setFocusableWindowState(w, oldFocusableWindowState);
            windowsWindowManager.setFocusable(w, oldFocusable);
            windowsWindowManager.setAlwaysOnTop(w, oldAlwaysOnTop);
            windowsWindowManager.toFront(w, getFlags());

        } else {
            if (w instanceof Frame) {

                windowsWindowManager.setExtendedState((Frame)w,frameExtendedState);
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
        System.out.println("Property Update: "+propertyName+" - "+newValue);
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
//      w.addHierarchyListener(this);
        w.addWindowListener(this);
        
    }

    /* (non-Javadoc)
     * @see java.awt.event.WindowListener#windowActivated(java.awt.event.WindowEvent)
     */
    @Override
    public void windowActivated(final WindowEvent windowevent) {
        // TODO Auto-generated method stub
        
    }

    /* (non-Javadoc)
     * @see java.awt.event.WindowListener#windowClosed(java.awt.event.WindowEvent)
     */
    @Override
    public void windowClosed(final WindowEvent windowevent) {
        // TODO Auto-generated method stub
        
    }

    /* (non-Javadoc)
     * @see java.awt.event.WindowListener#windowClosing(java.awt.event.WindowEvent)
     */
    @Override
    public void windowClosing(final WindowEvent windowevent) {
        // TODO Auto-generated method stub
        
    }

    /* (non-Javadoc)
     * @see java.awt.event.WindowListener#windowDeactivated(java.awt.event.WindowEvent)
     */
    @Override
    public void windowDeactivated(final WindowEvent windowevent) {
        // TODO Auto-generated method stub
        
    }

    /* (non-Javadoc)
     * @see java.awt.event.WindowListener#windowDeiconified(java.awt.event.WindowEvent)
     */
    @Override
    public void windowDeiconified(final WindowEvent windowevent) {
        // TODO Auto-generated method stub
        
    }

    /* (non-Javadoc)
     * @see java.awt.event.WindowListener#windowIconified(java.awt.event.WindowEvent)
     */
    @Override
    public void windowIconified(final WindowEvent windowevent) {
        // TODO Auto-generated method stub
        
    }

}