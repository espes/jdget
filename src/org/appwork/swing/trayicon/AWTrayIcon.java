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

import java.awt.AWTException;
import java.awt.Image;
import java.awt.Point;
import java.awt.SystemTray;
import java.awt.TrayIcon;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import javax.swing.JFrame;
import javax.swing.SwingUtilities;

import org.appwork.utils.ImageProvider.ImageProvider;
import org.appwork.utils.event.BasicEvent;
import org.appwork.utils.event.BasicEventSender;
import org.appwork.utils.os.CrossSystem;
import org.appwork.utils.swing.EDTHelper;
import org.appwork.utils.swing.EDTRunner;

/**
 * @author thomas
 */
public class AWTrayIcon implements MouseListener, TrayMouseListener {

    public static final int                    EVENT_TOGGLE_FRAME_VISIBILITY = 0;

    public static final int                    EVENT_SHOW_POPUP              = 1;

    public static final int                    EVENT_HIDE_POPUP              = 2;

    private final JFrame                       frame;

    private ExtTrayIcon                        trayIcon;

    private TrayIconPopup                      trayIconPopup;

    private final int                          visibleToggleClickCount       = 2;

    private final BasicEventSender<AWTrayIcon> eventSender;

    public AWTrayIcon(final JFrame frame) throws AWTException {
        this(frame, frame.getIconImages() == null || frame.getIconImages().size() == 0 ? ImageProvider.createIcon(frame.getTitle() != null && frame.getTitle().length() > 0 ? frame.getTitle().charAt(0) + "" : "T", 32, 32) : frame.getIconImages().get(0));

    }

    public AWTrayIcon(final JFrame frame, final Image icon) throws AWTException {
        this(frame, icon, frame.getTitle());

    }

    /**
     * @param frame2
     * @param icon
     * @param title
     * @throws AWTException
     */
    public AWTrayIcon(JFrame frame, Image icon, String title) throws AWTException {
        this.frame = frame;
        this.eventSender = new BasicEventSender<AWTrayIcon>();
        final SystemTray systemTray = SystemTray.getSystemTray();
        /*
         * trayicon message must be set, else windows cannot handle icon right
         * (eg autohide feature)
         */
        this.trayIcon = new ExtTrayIcon(icon, title);

        this.trayIcon.addMouseListener(this);
        this.trayIcon.addTrayMouseListener(this);

        systemTray.add(this.trayIcon);
    }

    public TrayIconPopup createPopup() {
        return null;
    }

    public void displayToolTip() {
        System.out.println("Tooltip");
        // trayIcon.getEstimatedTopLeft();
    }

    public void dispose() {

        new EDTRunner() {

            @Override
            protected void runInEDT() {
                try {
                    if (AWTrayIcon.this.trayIcon != null) {

                        SystemTray.getSystemTray().remove(AWTrayIcon.this.trayIcon);
                        AWTrayIcon.this.trayIcon.removeMouseListener(AWTrayIcon.this);
                        AWTrayIcon.this.trayIcon.removeTrayMouseListener(AWTrayIcon.this);
                        AWTrayIcon.this.trayIcon = null;
                        AWTrayIcon.this.hideToolTip();
                        if (AWTrayIcon.this.trayIconPopup != null) {
                            AWTrayIcon.this.trayIconPopup.dispose();

                            AWTrayIcon.this.trayIconPopup = null;
                            AWTrayIcon.this.eventSender.fireEvent(new BasicEvent<AWTrayIcon>(AWTrayIcon.this, AWTrayIcon.EVENT_HIDE_POPUP, AWTrayIcon.this, null));
                        }
                    }
                } catch (final Exception e) {
                }
            }

        };

    }

    /**
     * @return the eventSender
     */
    public BasicEventSender<AWTrayIcon> getEventSender() {
        return this.eventSender;
    }

    public JFrame getFrame() {
        return this.frame;
    }

    private void hideToolTip() {
    }

    public boolean isFrameVisible() {
        return frame != null && this.frame.isVisible();
    }

    public void mouseClicked(final MouseEvent e) {
    }

    public void mouseEntered(final MouseEvent e) {
    }

    public void mouseExited(final MouseEvent e) {
        this.hideToolTip();
    }

    public void mouseMoveOverTray(final MouseEvent me) {
        if (this.trayIconPopup != null && this.trayIconPopup.isVisible()) { return; }
        this.displayToolTip();
    }

    public void mousePressed(final MouseEvent e) {
        this.hideToolTip();

        if (e.getSource() instanceof TrayIcon) {
            if (!CrossSystem.isMac()) {
                if (e.getClickCount() == this.visibleToggleClickCount && !SwingUtilities.isRightMouseButton(e)) {
                    this.eventSender.fireEvent(new BasicEvent<AWTrayIcon>(this, AWTrayIcon.EVENT_TOGGLE_FRAME_VISIBILITY, this, null));

                    this.onToggleVisibility();

                } else {
                    if (this.trayIconPopup != null && this.trayIconPopup.isShowing()) {
                        this.trayIconPopup.dispose();

                        this.trayIconPopup = null;
                        this.eventSender.fireEvent(new BasicEvent<AWTrayIcon>(this, AWTrayIcon.EVENT_HIDE_POPUP, this, null));

                    } else if (SwingUtilities.isRightMouseButton(e)) {

                        this.trayIconPopup = this.createPopup();
                        if (this.trayIconPopup == null) { return; }

                        this.trayIconPopup.setPosition(e.getPoint());
                        this.trayIconPopup.setVisible(true);
                        this.trayIconPopup.startAutoHide();
                        this.eventSender.fireEvent(new BasicEvent<AWTrayIcon>(this, AWTrayIcon.EVENT_SHOW_POPUP, this, null));

                    }
                }
            } else {
                if (SwingUtilities.isLeftMouseButton(e)) {
                    if (e.getClickCount() == this.visibleToggleClickCount && !SwingUtilities.isLeftMouseButton(e)) {
                        this.eventSender.fireEvent(new BasicEvent<AWTrayIcon>(this, AWTrayIcon.EVENT_TOGGLE_FRAME_VISIBILITY, this, null));

                        this.onToggleVisibility();
                    } else {
                        if (this.trayIconPopup != null && this.trayIconPopup.isShowing()) {
                            this.trayIconPopup.dispose();
                            this.trayIconPopup = null;
                            this.eventSender.fireEvent(new BasicEvent<AWTrayIcon>(this, AWTrayIcon.EVENT_HIDE_POPUP, this, null));

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
                            this.eventSender.fireEvent(new BasicEvent<AWTrayIcon>(this, AWTrayIcon.EVENT_SHOW_POPUP, this, null));

                        }
                    }
                }
            }
        }
    }

    public void mouseReleased(final MouseEvent e) {
    }

    /**
     * 
     */
    public void onToggleVisibility() {
        this.setFrameVisible(!this.isFrameVisible());
    }

    public void setFrameVisible(final boolean visible) {
        if (frame == null) return;
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

                if (visible && resetAlwaysOnTop != null) {
                    SwingUtilities.invokeLater(new Runnable() {

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
