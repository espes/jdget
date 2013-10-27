package org.appwork.utils.swing.locator;

import java.awt.Point;
import java.awt.Window;

import org.appwork.storage.config.JsonConfig;
import org.appwork.utils.Application;
import org.appwork.utils.Hash;
import org.appwork.utils.swing.dialog.LocationStorage;
import org.appwork.utils.swing.dimensor.RememberLastDimensor;

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
        String storageID = RememberAbsoluteLocator.class.getSimpleName() + "-" + this.getID(dialog);
        if (storageID.length() > 128) {
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < storageID.length(); i++) {
                char c = storageID.charAt(i);
                switch (c) {
                case 'U':
                case 'E':
                case 'I':
                case 'O':
                case 'A':
                case 'J':
                case 'u':
                case 'e':
                case 'i':
                case 'o':
                case 'a':
                case 'j':
                    continue;
                default:
                    sb.append(c);

                }
            }
            storageID = sb.toString();

        }
        if (storageID.length() > 128) {
            storageID = RememberAbsoluteLocator.class.getSimpleName() + "-" + Hash.getMD5(storageID);
        }
        return JsonConfig.create(Application.getResource("cfg/" + storageID), LocationStorage.class);
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
