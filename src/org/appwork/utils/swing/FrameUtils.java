/**
 * Copyright (c) 2009 - 2011 AppWork UG(haftungsbeschränkt) <e-mail@appwork.org>
 * 
 * This file is part of org.appwork.utils.swing
 * 
 * This software is licensed under the Artistic License 2.0,
 * see the LICENSE file or http://www.opensource.org/licenses/artistic-license-2.0.php
 * for details
 */
package org.appwork.utils.swing;

import java.awt.event.WindowEvent;
import java.awt.event.WindowFocusListener;

import javax.swing.JFrame;

import org.appwork.utils.os.CrossSystem;

/**
 * @author thomas
 * 
 */
public class FrameUtils {

    /**
     * @param frame
     */
    public static void toFront(final JFrame frame) {
        if (CrossSystem.isLinux()) {
            /**
             * workaround for linux window manager, toFront does not work same
             * way as under windows. we add a windowfocuslistener and set window
             * to alwaysontop, that brings window to front. now the user clicks
             * on it and windowmanager will bring it finally to front. in case
             * the window looses its focus, alwaysontop will be set to original
             * value (eg user clicks on another window)
             */
            final boolean pre = frame.isAlwaysOnTop();

            frame.addWindowFocusListener(new WindowFocusListener() {

                @Override
                public void windowGainedFocus(final WindowEvent e) {

                }

                @Override
                public void windowLostFocus(final WindowEvent e) {
                    frame.setAlwaysOnTop(pre);
                    frame.removeWindowFocusListener(this);
                }
            });
            frame.setAlwaysOnTop(true);
            frame.toFront();
        } else {
            frame.toFront();
        }

    }

}
