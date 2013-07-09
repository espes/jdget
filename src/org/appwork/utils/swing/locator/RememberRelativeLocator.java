package org.appwork.utils.swing.locator;

import java.awt.Point;
import java.awt.Window;

import org.appwork.storage.config.JsonConfig;
import org.appwork.utils.Application;
import org.appwork.utils.swing.dialog.LocationStorage;

public class RememberRelativeLocator extends AbstractLocator {

    private final String    id;
    private final Window    parent;
    private AbstractLocator fallbackLocator;

    /**
     * @param jFrame
     * @param string
     */
    public RememberRelativeLocator(final String id, final Window jFrame) {
        this.id = id;
        if (id == null) { throw new IllegalArgumentException("id ==null"); }
        parent = jFrame;
        fallbackLocator = new CenterOfScreenLocator();
    }

    /**
     * @param frame
     * @return
     */
    private LocationStorage createConfig(final Window frame) {
        return JsonConfig.create(Application.getResource("cfg/" + RememberRelativeLocator.class.getName() + "-" + getID(frame)), LocationStorage.class);
    }

    /**
     * @return
     */
    protected AbstractLocator getFallbackLocator() {
        return fallbackLocator;
    }

    /**
     * @param frame
     * @return
     */
    protected String getID(final Window frame) {
        return id;
    }

    @Override
    public Point getLocationOnScreen(final Window frame) {
        try {
            final LocationStorage cfg = createConfig(frame);
            if (cfg.isValid()) {
                // Do a "is on screen check" here
                final Point pLoc = parent == null || !parent.isShowing() ? frame.getParent().getLocationOnScreen() : parent.getLocationOnScreen();
                return AbstractLocator.validate(new Point(cfg.getX() + pLoc.x, cfg.getY() + pLoc.y), frame);
            }
        } catch (final Throwable e) {
   
            // frame.getParent() might be null or invisble
            // e.printStackTrace();
        }
        return getFallbackLocator().getLocationOnScreen(frame);
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
            if (frame.isShowing()) {
                final Point loc = frame.getLocationOnScreen();
                final Point pLoc = parent == null ? frame.getParent().getLocationOnScreen() : parent.getLocationOnScreen();
                final LocationStorage cfg = createConfig(frame);
                cfg.setValid(true);
                cfg.setX(loc.x - pLoc.x);
                cfg.setY(loc.y - pLoc.y);
                cfg._getStorageHandler().write();
            }
        } catch (final Throwable e) {
            e.printStackTrace();
            // nothing.... frame.getParent or parent might be invisible
        }

    }

    public void setFallbackLocator(final AbstractLocator fallbackLocator) {
        this.fallbackLocator = fallbackLocator;
    }

}
