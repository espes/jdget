package org.appwork.uio;

import javax.swing.ImageIcon;

import org.appwork.utils.swing.dialog.AbstractDialog;
import org.appwork.utils.swing.dialog.Dialog;
import org.appwork.utils.swing.dialog.DialogCanceledException;
import org.appwork.utils.swing.dialog.DialogClosedException;
import org.appwork.utils.swing.dialog.UserIODefinition;

public class BasicDialogHandler implements UserIOHandlerInterface {
    private static final Dialog D = Dialog.I();

    public boolean showConfirmDialog(final int flags, final String title, final String message, final ImageIcon icon, final String ok, final String cancel) {
        try {
            D.showConfirmDialog(flags, title, message, icon, ok, cancel);
            return true;
        } catch (final DialogClosedException e) {
            // // TODO Auto-generated catch block
            // e.printStackTrace();
        } catch (final DialogCanceledException e) {
            // // TODO Auto-generated catch block
            // e.printStackTrace();
        }
        return false;
    }

    public boolean showConfirmDialog(final int flags, final String title, final String message) {
        return showConfirmDialog(flags, title, message, null, null, null);
    }

    public void showMessageDialog(final String message) {
        D.showMessageDialog(message);
    }

    @SuppressWarnings("unchecked")
    public <T extends UserIODefinition> T show(final Class<T> class1, final AbstractDialog<?> defImpl) {
        final T ret = (T) defImpl;
        try {
            D.showDialog(defImpl);
        } catch (final DialogClosedException e) {

            // no Reason to log here
        } catch (final DialogCanceledException e) {
            // no Reason to log here
        }
        return ret;
    }

    public void showErrorMessage(final String message) {
        D.showErrorDialog(message);
    }
}
