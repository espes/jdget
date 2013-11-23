package org.jdownloader.settings.staticreferences;

import org.appwork.storage.config.ConfigUtils;
import org.appwork.storage.config.JsonConfig;
import org.appwork.storage.config.handler.BooleanKeyHandler;
import org.appwork.storage.config.handler.IntegerKeyHandler;
import org.appwork.storage.config.handler.StorageHandler;
import org.appwork.storage.config.handler.StringKeyHandler;
import org.jdownloader.captcha.v2.solver.solver9kw.Captcha9kwSettings;

public class CFG_9KWCAPTCHA {
    public static void main(String[] args) {
        ConfigUtils.printStaticMappings(Captcha9kwSettings.class);
    }

    // Static Mappings for interface org.jdownloader.captcha.v2.solver.solver9kw.Captcha9kwSettings
    public static final Captcha9kwSettings                 CFG                = JsonConfig.create(Captcha9kwSettings.class);
    public static final StorageHandler<Captcha9kwSettings> SH                 = (StorageHandler<Captcha9kwSettings>) CFG._getStorageHandler();
    // let's do this mapping here. If we map all methods to static handlers, access is faster, and we get an error on init if mappings are
    // wrong.

    /**
     * Activate the blacklist
     **/
    public static final BooleanKeyHandler                  BLACKLISTCHECK     = SH.getKeyHandler("blacklistcheck", BooleanKeyHandler.class);

    /**
     * Your (User) ApiKey from 9kw.eu
     **/
    public static final StringKeyHandler                   API_KEY            = SH.getKeyHandler("ApiKey", StringKeyHandler.class);

    /**
     * Captcha blacklist for hoster with prio
     **/
    public static final StringKeyHandler                   BLACKLISTPRIO      = SH.getKeyHandler("blacklistprio", StringKeyHandler.class);

    /**
     * Captcha whitelist for hoster with prio
     **/
    public static final StringKeyHandler                   WHITELISTPRIO      = SH.getKeyHandler("whitelistprio", StringKeyHandler.class);

    /**
     * Max. Captchas Parallel
     **/
    public static final IntegerKeyHandler                  THREADPOOL_SIZE    = SH.getKeyHandler("ThreadpoolSize", IntegerKeyHandler.class);

    /**
     * Confirm option for captchas (Cost +6)
     **/
    public static final BooleanKeyHandler                  CONFIRM            = SH.getKeyHandler("confirm", BooleanKeyHandler.class);

    /**
     * Activate the Captcha Feedback
     **/
    public static final BooleanKeyHandler                  FEEDBACK           = SH.getKeyHandler("feedback", BooleanKeyHandler.class);

    /**
     * Activate the whitelist
     **/
    public static final BooleanKeyHandler                  WHITELISTCHECK     = SH.getKeyHandler("whitelistcheck", BooleanKeyHandler.class);

    /**
     * Max. Captchas per hour
     **/
    public static final IntegerKeyHandler                  HOUR               = SH.getKeyHandler("hour", IntegerKeyHandler.class);

    /**
     * Only https requests to 9kw.eu
     **/
    public static final BooleanKeyHandler                  HTTPS              = SH.getKeyHandler("https", BooleanKeyHandler.class);

    /**
     * Activate the Mouse Captchas
     **/
    public static final BooleanKeyHandler                  MOUSE              = SH.getKeyHandler("mouse", BooleanKeyHandler.class);

    /**
     * Activate the whitelist with prio
     **/
    public static final BooleanKeyHandler                  WHITELISTPRIOCHECK = SH.getKeyHandler("whitelistpriocheck", BooleanKeyHandler.class);

    /**
     * Captcha blacklist for hoster
     **/
    public static final StringKeyHandler                   BLACKLIST          = SH.getKeyHandler("blacklist", StringKeyHandler.class);

    /**
     * Activate the blacklist with prio
     **/
    public static final BooleanKeyHandler                  BLACKLISTPRIOCHECK = SH.getKeyHandler("blacklistpriocheck", BooleanKeyHandler.class);

    /**
     * Activate the 9kw.eu service
     **/
    public static final BooleanKeyHandler                  ENABLED            = SH.getKeyHandler("Enabled", BooleanKeyHandler.class);

    /**
     * More priority for captchas (Cost +1-10)
     **/
    public static final IntegerKeyHandler                  PRIO               = SH.getKeyHandler("prio", IntegerKeyHandler.class);

    /**
     * Activate the option selfsolve
     **/
    public static final BooleanKeyHandler                  SELFSOLVE          = SH.getKeyHandler("Selfsolve", BooleanKeyHandler.class);

    /**
     * Captcha whitelist for hoster
     **/
    public static final StringKeyHandler                   WHITELIST          = SH.getKeyHandler("whitelist", StringKeyHandler.class);

    /**
     * Confirm option for mouse captchas (Cost +6)
     **/
    public static final BooleanKeyHandler                  MOUSECONFIRM       = SH.getKeyHandler("mouseconfirm", BooleanKeyHandler.class);
}