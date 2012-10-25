package org.appwork.utils.swing.locator;

import java.awt.Dimension;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.Point;
import java.awt.Rectangle;

import javax.swing.JFrame;

import org.appwork.storage.config.JsonConfig;
import org.appwork.utils.Application;
import org.appwork.utils.swing.dialog.LocationStorage;

public class RememberAbsoluteLocator implements Locator {

    private String id;

    /**
     * @param string
     */
    public RememberAbsoluteLocator(String id) {
        this.id = id;

    }

    @Override
    public Point getLocationOnScreen(JFrame dialog) {
        LocationStorage cfg = createConfig(dialog);
        if (cfg.isValid()) {

            // Do a "is on screen check" here

            return validate(new Point(cfg.getX(), cfg.getY()), dialog);
        }
        return new CenterOfScreenLocator().getLocationOnScreen(dialog);
    }

    /**
     * @param point
     * @param dialog
     * @return
     */
    private Point validate(Point point, JFrame dialog) {
        final GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
        final GraphicsDevice[] screens = ge.getScreenDevices();

        // for (final GraphicsDevice screen : screens) {
        Dimension dimension = dialog.getPreferredSize();
        for (final GraphicsDevice screen : screens) {
            final Rectangle bounds = screen.getDefaultConfiguration().getBounds();
            if (point.x >= bounds.x && point.x < bounds.x + bounds.width) {
                if (point.y >= bounds.y && point.y < bounds.y + bounds.height) {
                    // found point on screen
                    if (point.x + dimension.width <= bounds.x + bounds.width) {

                        if (point.y + dimension.height <= bounds.y + bounds.height) {
                            // dialog is completly visible on this screen
                            return point;
                        }
                    }

                }
            }
        }

        return new CenterOfScreenLocator().getLocationOnScreen(dialog);

    }

    /**
     * @param dialog
     * @return
     */
    private LocationStorage createConfig(JFrame dialog) {
        return JsonConfig.create(Application.getResource("cfg/" + RememberAbsoluteLocator.class.getName() + "-" + getID(dialog)), LocationStorage.class);
    }

    /**
     * @param dialog
     * @return
     */
    protected String getID(JFrame dialog) {
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
    public void onClose(JFrame frame) {
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
