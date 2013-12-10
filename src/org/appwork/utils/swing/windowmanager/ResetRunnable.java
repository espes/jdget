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
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import org.appwork.swing.ExtJFrame;
import org.appwork.swing.PropertyStateEventProviderInterface;
import org.appwork.swing.event.PropertySetListener;
import org.appwork.utils.swing.windowmanager.WindowManager.FrameState;

/**
 * @author Thomas
 * 
 */
public class ResetRunnable implements ActionListener, PropertySetListener {

    private WindowsWindowManager windowsWindowManager;
    private Window               w;
    private FrameState           state;
    private boolean              oldFocusableWindowState;
    private boolean              oldAlwaysOnTop;
    private boolean              oldFocusable;

    public FrameState getState() {
        return state;
    }

    /**
     * @param windowsWindowManager
     * @param w
     * @param hasListener
     */
    public ResetRunnable(final WindowsWindowManager windowsWindowManager, final Window w, final WindowResetListener hasListener) {

        this.windowsWindowManager = windowsWindowManager;
        this.w = w;
        if (hasListener != null) {
            hasListener.resetProperties();
        }
        oldFocusableWindowState = w.getFocusableWindowState();
        oldAlwaysOnTop = w.isAlwaysOnTop();
        oldFocusable = w.isFocusable();

        if (w instanceof PropertyStateEventProviderInterface) {
            ((PropertyStateEventProviderInterface) w).getPropertySetEventSender().addListener(this, true);
        }

    }

    public Window getWindow() {
        return w;
    }

    /**
     * @param flags
     */
    public void setState(final FrameState flags) {
        state = flags;

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
        }

    };

    /*
     * (non-Javadoc)
     * 
     * @see
     * java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
     */
    @Override
    public void actionPerformed(final ActionEvent actionevent) {
        //System.out.println("Reset After Timeout");
        if (w instanceof PropertyStateEventProviderInterface) {
            ((PropertyStateEventProviderInterface) w).getPropertySetEventSender().removeListener(ResetRunnable.this);
        }
        windowsWindowManager.removeTimer(ResetRunnable.this);


        // it is important that we
        // 1. setAlwaysOnTop back
        // 2. setFocusableWindowState back

        // else setAlwaysOnTop would fire a WINDOW_ACTIVATED and a
        // WINDOW_GAINED_FOCUS even if the window does not get
        // active or focused
        windowsWindowManager.setAlwaysOnTop(w, oldAlwaysOnTop);
        switch (getState()) {
        case TO_FRONT_FOCUSED:
        case TO_FRONT:

            // it is important to reset focus states before calling
            // toFront
            windowsWindowManager.setFocusableWindowState(w, oldFocusableWindowState);
            windowsWindowManager.setFocusable(w, oldFocusable);
            break;

        default:

            // // it's important to call toBack first. else we see a flicker
            // // (window appears and disappears)
            // windowsWindowManager.toBack(w);

            windowsWindowManager.setFocusableWindowState(w, oldFocusableWindowState);
            windowsWindowManager.setFocusable(w, oldFocusable);

        }

    }

}
