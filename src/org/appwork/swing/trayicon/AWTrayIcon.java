/**
 * Copyright (c) 2009 - 2010 AppWork UG(haftungsbeschränkt) <e-mail@appwork.org>
 * 
 * This file is part of org.appwork.swing.trayicon.view
 * 
 * This software is licensed under the Artistic License 2.0,
 * see the LICENSE file or http://www.opensource.org/licenses/artistic-license-2.0.php
 * for details
 */
package org.appwork.swing.trayicon;

import java.awt.Image;
import java.awt.Point;
import java.awt.SystemTray;
import java.awt.TrayIcon;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import javax.swing.JFrame;
import javax.swing.SwingUtilities;

import org.appwork.utils.ImageProvider.ImageProvider;
import org.appwork.utils.logging.Log;
import org.appwork.utils.os.CrossSystem;
import org.appwork.utils.swing.EDTHelper;

/**
 * @author thomas
 */
public class AWTrayIcon implements MouseListener, TrayMouseListener {

    private final JFrame frame;

    private ExtTrayIcon trayIcon;

    private TrayIconPopup trayIconPopup;

    private final int visibleToggleClickCount = 2;

    public AWTrayIcon(final JFrame frame) {
        this(frame, ((frame.getIconImages() == null) || (frame.getIconImages().size() == 0)) ? ImageProvider.createIcon(((frame.getTitle() != null) && (frame.getTitle().length() > 0)) ? frame.getTitle().charAt(0) + "" : "T", 32, 32) : frame.getIconImages().get(0));
    }

    public AWTrayIcon(final JFrame frame, final Image icon) {
        this.frame = frame;

        final SystemTray systemTray = SystemTray.getSystemTray();
        /*
         * trayicon message must be set, else windows cannot handle icon right
         * (eg autohide feature)
         */
        this.trayIcon = new ExtTrayIcon(icon, frame.getTitle());

        this.trayIcon.addMouseListener(this);
        this.trayIcon.addTrayMouseListener(this);

        try {
            systemTray.add(this.trayIcon);
        } catch (final Exception e) {
            Log.exception(e);
        }
    }

    public TrayIconPopup createPopup() {
        return null;
    }

    public void displayToolTip() {
        // trayIcon.getEstimatedTopLeft();
    }

    private void hideToolTip() {
    }

    public void dispose() {
        try {
            if (this.trayIcon != null) {

                SystemTray.getSystemTray().remove(this.trayIcon);
                this.trayIcon.removeMouseListener(this);
                this.trayIcon.removeTrayMouseListener(this);
                this.trayIcon = null;

            }
        } catch (final Exception e) {
        }
    }

    public JFrame getFrame() {
        return this.frame;
    }

    private boolean isFrameVisible() {
        return this.frame.isVisible();
    }

    @Override
    public void mouseClicked(final MouseEvent e) {
    }

    @Override
    public void mouseEntered(final MouseEvent e) {
    }

    @Override
    public void mouseExited(final MouseEvent e) {
        this.hideToolTip();
    }

    @Override
    public void mouseMoveOverTray(final MouseEvent me) {
        if ((this.trayIconPopup != null) && this.trayIconPopup.isVisible()) { return; }
        this.displayToolTip();
    }

    @Override
    public void mousePressed(final MouseEvent e) {
        this.hideToolTip();

        if (e.getSource() instanceof TrayIcon) {
            if (!CrossSystem.isMac()) {
                if ((e.getClickCount() == this.visibleToggleClickCount) && !SwingUtilities.isRightMouseButton(e)) {
                    this.setFrameVisible(!this.isFrameVisible());
                } else {
                    if ((this.trayIconPopup != null) && this.trayIconPopup.isShowing()) {
                        this.trayIconPopup.dispose();
                        this.trayIconPopup = null;
                    } else if (SwingUtilities.isRightMouseButton(e)) {

                        this.trayIconPopup = this.createPopup();
                        if (this.trayIconPopup == null) { return; }
                        this.trayIconPopup.setPosition(e.getPoint());
                        this.trayIconPopup.setVisible(true);
                        this.trayIconPopup.startAutoHide();
                    }
                }
            } else {
                if (SwingUtilities.isLeftMouseButton(e)) {
                    if ((e.getClickCount() == this.visibleToggleClickCount) && !SwingUtilities.isLeftMouseButton(e)) {
                        this.setFrameVisible(!this.isFrameVisible());
                    } else {
                        if ((this.trayIconPopup != null) && this.trayIconPopup.isShowing()) {
                            this.trayIconPopup.dispose();
                            this.trayIconPopup = null;
                        } else if (SwingUtilities.isLeftMouseButton(e)) {

                            this.trayIconPopup = this.createPopup();
                            if (this.trayIconPopup == null) { return; }
                            final Point pointOnScreen = e.getLocationOnScreen();
                            if (e.getX() > 0) {
                                pointOnScreen.x -= e.getPoint().x;
                            }
                            this.trayIconPopup.setPosition(pointOnScreen);
                            this.trayIconPopup.setVisible(true);
                            this.trayIconPopup.startAutoHide();
                        }
                    }
                }
            }
        }
    }

    @Override
    public void mouseReleased(final MouseEvent e) {
    }

    public void setFrameVisible(final boolean visible) {

        new EDTHelper<Object>() {

            @Override
            public Object edtRun() {
                /*
                 * This is a workaround, because toFront does not work on all
                 * systems. Originally,this workaround used a Timer that resets
                 * the alwaysontop setting after 2000ms. This should work, too
                 */
                final JFrame resetAlwaysOnTop;

                if (!visible) {
                    AWTrayIcon.this.frame.setVisible(false);
                    resetAlwaysOnTop = null;
                } else {
                    if (!AWTrayIcon.this.frame.isAlwaysOnTop()) {
                        resetAlwaysOnTop = AWTrayIcon.this.frame;
                    } else {
                        resetAlwaysOnTop = null;
                    }
                    AWTrayIcon.this.frame.setAlwaysOnTop(true);
                    AWTrayIcon.this.frame.setVisible(true);
                    AWTrayIcon.this.frame.toFront();
                }

                if (visible && (resetAlwaysOnTop != null)) {
                    SwingUtilities.invokeLater(new Runnable() {

                        @Override
                        public void run() {
                            resetAlwaysOnTop.setAlwaysOnTop(false);
                        }

                    });
                }
                return null;
            }

        }.start();
    }

    public void setImage(final Image icon) {
        this.trayIcon.setImage(icon);
    }

    public void setToolTip(final String tooltip) {
        this.trayIcon.setToolTip(tooltip);
    }

}
