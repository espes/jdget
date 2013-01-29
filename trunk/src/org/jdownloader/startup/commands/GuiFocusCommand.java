package org.jdownloader.startup.commands;

import jd.Launcher;
import jd.gui.UIConstants;
import jd.gui.UserIF;

import org.appwork.utils.swing.EDTRunner;
import org.jdownloader.extensions.AbstractExtension;
import org.jdownloader.extensions.ExtensionController;

public class GuiFocusCommand extends AbstractStartupCommand {

    public GuiFocusCommand() {
        super("focus", "f");

    }

    @Override
    public void run(final String command, final String... parameters) {
        Launcher.GUI_COMPLETE.executeWhenReached(new Runnable() {

            @Override
            public void run() {
                new EDTRunner() {

                    @Override
                    protected void runInEDT() {
                        UserIF.getInstance().setFrameStatus(UIConstants.WINDOW_STATUS_FOREGROUND);
                    }
                };

            }

        });

        Launcher.INIT_COMPLETE.executeWhenReached(new Runnable() {

            @Override
            public void run() {
                for (AbstractExtension ae : ExtensionController.getInstance().getEnabledExtensions()) {
                    try {
                        ae.handleCommand(command, parameters);
                    } catch (Throwable e) {
                        logger.log(e);
                    }
                }

            }
        });
    }

    @Override
    public String getDescription() {
        return "Focus JDownloader and bring JD to TOP";
    }

}
