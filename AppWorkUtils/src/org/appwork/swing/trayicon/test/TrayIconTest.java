/**
 * Copyright (c) 2009 - 2010 AppWork UG(haftungsbeschränkt) <e-mail@appwork.org>
 * 
 * This file is part of org.appwork.swing.trayicon.test
 * 
 * This software is licensed under the Artistic License 2.0,
 * see the LICENSE file or http://www.opensource.org/licenses/artistic-license-2.0.php
 * for details
 */
package org.appwork.swing.trayicon.test;

import java.awt.AWTException;
import java.awt.BorderLayout;

import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JTextArea;

import org.appwork.swing.trayicon.AWTrayIcon;
import org.appwork.utils.swing.windowmanager.WindowManager;
import org.appwork.utils.swing.windowmanager.WindowManager.FrameState;

/**
 * @author thomas
 */
public class TrayIconTest {

    public static void main(final String[] args) throws AWTException {
        final JComponent comp = new JTextArea();

        final JFrame frame = new JFrame("Frame Title");
        frame.getContentPane().add(comp, BorderLayout.CENTER);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(400, 300);
          WindowManager.getInstance().setVisible(frame,true,FrameState.OS_DEFAULT);

        new AWTrayIcon(frame);
    }

}
