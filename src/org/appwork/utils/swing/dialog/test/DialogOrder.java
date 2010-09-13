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

import org.appwork.utils.swing.dialog.Dialog;

/**
 * @author thomas
 * 
 */
public class DialogOrder {

    /**
     * @param args
     */
    public static void main(final String[] args) {
        for (int i = 0; i < 10; i++) {
            DialogOrder.startDialogInThread(i);
        }
    }

    /**
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
            }
        }.start();
    }

}
