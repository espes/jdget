/**
 * Copyright (c) 2009 - 2011 AppWork UG(haftungsbeschränkt) <e-mail@appwork.org>
 * 
 * This file is part of org.appwork.screenshot
 * 
 * This software is licensed under the Artistic License 2.0,
 * see the LICENSE file or http://www.opensource.org/licenses/artistic-license-2.0.php
 * for details
 */
package org.appwork.screenshot;

import java.awt.AWTException;

import org.appwork.utils.swing.EDTRunner;

/**
 * @author thomas
 * 
 */
public class ScreenShot {
    public static void main(final String[] args) throws AWTException, InterruptedException {
        new EDTRunner() {

            @Override
            protected void runInEDT() {
                Layover layover;
                try {
                    layover = Layover.create();

                    layover.start();
                } catch (final AWTException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
        };

    }

}
