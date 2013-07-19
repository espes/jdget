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

import java.awt.Frame;
import java.awt.Window;

import javax.swing.JFrame;

import org.appwork.utils.os.CrossSystem;

/**
 * @author Thomas
 * 
 */
public abstract class WindowManager {
    public static enum FrameState {
        OS_DEFAULT,
        TO_FRONT,
        TO_BACK,
        TO_FRONT_FOCUSED;

    }

    public static enum WindowExtendedState {
        NORMAL,
        MAXIMIZED_BOTH,
        ICONIFIED;

        /**
         * @param state
         * @return
         */
        public static WindowExtendedState get(final int state) {
            if ((state & JFrame.NORMAL) != 0) { return NORMAL; }
            if ((state & JFrame.MAXIMIZED_BOTH) != 0) { return MAXIMIZED_BOTH; }
            if ((state & JFrame.ICONIFIED) != 0) { return ICONIFIED; }
            return null;
        }

    }

    static WindowManager INSTANCE = createOsWindowManager();

    /**
     * @return
     */
    private static WindowManager createOsWindowManager() {
        if (CrossSystem.isWindows()) {
            return new WindowsWindowManager();
        } else if (CrossSystem.isLinux()) {
            return new DefaultWindowManager();
        } else {
            return new DefaultWindowManager();
        }

    }

    /**
     * @param w
     */
    abstract public void setZState(Window w, FrameState state);

    abstract public void setVisible(Window w, boolean visible, FrameState state);

    public void setVisible(final Window w, final boolean visible) {
        setVisible(w, visible, FrameState.OS_DEFAULT);
    }

    public static WindowManager getInstance() {
        return INSTANCE;
    }

    public void show(final Window w, final FrameState state) {
        setVisible(w, true, state);

    }

    public void hide(final Window w, final FrameState state) {
        setVisible(w, false, state);

    }

    public void show(final Window w) {
        setVisible(w, true, FrameState.OS_DEFAULT);

    }

    public void hide(final Window w) {
        setVisible(w, false, FrameState.OS_DEFAULT);

    }

    public void setExtendedState(final Frame w, final WindowExtendedState state) {
        if (state == null) { throw new NullPointerException("State is null"); }
        switch (state) {
        case NORMAL:
            w.setExtendedState(JFrame.NORMAL);
            break;
        case ICONIFIED:
            w.setExtendedState(JFrame.ICONIFIED);
            break;
        case MAXIMIZED_BOTH:
            w.setExtendedState(JFrame.MAXIMIZED_BOTH);
            break;
        }

    }

    /**
     * @param mainFrame
     * @return
     */
    public boolean hasFocus(final Window window) {
        if (window != null && window.isFocusOwner()) { return true; }
        if (window != null && window.getFocusOwner() != null) { return true;

        }
        return false;

    }

}
