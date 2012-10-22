package org.jdownloader.translate;

import org.appwork.txtresource.Default;
import org.appwork.txtresource.Defaults;
import org.appwork.txtresource.DescriptionForTranslationEntry;
import org.appwork.txtresource.TranslateInterface;

@Defaults(lngs = { "en" })
public interface JdownloaderTranslation extends TranslateInterface {
    //

    @Default(lngs = { "en", "de" }, values = { "Error occured!", "Fehler aufgetreten!" })
    String dialog_title_exception();

    @Default(lngs = { "en", "de" }, values = { "Tools", "Tools" })
    String gui_menu_extensions();

    @Default(lngs = { "en", "de" }, values = { "Restart Required", "Neustart nötig" })
    String dialog_optional_showRestartRequiredMessage_title();

    @Default(lngs = { "en", "de" }, values = { "Your changes require a JDownloader restart to take effect. Restart now?", "Ihre Änderungen benötigen einen Neustart von JDownloader. Jetzt neu starten?" })
    String dialog_optional_showRestartRequiredMessage_msg();

    @Default(lngs = { "en", "de" }, values = { "Yes", "Ja" })
    String basics_yes();

    @Default(lngs = { "en", "de" }, values = { "No", "Nein" })
    String basics_no();

    @Default(lngs = { "en", "de" }, values = { "Show %s1 now?\r\nYou may open it later using Mainmenu->Window", "%s1 jetzt anzeigen?\r\n%s1 kann jederzeit über Hauptmenü -> Fenster angezeigt werden." })
    String gui_settings_extensions_show_now(String name);

    @Default(lngs = { "en", "de" }, values = { "General", "Allgemein" })
    String gui_settings_general_title();

    @Default(lngs = { "en", "de" }, values = { "Choose", "Auswählen" })
    String basics_browser_folder();

    @Default(lngs = { "en", "de" }, values = { "Choose directory", "Ordner auswählen" })
    String gui_setting_folderchooser_title();

    @Default(lngs = { "en", "de" }, values = { "If a Proxy Server is required to access internet, please enter proxy data here. JDownloader is able to rotate several Proxies to avoid IP waittimes. Default Proxy is used for all connections that are not IP restricted.", "Falls ein Proxy benötigt wird um ins Internet zu verbinden, kann dieser hier eingetragen werden. Um IP Wartezeit zu vermeiden, können mehrere Proxy Server eingetragen werden. Der Defaultproxy wird für alle Verbindungen ohne IP Beschränkungen verwendet." })
    String gui_settings_proxy_description();

    @Default(lngs = { "en", "de" }, values = { "Set the default download path here. Changing default path here, affects only new downloads.", "Standard Download Zielordner setzen. Eine Änderung betrifft nur neue Links." })
    String gui_settings_downloadpath_description();

    @Default(lngs = { "en", "de" }, values = { "Using the hashcheck option, JDownloader to verify your downloads for correctness after download.", "Über den automatischen Hashcheck kann JDownloader die geladenen Dateien automatisch auf Korrektheit überprüfen." })
    String gui_settings_filewriting_description();

    @Default(lngs = { "en", "de" }, values = { "Connection Manager", "Verbindungsverwaltung" })
    String gui_settings_proxy_title();

    @Default(lngs = { "en" }, values = { "No permissions to write to harddisk" })
    String download_error_message_iopermissions();

    @Default(lngs = { "en" }, values = { "[wait for new ip]" })
    String gui_downloadlink_hosterwaittime();

    @Default(lngs = { "en" }, values = { "Reconnect duration" })
    String gui_config_reconnect_showcase_time();

    @Default(lngs = { "en" }, values = { "%s1 Updates available" })
    String gui_mainframe_title_updatemessage2(Object s1);

    @Default(lngs = { "en" }, values = { "Outdated Javaversion found: %s1!" })
    String gui_javacheck_newerjavaavailable_title(Object s1);

    @Default(lngs = { "en" }, values = { "Canceled Captcha Dialog" })
    String captchacontroller_cancel_dialog_allorhost();

    @Default(lngs = { "en" }, values = { "File already exists." })
    String controller_status_fileexists_skip();

    @Default(lngs = { "en" }, values = { "Reconnect unknown" })
    String gui_warning_reconnectunknown();

    @Default(lngs = { "en" }, values = { "Reconnect successful" })
    String gui_warning_reconnectSuccess();

    @Default(lngs = { "en" }, values = { "Plugin error. Please inform Support" })
    String plugins_errors_pluginerror();

    @Default(lngs = { "en" }, values = { "Wrong password" })
    String decrypter_wrongpassword();

    @Default(lngs = { "en" }, values = { "Could not overwrite existing file" })
    String system_download_errors_couldnotoverwrite();

    @Default(lngs = { "en" }, values = { "Could not delete existing part file" })
    String system_download_errors_couldnotdelete();

    @Default(lngs = { "en" }, values = { "Network problems" })
    String download_error_message_networkreset();

    @Default(lngs = { "en" }, values = { "<b><u>No Reconnect selected</u></b><br/><p>Reconnection is an advanced approach for skipping long waits that some hosts impose on free users. <br>It is not helpful while using a premium account.</p><p>Read more about Reconnect <a href='http://support.jdownloader.org/index.php?_m=knowledgebase&_a=viewarticle&kbarticleid=1'>here</a></p>" })
    String jd_controlling_reconnect_plugins_DummyRouterPlugin_getGUI2();

    @Default(lngs = { "en" }, values = { "CRC-Check OK(%s1)" })
    String system_download_doCRC2_success(Object s1);

    @Default(lngs = { "en" }, values = { "Hoster problem?" })
    String plugins_errors_hosterproblem();

    @Default(lngs = { "en" }, values = { "Download incomplete" })
    String download_error_message_incomplete();

    @Default(lngs = { "en" }, values = { "Unexpected rangeheader format:" })
    String download_error_message_rangeheaderparseerror();

    @Default(lngs = { "en" }, values = { "Premium Error" })
    String downloadlink_status_error_premium();

    @Default(lngs = { "en" }, values = { "No suitable account available" })
    String downloadlink_status_error_premium_noacc();

    @Default(lngs = { "en" }, values = { "[Not available]" })
    String gui_download_onlinecheckfailed();

    @Default(lngs = { "en" }, values = { "No valid account found" })
    String decrypter_invalidaccount();

    @Default(lngs = { "en" }, values = { "Please enter the password for %s1" })
    String jd_plugins_PluginUtils_askPassword(Object s1);

    @Default(lngs = { "en" }, values = { "Plugin out of date" })
    String controller_status_plugindefective();

    @Default(lngs = { "en" }, values = { "Mirror %s1 is loading" })
    String system_download_errors_linkisBlocked(Object s1);

    @Default(lngs = { "en" }, values = { "Download failed" })
    String downloadlink_status_error_downloadfailed();

    @Default(lngs = { "en" }, values = { "Unknown error, retrying" })
    String downloadlink_status_error_retry();

    @Default(lngs = { "en" }, values = { "Reconnect failed!" })
    String gui_warning_reconnectFailed();

    @Default(lngs = { "en" }, values = { "running..." })
    String gui_warning_reconnect_running();

    @Default(lngs = { "en" }, values = { "Your current IP" })
    String gui_config_reconnect_showcase_currentip();

    @Default(lngs = { "en" }, values = { "CRC-Check FAILED(%s1)" })
    String system_download_doCRC2_failed(Object s1);

    @Default(lngs = { "en" }, values = { "No Internet connection?" })
    String plugins_errors_nointernetconn();

    @Default(lngs = { "en" }, values = { "Processing error" })
    String downloadlink_status_error_post_process();

    @Default(lngs = { "en" }, values = { "Download from this host is currently not possible" })
    String downloadlink_status_error_hoster_temp_unavailable();

    @Default(lngs = { "en" }, values = { "Disconnect?" })
    String plugins_errors_disconnect();

    @Default(lngs = { "en" }, values = { "CRC-Check running(%s1)" })
    String system_download_doCRC2(Object s1);

    @Default(lngs = { "en" }, values = { "File exists" })
    String jd_controlling_SingleDownloadController_askexists_title();

    @Default(lngs = { "en" }, values = { "[download currently not possible]" })
    String gui_downloadlink_hostertempunavail();

    @Default(lngs = { "en" }, values = { "Invalid Outputfile" })
    String system_download_errors_invalidoutputfile();

    @Default(lngs = { "en" }, values = { "Could not clone the connection" })
    String download_error_message_connectioncopyerror();

    @Default(lngs = { "en" }, values = { "Wait %s1" })
    String gui_download_waittime_status2(Object s1);

    @Default(lngs = { "en" }, values = { "You canceled a Captcha Dialog!\r\nHow do you want to continue?" })
    String captchacontroller_cancel_dialog_allorhost_msg();

    @Default(lngs = { "en" }, values = { "Wrong captcha code" })
    String decrypter_wrongcaptcha();

    @Default(lngs = { "en" }, values = { "Server does not support chunkload" })
    String download_error_message_rangeheaders();

    @Default(lngs = { "en" }, values = { "Captcha recognition" })
    String gui_downloadview_statustext_jac();

    @Default(lngs = { "en" }, values = { "Show all further pending Captchas" })
    String captchacontroller_cancel_dialog_allorhost_next();

    @Default(lngs = { "en" }, values = { "Although JDownloader runs on your javaversion, we advise to install the latest java updates. \r\nJDownloader will run more stable, faster, and will look better. \r\n\r\nVisit http://jdownloader.org/download." })
    String gui_javacheck_newerjavaavailable_msg();

    @Default(lngs = { "en" }, values = { "No Connection" })
    String downloadlink_status_error_no_connection();

    @Default(lngs = { "en" }, values = { "Download Limit reached" })
    String downloadlink_status_error_download_limit();

    @Default(lngs = { "en" }, values = { "File exists" })
    String downloadlink_status_error_file_exists();

    @Default(lngs = { "en" }, values = { "Reconnect failed too often! Autoreconnect is disabled! Please check your reconnect Settings!" })
    String jd_controlling_reconnect_Reconnector_progress_failed2();

    @Default(lngs = { "en" }, values = { "The file '%s1' in package %s2 already exists. What do you want to do?" })
    String jd_controlling_SingleDownloadController_askexists(String s1, String packagename);

    @Default(lngs = { "en" }, values = { "Captcha wrong" })
    String downloadlink_status_error_captcha_wrong();

    @Default(lngs = { "en" }, values = { "Not tested yet" })
    String gui_config_reconnect_showcase_message_none();

    @Default(lngs = { "en" }, values = { "Do not show pending Captchas for %s1" })
    String captchacontroller_cancel_dialog_allorhost_cancelhost(Object s1);

    @Default(lngs = { "en" }, values = { "Unknown error" })
    String decrypter_unknownerror();

    @Default(lngs = { "en" }, values = { "Temporarily unavailable" })
    String controller_status_tempunavailable();

    @Default(lngs = { "en" }, values = { "Cancel all pending Captchas" })
    String captchacontroller_cancel_dialog_allorhost_all();

    @Default(lngs = { "en" }, values = { "Download" })
    String download_connection_normal();

    @Default(lngs = { "en" }, values = { "Ip before reconnect" })
    String gui_config_reconnect_showcase_lastip();

    @Default(lngs = { "en" }, values = { "Hoster offline?" })
    String plugins_errors_hosteroffline();

    @Default(lngs = { "en" }, values = { "Start Test" })
    String gui_config_reconnect_showcase_reconnect2();

    @Default(lngs = { "en" }, values = { "Connecting..." })
    String gui_download_create_connection();

    @Default(lngs = { "en" }, values = { "Container Error" })
    String controller_status_containererror();

    @Default(lngs = { "en" }, values = { "Connection lost." })
    String controller_status_connectionproblems();

    @Default(lngs = { "en" }, values = { "The downloadsystem is out of memory" })
    String download_error_message_outofmemory();

    @Default(lngs = { "en" }, values = { "Plugin outdated" })
    String downloadlink_status_error_defect();

    @Default(lngs = { "en" }, values = { "Failed to overwrite" })
    String controller_status_fileexists_overwritefailed();

    @Default(lngs = { "en" }, values = { "Service temp. unavailable" })
    String download_error_message_unavailable();

    @Default(lngs = { "en" }, values = { "Could not write to file: %s1" })
    String download_error_message_localio(Object s1);

    @Default(lngs = { "en" }, values = { "Not enough harddiskspace" })
    String downloadlink_status_error();

    @Default(lngs = { "en" }, values = { "Invalid download directory" })
    String downloadlink_status_error_invalid_dest();

    @Default(lngs = { "en" }, values = { "Temp. unavailable" })
    String downloadlink_status_error_temp_unavailable();

    @Default(lngs = { "en" }, values = { "Error: " })
    String plugins_errors_error();

    @Default(lngs = { "en" }, values = { "Could not rename partfile" })
    String system_download_errors_couldnotrename();

    @Default(lngs = { "en" }, values = { "(Filesize unknown)" })
    String gui_download_filesize_unknown();

    @Default(lngs = { "en" }, values = { "Waiting for user input" })
    String downloadlink_status_waitinguserio();

    @Default(lngs = { "en" }, values = { "Password wrong" })
    String plugins_errors_wrongpassword();

    @Default(lngs = { "en" }, values = { "File not found" })
    String downloadlink_status_error_file_not_found();

    @Default(lngs = { "en" }, values = { "various" })
    String controller_packages_defaultname();

    @Default(lngs = { "en" }, values = { "No Reconnect" })
    String jd_controlling_reconnect_plugins_DummyRouterPlugin_getName();

    @Default(lngs = { "en" }, values = { "Download failed" })
    String plugins_error_downloadfailed();

    @Default(lngs = { "en" }, values = { "Fatal Error" })
    String downloadlink_status_error_fatal();

    @Default(lngs = { "en" }, values = { "Incomplete" })
    String downloadlink_status_incomplete();

    @Default(lngs = { "en" }, values = { "Download Managment" })
    String gui_settings_downloadcontroll_title();

    @Default(lngs = { "en" }, values = { "Connection limits, Download order, Priorities, .... set up the Downloadcontroller details." })
    String gui_settings_downloadcontroll_description();

    @Default(lngs = { "en" }, values = { "Linkgrabber Filter" })
    String gui_settings_linkgrabber_title();

    @Default(lngs = { "en" }, values = { "The linkfilter is used to filter links. Use it to ignore links, adresses, urls or files based on their properties. Add Exceptions to accept special links. Exceptions will be available as a Custom View in the Linkgrabber Sidebar." })
    String gui_settings_linkgrabber_filter_description();

    @Default(lngs = { "en" }, values = { "All options here help to make JDownloader usable by people of all abilities and disabilities." })
    String gui_settings_barrierfree_description();

    @Default(lngs = { "en" }, values = { "Account Manager" })
    String gui_settings_premium_title();

    @Default(lngs = { "en" }, values = { "Enter and manage all your Premium/Gold/Platin accounts." })
    String gui_settings_premium_description();

    @Default(lngs = { "en" }, values = { "Account %s1@%s2" })
    String pluginforhost_infogenerator_title(String user, String hoster);

    @Default(lngs = { "en" }, values = { "Basic Authentication" })
    String gui_settings_basicauth_title();

    @Default(lngs = { "en" }, values = { "Add HTTP and FTP credentials here. Basic Authentication can be used for basic logins which do not need an extra Plugin.\r\n\r\nUse the Account Manager for Premium/Gold/Platin Accounts!" })
    String gui_settings_basicauth_description();

    @Default(lngs = { "en" }, values = { "Reconnect" })
    String gui_settings_reconnect_title();

    @Default(lngs = { "en" }, values = { "Reconnect Wizard" })
    String reconnectmanager_wizard();

    @Default(lngs = { "en" }, values = { "Reconnect Method" })
    String gui_settings_reconnect_title_method();

    @Default(lngs = { "en" }, values = { "Extension Modules" })
    String gui_settings_extensions_description();

    @Default(lngs = { "en" }, values = { "Enabled/Disable this Extension" })
    String settings_sidebar_tooltip_enable_extension();

    @Default(lngs = { "en" }, values = { "Enabled" })
    String configheader_enabled();

    @Default(lngs = { "en" }, values = { "Plugin Settings" })
    String gui_settings_plugins_title();

    @Default(lngs = { "en" }, values = { "JDownloader uses 'Plugins' for %s1 websites to automate downloads which would take a lot of time without JDownloader. Some of these Plugins have settings to customize their behaviour." })
    String gui_settings_plugins_description(int num);

    @Default(lngs = { "en" }, values = { "All Settings found here are for Advanced Users only! Do not change anything here if you do not know 100% what you are doing." })
    String gui_settings_advanced_description();

    @Default(lngs = { "en" }, values = { "Look And Feel" })
    String gui_settings__gui_title();

    @Default(lngs = { "en" }, values = { "Offline" })
    String literally_offline();

    @Default(lngs = { "en" }, values = { "The Packagizer rules let you auto-set Download Settings on Files based on their properties." })
    String gui_settings_linkgrabber_packagizer_description();

    @Default(lngs = { "en" }, values = { "%s1 (copy)" })
    String LinkgrabberFilterRule_duplicate(String name);

    @Default(lngs = { "en" }, values = { "Duplicate Rule" })
    String DuplicateAction_DuplicateAction_();

    @Default(lngs = { "en" }, values = { "Save rules to a file.\r\n- Backup\r\n- Share them with others" })
    String ExportAction_ExportAction_tt();

    @Default(lngs = { "en" }, values = { "Import Rules from *.jdfilter (*.jdregexfilter) files" })
    String ImportAction_tt();

    @Default(lngs = { "en" }, values = { "Really delete all selected rules??" })
    String RemoveAction_actionPerformed_rly_msg();

    @Default(lngs = { "en" }, values = { "Offline Files" })
    String LinkFilterSettings_DefaultFilterList_getDefaultValue_();

    @Default(lngs = { "en" }, values = { "Offline Files" })
    String LinkCollector_addCrawledLink_offlinepackage();

    @Default(lngs = { "en" }, values = { "Various Files" })
    String LinkCollector_addCrawledLink_variouspackage();

    @Default(lngs = { "en" }, values = { "%s1" })
    String LinkCollector_archiv(String cleanFileName);

    @Default(lngs = { "en" }, values = { "Split Package %s1" })
    String SetDownloadFolderAction_actionPerformed_(String pkg);

    @Default(lngs = { "en" }, values = { "Change Download Folder for whole Package %s1, or only for %s2 selected link(s)" })
    String SetDownloadFolderAction_msg(String name, int num);

    @Default(lngs = { "en" }, values = { "Package" })
    String SetDownloadFolderAction_yes();

    @Default(lngs = { "en" }, values = { "Selection" })
    String SetDownloadFolderAction_no();

    @Default(lngs = { "en" }, values = { "Downloads are in progress!" })
    String DownloadWatchDog_onShutdownRequest_();

    @Default(lngs = { "en" }, values = { "Non resumable downloads are in progress!" })
    String DownloadWatchDog_onShutdownRequest_nonresumable();

    @Default(lngs = { "en" }, values = { "Do you want to stop running downloads to exit JDownloader?" })
    String DownloadWatchDog_onShutdownRequest_msg();

    @Default(lngs = { "en" }, values = { "LinkCollector is still in progress!" })
    String LinkCollector_onShutdownRequest_();

    @Default(lngs = { "en" }, values = { "Extraction is still in progress!" })
    String Extraction_onShutdownRequest_();

    @Default(lngs = { "en" }, values = { "Abort Extraction?" })
    String Extraction_onShutdownRequest_msg();

    @Default(lngs = { "en" }, values = { "Do you want to stop LinkCollector?" })
    String LinkCollector_onShutdownRequest_msg();

    @Default(lngs = { "en" }, values = { "Yes" })
    String literally_yes();

    @Default(lngs = { "en" }, values = { "Starting Downloads" })
    String Main_run_autostart_();

    @Default(lngs = { "en" }, values = { "Downloads will start a few seconds..." })
    String Main_run_autostart_msg();

    @Default(lngs = { "en" }, values = { "Start NOW!" })
    String Mainstart_now();

    @Default(lngs = { "en" }, values = { "Start Downloads" })
    String StartDownloadsAction_createTooltip_();

    @Default(lngs = { "en" }, values = { "Create Subfolder by Packagename" })
    String PackagizerSettings_folderbypackage_rule_name();

    @DescriptionForTranslationEntry("All words and all variants for the word 'password' should be placed here, seperated by a |. Example: passwort|pass|pw")
    @Default(lngs = { "en" }, values = { "пароль|пасс|pa?s?w|passwort|password|passw?|pw" })
    String pattern_password();

}