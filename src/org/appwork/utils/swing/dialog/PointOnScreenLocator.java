package org.appwork.utils.swing.dialog;

import java.awt.Point;

import javax.swing.JDialog;

public class PointOnScreenLocator implements Locator {

    private Point point;

    /**
     * @param point
     */
    public PointOnScreenLocator(Point point) {
        this.point = point;
    }

    @Override
    public Point getLocationOnScreen(AbstractDialog<?> dialog) {

        return point;
    }

    /* (non-Javadoc)
     * @see org.appwork.utils.swing.dialog.Locator#onClose(org.appwork.utils.swing.dialog.AbstractDialog)
     */
    @Override
    public void onClose(AbstractDialog<?> abstractDialog) {
        // TODO Auto-generated method stub
        
    }

}
