package org.jdownloader.settings.staticreferences;

import org.appwork.storage.config.ConfigUtils;
import org.appwork.storage.config.JsonConfig;
import org.appwork.storage.config.handler.BooleanKeyHandler;
import org.appwork.storage.config.handler.EnumKeyHandler;
import org.appwork.storage.config.handler.LongKeyHandler;
import org.appwork.storage.config.handler.StorageHandler;
import org.jdownloader.settings.SilentModeSettings;

public class CFG_SILENTMODE {
    public static void main(String[] args) {
        ConfigUtils.printStaticMappings(SilentModeSettings.class);
    }

    // Static Mappings for interface org.jdownloader.settings.SilentModeSettings
    public static final SilentModeSettings                 CFG                                         = JsonConfig.create(SilentModeSettings.class);
    public static final StorageHandler<SilentModeSettings> SH                                          = (StorageHandler<SilentModeSettings>) CFG._getStorageHandler();
    // let's do this mapping here. If we map all methods to static handlers, access is faster, and we get an error on init if mappings are
    // wrong.
    // false
    /**
     * Activate the Silent Mode
     **/
    public static final BooleanKeyHandler                  MANUAL_ENABLED                              = SH.getKeyHandler("ManualEnabled", BooleanKeyHandler.class);
    // WAIT_IN_BACKGROUND_UNTIL_WINDOW_GETS_FOCUS_OR_TIMEOUT
    public static final EnumKeyHandler                     ON_DIALOG_DURING_SILENT_MODE_ACTION         = SH.getKeyHandler("OnDialogDuringSilentModeAction", EnumKeyHandler.class);
    // 30000
    public static final LongKeyHandler                     ON_DIALOG_DURING_SILENT_MODE_ACTION_TIMEOUT = SH.getKeyHandler("OnDialogDuringSilentModeActionTimeout", LongKeyHandler.class);
    // true
    /**
     * Auto Disable the Silent mode on each startup
     **/
    public static final BooleanKeyHandler                  AUTO_RESET_ON_STARTUP_ENABLED               = SH.getKeyHandler("AutoResetOnStartupEnabled", BooleanKeyHandler.class);
    // NEVER
    /**
     * Activate Silent Mode Based on Frame Status
     **/
    public static final EnumKeyHandler                     AUTO_TRIGGER                                = SH.getKeyHandler("AutoTrigger", EnumKeyHandler.class);
    // DISABLE_DIALOG_SOLVER
    public static final EnumKeyHandler                     ON_CAPTCHA_DURING_SILENT_MODE_ACTION        = SH.getKeyHandler("OnCaptchaDuringSilentModeAction", EnumKeyHandler.class);
}