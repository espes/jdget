package org.jdownloader.extensions.antistandby;

import java.io.IOException;

import jd.controlling.downloadcontroller.DownloadWatchDog;

import org.appwork.utils.logging2.LogSource;
import org.appwork.utils.processes.ProcessBuilderFactory;
import org.jdownloader.logging.LogController;

public class MacAntiStandBy extends Thread {

    private AntiStandbyExtension extension;
    private LogSource            logger;
    private Process              process;

    public MacAntiStandBy(AntiStandbyExtension antiStandbyExtension) {
        super("MacAntiStandByThread");
        extension = antiStandbyExtension;
        logger = LogController.CL(AntiStandbyExtension.class);
    }

    public void run() {
        try {
            while (true) {

                switch (extension.getMode()) {
                case DOWNLOADING:
                    if (DownloadWatchDog.getInstance().getStateMachine().isState(DownloadWatchDog.PAUSE_STATE, DownloadWatchDog.RUNNING_STATE, DownloadWatchDog.STOPPING_STATE)) {

                        if (!processIsRunning()) {

                            logger.fine("JDAntiStandby: Start");
                            doit();
                        }

                    } else {
                        if (processIsRunning()) {

                            process.destroy();
                            process = null;
                        }
                    }
                    break;
                case RUNNING:
                    if (!processIsRunning()) {
                        doit();
                    }
                    break;
                default:
                    logger.finest("JDAntiStandby: Config error (unknown mode: " + extension.getMode() + ")");
                }
                try {
                    Thread.sleep(5000);
                } catch (InterruptedException e) {
                    logger.log(e);
                    return;
                }
            }
        } finally {
            if (processIsRunning()) {

                process.destroy();
                process = null;
            }
        }

    }

    private boolean processIsRunning() {

        if (process == null) return false;
        try {
            process.exitValue();
        } catch (IllegalThreadStateException e) {
            return true;
        }
        return false;
    }

    public void doit() {
        try {
            String[] command = { "pmset", "noidle" };
            // windows debug
            // command = new String[] { "calc.exe" };
            ProcessBuilder probuilder = ProcessBuilderFactory.create(command);
            logger.info("Call pmset nodile");
            process = probuilder.start();

        } catch (IOException e) {
            logger.log(e);

        }
    }
}
