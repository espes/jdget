package org.appwork.utils.swing.locator;

import java.awt.Point;
import java.awt.Window;

import org.appwork.storage.config.JsonConfig;
import org.appwork.utils.Application;
import org.appwork.utils.swing.dialog.LocationStorage;

public class RememberAbsoluteLocator extends AbstractLocator {

    private final String id;

    /**
     * @param string
     */
    public RememberAbsoluteLocator(final String id) {
        this.id = id;
        if (id == null) { throw new IllegalArgumentException("id ==null"); }
    }

    /**
     * @param dialog
     * @return
     */
    private LocationStorage createConfig(final Window dialog) {
        return JsonConfig.create(Application.getResource("cfg/" + RememberAbsoluteLocator.class.getName() + "-" + this.getID(dialog)), LocationStorage.class);
    }

    /**
     * @param dialog
     * @return
     */
    protected String getID(final Window dialog) {
        return this.id;
    }

    @Override
    public Point getLocationOnScreen(final Window dialog) {
        try {
            final LocationStorage cfg = this.createConfig(dialog);
            if (cfg.isValid()) {

                // Do a "is on screen check" here

                return AbstractLocator.validate(new Point(cfg.getX(), cfg.getY()), dialog);
            }
        } catch (final Throwable e) {
            e.printStackTrace();
        }
        return new CenterOfScreenLocator().getLocationOnScreen(dialog);
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.appwork.utils.swing.dialog.Locator#onClose(org.appwork.utils.swing
     * .dialog.frame)
     */
    @Override
    public void onClose(final Window frame) {
        try {
            if (frame.isShowing()) {
                final Point loc = frame.getLocationOnScreen();
                final LocationStorage cfg = this.createConfig(frame);
                cfg.setValid(true);
                cfg.setX(loc.x);
                cfg.setY(loc.y);
                cfg._getStorageHandler().write();
            }
        } catch (final Throwable e) {
            e.printStackTrace();
            // nothing.... frame.getParent or parent might be invisible
        }
    }

}
