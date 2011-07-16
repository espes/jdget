/**
 * Copyright (c) 2009 - 2011 AppWork UG(haftungsbeschränkt) <e-mail@appwork.org>
 * 
 * This file is part of org.appwork.screenshot
 * 
 * This software is licensed under the Artistic License 2.0,
 * see the LICENSE file or http://www.opensource.org/licenses/artistic-license-2.0.php
 * for details
 */
package org.appwork.screenshot;

import java.awt.AWTException;
import java.awt.Color;
import java.awt.DisplayMode;
import java.awt.Graphics2D;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.Image;
import java.awt.Rectangle;
import java.awt.Robot;
import java.awt.image.BufferedImage;

/**
 * @author thomas
 * 
 */
public class ScreensShotHelper {

    /**
     * @param x
     * @param y
     * @param width
     * @param height
     * @return
     */
    public static Image getScreenShot(final int x, final int y, final int width, final int height) {

        final GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
        final GraphicsDevice[] screens = ge.getScreenDevices();
        final Rectangle rect = new Rectangle(width, height);
        rect.x = x;
        rect.y = y;

        try {

            final BufferedImage img = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
            final Graphics2D g2 = img.createGraphics();
            g2.setColor(Color.ORANGE);
            g2.fillRect(0, 0, width, height);
            for (final GraphicsDevice screen : screens) {
                final DisplayMode dm = screen.getDisplayMode();
                // bounds are used to gete the position and size of this screen
                // in
                // the complete screen configuration
                final Rectangle bounds = screen.getDefaultConfiguration().getBounds();
                if (!bounds.intersects(rect)) {
                    continue;
                }

                final Rectangle part = bounds.intersection(rect);
                part.x -= bounds.x;
                part.y -= bounds.y;
                Robot robot;

                robot = new Robot(screen);

                final BufferedImage image = robot.createScreenCapture(part);
                final Rectangle intersections = bounds.intersection(rect);
                g2.drawImage(image, intersections.x - x, intersections.y - y, null);

            }
            g2.dispose();
            return img;
        } catch (final AWTException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return null;
    }
}
