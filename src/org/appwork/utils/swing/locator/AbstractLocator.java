/**
 * Copyright (c) 2009 - 2012 AppWork UG(haftungsbeschränkt) <e-mail@appwork.org>
 * 
 * This file is part of org.appwork.utils.swing.dialog
 * 
 * This software is licensed under the Artistic License 2.0,
 * see the LICENSE file or http://www.opensource.org/licenses/artistic-license-2.0.php
 * for details
 */
package org.appwork.utils.swing.locator;

import java.awt.Dimension;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.Insets;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.Window;

/**
 * @author Thomas
 * 
 */
public abstract class AbstractLocator implements Locator {

    public static Point correct(final Point point, final Window d) {
        final Dimension prefSize = d.getSize();

        return correct(point, prefSize);

    }

    public static Point correct(final Point point, final Dimension prefSize) {
        final GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
        final GraphicsDevice[] screens = ge.getScreenDevices();

        final Rectangle preferedRect = new Rectangle(point.x, point.y, prefSize.width, prefSize.height);
        GraphicsDevice biggestInteresctionScreem = null;
        int biggestIntersection = -1;

        for (final GraphicsDevice screen : screens) {
            final Rectangle bounds = screen.getDefaultConfiguration().getBounds();
            final Insets insets = Toolkit.getDefaultToolkit().getScreenInsets(screen.getDefaultConfiguration());
            bounds.x += insets.left;
            bounds.y += insets.top;
            bounds.width -= insets.left + insets.right;
            bounds.height -= insets.top + insets.bottom;
            final Rectangle interSec = bounds.intersection(preferedRect);
            if (Math.max(interSec.width, 0) * Math.max(interSec.height, 0) > biggestIntersection || biggestInteresctionScreem == null) {
                biggestIntersection = Math.max(interSec.width, 0) * Math.max(interSec.height, 0);
                biggestInteresctionScreem = screen;
                if (interSec.equals(preferedRect)) {
                    break;
                }
            }
        }
        final Rectangle bounds = biggestInteresctionScreem.getDefaultConfiguration().getBounds();
        final Insets insets = Toolkit.getDefaultToolkit().getScreenInsets(biggestInteresctionScreem.getDefaultConfiguration());
        bounds.x += insets.left;
        bounds.y += insets.top;
        bounds.width -= insets.left + insets.right;
        bounds.height -= insets.top + insets.bottom;
        if (preferedRect.x + preferedRect.width > bounds.x + bounds.width) {
            preferedRect.x = bounds.x + bounds.width - preferedRect.width;
        }
        if (preferedRect.y + preferedRect.height > bounds.y + bounds.height) {
            preferedRect.y = bounds.y + bounds.height - preferedRect.height;
        }
        if (preferedRect.x < bounds.x) {
            preferedRect.x = bounds.x;
        }

        if (preferedRect.y < bounds.y) {
            preferedRect.y = bounds.y;
        }

        return preferedRect.getLocation();
    }

    /**
     * @param point
     * @param dialog
     * @return
     */
    public static Point validate(Point point, final Window dialog) {
        point = AbstractLocator.correct(point, dialog);
        final GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
        final GraphicsDevice[] screens = ge.getScreenDevices();

        // for (final GraphicsDevice screen : screens) {
        for (final GraphicsDevice screen : screens) {
            final Rectangle bounds = screen.getDefaultConfiguration().getBounds();
            if (bounds.contains(point)) { return point;
            // if (point.x >= bounds.x && point.x < bounds.x + bounds.width) {
            // if (point.y >= bounds.y && point.y < bounds.y + bounds.height) {
            // // found point on screen
            // if (point.x + dimension.width <= bounds.x + bounds.width) {
            //
            // if (point.y + dimension.height <= bounds.y + bounds.height) {
            // // dialog is completly visible on this screen
            // return point;
            // }
            // }
            //
            // }
            // }
            }
        }

        return new CenterOfScreenLocator().getLocationOnScreen(dialog);

    }
}
