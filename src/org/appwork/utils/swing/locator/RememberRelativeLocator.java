package org.appwork.utils.swing.locator;

import java.awt.IllegalComponentStateException;
import java.awt.Point;
import java.awt.Window;

import org.appwork.storage.config.JsonConfig;
import org.appwork.utils.Application;
import org.appwork.utils.swing.dialog.LocationStorage;

public class RememberRelativeLocator extends AbstractLocator {

    private String          id;
    private Window          parent;
    private AbstractLocator fallbackLocator;

    /**
     * @param jFrame
     * @param string
     */
    public RememberRelativeLocator(final String id, final Window jFrame) {
        this.id = id;
        parent = jFrame;
        fallbackLocator = new CenterOfScreenLocator();
    }

    @Override
    public Point getLocationOnScreen(final Window frame) {
        final LocationStorage cfg = createConfig(frame);
        try {
            if (cfg.isValid()) {

                // Do a "is on screen check" here

                final Point pLoc = (parent == null || !parent.isShowing()) ? frame.getParent().getLocationOnScreen() : parent.getLocationOnScreen();
                return validate(new Point(cfg.getX() + pLoc.x, cfg.getY() + pLoc.y), frame);

            }
        } catch (final IllegalComponentStateException e) {
            // frame.getParent() might be null or invisble

             e.printStackTrace();
        }
        return getFallbackLocator().getLocationOnScreen(frame);
    }

    /**
     * @return
     */
    protected AbstractLocator getFallbackLocator() {
        return fallbackLocator;
    }

    public void setFallbackLocator(final AbstractLocator fallbackLocator) {
        this.fallbackLocator = fallbackLocator;
    }

    /**
     * @param frame
     * @return
     */
    private LocationStorage createConfig(final Window frame) {
        return JsonConfig.create(Application.getResource("cfg/" + RememberRelativeLocator.class.getName() + "-" + getID(frame)), LocationStorage.class);
    }

    /**
     * @param frame
     * @return
     */
    protected String getID(final Window frame) {
        if (id == null) { return frame.toString(); }
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
    public void onClose(final Window frame) {
        try {
            final LocationStorage cfg = createConfig(frame);
            cfg.setValid(true);
            if (frame.isShowing()) {
                final Point loc = frame.getLocationOnScreen();
                final Point pLoc = parent == null ? frame.getParent().getLocationOnScreen() : parent.getLocationOnScreen();
                cfg.setX(loc.x - pLoc.x);
                cfg.setY(loc.y - pLoc.y);
            }
        } catch (final IllegalComponentStateException e) {
            // nothing.... frame.getParent or parent might be invisible
        }

    }

}
