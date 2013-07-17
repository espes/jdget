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

import java.awt.AWTException;
import java.awt.Frame;
import java.awt.Point;
import java.awt.Robot;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.WindowEvent;
import java.awt.event.WindowFocusListener;
import java.awt.event.WindowListener;
import java.awt.event.WindowStateListener;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.JFrame;
import javax.swing.Timer;

import org.appwork.utils.IO;

/**
 * @author Thomas
 * 
 */
public class WindowsWindowManager extends WindowManager {

    private Robot  robot;
    private String blocker;

    public String getBlocker() {
        return blocker;
    }

    public static void writeForegroundLockTimeout(final int foregroundLockTimeout) {
        try {

            final Process p = Runtime.getRuntime().exec("reg add \"HKEY_CURRENT_USER\\Control Panel\\Desktop\" /v \"ForegroundLockTimeout\" /t REG_DWORD /d 0x" + Integer.toHexString(foregroundLockTimeout) + " /f");
            IO.readInputStreamToString(p.getInputStream());
            final int exitCode = p.exitValue();
            if (exitCode == 0) {

            } else {
                throw new IOException("Reg add execution failed");
            }
        } catch (final UnsupportedEncodingException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (final IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

    }

    public WindowsWindowManager() {

    }

    /**
     * @return
     * @throws IOException
     * @throws UnsupportedEncodingException
     */
    public static int readForegroundLockTimeout() throws UnsupportedEncodingException, IOException {
        final String iconResult = IO.readInputStreamToString(Runtime.getRuntime().exec("reg query \"HKEY_CURRENT_USER\\Control Panel\\Desktop\" /v \"ForegroundLockTimeout\"").getInputStream());
        final Matcher matcher = Pattern.compile("ForegroundLockTimeout\\s+REG_DWORD\\s+0x(.*)").matcher(iconResult);
        matcher.find();
        final String value = matcher.group(1);
        return Integer.parseInt(value, 16);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.appwork.utils.swing.WindowManager#toFront(java.awt.Window)
     */
    @Override
    public void toFront(final Window w, final FrameState... flags) {

        final boolean requestFocus = FrameState.FOCUS.containedBy(flags);

        if (requestFocus) {
            // setAutoRequestFocus status seems to be not important because we
            // requestFocus below. we prefer to request focus, because
            // setAutoRequestFocus is java 1.7 only
            // setFocusableWindowState is important. if it would be false, the
            // window would not even go to front.

            final boolean oldFocusableWindowState = setFocusableWindowState(w, true);
            // setAlwaysOnTop is not important.

            executeAfterASecond(new ActionListener() {

                @Override
                public void actionPerformed(final ActionEvent e) {

                    setFocusableWindowState(w, oldFocusableWindowState);

                }
            });
            toFrontAltWorkaround(w, true);
            requestFocus(w);

        } else {

            final boolean oldFocusableWindowState = setFocusableWindowState(w, false);
            final boolean oldAlwaysOnTop = setAlwaysOnTop(w, true);

            executeAfterASecond(new ActionListener() {

                @Override
                public void actionPerformed(final ActionEvent e) {

                    // it is important that we
                    // 1. setAlwaysOnTop back
                    // 2. setFocusableWindowState back

                    // else setAlwaysOnTop would fire a WINDOW_ACTIVATED and a
                    // WINDOW_GAINED_FOCUS even if the window does not get
                    // active or focused
                    setAlwaysOnTop(w, oldAlwaysOnTop);
                    setFocusableWindowState(w, oldFocusableWindowState);

                }
            });
            toFrontAltWorkaround(w, false);
        }

    }

    /**
     * @param actionListener
     */
    private void executeAfterASecond(final ActionListener actionListener) {
        final Timer timer = new Timer(1000, actionListener);
        timer.setRepeats(false);
        timer.restart();

    }

    protected void toFrontAltWorkaround(final Window w, final boolean requestFocus) {
        try {

            // Workaround...

            // try {
            // Robot r = null;
            // windows can prevent windows from getting to front or getting
            // the focus. @see writeForegroundLockTimeout
            // we can write the foregroundLockTimeout, but this would need a
            // windows reboot.
            // how foregroundLockTimeout works:
            // if application A has the focus, and gets user input(mouse,
            // keyboard), windows considers this application as active. it
            // gets "Inactive" after the foregroundLockTimeout in ms.
            // when no application is considered as active, windows allows
            // other applications to get focus.
            // on xp this seems to be enabled by default.
            // on win 7 foregroundLockTimeout seems to be on 0 by default.
            // WORKAROUND:
            // windows uses alt+tab to switch between windows. so there must
            // be an windows API exception for this shortcut
            // it seems that pressing alt, while trying to switch the focus
            // works pretty well
            // Tested: WIN7
            // org.appwork.utils.swing.WindowsWindowManager.setVisible(Window,
            // boolean, boolean, boolean) method
            if (requestFocus) {
                try {
                    pressAlt();

                    toFront(w);
                    repaint(w);

                } finally {
                    releaseAlt();
                }
                try {
                    // press alt again, because one alt click focuses the menu
                    pressAlt();

                } finally {
                    releaseAlt();
                }

            } else {
                toFront(w);
                repaint(w);
            }

        } catch (final Exception e) {
            e.printStackTrace();
            toFront(w);
            repaint(w);
        }
    }

    /**
     * 
     */
    private void releaseAlt() {
        if (robot != null) {
            System.out.println("key: Alt released");
            robot.keyRelease(KeyEvent.VK_ALT);
            robot = null;
        }

    }

    /**
     * @return
     * @throws AWTException
     */
    private void pressAlt() throws AWTException {
        if (robot == null) {
            robot = new Robot();
        }
        System.out.println("key: Alt pressed");
        robot.keyPress(KeyEvent.VK_ALT);

    }

    protected void repaint(final Window w) {
        System.out.println("Call repaint ");
        // This repaint may cause a kind of flickering on some systems. we have
        // reports from windows and linux users

        // w.repaint();
    }

    protected void toFront(final Window w) {
        System.out.println("Call toFront ");
        w.toFront();
    }

    /**
     * @param w
     * @param b
     * @return
     */
    protected boolean setAlwaysOnTop(final Window w, final boolean b) {
        final boolean ret = w.isAlwaysOnTop();
        if (b == ret) { return ret; }

        blocker = "alwaysOnTop";
        try {
            System.out.println("Call setAlwaysOnTop " + b);
            w.setAlwaysOnTop(b);

        } finally {
            blocker = null;
        }
        return ret;

    }

    /*
     * (non-Javadoc)
     * 
     * @see org.appwork.utils.swing.WindowManager#setVisible(java.awt.Window,
     * boolean, boolean, boolean)
     */
    @Override
    public void setVisible(final Window w, final boolean visible, final FrameState... flags) {

        if (w.isVisible()) {
            toFront(w, flags);
            return;
        }
        final boolean requestFocus = FrameState.FOCUS.containedBy(flags);
        final boolean toFront = requestFocus || FrameState.TO_FRONT.containedBy(flags);

        System.out.println("Focus: " + requestFocus + " front: " + toFront);

        addDebugListener(w);

        if (visible) {

            final WindowResetListener listener = assignWindowOpenListener(w, flags);

            if (requestFocus) {
                setFocusableWindowState(w, true);
            } else {
                // avoid that the dialog get's focues
                setFocusableWindowState(w, false);
                setFocusable(w, false);

                if (!toFront) {
                    if (w instanceof Frame) {

                        ((Frame) w).setExtendedState(Frame.ICONIFIED);
                    } else {
                        // on some systems, the window comes to front, even of
                        // focusable and focusablewindowstate are false
                        Point offscreen = setLocation(w, listener.getOffScreenPoint());
                        listener.setOffScreenPoint(offscreen);
                    }
                    //
                }

            }

            setVisibleInternal(w, visible);

        } else {
            setVisibleInternal(w, false);
        }
        System.out.println("SetVisible Returns");
    }

    /**
     * @param w
     * @param minValue
     * @param minValue2
     * @return
     */
    private Point setLocation(Window w, Point offscreen) {

        System.out.println("call setLocation " + offscreen);
        w.setLocation(offscreen);
        return w.getLocation();
    }

    /**
     * @param w
     * @param flags
     * @return
     */
    public WindowResetListener assignWindowOpenListener(final Window w, final FrameState... flags) {
        WindowResetListener hasListener = null;
        for (final WindowListener wl : w.getWindowListeners()) {
            if (wl != null && wl instanceof WindowResetListener) {
                hasListener = (WindowResetListener) wl;
                break;
            }
        }

        if (hasListener != null) {
            hasListener.setFlags(flags);
        } else {

            w.addWindowListener(hasListener = new WindowResetListener(this, w, flags));
        }
        return hasListener;
    }

    protected void addDebugListener(final Window w) {
        w.addWindowFocusListener(new WindowFocusListener() {

            @Override
            public void windowLostFocus(final WindowEvent windowevent) {
                // TODO Auto-generated method stub
                System.out.println(windowevent);
            }

            @Override
            public void windowGainedFocus(final WindowEvent windowevent) {
                // TODO Auto-generated method stub
                System.out.println(windowevent);
            }
        });
        w.addWindowListener(new WindowListener() {

            @Override
            public void windowOpened(final WindowEvent windowevent) {
                System.out.println(windowevent);

            }

            @Override
            public void windowIconified(final WindowEvent windowevent) {
                System.out.println(windowevent);

            }

            @Override
            public void windowDeiconified(final WindowEvent windowevent) {
                System.out.println(windowevent);

            }

            @Override
            public void windowDeactivated(final WindowEvent windowevent) {
                System.out.println(windowevent);

            }

            @Override
            public void windowClosing(final WindowEvent windowevent) {
                System.out.println(windowevent);

            }

            @Override
            public void windowClosed(final WindowEvent windowevent) {
                System.out.println(windowevent);

            }

            @Override
            public void windowActivated(final WindowEvent windowevent) {
                System.out.println(windowevent);

            }
        });
        w.addWindowStateListener(new WindowStateListener() {

            @Override
            public void windowStateChanged(final WindowEvent windowevent) {
                System.out.println(windowevent);

            }
        });
        // w.addComponentListener(new ComponentListener() {
        //
        // @Override
        // public void componentShown(ComponentEvent e) {
        // System.out.println(e);
        //
        // }
        //
        // @Override
        // public void componentResized(ComponentEvent e) {
        // System.out.println(e);
        //
        // }
        //
        // @Override
        // public void componentMoved(ComponentEvent e) {
        // System.out.println(e);
        //
        // }
        //
        // @Override
        // public void componentHidden(ComponentEvent e) {
        // System.out.println(e);
        //
        // }
        // });

        // w.addHierarchyBoundsListener(new HierarchyBoundsListener() {
        //
        // @Override
        // public void ancestorResized(HierarchyEvent e) {
        // System.out.println(e);
        // }
        //
        // @Override
        // public void ancestorMoved(HierarchyEvent e) {
        // System.out.println(e);
        // }
        // });
        // w.addHierarchyListener(new HierarchyListener() {
        //
        // @Override
        // public void hierarchyChanged(HierarchyEvent e) {
        // System.out.println(e);
        //
        // }
        // });
    }

    /**
     * @param w
     * @param b
     */
    protected void setFocusable(final Window w, final boolean b) {

        blocker = "focusable";
        try {
            System.out.println("Call setFocusable " + b);

            w.setFocusable(b);

        } finally {
            blocker = null;
        }
    }

    /**
     * @param w
     * @param b
     * @return
     */
    protected boolean setFocusableWindowState(final Window w, final boolean b) {
        final boolean ret = w.getFocusableWindowState();
        if (ret == b) { return ret; }

        System.out.println("Call setFocusableWindowState " + b);
        blocker = "focusableWindowState";
        try {
            w.setFocusableWindowState(b);
        } finally {
            blocker = null;
        }
        return ret;
    }

    /**
     * @param w
     */
    protected void toBack(final Window w) {
        System.out.println("Call toBack ");
        w.toBack();

    }

    /**
     * @param w
     * @param b
     */
    private void setVisibleInternal(final Window w, final boolean b) {

        blocker = "visible";
        try {
            System.out.println("Call setVisible " + b);
            w.setVisible(b);
        } finally {
            blocker = null;
        }

    }

    /**
     * @param w
     */
    private void requestFocus(final Window w) {
        // if a frame is active, one of its components has the focus, if we da a
        // requestfocus now, these components will lose the focus again
        if (w.getFocusOwner() != null || w.isFocusOwner()) { return; }

        System.out.println("Call requestFocus");
        w.requestFocus();
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.appwork.utils.swing.WindowManager#show(java.awt.Window,
     * org.appwork.utils.swing.WindowManager.WindowState[])
     */
    @Override
    public void show(final Window w, final FrameState... flags) {
        setVisible(w, true, flags);

    }

    /*
     * (non-Javadoc)
     * 
     * @see org.appwork.utils.swing.WindowManager#hide(java.awt.Window,
     * org.appwork.utils.swing.WindowManager.WindowState[])
     */
    @Override
    public void hide(final Window w, final FrameState... flags) {
        setVisible(w, false, flags);

    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.appwork.utils.swing.WindowManager#setWindowExtendedState(javax.swing
     * .JFrame, org.appwork.utils.swing.WindowManager.WindowExtendedState)
     */
    @Override
    public void setExtendedState(final Frame w, final WindowExtendedState state) {
        if (state == null) { throw new NullPointerException("State is null"); }
        switch (state) {
        case NORMAL:
            w.setExtendedState(JFrame.NORMAL);
        }

    }
}
