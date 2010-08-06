//    jDownloader - Downloadmanager
//    Copyright (C) 2008  JD-Team support@jdownloader.org
//
//    This program is free software: you can redistribute it and/or modify
//    it under the terms of the GNU General Public License as published by
//    the Free Software Foundation, either version 3 of the License, or
//    (at your option) any later version.
//
//    This program is distributed in the hope that it will be useful,
//    but WITHOUT ANY WARRANTY; without even the implied warranty of
//    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
//    GNU General Public License for more details.
//
//    You should have received a copy of the GNU General Public License
//    along with this program.  If not, see <http://www.gnu.org/licenses/>.

package org.appwork.swing.trayicon;

import java.awt.Dimension;
import java.awt.MouseInfo;
import java.awt.Point;
import java.awt.PointerInfo;
import java.awt.Toolkit;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import javax.swing.JWindow;

import org.appwork.utils.os.CrossSystem;
import org.appwork.utils.swing.EDTHelper;

public abstract class TrayIconPopup extends JWindow {

    /**
     * 
     */
    private static final long serialVersionUID = -6632773260637754243L;
    private boolean enteredPopup;
    private transient final Thread hideThread;

    private boolean hideThreadrunning = false;

    /**
     * @param awTrayIcon
     */
    public TrayIconPopup(final AWTrayIcon awTrayIcon) {
        super(awTrayIcon.getFrame());

        this.init();
        this.setAlwaysOnTop(true);
        this.pack();
        this.addMouseListener(new MouseListener() {
            public void mouseClicked(final MouseEvent e) {
            }

            public void mouseEntered(final MouseEvent e) {
                TrayIconPopup.this.enteredPopup = true;
            }

            public void mouseExited(final MouseEvent e) {
            }

            public void mousePressed(final MouseEvent e) {
            }

            public void mouseReleased(final MouseEvent e) {
            }
        });
        this.hideThread = new Thread() {
            /*
             * this thread handles closing of popup because enter/exit/move
             * events are too slow and can miss the exitevent
             */
            @Override
            public void run() {
                while (true && TrayIconPopup.this.hideThreadrunning) {
                    try {
                        Thread.sleep(500);
                    } catch (final InterruptedException e) {
                    }
                    if (TrayIconPopup.this.enteredPopup && TrayIconPopup.this.hideThreadrunning) {
                        final PointerInfo mouse = MouseInfo.getPointerInfo();
                        final Point current = TrayIconPopup.this.getLocation();
                        if ((mouse.getLocation().x < current.x) || (mouse.getLocation().x > current.x + TrayIconPopup.this.getSize().width)) {
                            TrayIconPopup.this.dispose();
                            break;
                        } else if ((mouse.getLocation().y < current.y) || (mouse.getLocation().y > current.y + TrayIconPopup.this.getSize().height)) {
                            TrayIconPopup.this.dispose();
                            break;
                        }
                    }
                }
            }
        };
        this.hideThreadrunning = true;
        this.hideThread.start();
    }

    /**
     * layout window
     */
    abstract protected void init();

    /**
     * @param point
     */
    public void setPosition(final Point p) {

        final Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        final int limitX = (int) screenSize.getWidth() / 2;
        final int limitY = (int) screenSize.getHeight() / 2;
        if (!CrossSystem.isMac()) {
            if (p.x <= limitX) {
                if (p.y <= limitY) {
                    // top left
                    this.setLocation(p.x, p.y);
                } else {
                    // bottom left
                    this.setLocation(p.x, p.y - this.getHeight());
                }
            } else {
                if (p.y <= limitY) {
                    // top right
                    this.setLocation(p.x - this.getWidth(), p.y);
                } else {
                    // bottom right
                    this.setLocation(p.x - this.getWidth(), p.y - this.getHeight());
                }
            }
        } else {
            if (p.getX() <= (screenSize.getWidth() - this.getWidth())) {
                this.setLocation((int) p.getX(), 22);
            } else {
                this.setLocation(p.x - this.getWidth(), 22);
            }
        }

    }

    /**
     * start autohide in 3 secs if mouse did not enter popup before
     */
    public void startAutoHide() {
        new Thread() {
            @Override
            public void run() {
                try {
                    Thread.sleep(3000);
                } catch (final InterruptedException e) {
                }
                if (!TrayIconPopup.this.enteredPopup) {
                    new EDTHelper<Object>() {
                        @Override
                        public Object edtRun() {
                            TrayIconPopup.this.hideThreadrunning = false;
                            TrayIconPopup.this.dispose();
                            return null;
                        }

                    }.start();
                }
            }
        }.start();
    }

}