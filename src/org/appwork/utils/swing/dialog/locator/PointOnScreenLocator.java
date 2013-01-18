package org.appwork.utils.swing.dialog.locator;

import java.awt.Point;

import org.appwork.utils.swing.dialog.AbstractDialog;

public class PointOnScreenLocator implements DialogLocator {

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
