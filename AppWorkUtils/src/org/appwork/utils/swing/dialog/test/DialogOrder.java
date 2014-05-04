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
import org.appwork.utils.swing.dialog.DialogCanceledException;
import org.appwork.utils.swing.dialog.DialogClosedException;
import org.appwork.utils.swing.dialog.InputDialog;

/**
 * @author thomas
 */
public class DialogOrder {

    /**
     * Close Order: 0 1 2 3 4 5 6 7 8 9 A B
     */
    public static void main(final String[] args) {
        for (int i = 0; i < 10; i++) {
            DialogOrder.startDialogInThread(i);
        }
        try {
            Thread.sleep(11000);
        } catch (final InterruptedException e) {
            e.printStackTrace();
        }
        DialogOrder.test2();
    }

    /**
     * Close Order: 0 1 2 3 4 5 6 7 8 9
     */
    private static void startDialogInThread(final int i) {
        new Thread(i + "") {
            @Override
            public void run() {
                try {
                    Thread.sleep(1000 * i);
                } catch (final InterruptedException e) {
                    e.printStackTrace();
                }
                try {
                    Dialog.getInstance().showInputDialog("Dialog " + i);
                } catch (final DialogClosedException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                } catch (final DialogCanceledException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
                System.out.println("Closed " + i);
            }
        }.start();
    }

    /**
     * Close Order: A B
     */
    private static void test2() {
        final InputDialog dialog = new InputDialog(0, "title", "message", "defaultMessage", null, null, null);

        dialog.setLeftActions(new AbstractAction("CLICK HERE!!!") {

            private static final long serialVersionUID = 3916626551625222343L;

            public void actionPerformed(final ActionEvent e) {

                Dialog.getInstance().showMessageDialog("INTERNAL");

                System.out.println("Closed A");
            }

        });
        try {
            Dialog.getInstance().showDialog(dialog);
        } catch (final DialogClosedException e1) {
            // TODO Auto-generated catch block
            e1.printStackTrace();
        } catch (final DialogCanceledException e1) {
            // TODO Auto-generated catch block
            e1.printStackTrace();
        }
        System.out.println("Closed B");
    }

}
