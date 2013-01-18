package org.appwork.utils.swing.dialog.locator;

import java.awt.Dimension;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.Point;
import java.awt.Rectangle;

import org.appwork.storage.config.JsonConfig;
import org.appwork.utils.Application;
import org.appwork.utils.swing.dialog.AbstractDialog;
import org.appwork.utils.swing.dialog.LocationStorage;
import org.appwork.utils.swing.locator.RememberAbsoluteLocator;

public class RememberAbsoluteDialogLocator implements DialogLocator {

    private String id;
    private RememberAbsoluteLocator delegate;

    /**
     * @param string
     */
    public RememberAbsoluteDialogLocator(String id) {
        delegate = new RememberAbsoluteLocator(id);

    }

    @Override
    public Point getLocationOnScreen(AbstractDialog<?> dialog) {
        return delegate.getLocationOnScreen(dialog.getDialog());
//        LocationStorage cfg = createConfig(dialog);
//        if (cfg.isValid()) {
//
//            // Do a "is on screen check" here
//
//            return validate(correct(new Point(cfg.getX(), cfg.getY()), dialog), dialog);
//        }
//        return AbstractDialog.LOCATE_CENTER_OF_SCREEN.getLocationOnScreen(dialog);
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
