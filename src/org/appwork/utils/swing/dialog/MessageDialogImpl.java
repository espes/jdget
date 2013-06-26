package org.appwork.utils.swing.dialog;

import javax.swing.ImageIcon;

import org.appwork.uio.MessageDialogInterface;
import org.appwork.uio.UIOManager;
import org.appwork.utils.locale._AWU;

public class MessageDialogImpl extends ConfirmDialog implements MessageDialogInterface {

    public MessageDialogImpl(final int flags, final String msg) {
        this(UIOManager.BUTTONS_HIDE_CANCEL | flags, _AWU.T.DIALOG_MESSAGE_TITLE(), msg, null, null);
    }

    public MessageDialogImpl(final int flag, final String title, final String msg, final ImageIcon icon, final String okText) {
        super(flag, title, msg, icon == null ? Dialog.getIconByText(title + msg) : icon, okText, null);

    }

}
