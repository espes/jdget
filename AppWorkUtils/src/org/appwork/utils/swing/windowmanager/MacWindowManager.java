/**
 * Copyright (c) 2009 - 2013 AppWork UG(haftungsbeschränkt) <e-mail@appwork.org>
 * 
 * This file is part of org.appwork.utils.swing
 * 
 * This software is licensed under the Artistic License 2.0,
 * see the LICENSE file or http://www.opensource.org/licenses/artistic-license-2.0.php
 * for details
 */
package org.appwork.utils.swing.windowmanager;

import java.awt.Window;

import org.appwork.utils.swing.windowmanager.WindowManager.FrameState;

/**
 * @author Thomas
 * 
 */
public class MacWindowManager extends WindowsWindowManager {
    protected void initForegroundLock() {

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
            setAlwaysOnTop(w, true);
            toFrontAltWorkaround(w, true);
            final com.apple.eawt.Application application = com.apple.eawt.Application.getApplication();
            // application.requestUserAttention(true);
            application.requestForeground(true);
            // requestFocus(w);
            requestFocus(w);

            break;

        case TO_BACK:
            setAlwaysOnTop(w, false);
            toBack(w);
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
}
