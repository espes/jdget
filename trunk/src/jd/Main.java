//    jDownloader - Downloadmanager
//    Copyright (C) 2008  JD-Team support@jdownloader.org  http://jdownloader.org
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
//

package jd;

import java.io.File;
import java.io.IOException;
import java.util.Date;
import java.util.Locale;
import java.util.Properties;
import java.util.Random;
import java.util.TreeSet;
import java.util.logging.Level;
import java.util.logging.Logger;

import jd.captcha.JACController;
import jd.captcha.JAntiCaptcha;
import jd.controlling.ClipboardMonitoring;
import jd.controlling.JDController;
import jd.controlling.JDLogger;
import jd.controlling.downloadcontroller.DownloadController;
import jd.controlling.downloadcontroller.DownloadWatchDog;
import jd.controlling.linkcollector.LinkCollector;
import jd.controlling.proxy.ProxyController;
import jd.controlling.proxy.ProxyEvent;
import jd.controlling.proxy.ProxyInfo;
import jd.gui.UserIF;
import jd.gui.swing.MacOSApplicationAdapter;
import jd.gui.swing.SwingGui;
import jd.gui.swing.jdgui.JDGui;
import jd.gui.swing.jdgui.events.EDTEventQueue;
import jd.gui.swing.laf.LookAndFeelController;
import jd.http.Browser;
import jd.http.ext.security.JSPermissionRestricter;
import jd.utils.JDUtilities;
import jd.utils.locale.JDL;

import org.appwork.app.launcher.parameterparser.CommandSwitch;
import org.appwork.app.launcher.parameterparser.CommandSwitchListener;
import org.appwork.app.launcher.parameterparser.ParameterParser;
import org.appwork.controlling.SingleReachableState;
import org.appwork.exceptions.WTFException;
import org.appwork.storage.JSonStorage;
import org.appwork.storage.config.JsonConfig;
import org.appwork.storage.config.ValidationException;
import org.appwork.storage.config.events.GenericConfigEventListener;
import org.appwork.storage.config.handler.KeyHandler;
import org.appwork.storage.jackson.JacksonMapper;
import org.appwork.utils.Application;
import org.appwork.utils.event.DefaultEventListener;
import org.appwork.utils.logging.Log;
import org.appwork.utils.net.httpconnection.HTTPProxy;
import org.appwork.utils.os.CrossSystem;
import org.appwork.utils.singleapp.AnotherInstanceRunningException;
import org.appwork.utils.singleapp.InstanceMessageListener;
import org.appwork.utils.singleapp.SingleAppInstance;
import org.appwork.utils.swing.EDTHelper;
import org.appwork.utils.swing.dialog.ConfirmDialog;
import org.appwork.utils.swing.dialog.Dialog;
import org.appwork.utils.swing.dialog.DialogNoAnswerException;
import org.jdownloader.api.ExternInterface;
import org.jdownloader.api.RemoteAPIController;
import org.jdownloader.dynamic.Dynamic;
import org.jdownloader.extensions.ExtensionController;
import org.jdownloader.gui.uiserio.JDSwingUserIO;
import org.jdownloader.gui.uiserio.NewUIO;
import org.jdownloader.images.NewTheme;
import org.jdownloader.jdserv.stats.StatsManager;
import org.jdownloader.plugins.controller.host.HostPluginController;
import org.jdownloader.settings.GeneralSettings;
import org.jdownloader.translate._JDT;
import org.jdownloader.update.JDUpdater;
import org.jdownloader.update.RestartController;
import org.jdownloader.update.WebupdateSettings;

/**
 * @author JD-Team
 */
public class Main {
    static {
        try {
            statics();
        } catch (Throwable e) {
            e.printStackTrace();
            RestartController.getInstance().restartViaUpdater();
            // TODO: call Updater.jar
        }
    }

    private static Logger              LOG;
    private static boolean             instanceStarted            = false;
    public static SingleAppInstance    SINGLE_INSTANCE_CONTROLLER = null;

    public static SingleReachableState INIT_COMPLETE              = new SingleReachableState("INIT_COMPLETE");
    public static SingleReachableState GUI_COMPLETE               = new SingleReachableState("GUI_COMPLETE");
    private static ParameterParser     PARAMETERS;
    public final static long           startup                    = System.currentTimeMillis();

    // private static JSonWrapper webConfig;

    /**
     * Sets special Properties for MAC
     */
    private static void initMACProperties() {
        // set DockIcon (most used in Building)
        try {
            com.apple.eawt.Application.getApplication().setDockIconImage(NewTheme.I().getImage("logo/jd_logo_128_128", -1));
        } catch (final Throwable e) {
            /* not every mac has this */
            Main.LOG.info("Error Initializing  Mac Look and Feel Special: " + e);
            e.printStackTrace();
        }

        // Use ScreenMenu in every LAF
        System.setProperty("apple.laf.useScreenMenuBar", "true");

        // native Mac just if User Choose Aqua as Skin
        if (LookAndFeelController.getInstance().getPlaf().getName().equals("Apple Aqua")) {
            // Mac Java from 1.3
            System.setProperty("com.apple.macos.useScreenMenuBar", "true");
            System.setProperty("com.apple.mrj.application.growbox.intrudes", "true");
            System.setProperty("com.apple.hwaccel", "true");

            // Mac Java from 1.4
            System.setProperty("apple.laf.useScreenMenuBar", "true");
            System.setProperty("apple.awt.showGrowBox", "true");
        }

        try {
            MacOSApplicationAdapter.enableMacSpecial();
        } catch (final Throwable e) {
            Main.LOG.info("Error Initializing  Mac Look and Feel Special: " + e);
            e.printStackTrace();
        }

    }

    public static void statics() {

        try {
            Dynamic.runPreStatic();
        } catch (Throwable e) {
            e.printStackTrace();

        }
        Application.setApplication(".jd_home");
        Application.getRoot(Main.class);
        // USe Jacksonmapper in this project
        JSonStorage.setMapper(new JacksonMapper());
        // do this call to keep the correct root in Application Cache

        NewUIO.setUserIO(new JDSwingUserIO());
    }

    /**
     * Checks if the user uses a correct java version
     */
    private static void javaCheck() {
        if (Application.getJavaVersion() < Application.JAVA15) {
            Main.LOG.warning("Javacheck: Wrong Java Version! JDownloader needs at least Java 1.5 or higher!");
            System.exit(0);
        }
        if (Application.isOutdatedJavaVersion(true)) {
            try {
                Dialog.getInstance().showConfirmDialog(Dialog.BUTTONS_HIDE_CANCEL, _JDT._.gui_javacheck_newerjavaavailable_title(Application.getJavaVersion()), _JDT._.gui_javacheck_newerjavaavailable_msg(), NewTheme.I().getIcon("warning", 32), null, null);
                CrossSystem.openURLOrShowMessage("http://jdownloader.org/download/index?updatejava=1");
            } catch (DialogNoAnswerException e) {
            }
        }
    }

    /**
     * Lädt ein Dynamicplugin.
     * 
     * @throws IOException
     */

    public static void main(final String args[]) {
        try {
            try {
                Dynamic.runMain(args);
            } catch (Throwable e) {
                e.printStackTrace();
            }
            Main.LOG = JDLogger.getLogger();
            // Mac OS specific
            if (CrossSystem.isMac()) {
                // Set MacApplicationName
                // Must be in Main
                System.setProperty("com.apple.mrj.application.apple.menu.about.name", "JDownloader");
                Main.initMACProperties();
            }
            /* hack for ftp plugin to use new ftp style */
            System.setProperty("ftpStyle", "new");
            /* random number: eg used for cnl2 without asking dialog */
            System.setProperty("jd.randomNumber", "" + (System.currentTimeMillis() + new Random().nextLong()));
            System.setProperty("file.encoding", "UTF-8");
            System.setProperty("sun.swing.enableImprovedDragGesture", "true");
            // only use ipv4, because debian changed default stack to ipv6
            System.setProperty("java.net.preferIPv4Stack", "true");
            // Disable the GUI rendering on the graphic card
            System.setProperty("sun.java2d.d3d", "false");
            final Properties pr = System.getProperties();
            final TreeSet<Object> propKeys = new TreeSet<Object>(pr.keySet());
            for (final Object it : propKeys) {
                final String key = it.toString();
                Main.LOG.finer(key + "=" + pr.get(key));
            }
            Main.LOG.info("Start JDownloader");
            PARAMETERS = new ParameterParser(args);
            PARAMETERS.getEventSender().addListener(new CommandSwitchListener() {

                @Override
                public void executeCommandSwitch(CommandSwitch event) {

                    if (event.getSwitchCommand().equalsIgnoreCase("forcelog")) {
                        JDInitFlags.SWITCH_FORCELOG = true;
                        Main.LOG.info("FORCED LOGGING Modus aktiv");
                    }
                    if (event.getSwitchCommand().equalsIgnoreCase("debug")) {
                        JDInitFlags.SWITCH_DEBUG = true;
                        Main.LOG.info("DEBUG Modus aktiv");
                    }

                    if (event.getSwitchCommand().equalsIgnoreCase("brdebug")) {
                        JDInitFlags.SWITCH_DEBUG = true;
                        Browser.setGlobalVerbose(true);
                        Main.LOG.info("Browser DEBUG Modus aktiv");

                    }
                    if (event.getSwitchCommand().equalsIgnoreCase("scan") || event.getSwitchCommand().equalsIgnoreCase("rfu")) {
                        JDInitFlags.REFRESH_CACHE = true;
                    }
                    if (event.getSwitchCommand().equalsIgnoreCase("trdebug")) {
                        JDL.DEBUG = true;
                        Main.LOG.info("Translation DEBUG Modus aktiv");
                    }
                    if (event.getSwitchCommand().equalsIgnoreCase("rfu")) {
                        JDInitFlags.SWITCH_RETURNED_FROM_UPDATE = true;
                    }
                }
            });
            PARAMETERS.parse(null);
            if (JDUtilities.getRunType() == JDUtilities.RUNTYPE_LOCAL) {
                JDInitFlags.SWITCH_DEBUG = true;
            }

            Main.preInitChecks();

            for (int i = 0; i < args.length; i++) {

                if (args[i].equalsIgnoreCase("-branch")) {

                    if (args[i + 1].equalsIgnoreCase("reset")) {
                        JDUpdater.getInstance().setBranchInUse(null);

                        Main.LOG.info("Switching back to default JDownloader branch");

                    } else {
                        JDUpdater.getInstance().setBranchInUse(args[i + 1]);

                        Main.LOG.info("Switching to " + args[i + 1] + " JDownloader branch");

                    }

                    i++;
                } else if (args[i].equals("-prot")) {

                    Main.LOG.finer(args[i] + " " + args[i + 1]);
                    i++;
                } else if (args[i].equals("-lng")) {

                    Main.LOG.finer(args[i] + " " + args[i + 1]);
                    if (new File(args[i + 1]).exists() && args[i + 1].trim().endsWith(".loc")) {
                        Main.LOG.info("Use custom languagefile: " + args[i + 1]);
                        JDL.setStaticLocale(args[i + 1]);
                    }
                    i++;

                } else if (args[i].equals("--new-instance") || args[i].equals("-n")) {

                    Main.LOG.finer(args[i] + " parameter");
                    JDInitFlags.SWITCH_NEW_INSTANCE = true;

                } else if (args[i].equals("--help") || args[i].equals("-h")) {

                    ParameterManager.showCmdHelp();
                    System.exit(0);

                } else if (args[i].equals("--captcha") || args[i].equals("-c")) {

                    if (args.length > i + 2) {

                        Main.LOG.setLevel(Level.OFF);
                        final String captchaValue = JAntiCaptcha.getCaptcha(args[i + 1], args[i + 2]);
                        System.out.println(captchaValue);
                        System.exit(0);

                    } else {

                        System.out.println("Error: Please define filepath and JAC method");
                        System.out.println("Usage: java -jar JDownloader.jar --captcha /path/file.png example.com");
                        System.exit(0);

                    }

                } else if (args[i].equals("--show") || args[i].equals("-s")) {

                    JACController.showDialog(false);
                    JDInitFlags.STOP = true;

                } else if (args[i].equals("--train") || args[i].equals("-t")) {

                    JACController.showDialog(true);
                    JDInitFlags.STOP = true;

                }

            }
            try {
                Main.SINGLE_INSTANCE_CONTROLLER = new SingleAppInstance("JD", JDUtilities.getJDHomeDirectoryFromEnvironment());
                Main.SINGLE_INSTANCE_CONTROLLER.setInstanceMessageListener(new InstanceMessageListener() {
                    public void parseMessage(final String[] args) {
                        ParameterManager.processParameters(args, false);
                    }
                });
                Main.SINGLE_INSTANCE_CONTROLLER.start();
                Main.instanceStarted = true;
            } catch (final AnotherInstanceRunningException e) {
                Main.LOG.info("existing jD instance found!");
                Main.instanceStarted = false;
            } catch (final Exception e) {
                JDLogger.exception(e);
                Main.LOG.severe("Instance Handling not possible!");
                Main.instanceStarted = true;
            }

            if (Main.instanceStarted || JDInitFlags.SWITCH_NEW_INSTANCE) {
                Main.start(args);
            } else {
                if (args.length > 0) {
                    Main.LOG.info("Send parameters to existing jD instance and exit");
                    Main.SINGLE_INSTANCE_CONTROLLER.sendToRunningInstance(args);
                } else {
                    Main.LOG.info("There is already a running jD instance");
                    Main.SINGLE_INSTANCE_CONTROLLER.sendToRunningInstance(new String[] { "--focus" });
                }
                System.exit(0);
            }
        } catch (Throwable e) {
            e.printStackTrace();
            RestartController.getInstance().restartViaUpdater();
        }
    }

    private static void preInitChecks() {
        Main.javaCheck();
    }

    public static boolean returnedfromUpdate() {
        return JDInitFlags.SWITCH_RETURNED_FROM_UPDATE;
    }

    private static void start(final String args[]) {
        if (!JDInitFlags.STOP) {
            go();
            for (final String p : args) {
                Main.LOG.finest("Param: " + p);
            }
            ParameterManager.processParameters(args, true);
        }
    }

    private static void go() {
        Main.LOG.info(new Date().toString());
        Main.LOG.info("init Configuration");
        if (JDInitFlags.SWITCH_DEBUG) {
            Main.LOG.info("DEBUG MODE ACTIVATED");
            // new PerformanceObserver().start();
            Main.LOG.setLevel(Level.ALL);
            Log.L.setLevel(Level.ALL);
        } else {
            JDLogger.removeConsoleHandler();
        }
        /* these can be initiated without a gui */
        final Thread thread = new Thread() {
            @Override
            public void run() {
                /* setup JSPermission */
                try {
                    JSPermissionRestricter.init();
                } catch (final Throwable e) {
                    Log.exception(e);
                }
                /* set gloabel logger for browser */
                Browser.setGlobalLogger(JDLogger.getLogger());
                /* init default global Timeouts */
                Browser.setGlobalReadTimeout(JsonConfig.create(GeneralSettings.class).getHttpReadTimeout());
                Browser.setGlobalConnectTimeout(JsonConfig.create(GeneralSettings.class).getHttpConnectTimeout());
                /* init global proxy stuff */
                Browser.setGlobalProxy(ProxyController.getInstance().getDefaultProxy());
                /* add global proxy change listener */
                ProxyController.getInstance().getEventSender().addListener(new DefaultEventListener<ProxyEvent<ProxyInfo>>() {

                    public void onEvent(ProxyEvent<ProxyInfo> event) {
                        if (event.getType().equals(ProxyEvent.Types.REFRESH)) {
                            HTTPProxy proxy = null;
                            if ((proxy = ProxyController.getInstance().getDefaultProxy()) != Browser._getGlobalProxy()) {
                                Log.L.info("Set new DefaultProxy: " + proxy);
                                Browser.setGlobalProxy(proxy);
                            }
                        }

                    }
                });
            }
        };
        thread.start();
        new EDTHelper<Void>() {
            @Override
            public Void edtRun() {
                LookAndFeelController.getInstance().setUIManager();
                return null;
            }
        }.waitForEDT();
        Locale.setDefault(Locale.ENGLISH);
        GUI_COMPLETE.executeWhenReached(new Runnable() {

            public void run() {
                new Thread() {
                    @Override
                    public void run() {
                        HostPluginController.getInstance().ensureLoaded();
                        /* load links */
                        DownloadController.getInstance().initDownloadLinks();
                        LinkCollector.getInstance().initLinkCollector();
                        /* start remote api */
                        RemoteAPIController.getInstance();
                        ExternInterface.getINSTANCE();
                        // GarbageController.getInstance();
                        /* load extensions */
                        ExtensionController.getInstance().init();
                        /* init clipboardMonitoring stuff */
                        if (org.jdownloader.settings.staticreferences.CFG_GUI.CLIPBOARD_MONITORED.isEnabled()) {
                            ClipboardMonitoring.getINSTANCE().startMonitoring();
                        }
                        org.jdownloader.settings.staticreferences.CFG_GUI.CLIPBOARD_MONITORED.getEventSender().addListener(new GenericConfigEventListener<Boolean>() {

                            public void onConfigValueModified(KeyHandler<Boolean> keyHandler, Boolean newValue) {
                                if (Boolean.TRUE.equals(newValue) && ClipboardMonitoring.getINSTANCE().isMonitoring() == false) {
                                    ClipboardMonitoring.getINSTANCE().startMonitoring();
                                } else {
                                    ClipboardMonitoring.getINSTANCE().stopMonitoring();
                                }
                            }

                            public void onConfigValidatorError(KeyHandler<Boolean> keyHandler, Boolean invalidValue, ValidationException validateException) {
                            }
                        });
                        /* check for available updates */
                        // activate auto checker only if we are in jared mode
                        if (JsonConfig.create(WebupdateSettings.class).isAutoUpdateCheckEnabled() && Application.isJared(Main.class)) {
                            JDUpdater.getInstance().startChecker();
                        }
                        /* start downloadwatchdog */
                        DownloadWatchDog.getInstance();

                        boolean doRestartRunninfDownloads = JsonConfig.create(GeneralSettings.class).isAutoRestartDownloadsIfExitWithRunningDownloads() && JsonConfig.create(GeneralSettings.class).isClosedWithRunningDownloads();
                        if (JsonConfig.create(GeneralSettings.class).isAutoStartDownloadsOnStartupEnabled() || doRestartRunninfDownloads) {
                            /* autostart downloads when no autoupdate is enabled */
                            Log.exception(new WTFException("REIMPLEMENT ME:autostart on startup"));

                            if (JsonConfig.create(GeneralSettings.class).getAutoStartCountdownSeconds() > 0) {
                                ConfirmDialog d = new ConfirmDialog(Dialog.LOGIC_COUNTDOWN, _JDT._.Main_run_autostart_(), _JDT._.Main_run_autostart_msg(), NewTheme.I().getIcon("start", 32), _JDT._.Mainstart_now(), null);
                                d.setCountdownTime(JsonConfig.create(GeneralSettings.class).getAutoStartCountdownSeconds());
                                try {
                                    Dialog.getInstance().showDialog(d);
                                    DownloadWatchDog.getInstance().startDownloads();
                                } catch (DialogNoAnswerException e) {
                                    if (e.isCausedByTimeout()) {
                                        DownloadWatchDog.getInstance().startDownloads();
                                    }

                                }
                            } else {
                                DownloadWatchDog.getInstance().startDownloads();
                            }
                            // Dialog.getInstance().showConfirmDialog(Dialog,
                            // title, message, tmpicon, okOption, cancelOption)
                        }
                    }
                }.start();
            }

        });
        new EDTHelper<Void>() {
            @Override
            public Void edtRun() {
                /* init gui here */
                JDGui.getInstance();
                EDTEventQueue.initEventQueue();
                Log.L.info("GUIDONE->" + (System.currentTimeMillis() - Main.startup));
                return null;
            }
        }.waitForEDT();
        /* this stuff can happen outside edt */
        SwingGui.setInstance(JDGui.getInstance());
        UserIF.setInstance(SwingGui.getInstance());
        JDController.getInstance().addControlListener(SwingGui.getInstance());
        try {
            /* thread should be finished here */
            thread.join(10000);
        } catch (InterruptedException e) {
        }
        Main.GUI_COMPLETE.setReached();
        Main.LOG.info("Initialisation finished");
        Main.LOG.info("Revision: " + JDUtilities.getRevision());
        Main.LOG.info("Runtype: " + JDUtilities.getRunType());
        Main.INIT_COMPLETE.setReached();

        // init statsmanager
        StatsManager.I();
    }
}