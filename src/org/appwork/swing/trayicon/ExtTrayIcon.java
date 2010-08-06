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

import java.awt.Component;
import java.awt.Dimension;
import java.awt.Image;
import java.awt.MouseInfo;
import java.awt.Point;
import java.awt.TrayIcon;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.util.ArrayList;

/**
 * @author thomas
 * 
 */
public class ExtTrayIcon extends TrayIcon implements MouseListener, MouseMotionListener {

    private ArrayList<MouseListener> mouseListeners;
    private ArrayList<TrayMouseListener> traymouseListeners;

    /**
     * @param icon
     * @param title
     */
    public ExtTrayIcon(Image icon, String title) {
        super(icon, title);
        this.setImageAutoSize(true);
        mouseListeners = new ArrayList<MouseListener>();
        traymouseListeners = new ArrayList<TrayMouseListener>();
        super.addMouseListener(this);
        super.addMouseMotionListener(this);
    }

    private boolean mouseover;
    private Thread mouseLocationObserver;
    // private TrayIcon trayIcon;
    private Point min;
    private Point max;
    private Dimension size;
    private MouseEvent lastEvent;
    private Component dummy;

    private static int TOOLTIP_DELAY = 1000;

    public void removeMouseListener(MouseListener listener) {
        if (listener == null) { return; }
        synchronized (this.mouseListeners) {
            this.mouseListeners.remove(listener);
        }
    }

    public void addMouseListener(MouseListener listener) {
        if (listener == null) { return; }
        synchronized (this.mouseListeners) {
            this.mouseListeners.add(listener);
        }

    }

    public void mouseClicked(MouseEvent e) {
        synchronized (this.mouseListeners) {
            for (MouseListener l : mouseListeners) {
                l.mouseClicked(e);
            }
        }
    }

    public void removeTrayMouseListener(TrayMouseListener listener) {
        if (listener == null) { return; }
        synchronized (this.traymouseListeners) {
            this.traymouseListeners.remove(listener);
        }
    }

    public void addTrayMouseListener(TrayMouseListener listener) {
        if (listener == null) { return; }
        synchronized (this.traymouseListeners) {
            this.traymouseListeners.add(listener);
        }

    }

    public void mouseEntered(MouseEvent e) {
        mouseover = true;
        final long enterTime = System.currentTimeMillis();
        mouseLocationObserver = new Thread() {
            public void run() {
                try {
                    boolean mouseStay = false;
                    while (true) {
                        Point point = MouseInfo.getPointerInfo().getLocation();
                        if (!isOver(point)) {
                            MouseEvent me;
                            me = new MouseEvent(dummy, 0, System.currentTimeMillis(), 0, point.x, point.y, 0, false);
                            me.setSource(lastEvent.getSource());

                            synchronized (mouseListeners) {
                                for (MouseListener l : mouseListeners) {
                                    l.mouseExited(me);
                                }
                            }
                            return;

                        } else {
                            if ((System.currentTimeMillis() - enterTime) >= TOOLTIP_DELAY && !mouseStay) {
                                mouseStay = true;
                                MouseEvent me;
                                me = new MouseEvent(dummy, 0, System.currentTimeMillis(), 0, point.x, point.y, 0, false);
                                // me.setSource(MouseAdapter.this);
                                // deligate.mouseStay(me);

                                synchronized (traymouseListeners) {
                                    for (TrayMouseListener l : traymouseListeners) {
                                        l.mouseMoveOverTray(me);
                                    }
                                }

                            }
                        }

                        Thread.sleep(100);
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                    return;
                } finally {
                    mouseLocationObserver = null;
                }
            }

        };
        mouseLocationObserver.start();

    }

    public void mouseExited(MouseEvent e) {
        mouseover = false;

        min = max = null;

        synchronized (mouseListeners) {
            for (MouseListener l : mouseListeners) {
                l.mouseExited(e);
            }
        }

    }

    public void mousePressed(MouseEvent e) {
       
        synchronized (mouseListeners) {
            for (MouseListener l : mouseListeners) {
                l.mousePressed(e);
            }
        }

    }

    public void mouseReleased(MouseEvent e) {
        synchronized (mouseListeners) {
            for (MouseListener l : mouseListeners) {
                l.mouseReleased(e);
            }
        }
    }

    public void mouseMoved(MouseEvent e) {
        lastEvent = e;
        /**
         * the more the user moves over the tray, the better we know it's
         * location *
         */

        if (this.min == null) {
            this.min = new Point(e.getPoint().x, e.getPoint().y);
            this.max = new Point(e.getPoint().x, e.getPoint().y);
        } else {
            min.x = Math.min(e.getPoint().x, min.x);
            min.y = Math.min(e.getPoint().y, min.y);
            max.x = Math.max(e.getPoint().x, max.x);
            max.y = Math.max(e.getPoint().y, max.y);
            // System.out.println(min+" - "+max);
        }

        if (!this.mouseover) {

            synchronized (mouseListeners) {
                for (MouseListener l : mouseListeners) {
                    l.mouseEntered(e);
                }
            }
        } else {

            // deligate.mouseMoved(e);
        }

    }

    public Point getEstimatedTopLeft() {
        int midx = (max.x + min.x) / 2;
        int midy = (max.y + min.y) / 2;

        return new Point(midx - size.width / 2, midy - size.height / 2);
    }

    /**
     * Passt die iconsize in die festgestellte geschätzte position ein. und
     * prüft ob point darin ist
     * 
     * @param point
     * @return
     */
    protected boolean isOver(Point point) {
        int midx = (max.x + min.x) / 2;
        int midy = (max.y + min.y) / 2;

        int width = Math.min(size.width, max.x - min.x);
        int height = Math.min(size.height, max.y - min.y);

        int minx = midx - width / 2;
        int miny = midy - height / 2;
        int maxx = midx + width / 2;
        int maxy = midy + height / 2;
        // java.awt.Point[x=1274,y=1175] - java.awt.Point[x=1309,y=1185]
        if (point.x >= minx && point.x <= maxx) {
            if (point.y >= miny && point.y <= maxy) { return true; }

        }
        return false;
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * java.awt.event.MouseMotionListener#mouseDragged(java.awt.event.MouseEvent
     * )
     */
    @Override
    public void mouseDragged(MouseEvent e) {
        // TODO Auto-generated method stub

    }

}
