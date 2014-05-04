/**
 * Copyright (c) 2009 - 2013 AppWork UG(haftungsbeschränkt) <e-mail@appwork.org>
 * 
 * This file is part of org.appwork.utils.swing.dialog
 * 
 * This software is licensed under the Artistic License 2.0,
 * see the LICENSE file or http://www.opensource.org/licenses/artistic-license-2.0.php
 * for details
 */
package org.appwork.utils.swing.dialog;

import java.awt.Window;

import org.appwork.utils.swing.windowmanager.WindowManager;
import org.appwork.utils.swing.windowmanager.WindowManager.FrameState;

/**
 * @author Thomas
 *
 */
public class BasicZHandler implements WindowZHandler{

    /* (non-Javadoc)
     * @see org.appwork.utils.swing.dialog.WindowZHandler#getWindowStateOnVisible(org.appwork.utils.swing.dialog.AbstractDialog)
     */
    @Override
    public FrameState getWindowStateOnVisible(final AbstractDialog<?> d) {
        for (final Window w : Window.getWindows()) {
            if (WindowManager.getInstance().hasFocus(w)) {
                return FrameState.TO_FRONT_FOCUSED;
            }
        }
       
        return FrameState.TO_FRONT;
    }

}
