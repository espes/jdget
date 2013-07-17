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

import java.awt.Point;
import java.awt.Window;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import org.appwork.utils.swing.WindowManager.FrameState;

public class WindowResetListener extends WindowAdapter implements PropertyChangeListener {

    private FrameState[]         flags;
    private Window               w;
    private boolean              oldFocusableWindowState;
    private boolean              oldFocusable;
    private WindowsWindowManager windowsWindowManager;
    private Point                location;
    private Point                offscreenpoint;

    /**
     * @param windowsWindowManager
     * @param w
     * @param flags2
     */
    public WindowResetListener(WindowsWindowManager windowsWindowManager, Window w, FrameState[] flags2) {
        this.w = w;
        this.flags = flags2;
        this.windowsWindowManager = windowsWindowManager;
        oldFocusableWindowState = w.getFocusableWindowState();
        oldFocusable = w.isFocusable();
        location = w.getLocation();
        w.addPropertyChangeListener(this);
        offscreenpoint = new Point(Integer.MAX_VALUE, Integer.MAX_VALUE);
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

    @Override
    public void windowOpened(final WindowEvent windowevent) {

        final boolean requestFocus = FrameState.FOCUS.containedBy(getFlags());
        final boolean forceToFront = requestFocus || FrameState.TO_FRONT.containedBy(getFlags());

        if (requestFocus || forceToFront) {
            // it is important to reset focus states before calling
            // toFront
            windowsWindowManager.setFocusableWindowState(w, oldFocusableWindowState);
            windowsWindowManager.setFocusable(w, oldFocusable);
            windowsWindowManager.toFront(w, getFlags());

        } else {

            // it's important to call toBack first. else we see a flicker
            // (window appears and disappears)
            windowsWindowManager.toBack(w);

            windowsWindowManager.setFocusableWindowState(w, oldFocusableWindowState);
            windowsWindowManager.setFocusable(w, oldFocusable);

        }

        w.removeWindowListener(this);
        w.removePropertyChangeListener(this);
        Point loc = w.getLocation();
        if (w.getLocation().equals(getOffScreenPoint())) {
            w.setLocation(location);
        } else {
            // window has been moved externaly
        }

    }

    /*
     * (non-Javadoc)
     * 
     * @see java.beans.PropertyChangeListener#propertyChange(java.beans.
     * PropertyChangeEvent)
     */
    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        // ignore changes from the windowmanager itself
        if (evt.getPropertyName() == null || evt.getPropertyName().equals(windowsWindowManager.getBlocker())) return;
        if ("focusableWindowState".equals(evt.getPropertyName())) {
            oldFocusableWindowState = (Boolean) evt.getNewValue();
        } else if ("focusable".equals(evt.getPropertyName())) {
            oldFocusable = (Boolean) evt.getNewValue();
        }

    }

    /**
     * @return
     */
    public Point getOffScreenPoint() {
        // TODO: find areal<offscreenpoint.
        return offscreenpoint;
    }

    /**
     * @param offscreen
     */
    public void setOffScreenPoint(Point offscreen) {
        offscreenpoint = offscreen;

    };

}