//    jDownloader - Downloadmanager
//    Copyright (C) 2009  JD-Team support@jdownloader.org
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

package org.jdownloader.extensions.antistandby;

import jd.controlling.downloadcontroller.DownloadWatchDog;

import org.appwork.utils.logging2.LogSource;
import org.jdownloader.logging.LogController;

public class WindowsAntiStandby extends Thread implements Runnable {

    private boolean                    run   = false;
    private static final int           sleep = 5000;
    private final AntiStandbyExtension jdAntiStandby;

    public WindowsAntiStandby(AntiStandbyExtension jdAntiStandby) {
        super();
        this.jdAntiStandby = jdAntiStandby;

    }

    @Override
    public void run() {
        LogSource logger = LogController.CL(AntiStandbyExtension.class);
        try {
            while (!isInterrupted()) {
                switch (jdAntiStandby.getMode()) {
                case DOWNLOADING:
                    if (DownloadWatchDog.getInstance().getStateMachine().isState(DownloadWatchDog.RUNNING_STATE, DownloadWatchDog.STOPPING_STATE)) {
                        if (!run) {
                            run = true;
                            logger.fine("JDAntiStandby: Start");
                        }
                        enableAntiStandby(true);
                    } else {
                        if (run) {
                            run = false;
                            logger.fine("JDAntiStandby: Stop");
                            enableAntiStandby(false);
                        }
                    }
                    break;
                case RUNNING:
                    if (!run) {
                        run = true;
                        logger.fine("JDAntiStandby: Start");
                    }
                    enableAntiStandby(true);
                    break;
                default:
                    logger.finest("JDAntiStandby: Config error (unknown mode: " + jdAntiStandby.getMode() + ")");
                    break;
                }
                sleep(sleep);
            }
        } catch (Exception e) {
            logger.log(e);
        } finally {
            try {
                enableAntiStandby(false);
            } catch (final Throwable e) {
            } finally {
                logger.fine("JDAntiStandby: Terminated");
                logger.close();
            }
        }
    }

    private void enableAntiStandby(boolean enabled) {

        Kernel32 kernel32 = (Kernel32) com.sun.jna.Native.loadLibrary("kernel32", Kernel32.class);
        if (enabled) {
            if (jdAntiStandby.getSettings().isDisplayRequired()) {

                kernel32.SetThreadExecutionState(Kernel32.ES_CONTINUOUS | Kernel32.ES_SYSTEM_REQUIRED | Kernel32.ES_DISPLAY_REQUIRED);
            } else {
                kernel32.SetThreadExecutionState(Kernel32.ES_CONTINUOUS | Kernel32.ES_SYSTEM_REQUIRED);
            }
        } else {
            kernel32.SetThreadExecutionState(Kernel32.ES_CONTINUOUS);
        }
    }

}
