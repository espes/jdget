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
    private boolean hideThreadrunning = false;

    private transient Thread hideThread;

    /**
     * @param awTrayIcon
     */
    public TrayIconPopup(AWTrayIcon awTrayIcon) {
        super(awTrayIcon.getFrame());

        this.init();
        setAlwaysOnTop(true);
        pack();
        this.addMouseListener(new MouseListener() {
            public void mouseClicked(MouseEvent e) {
            }

            public void mouseEntered(MouseEvent e) {
                enteredPopup = true;
            }

            public void mouseExited(MouseEvent e) {
            }

            public void mousePressed(MouseEvent e) {
            }

            public void mouseReleased(MouseEvent e) {
            }
        });
        hideThread = new Thread() {
            /*
             * this thread handles closing of popup because enter/exit/move
             * events are too slow and can miss the exitevent
             */
            public void run() {
                while (true && hideThreadrunning) {
                    try {
                        sleep(500);
                    } catch (InterruptedException e) {
                    }
                    if (enteredPopup && hideThreadrunning) {
                        PointerInfo mouse = MouseInfo.getPointerInfo();
                        Point current = TrayIconPopup.this.getLocation();
                        if (mouse.getLocation().x < current.x || mouse.getLocation().x > current.x + TrayIconPopup.this.getSize().width) {
                            dispose();
                            break;
                        } else if (mouse.getLocation().y < current.y || mouse.getLocation().y > current.y + TrayIconPopup.this.getSize().height) {
                            dispose();
                            break;
                        }
                    }
                }
            }
        };
        hideThreadrunning = true;
        hideThread.start();
    }

    /**
     * layout window
     */
    abstract protected void init();

    /**
     * start autohide in 3 secs if mouse did not enter popup before
     */
    public void startAutoHide() {
        new Thread() {
            @Override
            public void run() {
                try {
                    Thread.sleep(3000);
                } catch (InterruptedException e) {
                }
                if (!enteredPopup) {
                    new EDTHelper<Object>() {
                        @Override
                        public Object edtRun() {
                            hideThreadrunning = false;
                            dispose();
                            return null;
                        }

                    }.start();
                }
            }
        }.start();
    }

    /**
     * @param point
     */
    public void setPosition(Point p) {

        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        int limitX = (int) screenSize.getWidth() / 2;
        int limitY = (int) screenSize.getHeight() / 2;
        if (!CrossSystem.isMac()) {
            if (p.x <= limitX) {
                if (p.y <= limitY) {
                    // top left
                    setLocation(p.x, p.y);
                } else {
                    // bottom left
                    setLocation(p.x, p.y - getHeight());
                }
            } else {
                if (p.y <= limitY) {
                    // top right
                    setLocation(p.x - getWidth(), p.y);
                } else {
                    // bottom right
                    setLocation(p.x - getWidth(), p.y - getHeight());
                }
            }
        } else {
            if (p.getX() <= (screenSize.getWidth() - getWidth())) {
                setLocation((int) p.getX(), 22);
            } else {
                setLocation(p.x - getWidth(), 22);
            }
        }

    }

}