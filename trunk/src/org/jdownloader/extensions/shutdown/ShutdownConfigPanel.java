package org.jdownloader.extensions.shutdown;

import java.awt.event.ActionEvent;
import java.io.File;
import java.io.IOException;

import jd.gui.swing.jdgui.views.settings.components.Checkbox;
import jd.gui.swing.jdgui.views.settings.components.ComboBox;
import jd.nutils.Executer;

import org.appwork.storage.config.ValidationException;
import org.appwork.storage.config.events.GenericConfigEventListener;
import org.appwork.storage.config.handler.KeyHandler;
import org.appwork.swing.components.ExtButton;
import org.appwork.utils.Application;
import org.appwork.utils.IO;
import org.appwork.utils.os.CrossSystem;
import org.appwork.utils.swing.dialog.Dialog;
import org.appwork.utils.swing.dialog.DialogCanceledException;
import org.appwork.utils.swing.dialog.DialogClosedException;
import org.jdownloader.actions.AppAction;
import org.jdownloader.extensions.ExtensionConfigPanel;
import org.jdownloader.extensions.shutdown.translate.T;

public class ShutdownConfigPanel extends ExtensionConfigPanel<ShutdownExtension> {

    public ShutdownConfigPanel(ShutdownExtension trayExtension) {
        super(trayExtension);
        // Property subConfig = getPropertyConfig();
        KeyHandler<Mode> keyHandler2 = (KeyHandler<Mode>) CFG_SHUTDOWN.SH.getKeyHandler(CFG_SHUTDOWN.SHUTDOWN_MODE.getKey(), KeyHandler.class);

        addPair(T._.gui_config_jdshutdown_mode(), null, new ComboBox<Mode>(keyHandler2, new Mode[] { Mode.SHUTDOWN, Mode.STANDBY, Mode.HIBERNATE, Mode.CLOSE }, new String[] { Mode.SHUTDOWN.getTranslation(), Mode.STANDBY.getTranslation(), Mode.HIBERNATE.getTranslation(), Mode.CLOSE.getTranslation() }));
        addPair(T._.gui_config_jdshutdown_forceshutdown(), null, new Checkbox(CFG_SHUTDOWN.FORCE_SHUTDOWN_ENABLED));

        addPair(T._.config_active_by_default(), null, new Checkbox(CFG_SHUTDOWN.SHUTDOWN_ACTIVE_BY_DEFAULT_ENABLED));
        if (CrossSystem.isWindows()) {

            CFG_SHUTDOWN.SHUTDOWN_MODE.getEventSender().addListener(new GenericConfigEventListener<Enum>() {

                @Override
                public void onConfigValidatorError(KeyHandler<Enum> keyHandler, Enum invalidValue, ValidationException validateException) {
                }

                @Override
                public void onConfigValueModified(KeyHandler<Enum> keyHandler, Enum newValue) {
                    ShutdownExtension.checkStandbyHibernateSettings((Mode) newValue);

                }

            });
        }
        if (CrossSystem.isMac()) {
            add(new ExtButton(new AppAction() {
                {
                    setName(T._.install_force());
                }

                @Override
                public void actionPerformed(ActionEvent e) {

                    try {
                        installMacForcedShutdown();
                        CFG_SHUTDOWN.FORCE_FOR_MAC_INSTALLED.setValue(true);
                    } catch (Throwable e2) {
                        e2.printStackTrace();
                        CFG_SHUTDOWN.FORCE_FOR_MAC_INSTALLED.setValue(false);
                    }

                }
            }), "gapleft 37");
            CFG_SHUTDOWN.FORCE_SHUTDOWN_ENABLED.getEventSender().addListener(new GenericConfigEventListener<Boolean>() {

                @Override
                public void onConfigValidatorError(KeyHandler<Boolean> keyHandler, Boolean invalidValue, ValidationException validateException) {
                }

                @Override
                public void onConfigValueModified(KeyHandler<Boolean> keyHandler, Boolean newValue) {
                    if (newValue != null && newValue) {
                        if (!CFG_SHUTDOWN.FORCE_FOR_MAC_INSTALLED.isEnabled()) {
                            try {
                                installMacForcedShutdown();
                                CFG_SHUTDOWN.FORCE_FOR_MAC_INSTALLED.setValue(true);
                            } catch (Throwable e) {
                                e.printStackTrace();
                                CFG_SHUTDOWN.FORCE_FOR_MAC_INSTALLED.setValue(false);
                            }
                        }
                    }
                }

            });
        }

        /* enable force shutdown for Mac OSX */

    }

    protected void installMacForcedShutdown() throws IOException, DialogClosedException, DialogCanceledException {

        Dialog.getInstance().showConfirmDialog(0, T._.install_title(), T._.install_msg());
        Executer exec = new Executer("/usr/bin/osascript");
        File tmp = Application.getResource("tmp/osxnopasswordforshutdown.scpt");
        tmp.delete();
        try {
            IO.writeToFile(tmp, IO.readURL(getClass().getResource("osxnopasswordforshutdown.scpt")));

            exec.addParameter(tmp.getAbsolutePath());
            exec.setWaitTimeout(0);
            exec.start();

        } finally {

            tmp.deleteOnExit();
        }

    }

    @Override
    public void save() {
    }

    @Override
    public void updateContents() {

    }

}
