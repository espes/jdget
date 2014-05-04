package org.appwork.utils.swing.dialog.locator;

import java.awt.Point;
import java.awt.Window;

import javax.swing.JFrame;

import org.appwork.utils.swing.dialog.AbstractDialog;
import org.appwork.utils.swing.dialog.InternDialog;
import org.appwork.utils.swing.locator.AbstractLocator;
import org.appwork.utils.swing.locator.RememberRelativeLocator;

public class RememberRelativeDialogLocator implements DialogLocator {

    private RememberRelativeLocator delegate;
    private String                  id;

    /**
     * @param jFrame
     * @param string
     */
    public RememberRelativeDialogLocator(final String id, final JFrame jFrame) {
        this.id = id;
        if (id == null) { throw new IllegalArgumentException("id ==null"); }
        delegate = new RememberRelativeLocator(id, jFrame) {
            /*
             * (non-Javadoc)
             * 
             * @see
             * org.appwork.utils.swing.locator.RememberRelativeLocator#getID
             * (java.awt.Window)
             */
            @Override
            protected String getID(final Window frame) {
                // TODO Auto-generated method stub
                return RememberRelativeDialogLocator.this.getID(frame);
            }
        };

    }

    /**
     * @param frame
     * @return
     */
    protected String getID(final Window frame) {
        return id;
    }

    @Override
    public Point getLocationOnScreen(final AbstractDialog<?> dialog) {
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
    public void onClose(final AbstractDialog<?> abstractDialog) {
        delegate.onClose(abstractDialog.getDialog());

    }

    /**
     * @param rememberAbsoluteDialogLocator
     */
    public void setFallbackLocator(final DialogLocator fallback) {
        delegate.setFallbackLocator(new AbstractLocator() {

            @Override
            public Point getLocationOnScreen(final Window frame) {

                return fallback.getLocationOnScreen(((InternDialog) frame).getDialogModel());

            }

            @Override
            public void onClose(final Window frame) {
                try {
                    fallback.onClose(((InternDialog) frame).getDialogModel());
                } catch (final Exception e) {
                    e.printStackTrace();
                }
            }
        });

    }

}
