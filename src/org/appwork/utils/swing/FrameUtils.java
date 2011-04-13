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

import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;

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
            frame.addFocusListener(new FocusListener() {

                @Override
                public void focusGained(final FocusEvent e) {

                }

                @Override
                public void focusLost(final FocusEvent e) {
                    frame.setAlwaysOnTop(pre);

                }
            });

            frame.setAlwaysOnTop(true);
        } else {
            frame.toFront();
        }

    }

}
