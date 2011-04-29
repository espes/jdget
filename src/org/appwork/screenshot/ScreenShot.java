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
import java.awt.image.BufferedImage;

import javax.swing.ImageIcon;

import org.appwork.utils.swing.EDTHelper;
import org.appwork.utils.swing.dialog.Dialog;
import org.appwork.utils.swing.dialog.DialogCanceledException;
import org.appwork.utils.swing.dialog.DialogClosedException;

/**
 * @author thomas
 * 
 */
public class ScreenShot {
    public static void main(final String[] args) throws AWTException, InterruptedException {

        final ScreenShooter layover = new EDTHelper<ScreenShooter>() {

            @Override
            public ScreenShooter edtRun() {
                try {
                    final ScreenShooter layover = ScreenShooter.create();
                    ;

                    layover.start();
                    return layover;

                } catch (final AWTException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                    return null;
                }
            }

        }.getReturnValue();

        final BufferedImage screenshot = layover.getScreenshot();
        if (screenshot != null) {

            try {
                Dialog.getInstance().showConfirmDialog(0, "", "", new ImageIcon(screenshot), null, null);
            } catch (final DialogClosedException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } catch (final DialogCanceledException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }

        }
        System.exit(0);

    }

}
