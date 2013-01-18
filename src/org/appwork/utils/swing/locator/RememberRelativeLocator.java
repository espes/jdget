package org.appwork.utils.swing.locator;

import java.awt.Dimension;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Window;

import org.appwork.storage.config.JsonConfig;
import org.appwork.utils.Application;
import org.appwork.utils.swing.dialog.LocationStorage;

public class RememberRelativeLocator extends AbstractLocator{

    private String id;
    private Window parent;

    /**
     * @param jFrame
     * @param string
     */
    public RememberRelativeLocator(String id, Window jFrame) {
        this.id = id;
        parent = jFrame;
    }

    @Override
    public Point getLocationOnScreen(Window frame) {
        LocationStorage cfg = createConfig(frame);
        if (cfg.isValid()) {

            // Do a "is on screen check" here

            Point pLoc = parent == null ? frame.getParent().getLocationOnScreen() : parent.getLocationOnScreen();
            return validate(new Point(cfg.getX() + pLoc.x, cfg.getY() + pLoc.y), frame);
        }
        return new CenterOfScreenLocator().getLocationOnScreen(frame);
    }

    /**
     * @param frame
     * @return
     */
    private LocationStorage createConfig(Window frame) {
        return JsonConfig.create(Application.getResource("cfg/" + RememberRelativeLocator.class.getName() + "-" + getID(frame)), LocationStorage.class);
    }

    /**
     * @param frame
     * @return
     */
    protected String getID(Window frame) {
        if (id == null) return frame.toString();
        return id;
    }


    /*
     * (non-Javadoc)
     * 
     * @see
     * org.appwork.utils.swing.frame.Locator#onClose(org.appwork.utils.swing
     * .frame.frame)
     */
    @Override
    public void onClose(Window frame) {
        LocationStorage cfg = createConfig(frame);
        cfg.setValid(true);
        if (frame.isShowing()) {
            Point loc = frame.getLocationOnScreen();
            Point pLoc = parent == null ? frame.getParent().getLocationOnScreen() : parent.getLocationOnScreen();
            cfg.setX(loc.x - pLoc.x);
            cfg.setY(loc.y - pLoc.y);
        }

    }

}
