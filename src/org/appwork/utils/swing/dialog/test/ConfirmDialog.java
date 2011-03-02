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
public class ConfirmDialog {

    /**
     * @param args
     * @throws DialogCanceledException
     * @throws DialogClosedException
     */
    public static void main(final String[] args) throws DialogClosedException, DialogCanceledException {
        Dialog.getInstance().showConfirmDialog(0, "title", "message this is a longer message. it is very long. probably over one line long. ", null, null, null);
        // Dialog.getInstance().showConfirmDialog(Dialog.STYLE_SHOW_DO_NOT_DISPLAY_AGAIN,
        // "title", "message", null, null, null);
    }

}
