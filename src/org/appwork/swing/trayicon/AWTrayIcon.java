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
 * 
 */
public class AWTrayIcon implements MouseListener, TrayMouseListener {

    private final JFrame frame;

    private ExtTrayIcon trayIcon;

    private TrayIconPopup trayIconPopup;

    private final int visibleToggleClickCount = 2;

    public AWTrayIcon(final JFrame frame) {

        this(frame, ((frame.getIconImages() == null) || (frame.getIconImages().size() == 0)) ? ImageProvider.createIcon(((frame.getTitle() != null) && (frame.getTitle().length() > 0)) ? frame.getTitle().charAt(0) + "" : "T", 32, 32) : frame.getIconImages().get(0));

    }

    /**
     * @param frame
     * @param image
     */
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

    /**
     * @return
     */
    public TrayIconPopup createPopup() {
        // TODO Auto-generated method stub
        return null;
        // return new TrayIconPopup(this) {
        //
        // /**
        // *
        // */
        // private static final long serialVersionUID = 1L;
        //
        // @Override
        // protected void init() {
        // this.setLayout(new MigLayout("ins 5,wrap 1", "[grow,fill]",
        // "[grow,fill]"));
        // add(new JButton("blabla"));
        // add(new JSeparator());
        // add(new JButton("Exit"));
        // }
        //
        // };
    }

    /**
     * 
     */
    public void displayToolTip() {
        // trayIcon.getEstimatedTopLeft();
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

    /**
     * @return
     */
    public JFrame getFrame() {
        // TODO Auto-generated method stub
        return this.frame;
    }

    /**
     * 
     */
    private void hideToolTip() {
        // TODO Auto-generated method stub

    }

    /**
     * @return
     */
    private boolean isFrameVisible() {
        // TODO Auto-generated method stub
        return this.frame.isVisible();
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.awt.event.MouseListener#mouseClicked(java.awt.event.MouseEvent)
     */
    @Override
    public void mouseClicked(final MouseEvent e) {
        // TODO Auto-generated method stub

    }

    /*
     * (non-Javadoc)
     * 
     * @see java.awt.event.MouseListener#mouseEntered(java.awt.event.MouseEvent)
     */
    @Override
    public void mouseEntered(final MouseEvent e) {
        // TODO Auto-generated method stub

    }

    /*
     * (non-Javadoc)
     * 
     * @see java.awt.event.MouseListener#mouseExited(java.awt.event.MouseEvent)
     */
    @Override
    public void mouseExited(final MouseEvent e) {
        this.hideToolTip();

    }

    /**
     * @param b
     */

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.appwork.swing.trayicon.view.TrayMouseListener#mouseStay(java.awt.
     * event.MouseEvent)
     */
    @Override
    public void mouseMoveOverTray(final MouseEvent me) {
        if ((this.trayIconPopup != null) && this.trayIconPopup.isVisible()) { return; }
        this.displayToolTip();
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.awt.event.MouseListener#mousePressed(java.awt.event.MouseEvent)
     */
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

    /*
     * (non-Javadoc)
     * 
     * @see
     * java.awt.event.MouseListener#mouseReleased(java.awt.event.MouseEvent)
     */
    @Override
    public void mouseReleased(final MouseEvent e) {
        // TODO Auto-generated method stub

    }

    /**
     * 
     */
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

    /**
     * @param icon
     */
    public void setImage(final Image icon) {
        this.trayIcon.setImage(icon);
    }

    /**
     * @param tooltip
     */
    public void setToolTip(final String tooltip) {
        this.trayIcon.setToolTip(tooltip);
    }

}
