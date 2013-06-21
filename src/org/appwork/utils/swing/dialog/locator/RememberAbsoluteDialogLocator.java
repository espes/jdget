package org.appwork.utils.swing.dialog.locator;

import java.awt.Point;

import org.appwork.utils.swing.dialog.AbstractDialog;
import org.appwork.utils.swing.locator.RememberAbsoluteLocator;

public class RememberAbsoluteDialogLocator implements DialogLocator {

    private String                        id;
    private final RememberAbsoluteLocator delegate;

    /**
     * @param string
     */
    public RememberAbsoluteDialogLocator(final String id) {
        if (id == null) { throw new IllegalArgumentException("id ==null"); }
        this.delegate = new RememberAbsoluteLocator(id);
    }

    @Override
    public Point getLocationOnScreen(final AbstractDialog<?> dialog) {
        return this.delegate.getLocationOnScreen(dialog.getDialog());
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.appwork.utils.swing.dialog.Locator#onClose(org.appwork.utils.swing
     * .dialog.AbstractDialog)
     */
    @Override
    public void onClose(final AbstractDialog<?> abstractDialog) {
        this.delegate.onClose(abstractDialog.getDialog());
    }

}
