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
            final boolean pre = frame.isAlwaysOnTop();

            frame.addWindowFocusListener(new WindowFocusListener() {

                @Override
                public void windowGainedFocus(final WindowEvent e) {
                    frame.setAlwaysOnTop(pre);
                    frame.removeWindowFocusListener(this);
                }

                @Override
                public void windowLostFocus(final WindowEvent e) {
                    // TODO Auto-generated method stub

                }
            });
            frame.setAlwaysOnTop(true);
            frame.toFront();
        } else {
            frame.toFront();
        }

    }

}
