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
        TO_FRONT,
        FOCUS;

        /**
         * @param flags
         * @return
         */
        public boolean containedBy(final FrameState[] flags) {
            if (flags == null) { return false; }
            for (final FrameState f : flags) {
                if (f == this) { return true; }
            }
            return false;
        }
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
            if ((state & JFrame.NORMAL) != 0) {
                return NORMAL;
            }
            if ((state & JFrame.MAXIMIZED_BOTH) != 0) {
                return MAXIMIZED_BOTH;
            }
            if ((state & JFrame.ICONIFIED) != 0) {
                return ICONIFIED;
            }
            return null;
        }

    }

    static WindowManager INSTANCE = createOsWindowManager();

    /**
     * @return
     */
    private static WindowManager createOsWindowManager() {

        switch (CrossSystem.OS_ID) {
        case CrossSystem.OS_WINDOWS_2000:
        case CrossSystem.OS_WINDOWS_2003:
        case CrossSystem.OS_WINDOWS_7:
        case CrossSystem.OS_WINDOWS_8:
        case CrossSystem.OS_WINDOWS_NT:
        case CrossSystem.OS_WINDOWS_OTHER:
        case CrossSystem.OS_WINDOWS_SERVER_2008:
        case CrossSystem.OS_WINDOWS_VISTA:
        case CrossSystem.OS_WINDOWS_XP:
            return new WindowsWindowManager();

        default:
            return new DefaultWindowManager();
        }

    }

    /**
     * @param w
     */
    abstract public void toFront(Window w, FrameState... flags);

    abstract public void setVisible(Window w, boolean visible, FrameState... flags);

    abstract public void show(Window w, FrameState... flags);

    abstract public void hide(Window w, FrameState... flags);

    public static WindowManager getInstance() {
        return INSTANCE;
    }

    /**
     * @param mainFrame
     * @param normal
     */
    abstract public void setExtendedState(Frame w, WindowExtendedState state);

}
