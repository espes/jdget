/**
 * Copyright (c) 2009 - 2011 AppWork UG(haftungsbeschränkt) <e-mail@appwork.org>
 * 
 * This file is part of org.appwork.utils.swing.dialog.test
 * 
 * This software is licensed under the Artistic License 2.0,
 * see the LICENSE file or http://www.opensource.org/licenses/artistic-license-2.0.php
 * for details
 */
package org.appwork.utils.swing.dialog.test;

import java.awt.Point;

import javax.swing.JTextArea;

import org.appwork.utils.swing.dialog.BalloonDialog;
import org.appwork.utils.swing.dialog.Dialog;
import org.appwork.utils.swing.dialog.DialogCanceledException;
import org.appwork.utils.swing.dialog.DialogClosedException;
import org.appwork.utils.swing.dialog.OffScreenException;

/**
 * @author thomas
 * 
 */
public class BallonTest {
    public static void main(final String[] args) {
        final JTextArea tx = new JTextArea();
        tx.setEditable(false);
        tx.setText("Hallo Nase das \r\nist meine Message\r\nZweite Zeile\r\nDritte  fsd fbdsjhfgsadjfdsajhkdsfaf a fds fadsfsadfds zeishafkjsdgfja hkdgsdakfdsafdsf  dsf dsfnd ko\r\nIch bin eine ziemlich lange zeile");
        tx.setOpaque(false);
        // tx.setText("Schau dir das an! Das ist doch toll");
        try {
            final BalloonDialog d = new BalloonDialog(0, tx, new Point(1610, 390));

            Dialog.getInstance().showDialog(d);
        } catch (final DialogClosedException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (final DialogCanceledException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (final OffScreenException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
}
