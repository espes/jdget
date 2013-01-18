package org.appwork.utils.swing.dialog.locator;

import java.awt.Point;

import javax.swing.JFrame;

import org.appwork.utils.swing.dialog.AbstractDialog;
import org.appwork.utils.swing.locator.RememberRelativeLocator;

public class RememberRelativeDialogLocator implements DialogLocator {

    private RememberRelativeLocator delegate;

    /**
     * @param jFrame
     * @param string
     */
    public RememberRelativeDialogLocator(String id, JFrame jFrame) {
        delegate = new RememberRelativeLocator(id, jFrame);

    }

    @Override
    public Point getLocationOnScreen(AbstractDialog<?> dialog) {
        return delegate.getLocationOnScreen(dialog.getDialog());

    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.appwork.utils.swing.dialog.Locator#onClose(org.appwork.utils.swing
     * .dialog.AbstractDialog)
     */
    @Override
    public void onClose(AbstractDialog<?> abstractDialog) {
        delegate.onClose(abstractDialog.getDialog());

    }

}
