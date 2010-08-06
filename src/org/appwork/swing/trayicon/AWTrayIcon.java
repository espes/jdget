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

    private ExtTrayIcon trayIcon;

    private TrayIconPopup trayIconPopup;

    private JFrame frame;

    private int visibleToggleClickCount = 2;

    public AWTrayIcon(JFrame frame) {

        this(frame, (frame.getIconImages() == null || frame.getIconImages().size() == 0) ? ImageProvider.createIcon((frame.getTitle() != null && frame.getTitle().length() > 0) ? frame.getTitle().charAt(0) + "" : "T", 32, 32) : frame.getIconImages().get(0));

    }

    /**
     * @param frame
     * @param image
     */
    public AWTrayIcon(JFrame frame, Image icon) {
        this.frame = frame;

        SystemTray systemTray = SystemTray.getSystemTray();
        /*
         * trayicon message must be set, else windows cannot handle icon right
         * (eg autohide feature)
         */
        trayIcon = new ExtTrayIcon(icon, frame.getTitle());

        trayIcon.addMouseListener(this);
        trayIcon.addTrayMouseListener(this);

        try {
            systemTray.add(trayIcon);
        } catch (Exception e) {
            Log.exception(e);
        }
    }

    /**
     * @param tooltip
     */
    public void setToolTip(String tooltip) {
        trayIcon.setToolTip(tooltip);
    }

    /**
     * @param icon
     */
    public void setImage(Image icon) {
        trayIcon.setImage(icon);
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.awt.event.MouseListener#mouseClicked(java.awt.event.MouseEvent)
     */
    @Override
    public void mouseClicked(MouseEvent e) {
        // TODO Auto-generated method stub

    }

    /*
     * (non-Javadoc)
     * 
     * @see java.awt.event.MouseListener#mouseEntered(java.awt.event.MouseEvent)
     */
    @Override
    public void mouseEntered(MouseEvent e) {
        // TODO Auto-generated method stub

    }

    /*
     * (non-Javadoc)
     * 
     * @see java.awt.event.MouseListener#mouseExited(java.awt.event.MouseEvent)
     */
    @Override
    public void mouseExited(MouseEvent e) {
        this.hideToolTip();

    }

    public void dispose() {
        try {
            if (trayIcon != null) {

                SystemTray.getSystemTray().remove(trayIcon);
                trayIcon.removeMouseListener(this);
                trayIcon.removeTrayMouseListener(this);
                trayIcon=null;

            }
        } catch (Exception e) {
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.awt.event.MouseListener#mousePressed(java.awt.event.MouseEvent)
     */
    @Override
    public void mousePressed(MouseEvent e) {
        this.hideToolTip();
        System.out.println(e);
        if (e.getSource() instanceof TrayIcon) {
            if (!CrossSystem.isMac()) {
                if (e.getClickCount() >= this.visibleToggleClickCount && !SwingUtilities.isRightMouseButton(e)) {
                    this.setFrameVisible(!isFrameVisible());
                } else {
                    if (trayIconPopup != null && trayIconPopup.isShowing()) {
                        trayIconPopup.dispose();
                        trayIconPopup = null;
                    } else if (SwingUtilities.isRightMouseButton(e)) {

                        trayIconPopup = this.createPopup();
                        if (trayIconPopup == null) return;
                        trayIconPopup.setPosition(e.getPoint());
                        trayIconPopup.setVisible(true);
                        trayIconPopup.startAutoHide();
                    }
                }
            } else {
                if (SwingUtilities.isLeftMouseButton(e)) {
                    if (e.getClickCount() >= visibleToggleClickCount && !SwingUtilities.isLeftMouseButton(e)) {
                        this.setFrameVisible(!isFrameVisible());
                    } else {
                        if (trayIconPopup != null && trayIconPopup.isShowing()) {
                            trayIconPopup.dispose();
                            trayIconPopup = null;
                        } else if (SwingUtilities.isLeftMouseButton(e)) {

                            trayIconPopup = this.createPopup();
                            if (trayIconPopup == null) return;
                            Point pointOnScreen = e.getLocationOnScreen();
                            if (e.getX() > 0) pointOnScreen.x -= e.getPoint().x;
                            trayIconPopup.setPosition(pointOnScreen);
                            trayIconPopup.setVisible(true);
                            trayIconPopup.startAutoHide();
                        }
                    }
                }
            }
        }
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
     * @param b
     */

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
                    frame.setVisible(false);
                    resetAlwaysOnTop = null;
                } else {
                    if (!frame.isAlwaysOnTop()) {
                        resetAlwaysOnTop = frame;
                    } else {
                        resetAlwaysOnTop = null;
                    }
                    frame.setAlwaysOnTop(true);
                    frame.setVisible(true);
                    frame.toFront();
                }

                if (visible && resetAlwaysOnTop != null) {
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
     * @return
     */
    private boolean isFrameVisible() {
        // TODO Auto-generated method stub
        return frame.isVisible();
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * java.awt.event.MouseListener#mouseReleased(java.awt.event.MouseEvent)
     */
    @Override
    public void mouseReleased(MouseEvent e) {
        // TODO Auto-generated method stub

    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.appwork.swing.trayicon.view.TrayMouseListener#mouseStay(java.awt.
     * event.MouseEvent)
     */
    @Override
    public void mouseMoveOverTray(MouseEvent me) {
        if (trayIconPopup != null && trayIconPopup.isVisible()) return;
        displayToolTip();
    }

    /**
     * 
     */
    public void displayToolTip() {
        // trayIcon.getEstimatedTopLeft();
    }

    /**
     * @return
     */
    public JFrame getFrame() {
        // TODO Auto-generated method stub
        return frame;
    }

}
