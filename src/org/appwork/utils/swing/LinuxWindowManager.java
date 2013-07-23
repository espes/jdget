/**
 * Copyright (c) 2009 - 2013 AppWork UG(haftungsbeschränkt) <e-mail@appwork.org>
 * 
 * This file is part of org.appwork.utils.swing
 * 
 * This software is licensed under the Artistic License 2.0,
 * see the LICENSE file or http://www.opensource.org/licenses/artistic-license-2.0.php
 * for details
 */
package org.appwork.utils.swing;

import java.awt.Window;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.util.Timer;
import java.util.TimerTask;
import java.util.WeakHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import javax.swing.SwingUtilities;

/**
 * @author daniel
 * 
 */
public class LinuxWindowManager extends WindowManager {

    private abstract class LinuxWindowListener implements WindowListener {

        private final AtomicBoolean removed = new AtomicBoolean(false);

        public boolean remove(final Window w, final boolean undo) {
            if (this.removed.getAndSet(true) == false) {
                w.removeWindowListener(this);
                if (undo) {
                    this.undo(w);
                }
                return true;
            }
            return false;
        }

        abstract public void undo(Window w);

        @Override
        public void windowActivated(final WindowEvent e) {
        }

        @Override
        public void windowClosed(final WindowEvent e) {
        }

        @Override
        public void windowClosing(final WindowEvent e) {
        }

        @Override
        public void windowDeactivated(final WindowEvent e) {
        }

        @Override
        public void windowDeiconified(final WindowEvent e) {
        }

        @Override
        public void windowIconified(final WindowEvent e) {
        }

        @Override
        public void windowOpened(final WindowEvent e) {
        }

    }

    private class LinuxWindowListenerMap {
        private final AtomicReference<LinuxWindowListener> toFront = new AtomicReference<LinuxWindowListener>(null);
    }

    private final Timer                                       timer             = new Timer("LinuxWindowManager", true);
    private boolean                                           debugFlag         = true;
    private final int                                         toBackTimer       = 100;
    private final int                                         toFrontFocusTimer = 100;
    private final int                                         toFrontTimer      = 100;
    private boolean                                           useAlwaysOnTop    = false;

    private final WeakHashMap<Window, LinuxWindowListenerMap> windowListeners   = new WeakHashMap<Window, LinuxWindowListenerMap>();

    public boolean isDebugFlag() {
        return this.debugFlag;
    }

    /**
     * @return the useAlwaysOnTop
     */
    public boolean isUseAlwaysOnTop() {
        return this.useAlwaysOnTop;
    }

    public void setDebugFlag(final boolean debugFlag) {
        this.debugFlag = debugFlag;
    }

    /**
     * @param useAlwaysOnTop
     *            the useAlwaysOnTop to set
     */
    public void setUseAlwaysOnTop(final boolean useAlwaysOnTop) {
        this.useAlwaysOnTop = useAlwaysOnTop;
    }

    @Override
    public void setVisible(final Window w, final boolean visible, final FrameState state) {
        if (visible == false) {
            w.setVisible(false);
        } else {
            this.setZState(w, state, true);
        }
    }

    @Override
    public void setZState(final Window w, final FrameState state) {
        this.setZState(w, state, false);
    }

    public void setZState(final Window w, final FrameState state, final boolean makeVisible) {
        if (makeVisible == false && w.isVisible() == false) { return; }
        LinuxWindowListenerMap listenerMap = this.windowListeners.get(w);
        if (listenerMap == null) {
            listenerMap = new LinuxWindowListenerMap();
            this.windowListeners.put(w, listenerMap);
        }
        LinuxWindowListener listener = null;
        switch (state) {
        case TO_BACK:
            listener = listenerMap.toFront.getAndSet(null);
            if (listener != null) {
                if (listener.remove(w, true) && this.isDebugFlag()) {
                    System.out.println("Remove previous toFront listener(undo): " + listener);
                }
            }
            /* we set window invisible */
            w.setVisible(false);
            /* toBack should not steal focus */
            w.setFocusable(false);
            w.setFocusableWindowState(false);
            /* make window visible again and put to back */
            w.setVisible(true);
            w.toBack();
            if (this.isDebugFlag()) {
                System.out.println("Request toBack(use focusable false and invisible/visible workaround): " + w);
            }
            /* delay restoring of focusable properties */
            this.timer.schedule(new TimerTask() {

                @Override
                public void run() {
                    SwingUtilities.invokeLater(new Runnable() {

                        @Override
                        public void run() {
                            if (LinuxWindowManager.this.isDebugFlag()) {
                                System.out.println("Request toBack(timer,restore focusable true): " + w);
                            }
                            w.setFocusable(true);
                            w.setFocusableWindowState(true);
                        }
                    });
                }
            }, this.toBackTimer);
            break;
        case TO_FRONT:
            listener = listenerMap.toFront.getAndSet(null);
            if (listener != null) {
                if (listener.remove(w, false) && this.isDebugFlag()) {
                    System.out.println("Remove previous toFront listener(no undo): " + listener);
                }
            }

            if (!w.isActive()) {
                if (this.isUseAlwaysOnTop()) {
                    if (this.isDebugFlag()) {
                        System.out.println("Request toFront(use AlwaysOnTop workaround): " + w);
                    }
                    /* window is not active, so we need to bring it to top */
                    listener = new LinuxWindowListener() {

                        @Override
                        public void undo(final Window w) {
                            System.out.println("Request toFront(reset AlwaysOnTop workaround): " + w);
                            w.setAlwaysOnTop(false);
                        }

                        @Override
                        public void windowActivated(final WindowEvent e) {
                            this.remove(e.getWindow(), true);
                        }

                        @Override
                        public void windowIconified(final WindowEvent e) {
                            this.remove(e.getWindow(), true);
                        }
                    };
                    listenerMap.toFront.set(listener);
                    w.addWindowListener(listener);
                    /* force always on top, but does not autosteal focus */
                    w.setAlwaysOnTop(true);
                    if (w.isVisible() == false) {
                        w.setVisible(true);
                    }
                } else {
                    if (this.isDebugFlag()) {
                        System.out.println("Request toFront(use toFront): " + w);
                    }
                    /* toFront should not steal focus */
                    w.setFocusable(false);
                    w.setFocusableWindowState(false);
                    if (w.isVisible()) {
                        w.toFront();
                    } else {
                        w.setVisible(true);
                        w.toFront();
                    }
                    /* delay restoring of focusable properties */
                    this.timer.schedule(new TimerTask() {

                        @Override
                        public void run() {
                            SwingUtilities.invokeLater(new Runnable() {

                                @Override
                                public void run() {
                                    if (LinuxWindowManager.this.isDebugFlag()) {
                                        System.out.println("Request toFront(timer,restore focusable true): " + w);
                                    }
                                    w.setFocusable(true);
                                    w.setFocusableWindowState(true);
                                }
                            });
                        }
                    }, this.toFrontTimer);
                }
            } else {
                if (this.isDebugFlag()) {
                    System.out.println("Request toFront(window is active): " + w);
                }
            }
            break;
        case TO_FRONT_FOCUSED:
            this.setZState(w, FrameState.TO_FRONT, makeVisible);
            /* delay stealing focus */
            this.timer.schedule(new TimerTask() {

                @Override
                public void run() {
                    SwingUtilities.invokeLater(new Runnable() {

                        public void run() {
                            if (LinuxWindowManager.this.isDebugFlag()) {
                                System.out.println("Request toFrontFocused(timer,toFront): " + w);
                            }
                            w.toFront();
                            w.requestFocus();
                        }

                    });
                }
            }, this.toFrontFocusTimer);
            break;
        case OS_DEFAULT:
            if (w.isVisible() == false) {
                w.setVisible(true);
            }
            break;
        default:
            break;
        }
    }
}
