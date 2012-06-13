package org.appwork.utils.swing.dialog;

import java.awt.Dimension;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.Point;
import java.awt.Rectangle;

import org.appwork.storage.config.JsonConfig;
import org.appwork.utils.Application;
import org.appwork.utils.swing.SwingUtils;

public class RememberAbsoluteLocator implements Locator {

    private String id;

    /**
     * @param string
     */
    public RememberAbsoluteLocator(String id) {
        this.id = id;
    }

    @Override
    public Point getLocationOnScreen(AbstractDialog<?> dialog) {
        LocationStorage cfg = createConfig(dialog);
        if (cfg.isValid()) {

            // Do a "is on screen check" here

            return validate(new Point(cfg.getX(), cfg.getY()), dialog);
        }
        return AbstractDialog.LOCATE_CENTER_OF_SCREEN.getLocationOnScreen(dialog);
    }

    /**
     * @param point
     * @param dialog
     * @return
     */
    private Point validate(Point point, AbstractDialog<?> dialog) {
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

        return AbstractDialog.LOCATE_CENTER_OF_SCREEN.getLocationOnScreen(dialog);

    }

    /**
     * @param dialog
     * @return
     */
    private LocationStorage createConfig(AbstractDialog<?> dialog) {
        return JsonConfig.create(Application.getResource("cfg/" + RememberAbsoluteLocator.class.getName() + "-" + getID(dialog) + ".json"), LocationStorage.class);
    }

    /**
     * @param dialog
     * @return
     */
    protected String getID(AbstractDialog<?> dialog) {
        if (id == null) return dialog.toString();
        return id;
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
        LocationStorage cfg = createConfig(abstractDialog);
        cfg.setValid(true);
        Point loc = abstractDialog.getDialog().getLocationOnScreen();
        cfg.setX(loc.x);
        cfg.setY(loc.y);

    }

}
