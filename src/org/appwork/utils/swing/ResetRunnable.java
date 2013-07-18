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

import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import org.appwork.utils.swing.WindowManager.FrameState;

/**
 * @author Thomas
 * 
 */
public class ResetRunnable implements PropertyChangeListener,ActionListener {

    private WindowsWindowManager windowsWindowManager;
    private Window               w;
    private FrameState[]         flags;
    private boolean oldFocusableWindowState;
    private boolean oldAlwaysOnTop;
    private boolean oldFocusable;

    public FrameState[] getFlags() {
        return flags;
    }

    /**
     * @param windowsWindowManager
     * @param w
     */
    public ResetRunnable(final WindowsWindowManager windowsWindowManager, final Window w) {
      
        this.windowsWindowManager = windowsWindowManager;
        this.w = w;
        oldFocusableWindowState = w.getFocusableWindowState();
        oldAlwaysOnTop=w.isAlwaysOnTop();
        oldFocusable = w.isFocusable();
        w.addPropertyChangeListener(this);

    }

    public Window getWindow() {
        return w;
    }


    /**
     * @param flags
     */
    public void setFlags(final FrameState[] flags) {
        this.flags = flags;

    }
    
    
    /*
     * (non-Javadoc)
     * 
     * @see java.beans.PropertyChangeListener#propertyChange(java.beans.
     * PropertyChangeEvent)
     */
    @Override
    public void propertyChange(final PropertyChangeEvent evt) {
        // ignore changes from the windowmanager itself
        if (evt.getPropertyName() == null || evt.getPropertyName().equals(windowsWindowManager.getBlocker())) {
            return;
        }
        if ("focusableWindowState".equals(evt.getPropertyName())) {
            oldFocusableWindowState = (Boolean) evt.getNewValue();
        } else if ("focusable".equals(evt.getPropertyName())) {
            oldFocusable = (Boolean) evt.getNewValue();
        }else if ("alwaysOnTop".equals(evt.getPropertyName())) {
            oldAlwaysOnTop = (Boolean) evt.getNewValue();
        }
    }

    /* (non-Javadoc)
     * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
     */
    @Override
    public void actionPerformed(final ActionEvent actionevent) {
        System.out.println("Reset After Timeout");
        final boolean requestFocus = FrameState.FOCUS.containedBy(getFlags());
        final boolean forceToFront = requestFocus || FrameState.TO_FRONT.containedBy(getFlags());

        
        // it is important that we
        // 1. setAlwaysOnTop back
        // 2. setFocusableWindowState back

        // else setAlwaysOnTop would fire a WINDOW_ACTIVATED and a
        // WINDOW_GAINED_FOCUS even if the window does not get
        // active or focused
        windowsWindowManager.setAlwaysOnTop(w, oldAlwaysOnTop);
        if (requestFocus || forceToFront) {
            // it is important to reset focus states before calling
            // toFront
            windowsWindowManager.setFocusableWindowState(w, oldFocusableWindowState);
            windowsWindowManager.setFocusable(w, oldFocusable);
      

        } else {

//            // it's important to call toBack first. else we see a flicker
//            // (window appears and disappears)
//            windowsWindowManager.toBack(w);

            windowsWindowManager.setFocusableWindowState(w, oldFocusableWindowState);
            windowsWindowManager.setFocusable(w, oldFocusable);

        }
       
        w.removePropertyChangeListener(ResetRunnable.this);
        windowsWindowManager.removeTimer(ResetRunnable.this); 
        
    }

}
