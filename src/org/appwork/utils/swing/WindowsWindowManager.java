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
import java.awt.Component;
import java.awt.Frame;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Robot;
import java.awt.Window;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.WindowEvent;
import java.awt.event.WindowFocusListener;
import java.awt.event.WindowListener;
import java.awt.event.WindowStateListener;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.Timer;

import org.appwork.swing.ExtJFrame;
import org.appwork.utils.IO;
import org.appwork.utils.StringUtils;

/**
 * @author Thomas
 * 
 */
public class WindowsWindowManager extends WindowManager {

    private Robot                          robot;
    private String                         blocker;
    private HashMap<Window, ResetRunnable> runnerMap;

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

        runnerMap = new HashMap<Window, ResetRunnable>();
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
    public void setZState(final Window w, final FrameState state) {

        switch (state) {
        case OS_DEFAULT:
            // do nothing
            return;

        case TO_FRONT_FOCUSED:

            // setAutoRequestFocus status seems to be not important because we
            // requestFocus below. we prefer to request focus, because
            // setAutoRequestFocus is java 1.7 only
            // setFocusableWindowState is important. if it would be false, the
            // window would not even go to front.
            if (w.hasFocus()) { return; }
            // if (true) { return; }
            WindowResetListener hasListener = findListener(w);

            ResetRunnable runner = runnerMap.get(w);
            if (runner == null) {
                runner = new ResetRunnable(this, w, hasListener);
                runnerMap.put(w, runner);
                executeAfterASecond(runner);
            }
            runner.setState(state);

            setFocusableWindowState(w, true);
            setFocusable(w, true);

            toFrontAltWorkaround(w, true);

            requestFocus(w);
            break;
        default:
            hasListener = findListener(w);

            runner = runnerMap.get(w);
            if (runner == null) {
                runner = new ResetRunnable(this, w, hasListener);
                runnerMap.put(w, runner);
                executeAfterASecond(runner);
            }
            runner.setState(state);

            setFocusableWindowState(w, false);
            setAlwaysOnTop(w, true);

            toFrontAltWorkaround(w, false);
        }

    }

    /**
     * @param actionListener
     */
    private void executeAfterASecond(final ActionListener actionListener) {
        System.out.println("Launch timer");
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

            // actually, we only need to asure that alt is pressed during
            // tofront
            // this would activte the window context menu.
            // that's why we use alt+ctrl+shit+ü - let's hope that no
            // application uses this shortcut. else we would trigger it when
            // bringing a window to front.
            // we probably need this workaround only if foregroundtimeoutlock
            // is>0
            robot.keyRelease(KeyEvent.VK_ALT);
            robot.keyRelease(KeyEvent.VK_SHIFT);
            robot.keyRelease(KeyEvent.VK_CONTROL);
            // ü
            robot.keyRelease(65);
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
        // actually, we only need to asure that alt is pressed during tofront
        // this would activte the window context menu.
        // that's why we use alt+ctrl+shit+ü - let's hope that no application
        // uses this shortcut. else we would trigger it when bringing a window
        // to front.
        // we probably need this workaround only if foregroundtimeoutlock is>0
        robot.keyPress(KeyEvent.VK_ALT);
        robot.keyPress(KeyEvent.VK_SHIFT);
        robot.keyPress(KeyEvent.VK_CONTROL);
        // ü
        robot.keyPress(65);

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

        blocker = ExtJFrame.PROPERTY_ALWAYS_ON_TOP;
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
    public void setVisible(final Window w, final boolean visible, final FrameState state) {

        if (w.isVisible()) {
            setZState(w, state);
            return;
        }

        System.out.println("Focus: " + state);

        addDebugListener(w);

        if (visible) {
            if (state == FrameState.OS_DEFAULT) {
                setVisibleInternal(w, visible);
                return;
            }
            final WindowResetListener listener = assignWindowOpenListener(w, state);

            switch (state) {
            case TO_FRONT_FOCUSED:

                setFocusableWindowState(w, true);
                break;

            default:

                // avoid that the dialog get's focues
                setFocusableWindowState(w, false);
                setFocusable(w, false);

                if (state != FrameState.TO_FRONT) {
                    if (w instanceof Frame && false) {

                        setExtendedState(((Frame) w), Frame.ICONIFIED);
                    } else {
                        // on some systems, the window comes to front, even of
                        // focusable and focusablewindowstate are false
                        final GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
                        final GraphicsDevice[] screens = ge.getScreenDevices();

                        final Point p = new Point(Integer.MIN_VALUE, Integer.MIN_VALUE);
                        // search offscreen position
                        for (final GraphicsDevice screen : screens) {
                            final Rectangle bounds = screen.getDefaultConfiguration().getBounds();
                            p.x = Math.max(bounds.x + bounds.width, p.x);
                            p.y = Math.max(bounds.y + bounds.height, p.y);
                        }
                        p.x++;
                        p.y++;
                        setLocation(w, p);

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
    private Point setLocation(final Window w, final Point offscreen) {

        blocker = ExtJFrame.PROPERTY_LOCATION;
        try {
            System.out.println("call setLocation " + offscreen);
            w.setLocation(offscreen);

        } finally {
            blocker = null;
        }
        return w.getLocation();
    }

    /**
     * @param w
     * @param frameExtendedState
     */
    protected void setExtendedState(final Frame w, final int frameExtendedState) {
        blocker = ExtJFrame.PROPERTY_EXTENDED_STATE;
        try {
            if (frameExtendedState == w.getExtendedState()) { return; }
            System.out.println("Call setExtendedState " + frameExtendedState);

            w.setExtendedState(frameExtendedState);

        } finally {
            blocker = null;
        }

    }

    /**
     * @param w
     * @param flags
     * @return
     */
    public WindowResetListener assignWindowOpenListener(final Window w, final FrameState state) {
        WindowResetListener hasListener = findListener(w);

        if (hasListener != null) {
            hasListener.setState(state);
        } else {

            hasListener = new WindowResetListener(this, w, state);
            hasListener.add();
        }
        return hasListener;
    }

    protected WindowResetListener findListener(final Window w) {
        WindowResetListener hasListener = null;
        for (final WindowListener wl : w.getWindowListeners()) {
            if (wl != null && wl instanceof WindowResetListener) {
                hasListener = (WindowResetListener) wl;
                break;
            }
        }
        return hasListener;
    }

    protected void addDebugListener(final Window w) {
        w.addWindowFocusListener(new WindowFocusListener() {

            @Override
            public void windowLostFocus(final WindowEvent windowevent) {
                // TODO Auto-generated method stub
                log(windowevent);
            }

            @Override
            public void windowGainedFocus(final WindowEvent windowevent) {
                // TODO Auto-generated method stub
                log(windowevent);
            }
        });
        w.addWindowListener(new WindowListener() {

            @Override
            public void windowOpened(final WindowEvent windowevent) {
                log(windowevent);

            }

            @Override
            public void windowIconified(final WindowEvent windowevent) {
                log(windowevent);

            }

            @Override
            public void windowDeiconified(final WindowEvent windowevent) {
                log(windowevent);

            }

            @Override
            public void windowDeactivated(final WindowEvent windowevent) {
                log(windowevent);

            }

            @Override
            public void windowClosing(final WindowEvent windowevent) {
                log(windowevent);

            }

            @Override
            public void windowClosed(final WindowEvent windowevent) {
                log(windowevent);

            }

            @Override
            public void windowActivated(final WindowEvent windowevent) {
                log(windowevent);

            }
        });
        w.addWindowStateListener(new WindowStateListener() {

            @Override
            public void windowStateChanged(final WindowEvent windowevent) {
                log(windowevent);

            }
        });
        // w.addComponentListener(new ComponentListener() {
        //
        // @Override
        // public void componentShown(final ComponentEvent e) {
        // System.out.println(e);
        //
        // }
        //
        // @Override
        // public void componentResized(final ComponentEvent e) {
        // System.out.println(e);
        //
        // }
        //
        // @Override
        // public void componentMoved(final ComponentEvent e) {
        // System.out.println(e);
        //
        // }
        //
        // @Override
        // public void componentHidden(final ComponentEvent e) {
        // System.out.println(e);
        //
        // }
        // });
        //
        // w.addHierarchyBoundsListener(new HierarchyBoundsListener() {
        //
        // @Override
        // public void ancestorResized(final HierarchyEvent e) {
        // System.out.println(e);
        // }
        //
        // @Override
        // public void ancestorMoved(final HierarchyEvent e) {
        // System.out.println(e);
        // }
        // });
        // w.addHierarchyListener(new HierarchyListener() {
        //
        // @Override
        // public void hierarchyChanged(final HierarchyEvent e) {
        // System.out.println(e);
        //
        // }
        // });
    }

    /**
     * @param windowevent
     */
    protected void log(final WindowEvent windowevent) {
        System.out.println(name(windowevent.getWindow()) + "." + name(windowevent.getComponent()) + " " + type(windowevent.getID()) + " to " + name(windowevent.getOppositeWindow()));

    }

    /**
     * @param window
     * @return
     */
    private String name(final Component window) {
        if (window == null) { return null; }
        String ret = window.getName();
        if (StringUtils.isEmpty(ret)) {
            ret = window.getClass().getName();
        }
        return ret;
    }

    /**
     * @param id
     * @return
     */
    private String type(final int id) {

        /* 389 */switch (id) {

        /* 391 */case 200: /* 391 */
            return "WINDOW_OPENED";

            /* 394 */case 201: /* 394 */
            return "WINDOW_CLOSING";

            /* 397 */case 202: /* 397 */
            return "WINDOW_CLOSED";

            /* 400 */case 203: /* 400 */
            return "WINDOW_ICONIFIED";

            /* 403 */case 204: /* 403 */
            return "WINDOW_DEICONIFIED";

            /* 406 */case 205: /* 406 */
            return "WINDOW_ACTIVATED";

            /* 409 */case 206: /* 409 */
            return "WINDOW_DEACTIVATED";

            /* 412 */case 207: /* 412 */
            return "WINDOW_GAINED_FOCUS";

            /* 415 */case 208: /* 415 */
            return "WINDOW_LOST_FOCUS";

            /* 418 */case 209: /* 418 */
            return "WINDOW_STATE_CHANGED";

            /* 421 */default:/* 421 */
            return "unknown type";

        }
    }

    /**
     * @param w
     * @param b
     */
    protected void setFocusable(final Window w, final boolean b) {
        if (w.isFocusable() == b) { return; }
        blocker = ExtJFrame.PROPERTY_FOCUSABLE;
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
        blocker = ExtJFrame.PROPERTY_FOCUSABLE_WINDOW_STATE;
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

        blocker = ExtJFrame.PROPERTY_VISIBLE;
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
        if (w.getFocusOwner() != null || w.isFocusOwner() || w.isFocused()) { return; }

        System.out.println("Call requestFocus");
        w.requestFocus();
    }

    /**
     * @param resetRunnable
     */
    public void removeTimer(final ResetRunnable resetRunnable) {
        runnerMap.remove(resetRunnable.getWindow());

    }

}
