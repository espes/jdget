package org.jdownloader.settings.staticreferences;

import org.appwork.storage.config.ConfigUtils;
import org.appwork.storage.config.JsonConfig;
import org.appwork.storage.config.handler.BooleanKeyHandler;
import org.appwork.storage.config.handler.IntegerKeyHandler;
import org.appwork.storage.config.handler.ObjectKeyHandler;
import org.appwork.storage.config.handler.StorageHandler;
import org.appwork.storage.config.handler.StringKeyHandler;
import org.jdownloader.gui.views.linkgrabber.addlinksdialog.LinkgrabberSettings;

public class CFG_LINKGRABBER {
    public static void main(String[] args) {
        ConfigUtils.printStaticMappings(LinkgrabberSettings.class);
    }

    // Static Mappings for interface org.jdownloader.gui.views.linkgrabber.addlinksdialog.LinkgrabberSettings
    public static final LinkgrabberSettings                 CFG                                                      = JsonConfig.create(LinkgrabberSettings.class);
    public static final StorageHandler<LinkgrabberSettings> SH                                                       = (StorageHandler<LinkgrabberSettings>) CFG._getStorageHandler();
    // let's do this mapping here. If we map all methods to static handlers, access is faster, and we get an error on init if mappings are
    // wrong.

    /**
     * If true, Linkcollector will create an extra package for each multipart or *.rar archive
     **/
    public static final BooleanKeyHandler                   ARCHIVE_PACKAGIZER_ENABLED                               = SH.getKeyHandler("ArchivePackagizerEnabled", BooleanKeyHandler.class);

    /**
     * If >0, there will be no packages with * or less links
     **/
    public static final IntegerKeyHandler                   VARIOUS_PACKAGE_LIMIT                                    = SH.getKeyHandler("VariousPackageLimit", IntegerKeyHandler.class);

    public static final BooleanKeyHandler                   VARIOUS_PACKAGE_ENABLED                                  = SH.getKeyHandler("VariousPackageEnabled", BooleanKeyHandler.class);

    /**
     * If true, Offline Links, that do not fit in a existing package, will be moved to a offline package.
     **/
    public static final BooleanKeyHandler                   OFFLINE_PACKAGE_ENABLED                                  = SH.getKeyHandler("OfflinePackageEnabled", BooleanKeyHandler.class);

    public static final BooleanKeyHandler                   LINKGRABBER_ADD_AT_TOP                                   = SH.getKeyHandler("LinkgrabberAddAtTop", BooleanKeyHandler.class);

    public static final StringKeyHandler                    LATEST_DOWNLOAD_DESTINATION_FOLDER                       = SH.getKeyHandler("LatestDownloadDestinationFolder", StringKeyHandler.class);

    public static final BooleanKeyHandler                   LINKGRABBER_AUTO_CONFIRM_ENABLED                         = SH.getKeyHandler("LinkgrabberAutoConfirmEnabled", BooleanKeyHandler.class);

    /**
     * If true, AddLinks Dialogs will use the last used downloadfolder as defaultvalue. IF False, the Default Download Paath (settings) will
     * be used
     **/
    public static final BooleanKeyHandler                   USE_LAST_DOWNLOAD_DESTINATION_AS_DEFAULT                 = SH.getKeyHandler("UseLastDownloadDestinationAsDefault", BooleanKeyHandler.class);

    public static final BooleanKeyHandler                   AUTO_EXTRACTION_ENABLED                                  = SH.getKeyHandler("AutoExtractionEnabled", BooleanKeyHandler.class);

    /**
     * If false, The AddLinks Dialog in Linkgrabber works on the pasted text, and does not prefilter URLS any more
     **/
    public static final BooleanKeyHandler                   ADD_LINKS_PRE_PARSER_ENABLED                             = SH.getKeyHandler("AddLinksPreParserEnabled", BooleanKeyHandler.class);

    /**
     * Set to false to hide the 'Add Downloads' Context Menu Action in Linkgrabber
     **/
    public static final BooleanKeyHandler                   CONTEXT_MENU_ADD_LINKS_ACTION_ALWAYS_VISIBLE             = SH.getKeyHandler("ContextMenuAddLinksActionAlwaysVisible", BooleanKeyHandler.class);

    public static final ObjectKeyHandler                    PACKAGE_NAME_HISTORY                                     = SH.getKeyHandler("PackageNameHistory", ObjectKeyHandler.class);

    /**
     * If true, Plugins will try to correct filenames to match to others. For example in splitted archives.
     **/
    public static final BooleanKeyHandler                   AUTO_FILENAME_CORRECTION_ENABLED                         = SH.getKeyHandler("AutoFilenameCorrectionEnabled", BooleanKeyHandler.class);

    public static final ObjectKeyHandler                    DOWNLOAD_DESTINATION_HISTORY                             = SH.getKeyHandler("DownloadDestinationHistory", ObjectKeyHandler.class);

    /**
     * Selecting Views in Linkgrabber Sidebar autoselects the matching links in the table. Set this to false to avoid this.
     **/
    public static final BooleanKeyHandler                   QUICK_VIEW_SELECTION_ENABLED                             = SH.getKeyHandler("QuickViewSelectionEnabled", BooleanKeyHandler.class);

    public static final BooleanKeyHandler                   LINKGRABBER_AUTO_START_ENABLED                           = SH.getKeyHandler("LinkgrabberAutoStartEnabled", BooleanKeyHandler.class);

    /**
     * show a restore button for filtered links
     **/
    public static final BooleanKeyHandler                   RESTORE_BUTTON_ENABLED                                   = SH.getKeyHandler("RestoreButtonEnabled", BooleanKeyHandler.class);

    /**
     * AutoConfirm waits a delay before confirming the links. Default is 15000ms
     **/
    public static final IntegerKeyHandler                   AUTO_CONFIRM_DELAY                                       = SH.getKeyHandler("AutoConfirmDelay", IntegerKeyHandler.class);

    /**
     * Define the Pattern that is used to create Packagename created by SplitPackages! {PACKAGENAME,HOSTNAME}
     **/
    public static final StringKeyHandler                    SPLIT_PACKAGE_NAME_FACTORY_PATTERN                       = SH.getKeyHandler("SplitPackageNameFactoryPattern", StringKeyHandler.class);

    /**
     * If set, the addlinks dialog has this text. Use it for debug reasons.
     **/
    public static final StringKeyHandler                    PRESET_DEBUG_LINKS                                       = SH.getKeyHandler("PresetDebugLinks", StringKeyHandler.class);

    /**
     * If true, JD will switch to the Download Tab after confirming Links in Linkgrabber
     **/
    public static final BooleanKeyHandler                   AUTO_SWITCH_TO_DOWNLOAD_TABLE_ON_CONFIRM_DEFAULT_ENABLED = SH.getKeyHandler("AutoSwitchToDownloadTableOnConfirmDefaultEnabled", BooleanKeyHandler.class);

    /**
     * If true, the Linkcollector asks the Hosterplugins to filter the packageidentifier. This helps to map corrupt filenames into the
     * correct packages.
     **/
    public static final BooleanKeyHandler                   AUTO_PACKAGE_MATCHING_CORRECTION_ENABLED                 = SH.getKeyHandler("AutoPackageMatchingCorrectionEnabled", BooleanKeyHandler.class);

    public static final IntegerKeyHandler                   ADD_DIALOG_WIDTH                                         = SH.getKeyHandler("AddDialogWidth", IntegerKeyHandler.class);

    public static final IntegerKeyHandler                   ADD_DIALOG_HEIGHT                                        = SH.getKeyHandler("AddDialogHeight", IntegerKeyHandler.class);
}