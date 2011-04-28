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

import java.awt.Dimension;
import java.awt.DisplayMode;
import java.awt.Graphics;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.Robot;
import java.awt.Toolkit;
import java.awt.image.BufferedImage;

import javax.swing.JWindow;

import org.appwork.utils.ImageProvider.ImageProvider;

/**
 * @author thomas
 * 
 */
public class ScreenShot {
    public static void main(final String[] args) {
        new ScreenShot().start();
    }

    public ScreenShot() {

    }

    /**
     * 
     */
    private void captureScreen() {

        try {

            final Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();

            final GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
            final GraphicsDevice[] screens = ge.getScreenDevices();

            // Get size of each screen
            final Robot robot;

            robot = new Robot();
            int x = 0;
            for (final GraphicsDevice screen : screens) {
                final DisplayMode dm = screen.getDisplayMode();
                final int screenWidth = dm.getWidth();
                final int screenHeight = dm.getHeight();

                System.out.println(screen + " : " + screenWidth + "x" + screenHeight);
                final Rectangle rect = new Rectangle(screenWidth, screenHeight);
                rect.setLocation(x, 0);

                final BufferedImage image = robot.createScreenCapture(rect);
                final BufferedImage scaled = ImageProvider.getScaledInstance(image, screenWidth / 2, screenHeight / 2, RenderingHints.VALUE_INTERPOLATION_BILINEAR, true);
                final JWindow window = new JWindow() {

                    @Override
                    public void paint(final Graphics g) {
                        g.drawImage(image, 0, 0, null);
                        g.drawString(":-P", 0, 0);
                    }

                };
                window.setSize(screenWidth, screenHeight);
                window.setLocation(x, 0);
                window.setAlwaysOnTop(true);
                window.setVisible(true);

                x += screenWidth;
            }

            final Rectangle screenRectangle = new Rectangle(screenSize);

            System.out.println("JHJ");
        } catch (final Exception e1) {

        }
    }

    /**
     * 
     */
    private void start() {
        this.captureScreen();

    }
}
