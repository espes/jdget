/**
 * Copyright (c) 2009 - 2010 AppWork UG(haftungsbeschränkt) <e-mail@appwork.org>
 * 
 * This file is part of org.appwork.utils.swing.dialog.test
 * 
 * This software is licensed under the Artistic License 2.0,
 * see the LICENSE file or http://www.opensource.org/licenses/artistic-license-2.0.php
 * for details
 */
package org.appwork.utils.swing.dialog.test;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;

import org.appwork.utils.swing.dialog.Dialog;
import org.appwork.utils.swing.dialog.InputDialog;

/**
 * @author thomas
 * 
 */
public class DialogOrder {

    /**
     * 
     * closeord: 0 1 2 3 4 5 6 7 8 9 A B
     * 
     * @param args
     */
    public static void main(final String[] args) {
        for (int i = 0; i < 10; i++) {
            DialogOrder.startDialogInThread(i);
        }
        try {
            Thread.sleep(11000);
        } catch (final InterruptedException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        DialogOrder.test2();

    }

    /**
     * 
     * close order should run from 0 to 9
     * 
     * @param i
     */
    private static void startDialogInThread(final int i) {
        new Thread(i + "") {
            public void run() {
                try {
                    Thread.sleep(1000 * i);
                } catch (final InterruptedException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
                Dialog.getInstance().showInputDialog("Dialog " + i);
                System.out.println("CLosed " + i);
            }
        }.start();
    }

    /**
     * closeorder: A B
     */
    private static void test2() {
        final InputDialog dialog = new InputDialog(0, "title", "message", "defaultMessage", null, null, null);

        dialog.setLeftActions(new AbstractAction("CLICK HERE!!!") {

            @Override
            public void actionPerformed(final ActionEvent e) {
                Dialog.getInstance().showMessageDialog("INTERNAL");
                System.out.println("A");
            }

        });
        Dialog.getInstance().showDialog(dialog);
        System.out.println("B");
    }

}
