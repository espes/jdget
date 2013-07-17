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

/**
 * @author Thomas
 * 
 */
public interface WindowManager {
    public static enum WindowState {
        TO_FRONT,
        FOCUS;

        /**
         * @param flags
         * @return
         */
        public boolean containedBy(final WindowState[] flags) {
            if (flags == null) { return false; }
            for (final WindowState f : flags) {
                if (f == this) { return true; }
            }
            return false;
        }
    }

    /**
     * @param w
     */
    void toFront(Window w, WindowState... flags);

    void setVisible(Window w, boolean visible, WindowState... flags);

    void show(Window w, WindowState... flags);

    void hide(Window w, WindowState... flags);

}
