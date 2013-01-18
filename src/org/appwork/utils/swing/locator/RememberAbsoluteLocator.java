package org.appwork.utils.swing.locator;

import java.awt.Point;
import java.awt.Window;

import org.appwork.storage.config.JsonConfig;
import org.appwork.utils.Application;
import org.appwork.utils.swing.dialog.LocationStorage;

public class RememberAbsoluteLocator extends AbstractLocator {

    private String id;

    /**
     * @param string
     */
    public RememberAbsoluteLocator(String id) {
        this.id = id;

    }

    @Override
    public Point getLocationOnScreen(Window dialog) {
        LocationStorage cfg = createConfig(dialog);
        if (cfg.isValid()) {

            // Do a "is on screen check" here

            return validate(new Point(cfg.getX(), cfg.getY()), dialog);
        }
        return new CenterOfScreenLocator().getLocationOnScreen(dialog);
    }

    /**
     * @param dialog
     * @return
     */
    private LocationStorage createConfig(Window dialog) {
        return JsonConfig.create(Application.getResource("cfg/" + RememberAbsoluteLocator.class.getName() + "-" + getID(dialog)), LocationStorage.class);
    }

    /**
     * @param dialog
     * @return
     */
    protected String getID(Window dialog) {
        if (id == null) return dialog.toString();
        return id;
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.appwork.utils.swing.dialog.Locator#onClose(org.appwork.utils.swing
     * .dialog.frame)
     */
    @Override
    public void onClose(Window frame) {
        if (frame.isShowing()) {
            LocationStorage cfg = createConfig(frame);
            cfg.setValid(true);
            Point loc = frame.getLocationOnScreen();
            cfg.setX(loc.x);
            cfg.setY(loc.y);
            cfg.getStorageHandler().write();
        }

    }

}
