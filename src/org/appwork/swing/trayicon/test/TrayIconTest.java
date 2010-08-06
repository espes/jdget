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

import java.awt.BorderLayout;

import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JTextArea;

import org.appwork.swing.trayicon.AWTrayIcon;

/**
 * @author thomas
 * 
 */
public class TrayIconTest {

    /**
     * @param args
     */
    public static void main(String[] args) {
        String title = "Frame Title";
        JFrame frame = new JFrame(title);
        JComponent comp = new JTextArea();
        frame.getContentPane().add(comp, BorderLayout.CENTER);
        frame.setSize(400, 300);
        frame.setVisible(true);

        AWTrayIcon trayIcon = new AWTrayIcon(frame);

        // trayIcon.setFrameVisible(visible)
    }

}
