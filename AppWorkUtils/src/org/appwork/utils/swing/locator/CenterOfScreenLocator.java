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
import java.awt.Frame;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.Insets;
import java.awt.MouseInfo;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.Window;

import org.appwork.utils.swing.SwingUtils;

/**
 * @author Thomas
 * 
 */
public class CenterOfScreenLocator extends AbstractLocator {

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.appwork.utils.swing.dialog.Locator#getLocationOnScreen(javax.swing
     * .JDialog)
     */
    @Override
    public Point getLocationOnScreen(final Window dialog) {

        if (dialog.getParent() == null || !dialog.getParent().isDisplayable() || !dialog.getParent().isVisible()) {
            final Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();

            return correct(new Point((int) (screenSize.getWidth() - dialog.getWidth()) / 2, (int) (screenSize.getHeight() - dialog.getHeight()) / 2),dialog);

        } else if (dialog.getParent() instanceof Frame && ((Frame) dialog.getParent()).getExtendedState() == Frame.ICONIFIED) {
            // dock dialog at bottom right if mainframe is not visible

            final GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
            final GraphicsDevice[] screens = ge.getScreenDevices();

            for (final GraphicsDevice screen : screens) {
                final Rectangle bounds = screen.getDefaultConfiguration().getBounds();
                screen.getDefaultConfiguration().getDevice();

                final Insets insets = Toolkit.getDefaultToolkit().getScreenInsets(screen.getDefaultConfiguration());
                if (bounds.contains(MouseInfo.getPointerInfo().getLocation())) {

                return correct(new Point((int) (bounds.x + bounds.getWidth() - dialog.getWidth() - 20 - insets.right), (int) (bounds.y + bounds.getHeight() - dialog.getHeight() - 20 - insets.bottom)), dialog);

                }

            }
            final Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
            return correct(new Point((int) (screenSize.getWidth() - dialog.getWidth() - 20), (int) (screenSize.getHeight() - dialog.getHeight() - 60)), dialog);
        } else {
            final Point ret = SwingUtils.getCenter(dialog.getParent(), dialog);

            return correct(ret, dialog);
        }

        // if (frame.getParent() == null || !frame.getParent().isDisplayable()
        // || !frame.getParent().isVisible()) {
        // final Dimension screenSize =
        // Toolkit.getDefaultToolkit().getScreenSize();
        //
        // return (new Point((int) (screenSize.getWidth() - frame.getWidth()) /
        // 2, (int) (screenSize.getHeight() - frame.getHeight()) / 2));
        //
        // } else if (frame.getParent() instanceof Frame && ((Frame)
        // frame.getParent()).getExtendedState() == Frame.ICONIFIED) {
        // // dock dialog at bottom right if mainframe is not visible
        //
        // final GraphicsEnvironment ge =
        // GraphicsEnvironment.getLocalGraphicsEnvironment();
        // final GraphicsDevice[] screens = ge.getScreenDevices();
        //
        // for (final GraphicsDevice screen : screens) {
        // final Rectangle bounds =
        // screen.getDefaultConfiguration().getBounds();
        // screen.getDefaultConfiguration().getDevice();
        //
        // Insets insets =
        // Toolkit.getDefaultToolkit().getScreenInsets(screen.getDefaultConfiguration());
        // if (bounds.contains(MouseInfo.getPointerInfo().getLocation())) {
        // return (new Point((int) (bounds.x + bounds.getWidth() -
        // frame.getWidth() - 20 - insets.right), (int) (bounds.y +
        // bounds.getHeight() - frame.getHeight() - 20 - insets.bottom))); }
        //
        // }
        // final Dimension screenSize =
        // Toolkit.getDefaultToolkit().getScreenSize();
        // return (new Point((int) (screenSize.getWidth() - frame.getWidth() -
        // 20), (int) (screenSize.getHeight() - frame.getHeight() - 60)));
        // } else {
        // return SwingUtils.getCenter(frame.getParent(), frame);
        // }

    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.appwork.utils.swing.dialog.Locator#onClose(org.appwork.utils.swing
     * .dialog.AbstractDialog)
     */
    @Override
    public void onClose(final Window abstractDialog) {
        // TODO Auto-generated method stub

    }

}
