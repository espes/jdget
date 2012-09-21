package org.appwork.utils.swing.locator;

import java.awt.Dimension;
import java.awt.Frame;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Window;

import javax.swing.JFrame;

import org.appwork.storage.config.JsonConfig;
import org.appwork.utils.Application;
import org.appwork.utils.swing.dialog.LocationStorage;

public class RememberRelativeLocator implements Locator {

    private String id;
    private Window parent;

    /**
     * @param jFrame
     * @param string
     */
    public RememberRelativeLocator(String id, JFrame jFrame) {
        this.id = id;
        parent = jFrame;
    }

    @Override
    public Point getLocationOnScreen(JFrame frame) {
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
    private LocationStorage createConfig(JFrame frame) {
        return JsonConfig.create(Application.getResource("cfg/" + RememberRelativeLocator.class.getName() + "-" + getID(frame) + ".json"), LocationStorage.class);
    }

    /**
     * @param frame
     * @return
     */
    protected String getID(JFrame frame) {
        if (id == null) return frame.toString();
        return id;
    }

    private Point validate(Point point, JFrame frame) {
        final GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
        final GraphicsDevice[] screens = ge.getScreenDevices();

        // for (final GraphicsDevice screen : screens) {
        Dimension dimension = frame.getPreferredSize();
        for (final GraphicsDevice screen : screens) {
            final Rectangle bounds = screen.getDefaultConfiguration().getBounds();
            if (point.x >= bounds.x && point.x < bounds.x + bounds.width) {
                if (point.y >= bounds.y && point.y < bounds.y + bounds.height) {
                    // found point on screen
                    if (point.x + dimension.width <= bounds.x + bounds.width) {

                        if (point.y + dimension.height <= bounds.y + bounds.height) {
                            // frame is completly visible on this screen
                            return point;
                        }
                    }

                }
            }
        }

        return new CenterOfScreenLocator().getLocationOnScreen(frame);

    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.appwork.utils.swing.frame.Locator#onClose(org.appwork.utils.swing
     * .frame.frame)
     */
    @Override
    public void onClose(JFrame frame) {
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
