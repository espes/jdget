package org.appwork.utils.swing.dialog.locator;

import java.awt.Point;
import java.awt.Window;

import javax.swing.JFrame;

import org.appwork.utils.swing.dialog.AbstractDialog;
import org.appwork.utils.swing.locator.RememberRelativeLocator;

public class RememberRelativeDialogLocator implements DialogLocator {

    private RememberRelativeLocator delegate;
    private String id;

    /**
     * @param jFrame
     * @param string
     */
    public RememberRelativeDialogLocator(final String id, final JFrame jFrame) {
        this.id=id;
        delegate = new RememberRelativeLocator(id, jFrame){
            /* (non-Javadoc)
             * @see org.appwork.utils.swing.locator.RememberRelativeLocator#getID(java.awt.Window)
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
        if(id==null) {
            return frame.toString();
        }
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

}
