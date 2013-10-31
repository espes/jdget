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

import java.awt.Frame;
import java.awt.Window;

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
            if ((state & Frame.MAXIMIZED_BOTH) != 0) { return MAXIMIZED_BOTH; }
            if ((state & Frame.NORMAL) != 0) { return NORMAL; }
            if ((state & Frame.ICONIFIED) != 0) { return ICONIFIED; }
            return NORMAL;
        }

    }

    static WindowManager INSTANCE = WindowManager.createOsWindowManager();

    /**
     * @return
     */
    private static WindowManager createOsWindowManager() {
        if (CrossSystem.isWindows()) {
            return new WindowsWindowManager();
        } else if (CrossSystem.isLinux()) {
            return new LinuxWindowManager();
        } else if (CrossSystem.isMac()) {
            return new MacWindowManager();
        } else {
            return new DefaultWindowManager();
        }

    }

    public static WindowManager getInstance() {
        return WindowManager.INSTANCE;
    }

    /**
     * @return
     */
    public WindowExtendedState getExtendedState(final Frame w) {

        return WindowExtendedState.get(w.getExtendedState());
    }

    /**
     * @param mainFrame
     * @return
     */
    public boolean hasFocus(final Window window) {
        if (window != null && window.isFocusOwner()) { return true; }
        if (window != null && window.getFocusOwner() != null) { return true;

        }
        if (window != null && window.isFocused()) { return true;

        }
        if (window != null && window.hasFocus()) { return true;

        }
        return false;

    }

    public void hide(final Window w) {
        this.setVisible(w, false, FrameState.OS_DEFAULT);

    }

    public void hide(final Window w, final FrameState state) {
        this.setVisible(w, false, state);

    }

    public void setExtendedState(final Frame w, final WindowExtendedState state) {
        if (state == null) { throw new NullPointerException("State is null"); }
        switch (state) {
        case NORMAL:
            w.setExtendedState(Frame.NORMAL);
            break;
        case ICONIFIED:
            w.setExtendedState(Frame.ICONIFIED);
            break;
        case MAXIMIZED_BOTH:
            w.setExtendedState(Frame.MAXIMIZED_BOTH);
            break;
        }

    }

    public void setVisible(final Window w, final boolean visible) {
        this.setVisible(w, visible, FrameState.OS_DEFAULT);
    }

    abstract public void setVisible(Window w, boolean visible, FrameState state);

    /**
     * @param w
     */
    abstract public void setZState(Window w, FrameState state);

    public void show(final Window w) {
        this.setVisible(w, true, FrameState.OS_DEFAULT);

    }

    public void show(final Window w, final FrameState state) {
        this.setVisible(w, true, state);

    }

    /**
     * @return
     */
    public boolean hasFocus() {
        for (final Window w : Window.getWindows()) {
            if (hasFocus(w)) {
                return true;
            }
        }
        return false;
    }

}
