/**
 * Copyright (c) 2009 - 2011 AppWork UG(haftungsbeschränkt) <e-mail@appwork.org>
 * 
 * This file is part of org.appwork.utils.swing.windowflasher
 * 
 * This software is licensed under the Artistic License 2.0,
 * see the LICENSE file or http://www.opensource.org/licenses/artistic-license-2.0.php
 * for details
 */
package org.appwork.utils.swing.windowflasher;

import java.awt.Frame;

import org.appwork.app.gui.BasicGui;
import org.appwork.utils.swing.EDTRunner;

/**
 * @author thomas
 * 
 */
public class Test {
    public static void main(final String[] args) {
        new EDTRunner() {

            @Override
            protected void runInEDT() {

                final BasicGui bg = new BasicGui("Flasher") {

                    @Override
                    protected void layoutPanel() {
                        // TODO Auto-generated method stub

                    }

                    @Override
                    protected void requestExit() {
                        System.exit(0);
                    }
                };

                bg.getFrame().setVisible(true);
                bg.getFrame().setExtendedState(Frame.ICONIFIED);
                while (true) {
                    try {
                        Thread.sleep(10000);
                    } catch (final InterruptedException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                    new EDTRunner() {

                        @Override
                        protected void runInEDT() {
                            System.out.println("FLASH");
                            bg.getFlasher().start();
                        }
                    };

                }
            }
        };
    }
}
