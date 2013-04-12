package org.appwork.uio;

import javax.swing.ImageIcon;

import org.appwork.utils.locale._AWU;
import org.appwork.utils.swing.dialog.ConfirmDialog;
import org.appwork.utils.swing.dialog.Dialog;

public class MessageDialogImpl extends ConfirmDialog implements MessageDialogInterface {

    public MessageDialogImpl(final int flags, final String msg) {
        this(Dialog.BUTTONS_HIDE_CANCEL | flags, _AWU.T.DIALOG_MESSAGE_TITLE(), msg, null, null);
    }

    public MessageDialogImpl(final int flag, final String title, final String msg, final ImageIcon icon, final String okText) {
        super(flag, title, msg, icon == null ? Dialog.getIconByText(title + msg) : icon, okText, null);

    }

}
