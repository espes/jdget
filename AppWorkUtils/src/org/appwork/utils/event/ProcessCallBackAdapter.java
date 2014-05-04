package org.appwork.utils.event;

import javax.swing.ImageIcon;

import org.appwork.uio.UIOManager;
import org.appwork.utils.swing.dialog.Dialog;
import org.appwork.utils.swing.dialog.DialogCanceledException;
import org.appwork.utils.swing.dialog.DialogClosedException;

public class ProcessCallBackAdapter implements ProcessCallBack {

    @Override
    public void setProgress(final Object caller, final int percent) {
        // TODO Auto-generated method stub

    }

    @Override
    public void setStatus(final Object caller, final Object statusObject) {
        // TODO Auto-generated method stub

    }

    @Override
    public void setStatusString(final Object caller, final String string) {
        // TODO Auto-generated method stub

    }

    @Override
    public void showDialog(final Object caller, final String title, final String message, final ImageIcon icon) {
        try {
            Dialog.getInstance().showConfirmDialog(UIOManager.BUTTONS_HIDE_CANCEL, title, message, icon, null, null);
        } catch (final DialogClosedException e) {
            e.printStackTrace();
        } catch (final DialogCanceledException e) {
            e.printStackTrace();
        }

    }

}
