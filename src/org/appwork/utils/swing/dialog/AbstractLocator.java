/**
 * Copyright (c) 2009 - 2012 AppWork UG(haftungsbeschränkt) <e-mail@appwork.org>
 * 
 * This file is part of org.appwork.utils.swing.dialog
 * 
 * This software is licensed under the Artistic License 2.0,
 * see the LICENSE file or http://www.opensource.org/licenses/artistic-license-2.0.php
 * for details
 */
package org.appwork.utils.swing.dialog;

import java.awt.Dimension;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.Insets;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Toolkit;

/**
 * @author Thomas
 * 
 */
public abstract class AbstractLocator implements Locator {
    public Point correct(Point point, AbstractDialog<?> d) {

        final GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
        final GraphicsDevice[] screens = ge.getScreenDevices();

        // for (final GraphicsDevice screen : screens) {
        Dimension prefSize = d.getPreferredSize();
        Rectangle preferedRect = new Rectangle(point.x, point.y, prefSize.width, prefSize.height);

        GraphicsDevice biggestInteresctionScreem = null;
        int biggestIntersection = -1;

        for (final GraphicsDevice screen : screens) {
            final Rectangle bounds = screen.getDefaultConfiguration().getBounds();
            Insets insets = Toolkit.getDefaultToolkit().getScreenInsets(screen.getDefaultConfiguration());
            bounds.x += insets.left;
            bounds.y += insets.top;
            bounds.width -= insets.left + insets.right;
            bounds.height -= insets.top + insets.bottom;
            Rectangle interSec = bounds.intersection(preferedRect);

            if (interSec.width * interSec.height > biggestIntersection || biggestInteresctionScreem == null) {
                biggestIntersection = interSec.width * interSec.height;
                biggestInteresctionScreem = screen;
                if (interSec.equals(prefSize)) break;
            }

        }

        final Rectangle bounds = biggestInteresctionScreem.getDefaultConfiguration().getBounds();
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
}
