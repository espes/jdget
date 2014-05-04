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
import org.appwork.utils.swing.dialog.DialogCanceledException;
import org.appwork.utils.swing.dialog.DialogClosedException;

/**
 * @author thomas
 * 
 */
public class ConfirmDialogTest {

    /**
     * @param args
     * @throws DialogCanceledException
     * @throws DialogClosedException
     */
    public static void main(final String[] args) {

        try {
            Dialog.getInstance().showConfirmDialog(0, "title", "After adding links, JDownloader lists them in the Linkgrabber View to find file/package information likeOnlinestatus, Filesize, or Filename. Afterwards, Links are sorted into packages. Please choose whether JDownloader shall auto start download to your default downloadfolder \"(%s1)\"afterwards, or keep links in Linkgrabber until you click [continue] manually. You can change this option at any timein the Linkgrabber View.message this is a longer message. it is very long. probably over one line long. ", null, null, null);
        } catch (final DialogClosedException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (final DialogCanceledException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        // Dialog.getInstance().showConfirmDialog(Dialog.STYLE_SHOW_DO_NOT_DISPLAY_AGAIN,
        // "title", "message", null, null, null);
    }

}
