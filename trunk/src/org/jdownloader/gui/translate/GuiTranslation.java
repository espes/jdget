package org.jdownloader.gui.translate;

import org.appwork.txtresource.Default;
import org.appwork.txtresource.Defaults;
import org.appwork.txtresource.DescriptionForTranslationEntry;
import org.appwork.txtresource.TranslateInterface;
import org.appwork.utils.net.httpconnection.HTTPProxy;

@Defaults(lngs = { "en" })
public interface GuiTranslation extends TranslateInterface {

    @Default(lngs = { "en" }, values = { "About JDownloader" })
    String action_aboutaction();

    @Default(lngs = { "en" }, values = { "About JDownloader" })
    String action_aboutaction_tooltip();

    @Default(lngs = { "en" }, values = { "Add a new Premium Account" })
    String action_add_premium_account_tooltip();

    @Default(lngs = { "en" }, values = { "Add Container" })
    String action_addcontainer();

    String action_addcontainer_tooltip();

    @Default(lngs = { "en" }, values = { "Changelog" })
    String action_changelog();

    String action_changelog_tooltip();

    @Default(lngs = { "en" }, values = { "Enable or Disable Clipboard Observer." })
    String action_clipboard_observer_tooltip();

    @Default(lngs = { "en" }, values = { "Exit" })
    String action_exit();

    String action_exit_tooltip();

    @Default(lngs = { "en" }, values = { "Help" })
    String action_help();

    String action_help_tooltip();

    @Default(lngs = { "en" }, values = { "Open the default download destination" })
    String action_open_dlfolder_tooltip();

    @Default(lngs = { "en" }, values = { "Enable or disable all Premium Accounts" })
    String action_premium_toggle_tooltip();

    @Default(lngs = { "en" }, values = { "Perform a Reconnect, to get a new dynamic IP" })
    String action_reconnect_invoke_tooltip();

    @Default(lngs = { "en" }, values = { "Enable or Disable Auto-Reconnection" })
    String action_reconnect_toggle_tooltip();

    // action_//
    @Default(lngs = { "en" }, values = { "Restart" })
    String action_restart();

    @Default(lngs = { "en" }, values = { "Restart JDownloader" })
    String action_restart_tooltip();

    // action_//
    @Default(lngs = { "en" }, values = { "Settings" })
    String action_settings_menu();

    @Default(lngs = { "en" }, values = { "Open Settings panel" })
    String action_settings_menu_tooltip();

    @Default(lngs = { "en" }, values = { "Check for new updates." })
    String action_start_update_tooltip();

    @Default(lngs = { "en" }, values = { "Stops all running Downloads" })
    String action_stop_downloads_tooltip();

    @Default(lngs = { "en" }, values = { "Use Logins" })
    String authtablemodel_column_enabled();

    @Default(lngs = { "en" }, values = { "Host/URL" })
    String authtablemodel_column_host();

    @Default(lngs = { "en" }, values = { "Password" })
    String authtablemodel_column_password();

    @Default(lngs = { "en" }, values = { "Servertype" })
    String authtablemodel_column_type();

    @Default(lngs = { "en" }, values = { "ftp://" })
    String authtablemodel_column_type_ftp();

    @Default(lngs = { "en" }, values = { "http://" })
    String authtablemodel_column_type_http();

    @Default(lngs = { "en" }, values = { "Username" })
    String authtablemodel_column_username();

    @Default(lngs = { "en" }, values = { "Extension Modules" })
    String extensionManager_title();

    @Default(lngs = { "en" }, values = { "Browse" })
    String gui_btn_select();

    @Default(lngs = { "en" }, values = { "Enter Captcha for %s1 to continue!" })
    String gui_captchaWindow_askForInput(String hoster);

    @Default(lngs = { "en" }, values = { "Host/IP" })
    String gui_column_host();

    @Default(lngs = { "en" }, values = { "Password" })
    String gui_column_pass();

    @Default(lngs = { "en" }, values = { "Port" })
    String gui_column_port();

    @Default(lngs = { "en" }, values = { "Proxytype" })
    String gui_column_proxytype();

    @Default(lngs = { "en" }, values = { "Use" })
    String gui_column_use();

    @Default(lngs = { "en" }, values = { "Username" })
    String gui_column_user();

    @Default(lngs = { "en" }, values = { "SFV/CRC check when possible" })
    String gui_config_download_crc();

    @Default(lngs = { "en" }, values = { "Maximum of simultaneous downloads per host" })
    String gui_config_download_simultan_downloads_per_host2();

    @Default(lngs = { "en" }, values = { "File writing" })
    String gui_config_download_write();

    @Default(lngs = { "en" }, values = { "Download directory" })
    String gui_config_general_downloaddirectory();

    @Default(lngs = { "en" }, values = { "Remove finished downloads ..." })
    String gui_config_general_todowithdownloads();

    @Default(lngs = { "en" }, values = { "at startup" })
    String gui_config_general_toDoWithDownloads_atstart();

    @Default(lngs = { "en" }, values = { "immediately" })
    String gui_config_general_toDoWithDownloads_immediate();

    @Default(lngs = { "en" }, values = { "never" })
    String gui_config_general_toDoWithDownloads_never();

    @Default(lngs = { "en" }, values = { "when package is ready" })
    String gui_config_general_toDoWithDownloads_packageready();

    @Default(lngs = { "en" }, values = { "Auto open Link Containers (dlc,ccf,...)" })
    String gui_config_simple_container();

    @Default(lngs = { "en" }, values = { "Miscellaneous" })
    String gui_config_various();

    @Default(lngs = { "en" }, values = { "Message" })
    String gui_dialogs_message_title();

    @Default(lngs = { "en" }, values = { "Load DLC file" })
    String gui_filechooser_loaddlc();

    @Default(lngs = { "en" }, values = { "New Package Name" })
    String gui_linklist_editpackagename_message();

    @Default(lngs = { "en" }, values = { "Help" })
    String gui_menu_about();

    @Default(lngs = { "en" }, values = { "Pause downloads. Limits global speed to %s1 KiB/s" })
    String gui_menu_action_break2_desc(Object s1);

    @Default(lngs = { "en" }, values = { "action.premium.buy" })
    String gui_menu_action_premium_buy_name();

    @Default(lngs = { "en" }, values = { "Do you want to reconnect your internet connection?" })
    String gui_reconnect_confirm();

    @Default(lngs = { "en" }, values = { "Advanced Settings" })
    String gui_settings_advanced_title();

    @Default(lngs = { "en" }, values = { "Open in Browser" })
    String gui_table_contextmenu_browselink();

    @Default(lngs = { "en" }, values = { "Check Online Status" })
    String gui_table_contextmenu_check();

    @Default(lngs = { "en" }, values = { "Create DLC" })
    String gui_table_contextmenu_dlc();

    @Default(lngs = { "en" }, values = { "Open Directory" })
    String gui_table_contextmenu_downloaddir();

    @Default(lngs = { "en" }, values = { "Change Package Name" })
    String gui_table_contextmenu_editpackagename();

    @Default(lngs = { "en" }, values = { "Move into new Package" })
    String gui_table_contextmenu_newpackage();

    @Default(lngs = { "en" }, values = { "Open File" })
    String gui_table_contextmenu_openfile();

    @Default(lngs = { "en" }, values = { "Reset" })
    String gui_table_contextmenu_reset();

    @Default(lngs = { "en" }, values = { "Resume" })
    String gui_table_contextmenu_resume();

    @Default(lngs = { "en" }, values = { "Stop after this download" })
    String gui_table_contextmenu_stopmark_set();

    @Default(lngs = { "en" }, values = { "Remove Stopmark" })
    String gui_table_contextmenu_stopmark_unset();

    @Default(lngs = { "en" }, values = { "Watch As you download" })
    String gui_table_contextmenu_watch_as_you_download();

    @Default(lngs = { "en" }, values = { "Plugin error" })
    String gui_treetable_error_plugin();

    @Default(lngs = { "en" }, values = { "Low Priority" })
    String gui_treetable_tooltip_priority_1();

    @Default(lngs = { "en" }, values = { "Default Priority" })
    String gui_treetable_tooltip_priority0();

    @Default(lngs = { "en" }, values = { "High Priority" })
    String gui_treetable_tooltip_priority1();

    @Default(lngs = { "en" }, values = { "Higher Priority" })
    String gui_treetable_tooltip_priority2();

    @Default(lngs = { "en" }, values = { "Highest Priority" })
    String gui_treetable_tooltip_priority3();

    @Default(lngs = { "en" }, values = { "File" })
    String jd_gui_skins_simple_simplegui_menubar_filemenu();

    @Default(lngs = { "en" }, values = { "Contributors" })
    String jd_gui_swing_components_AboutDialog_contributers();

    @Default(lngs = { "en" }, values = { "Support board" })
    String jd_gui_swing_components_AboutDialog_forum();

    @Default(lngs = { "en" }, values = { "Homepage" })
    String jd_gui_swing_components_AboutDialog_homepage();

    @Default(lngs = { "en" }, values = { "Show license" })
    String jd_gui_swing_components_AboutDialog_license();

    @Default(lngs = { "en" }, values = { "JDownloader License" })
    String jd_gui_swing_components_AboutDialog_license_title();

    @Default(lngs = { "en" }, values = { "About JDownloader" })
    String jd_gui_swing_components_AboutDialog_title();

    @Default(lngs = { "en" }, values = { "Name:" })
    String jd_gui_swing_components_AccountDialog_name();

    @Default(lngs = { "en" }, values = { "Pass:" })
    String jd_gui_swing_components_AccountDialog_pass();

    @Default(lngs = { "en" }, values = { "Add new Account" })
    String jd_gui_swing_components_AccountDialog_title();

    @Default(lngs = { "en" }, values = { "Host/Port:" })
    String jd_gui_swing_dialog_ProxyDialog_hostport();

    @Default(lngs = { "en" }, values = { "HTTP" })
    String jd_gui_swing_dialog_ProxyDialog_http();

    @Default(lngs = { "en" }, values = { "Password:" })
    String jd_gui_swing_dialog_ProxyDialog_password();

    @Default(lngs = { "en" }, values = { "Socks5" })
    String jd_gui_swing_dialog_ProxyDialog_socks5();

    @Default(lngs = { "en" }, values = { "Socks4" })
    String jd_gui_swing_dialog_ProxyDialog_socks4();

    @Default(lngs = { "en" }, values = { "Direct" })
    String jd_gui_swing_dialog_ProxyDialog_direct();

    @Default(lngs = { "en" }, values = { "Add new Proxy" })
    String jd_gui_swing_dialog_ProxyDialog_title();

    @Default(lngs = { "en" }, values = { "Type:" })
    String jd_gui_swing_dialog_ProxyDialog_type();

    @Default(lngs = { "en" }, values = { "Username:" })
    String jd_gui_swing_dialog_ProxyDialog_username();

    @Default(lngs = { "en" }, values = { "This option needs a JDownloader restart." })
    String jd_gui_swing_jdgui_settings_ConfigPanel_restartquestion();

    @Default(lngs = { "en" }, values = { "Restart NOW!" })
    String jd_gui_swing_jdgui_settings_ConfigPanel_restartquestion_ok();

    @Default(lngs = { "en" }, values = { "Restart required!" })
    String jd_gui_swing_jdgui_settings_ConfigPanel_restartquestion_title();

    @Default(lngs = { "en" }, values = { "Settings" })
    String jd_gui_swing_jdgui_views_configurationview_tab_title();

    @Default(lngs = { "en" }, values = { "All options and settings for JDownloader" })
    String jd_gui_swing_jdgui_views_configurationview_tab_tooltip();

    @Default(lngs = { "en" }, values = { "Download" })
    String jd_gui_swing_jdgui_views_downloadview_tab_title();

    @Default(lngs = { "en" }, values = { "Downloadlist and Progress" })
    String jd_gui_swing_jdgui_views_downloadview_tab_tooltip();

    @Default(lngs = { "en" }, values = { "Stopmark is set" })
    String jd_gui_swing_jdgui_views_downloadview_TableRenderer_stopmark();

    @Default(lngs = { "en" }, values = { "Linkgrabber" })
    String jd_gui_swing_jdgui_views_linkgrabberview_tab_title();

    @Default(lngs = { "en" }, values = { "Collect, add and select links and URLs" })
    String jd_gui_swing_jdgui_views_linkgrabberview_tab_tooltip();

    @Default(lngs = { "en" }, values = { "Please confirm!" })
    String jd_gui_userio_defaulttitle_confirm();

    @Default(lngs = { "en" }, values = { "Please enter!" })
    String jd_gui_userio_defaulttitle_input();

    @Default(lngs = { "en" }, values = { "offline" })
    String linkgrabber_onlinestatus_offline();

    @Default(lngs = { "en" }, values = { "online" })
    String linkgrabber_onlinestatus_online();

    @Default(lngs = { "en" }, values = { "temp. uncheckable" })
    String linkgrabber_onlinestatus_uncheckable();

    @Default(lngs = { "en" }, values = { "not checked" })
    String linkgrabber_onlinestatus_unchecked();

    @Default(lngs = { "en" }, values = { "dd.MM.yy HH:mm" })
    String added_date_column_dateformat();

    @Default(lngs = { "en" }, values = { "Actions" })
    String premiumaccounttablemodel_column_actions();

    @Default(lngs = { "en" }, values = { "Enabled" })
    String premiumaccounttablemodel_column_enabled();

    @Default(lngs = { "en" }, values = { "Expire Date" })
    String premiumaccounttablemodel_column_expiredate();

    @Default(lngs = { "en" }, values = { "Hoster" })
    String premiumaccounttablemodel_column_hoster();

    @Default(lngs = { "en" }, values = { "Password" })
    String premiumaccounttablemodel_column_password();

    @Default(lngs = { "en" }, values = { "Download Traffic left" })
    String premiumaccounttablemodel_column_trafficleft();

    @Default(lngs = { "en" }, values = { "Unlimited" })
    String premiumaccounttablemodel_column_trafficleft_unlimited();

    @Default(lngs = { "en" }, values = { "Temp. disabled %s1" })
    String premiumaccounttablemodel_column_trafficleft_tempdisabled(String s);

    @Default(lngs = { "en" }, values = { "Username" })
    String premiumaccounttablemodel_column_user();

    @Default(lngs = { "en" }, values = { "Buy" })
    String settings_accountmanager_buy();

    @Default(lngs = { "en" }, values = { "Account Information" })
    String settings_accountmanager_info();

    @Default(lngs = { "en" }, values = { "Premiumzone" })
    String settings_accountmanager_premiumzone();

    @Default(lngs = { "en" }, values = { "Refresh" })
    String settings_accountmanager_refresh();

    @Default(lngs = { "en" }, values = { "Renew / Buy new Account" })
    String settings_accountmanager_renew();

    @Default(lngs = { "en" }, values = { "Others" })
    String settings_linkgrabber_filter_others();

    @Default(lngs = { "en" }, values = { "Add" })
    String settings_linkgrabber_filter_action_add();

    @Default(lngs = { "en" }, values = { "Enable/Disable" })
    String settings_linkgrabber_filter_columns_enabled();

    @Default(lngs = { "en" }, values = { "If the file already exists:" })
    String system_download_triggerfileexists();

    @Default(lngs = { "en" }, values = { "Ask for each file" })
    String system_download_triggerfileexists_ask();

    @Default(lngs = { "en" }, values = { "Overwrite" })
    String system_download_triggerfileexists_overwrite();

    @Default(lngs = { "en" }, values = { "Auto rename" })
    String system_download_triggerfileexists_rename();

    @Default(lngs = { "en" }, values = { ".*(error|failed).*" })
    String userio_errorregex();

    @Default(lngs = { "en" }, values = { "No Proxy" })
    String gui_column_proxytype_no_proxy();

    @Default(lngs = { "en" }, values = { "HTTP Proxy" })
    String gui_column_proxytype_http();

    @Default(lngs = { "en" }, values = { "Socks 5 Proxy" })
    String gui_column_proxytype_socks5();

    @Default(lngs = { "en" }, values = { "Use for Proxy Rotation" })
    String gui_column_proxytype_rotation_check();

    @Default(lngs = { "en" }, values = { "Direct Gateway" })
    String gui_column_proxytype_direct();

    @Default(lngs = { "en" }, values = { "If you have several external gateways, you can use each like a different external ip." })
    String gui_column_proxytype_direct_tt();

    @Default(lngs = { "en" }, values = { "No Proxy! Use the default direct connection." })
    String gui_column_proxytype_no_proxy_tt();

    @Default(lngs = { "en" }, values = { "Hypertext Transfer Protocol (HTTP Proxy)" })
    String gui_column_proxytype_http_tt();

    @Default(lngs = { "en" }, values = { "SOCKS-5-Protocol Proxy Server " })
    String gui_column_proxytype_socks5_tt();

    @Default(lngs = { "en" }, values = { "SOCKS-4-Protocol Proxy Server " })
    String gui_column_proxytype_socks4_tt();

    @Default(lngs = { "en" }, values = { "Proxy Rotation requires at least one active entry." })
    String proxytablemodel_atleast_one_rotate_required();

    @Default(lngs = { "en" }, values = { "Buy a Premium Account" })
    String buyaction_title();

    @Default(lngs = { "en" }, values = { "Most Hosters offer a \"Premium Mode\". Download in Premium Mode is usually much faster." })
    String buyaction_message();

    @Default(lngs = { "en" }, values = { "Continue" })
    String buyaction_title_buy_account();

    @Default(lngs = { "en" }, values = { "Check Account" })
    String accountdialog_check();

    @Default(lngs = { "en" }, values = { "JDownloader checks if logins are correct" })
    String accountdialog_check_msg();

    @Default(lngs = { "en" }, values = { "Cannot add Account because %s1" })
    String accountdialog_check_invalid(String status);

    @Default(lngs = { "en" }, values = { "Verified logins:\r\n%s1" })
    String accountdialog_check_valid(String status);

    @Default(lngs = { "en" }, values = { "Accountcheck Failed" })
    String accountdialog_check_failed();

    @Default(lngs = { "en" }, values = { "Account check failed.\r\nPlease make sure that your entered logins are correct." })
    String accountdialog_check_failed_msg();

    @Default(lngs = { "en" }, values = { "Accountlogins are correct, but your Account has expired." })
    String accountdialog_check_expired(String status);

    @Default(lngs = { "en" }, values = { "Renew Account?" })
    String accountdialog_check_expired_title();

    @Default(lngs = { "en" }, values = { "Renew/Extend Account now!" })
    String accountdialog_check_expired_renew();

    @Default(lngs = { "en" }, values = { "Enter Username..." })
    String jd_gui_swing_components_AccountDialog_help_username();

    @Default(lngs = { "en" }, values = { "Really remove %s1 account(s)?" })
    String account_remove_action_title(int num);

    @Default(lngs = { "en" }, values = { "Really remove %s1" })
    String account_remove_action_msg(String string);

    @Default(lngs = { "en" }, values = { "n.a." })
    String added_date_column_invalid();

    @Default(lngs = { "en" }, values = { "Added Date" })
    String added_date_column_title();

    @Default(lngs = { "en" }, values = { "Name" })
    String filecolumn_title();

    @Default(lngs = { "en" }, values = { "Finished Date" })
    String FinishedDateColumn_FinishedDateColumn();

    @Default(lngs = { "en" }, values = { "Size" })
    String SizeColumn_SizeColumn();

    @Default(lngs = { "en" }, values = { "Watch As You Download" })
    String WatchAsYouDownloadColumn_WatchAsYouDownloadColumn();

    @Default(lngs = { "en" }, values = { "Bytes Left" })
    String RemainingColumn_RemainingColumn();

    @Default(lngs = { "en" }, values = { "Priority" })
    String PriorityColumn_PriorityColumn();

    @Default(lngs = { "en" }, values = { "Bytes Loaded" })
    String LoadedColumn_LoadedColumn();

    @Default(lngs = { "en" }, values = { "Hoster" })
    String HosterColumn_HosterColumn();

    @Default(lngs = { "en" }, values = { "Status" })
    String StatusColumn_StatusColumn();

    @Default(lngs = { "en" }, values = { "Download Control" })
    String StopSignColumn_StopSignColumn();

    @Default(lngs = { "en" }, values = { "Availability" })
    String AvailabilityColumn_AvailabilityColumn();

    @Default(lngs = { "en" }, values = { "Progress" })
    String ProgressColumn_ProgressColumn();

    @Default(lngs = { "en" }, values = { "ETA" })
    String ETAColumn_ETAColumn();

    @Default(lngs = { "en" }, values = { "Speed" })
    String SpeedColumn_SpeedColumn();

    @Default(lngs = { "en" }, values = { "Move selected Links & Packages to top" })
    String BottomBar_BottomBar_totop_tooltip();

    @Default(lngs = { "en" }, values = { "Move selected Links & Packages up" })
    String BottomBar_BottomBar_moveup_tooltip();

    @Default(lngs = { "en" }, values = { "Move selected Links & Packages down" })
    String BottomBar_BottomBar_movedown_tooltip();

    @Default(lngs = { "en" }, values = { "Move selected Links & Packages to bottom" })
    String BottomBar_BottomBar_tobottom_tooltip();

    @Default(lngs = { "en" }, values = { "Connection" })
    String ConnectionColumn_ConnectionColumn();

    @Default(lngs = { "en" }, values = { "Connection: %s1" })
    String ConnectionColumn_getStringValue_connection(HTTPProxy currentProxy);

    @Default(lngs = { "en" }, values = { "Downloading with %s1 chunk(s)" })
    String ConnectionColumn_getStringValue_chunks(int currentChunks);

    @Default(lngs = { "en" }, values = { "Sorted by '%s1'-Column" })
    String DownloadsTable_actionPerformed_sortwarner_title(String column);

    @Default(lngs = { "en" }, values = { "Your Download list is not in download order any more. \r\nClick twice on the highlighted column header,\r\nto return to default (Top-Down) order." })
    String DownloadsTable_actionPerformed_sortwarner_text();

    @Default(lngs = { "en" }, values = { "Comment" })
    String CommentColumn_CommentColumn_();

    @Default(lngs = { "en" }, values = { "Views" })
    String LinkGrabberSideBarHeader_LinkGrabberSideBarHeader();

    @Default(lngs = { "en" }, values = { "Add New Links" })
    @DescriptionForTranslationEntry("Add Links Button in Linkgrabber bottom left")
    String AddLinksToLinkgrabberAction();

    @Default(lngs = { "en" }, values = { "Restore %s1 filtered Links" })
    String RestoreFilteredLinksAction_(int x);

    @Default(lngs = { "en" }, values = { "Hoster" })
    String LinkGrabberSidebar_LinkGrabberSidebar_hosterfilter();

    @Default(lngs = { "en" }, values = { "File Types" })
    String LinkGrabberSidebar_LinkGrabberSidebar_extensionfilter();

    @Default(lngs = { "en" }, values = { "Add at top" })
    String LinkGrabberSidebar_LinkGrabberSidebar_addtop();

    @Default(lngs = { "en" }, values = { "Adds links at top of download list. They will be next in downloadorder" })
    String LinkGrabberSidebar_LinkGrabberSidebar_addtop_tt();

    @Default(lngs = { "en" }, values = { "Auto confirm" })
    String LinkGrabberSidebar_LinkGrabberSidebar_autoconfirm();

    @Default(lngs = { "en" }, values = { "If enabled, Links will be moved to downloadlist automatically after a given timeout" })
    String LinkGrabberSidebar_LinkGrabberSidebar_autoconfirm_tt();

    @Default(lngs = { "en" }, values = { "Autostart Download" })
    String LinkGrabberSidebar_LinkGrabberSidebar_autostart();

    @Default(lngs = { "en" }, values = { "Starts downloading after adding links to the downloadlist" })
    String LinkGrabberSidebar_LinkGrabberSidebar_autostart_tt();

    @Default(lngs = { "en" }, values = { "Load Linkcontainer" })
    String AddContainerAction();

    @Default(lngs = { "en" }, values = { "Analyse Text with Links" })
    String AddOptionsAction_actionPerformed_addlinks();

    @Default(lngs = { "en" }, values = { "Are you sure?" })
    String literally_are_you_sure();

    @Default(lngs = { "en" }, values = { "Yes" })
    String literally_yes();

    @Default(lngs = { "en" }, values = { "No" })
    String literall_no();

    @Default(lngs = { "en" }, values = { "Load a Link Container" })
    String AddContainerAction_actionPerformed_();

    @Default(lngs = { "en" }, values = { "Link Container (%s1)" })
    String AddContainerAction_actionPerformed_extensions(String containerExtensions);

    @Default(lngs = { "en" }, values = { "Download All" })
    String ConfirmOptionsAction_actionPerformed_all();

    @Default(lngs = { "en" }, values = { "Download Selected" })
    String ConfirmOptionsAction_actionPerformed_selected();

    @Default(lngs = { "en" }, values = { "Analyse and Add Links" })
    String AddLinksDialog_AddLinksDialog_();

    @Default(lngs = { "en" }, values = { "Continue" })
    String AddLinksDialog_AddLinksDialog_confirm();

    @Default(lngs = { "en" }, values = { "JDownloader helps you to parse text or websites for links. Enter Links, URLs, Websites, or text below, \r\nchoose a Download Destination, and click 'Start Analyse'. \r\nAll 'downloadable' Files will be listed in the the Linkgrabber view afterwards." })
    String AddLinksDialog_layoutDialogContent_description();

    @Default(lngs = { "en" }, values = { "Enter Links, URLs, Websites, or any other text here..." })
    String AddLinksDialog_layoutDialogContent_input_help();

    @Default(lngs = { "en" }, values = { "Please choose Download Destination here." })
    String AddLinksDialog_layoutDialogContent_save_tt();

    @Default(lngs = { "en" }, values = { "Choose a Package Name for the Downloads above. If empty, JDownloader will create Packages based on the filenames" })
    String AddLinksDialog_layoutDialogContent_package_tt();

    @Default(lngs = { "en" }, values = { "Enter a Package name, or leave empty for auto mode" })
    String AddLinksDialog_layoutDialogContent_packagename_help();

    @Default(lngs = { "en" }, values = { "Enter the archive's extraction password" })
    String AddLinksDialog_createExtracOptionsPanel_password();

    @Default(lngs = { "en" }, values = { "Enable this option to extract all found archives after download" })
    String AddLinksDialog_layoutDialogContent_autoextract_tooltip();

    @Default(lngs = { "en" }, values = { "Start Deep Link Analyse" })
    String ConfirmOptionsAction_actionPerformed_deep();

    @Default(lngs = { "en" }, values = { "Start Normal Link Analyse" })
    String ConfirmOptionsAction_actionPerformed_normale();

    @Default(lngs = { "en" }, values = { "Please enter Urls, Links, Websites, or plain text" })
    String AddLinksDialog_validateForm_input_missing();

    @Default(lngs = { "en" }, values = { "Please make sure to enter a valid Download Destination." })
    String AddLinksDialog_validateForm_folder_invalid_missing();

    @Default(lngs = { "en" }, values = { "Please enter a valid Download Folder" })
    String AddLinksDialog_layoutDialogContent_help_destination();

    @Default(lngs = { "en" }, values = { "Reconnect Activity" })
    String StatusBarImpl_initGUI_reconnect();

    @Default(lngs = { "en" }, values = { "Linkgrabber Activity" })
    String StatusBarImpl_initGUI_linkgrabber();

    @Default(lngs = { "en" }, values = { "Abort Linkgrabber" })
    String StatusBarImpl_initGUI_abort_linkgrabber();

    @Default(lngs = { "en" }, values = { "Extraction Activity" })
    String StatusBarImpl_initGUI_extract();

    @Default(lngs = { "en" }, values = { "JDownloader is crawling links for you. Open Linkgrabber to see the results." })
    String StatusBarImpl_initGUI_linkgrabber_desc();

    @Default(lngs = { "en" }, values = { " - idle - " })
    String StatusBarImpl_initGUI_linkgrabber_desc_inactive();

    @Default(lngs = { "en" }, values = { "Continue" })
    String literally_continue();

    @Default(lngs = { "en" }, values = { "Warning" })
    String literally_warning();

    @Default(lngs = { "en" }, values = { "Error" })
    String literall_error();

    @Default(lngs = { "en" }, values = { "Edit" })
    String literally_edit();

    @Default(lngs = { "en" }, values = { "Test" })
    String ReconnectTestAction();

    @Default(lngs = { "en" }, values = { "Reconnect" })
    String ReconnectDialog_ReconnectDialog_();

    @Default(lngs = { "en" }, values = { "Duration: " })
    String ReconnectDialog_layoutDialogContent_duration();

    @Default(lngs = { "en" }, values = { "Old IP: " })
    String ReconnectDialog_layoutDialogContent_old();

    @Default(lngs = { "en" }, values = { "Reconnect Plugin: %s1" })
    String ReconnectDialog_layoutDialogContent_header(String name);

    @Default(lngs = { "en" }, values = { "Forbidden IP: %s1" })
    String ReconnectDialog_onIPForbidden_(String externalIp);

    @Default(lngs = { "en" }, values = { "Router is Offline" })
    String ReconnectDialog_onIPOffline_();

    @Default(lngs = { "en" }, values = { "Router is Online" })
    String ReconnectDialog_onIPOnline_();

    @Default(lngs = { "en" }, values = { "Reconnect Successful" })
    String ReconnectDialog_onIPValidated_();

    @Default(lngs = { "en" }, values = { "Close" })
    String literally_close();

    @Default(lngs = { "en" }, values = { "-offline-" })
    String literally_offline();

    @Default(lngs = { "en" }, values = { "Current External IP" })
    String ReconnectDialog_layoutDialogContent_currentip();

    @Default(lngs = { "en" }, values = { "Auto Setup Reconnect" })
    String AutoDetectAction_actionPerformed_d_title();

    @Default(lngs = { "en" }, values = { "Automatic Proxy Detection" })
    String ProxyAutoAction_actionPerformed_d_title();

    @Default(lngs = { "en" }, values = { "Please wait. JDownloader tries to find the correct settings for your internet connection. \r\nThis may take several minutes." })
    String AutoDetectAction_actionPerformed_d_msg();

    @Default(lngs = { "en" }, values = { "It seems that you are using a Direct Modem Connection to access the internet.\r\nReconnect for Modem Connections have to be set up manually.\r\n\r\nDo you use a Router or a Modem to connect to the internet?" })
    String AutoSetupAction_actionPerformed_modem();

    @Default(lngs = { "en" }, values = { "Modem/Dial-Up" })
    String AutoSetupAction_actionPerformed_choose_modem();

    @Default(lngs = { "en" }, values = { "I'm not sure!" })
    String AutoSetupAction_actionPerformed_dont_know();

    @Default(lngs = { "en" }, values = { "Router" })
    String AutoSetupAction_actionPerformed_router();

    @Default(lngs = { "en" }, values = { "Auto Setup only works for Router connections. \r\nPlease see the opened Site for Modem Reconnects." })
    String AutoSetupAction_actionPerformed_noautoformodem();

    @Default(lngs = { "en" }, values = { "Reconnect failed. Please check your settings." })
    String ReconnectDialog_layoutDialogContent_error();

    @Default(lngs = { "en" }, values = { "Use found Script" })
    String ReconnectFindDialog_ReconnectFindDialog_ok();

    @Default(lngs = { "en" }, values = { "No working Script found so far." })
    String ReconnectFindDialog_packed_no_found_script_tooltip();

    @Default(lngs = { "en" }, values = { "Stop Scanning, and use the best script found until now." })
    String ReconnectFindDialog_packed_interrupt_tooltip();

    @Default(lngs = { "en" }, values = { "Optimize Reconnect - Do tests to improve timing ..." })
    String AutoDetectAction_actionPerformed_dooptimization_title();

    @Default(lngs = { "en" }, values = { "JDownloader found %s1 working Reconnect Script(s). \r\nTo improve Reconnect Timings, you should do some automatic tests runs. These tests would take about %s2. \r\nIf you skip this step, your current Reconnect Settings will need about %s3 to perform a successful Reconnect." })
    String AutoDetectAction_actionPerformed_dooptimization_msg(int scriptNum, String optiDuration, String bestDuration);

    @Default(lngs = { "en" }, values = { "Continue" })
    String AutoDetectAction_run_optimization();

    @Default(lngs = { "en" }, values = { "Skip" })
    String AutoDetectAction_skip_optimization();

    @Default(lngs = { "en" }, values = { "Found successful Script" })
    String LiveHeaderDetectionWizard_testList_firstSuccess_title();

    @Default(lngs = { "en" }, values = { "JDownloader found a working Reconnect Script. \r\nGetting a new IP took %s1 with this Script. \r\nThere might be a faster one. Do you want JDownloader to continue scanning for faster scripts?" })
    String LiveHeaderDetectionWizard_testList_firstsuccess_msg(String time);

    @Default(lngs = { "en" }, values = { "Yes, please try it!" })
    String LiveHeaderDetectionWizard_testList_ok();

    @Default(lngs = { "en" }, values = { "No, stop search and use this script!" })
    String LiveHeaderDetectionWizard_testList_use();

    @Default(lngs = { "en" }, values = { "Optimizing  %s1" })
    String AutoDetectAction_run_optimize(String name);

    @Default(lngs = { "en" }, values = { "Offline - Waiting for a internet connection!" })
    String LiveaheaderDetection_wait_for_online();

    @Default(lngs = { "en" }, values = { "This Wizard will scan your Network configuration, and try to find Reconnect Scripts from our online Database.\r\nThis may take between a few seconds, and several minutes.\r\nOnly Dynamic IP DSL Connections via a Router can be autodetected." })
    String AutoSetupAction_tt();

    @Default(lngs = { "en" }, values = { "Click here do a Reconnect and get a new External IP." })
    String ReconnectTestAction_tt_2();

    @Default(lngs = { "en" }, values = { "Validating your Reconnect Script..." })
    String LiveHeaderDetectionWizard_sendRouter_havetovalidate();

    @Default(lngs = { "en" }, values = { "Rule Name" })
    String settings_linkgrabber_filter_columns_name();

    @Default(lngs = { "en" }, values = { "Save" })
    String literally_save();

    @Default(lngs = { "en" }, values = { "Rule: %s1" })
    String FilterRuleDialog_FilterRuleDialog_(String name);

    @Default(lngs = { "en" }, values = { "Enter short name for this rule..." })
    String FilterRuleDialog_layoutDialogContent_ht_name();

    @Default(lngs = { "en" }, values = { "equals" })
    String FilterRuleDialog_layoutDialogContent_equals();

    @Default(lngs = { "en" }, values = { "contains" })
    String FilterRuleDialog_layoutDialogContent_contains();

    @Default(lngs = { "en" }, values = { "Enter file name matcher... (Use * as Wildcard)" })
    String FilterRuleDialog_layoutDialogContent_ht_filename();

    @Default(lngs = { "en" }, values = { "File Name" })
    String FilterRuleDialog_layoutDialogContent_lbl_filename();

    @Default(lngs = { "en" }, values = { "File Size" })
    String FilterRuleDialog_layoutDialogContent_lbl_size();

    @Default(lngs = { "en" }, values = { "Unsupported Network Settings?" })
    String ReconnectPluginController_autoFind_gatewaywarning_t();

    @Default(lngs = { "en" }, values = { "It seems that your network setup is not supported by Reconnect Wizard.\r\nThis Wizard is able to find Reconnect Settings for 'DSL via Router' - Connections.\r\nClick [Continue] to try it anyway." })
    String ReconnectPluginController_autoFind_gatewaywarning();

    @Default(lngs = { "en" }, values = { "Checking Network Settings..." })
    String LiveaheaderDetection_network_setup_check();

    @Default(lngs = { "en" }, values = { "Find Router IP..." })
    String LiveaheaderDetection_find_router();

    @Default(lngs = { "en" }, values = { "File Type" })
    String FilterRuleDialog_layoutDialogContent_lbl_type();

    @Default(lngs = { "en" }, values = { "Audio File" })
    String FilterRuleDialog_createTypeFilter_mime_audio();

    @Default(lngs = { "en" }, values = { "Video File" })
    String FilterRuleDialog_createTypeFilter_mime_video();

    @Default(lngs = { "en" }, values = { "Archive" })
    String FilterRuleDialog_createTypeFilter_mime_archives();

    @Default(lngs = { "en" }, values = { "Image" })
    String FilterRuleDialog_createTypeFilter_mime_images();

    @Default(lngs = { "en" }, values = { "Enter comma seperated extensions like 'pdf,html,png,' ... (Use * as Wildcard)" })
    String FilterRuleDialog_createTypeFilter_mime_custom_help();

    @Default(lngs = { "en" }, values = { "Hoster" })
    String FilterRuleDialog_layoutDialogContent_lbl_hoster();

    @Default(lngs = { "en" }, values = { "Enter a URL Expression like 'rapidshare.com'... (Use * as Wildcard)" })
    String FilterRuleDialog_layoutDialogContent_lbl_hoster_help();

    @Default(lngs = { "en" }, values = { "Source" })
    String FilterRuleDialog_layoutDialogContent_lbl_source();

    @Default(lngs = { "en" }, values = { "Enter a Crawler Source Expression like 'jamendo.com'... (Use * as Wildcard)" })
    String FilterRuleDialog_layoutDialogContent_lbl_source_help();

    @Default(lngs = { "en" }, values = { "If the following conditions match ... " })
    String FilterRuleDialog_layoutDialogContent_if();

    @Default(lngs = { "en" }, values = { "Condition Name" })
    String FilterRuleDialog_layoutDialogContent_name();

    @Default(lngs = { "en" }, values = { "%s1!" })
    String settings_linkgrabber_filter_columns_if(String trim);

    @Default(lngs = { "en" }, values = { "contains %s1" })
    String RegexFilter_toString_contains(String regex);

    @Default(lngs = { "en" }, values = { "is %s1" })
    String RegexFilter_toString_matches(String regex);

    @Default(lngs = { "en" }, values = { "doesn't contain %s1" })
    String RegexFilter_toString_contains_not(String regex);

    @Default(lngs = { "en" }, values = { "isn't %s1" })
    String RegexFilter_toString_matches_not(String regex);

    @Default(lngs = { "en" }, values = { "is %s1" })
    String FilesizeFilter_toString_same(String size);

    @Default(lngs = { "en" }, values = { "is between %s1 and %s2" })
    String FilesizeFilter_toString_(String formatBytes, String formatBytes2);

    @Default(lngs = { "en" }, values = { "isn't %s1" })
    String FilesizeFilter_toString_same_not(String size);

    @Default(lngs = { "en" }, values = { "isn't between %s1 and %s2" })
    String FilesizeFilter_toString_not(String formatBytes, String formatBytes2);

    @Default(lngs = { "en" }, values = { "Filename %s1" })
    String FilterRule_toString_name(String string);

    @Default(lngs = { "en" }, values = { "Packagename %s1" })
    String FilterRule_toString_packagename(String string);

    @Default(lngs = { "en" }, values = { "\r\n%s1 %s2" })
    String FilterRule_toString_name2(String name, String string);

    @Default(lngs = { "en" }, values = { "\r\n%s1 %s2" })
    String FilterRule_toString_package2(String name, String string);

    @Default(lngs = { "en" }, values = { "Hoster URL %s1" })
    String FilterRule_toString_hoster(String string);

    @Default(lngs = { "en" }, values = { "\r\nURL(%s1) %s2" })
    String FilterRule_toString_hoster2(String a, String string);

    @Default(lngs = { "en" }, values = { "Source URL %s1" })
    String FilterRule_toString_source(String string);

    @Default(lngs = { "en" }, values = { ",\r\n%s1" })
    String FilterRule_toString_comma2(String string);

    @Default(lngs = { "en" }, values = { " and \r\n%s1" })
    String FilterRule_toString_and2(String string);

    @Default(lngs = { "en" }, values = { "Filter Links if, " })
    String settings_linkgrabber_filter_columns_condition();

    @Default(lngs = { "en" }, values = { "Size %s1" })
    String FilterRule_toString_size(String string);

    @Default(lngs = { "en" }, values = { "\r\nSize(%s1) %s2" })
    String FilterRule_toString_size2(String string, String string2);

    @Default(lngs = { "en" }, values = { "File %s1" })
    String FilterRule_toString_type(String string);

    @Default(lngs = { "en" }, values = { "\r\n%s1-File %s2" })
    String FilterRule_toString_type2(String ext, String string);

    @Default(lngs = { "en" }, values = { "'%s1'-File" })
    String FiletypeFilter_toString_custom(String customs);

    @Default(lngs = { "en" }, values = { " or %s1" })
    String FilterRule_toString_or(String string);

    @Default(lngs = { "en" }, values = { "is a %s1" })
    String FiletypeFilter_toString_(String string);

    @Default(lngs = { "en" }, values = { "isn't a %s1" })
    String FiletypeFilter_toString_not(String string);

    @Default(lngs = { "en" }, values = { "Packagizer" })
    String gui_config_linkgrabber_packagizer();

    @Default(lngs = { "en" }, values = { "Add" })
    String literally_add();

    @Default(lngs = { "en" }, values = { "Remove" })
    String literally_remove();

    @Default(lngs = { "en" }, values = { "Download Directory" })
    String PackagizerFilterRuleDialog_layoutDialogContent_dest();

    @Default(lngs = { "en" }, values = { "Move to" })
    String PackagizerFilterRuleDialog_layoutDialogContent_move();

    @Default(lngs = { "en" }, values = { "Priority" })
    String PackagizerFilterRuleDialog_layoutDialogContent_priority();

    @Default(lngs = { "en" }, values = { "Package Name" })
    String PackagizerFilterRuleDialog_layoutDialogContent_packagename();

    @Default(lngs = { "en" }, values = { "Enable Download" })
    String PackagizerFilterRuleDialog_layoutDialogContent_enable();

    @Default(lngs = { "en" }, values = { "Extract Archives" })
    String PackagizerFilterRuleDialog_layoutDialogContent_extract();

    @Default(lngs = { "en" }, values = { "Chunks/Connections" })
    String PackagizerFilterRuleDialog_layoutDialogContent_chunks();

    @Default(lngs = { "en" }, values = { "... then set (before downloading)" })
    String PackagizerFilterRuleDialog_layoutDialogContent_then();

    @Default(lngs = { "en" }, values = { "... then do ( these actions only apply to extracted files" })
    String PackagizerFilterRuleDialog_layoutDialogContent_do2();

    @Default(lngs = { "en" }, values = { "Date or Time" })
    String PackagizerFilterRuleDialog_createVariablesMenu_date();

    @Default(lngs = { "en" }, values = { "Dynamic Variables" })
    String PackagizerFilterRuleDialog_createVariablesMenu_menu();

    @Default(lngs = { "en" }, values = { "Original Filename Wildcard(*) #%s1" })
    String PackagizerFilterRuleDialog_createVariablesMenu_filename(int i);

    @Default(lngs = { "en" }, values = { "Hoster Url Wildcard(*) #%s1" })
    String PackagizerFilterRuleDialog_createVariablesMenu_hoster(int i);

    @Default(lngs = { "en" }, values = { "Source Url Wildcard(*) #%s1" })
    String PackagizerFilterRuleDialog_createVariablesMenu_source(int i);

    @Default(lngs = { "en" }, values = { "Packagename" })
    String PackagizerFilterRuleDialog_createVariablesMenu_packagename();

    @Default(lngs = { "en" }, values = { "Auto Start Download" })
    String PackagizerFilterRuleDialog_layoutDialogContent_autostart2();

    @Default(lngs = { "en" }, values = { "Auto Confirm" })
    String PackagizerFilterRuleDialog_layoutDialogContent_autoadd2();

    @Default(lngs = { "en" }, values = { "Settings" })
    String SettingsMenu_SettingsMenu_();

    @Default(lngs = { "en" }, values = { "Max. Chunks per Download" })
    String ChunksEditor_ChunksEditor_();

    @Default(lngs = { "en" }, values = { "Max. simultaneous Downloads" })
    String ParalellDownloadsEditor_ParalellDownloadsEditor_();

    @Default(lngs = { "en" }, values = { "Speed Limit" })
    String SpeedlimitEditor_SpeedlimitEditor_();

    @Default(lngs = { "en" }, values = { "%s1/s" })
    String SpeedlimitEditor_format(String formatBytes);

    @Default(lngs = { "en" }, values = { "Open Quicksettings" })
    String BottomBar_BottomBar_settings();

    @Default(lngs = { "en" }, values = { "Max. simultaneous Downloads" })
    String gui_config_download_simultan_downloads();

    @Default(lngs = { "en" }, values = { "Max. Chunks per Download" })
    String gui_config_download_max_chunks();

    @Default(lngs = { "en" }, values = { "Speedlimit: %s1/s" })
    String SpeedMeterPanel_createTooltipText_(String formatBytes);

    @Default(lngs = { "en" }, values = { "Import" })
    String LinkgrabberFilter_LinkgrabberFilter_import();

    @Default(lngs = { "en" }, values = { "Import Filterrules" })
    String LinkgrabberFilter_import_dialog_title();

    @Default(lngs = { "en" }, values = { "Rule File %s1 is empty or is invalid." })
    String LinkgrabberFilter_LinkgrabberFilter_import_invalid(String name);

    @Default(lngs = { "en" }, values = { "Export" })
    String LinkgrabberFilter_LinkgrabberFilter_export();

    @Default(lngs = { "en" }, values = { "Export to File" })
    String LinkgrabberFilter_export_dialog_title();

    @Default(lngs = { "en" }, values = { "Are you sure?" })
    String literall_are_you_sure();

    @Default(lngs = { "en" }, values = { "Global Link Filter" })
    String LinkGrabberSidebar_LinkGrabberSidebar_globfilter();

    @Default(lngs = { "en" }, values = { "Enable/Disable global Linkgrabber Filter (see Settings)" })
    String LinkGrabberSidebar_LinkGrabberSidebar_globfilter_tt();

    @Default(lngs = { "en" }, values = { "Hate Captchas? Click here!" })
    String CaptchaDialog_getDefaultButtonPanel_premium();

    @Default(lngs = { "en" }, values = { "File '%s1'(%s2) from %s3" })
    String CaptchaDialog_layoutDialogContent_header(String filename, String formatBytes, String hoster);

    @Default(lngs = { "en" }, values = { "File '%s1' from %s3" })
    String CaptchaDialog_layoutDialogContent_header2(String filename, String hoster);

    @Default(lngs = { "en" }, values = { "Avoid %s1 Captchas" })
    String PremiumInfoDialog_PremiumInfoDialog_(String tld);

    @Default(lngs = { "en" }, values = { "JDownloader is able to auto recognize most of the Captchas out there.\r\nHowever, %s1 unfortunately uses a Captcha Type which cannot be recognized.\r\nTo download without interruptions, we recommend to use %s1's Premium Mode." })
    String PremiumInfoDialog_layoutDialogContent_explain(String name);

    @Default(lngs = { "en" }, values = { "Unlimited Downloadspeed - use your full Internet Bandwidth!" })
    String PremiumFeature_speed_();

    @Default(lngs = { "en" }, values = { "Unlimited Traffic - Download as much as you want." })
    String PremiumFeature_bandwidth_();

    @Default(lngs = { "en" }, values = { "Download files in parallel" })
    String PremiumFeature_parallel_();

    @Default(lngs = { "en" }, values = { "Resume stopped or broken Downloads" })
    String PremiumFeature_resume_();

    @Default(lngs = { "en" }, values = { "Boost Downloadspeed with Chunkload - Use several connections per file" })
    String PremiumFeature_chunkload_();

    @Default(lngs = { "en" }, values = { "Besides avoiding annoying Captchas, there are further benefits of using Premium:" })
    String PremiumInfoDialog_layoutDialogContent_advantages_header();

    @Default(lngs = { "en" }, values = { "No Waittime before or between Downloads" })
    String PremiumFeature_noWaittime_();

    @Default(lngs = { "en" }, values = { "Give it a try" })
    String PremiumInfoDialog_layoutDialogContent_interested();

    @Default(lngs = { "en" }, values = { "No Thanks" })
    String literall_no_thanks();

    @Default(lngs = { "en" }, values = { "1. Get a Premium Account" })
    String BuyAndAddPremiumAccount_layoutDialogContent_get();

    @Default(lngs = { "en" }, values = { "2. Enter your Logins" })
    String BuyAndAddPremiumAccount_layoutDialogContent_enter();

    @Default(lngs = { "en" }, values = { "Click here to get an Premium Account" })
    String OpenURLAction_OpenURLAction_();

    @Default(lngs = { "en" }, values = { "Enter password..." })
    String BuyAndAddPremiumAccount_layoutDialogContent_pass();

    @Default(lngs = { "en" }, values = { "Max. sim. Downloads per Hoster" })
    String ParalellDownloadsEditor_ParallelDownloadsPerHostEditor_();

    @Default(lngs = { "en" }, values = { "Enable/Disable this option" })
    String AbstractConfigPanel_addPair_enabled();

    // @Default(lngs = { "en" }, values = { "Enter Filename Filter Expression..." })
    // String SearchField_SearchField_helptext();

    @Default(lngs = { "en" }, values = { "Start Downloads" })
    String ConfirmAction_ConfirmAction_context_add_and_start();

    @Default(lngs = { "en" }, values = { "Add to Download List" })
    String ConfirmAction_ConfirmAction_context_add();

    @Default(lngs = { "en" }, values = { "Save to" })
    String LinkGrabberTableModel_initColumns_folder();

    @Default(lngs = { "en" }, values = { "Download from" })
    String LinkGrabberTableModel_initColumns_url();

    @Default(lngs = { "en" }, values = { "Click to open Url in browser" })
    String UrlColumn_UrlColumn_open_tt_();

    @Default(lngs = { "en" }, values = { "Move to new Package" })
    String MergeToPackageAction_MergeToPackageAction_();

    @Default(lngs = { "en" }, values = { "Set Download Directory" })
    String SetDownloadFolderAction_SetDownloadFolderAction_();

    @Default(lngs = { "en" }, values = { "Watch As You Download" })
    String WatchAsYouDownload_WatchAsYouDownloadAction_();

    @Default(lngs = { "en" }, values = { "Enable" })
    String EnabledAction_EnabledAction_enable();

    @Default(lngs = { "en" }, values = { "Disable" })
    String EnabledAction_EnabledAction_disable();

    @Default(lngs = { "en" }, values = { "Set Download Password" })
    String SetDownloadPassword_SetDownloadPassword_();

    @Default(lngs = { "en" }, values = { "Download Password" })
    String DownloadPasswordColumn_DownloadPasswordColumn_object_();

    @Default(lngs = { "en" }, values = { "Other" })
    String ContextMenuFactory_createPopup_other();

    @Default(lngs = { "en" }, values = { "Incomplete Split-Archives" })
    String RemoveIncompleteArchives_RemoveIncompleteArchives_object_();

    @Default(lngs = { "en" }, values = { "Split Packages By Hoster" })
    String SplitPackagesByHost_SplitPackagesByHost_object_();

    @Default(lngs = { "en" }, values = { "Sort Package(s) on '%s1'" })
    String SortAction_SortAction_object_(String columnName);

    @Default(lngs = { "en" }, values = { "Crawl %s1-Link" })
    String CaptchaDialog_layoutDialogContent_header_crawler(String tld);

    @Default(lngs = { "en" }, values = { "Crawl %s1 @ %s2" })
    String CaptchaDialog_layoutDialogContent_header_crawler2(String crawlerStatus, String tld);

    @Default(lngs = { "en" }, values = { "Filter List - filter all links that match these rules." })
    String LinkgrabberFilter_initComponents_filter_();

    @Default(lngs = { "en" }, values = { "Views - Grab all links that match these rules anyway." })
    String LinkgrabberFilter_initComponents_exceptions_();

    @Default(lngs = { "en" }, values = { "contains not" })
    String FilterRuleDialog_layoutDialogContent_contains_not();

    @Default(lngs = { "en" }, values = { "equals not" })
    String FilterRuleDialog_layoutDialogContent_equals_not();

    @Default(lngs = { "en" }, values = { "is between" })
    String FilterRuleDialog_layoutDialogContent_is_between();

    @Default(lngs = { "en" }, values = { "is not between" })
    String FilterRuleDialog_layoutDialogContent_is_not_between();

    @Default(lngs = { "en" }, values = { "is" })
    String FilterRuleDialog_layoutDialogContent_is_type();

    @Default(lngs = { "en" }, values = { "is not" })
    String FilterRuleDialog_layoutDialogContent_is_not_type();

    @Default(lngs = { "en" }, values = { "Filter Rule" })
    String FilterRuleDialog_FilterRuleDialog_title_();

    @Default(lngs = { "en" }, values = { "Filter all Links matching these conditions" })
    String FilterRuleDialog_getIfText_();

    @Default(lngs = { "en" }, values = { "Filter Exception Rule" })
    String ExceptionsRuleDialog_ExceptionsRuleDialog_title_();

    @Default(lngs = { "en" }, values = { "Allow all Links matching these conditions, and ignore the Filter List" })
    String ExceptionsRuleDialog_getIfText_();

    @Default(lngs = { "en" }, values = { "Allow Links if, " })
    String ExceptionsTableModel_initColumns_condition_();

    @Default(lngs = { "en" }, values = { "Test filter Rules" })
    String LinkgrabberFilter_LinkgrabberFilter_test_();

    @Default(lngs = { "en" }, values = { "Enter Downloadlink to test Filters..." })
    String LinkgrabberFilter_LinkgrabberFilter_test_help_();

    @Default(lngs = { "en" }, values = { "Test Filters - running" })
    String TestWaitDialog_TestWaitDialog_title_();

    @Default(lngs = { "en" }, values = { "Result:" })
    String TestWaitDialog_layoutDialogContent_filtered();

    @Default(lngs = { "en" }, values = { "%s1/%s2 Links filtered  (%s3 %)" })
    String TestWaitDialog_runInEDT_(int filtered, int size, double d);

    @Default(lngs = { "en" }, values = { "Testlink:" })
    String TestWaitDialog_layoutDialogContent_testlink_();

    @Default(lngs = { "en" }, values = { "Url" })
    String ResultTableModel_initColumns_link_();

    @Default(lngs = { "en" }, values = { "Filtered Link" })
    String ResultTableModel_getTooltipText_dropped_();

    @Default(lngs = { "en" }, values = { "Accepted Link" })
    String ResultTableModel_getTooltipText_accept_();

    @Default(lngs = { "en" }, values = { "Matching Rule Name" })
    String ResultTableModel_initColumns_rule_();

    @Default(lngs = { "en" }, values = { "Test Filters - finished" })
    String TestWaitDialog_TestWaitDialog_title_finished();

    @Default(lngs = { "en" }, values = { "Matching Rule Condition" })
    String ResultTableModel_initColumns_ruledesc_();

    @Default(lngs = { "en" }, values = { "Original Filename" })
    String ResultTableModel_initColumns_filename_();

    @Default(lngs = { "en" }, values = { "Filesize" })
    String ResultTableModel_initColumns_size_();

    @Default(lngs = { "en" }, values = { "Filetype" })
    String ResultTableModel_initColumns_filetype_();

    @Default(lngs = { "en" }, values = { "Hoster" })
    String ResultTableModel_initColumns_hoster();

    @Default(lngs = { "en" }, values = { "Source" })
    String ResultTableModel_initColumns_source();

    @Default(lngs = { "en" }, values = { "Filtered" })
    String ResultTableModel_getStringValue_filtered_();

    @Default(lngs = { "en" }, values = { "Accepted" })
    String ResultTableModel_getStringValue_accepted_();

    @Default(lngs = { "en" }, values = { "No Available Downloads found! Retry with a different Testlink." })
    String TestWaitDialog_runInEDTnothing_found();

    @Default(lngs = { "en" }, values = { "is" })
    String ConditionDialog_layoutDialogContent_online_is_();

    @Default(lngs = { "en" }, values = { "isn't" })
    String ConditionDialog_layoutDialogContent_online_isnot();

    @Default(lngs = { "en" }, values = { "currently uncheckable - Download may be possible." })
    String ConditionDialog_layoutDialogContent_uncheckable_();

    @Default(lngs = { "en" }, values = { "online - Download is possible." })
    String ConditionDialog_layoutDialogContent_online_();

    @Default(lngs = { "en" }, values = { "offline - Download not possible." })
    String ConditionDialog_layoutDialogContent_offline_();

    @Default(lngs = { "en" }, values = { "File" })
    String FilterRuleDialog_layoutDialogContent_lbl_online();

    @Default(lngs = { "en" }, values = { "File is offline" })
    String FilterRule_toString_offline();

    @Default(lngs = { "en" }, values = { "File is online" })
    String FilterRule_toString_online();

    @Default(lngs = { "en" }, values = { "Filestatus cannot be checked" })
    String FilterRule_toString_uncheckable();

    @Default(lngs = { "en" }, values = { "File isn't offline" })
    String FilterRule_toString_offline_not();

    @Default(lngs = { "en" }, values = { "File isn't online" })
    String FilterRule_toString_online_not();

    @Default(lngs = { "en" }, values = { "Filestatus is known" })
    String FilterRule_toString_uncheckable_not();

    @Default(lngs = { "en" }, values = { "Online Status" })
    String ResultTableModel_initColumns_online_();

    @Default(lngs = { "en" }, values = { "Filtered/Accepted" })
    String ResultTableModel_initColumns_filtered_();

    @Default(lngs = { "en" }, values = { "This condition requires that the file is online." })
    String ConditionDialog_updateOnline_linkcheck_required();

    @Default(lngs = { "en" }, values = { "Custom Views" })
    String LinkGrabberSidebar_LinkGrabberSidebar_exceptionfilter();

    @Default(lngs = { "en" }, values = { "Choose Icon for this Filter" })
    String ConditionDialog_layoutDialogContent_object_();

    @Default(lngs = { "en" }, values = { "Show/Hide Sidebar" })
    String LinkGrabberPanel_LinkGrabberPanel_btn_showsidebar_tt_up();

    @Default(lngs = { "en" }, values = { "Sidebar Hidden!" })
    String LinkGrabberPanel_onConfigValueModified_title_();

    @Default(lngs = { "en" }, values = { "The Sidebar is hidden now. Click again to show it.\r\n\r\nWARNING: Views and Settings are still active! Check advanced Settings to disable the sidebar and all it's features completely." })
    String LinkGrabberPanel_onConfigValueModified_msg_();

    @Default(lngs = { "en" }, values = { "Filename" })
    String PackagizerFilterRuleDialog_layoutDialogContent_filename();

    @Default(lngs = { "en" }, values = { "Rename" })
    String PackagizerFilterRuleDialog_layoutDialogContent_rename();

    @Default(lngs = { "en" }, values = { "Enter Package Name Pattern..." })
    String PackagizerFilterRuleDialog_layoutDialogContent_packagename_help_();

    @Default(lngs = { "en" }, values = { "Enter Filename Pattern..." })
    String PackagizerFilterRuleDialog_layoutDialogContent_filename_help_();

    @Default(lngs = { "en" }, values = { "Enter absolute or relative Path..." })
    String PackagizerFilterRuleDialog_layoutDialogContent_dest_help();

    @Default(lngs = { "en" }, values = { "Original Filename" })
    String PackagizerFilterRuleDialog_createVariablesMenu_filename_org();

    @Default(lngs = { "en" }, values = { "Original Filetype" })
    String PackagizerFilterRuleDialog_createVariablesMenu_filetype_org();

    @Default(lngs = { "en" }, values = { "Priority" })
    String settings_linkgrabber_filter_columns_exepriority();

    @Default(lngs = { "en" }, values = { "Rules with higher Priority always overwrite lower ones." })
    String FilterTableModel_getTooltipText_prio_();

    @Default(lngs = { "en" }, values = { "Condition" })
    String settings_linkgrabber_filter_columns_cond();

    @Default(lngs = { "en" }, values = { "%s1 (Invalid Condition)" })
    String FilterTableModel_getStringValue_name_invalid(String name);

    @Default(lngs = { "en" }, values = { "Invalid Condition - Please check rule!" })
    String FilterTableModel_initColumns_invalid_condition_();

    @Default(lngs = { "en" }, values = { "Enabled" })
    String PackagizerFilterRuleDialog_updateGUI_enabled_();

    @Default(lngs = { "en" }, values = { "Disabled" })
    String PackagizerFilterRuleDialog_updateGUI_disabled_();

    @Default(lngs = { "en" }, values = { "Crawling for Downloads" })
    String AddLinksProgress_AddLinksProgress_();

    @Default(lngs = { "en" }, values = { "Searching for Downloads in %s1..." })
    String AddLinksProgress_layoutDialogContent_header_(String string);

    @Default(lngs = { "en" }, values = { "Duration:" })
    String AddLinksProgress_layoutDialogContent_duration();

    @Default(lngs = { "en" }, values = { "Downloads found:" })
    String AddLinksProgress_found();

    @Default(lngs = { "en" }, values = { "Filtered Links - Check LinkFilter Settings" })
    String AddLinksProgress_filter();

    @Default(lngs = { "en" }, values = { "Hide" })
    String literally_hide();

    @Default(lngs = { "en" }, values = { "Abort" })
    String literally_abort();

    @Default(lngs = { "en" }, values = { "Crawler Window hidden!" })
    String AddLinksProgress_setReturnmask_title_();

    @Default(lngs = { "en" }, values = { "You hid the Crawler Window. \r\nNo reason to worry! You can see the Linkcrawler Status here." })
    String AddLinksProgress_setReturnmask_msg_();

    @Default(lngs = { "en" }, values = { "Permanently Offline" })
    String Permanently_Offline_Package();

    @Default(lngs = { "en" }, values = { "Please wait: Parsing Clipboard content" })
    String AddLinksDialog_ParsingClipboard();

    @Default(lngs = { "en" }, values = { "Enable/Disable Regular Expressions for this Condition" })
    String ConditionDialog_layoutDialogContent_regex_tooltip_();

    @Default(lngs = { "en" }, values = { "Your conditions are not valid. Please check expressions in the highlighted fields." })
    String ConditionDialog_validate_object_();

    @Default(lngs = { "en" }, values = { "Test %s1 Expression" })
    String TestAction_TestAction_object_(String name);

    @Default(lngs = { "en" }, values = { "Please enter %s1" })
    String TestAction_actionPerformed_test_title_(String str);

    @Default(lngs = { "en" }, values = { "To test your expression %s1, please enter the matching %s2" })
    String TestAction_actionPerformed_msg_(String expression, String name);

    @Default(lngs = { "en" }, values = { "Source Url" })
    String ConditionDialog_getPopupMenu_sourceurl_();

    @Default(lngs = { "en" }, values = { "Your Input %s1 does not equal %s2!" })
    String TestAction_actionPerformed_nomatch_(String input, String pattern);

    @Default(lngs = { "en" }, values = { "Your Input %s1 does not contain %s2!" })
    String TestAction_actionPerformed_nomatch_contain(String input, String pattern);

    @Default(lngs = { "en" }, values = { "%s1: %s2" })
    String TestAction_actionPerformed_match_(int i, String m);

    @Default(lngs = { "en" }, values = { "Your input %s1 contains %s2. \r\nWildcard Matches:\r\n%s3" })
    String TestAction_actionPerformed_object_(String input, String pattern, String string);

    @Default(lngs = { "en" }, values = { "Your input %s1 equals %s2. \r\nWildcard Matches:\r\n%s3" })
    String TestAction_actionPerformed_object_matches(String input, String pattern, String string);

    @Default(lngs = { "en" }, values = { "Hoster Url" })
    String ConditionDialog_getPopupMenu_hosterurl_();

    @Default(lngs = { "en" }, values = { "Filename" })
    String ConditionDialog_getPopupMenu_filename_();

    @Default(lngs = { "en" }, values = { "Your input %s1 contains %s2!" })
    String TestAction_actionPerformed_contains_(String input, String pattern);

    @Default(lngs = { "en" }, values = { "Your input %s1 equals %s2!" })
    String TestAction_actionPerformed_equals_(String input, String pattern);

    @Default(lngs = { "en" }, values = { "Filename %s1 matches your Filetype Filter: %s2." })
    String TestAction_actionPerformed_match_ext_(String input, String ext);

    @Default(lngs = { "en" }, values = { "Filename %s1 does not match your Filetype Filter." })
    String TestAction_actionPerformed_nomatch_ext_(String input);

    @Default(lngs = { "en" }, values = { "has" })
    String ConditionDialog_layoutDialogContent_online_has_();

    @Default(lngs = { "en" }, values = { "hasn't" })
    String ConditionDialog_layoutDialogContent_online_hasnot_();

    @Default(lngs = { "en" }, values = { "a valid Premium Account" })
    String ConditionDialog_layoutDialogContent_premium();

    @Default(lngs = { "en" }, values = { "Plugin" })
    String FilterRuleDialog_layoutDialogContent_lbl_plugin();

    @Default(lngs = { "en" }, values = { "a Captcha Solver" })
    String ConditionDialog_layoutDialogContent_captcha();

    @Default(lngs = { "en" }, values = { "Valid Premiumaccount is available" })
    String FilterRule_toString_premium();

    @Default(lngs = { "en" }, values = { "Captchas are solved automatically" })
    String FilterRule_toString_autocaptcha();

    @Default(lngs = { "en" }, values = { "No valid Premiumaccount available" })
    String FilterRule_toString_premium_not();

    @Default(lngs = { "en" }, values = { "Captchas must be entered manually" })
    String FilterRule_toString_autocaptcha_not();

    @Default(lngs = { "en" }, values = { "%s1/%s2 online" })
    String AvailabilityColumn_getStringValue_object_(int i, int size);

    @Default(lngs = { "en" }, values = { "Set Priority" })
    String PriorityAction_PriorityAction_();

    @Default(lngs = { "en" }, values = { "Downloads will start soon. Click here to cancel countdown." })
    String AutoConfirmButton_AutoConfirmButton_tooltip_();

    @Default(lngs = { "en" }, values = { "Reconnect Setup is invalid. Check Settings!" })
    String ReconnectDialog_run_failed_not_setup_();

    @Default(lngs = { "en" }, values = { "Skip file" })
    String IfFileExistsDialog_layoutDialogContent_skip_();

    @Default(lngs = { "en" }, values = { "Overwrite existing file" })
    String IfFileExistsDialog_layoutDialogContent_overwrite_();

    @Default(lngs = { "en" }, values = { "Rename file" })
    String IfFileExistsDialog_layoutDialogContent_rename_();

    @Default(lngs = { "en" }, values = { "Remember selection for this Package" })
    String IfFileExistsDialog_getDontShowAgainLabelText_();

    @Default(lngs = { "en" }, values = { "Enter Test Url..." })
    String PackagizerFilterRuleDialog_PackagizerFilterRuleDialog_test_help();

    @Default(lngs = { "en" }, values = { "Test Filter: \"%s1\"" })
    String FilterRuleDialog_runTest_title_(String string);

    @Default(lngs = { "en" }, values = { "Matches" })
    String ViewTestResultTableModel_initColumns_matches_();

    @Default(lngs = { "en" }, values = { "Packigizer Rule Test: \"%s1\"" })
    String PackagizerRuleDialog_runTest_title_(String string);

    @Default(lngs = { "en" }, values = { "Matches" })
    String PackagizerSingleTestTableModel_initColumns_matches_();

    @Default(lngs = { "en" }, values = { "Download Folder" })
    String PackagizerSingleTestTableModel_initColumns_downloadfolder_();

    @Default(lngs = { "en" }, values = { "Package Name" })
    String PackagizerSingleTestTableModel_initColumns_packagename_();

    @Default(lngs = { "en" }, values = { "File Name" })
    String PackagizerSingleTestTableModel_initColumns_filename_();

    @Default(lngs = { "en" }, values = { "File Name" })
    String searchcategory_filename();

    @Default(lngs = { "en" }, values = { "Hoster" })
    String searchcategory_hoster();

    @Default(lngs = { "en" }, values = { "Comment" })
    String searchcategory_comment();

    @Default(lngs = { "en" }, values = { "Package Name" })
    String searchcategory_package();

    @Default(lngs = { "en" }, values = { "Running Downloads" })
    String downloadview_running();

    @Default(lngs = { "en" }, values = { "All Downloads" })
    String downloadview_all();

    @Default(lngs = { "en" }, values = { "Skipped Downloads" })
    String downloadview_skipped();

    @Default(lngs = { "en" }, values = { "Failed Downloads" })
    String downloadview_failed();

    @Default(lngs = { "en" }, values = { "Successful Downloads" })
    String downloadview_successful();

    @Default(lngs = { "en" }, values = { "Please enter the file name you are looking for..." })
    String searchcategory_filename_help();

    @Default(lngs = { "en" }, values = { "Please enter the domain you are looking for..." })
    String searchcategory_hoster_help();

    @Default(lngs = { "en" }, values = { "Please enter the comment you are looking for..." })
    String searchcategory_comment_help();

    @Default(lngs = { "en" }, values = { "Please enter the package name you are looking for..." })
    String searchcategory_package_help();

    @Default(lngs = { "en" }, values = { "Choose a View to filter the download list..." })
    String PseudoCombo_PseudoCombo_tt_();

    @Default(lngs = { "en" }, values = { "Renamed Multiarchive!" })
    String FileColumn_setStringValue_title_();

    @Default(lngs = { "en" }, values = { "You renamed one file of an archive that has several parts.\r\nYou have to rename all parts in order to keep the archive extractable!" })
    String FileColumn_setStringValue_msg_();

    @Default(lngs = { "en" }, values = { "Chunks Progress:" })
    String ProgressColumn_createToolTip_object_();

    @Default(lngs = { "en" }, values = { "Always" })
    String gui_config_general_AutoDownloadStartOption_always();

    @Default(lngs = { "en" }, values = { "Only if downloads were running at last Session's end" })
    String gui_config_general_AutoDownloadStartOption_only_if_closed_running();

    @Default(lngs = { "en" }, values = { "Never" })
    String gui_config_general_AutoDownloadStartOption_never();

    @Default(lngs = { "en" }, values = { "Autostart Downloads at Application Start" })
    String system_download_autostart();

    @Default(lngs = { "en" }, values = { "Show Countdown (seconds)" })
    String system_download_autostart_countdown();

    @Default(lngs = { "en" }, values = { "Autostart Downloads" })
    String gui_config_download_autostart();

    @Default(lngs = { "en" }, values = { "Choose if, and when JDownloader should start pending downloads without user interaction..." })
    String gui_config_download_autostart_desc();

    @Default(lngs = { "en" }, values = { "Checking for Updates" })
    String JDUpdater_JDUpdater_object_icon();

    @Default(lngs = { "en" }, values = { "Password required" })
    String JDGui_setVisible_password_();

    @Default(lngs = { "en" }, values = { "Enter the Password to open JDownloader:" })
    String JDGui_setVisible_password_msg();

    @Default(lngs = { "en" }, values = { "The entered Password was wrong!" })
    String JDGui_setVisible_password_wrong();

    @Default(lngs = { "en" }, values = { "New Package" })
    String MergeToPackageAction_actionPerformed_newpackage_();

    @Default(lngs = { "en" }, values = { "Archive Incomplete: %s1" })
    String ConfirmAction_run_incomplete_archive_title_(String name);

    @Default(lngs = { "en" }, values = { "You added an Archive that has several parts. \r\nYou need at least one Link for each part. Some Links are missing." })
    String ConfirmAction_run_incomplete_archive_msg();

    @Default(lngs = { "en" }, values = { "Continue anyway" })
    String ConfirmAction_run_incomplete_archive_continue();

    @Default(lngs = { "en" }, values = { "Show details" })
    String ConfirmAction_run_incomplete_archive_details();

    @Default(lngs = { "en" }, values = { "Stop Mark for Download Links" })
    String StopsignAction_actionPerformed_help_title_();

    @Default(lngs = { "en" }, values = { "If you set a Stop Mark on a Download Link, JDownloader will stop downloading after this file has been loaded.\r\nIf you want to download until this Package is finished, you should set the Stop Mark on the Package instead." })
    String StopsignAction_actionPerformed_help_msg_();

    @Default(lngs = { "en" }, values = { "Stop Mark for Packages" })
    String StopsignAction_actionPerformed_help_title_package_();

    @Default(lngs = { "en" }, values = { "If you set a Stop Mark on a Package, JDownloader will stop downloading after all files in this Package have finished." })
    String StopsignAction_actionPerformed_help_msg_package_();

    @Default(lngs = { "en" }, values = { "Save DLC File to..." })
    String CreateDLCAction_actionPerformed_title_();

    @Default(lngs = { "en" }, values = { "Created DLC File: %s1" })
    String DLCFactory_createDLC_created_(String absolutePath);

    @Default(lngs = { "en" }, values = { "Properties" })
    String ContextMenuFactory_createPopup_properties_package();

    @Default(lngs = { "en" }, values = { "Download Speed Limit" })
    String ContextMenuFactory_createPopup_speed();

    @Default(lngs = { "en" }, values = { "Custom Speed Limit" })
    String CustomSpeedLimitator_SpeedlimitEditor__lbl();

    @Default(lngs = { "en" }, values = { "Show Download URLs" })
    String ContextMenuFactory_createPopup_url();

    @Default(lngs = { "en" }, values = { "Source URL for this Download Link" })
    String LinkURLEditor();

    @Default(lngs = { "en" }, values = { "Copy URLs" })
    String LinkURLEditor_onContextMenu_copy_();

    @Default(lngs = { "en" }, values = { "Download Directory for %s1" })
    String OpenDownloadFolderAction_actionPerformed_object_(String string);

    @Default(lngs = { "en" }, values = { "Explore" })
    String OpenDownloadFolderAction_actionPerformed_button_();

    @Default(lngs = { "en" }, values = { "Save" })
    String OpenDownloadFolderAction_actionPerformed_save_();

    @Default(lngs = { "en" }, values = { "Current Directory: %s1" })
    String OpenDownloadFolderAction_layoutDialogContent_current_(String absolutePath);

    @Default(lngs = { "en" }, values = { "Choose new Directory:" })
    String OpenDownloadFolderAction_layoutDialogContent_object_();

    @Default(lngs = { "en" }, values = { "Set Comment(s)" })
    String SetCommentAction_SetCommentAction_object_();

    @Default(lngs = { "en" }, values = { "Edit Link/Package Comments" })
    String SetCommentAction_actionPerformed_dialog_title_();

    @Default(lngs = { "en" }, values = { "Custom Speedlimit" })
    String SpeedLimitator_SpeedLimitator_object_();

    @Default(lngs = { "en" }, values = { "Windows" })
    String AddonsMenu_updateMenu_windows_();

    @Default(lngs = { "en" }, values = { "Import Proxies" })
    String ProxyConfig_actionPerformed_import_title_();

    @Default(lngs = { "en" }, values = { "Enter one or more Proxyserver(s). \r\nFormat: (socks4|socks5|http)://[<user>:<pass>]@<host>:<port> - line seperated" })
    String ProxyConfig_actionPerformed_import_proxies_explain_();

    @Default(lngs = { "en" }, values = { "Export Proxies" })
    String ProxyTable_copy_export_title_();

    @Default(lngs = { "en" }, values = { "Socks 4 Proxy" })
    String gui_column_proxytype_socks4();

    @Default(lngs = { "en" }, values = { "Add a Premium Account" })
    String BuyAndAddPremiumAccount_BuyAndAddPremiumAccount_title_();

    @Default(lngs = { "en" }, values = { "JDownloader downloads as fast as possible.\r\nHowever, %s1 seems to limit the speed in free mode.\r\nTo download without interruptions and full speed, we recommend to use %s1's Premium Mode." })
    String SpeedColumn_getDescription_object_(String tld);

    @Default(lngs = { "en" }, values = { "Speed Alarm for %s1" })
    String SpeedColumn_onSingleClick_object_(String host);

    @Default(lngs = { "en" }, values = { "Finished" })
    String TaskColumn_getStringValue_finished_();

    @Default(lngs = { "en" }, values = { "Running" })
    String TaskColumn_getStringValue_running_();

    @Default(lngs = { "en" }, values = { "Current IP is blocked by %s1" })
    String TaskColumn_onSingleClick_object_(String host);

    @Default(lngs = { "en" }, values = { "%s1 blocked your current IP (Internet Address).\r\nIn many cases, JDownloader us able to do a Reconnect and get a new IP.\r\nIf this is not possible, we recommend to use %s1's Premium Mode." })
    String TaskColumn_getDescription_object_(String tld);

    @Default(lngs = { "en" }, values = { "Premium Alerts" })
    String literall_premium_alert();

    @Default(lngs = { "en" }, values = { "Matches on any File or Link and ignores conditions below" })
    String FilterRuleDialog_layoutDialogContent_lbl_always();

    @Default(lngs = { "en" }, values = { "Matches for any File or Link" })
    String BooleanFilter_toString_();

    @Default(lngs = { "en" }, values = { "Enter a Password if the Download is Password protected" })
    String AddLinksDialog_layoutDialogContent_downloadpassword_tt();

    @Default(lngs = { "en" }, values = { "Enter a Password for protected Links" })
    String AddLinksDialog_createExtracOptionsPanel_downloadpassword();

    @Default(lngs = { "en" }, values = { "Auto Extract" })
    String AddLinksDialog_layoutDialogContent_autoextract_lbl();

    @Default(lngs = { "en" }, values = { "Subfolder by Package" })
    String SetDownloadFolderInDownloadTableAction_modifiyNamePanel_package_();

    @Default(lngs = { "en" }, values = { "You really should know that..." })
    String literall_usage_tipp();

    @Default(lngs = { "en" }, values = { "Delete" })
    String DeleteQuickAction_DeleteQuickAction_object_();

    @Default(lngs = { "en" }, values = { "Do you want to remove all Links of the Archive %s1 from Linkgrabber?" })
    String RemoveIncompleteArchives_run_(String name);

    @Default(lngs = { "en" }, values = { "Folder does not exist!" })
    String DownloadFolderChooserDialog_handleNonExistingFolders_title_();

    @Default(lngs = { "en" }, values = { "The folder %s1\r\ndoes not exist. Do you want to create it?" })
    String DownloadFolderChooserDialog_handleNonExistingFolders_msg_(String path);

    @Default(lngs = { "en" }, values = { "Language" })
    String gui_config_language();

    @Default(lngs = { "en" }, values = { "Restart Required!" })
    String GUISettings_save_language_changed_restart_required_title();

    @Default(lngs = { "en" }, values = { "You have to restart JDownloader for a language change. Restart now?" })
    String GUISettings_save_language_changed_restart_required_msg();

    @Default(lngs = { "en" }, values = { "Nothing found!" })
    String AddLinksAction_actionPerformed_deep_title();

    @Default(lngs = { "en" }, values = { "JDownloader couldn't find any links to download on this website.\r\nWould you like to perform a deep link analysis to show all the files that can be downloaded from it?" })
    String AddLinksAction_actionPerformed_deep_msg();

    @Default(lngs = { "en" }, values = { "Download can be resumed" })
    String ConnectionColumn_DownloadIsResumeable();

    @Default(lngs = { "en" }, values = { "Account in use: %s1" })
    String ConnectionColumn_DownloadUsesAccount(String s);

    @Default(lngs = { "en" }, values = { "New Package" })
    String NewPackageDialog_NewPackageDialog_();

    @Default(lngs = { "en" }, values = { "Package Name" })
    String NewPackageDialog_layoutDialogContent_newname_();

    @Default(lngs = { "en" }, values = { "Save to" })
    String NewPackageDialog_layoutDialogContent_saveto();

    @Default(lngs = { "en" }, values = { "Build Date:" })
    String jd_gui_swing_components_AboutDialog_builddate();

    @Default(lngs = { "en" }, values = { "Source Revisions" })
    String jd_gui_swing_components_AboutDialog_sourcerevisions();

    @Default(lngs = { "en" }, values = { "Core:" })
    String jd_gui_swing_components_AboutDialog_core();

    @Default(lngs = { "en" }, values = { "AppWork Utilities:" })
    String jd_gui_swing_components_AboutDialog_appworkutilities();

    @Default(lngs = { "en" }, values = { "Browser:" })
    String jd_gui_swing_components_AboutDialog_browser();

    @Default(lngs = { "en" }, values = { "Updater:" })
    String jd_gui_swing_components_AboutDialog_updater();

    @Default(lngs = { "en" }, values = { "License Registration Number %s1" })
    String jd_gui_swing_components_AboutDialog_synthetica2(String string);

    @Default(lngs = { "en" }, values = { "Filter" })
    String LinkgrabberFilter_initComponents_filter__title();

    @Default(lngs = { "en" }, values = { "Views" })
    String LinkgrabberFilter_initComponents_exceptions_title();

    @Default(lngs = { "en" }, values = { "Create a Log" })
    String LogAction();

    @Default(lngs = { "en" }, values = { "Create a Log" })
    String LogAction_tooltip();

    @Default(lngs = { "en" }, values = { "Please send this log ID to your supporter!" })
    String LogAction_actionPerformed_givelogid_();

    @Default(lngs = { "en" }, values = { "Checksum" })
    String checksumcolumnmd5();

    @Default(lngs = { "en" }, values = { "If there ever has been any warrenty for anything, it ends here!" })
    String AdvancedSettings_onShow_title_();

    @Default(lngs = { "en" }, values = { "These advanced settings should not be modified unless you know what you are doing. \r\nChanging them may affect stability, security and performance of JDownloader.\r\nYou have been warned." })
    String AdvancedSettings_onShow_msg_();

    @Default(lngs = { "en" }, values = { "Filter Settings" })
    String AdvancedSettings_AdvancedSettings_filter_();

    @Default(lngs = { "en" }, values = { "Key" })
    String AdvancedTableModel_initColumns_key_();

    @Default(lngs = { "en" }, values = { "Type" })
    String AdvancedTableModel_initColumns_type_();

    @Default(lngs = { "en" }, values = { "Value" })
    String AdvancedValueColumn_AdvancedValueColumn_object_();

    @Default(lngs = { "en" }, values = { "Valid Range: %s1 - %s2" })
    String RangeValidator_toString_object_(long min, long max);

    @Default(lngs = { "en" }, values = { "We need your help" })
    String JDGui_showStatsDialog_title_();

    @Default(lngs = { "en" }, values = { "To get a even better user experience in JDownloader 2,\r\nwe need some information about how you use JDownloader, which features you like, and which not.\r\nTo get this information, JDownloader will collect anonymous usage statistics. We understand that there might be users that do not want this. \r\nYou can disable the Stats Manager in the Advanced Settings at any time.\r\n\r\nIf you have any questions about this, feel free to contact our Developer Team in our Support Chat. " })
    String JDGui_showStatsDialog_message_();

    @Default(lngs = { "en" }, values = { "I want to help" })
    String JDGui_showStatsDialog_yes_();

    @Default(lngs = { "en" }, values = { "No, thanks" })
    String JDGui_showStatsDialog_no_();

    @Default(lngs = { "en" }, values = { "Please wait. Loading all plugins..." })
    String PluginSettingsPanel_PluginSettingsPanel_waittext_();

    @Default(lngs = { "en" }, values = { "Host Plugin: %s1" })
    String PluginSettingsPanel_runInEDT_plugin_header_text_host(String displayName);

    @Default(lngs = { "en" }, values = { "Crawler Plugin: %s1" })
    String PluginSettingsPanel_runInEDT_plugin_header_text_decrypt(String displayName);

    @Default(lngs = { "en" }, values = { "Delete %s1 link(s) - %s2 Link(s) remaining!" })
    String ResetLinkGrabberOptionDialog_layoutDialogContent_remove_links2(int toDelete, int remaining);

    @Default(lngs = { "en" }, values = { "Reset Table Sorter" })
    String ResetLinkGrabberOptionDialog_layoutDialogContent_sort();

    @Default(lngs = { "en" }, values = { "Clear Search Filter" })
    String ResetLinkGrabberOptionDialog_layoutDialogContent_search();

    @Default(lngs = { "en" }, values = { "Cancel pending Crawler jobs" })
    String ResetLinkGrabberOptionDialog_layoutDialogContent_interrup_crawler();

    @Default(lngs = { "en" }, values = { "Cannot reconnect: You are offline!" })
    String ReconnectInvoker_validate_offline_();

    @Default(lngs = { "en" }, values = { "Reconnect Failed" })
    String ReconnectDialog_failed();

    @Default(lngs = { "en" }, values = { "Launcher:" })
    String jd_gui_swing_components_AboutDialog_launcher();

    @Default(lngs = { "en" }, values = { "Could not create Folder %s1" })
    String DownloadFolderChooserDialog_handleNonExistingFolders_couldnotcreatefolder(String folder);

    @Default(lngs = { "en" }, values = { "Reset selected entries to default values" })
    String AdvancedTable_onContextMenu_reset_selection();

    @Default(lngs = { "en" }, values = { "Are you sure?" })
    String lit_are_you_sure();

    @Default(lngs = { "en" }, values = { "Do you really want to reset %s1 advanced config entry/ies to their default values?" })
    String AdvancedTablecontextmenu_reset(int size);

    @Default(lngs = { "en" }, values = { "Description" })
    String AdvancedTableModel_initColumns_desc_();

    @Default(lngs = { "en" }, values = { "Duration" })
    String DurationColumn_DurationColumn_object_();

    @Default(lngs = { "en" }, values = { "Do you want to stop all running downloads?\r\nYou would loose %s1 due to %s2 non resumable Download/s!" })
    String StopDownloadsAction_run_msg_(String bytes, int i);

    @Default(lngs = { "en" }, values = { "Do you want to disable and stop running downloads?\r\nYou would loose %s1 due to %s2 non resumable Download/s!" })
    String EnableAction_run_msg_(String bytes, int i);

    @Default(lngs = { "en" }, values = { "Do you want to skip and stop running  downloads?\r\nYou would loose %s1 due to %s2 non resumable Download/s!" })
    String SkipAction_run_msg_(String bytes, int i);

    @Default(lngs = { "en" }, values = { "No" })
    String lit_no();

    @Default(lngs = { "en" }, values = { "Yes" })
    String lit_yes();

    @Default(lngs = { "en" }, values = { "dd.MM.yy" })
    String PremiumAccountTableModel_getDateFormatString_();

    @Default(lngs = { "en" }, values = { "Download Overview" })
    String OverViewHeader_OverViewHeader_();

    @Default(lngs = { "en" }, values = { "Package(s):" })
    String DownloadOverview_DownloadOverview_packages();

    @Default(lngs = { "en" }, values = { "Link(s):" })
    String DownloadOverview_DownloadOverview_links();

    @Default(lngs = { "en" }, values = { "Reset Dialog 'Don't show again' flags" })
    String GUISettings_GUISettings_resetdialogs_();

    @Default(lngs = { "en" }, values = { "Dialog 'Don't show again' are resetted now!" })
    String GUISettings_actionPerformed_reset_done();

    @Default(lngs = { "en" }, values = { "Dialog Windows" })
    String gui_config_dialogs();

    @Default(lngs = { "en" }, values = { "Please wait. Loading Links..." })
    String DownloadsTable_DownloadsTable_object_wait_for_loading_links();

    @Default(lngs = { "en" }, values = { "Please wait. Loading Links..." })
    String LinkGrabberTable_LinkGrabberTable_object_wait_for_loading_links();

    @Default(lngs = { "en" }, values = { "Skip" })
    String AbstractCaptchaDialog_AbstractCaptchaDialog_cancel();

    @Default(lngs = { "en" }, values = { "Send" })
    String AbstractCaptchaDialog_AbstractCaptchaDialog_continue();

    @Default(lngs = { "en" }, values = { "Stop all Downloads" })
    String AbstractCaptchaDialog_createPopup_skip_and_stop_all_downloads();

    @Default(lngs = { "en" }, values = { "Skip all captchas for host '%s1'" })
    String AbstractCaptchaDialog_createPopup_skip_and_disable_all_downloads_from(String host);

    @Default(lngs = { "en" }, values = { "Skip all captchas for Package '%s1'" })
    String AbstractCaptchaDialog_createPopup_skip_and_disable_package(String packageName);

    @Default(lngs = { "en" }, values = { "Skip all captchas" })
    String AbstractCaptchaDialog_createPopup_skip_and_hide_all_captchas_download();

    @Default(lngs = { "en" }, values = { "Skipped Downloads!" })
    String StatusBarImpl_skippedLinksMarker_title();

    @Default(lngs = { "en" }, values = { "Skipped Downloads: %s1. Click here to retry them." })
    String StatusBarImpl_skippedLinksMarker_desc(int i);

    @Default(lngs = { "en" }, values = { "Refresh" })
    String CaptchaDialog_layoutDialogContent_refresh();

    @Default(lngs = { "en" }, values = { "Links have been skipped" })
    String ChallengeDialogHandler_viaGUI_skipped_help_title();

    @Default(lngs = { "en" }, values = { "Congrats! You found the 'Skip Captcha Downloads'-Feature!\r\nOne or more Downloadlinks have been skipped to avoid further Captcha Dialogs.\r\nIf you want to Download these links anyway, there are three options:\r\n\r\n  1. Restart Downloads\r\n  2. Rightclick -> More ->Reset Downloadlink\r\n  3. Rightclick -> Start Download" })
    String ChallengeDialogHandler_viaGUI_skipped_help_msg();

    @Default(lngs = { "en" }, values = { "Rebuilding Plugincache..." })
    String DownloadsTable_DownloadsTable_init_plugins();

    @Default(lngs = { "en" }, values = { "1. Choose a Hoster" })
    String AddAccountDialog_layoutDialogContent_choosehoster_();

    @Default(lngs = { "en" }, values = { "2. Enter your Acount's Login Information" })
    String AddAccountDialog_layoutDialogContent_enterlogininfo();

    @Default(lngs = { "en" }, values = { "Save" })
    String lit_save();

    @Default(lngs = { "en" }, values = { "The %s1 plugin is outdated. Please run an update!" })
    String AddAccountDialog_actionPerformed_outdated(String host);

    @Default(lngs = { "en" }, values = { "or Premium Key" })
    String LetitBitAccountFactory_LetitBitPanel_key();

    @Default(lngs = { "en" }, values = { "Enter a premiumkey instead of username +  password..." })
    String LetitBitAccountFactory_LetitBitPanel_key_help();

    @Default(lngs = { "en" }, values = { "Cancel" })
    String lit_cancel();

    @Default(lngs = { "en" }, values = { "Cancel & Stop further Crawling" })
    String AbstractCaptchaDialog_createPopup_cancel_linkgrabbing();

    @Default(lngs = { "en" }, values = { "Cancel, but continue without asking for Captchas" })
    String AbstractCaptchaDialog_createPopup_cancel_stop_showing_crawlercaptchs();

    @Default(lngs = { "en" }, values = { "Total Bytes:" })
    String DownloadOverview_DownloadOverview_size();

    @Default(lngs = { "en" }, values = { "Bytes loaded:" })
    String DownloadOverview_DownloadOverview_loaded();

    @Default(lngs = { "en" }, values = { "Downloadspeed:" })
    String DownloadOverview_DownloadOverview_speed();

    @Default(lngs = { "en" }, values = { "ETA:" })
    String DownloadOverview_DownloadOverview_eta();

    @Default(lngs = { "en" }, values = { "Total Information - all Links in the List" })
    String OverViewHeader_actionPerformed_total_();

    @Default(lngs = { "en" }, values = { "Visible Links Information - do not include filtered Links" })
    String OverViewHeader_actionPerformed_visible_only_();

    @Default(lngs = { "en" }, values = { "Selected Links Information - only include selected Links & Packages" })
    String OverViewHeader_actionPerformed_selected_();

    @Default(lngs = { "en" }, values = { "Total Information - all Links in the List" })
    String DownloadOverview_DownloadOverview_tooltip1();

    @Default(lngs = { "en" }, values = { "Visible Links Information - do not include filtered Links" })
    String DownloadOverview_DownloadOverview_tooltip2();

    @Default(lngs = { "en" }, values = { "Selected Links Information - only include selected Links & Packages" })
    String DownloadOverview_DownloadOverview_tooltip3();

    @Default(lngs = { "en" }, values = { "Smart Information - Depends on your selection & filter settings" })
    String OverViewHeader_actionPerformed_smart_();

    @Default(lngs = { "en" }, values = { "Show Quicksettings" })
    String OverViewHeader_actionPerformed_quicksettings();

    @Default(lngs = { "en" }, values = { "Running Downloads:" })
    String DownloadOverview_DownloadOverview_running_downloads();

    @Default(lngs = { "en" }, values = { "Open Connections:" })
    String DownloadOverview_DownloadOverview_connections();

    @Default(lngs = { "en" }, values = { "Do you want to delete all selected Downloadlinks? Be wise - Once confirmed, this cannot be undone!" })
    String RemoveSelectionAction_actionPerformed_();

    @Default(lngs = { "en" }, values = { "Start Downloads" })
    String ForceDownloadAction_actionPerformed_help_title_();

    @Default(lngs = { "en" }, values = { "...you just forced JDownloader to start the selected Link(s) as soon as possible.\r\nThis ignores all limitations you set up (e.g. Maximum simultane Downloads).\r\nIf you just want to start all Downloads from top to bottom, you should use the 'Start Downloads' Button (The Playback Icon) in the Toolbar above." })
    String ForceDownloadAction_actionPerformed_help_msg_();

    @Default(lngs = { "en" }, values = { "Download is waiting for a prioritized start" })
    String ConnectionColumn_DownloadIsForced();

    @Default(lngs = { "en" }, values = { "Force Download Start" })
    String ForceDownloadAction_ForceDownloadAction();

    @Default(lngs = { "en" }, values = { "Skip Download" })
    String ForceDownloadAction_SkipDownloadAction();

    @Default(lngs = { "en" }, values = { "Unskip Download" })
    String ForceDownloadAction_UnskipDownloadAction();

    @Default(lngs = { "en" }, values = { "Move downloaded Files to Trash & remove Links from JDownloader" })
    String ConfirmDeleteLinksDialog_layoutDialogContent_Recycle_2();

    @Default(lngs = { "en" }, values = { "Delete downloaded Files permanently from harddisk & remove Links from JDownloader" })
    String ConfirmDeleteLinksDialog_layoutDialogContent_delete_2();

    @Default(lngs = { "en" }, values = { "Keep all downloaded Files - just remove Links from JDownloader" })
    String ConfirmDeleteLinksDialog_layoutDialogContent_no_filedelete2();

    @Default(lngs = { "en" }, values = { "\r\n%s1 Links(s) to delete\r\n%s4 Files(%s2) on Disk\r\nLinks left in Downloadlist: %s3" })
    String DeleteSelectionAction_actionPerformed_affected2(int linkCount, String bytes, int linksLeft, int localFileCount);

    @Default(lngs = { "en" }, values = { "Ups... something is wrong!" })
    String lit_ups_something_is_wrong();

    @Default(lngs = { "en" }, values = { "There are no Downloadlinks to delete!" })
    String DownloadController_deleteLinksRequest_nolinks();

    @Default(lngs = { "en" }, values = { "Delete" })
    String lit_delete();

    @Default(lngs = { "en" }, values = { "Right Click Menu: Download Table" })
    String gui_config_menumanager_downloadlist();

    @Default(lngs = { "en" }, values = { "Right Click Menu: Linkgrabber Table" })
    String gui_config_menumanager_linkgrabber();

    @Default(lngs = { "en" }, values = { "You can customize many menus in JDownloader - Main Toolbar, Context menus,..." })
    String gui_config_menumanager_desc();

    @Default(lngs = { "en" }, values = { "Advanced Options to add Links..." })
    String AddOptionsAction_AddOptionsAction_tt();

    @Default(lngs = { "en" }, values = { "Add new Links to download..." })
    String AddLinksAction_AddLinksAction_tt();

    @Default(lngs = { "en" }, values = { "Menu Customizer: %s1" })
    String ManagerFrame_ManagerFrame_title(String name);

    @Default(lngs = { "en" }, values = { "Sort package by Column" })
    String SortAction_SortAction_object_empty();

    @Default(lngs = { "en" }, values = { "Enable/Disable Link" })
    String EnabledAction_EnabledAction_empty();

    @Default(lngs = { "en" }, values = { "Set/Remove Stopmark" })
    String gui_table_contextmenu_stopmark();

    @Default(lngs = { "en" }, values = { "----------------------------" })
    String Renderer_getTreeCellRendererComponent_seperator();

    @Default(lngs = { "en" }, values = { "Edit Menu Structure (Use Drag&Drop)" })
    String ManagerFrame_layoutPanel_menustructure_();

    @Default(lngs = { "en" }, values = { "Edit Menu Node" })
    String ManagerFrame_layoutPanel_info();

    @Default(lngs = { "en" }, values = { "Add Action" })
    String ManagerFrame_layoutPanel_add();

    @Default(lngs = { "en" }, values = { "Submenu" })
    String InfoPanel_update_submenu();

    @Default(lngs = { "en" }, values = { "Link" })
    String InfoPanel_update_link();

    @Default(lngs = { "en" }, values = { "Action" })
    String InfoPanel_update_action();

    @Default(lngs = { "en" }, values = { "Properties" })
    String InfoPanel_InfoPanel_properties_();

    @Default(lngs = { "en" }, values = { "Choose Action" })
    String ManagerFrame_actionPerformed_addaction_title();

    @Default(lngs = { "en" }, values = { "Please Choose the action you want to add!" })
    String ManagerFrame_actionPerformed_addaction_msg();

    @Default(lngs = { "en" }, values = { "Add Submenu" })
    String ManagerFrame_layoutPanel_addSubmenu();

    @Default(lngs = { "en" }, values = { "Add Special Entry" })
    String ManagerFrame_layoutPanel_addspecials();

    @Default(lngs = { "en" }, values = { "Create a custom Submenu" })
    String NewSubMenuDialog_NewSubMenuDialog_title();

    @Default(lngs = { "en" }, values = { "Choose Icon and Name..." })
    String NewSubMenuDialog_layoutDialogContent_name_();

    @Default(lngs = { "en" }, values = { "Open Menu Manager" })
    String MenuManagerAction_MenuManagerAction();

    @Default(lngs = { "en" }, values = { "Predefined: \"%s1\"" })
    String AddSubMenuAction_getListCellRendererComponent(String name);

    @Default(lngs = { "en" }, values = { "---- Seperator Line ----" })
    String AddSpecialAction_actionPerformed_seperator();

    @Default(lngs = { "en" }, values = { "Choose Menu Item" })
    String AddSpecialAction_actionPerformed_title();

    @Default(lngs = { "en" }, values = { "Please choose what item you want to add!" })
    String AddSpecialAction_actionPerformed_msg();

    @Default(lngs = { "en" }, values = { "Add" })
    String lit_add();

    @Default(lngs = { "en" }, values = { "Reset to Default" })
    String ManagerFrame_layoutPanel_resettodefault();

    @Default(lngs = { "en" }, values = { "Reset to Default: %s1" })
    String ManagerFrame_layoutPanel_resettodefault_parametered(String dflt);

    @Default(lngs = { "en" }, values = { "Change Icon" })
    String InfoPanel_changeicon();

    @Default(lngs = { "en" }, values = { "(%s2) %s1" })
    String InfoPanel_updateInfo_header_actionlabel(String name, String infoPanel_update_action);

    @Default(lngs = { "en" }, values = { "Name" })
    String InfoPanel_InfoPanel_itemname();

    @Default(lngs = { "en" }, values = { "Add %s1" })
    String AddGenericItem_AddGenericItem_(String name);

    @Default(lngs = { "en" }, values = { "Seperator" })
    String SeperatorData_SeperatorData();

    @Default(lngs = { "en" }, values = { "export" })
    String lit_export();

    @Default(lngs = { "en" }, values = { "import" })
    String lit_import();

    @Default(lngs = { "en" }, values = { "Export the current Menu Structure" })
    String ManagerFrame_actionPerformed_export_title();

    @Default(lngs = { "en" }, values = { "An Error occured..." })
    String lit_error_occured();

    @Default(lngs = { "en" }, values = { "Use Account(s) to download" })
    String AccountManagerSettings_AccountManagerSettings_disable_global();

    @Default(lngs = { "en" }, values = { "Main Toolbar" })
    String gui_config_menumanager_toolbar();

    @Default(lngs = { "en" }, values = { "All Actions are already on use in the selected submenu" })
    String AddActionAction_getListCellRendererComponent_no_action_();

    @Default(lngs = { "en" }, values = { "Downloads list - Rightclick menu" })
    String DownloadListContextMenuManager_getName();

    @Default(lngs = { "en" }, values = { "LinkGrabber list - Rightclick menu" })
    String LinkgrabberContextMenuManager_getName();

    @Default(lngs = { "en" }, values = { "Main Toolbar" })
    String MainToolbarManager_getName();

    @Default(lngs = { "en" }, values = { "%s1" })
    String ExtensionQuickToggleAction_name_selected2(String name);

    @Default(lngs = { "en" }, values = { "%s1" })
    String ExtensionQuickToggleAction_name_deselected2(String name);

    @Default(lngs = { "en" }, values = { "Disable  the '%s1' Extension" })
    String ExtensionQuickToggleAction_name_selected_tt(String name);

    @Default(lngs = { "en" }, values = { "Enable the '%s1' Extension" })
    String ExtensionQuickToggleAction_name_deselected_tt(String name);

    @Default(lngs = { "en" }, values = { "Move to Top" })
    String MoveToTopAction_MoveToTopAction();

    @Default(lngs = { "en" }, values = { "Move up" })
    String MoveUpAction_MoveUpAction();

    @Default(lngs = { "en" }, values = { "Move to Bottom" })
    String MoveToBottomAction_MoveToBottomAction();

    @Default(lngs = { "en" }, values = { "Move down" })
    String MoveDownAction_MoveDownAction();

    @Default(lngs = { "en" }, values = { "Main Menu" })
    String gui_config_menumanager_mainmenu();

    @Default(lngs = { "en" }, values = { "Main Menu" })
    String MainMenuManager_getName();

    @Default(lngs = { "en" }, values = { "Shortcut" })
    String InfoPanel_InfoPanel_shortcuts();

    @Default(lngs = { "en" }, values = { "Click here & press a shortcut..." })
    String InfoPanel_InfoPanel_shortcuthelp2();

    @Default(lngs = { "en" }, values = { "Enter a name for this action..." })
    String InfoPanel_InfoPanel_customname_help();

    @Default(lngs = { "en" }, values = { "Rename" })
    String RenameAction_RenameAction();

    @Default(lngs = { "en" }, values = { "Rename the currently selected Link or Package" })
    String RenameAction_RenameAction_tt();

    @Default(lngs = { "en" }, values = { "User Interface" })
    String GUISettings_getTitle();

    @Default(lngs = { "en" }, values = { "Customize JDownloader -  Mofify the User Interface, Menus, Language, ... whatever" })
    String GUISettings_GUISettings_description();

    @Default(lngs = { "en" }, values = { "Tray Menu" })
    String gui_config_menumanager_traymenu();

    @Default(lngs = { "en" }, values = { "Enable & disable the Silent Mode - Avoid Popups" })
    String action_silentmode_tooltip();

    @Default(lngs = { "en" }, values = { "Restart required for %s1" })
    String AdvancedConfigEntry_setValue_restart_warning_title(String key);

    @Default(lngs = { "en" }, values = { "You changed the %s1-option. You have to restart JDownloader to reinitialize this option." })
    String AdvancedConfigEntry_setValue_restart_warning(String key);

    @Default(lngs = { "en" }, values = { "Enable/Disable all Captcha Exchange Services  (like 9kw, Captchabrotherhood, ...)" })
    String CaptchaExchangeToogleAction_createTooltip_();

    @Default(lngs = { "en" }, values = { "Enable/Disable JAC - the JDownloader Auto Captcha Solver" })
    String JAntiCaptchaToogleAction_createTooltip_();

    @Default(lngs = { "en" }, values = { "Enable/Disable Remote Anti Captcha (Like Mobile Apps, My.JDownloader,..." })
    String RemoteCaptchaToogleAction_createTooltip_();

    @Default(lngs = { "en" }, values = { "Enable/Disable Captcha Dialogs. (Check our SilentMode)" })
    String CaptchaDialogsToogleAction_createTooltip_();

    @Default(lngs = { "en" }, values = { "Auto Reconnect" })
    String AutoReconnectToggleAction_getNameWhenDisabled_();

    @Default(lngs = { "en" }, values = { "Auto Reconnect" })
    String AutoReconnectToggleAction_getNameWhenEnabled_();

    @Default(lngs = { "en" }, values = { "Captcha Dialogs" })
    String CaptchaDialogsToogleAction_getNameWhenDisabled_();

    @Default(lngs = { "en" }, values = { "Captcha Dialogs" })
    String CaptchaDialogsToogleAction_getNameWhenEnabled_();

    @Default(lngs = { "en" }, values = { "Captcha Exchange Services" })
    String CaptchaExchangeToogleAction_getNameWhenDisabled_();

    @Default(lngs = { "en" }, values = { "Captcha Exchange Services" })
    String CaptchaExchangeToogleAction_getNameWhenEnabled_();

    @Default(lngs = { "en" }, values = { "Clipboard Monitoring" })
    String ClipBoardToggleAction_getNameWhenDisabled_();

    @Default(lngs = { "en" }, values = { "Clipboard Monitoring" })
    String ClipBoardToggleAction_getNameWhenEnabled_();

    @Default(lngs = { "en" }, values = { "Premium Downloads" })
    String GlobalPremiumSwitchToggleAction_getNameWhenDisabled_();

    @Default(lngs = { "en" }, values = { "Premium Downloads" })
    String GlobalPremiumSwitchToggleAction_getNameWhenEnabled_();

    @Default(lngs = { "en" }, values = { "Auto Captcha Recognition" })
    String JAntiCaptchaToogleAction_getNameWhenDisabled_();

    @Default(lngs = { "en" }, values = { "Auto Captcha Recognition" })
    String JAntiCaptchaToogleAction_getNameWhenEnabled_();

    @Default(lngs = { "en" }, values = { "Captchas via My.JDownloader" })
    String RemoteCaptchaToogleAction_getNameWhenDisabled_();

    @Default(lngs = { "en" }, values = { "Captchas via My.JDownloader" })
    String RemoteCaptchaToogleAction_getNameWhenEnabled_();

    @Default(lngs = { "en" }, values = { "Silent Mode" })
    String SilentModeToggleAction_getNameWhenDisabled_();

    @Default(lngs = { "en" }, values = { "Silent Mode" })
    String SilentModeToggleAction_getNameWhenEnabled_();

    @Default(lngs = { "en" }, values = { "Captcha Chance To Skip Bubble Timeout" })
    String CaptchaExchangeSpinnerAction_skipbubbletimeout_();

    @Default(lngs = { "en" }, values = { "More..." })
    String premiumaccounttablemodel_column_info();

    @Default(lngs = { "en" }, values = { "Show Details" })
    String premiumaccounttablemodel_column_info_button();

    @Default(lngs = { "en" }, values = { "Menus and Toolbars" })
    String gui_config_menumanager_header();

    @Default(lngs = { "en" }, values = { "General Reconnect Options" })
    String ReconnectSettings_ReconnectSettings_settings_();

    @Default(lngs = { "en" }, values = { "General Reconnect related options can be found here." })
    String ReconnectSettings_ReconnectSettings_settings_desc();

    @Default(lngs = { "en" }, values = { "Auto Reconnect Enabled" })
    String ReconnectSettings_ReconnectSettings_enabled_();

    @Default(lngs = { "en" }, values = { "Do not start downloads if others wait for a Reconnect" })
    String ReconnectSettings_ReconnectSettings_prefer_reconnect_desc();

    @Default(lngs = { "en" }, values = { "Reconnects can interrupt resumable downloads" })
    String ReconnectSettings_ReconnectSettings_interrupt_resumable_allowed();

    @Default(lngs = { "en" }, values = { "Enter a comment or leave empty..." })
    String AddLinksDialog_layoutDialogContent_comment_help();

    @Default(lngs = { "en" }, values = { "Enter a comment. This comment will be stored in every package and downloadlink." })
    String AddLinksDialog_layoutDialogContent_comment_tt();

    @Default(lngs = { "en" }, values = { "Please choose a plugin: " })
    String PluginSettingsPanel_runInEDT_choose_();

    @Default(lngs = { "en" }, values = { "Reset Window Positions and Dimensions" })
    String GUISettings_GUISettings_resetdialog_positions_();

    @Default(lngs = { "en" }, values = { "Native Authentication" })
    String gui_column_nativeauth();

    @Default(lngs = { "en" }, values = { "Default Connection" })
    String ProxyConfig_ProxyConfig_defaultproxy_();

    @Default(lngs = { "en" }, values = { "Found Proxies: %s1" })
    String ProxyAutoAction_run_added_proxies_(int size);

    @Default(lngs = { "en" }, values = { "Horizontal Scrollbar" })
    String HorizontalScrollbarAction_columnControlMenu_scrollbar_();

    @Default(lngs = { "en" }, values = { "Overwrite file?" })
    String lit_file_exists();

    @Default(lngs = { "en" }, values = { "The file %s1 already exists.\r\nDo you want to overwrite it?" })
    String lit_file_already_exists_overwrite_question(String absoluteFile);

    @Default(lngs = { "en" }, values = { "No file selected!" })
    String DLCFactory_createDLCByCrawledLinks_nofile_title();

    @Default(lngs = { "en" }, values = { "Please choose a valid path to save the DownloadLinkContainer (DLC)." })
    String DLCFactory_createDLCByCrawledLinks_nofile_msg();

    @Default(lngs = { "en" }, values = { "DLC creation Sucessful" })
    String DLCFactory_writeDLC_success_ok();

    @Default(lngs = { "en" }, values = { "Open Path" })
    String DLCFactory_writeDLC_showpath();

    @Default(lngs = { "en" }, values = { "Close" })
    String lit_close();

    @Default(lngs = { "en" }, values = { "Overview" })
    String LinkgrabberOverViewHeader_LinkgrabberOverViewHeader_();

    @Default(lngs = { "en" }, values = { "Hoster(s):" })
    String DownloadOverview_DownloadOverview_hoster();

    @Default(lngs = { "en" }, values = { "Online:" })
    String DownloadOverview_DownloadOverview_online();

    @Default(lngs = { "en" }, values = { "Offline:" })
    String DownloadOverview_DownloadOverview_offline();

    @Default(lngs = { "en" }, values = { "Unknown Status:" })
    String DownloadOverview_DownloadOverview_unknown();

    @Default(lngs = { "en" }, values = { "Cannot modify this rule" })
    String PackagizerFilterRuleDialog_layoutDialogContent_cannot_modify_();

    @Default(lngs = { "en" }, values = { "Predifined rules cannot be modified" })
    String PackagizerFilter_valueChanged_disable_static();

    @Default(lngs = { "en" }, values = { "Predefined rule: %s1" })
    String FilterTableModel_initColumns_static_(String name);

    @Default(lngs = { "en" }, values = { "My.JDownloader" })
    String MyJDownloaderSettingsPanel_MyJDownloaderSettingsPanel_title_();

    @Default(lngs = { "en" }, values = { "Sign up for an account at my.jdownloader.org, and access a lot of premium features like a JDownloader Remotecontrol via a Webinterface for your Browser, or apps for Android, IPhone and WindowsPhone" })
    String MyJDownloaderSettingsPanel_MyJDownloaderSettingsPanel_description();

    @Default(lngs = { "en" }, values = { "Go to My.JDownloader.org" })
    String MyJDownloaderSettingsPanel_MyJDownloaderSettingsPanel_open_();

    @Default(lngs = { "en" }, values = { "My Account" })
    String MyJDownloaderSettingsPanel_MyJDownloaderSettingsPanel_logins_();

    @Default(lngs = { "en" }, values = { "Black-/Whitelist" })
    String MyJDownloaderSettingsPanel_MyJDownloaderSettingsPanel_blackwhitelist_();

    @Default(lngs = { "en" }, values = { "Username/Email" })
    String MyJDownloaderSettingsPanel_MyJDownloaderSettingsPanel_username_();

    @Default(lngs = { "en" }, values = { "Password" })
    String MyJDownloaderSettingsPanel_MyJDownloaderSettingsPanel_password_();

    @Default(lngs = { "en" }, values = { "Your Account has been deactivated because your Email has not been confirmed yet. We sent you an Email to confirm your adress. Please click the Link in the Email." })
    String MyJDownloaderSettingsPanel_runInEDT_account_unconfirmed_();

    @Default(lngs = { "en" }, values = { "Your Account has been deactivated because an error occured: %s1" })
    String MyJDownloaderSettingsPanel_runInEDT_account_unknown(String string);

    @Default(lngs = { "en" }, values = { "Your Account has been deactivated because the entered Username/Email or Password is wrong." })
    String MyJDownloaderSettingsPanel_runInEDT_account_badlogins();

    @Default(lngs = { "en" }, values = { "Connection estabilished. Great!" })
    String MyJDownloaderSettingsPanel_runInEDT_connected_2();

    @Default(lngs = { "en" }, values = { "Current connections: %s1" })
    String MyJDownloaderSettingsPanel_runInEDT_connections(int connections);

    @Default(lngs = { "en" }, values = { "Not Connected!" })
    String MyJDownloaderSettingsPanel_runInEDT_disconnected_();

    @Default(lngs = { "en" }, values = { "Connect" })
    String MyJDownloaderSettingsPanel_MyJDownloaderSettingsPanel_connect_();

    @Default(lngs = { "en" }, values = { "Disconnect" })
    String MyJDownloaderSettingsPanel_MyJDownloaderSettingsPanel_disconnect_();

    @Default(lngs = { "en" }, values = { "Auto connect" })
    String MyJDownloaderSettingsPanel_MyJDownloaderSettingsPanel_autoconnect_();

    @Default(lngs = { "en" }, values = { "Auto connect on JDownloader start" })
    String MyJDownloaderSettingsPanel_MyJDownloaderSettingsPanel_autoconnect_tt();

    @Default(lngs = { "en" }, values = { "Save Changes" })
    String MyJDownloaderSettingsPanel_MyJDownloaderSettingsPanel_reconnect_();

    @Default(lngs = { "en" }, values = { "Enter the device name of this JDownloader instance. You can manage different JDownloader instances in one My.JDownloader Account." })
    String MyJDownloaderSettingsPanel_MyJDownloaderSettingsPanel_jd_name();

    @Default(lngs = { "en" }, values = { "Device Name" })
    String MyJDownloaderSettingsPanel_MyJDownloaderSettingsPanel_devicename_();

    @Default(lngs = { "en" }, values = { "Enter your My.JDownloader Logins below. If you do not have an account yet, click the Link above and register for an account. It's free!" })
    String MyJDownloaderSettingsPanel_MyJDownloaderSettingsPanel_jd_logins();

    @Default(lngs = { "en" }, values = { "Updates available!" })
    String JDGui_updateTitle_updates_available2();

    @Default(lngs = { "en" }, values = { "Lock all Columns" })
    String LockAllColumnsAction_LockAllColumnsAction_lockall_columns_();

    @Default(lngs = { "en" }, values = { "Unlock all Columns" })
    String LockAllColumnsAction_LockAllColumnsAction_unlockall_columns_();

    @Default(lngs = { "en" }, values = { "Choose new Color:" })
    String AdvancedValueColumn_onSingleClick_colorchooser_title_();

    @Default(lngs = { "en" }, values = { "...you can use dynamic variables or Regular Expression matches here.\r\nJust do a rightclick in the textfield to explorer the full power of the Packagizer" })
    String PackagizerFilterRuleDialog_layoutDialogContent_help_dynamic_variables();

    @Default(lngs = { "en" }, values = { "Menuitem is visible" })
    String InfoPanel_InfoPanel_hidden_2();

    @Default(lngs = { "en" }, values = { "disabled" })
    String lit_disabled();

    @Default(lngs = { "en" }, values = { "failed" })
    String lit_failed();

    @Default(lngs = { "en" }, values = { "offline" })
    String lit_offline();

    @Default(lngs = { "en" }, values = { "finished" })
    String lit_finished();

    @Default(lngs = { "en" }, values = { "Delete Selected Links" })
    String GenericDeleteSelectedToolbarAction_updateName_object_selected_all();

    @Default(lngs = { "en" }, values = { "Delete Links: Selected & " })
    String GenericDeleteSelectedToolbarAction_updateName_object_selected();

    @Default(lngs = { "en" }, values = { "Load Proxy Setup" })
    String SetProxySetupAction_SetProxySetupAction_();

    @Default(lngs = { "en" }, values = { "Export Proxy Setup" })
    String SaveAsProxyProfileAction_SaveAsProxyProfileAction_();

    @Default(lngs = { "en" }, values = { "Load Proxy Setup" })
    String LoadProxyProfileAction_LoadProxyProfileAction_();

    @Default(lngs = { "en" }, values = { "Save Proxy Setup" })
    String SaveAsProxyProfileAction_actionPerformed_choose_file();

    @Default(lngs = { "en" }, values = { "Load Proxy Setup" })
    String LoadProxyProfileAction_actionPerformed_();

    @Default(lngs = { "en" }, values = { "Window Management" })
    String GUISettings_GUISettings_object_frames();

    @Default(lngs = { "en" }, values = { "Ever got annoyed by Dialog Popups, Windows coming to front, Captcha Dialogs that steal the focus from other Windows or anything else Window or focus related? Yes? We're sorry! Please take the following options as our apology." })
    String GUISettings_GUISettings_object_frames_description();

    @Default(lngs = { "en" }, values = { "Silent Mode" })
    String GUISettings_GUISettings_sielntMode();

    @Default(lngs = { "en" }, values = { "If the Silentmode is enabled, you will not get any Popup, Dialogs, Captcharequests or other notifications unless JDownloader is your active Window." })
    String GUISettings_GUISettings_sielntMode_description();

    @Default(lngs = { "en" }, values = { "according to %s1 specifications" })
    String GUISettings_GUISettings_framestate_os_default(String string);

    @Default(lngs = { "en" }, values = { "behind all windows" })
    String GUISettings_GUISettings_framestate_back();

    @Default(lngs = { "en" }, values = { "in front of all windows" })
    String GUISettings_GUISettings_framestate_front();

    @Default(lngs = { "en" }, values = { "in front of all windows and focused" })
    String GUISettings_GUISettings_framestate_focus();

    @Default(lngs = { "en" }, values = { "Show new Dialogs..." })
    String GUISettings_GUISettings_dialog_focus();

    @Default(lngs = { "en" }, values = { "When new Links were added, then..." })
    String GUISettings_GUISettings_dialog_linkgrabber_on_new_links();

    @Default(lngs = { "en" }, values = { "do nothing" })
    String GUISettings_GUISettings_newlinks_nothing();

    @Default(lngs = { "en" }, values = { "show linkgrabber in front of all other windows" })
    String GUISettings_GUISettings_newlinks_front();

    @Default(lngs = { "en" }, values = { "show linkgrabber in front of all other windows & steal the focus" })
    String GUISettings_GUISettings_newlinks_focus();

    @Default(lngs = { "en" }, values = { "Extraction error" })
    String TaskColumn_getStringValue_extraction_error();

    @Default(lngs = { "en" }, values = { "Extraction error: CRC" })
    String TaskColumn_getStringValue_extraction_error_crc();

    @Default(lngs = { "en" }, values = { "Extraction error: Not enough Space" })
    String TaskColumn_getStringValue_extraction_error_space();

    @Default(lngs = { "en" }, values = { "Extraction error: File not found" })
    String TaskColumn_getStringValue_extraction_error_file_not_found();

    @Default(lngs = { "en" }, values = { "Extraction OK" })
    String TaskColumn_getStringValue_extraction_success();

    @Default(lngs = { "en" }, values = { "A System reboot is required for changes to take effect." })
    String LookAndFeelController_onConfigValueModified_reboot_required();

    @Default(lngs = { "en" }, values = { "Click here to install pending updates!" })
    String UpdateAction_runInEDT_updates_pendings();

    @Default(lngs = { "en" }, values = { "Click here to search for new updates!" })
    String UpdateAction_runInEDT_no_updates_pendings();

    @Default(lngs = { "en" }, values = { "Find Updates" })
    String CheckForUpdatesAction_CheckForUpdatesAction();

    @Default(lngs = { "en" }, values = { "Clear filtered Links" })
    String ClearFilteredLinksAction();

    @Default(lngs = { "en" }, values = { "Do you really want to remove all filtered Links(%s1)?" })
    String ClearFilteredLinksAction_msg(int count);

    @Default(lngs = { "en" }, values = { "Quick Settings" })
    String quicksettings_SettingsMenuContainer();

    @Default(lngs = { "en" }, values = { "Open notify settings" })
    String Notify_createHeader_settings_tt();

    @Default(lngs = { "en" }, values = { "Close this notification" })
    String Notify_createHeader_close_tt();

    @Default(lngs = { "en" }, values = { "Bubble Notify" })
    String NotifierConfigPanel_getTitle();

    @Default(lngs = { "en" }, values = { "New Links added" })
    String balloon_new_links();

    @Default(lngs = { "en" }, values = { "Bubble Notifications are tiny messages that notify you when special events occur. " })
    String plugins_optional_JDLightTray_ballon_desc();

    @Default(lngs = { "en" }, values = { "during Linkcrawling and Onlinecheck" })
    String plugins_optional_JDLightTray_ballon_newlinks3();

    @Default(lngs = { "en" }, values = { "an Update is ready for installation" })
    String plugins_optional_JDLightTray_ballon_updates2();

    @Default(lngs = { "en" }, values = { "JDownloader Updates available!" })
    String balloon_updates();

    @Default(lngs = { "en" }, values = { "New Updates for JDownloader are available.\r\nClick here to start Installation!" })
    String balloon_updates_msg();

    @Default(lngs = { "en" }, values = { "during a Reconnect" })
    String plugins_optional_JDLightTray_ballon_reconnectstart3();

    @Default(lngs = { "en" }, values = { "JDownloader Reconnect" })
    String balloon_reconnect();

    @Default(lngs = { "en" }, values = { "Bubble Settings" })
    String BubbleNotifyConfigPanel_BubbleNotifyConfigPanel_settings_();

    @Default(lngs = { "en" }, values = { "Show Bubbles while Silent Mode is active" })
    String BubbleNotifyConfigPanel_BubbleNotifyConfigPanel_silent_();

    @Default(lngs = { "en" }, values = { "Captcha Input required: %s1" })
    String CaptchaNotify_CaptchaNotify_title_(String host);

    @Default(lngs = { "en" }, values = { "Click here to solve the Captcha" })
    String CaptchaNotify_CaptchaNotifyPanel_text();

    @Default(lngs = { "en" }, values = { "a Captcha Dialog is waiting in the Background" })
    String plugins_optional_JDLightTray_ballon_captcha2();

    @Default(lngs = { "en" }, values = { "Time to Live (in ms)" })
    String BubbleNotifyConfigPanel_BubbleNotifyConfigPanel_timeout();

    @Default(lngs = { "en" }, values = { "Animation duration (Fade in & Out time in ms)" })
    String BubbleNotifyConfigPanel_BubbleNotifyConfigPanel_fadetime();

    @Default(lngs = { "en" }, values = { "Reset selected link(s)?\r\nThis would reset %s1 Link(s) & delete %s3 file(s)(%s2) from harddisk." })
    String gui_downloadlist_reset2(int totalCount, String formatBytes, int localFileCount);

    @Default(lngs = { "en" }, values = { "Linkgrabber" })
    String GeneralSettingsConfigPanel_GeneralSettingsConfigPanel_linkgrabber();

    @Default(lngs = { "en" }, values = { "Group single files in a 'various package'" })
    String GeneralSettingsConfigPanel_GeneralSettingsConfigPanel_various_package();

    @Default(lngs = { "en" }, values = { "Captchas" })
    String AntiCaptchaConfigPanel_getTitle();

    @Default(lngs = { "en" }, values = { "Plugins" })
    String PluginSettings_getTitle();

    @Default(lngs = { "en" }, values = { "Many services ask you to enter a so called 'Captcha'. Usually, a captcha is a tiny image that contains a few letters. You have to type these letters to proove that you are human. JDownloader will try to solve these captchas without asking you. However, there are captchas that are too hard to read for JDownloader - thus JD will ask you." })
    String AntiCaptchaConfigPanel_onShow_description();

    @Default(lngs = { "en" }, values = { "Captcha Solver Services" })
    String AntiCaptchaConfigPanel_AntiCaptchaConfigPanel_solver();

    @Default(lngs = { "en" }, values = { "Play Notify Sound for Captchas" })
    String AntiCaptchaConfigPanel_AntiCaptchaConfigPanel_sounds();

    @Default(lngs = { "en" }, values = { "Download Captchas auto close after timeout" })
    String AntiCaptchaConfigPanel_AntiCaptchaConfigPanel_countdown_download();

    @Default(lngs = { "en" }, values = { "Some captchas are too hard for JDownloader to auto recognize. There are a few Captcha Solver Services that help you to get these captchas solved anyways..." })
    String AntiCaptchaConfigPanel_onShow_description_solver();

    @Default(lngs = { "en" }, values = { "The my.JDownloader.org Service sends all captcha requests to your mobile or any other internet device. Use this service if you want to solve captchas remotely." })
    String MyJDownloaderService_createPanel_description_();

    @Default(lngs = { "en" }, values = { "Earn 'credits' by solving captchas for others. In return, others solve your captchas while you are not in front of your computer. Check out the service's website for more details." })
    String AntiCaptchaConfigPanel_onShow_description_ces();

    @Default(lngs = { "en" }, values = { "Visit the Website" })
    String lit_open_website();

    @Default(lngs = { "en" }, values = { "Enter Api Key" })
    String NinekwService_createPanel_apikey();

    @Default(lngs = { "en" }, values = { "Enter your 9kw.eu API Key below. If you do not have an account yet, click the Link above and register for an account. It's free!" })
    String NinekwService_createPanel_logins_();

    @Default(lngs = { "en" }, values = { "Enter your deathbycaptcha.eu username and password below. If you do not have an account yet, click the Link above and register for an account!" })
    String dbcService_createPanel_logins_();

    @Default(lngs = { "en" }, values = { "Enable 9kw.eu Service" })
    String NinekwService_createPanel_enabled();

    @Default(lngs = { "en" }, values = { "Blacklist" })
    String NinekwService_createPanel_blacklist();

    @Default(lngs = { "en" }, values = { "Blacklistcheck" })
    String NinekwService_createPanel_blacklistcheck();

    @Default(lngs = { "en" }, values = { "Whitelist" })
    String NinekwService_createPanel_whitelist();

    @Default(lngs = { "en" }, values = { "Whitelistcheck" })
    String NinekwService_createPanel_whitelistcheck();

    @Default(lngs = { "en" }, values = { "Blacklist (Prio)" })
    String NinekwService_createPanel_blacklistprio();

    @Default(lngs = { "en" }, values = { "Blacklistcheck (Prio)" })
    String NinekwService_createPanel_blacklistpriocheck();

    @Default(lngs = { "en" }, values = { "Whitelist (Prio)" })
    String NinekwService_createPanel_whitelistprio();

    @Default(lngs = { "en" }, values = { "Whitelistcheck (Prio)" })
    String NinekwService_createPanel_whitelistpriocheck();

    @Default(lngs = { "en" }, values = { "Blacklist (Timeout)" })
    String NinekwService_createPanel_blacklisttimeout();

    @Default(lngs = { "en" }, values = { "Blacklistcheck (Timeout)" })
    String NinekwService_createPanel_blacklisttimeoutcheck();

    @Default(lngs = { "en" }, values = { "Whitelist (Timeout)" })
    String NinekwService_createPanel_whitelisttimeout();

    @Default(lngs = { "en" }, values = { "Whitelistcheck (Timeout)" })
    String NinekwService_createPanel_whitelisttimeoutcheck();

    @Default(lngs = { "en" }, values = { "With Mouse" })
    String NinekwService_createPanel_mouse();

    @Default(lngs = { "en" }, values = { "Feedback" })
    String NinekwService_createPanel_feedback();

    @Default(lngs = { "en" }, values = { "Warn on low credits" })
    String NinekwService_createPanel_lowcredits();

    @Default(lngs = { "en" }, values = { "Https" })
    String NinekwService_createPanel_https();

    @Default(lngs = { "en" }, values = { "Confirm (Cost +6)" })
    String NinekwService_createPanel_confirm();

    @Default(lngs = { "en" }, values = { "Mouse Confirm (Cost +6)" })
    String NinekwService_createPanel_mouseconfirm();

    @Default(lngs = { "en" }, values = { "Debug" })
    String NinekwService_createPanel_debug();

    @Default(lngs = { "en" }, values = { "Selfsolve" })
    String NinekwService_createPanel_selfsolve();

    @Default(lngs = { "en" }, values = { "Prio 1-20 (Cost +1-20)" })
    String NinekwService_createPanel_prio();

    @Default(lngs = { "en" }, values = { "Captcha per hour" })
    String NinekwService_createPanel_hour();

    @Default(lngs = { "en" }, values = { "Max. Captchas Parallel" })
    String NinekwService_createPanel_threadsizepool();

    @Default(lngs = { "en" }, values = { "ms - Max. Timeout (60-3999s)" })
    String NinekwService_createPanel_9kwtimeout();

    @Default(lngs = { "en" }, values = { "New Timeout (60-3999s)" })
    String NinekwService_createPanel_9kwtimeoutother();

    @Default(lngs = { "en" }, values = { "Enter your Captcha Brotherhood Logins below. If you do not have an account yet, click the Link above and register for an account. It's free!" })
    String captchabrotherhoodService_createPanel_logins_();

    @Default(lngs = { "en" }, values = { "Username" })
    String captchabrotherhoodService_createPanel_username();

    @Default(lngs = { "en" }, values = { "Password" })
    String captchabrotherhoodService_createPanel_password();

    @Default(lngs = { "en" }, values = { "Blacklist" })
    String captchabrotherhoodService_createPanel_blacklist();

    @Default(lngs = { "en" }, values = { "Whitelist" })
    String captchabrotherhoodService_createPanel_whitelist();

    @Default(lngs = { "en" }, values = { "Finding Links..." })
    String LinkCrawlerBubble_update_header();

    @Default(lngs = { "en" }, values = { "Parse Clipboard" })
    String LinkCrawlerBubble_update_header_from_Clipboard();

    @Default(lngs = { "en" }, values = { "Parse Clipboard: %s1" })
    String LinkCrawlerBubble_update_header_from_Clipboard_url(String txt);

    @Default(lngs = { "en" }, values = { "Pending Downloads" })
    String downloadview_todo();

    @Default(lngs = { "en" }, values = { "a dedicated plugin (no direct http)" })
    String ConditionDialog_layoutDialogContent_directhttp();

    @Default(lngs = { "en" }, values = { "Link has no dedicated Plugin(direct http)" })
    String FilterRule_toString_directhttp();

    @Default(lngs = { "en" }, values = { "Link has a dedicated Plugin" })
    String FilterRule_toString_directhttp_not();

    @Default(lngs = { "en" }, values = { "Start all Downloads" })
    String ConfirmAllContextmenuAction_context_add_and_start();

    @Default(lngs = { "en" }, values = { "Add all to Download List" })
    String ConfirmAllContextmenuAction_context_add();

    @Default(lngs = { "en" }, values = { "Stop Downloads, but finish running ones" })
    String StopDownloadsAction_createTooltip();

    @Default(lngs = { "en" }, values = { "Overview" })
    String AccountManager_AccountManager_accounts_();

    @Default(lngs = { "en" }, values = { "Account Usage Rules" })
    String AccountManager_AccountManager_hosterorder_();

    @Default(lngs = { "en" }, values = { "Account Group" })
    String FileColumn_getStringValue_accountgroup_();

    @Default(lngs = { "en" }, values = { "Hoster" })
    String HosterRuleTableModel_initColumns_hoster_();

    @Default(lngs = { "en" }, values = { "Use Hoster Rules if you have more than one Premium Account and want to specify in which order these accounts should be used." })
    String HosterOrderPanel_HosterOrderPanel_description_();

    @Default(lngs = { "en" }, values = { "Enabled Captchabrotherhood Service" })
    String captchabrotherhoodService_createPanel_enabled();

    @Default(lngs = { "en" }, values = { "Add, remove or modify premium accounts" })
    String AccountManager_AccountManager_accounts_tt();

    @Default(lngs = { "en" }, values = { "Set up rules to control the account order, in case you have more than one account for an hoster" })
    String AccountManager_AccountManager_hosterorder_tt();

    @Default(lngs = { "en" }, values = { "Add a new Rule" })
    String NewRuleAction_getTooltipText_tt_();

    @Default(lngs = { "en" }, values = { "Please enter a Domain" })
    String NewRuleAction_actionPerformed_choose_hoster_();

    @Default(lngs = { "en" }, values = { "Please choose a Hoster to create a rule for.\r\nIf the desired Hoster is missing, you probably do not have an account for this hoster.\r\nMake sure to add an Account first in the Account Overview." })
    String NewRuleAction_actionPerformed_choose_hoster_message();

    @Default(lngs = { "en" }, values = { "Edit" })
    String HosterRuleTableModel_initColumns_edit_();

    @Default(lngs = { "en" }, values = { "Remove %s1 rule(s)?" })
    String accountUsageRule_remove_action_title(int size);

    @Default(lngs = { "en" }, values = { "Do you really want to remove these rule(s)?\r\n%s1" })
    String accountUsageRule_remove_action_msg(String string);

    @Default(lngs = { "en" }, values = { "Account Usage Rule for %s1" })
    String EditHosterRuleDialog_EditHosterRuleDialog_title_(String hoster);

    @Default(lngs = { "en" }, values = { "Add Group" })
    String AddGroupAction_AddGroupAction();

    @Default(lngs = { "en" }, values = { "Distribution Rule" })
    String GroupRuleColumn_GroupRuleColumn_distrubutionrule_();

    @Default(lngs = { "en" }, values = { "Randomly use an account of this group" })
    String Rules_random();

    @Default(lngs = { "en" }, values = { "First comes first - Use the given order" })
    String Rules_order();

    @Default(lngs = { "en" }, values = { "There is nothing to delete here!" })
    String GenericDeleteSelectedToolbarAction_actionPerformed_nothing_to_delete_();

    @Default(lngs = { "en" }, values = { "Clear Downloadlist" })
    String GenericDeleteSelectedToolbarAction_updateName_object_all();

    @Default(lngs = { "en" }, values = { "Delete Links: " })
    String GenericDeleteSelectedToolbarAction_updateName_object();

    @Default(lngs = { "en" }, values = { "Below, all premium accounts and the 'free download' option for %s1 are listed. You can set up a custom order here. JDownloader will try to use the account(s) in the first group for downloading. If all Accounts in the first group fail, it will try the second group, and so on. If there are several accounts in one group, the group's 'Distribution Rule' will be used to select the next account." })
    String EditHosterRuleDialog_layoutDialogContent_description_(String hoster);

    @Default(lngs = { "en" }, values = { "Remove group" })
    String DeleteGroupAction_DeleteGroupAction();

    @Default(lngs = { "en" }, values = { "Single Hoster Accounts" })
    String HosterRuleController_validateRule_single_hoster_account();

    @Default(lngs = { "en" }, values = { "Multi Hoster Accounts" })
    String HosterRuleController_validateRule_multi_hoster_account();

    @Default(lngs = { "en" }, values = { "Download without any Account (Free Mode)" })
    String HosterRuleController_validateRule_free();

    @Default(lngs = { "en" }, values = { "Free Download (No Account)" })
    String PackageColumn_getStringValue_freedownload_();

    @Default(lngs = { "en" }, values = { "Reset the current plugin's settings to default." })
    String PluginSettingsPanel_PluginSettingsPanel_reset();

    @Default(lngs = { "en" }, values = { "The %s1 Plugin Settings have been resetted to default values!" })
    String PluginSettingsPanel_actionPerformed_reset_done(String domain);

    @Default(lngs = { "en" }, values = { "%s1 (Multi Hoster)" })
    String AccountTooltip_AccountTooltip_multi(String tld);

    @Default(lngs = { "en" }, values = { "With these account(s), you can download from:" })
    String AccountTooltip_AccountTooltip_supported_hosters();

    @Default(lngs = { "en" }, values = { "Are you sure that you want to reset all plugin settings for the %s1 plugin?" })
    String PluginSettingsPanel_are_you_sure(String displayName);

    @Default(lngs = { "en" }, values = { "Credit Balance:" })
    String ServicePanel9kwTooltip_runInEDT_credits_();

    @Default(lngs = { "en" }, values = { "Error while loading 9kw.eu Account Information" })
    String ServicePanel9kwTooltip_runInEDT_error();

    @Default(lngs = { "en" }, values = { "Solved Captchas:" })
    String ServicePanel9kwTooltip_runInEDT_solved();

    @Default(lngs = { "en" }, values = { "Answered Captchas:" })
    String ServicePanel9kwTooltip_runInEDT_answered();

    @Default(lngs = { "en" }, values = { "Account Error: %s1" })
    String ServicePanel9kwTooltip_runInEDT_error2(String error);

    @Default(lngs = { "en" }, values = { "Clean Up..." })
    String DeleteMenuContainer_DeleteMenuContainer_delete_2();

    @Default(lngs = { "en" }, values = { "Download list Bottombar Menumanager" })
    String BottomBarMenuManager_getName();

    @Default(lngs = { "en" }, values = { "Filter & Searchbar" })
    String FilterMenuItem_FilterMenuItem();

    @Default(lngs = { "en" }, values = { "Table Quickfilter" })
    String QuickFilterMenuItem_QuickFilterMenuItem();

    @Default(lngs = { "en" }, values = { "Download Tab Bottom Panel" })
    String gui_config_menumanager_downloadBottom();

    @Default(lngs = { "en" }, values = { "Apply" })
    String lit_apply();

    @Default(lngs = { "en" }, values = { "Customize this Bottom Panel" })
    String BottomBarMenuManagerAction_BottomBarMenuManagerAction();

    @Default(lngs = { "en" }, values = { "Horizontal expanding empty Box" })
    String HorizontalBoxItem_HorizontalBoxItem();

    @Default(lngs = { "en" }, values = { "Are you sure that you want to do this:\r\n%s1" })
    String GenericDeleteFromDownloadlistAction_actionPerformed_ask_(String taskname);

    @Default(lngs = { "en" }, values = { "Clear Linkgrabber List" })
    String GenericDeleteFromLinkgrabberAction_createName_updateName_object_all();

    @Default(lngs = { "en" }, values = { "Auto Confirm Button" })
    String AutoConfirmMenuLink_getName();

    @Default(lngs = { "en" }, values = { "Sidebar Seperator: |" })
    String LeftRightDividerItem_LeftRightDividerItem();

    @Default(lngs = { "en" }, values = { "Linkgrabber Bottom Bar" })
    String gui_config_menumanager_linkgrabberBottom();

    @Default(lngs = { "en" }, values = { "Overview Panel visible" })
    String DownloadsOverviewPanelToggleAction_DownloadsOverviewPanelToggleAction();

    @Default(lngs = { "en" }, values = { "Overview Panel visible" })
    String LinkgrabberOverviewPanelToggleAction_LinkgrabberOverviewPanelToggleAction();

    @Default(lngs = { "en" }, values = { "Sidebar visible" })
    String LinkgrabberSidebarToggleAction_LinkgrabberSidebarToggleAction();

    @Default(lngs = { "en" }, values = { "Add & Analyse Links from your Clipboard" })
    String PasteLinksAction_PasteLinksAction();

    @Default(lngs = { "en" }, values = { "Add & DeepAnalyse Links from your Clipboard" })
    String PasteLinksAction_PasteLinksAction_deep();

    @Default(lngs = { "en" }, values = { "Filename:" })
    String IfFileExistsDialog_layoutDialogContent_filename();

    @Default(lngs = { "en" }, values = { "Package:" })
    String IfFileExistsDialog_layoutDialogContent_package();

    @Default(lngs = { "en" }, values = { "Hoster:" })
    String IfFileExistsDialog_layoutDialogContent_hoster();

    @Default(lngs = { "en" }, values = { "New File's size:" })
    String IfFileExistsDialog_layoutDialogContent_filesize2();

    @Default(lngs = { "en" }, values = { "Archivename:" })
    String ExtractionListenerList_layoutDialogContent_archivename();

    @Default(lngs = { "en" }, values = { "First File:" })
    String ExtractionListenerList_layoutDialogContent_filename();

    @Default(lngs = { "en" }, values = { "Password:" })
    String ExtractionListenerList_layoutDialogContent_password();

    @Default(lngs = { "en" }, values = { "Reset to Default?" })
    String MenuManagerDialog_actionPerformed_title();

    @Default(lngs = { "en" }, values = { "Package or Link Properties" })
    String LinkgrabberPropertiesToggleAction_LinkgrabberPropertiesToggleAction();

    @Default(lngs = { "en" }, values = { "Package Properties: %s1" })
    String LinkgrabberPropertiesHeader_update_package(String name);

    @Default(lngs = { "en" }, values = { "File Properties: %s1" })
    String LinkgrabberPropertiesHeader_update_link(String name);

    @Default(lngs = { "en" }, values = { "Save to:" })
    String propertiespanel_downloadpath();

    @Default(lngs = { "en" }, values = { "Package name:" })
    String propertiespanel_packagename();

    @Default(lngs = { "en" }, values = { "Comment:" })
    String propertiespanel_comment();

    @Default(lngs = { "en" }, values = { "Archive Password:" })
    String propertiespanel_archivepassword();

    @Default(lngs = { "en" }, values = { "Panel has been hidden" })
    String DownloadsPanel_onCloseAction();

    @Default(lngs = { "en" }, values = { "you clicked the close button to hide this panel.\r\nIf you ever feel like getting it back, use the settings button in the bar at the bottom right!" })
    String DownloadsPanel_onCloseAction_help();

    @Default(lngs = { "en" }, values = { "you clicked the close button to hide the properties panel.\r\nIf you ever feel like getting it back, use the settings button in the bar at the bottom right\r\nor rightclick on a link/package in the table to open the context menu.!" })
    String Linkgrabber_properties_onCloseAction_help();

    @Default(lngs = { "en" }, values = { "You activated the properties panel!" })
    String LinkGrabberPanel_setPropertiesPanelVisible();

    @Default(lngs = { "en" }, values = { "...the properties panel is not visible right now, because there is no package or Downloadlink selected. \r\nTo see the properties panel, add Downloads to the Linkgrabber an click on a table row." })
    String LinkGrabberPanel_setPropertiesPanelVisible_help();

    @Default(lngs = { "en" }, values = { "Auto Extract Disabled" })
    String PackagePropertiesPanel_getListCellRendererComponent_autoextractdisabled();

    @Default(lngs = { "en" }, values = { "Auto Extract Enabled" })
    String PackagePropertiesPanel_getListCellRendererComponent_autoextractenabled();

    @Default(lngs = { "en" }, values = { "Global Setting(Auto Extract Enabled)" })
    String PackagePropertiesPanel_getListCellRendererComponent_autoextract_default_true();

    @Default(lngs = { "en" }, values = { "Global Setting(Auto Extract disable)" })
    String PackagePropertiesPanel_getListCellRendererComponent_autoextract_default_false();

    @Default(lngs = { "en" }, values = { "Auto Extract Disabled" })
    String PackagePropertiesPanel_getListCellRendererComponent_autoextractdisabled_closed();

    @Default(lngs = { "en" }, values = { "Auto Extract Enabled" })
    String PackagePropertiesPanel_getListCellRendererComponent_autoextractenabled_closed();

    @Default(lngs = { "en" }, values = { "Auto Extract Enabled*" })
    String PackagePropertiesPanel_getListCellRendererComponent_autoextract_default_true_closed();

    @Default(lngs = { "en" }, values = { "Auto Extract Disable*" })
    String PackagePropertiesPanel_getListCellRendererComponent_autoextract_default_false_closed();

    @Default(lngs = { "en" }, values = { "Mixed Priority" })
    String PackagePropertiesPanel_getLabel_mixed_priority();

    @Default(lngs = { "en" }, values = { "File name:" })
    String propertiespanel_filename();

    @Default(lngs = { "en" }, values = { "The File name" })
    String AddLinksDialog_layoutDialogContent_filename_tt();

    @Default(lngs = { "en" }, values = { "Open Properties Panel" })
    String PropertiesAction_PropertiesAction();

    @Default(lngs = { "en" }, values = { "Speedmeter visible" })
    String SpeedlimitToggleAction_SpeedlimitToggleAction();

    @Default(lngs = { "en" }, values = { "Open Toolbar manager" })
    String MenuManagerMainToolbarAction_MenuManagerMainToolbarAction();

    @Default(lngs = { "en" }, values = { "Pause" })
    String download_paused();

    @Default(lngs = { "en" }, values = { "All downloads have been paused." })
    String download_paused_msg();

    @Default(lngs = { "en" }, values = { "Stop" })
    String download_stopped();

    @Default(lngs = { "en" }, values = { "All downloads stopped or finished." })
    String download_stopped_msg();

    @Default(lngs = { "en" }, values = { "Start" })
    String download_start();

    @Default(lngs = { "en" }, values = { "Go, go, go! Downloads are running now!" })
    String download_start_msg();

    @Default(lngs = { "en" }, values = { "A Download started!" })
    String DownloadStartNotify_DownloadStartNotify();

    @Default(lngs = { "en" }, values = { "Filename" })
    String lit_filename();

    @Default(lngs = { "en" }, values = { "Hoster" })
    String lit_hoster();

    @Default(lngs = { "en" }, values = { "Account" })
    String lit_account();

    @Default(lngs = { "en" }, values = { "Proxy" })
    String lit_proxy();

    @Default(lngs = { "en" }, values = { "Save to" })
    String lit_save_to();

    @Default(lngs = { "en" }, values = { "A Download stopped!" })
    String DownloadStoppedNotify();

    @Default(lngs = { "en" }, values = { "Status" })
    String lit_status();

    @Default(lngs = { "en" }, values = { "a Downloadlink started or stopped" })
    String plugins_optional_JDLightTray_ballon_startstopdownloads2();

    @Default(lngs = { "en" }, values = { "the Download started, stopped or has been paused " })
    String plugins_optional_JDLightTray_ballon_startpausestop2();

    @Default(lngs = { "en" }, values = { "Sort Downloadorder on '%s1'" })
    String SortPackagesDownloadOrdnerOnColumn(String name);

    @Default(lngs = { "en" }, values = { "Archive Part" })
    String LinkGrabberTableModel_partcolumn();

    @Default(lngs = { "en" }, values = { "Download Password:" })
    String propertiespanel_passwod();

    @Default(lngs = { "en" }, values = { "Enter the password required to download the file..." })
    String AddLinksDialog_layoutDialogContent_password_tt();

    @Default(lngs = { "en" }, values = { "MD5/SHA1:" })
    String propertiespanel_checksum();

    @Default(lngs = { "en" }, values = { "Enter the MD5 or SHA1 Checksum..." })
    String AddLinksDialog_layoutDialogContent_checksum_tt();

    @Default(lngs = { "en" }, values = { "Link origin" })
    String FilterRuleDialog_layoutDialogContent_lbl_crawlersource();

    @Default(lngs = { "en" }, values = { "No Selection" })
    String PseudoMultiCombo_nothing();

    @Default(lngs = { "en" }, values = { "Custom Type" })
    String ConditionDialog_getLabel_customtype_();

    @Default(lngs = { "en" }, values = { "Unknown" })
    String OriginFilter_toString_nothing();

    @Default(lngs = { "en" }, values = { "Link Source is: %s1" })
    String OriginFilter_toString(String string);

    @Default(lngs = { "en" }, values = { "Link Source is not: %s1" })
    String OriginFilter_toString_isNot(String string);

    @Default(lngs = { "en" }, values = { "Auto Forced Download Start" })
    String PackagizerFilterRuleDialog_layoutDialogContent_force();

    @Default(lngs = { "en" }, values = { "Open Links in Browser" })
    String OpenInBrowserAction_actionPerformed_open_in_browser__multi();

    @Default(lngs = { "en" }, values = { "You choose to open %s1 Links in your browser." })
    String OpenInBrowserAction_actionPerformed_open_in_browser__multi_msg(int size);

    @Default(lngs = { "en" }, values = { "Unknown" })
    String SizeColumn_getSizeString_zero();

    @Default(lngs = { "en" }, values = { "Captchas solved by the Captchabrotherhood Captcha Exchange System" })
    String CaptchaBrotherhoodService_getDescription_tt_();

    @Default(lngs = { "en" }, values = { "Captcha solved by My.JDownloader Remote Applications (Webinterface,Mobile Apps,...)" })
    String MyJDownloaderService_getDescription_tt_();

    @Default(lngs = { "en" }, values = { "Captchas solved by the 9kw.eu Captcha Exchange System" })
    String NinekwService_getDescription_tt_();

    @Default(lngs = { "en" }, values = { "Always" })
    String BubbleNotifyConfigPanel_BubbleNotifyConfigPanel_always();

    @Default(lngs = { "en" }, values = { "Only if JDownloader is not the active application" })
    String BubbleNotifyConfigPanel_BubbleNotifyConfigPanel_jdnotactive();

    @Default(lngs = { "en" }, values = { "Only if JDownloader is minimized to tray or taskbar" })
    String BubbleNotifyConfigPanel_BubbleNotifyConfigPanel_trayortask();

    @Default(lngs = { "en" }, values = { "Only if JDownloader is minimized to taskbar" })
    String BubbleNotifyConfigPanel_BubbleNotifyConfigPanel_taskbar();

    @Default(lngs = { "en" }, values = { "Only if JDownloader is minimized to tray" })
    String BubbleNotifyConfigPanel_BubbleNotifyConfigPanel_tray();

    @Default(lngs = { "en" }, values = { "Never" })
    String BubbleNotifyConfigPanel_BubbleNotifyConfigPanel_never();

    @Default(lngs = { "en" }, values = { "Show Bubbles if..." })
    String BubbleNotifyConfigPanel_BubbleNotifyConfigPanel_enabledstate();

    @Default(lngs = { "en" }, values = { "or" })
    String lit_or();

    @Default(lngs = { "en" }, values = { "Memory Problem detected!" })
    String MEMORY_RESTART_TITLE();

    @Default(lngs = { "en" }, values = { "It seems that there is a memory Problem. A Restart of JDownloader is required to fix this problem.\r\nIf even 2 restarts do not fix it, please visit our support chat." })
    String MEMORY_RESTART_MSG();

    @Default(lngs = { "en" }, values = { "Restart" })
    String lit_restart();

    @Default(lngs = { "en" }, values = { "Support Chat" })
    String memory_chat();

    @Default(lngs = { "en" }, values = { "Result:" })
    String ReconnectBubbleContent_onResult_result();

    @Default(lngs = { "en" }, values = { "Found Link(s)" })
    String LinkCrawlerBubbleContent_LinkCrawlerBubbleContent_foundlink();

    @Default(lngs = { "en" }, values = { "Found Package(s)" })
    String LinkCrawlerBubbleContent_LinkCrawlerBubbleContent_foundpackages();

    @Default(lngs = { "en" }, values = { "Offline" })
    String LinkCrawlerBubbleContent_LinkCrawlerBubbleContent_foundoffline();

    @Default(lngs = { "en" }, values = { "Status" })
    String LinkCrawlerBubbleContent_LinkCrawlerBubbleContent_status();

    @Default(lngs = { "en" }, values = { "Crawling..." })
    String LinkCrawlerBubbleContent_update_runnning();

    @Default(lngs = { "en" }, values = { "Done!" })
    String LinkCrawlerBubbleContent_update_finished();

    @Default(lngs = { "en" }, values = { "Onlinecheck..." })
    String LinkCrawlerBubbleContent_update_online();

    @Default(lngs = { "en" }, values = { "Online" })
    String LinkCrawlerBubbleContent_LinkCrawlerBubbleContent_foundonline();

    @Default(lngs = { "en" }, values = { "Click to open the Panel's Settings" })
    String AbstractPanelHeader_AbstractPanelHeader_settings_tt();

    @Default(lngs = { "en" }, values = { "Show Download folder" })
    String LinkgrabberPropertiesHeader_saveto();

    @Default(lngs = { "en" }, values = { "Show Filename" })
    String LinkgrabberPropertiesHeader_filename();

    @Default(lngs = { "en" }, values = { "Show Packagename" })
    String LinkgrabberPropertiesHeader_packagename();

    @Default(lngs = { "en" }, values = { "Show Download Password" })
    String LinkgrabberPropertiesHeader_downloadpassword();

    @Default(lngs = { "en" }, values = { "Show Checksum (MD5/SHA1)" })
    String LinkgrabberPropertiesHeader_checksum();

    @Default(lngs = { "en" }, values = { "Show Comment & Priority" })
    String LinkgrabberPropertiesHeader_comment_and_priority();

    @Default(lngs = { "en" }, values = { "Show Archive Information" })
    String LinkgrabberPropertiesHeader_archiveline();

    @Default(lngs = { "en" }, values = { "Show Download from" })
    String LinkgrabberPropertiesHeader_downloadfrom();

    @Default(lngs = { "en" }, values = { "Download from" })
    String propertiespanel_downloadfrom();

    @Default(lngs = { "en" }, values = { "The address behind the selected entry" })
    String AddLinksDialog_layoutDialogContent_downloadfrom_tt();

    @Default(lngs = { "en" }, values = { "Exiting File's size:" + "" })
    String IfFileExistsDialog_layoutDialogContent_filesize_existing();

    @Default(lngs = { "en" }, values = { "Enter predefined package name matcher... (Use * as Wildcard)" })
    String FilterRuleDialog_layoutDialogContent_ht_Package();

    @Default(lngs = { "en" }, values = { "Package Name" })
    String FilterRuleDialog_layoutDialogContent_lbl_Package();

    @Default(lngs = { "en" }, values = { "Package Name" })
    String ConditionDialog_getPopupMenu_Package_();

    @Default(lngs = { "en" }, values = { "Package Name Wildcard(*) #%s1" })
    String PackagizerFilterRuleDialog_createVariablesMenu_package(int i);

    @Default(lngs = { "en" }, values = { "Unknown" })
    String lit_unknown();

    @Default(lngs = { "en" }, values = { "Starting..." })
    String TaskColumn_fillColumnHelper_starting();

    @Default(lngs = { "en" }, values = { "Keep only selected Links" })
    String GenericDeleteSelectedToolbarAction_updateName_object_keep_selected_all();

    @Default(lngs = { "en" }, values = { "Delete Links: Unselected & " })
    String GenericDeleteSelectedToolbarAction_updateName_object_keep_selected_selected();

    @Default(lngs = { "en" }, values = { "Beta Release! Use for testing purposes only!" })
    String jd_gui_swing_components_AboutDialog_beta();

    @Default(lngs = { "en" }, values = { "Contained Modules:" })
    String jd_gui_swing_components_AboutDialog_mopdules();

    @Default(lngs = { "en" }, values = { "Look And Feel:" })
    String jd_gui_swing_components_AboutDialog_laf();

    @Default(lngs = { "en" }, values = { "Plugins (Supported Websites):" })
    String jd_gui_swing_components_AboutDialog_plugins();

    @Default(lngs = { "en" }, values = { "Clipboard" })
    String lit_clipboard();

    @Default(lngs = { "en" }, values = { "Copied Information to Clipboard:\r\n%s1" })
    String AboutDialog_actionPerformed_clipboard_(String s);

    @Default(lngs = { "en" }, values = { "'JDownloader' is an international Trademark of AppWork GmbH" })
    String jd_gui_swing_components_AboutDialog_trademark();

    @Default(lngs = { "en" }, values = { "Translation:" })
    String jd_gui_swing_components_AboutDialog_translations();

    @Default(lngs = { "en" }, values = { "Average time to solve:" })
    String ServicePanel9kwTooltip_runInEDT_avgsolvetime();

    @Default(lngs = { "en" }, values = { "Queues:" })
    String ServicePanel9kwTooltip_runInEDT_queue();

    @Default(lngs = { "en" }, values = { "Worker:" })
    String ServicePanel9kwTooltip_runInEDT_worker();

    @Default(lngs = { "en" }, values = { "Worker Mouse:" })
    String ServicePanel9kwTooltip_runInEDT_worker_mouse();

    @Default(lngs = { "en" }, values = { "Worker Confirm:" })
    String ServicePanel9kwTooltip_runInEDT_worker_confirm();

    @Default(lngs = { "en" }, values = { "Worker Text:" })
    String ServicePanel9kwTooltip_runInEDT_worker_text();

    @Default(lngs = { "en" }, values = { "In Work:" })
    String ServicePanel9kwTooltip_runInEDT_inwork();

    @Default(lngs = { "en" }, values = { "Tray Icon is disabled!" })
    String JDGui_tray_title();

    @Default(lngs = { "en" }, values = { "It seems that your Tray Icon has been disabled by accident.\r\nDo you want to enable it now?" })
    String JDGui_tray_msg();

    @Default(lngs = { "en" }, values = { "This condition cannot be used to trigger 'After Extraction' Actions!" })
    String PackagizerFilterRuleDialog_stateChanged_tt_disabled_archive();

    @Default(lngs = { "en" }, values = { "Condition - Trigger Conflict!" })
    String PackagizerFilterRuleDialog_layoutDialogContent_help_title();

    @Default(lngs = { "en" }, values = { "You cannot use the Source or the Origin Condition to trigger this 'After Extraction' Action" })
    String PackagizerFilterRuleDialog_layoutDialogContent_help_msg();

    @Default(lngs = { "en" }, values = { "Archive Extraction" })
    String ExtractorToolTip_ExtractorToolTip_title();

    @Default(lngs = { "en" }, values = { "Successful" })
    String lit_successfull();

    @Default(lngs = { "en" }, values = { "Open general Bubble Settings" })
    String bubblepopup_open_settings();

    @Default(lngs = { "en" }, values = { "Hide this Bubble Type permanently" })
    String bubble_hide_permanent();

    @Default(lngs = { "en" }, values = { "Are you sure that you do not want bubbles any more when %s1?\r\nYou can enable or disable bubbles at any time in the settings panel." })
    String bubble_disable_rly_msg(String label);

    @Default(lngs = { "en" }, values = { "Progress" })
    String lit_progress();

    @Default(lngs = { "en" }, values = { "Animated Icon" })
    String LinkCrawlerBubbleContent_LinkCrawlerBubbleContent_icon();

    @Default(lngs = { "en" }, values = { "Include Disabled Links" })
    String OverViewHeader_disabled();

    @Default(lngs = { "en" }, values = { "Enable the Death By Captcha Service" })
    String DeatchbyCaptcha_Service_createPanel_enabled();

    @Default(lngs = { "en" }, values = { "Report incorrect Captchas" })
    String DeatchbyCaptcha_Service_createPanel_feedback();

    @Default(lngs = { "en" }, values = { "Blacklist" })
    String DeatchbyCaptcha_Service_createPanel_blacklist();

    @Default(lngs = { "en" }, values = { "Whitelist" })
    String DeatchbyCaptcha_Service_createPanel_whitelist();

    @Default(lngs = { "en" }, values = { "Captchas solved by the deathbycaptcha.eu System" })
    String DeatchbyCaptcha_Service_getDescription_tt_();

    @Default(lngs = { "en" }, values = { "Account is banned!" })
    String ServicePanel9kwTooltip_runInEDT_banned();

    @Default(lngs = { "en" }, values = { "Account ID" })
    String lit_accountid();

    @Default(lngs = { "en" }, values = { "Rate" })
    String lit_rate();

    @Default(lngs = { "en" }, values = { "Captchas remaining" })
    String ServicePanelDBCTooltip_captcha_free();

    @Default(lngs = { "en" }, values = { "Abort Crawling" })
    String GenericDeleteFromLinkgrabberAction_cancelCrawler();

    @Default(lngs = { "en" }, values = { "Clear filtered Links" })
    String GenericDeleteFromLinkgrabberAction_clearFiltered();

    @Default(lngs = { "en" }, values = { "Clear Searchfield" })
    String GenericDeleteFromLinkgrabberAction_clearSearch();

    @Default(lngs = { "en" }, values = { "Reset Sorting" })
    String GenericDeleteFromLinkgrabberAction_resetSorter();

    @Default(lngs = { "en" }, values = { "Keep only selected Links" })
    String GenericDeleteSelectedToolbarAction_updateName_object_keep_selected();

    @Default(lngs = { "en" }, values = { "Delete Links: Unselected & " })
    String GenericDeleteSelectedToolbarAction_updateName_object_keep_unselected();

    @Default(lngs = { "en" }, values = { "Do you really want to perform this clean up action: \r\n%s1?" })
    String GenericResetLinkgrabberRlyDialog_GenericResetLinkgrabberRlyDialog_sure_(String string);

    @Default(lngs = { "en" }, values = { "Clear filtered Links" })
    String ResetLinkGrabberOptionDialog_layoutDialogContent_filtered();

    @Default(lngs = { "en" }, values = { "Tasks to do:" })
    String GenericResetLinkgrabberRlyDialog_layoutDialogContent_todo_();

    @Default(lngs = { "en" }, values = { "Continue" })
    String lit_continue();

    @Default(lngs = { "en" }, values = { "An Error occured! " })
    String FilePackageView_addLinkToTemp_downloaderror_();

    @Default(lngs = { "en" }, values = { "Icons & Artwork:" })
    String jd_gui_swing_components_AboutDialog_icons();

    @Default(lngs = { "en" }, values = { "a Remote Captcha Services are running" })
    String CESBubbleSupport_CESBubbleSupport();

    @Default(lngs = { "en" }, values = { "Remote Captcha: %s1" })
    String CESBubble_CESBubble(String string);

    @Default(lngs = { "en" }, values = { "Status:" })
    String CESBubbleContent_CESBubbleContent_status();

    @Default(lngs = { "en" }, values = { "Send Captcha to %s2 in %s1" })
    String CESBubbleContent_CESBubbleContent_wait(String string, String service);

    @Default(lngs = { "en" }, values = { "Uploading..." })
    String DeathByCaptchaSolver_solveBasicCaptchaChallenge_uploading();

    @Default(lngs = { "en" }, values = { "Solving..." })
    String DeathByCaptchaSolver_solveBasicCaptchaChallenge_solving();

    @Default(lngs = { "en" }, values = { "Result: %s1" })
    String DeathByCaptchaSolver_solveBasicCaptchaChallenge_answer(String substring);

    @Default(lngs = { "en" }, values = { "Account:" })
    String CESBubbleContent_CESBubbleContent_credits();

    @Default(lngs = { "en" }, values = { "No Answer!" })
    String DeathByCaptchaSolver_solveBasicCaptchaChallenge_failed();

    @Default(lngs = { "en" }, values = { "Expand/Collapse all Packages" })
    String CollapseExpandAllAction_CollapseExpandAllAction();

    @Default(lngs = { "en" }, values = { "Expand/Collapse All" })
    String CollapseExpandAllAction_CollapseExpandAllAction_();

    @Default(lngs = { "en" }, values = { "Expand/Collapse" })
    String CollapseExpandAllAction_CollapseExpandAllAction_selectiononly();

    @Default(lngs = { "en" }, values = { "About the Package Condition..." })
    String ConditionDialog_help_packagecondition_title();

    @Default(lngs = { "en" }, values = { "the Package Condition only matches to packagenames that could be read from the source website.\r\nIn most cases however, the packagename is generated from the filenames in the package.\r\nIn this case, this condition will not work. Please use the filename condition instead!" })
    String ConditionDialog_help_packagecondition_msg();

    @Default(lngs = { "en" }, values = { "Directory" })
    String lit_directory();

    @Default(lngs = { "en" }, values = { "Export all Menus to a Directory" })
    String ExportAllMenusAdvancedAction_actionPerformed();

    @Default(lngs = { "en" }, values = { "Import all Menus" })
    String ImportAllMenusAdvancedAction_actionPerformed();

    @Default(lngs = { "en" }, values = { "FFmpeg is missing!" })
    String FFMpegInstallTypeChooserDialog_FFMpegInstallTypeChooserDialog_title();

    @Default(lngs = { "en" }, values = { "<html>To %s1, the \"Third party\" application FFmpeg™ is required.FFmpeg™ is a leading multimedia application that can be started by JDownloader to merge video and audio streams and vice versa. Unfortunately, JDownloader could not find FFmpeg™ installed on your system.</html>" })
    String FFMpegInstallTypeChooserDialog_FFMpegInstallTypeChooserDialog_message_parameterd_2(String str);

    @Default(lngs = { "en" }, values = { "Ok, but what now?" })
    String FFMpegInstallTypeChooserDialog_layoutDialogContent_path_chooser();

    @Default(lngs = { "en" }, values = { "Step %s1:" })
    String lit_step_x(int i);

    @Default(lngs = { "en" }, values = { "Download FFmpeg™. Recommended Version: Offical \"FFmpeg static build for linux\"" })
    String FFMpegInstallTypeChooserDialog_FFMpegInstallTypeChooserDialog_message_download_linux();

    @Default(lngs = { "en" }, values = { "Download FFmpeg™. Recommended Version: Offical \"FFmpeg binary for mac\"" })
    String FFMpegInstallTypeChooserDialog_FFMpegInstallTypeChooserDialog_message_download_mac();

    @Default(lngs = { "en" }, values = { "Download FFmpeg™. Recommended Version: Offical \"FFmpeg.exe build for windows\"" })
    String FFMpegInstallTypeChooserDialog_FFMpegInstallTypeChooserDialog_message_download_windows2();

    @Default(lngs = { "en" }, values = { "Download FFmpeg™. Recommended Version: Offical \"FFmpeg build for %s1\"" })
    String FFMpegInstallTypeChooserDialog_FFMpegInstallTypeChooserDialog_message_download_others(String osString);

    @Default(lngs = { "en" }, values = { "Install it" })
    String FFMpegInstallTypeChooserDialog_FFMpegInstallTypeChooserDialog_message_install();

    @Default(lngs = { "en" }, values = { "Tell JDownloader the Installation Location" })
    String FFMpegInstallTypeChooserDialog_FFMpegInstallTypeChooserDialog_message_path();

    @Default(lngs = { "en" }, values = { "What's the matter?" })
    String FFMpegInstallTypeChooserDialog_layoutDialogContent_problem();

    @Default(lngs = { "en" }, values = { "<html>If you want JDownloader to download and install the required official FFmpeg™ Package, please click [Install FFmpeg™ now].</html>" })
    String FFMpegInstallTypeChooserDialog_FFMpegInstallTypeChooserDialog_solve2();

    @Default(lngs = { "en" }, values = { "Searching for the FFmpeg™ Binary" })
    String FFMpegInstallTypeChooserDialog_run_searching_();

    @Default(lngs = { "en" }, values = { "Please wait..." })
    String lit_please_wait_dotdotdot();

    @Default(lngs = { "en" }, values = { "Please choose the correct path to the  FFmpeg™ binary..." })
    String FFMpegInstallTypeChooserDialog_layoutDialogContent_help_();

    @Default(lngs = { "en" }, values = { "FFmpeg™ Setup" })
    String FFMpegInstallProgress_getMessage();

    @Default(lngs = { "en" }, values = { "FFmpeg™ is missing" })
    String YoutubeDash_handleFree_ffmpegmissing();

    @Default(lngs = { "en" }, values = { "Merging Streams..." })
    String FFMpegProgress_getMessage_merging_();

    @Default(lngs = { "en" }, values = { "Merging Failed." })
    String YoutubeDash_handleFree_error_();

    @Default(lngs = { "en" }, values = { "Variant" })
    String VariantColumn_VariantColumn_name_();

    @Default(lngs = { "en" }, values = { "Plugin Menu Entries" })
    String LinkgrabberPluginLink_getName_object_();

    @Default(lngs = { "en" }, values = { "Plugin Menu Entries" })
    String DownloadsTablePluginLink_getName_object_();

    @Default(lngs = { "en" }, values = { "Force Downloads' Start" })
    String ConfirmAction_ConfirmAction_context_add_and_force();

    @Default(lngs = { "en" }, values = { "Force all Downloads' Start" })
    String ConfirmAllContextmenuAction_context_add_and_force();

    @Default(lngs = { "en" }, values = { "Skip File" })
    String OnSkipDueToAlreadyExistsAction_skip_file();

    @Default(lngs = { "en" }, values = { "Skip File & mark as finished" })
    String OnSkipDueToAlreadyExistsAction_mark_successful();

    @Default(lngs = { "en" }, values = { "Skip the file" })
    String skip_file();

    @Default(lngs = { "en" }, values = { "Overwrite the file" })
    String overwrite();

    @Default(lngs = { "en" }, values = { "Auto-Rename the new File" })
    String auto_rename();

    @Default(lngs = { "en" }, values = { "Ask me" })
    String ask();

    @Default(lngs = { "en" }, values = { "merge YouTube® Audio & Video streams" })
    String YoutubeDash_handleDownload_youtube_dash();

    @Default(lngs = { "en" }, values = { "split YouTube® Audio from a Video stream" })
    String YoutubeDash_handleDownload_youtube_demux();

    @Default(lngs = { "en" }, values = { "Same choice for all affected archives" })
    String ConfirmLinksContextAction_getDontShowAgainLabelText_object_();

    @Default(lngs = { "en" }, values = { "Enable/Disable the global Speed Limiter" })
    String SpeedLimiterToggleAction_tooltip();

    @Default(lngs = { "en" }, values = { "Speedlimiter enabled" })
    String SpeedLimiterToggleAction_getNameWhenDisabled_();

    @Default(lngs = { "en" }, values = { "Information overwrites packagizer rules" })
    String AddLinksDialog_getDefaultButtonPanel_overwrite_packagizer();

    //
    @Default(lngs = { "en" }, values = { "New Survey available!" })
    String osr_label();

    @Default(lngs = { "en" }, values = { "Open Source Survey" })
    String osr_dialog_title();

    @Default(lngs = { "en" }, values = { "<html><b>JDownloader</b> has partnered with the <b>Open Source Research Group at Friedrich-Alexander University Erlangen-Nürnberg</b> to conduct a series of surveys.  The research is intended to improve the JDownloader experience and/or promote academic research into applied software development. There is no financial motivation behind this collaboration, but the whole JDownloader Project will profit from the survey results.<br>Please consider taking a moment to support this research.</html>" })
    String osr_dialog_message();

    @Default(lngs = { "en" }, values = { "Start Survey" })
    String osr_start();

    @Default(lngs = { "en" }, values = { "Copy Information" })
    String CopyGenericContextAction();

    @Default(lngs = { "en" }, values = { "Copy Information: %s1" })
    String CopyGenericContextAction_tt(String copyPattern);

    @Default(lngs = { "en" }, values = { "Status" })
    String premiumaccounttablemodel_column_status();

    @Default(lngs = { "en" }, values = { "Account is ok" })
    String PremiumAccountTableModel_getStringValue_account_ok_();

    @Default(lngs = { "en" }, values = { "Account expired" })
    String PremiumAccountTableModel_getStringValue_status_expired();

    @Default(lngs = { "en" }, values = { "Invalid logins" })
    String PremiumAccountTableModel_getStringValue_status_invalid();

    @Default(lngs = { "en" }, values = { "Plugin Error" })
    String PremiumAccountTableModel_getStringValue_status_plugin_error();

    @Default(lngs = { "en" }, values = { "Unknown Error" })
    String PremiumAccountTableModel_getStringValue_status_unknown_error();

    @Default(lngs = { "en" }, values = { "Temporarily disabled" })
    String PremiumAccountTableModel_getStringValue_temp_disabled();

    @Default(lngs = { "en" }, values = { "Temporarily disabled: %s1" })
    String PremiumAccountTableModel_getStringValue_temp_disabled2(String ret);

    @Default(lngs = { "en" }, values = { "Account is ok: %s1" })
    String PremiumAccountTableModel_getStringValue_account_ok_2(String ret);

    @Default(lngs = { "en" }, values = { "Loading information..." })
    String PremiumAccountTableModel_refresh();

    @Default(lngs = { "en" }, values = { "Import Accounts?" })
    String AccountLoader_onNewFile_title();

    @Default(lngs = { "en" }, values = { "Do you want to import these Premium Accou\r\n%s1?" })
    String AccountLoader_onNewFile_msg(String absolutePath);

    @Default(lngs = { "en" }, values = { "Could not import and new Accounts..." })
    String AccountLoader_onNewFile_noaccounts();

    @Default(lngs = { "en" }, values = { "Imported %s1 account(s)" })
    String AccountLoader_onNewFile_accounts_imported(int size);

    @Default(lngs = { "en" }, values = { "Convert CLR Script to LiveHeader" })
    String ConvertCLRAction_actionPerformed_title();

    @Default(lngs = { "en" }, values = { "Please enter the Cryptload CLR Reconnect Script..." })
    String ConvertCLRAction_actionPerformed_msg();

    @Default(lngs = { "en" }, values = { "Done - %s1!" })
    String ConvertCLRAction_actionPerformed_finished_title(String lh);

    @Default(lngs = { "en" }, values = { "Please copy & paste your script to the JDownloader Liveheader reconnect Section." })
    String ConvertCLRAction_actionPerformed_finished_msg();

    @Default(lngs = { "en" }, values = { "Adopt Checksums" })
    String ConfirmHashValuesAction();

    @Default(lngs = { "en" }, values = { "Subtitles: %s1" })
    String YoutubeDash_getName_subtitles_(String displayName);

    @Default(lngs = { "en" }, values = { "%s1" })
    String YoutubeDash_getName_subtitles_filename(String displayName);

    @Default(lngs = { "en" }, values = { "If Link contains Video & Playlist Information" })
    String YoutubeDashConfigPanel_YoutubeDashConfigPanel_if_link_contains_video_and_playlist();

    @Default(lngs = { "en" }, values = { "Explanation of the available tags:" })
    String YoutubeDashConfigPanel_YoutubeDashConfigPanel_tags();

    @Default(lngs = { "en" }, values = { "Customized Filename" })
    String YoutubeDashConfigPanel_YoutubeDashConfigPanel_filename_pattern();

    @Default(lngs = { "en" }, values = { "Filename & Packagename" })
    String YoutubeDashConfigPanel_YoutubeDashConfigPanel_filename_or_package_pattern_header();

    @Default(lngs = { "en" }, values = { "Group Variants by" })
    String YoutubeDashConfigPanel_YoutubeDashConfigPanel_grouping();

    @Default(lngs = { "en" }, values = { "Proxy" })
    String YoutubeDashConfigPanel_YoutubeDashConfigPanel_proxy_header();

    @Default(lngs = { "en" }, values = { "Enable Proxy" })
    String YoutubeDashConfigPanel_YoutubeDashConfigPanel_userproxy();

    @Default(lngs = { "en" }, values = { "Prefer HTTPS Connections" })
    String YoutubeDashConfigPanel_YoutubeDashConfigPanel_https();

    @Default(lngs = { "en" }, values = { "Quality: %s1" })
    String YoutubeDashConfigPanel_MultiVariantBox_getLabel_(String qualityExtension);

    @Default(lngs = { "en" }, values = { "Best" })
    String YoutubeDashConfigPanel_getLabel_best();

    @Default(lngs = { "en" }, values = { "Worst" })
    String YoutubeDashConfigPanel_getLabel_worst();

    @Default(lngs = { "en" }, values = { "Allowed Media Types" })
    String YoutubeDashConfigPanel_allowedtypoes();

    @Default(lngs = { "en" }, values = { "Mp4 Videos" })
    String YoutubeDashConfigPanel_allowedtypoes_mp4();

    @Default(lngs = { "en" }, values = { "Webm Videos" })
    String YoutubeDashConfigPanel_allowedtypoes_webm();

    @Default(lngs = { "en" }, values = { "Gp3 Videos" })
    String YoutubeDashConfigPanel_allowedtypoes_gp3();

    @Default(lngs = { "en" }, values = { "Flv Videos" })
    String YoutubeDashConfigPanel_allowedtypoes_flv();

    @Default(lngs = { "en" }, values = { "Stereoscopic 3D-Videos" })
    String YoutubeDashConfigPanel_allowedtypoes_3D();

    @Default(lngs = { "en" }, values = { "Audio" })
    String YoutubeDashConfigPanel_allowedtypoes_audio();

    @Default(lngs = { "en" }, values = { "Image" })
    String YoutubeDashConfigPanel_allowedtypoes_image();

    @Default(lngs = { "en" }, values = { "Subtitles" })
    String YoutubeDashConfigPanel_allowedtypoes_subtitles();

    @Default(lngs = { "en" }, values = { "Default Variants" })
    String YoutubeDashConfigPanel_links();

    @Default(lngs = { "en" }, values = { "Create a Link for the best Video Quality" })
    String YoutubeDashConfigPanel_allowedtypoes_best_video();

    @Default(lngs = { "en" }, values = { "Create a Link for the best Audio Quality" })
    String YoutubeDashConfigPanel_allowedtypoes_best_audio();

    @Default(lngs = { "en" }, values = { "Create a Link for the best 3D-Video Quality" })
    String YoutubeDashConfigPanel_allowedtypoes_best_3d();

    @Default(lngs = { "en" }, values = { "Create a Link for the best Subtitles" })
    String YoutubeDashConfigPanel_allowedtypoes_best_subtitle();

    @Default(lngs = { "en" }, values = { "<html>If you want JDownloader to create more links besides the 'Best Quality Links', you can set this up below.</html>" })
    String YoutubeDashConfigPanel_YoutubeDashConfigPanel_extra_desc();

    @Default(lngs = { "en" }, values = { "Paste Links (Deep Crawler)" })
    String PasteContextLinksAction_deep();

    @Default(lngs = { "en" }, values = { "Paste Links" })
    String PasteContextLinksAction();

    @Default(lngs = { "en" }, values = { "Add additional Link" })
    String VariantColumn_fillPopup_add();

    @Default(lngs = { "en" }, values = { "%s1 Settings" })
    String VariantColumn_fillPopup_settings(String string);

    @Default(lngs = { "en" }, values = { "Create a Link for the best Image" })
    String YoutubeDashConfigPanel_allowedtypoes_best_image();

    @Default(lngs = { "en" }, values = { "Set Variant" })
    String YoutubeDashV2_extendLinkgrabberContextMenu_context_menu();

    @Default(lngs = { "en" }, values = { "Fast Crawling" })
    String YoutubeDashConfigPanel_YoutubeDashConfigPanel_fastcrawling();

    @Default(lngs = { "en" }, values = { "<html>Fast Crawling skips the filesize check and this increases the crawling performance. If enabled, you will not see the final filesize until the actual download starts.</html>" })
    String YoutubeDashConfigPanel_YoutubeDashConfigPanel_fastcrawling_desc();

    @Default(lngs = { "en" }, values = { "Install FFmpeg™ now" })
    String ffmpeg_install_now();

    @Default(lngs = { "en" }, values = { "Finished Link(s):" })
    String DownloadOverview_DownloadOverview_finished_downloads();

    @Default(lngs = { "en" }, values = { "Skipped Link(s):" })
    String DownloadOverview_DownloadOverview_skipped_downloads();

    @Default(lngs = { "en" }, values = { "Failed Link(s):" })
    String DownloadOverview_DownloadOverview_failed_downloads();

    @Default(lngs = { "en" }, values = { "Result of the Account Verification:\r\n%s1" })
    String accountdialog_check_result(String errorMessage);

    @Default(lngs = { "en" }, values = { "Media Type: Video,3DVideo,Audio,Subtitles,..." })
    String YoutubeHelper_getDescription_group();

    @Default(lngs = { "en" }, values = { "VariantID. e.g. 'mp4_128kbit_aac'" })
    String YoutubeHelper_getDescription_variantid();

    @Default(lngs = { "en" }, values = { "Variant tag. e.g. '1080p'" })
    String YoutubeHelper_getDescription_quality();

    @Default(lngs = { "en" }, values = { "Unique Video ID" })
    String YoutubeHelper_getDescription_id();

    @Default(lngs = { "en" }, values = { "File Extension e.g. .mp4" })
    String YoutubeHelper_getDescription_extension();

    @Default(lngs = { "en" }, values = { "Age Protection true|false" })
    String YoutubeHelper_getDescription_age();

    @Default(lngs = { "en" }, values = { "Uploader Name" })
    String YoutubeHelper_getDescription_user();

    @Default(lngs = { "en" }, values = { "Channel" })
    String YoutubeHelper_getDescription_channel();

    @Default(lngs = { "en" }, values = { "Channel ID" })
    String YoutubeHelper_getDescription_channel_id();

    @Default(lngs = { "en" }, values = { "Clip duration *duration* or e.g. *duration[seconds]*" })
    String YoutubeHelper_getDescription_duration();

    @Default(lngs = { "en" }, values = { "Video Title" })
    String YoutubeHelper_getDescription_title();

    @Default(lngs = { "en" }, values = { "Publishing Date(Year,month,day). *date* or e.g. *date[YYYY/MM/DD]*" })
    String YoutubeHelper_getDescription_date();

    @Default(lngs = { "en" }, values = { "Accurate publishing Date (incl. hours, minutes). *date_time* or e.g. *date_time[YYYY/MM/DD hh:mm]*" })
    String YoutubeHelper_getDescription_date_accurate();

    @Default(lngs = { "en" }, values = { "Accurate updated publishing Date (incl. hours, minutes). *date_time* or e.g. *date_time[YYYY/MM/DD hh:mm]*" })
    String YoutubeHelper_getDescription_updatedate_accurate();

    @Default(lngs = { "en" }, values = { "Playlist Number. *videonumber* or e.g. *videonumber[000]*" })
    String YoutubeHelper_getDescription_videonumber();

    @Default(lngs = { "en" }, values = { "Customized Packagename" })
    String YoutubeDashConfigPanel_YoutubeDashConfigPanel_package_pattern();

    @Default(lngs = { "en" }, values = { "Audio" })
    String YoutubeVariantInterface_getLabel_audio();

    @Default(lngs = { "en" }, values = { "Video" })
    String YoutubeVariantInterface_getLabel_video();

    @Default(lngs = { "en" }, values = { "3D-Video" })
    String YoutubeVariantInterface_getLabel_video3d();

    @Default(lngs = { "en" }, values = { "Images" })
    String YoutubeVariantInterface_getLabel_image();

    @Default(lngs = { "en" }, values = { "Subtitles" })
    String YoutubeVariantInterface_getLabel_subtitles();

    @Default(lngs = { "en" }, values = { "Add additional Variant" })
    String YoutubeDashV2_extendLinkgrabberContextMenu_context_menu_add();

    @Default(lngs = { "en" }, values = { "Best %s1 available" })
    String YoutubeDashV2_add_best(String label);

    @Default(lngs = { "en" }, values = { "Worst %s1 available" })
    String YoutubeDashV2_add_worst(String label);

    @Default(lngs = { "en" }, values = { "%s1 Server Error. Try again later" })
    String hoster_servererror(String string);

    @Default(lngs = { "en" }, values = { "Password required" })
    String AskForPasswordDialog_AskForPasswordDialog_title_();

    @Default(lngs = { "en" }, values = { "User Google+ ID" })
    String YoutubeHelper_getDescription_googleplus_id();

    @Default(lngs = { "en" }, values = { "Ask me" })
    String GraphicalUserInterfaceSettings_getLabel_ask_();

    @Default(lngs = { "en" }, values = { "Do not move - keep in Linkgrabber" })
    String GraphicalUserInterfaceSettings_getLabel_do_not_move();

    @Default(lngs = { "en" }, values = { "Move to Downloadlist anyway" })
    String GraphicalUserInterfaceSettings_getLabel_move_anyway();

    @Default(lngs = { "en" }, values = { "Do not move - Delete Links" })
    String GraphicalUserInterfaceSettings_getLabel_delete();

    @Default(lngs = { "en" }, values = { "Address" })
    String lit_url();

    @Default(lngs = { "en" }, values = { "Plugin" })
    String lit_plugin();

    @Default(lngs = { "en" }, values = { "SubRip Subtitle File" })
    String YoutubeVariant_name_SUBTITLES();

    @Default(lngs = { "en" }, values = { "Best Quality Image" })
    String YoutubeVariant_name_IMAGE_MAX();

    @Default(lngs = { "en" }, values = { "High Quality Image" })
    String YoutubeVariant_name_IMAGE_HQ();

    @Default(lngs = { "en" }, values = { "Medium Quality Image" })
    String YoutubeVariant_name_IMAGE_MQ();

    @Default(lngs = { "en" }, values = { "Low Quality Image" })
    String YoutubeVariant_name_IMAGE_LQ();

    @Default(lngs = { "en" }, values = { "128kbit/s M4A-Audio" })
    String YoutubeVariant_name_M4A_128();

    @Default(lngs = { "en" }, values = { "256kbit/s M4A-Audio" })
    String YoutubeVariant_name_M4A_256();

    @Default(lngs = { "en" }, values = { "48kbit/s M4A-Audio" })
    String YoutubeVariant_name_M4A_48();

    @Default(lngs = { "en" }, values = { "128kbit/s AAC-Audio" })
    String YoutubeVariant_name_AAC_128();

    @Default(lngs = { "en" }, values = { "256kbit/s AAC-Audio" })
    String YoutubeVariant_name_AAC_256();

    @Default(lngs = { "en" }, values = { "48kbit/s AAC-Audio" })
    String YoutubeVariant_name_AAC_48();

    @Default(lngs = { "en" }, values = { "270p FLV-Video(high)" })
    String YoutubeVariant_name_FLV_270_HIGH();

    @Default(lngs = { "en" }, values = { "240p FLV-Video(low)" })
    String YoutubeVariant_name_FLV_240_LOW();

    @Default(lngs = { "en" }, values = { "360p FLV-Video" })
    String YoutubeVariant_name_FLV_360();

    @Default(lngs = { "en" }, values = { "480p FLV-Video" })
    String YoutubeVariant_name_FLV_480();

    @Default(lngs = { "en" }, values = { "64kbit/s Mp3-Audio" })
    String YoutubeVariant_name_MP3_1();

    @Default(lngs = { "en" }, values = { "64kbit/s Mp3-Audio" })
    String YoutubeVariant_name_MP3_2();

    @Default(lngs = { "en" }, values = { "1080p MP4-Video" })
    String YoutubeVariant_name_MP4_1080();

    @Default(lngs = { "en" }, values = { "360p MP4-Video" })
    String YoutubeVariant_name_MP4_360();

    @Default(lngs = { "en" }, values = { "240p MP4-3D-Video" })
    String YoutubeVariant_name_MP4_3D_240();

    @Default(lngs = { "en" }, values = { "360p MP4-3D-Video" })
    String YoutubeVariant_name_MP4_3D_360();

    @Default(lngs = { "en" }, values = { "1080p MP4-3D-Video" })
    String YoutubeVariant_name_MP4_3D_1080();

    @Default(lngs = { "en" }, values = { "720p MP4-3D-Video" })
    String YoutubeVariant_name_MP4_3D_720();

    @Default(lngs = { "en" }, values = { "720p MP4-Video" })
    String YoutubeVariant_name_MP4_720();

    @Default(lngs = { "en" }, values = { "1080p MP4-Video" })
    String YoutubeVariant_name_MP4_DASH_1080_AAC128();

    @Default(lngs = { "en" }, values = { "1080p MP4-Video" })
    String YoutubeVariant_name_MP4_DASH_1080_AAC256();

    @Default(lngs = { "en" }, values = { "1080p MP4-Video" })
    String YoutubeVariant_name_MP4_DASH_1080_AAC48();

    @Default(lngs = { "en" }, values = { "144p MP4-Video" })
    String YoutubeVariant_name_MP4_DASH_144_AAC128();

    @Default(lngs = { "en" }, values = { "144p MP4-Video" })
    String YoutubeVariant_name_MP4_DASH_144_AAC256();

    @Default(lngs = { "en" }, values = { "144p MP4-Video" })
    String YoutubeVariant_name_MP4_DASH_144_AAC48();

    @Default(lngs = { "en" }, values = { "1440p MP4-Video" })
    String YoutubeVariant_name_MP4_DASH_1440_AAC128();

    @Default(lngs = { "en" }, values = { "1440p MP4-Video" })
    String YoutubeVariant_name_MP4_DASH_1440_AAC256();

    @Default(lngs = { "en" }, values = { "1440p MP4-Video" })
    String YoutubeVariant_name_MP4_DASH_1440_AAC48();

    @Default(lngs = { "en" }, values = { "240p MP4-Video" })
    String YoutubeVariant_name_MP4_DASH_240_AAC128();

    @Default(lngs = { "en" }, values = { "240p MP4-Video" })
    String YoutubeVariant_name_MP4_DASH_240_AAC256();

    @Default(lngs = { "en" }, values = { "240p MP4-Video" })
    String YoutubeVariant_name_MP4_DASH_240_AAC48();

    @Default(lngs = { "en" }, values = { "360p MP4-Video" })
    String YoutubeVariant_name_MP4_DASH_360_AAC128();

    @Default(lngs = { "en" }, values = { "360p MP4-Video" })
    String YoutubeVariant_name_MP4_DASH_360_AAC256();

    @Default(lngs = { "en" }, values = { "360p MP4-Video" })
    String YoutubeVariant_name_MP4_DASH_360_AAC48();

    @Default(lngs = { "en" }, values = { "480p MP4-Video" })
    String YoutubeVariant_name_MP4_DASH_480_AAC128();

    @Default(lngs = { "en" }, values = { "480p MP4-Video" })
    String YoutubeVariant_name_MP4_DASH_480_AAC256();

    @Default(lngs = { "en" }, values = { "480p MP4-Video" })
    String YoutubeVariant_name_MP4_DASH_480_AAC48();

    @Default(lngs = { "en" }, values = { "720p MP4-Video" })
    String YoutubeVariant_name_MP4_DASH_720_AAC128();

    @Default(lngs = { "en" }, values = { "720p MP4-Video" })
    String YoutubeVariant_name_MP4_DASH_720_AAC256();

    @Default(lngs = { "en" }, values = { "720p MP4-Video" })
    String YoutubeVariant_name_MP4_DASH_720_AAC48();

    @Default(lngs = { "en" }, values = { "2160p MP4-Video" })
    String YoutubeVariant_name_MP4_DASH_ORIGINAL_AAC128();

    @Default(lngs = { "en" }, values = { "2160p MP4-Video" })
    String YoutubeVariant_name_MP4_DASH_ORIGINAL_AAC256();

    @Default(lngs = { "en" }, values = { "2160p MP4-Video" })
    String YoutubeVariant_name_MP4_DASH_ORIGINAL_AAC48();

    @Default(lngs = { "en" }, values = { "2160p MP4-Video" })
    String YoutubeVariant_name_MP4_ORIGINAL();

    @Default(lngs = { "en" }, values = { "144p 3GP Video" })
    String YoutubeVariant_name_THREEGP_144();

    @Default(lngs = { "en" }, values = { "240p 3GP Video(high)" })
    String YoutubeVariant_name_THREEGP_240_HIGH();

    @Default(lngs = { "en" }, values = { "240p 3GP Video(low)" })
    String YoutubeVariant_name_THREEGP_240_LOW();

    @Default(lngs = { "en" }, values = { "1080p WebM-Video" })
    String YoutubeVariant_name_WEBM_1080();

    @Default(lngs = { "en" }, values = { "360p WebM-Video" })
    String YoutubeVariant_name_WEBM_360();

    @Default(lngs = { "en" }, values = { "360p WebM-3D-Video(128Kbit/s Audio)" })
    String YoutubeVariant_name_WEBM_3D_360_128();

    @Default(lngs = { "en" }, values = { "360p WebM-3D-Video(192Kbit/s Audio)" })
    String YoutubeVariant_name_WEBM_3D_360_192();

    @Default(lngs = { "en" }, values = { "720p WebM-3D-Video" })
    String YoutubeVariant_name_WEBM_3D_720();

    @Default(lngs = { "en" }, values = { "480p WebM-Video" })
    String YoutubeVariant_name_WEBM_480();

    @Default(lngs = { "en" }, values = { "720p WebM-Video" })
    String YoutubeVariant_name_WEBM_720();

    @Default(lngs = { "en" }, values = { "Subtitles" })
    String YoutubeVariant_filenametag_SUBTITLES();

    @Default(lngs = { "en" }, values = { "BQ" })
    String YoutubeVariant_filenametag_IMAGE_MAX();

    @Default(lngs = { "en" }, values = { "HQ" })
    String YoutubeVariant_filenametag_IMAGE_HQ();

    @Default(lngs = { "en" }, values = { "MQ" })
    String YoutubeVariant_filenametag_IMAGE_MQ();

    @Default(lngs = { "en" }, values = { "LQ" })
    String YoutubeVariant_filenametag_IMAGE_LQ();

    @Default(lngs = { "en" }, values = { "128kbit" })
    String YoutubeVariant_filenametag_M4A_128();

    @Default(lngs = { "en" }, values = { "256kbit" })
    String YoutubeVariant_filenametag_M4A_256();

    @Default(lngs = { "en" }, values = { "48kbit" })
    String YoutubeVariant_filenametag_M4A_48();

    @Default(lngs = { "en" }, values = { "128kbit" })
    String YoutubeVariant_filenametag_AAC_128();

    @Default(lngs = { "en" }, values = { "256kbit" })
    String YoutubeVariant_filenametag_AAC_256();

    @Default(lngs = { "en" }, values = { "48kbit" })
    String YoutubeVariant_filenametag_AAC_48();

    @Default(lngs = { "en" }, values = { "270p[HQ]" })
    String YoutubeVariant_filenametag_FLV_270_HIGH();

    @Default(lngs = { "en" }, values = { "240p[LQ]" })
    String YoutubeVariant_filenametag_FLV_240_LOW();

    @Default(lngs = { "en" }, values = { "360p" })
    String YoutubeVariant_filenametag_FLV_360();

    @Default(lngs = { "en" }, values = { "480p" })
    String YoutubeVariant_filenametag_FLV_480();

    @Default(lngs = { "en" }, values = { "64kbit" })
    String YoutubeVariant_filenametag_MP3_1();

    @Default(lngs = { "en" }, values = { "64kbit" })
    String YoutubeVariant_filenametag_MP3_2();

    @Default(lngs = { "en" }, values = { "1080p" })
    String YoutubeVariant_filenametag_MP4_1080();

    @Default(lngs = { "en" }, values = { "360p" })
    String YoutubeVariant_filenametag_MP4_360();

    @Default(lngs = { "en" }, values = { "240p 3D" })
    String YoutubeVariant_filenametag_MP4_3D_240();

    @Default(lngs = { "en" }, values = { "360p 3D" })
    String YoutubeVariant_filenametag_MP4_3D_360();

    @Default(lngs = { "en" }, values = { "1080p 3D" })
    String YoutubeVariant_filenametag_MP4_3D_1080();

    @Default(lngs = { "en" }, values = { "720p 3D" })
    String YoutubeVariant_filenametag_MP4_3D_720();

    @Default(lngs = { "en" }, values = { "720p" })
    String YoutubeVariant_filenametag_MP4_720();

    @Default(lngs = { "en" }, values = { "1080p" })
    String YoutubeVariant_filenametag_MP4_DASH_1080_AAC128();

    @Default(lngs = { "en" }, values = { "1080p" })
    String YoutubeVariant_filenametag_MP4_DASH_1080_AAC256();

    @Default(lngs = { "en" }, values = { "1080p" })
    String YoutubeVariant_filenametag_MP4_DASH_1080_AAC48();

    @Default(lngs = { "en" }, values = { "144p" })
    String YoutubeVariant_filenametag_MP4_DASH_144_AAC128();

    @Default(lngs = { "en" }, values = { "144p" })
    String YoutubeVariant_filenametag_MP4_DASH_144_AAC256();

    @Default(lngs = { "en" }, values = { "144p" })
    String YoutubeVariant_filenametag_MP4_DASH_144_AAC48();

    @Default(lngs = { "en" }, values = { "1440p" })
    String YoutubeVariant_filenametag_MP4_DASH_1440_AAC128();

    @Default(lngs = { "en" }, values = { "1440p" })
    String YoutubeVariant_filenametag_MP4_DASH_1440_AAC256();

    @Default(lngs = { "en" }, values = { "1440p" })
    String YoutubeVariant_filenametag_MP4_DASH_1440_AAC48();

    @Default(lngs = { "en" }, values = { "240p" })
    String YoutubeVariant_filenametag_MP4_DASH_240_AAC128();

    @Default(lngs = { "en" }, values = { "240p" })
    String YoutubeVariant_filenametag_MP4_DASH_240_AAC256();

    @Default(lngs = { "en" }, values = { "240p" })
    String YoutubeVariant_filenametag_MP4_DASH_240_AAC48();

    @Default(lngs = { "en" }, values = { "360p" })
    String YoutubeVariant_filenametag_MP4_DASH_360_AAC128();

    @Default(lngs = { "en" }, values = { "360p" })
    String YoutubeVariant_filenametag_MP4_DASH_360_AAC256();

    @Default(lngs = { "en" }, values = { "360p" })
    String YoutubeVariant_filenametag_MP4_DASH_360_AAC48();

    @Default(lngs = { "en" }, values = { "480p" })
    String YoutubeVariant_filenametag_MP4_DASH_480_AAC128();

    @Default(lngs = { "en" }, values = { "480p" })
    String YoutubeVariant_filenametag_MP4_DASH_480_AAC256();

    @Default(lngs = { "en" }, values = { "480p" })
    String YoutubeVariant_filenametag_MP4_DASH_480_AAC48();

    @Default(lngs = { "en" }, values = { "720p" })
    String YoutubeVariant_filenametag_MP4_DASH_720_AAC128();

    @Default(lngs = { "en" }, values = { "720p" })
    String YoutubeVariant_filenametag_MP4_DASH_720_AAC256();

    @Default(lngs = { "en" }, values = { "720p" })
    String YoutubeVariant_filenametag_MP4_DASH_720_AAC48();

    @Default(lngs = { "en" }, values = { "2160p" })
    String YoutubeVariant_filenametag_MP4_DASH_ORIGINAL_AAC128();

    @Default(lngs = { "en" }, values = { "2160p" })
    String YoutubeVariant_filenametag_MP4_DASH_ORIGINAL_AAC256();

    @Default(lngs = { "en" }, values = { "2160p" })
    String YoutubeVariant_filenametag_MP4_DASH_ORIGINAL_AAC48();

    @Default(lngs = { "en" }, values = { "2160p" })
    String YoutubeVariant_filenametag_MP4_ORIGINAL();

    @Default(lngs = { "en" }, values = { "144p" })
    String YoutubeVariant_filenametag_THREEGP_144();

    @Default(lngs = { "en" }, values = { "240p[HQ]" })
    String YoutubeVariant_filenametag_THREEGP_240_HIGH();

    @Default(lngs = { "en" }, values = { "240p[LQ]" })
    String YoutubeVariant_filenametag_THREEGP_240_LOW();

    @Default(lngs = { "en" }, values = { "1080p" })
    String YoutubeVariant_filenametag_WEBM_1080();

    @Default(lngs = { "en" }, values = { "360p" })
    String YoutubeVariant_filenametag_WEBM_360();

    @Default(lngs = { "en" }, values = { "360p 3D [128kbit Audio]" })
    String YoutubeVariant_filenametag_WEBM_3D_360_128();

    @Default(lngs = { "en" }, values = { "360p 3D [192kbit Audio]" })
    String YoutubeVariant_filenametag_WEBM_3D_360_192();

    @Default(lngs = { "en" }, values = { "720p 3D" })
    String YoutubeVariant_filenametag_WEBM_3D_720();

    @Default(lngs = { "en" }, values = { "480p" })
    String YoutubeVariant_filenametag_WEBM_480();

    @Default(lngs = { "en" }, values = { "720p" })
    String YoutubeVariant_filenametag_WEBM_720();

    @Default(lngs = { "en" }, values = { "Video Files" })
    String YoutubeDashConfigPanel_getType_videos();

    @Default(lngs = { "en" }, values = { "Video File" })
    String YoutubeDashConfigPanel_getType_video();

    @Default(lngs = { "en" }, values = { "Audio Files" })
    String YoutubeDashConfigPanel_getType_audios();

    @Default(lngs = { "en" }, values = { "Audio File" })
    String YoutubeDashConfigPanel_getType_audio();

    @Default(lngs = { "en" }, values = { "Image Files" })
    String YoutubeDashConfigPanel_getType_images();

    @Default(lngs = { "en" }, values = { "Image File" })
    String YoutubeDashConfigPanel_getType_image();

    @Default(lngs = { "en" }, values = { "%s1 Free Download" })
    String PluginForHost_showFreeDialog_title(String domain);

    @Default(lngs = { "en" }, values = { "You are using the %s1 Free Mode.\r\nJDownloader always tries to get the best out of each hoster's free mode!\r\n\r\n                           However, if you want to download multiple files\r\n                        - possibly at fullspeed and without any wait times - \r\n                        you really should have a look at the Premium Mode.\r\n\r\nIn our experience, Premium is well worth the money. Decide for yourself, though. Let's give it a try?" })
    String PluginForHost_showFreeDialog_message(String domain);

    @Default(lngs = { "en" }, values = { "192kbit/s M4A-Audio" })
    String YoutubeVariant_name_M4A_192();

    @Default(lngs = { "en" }, values = { "192kbit" })
    String YoutubeVariant_filenametag_M4A_192();

    @Default(lngs = { "en" }, values = { "192kbit/s AAC-Audio" })
    String YoutubeVariant_name_AAC_192();

    @Default(lngs = { "en" }, values = { "192kbit" })
    String YoutubeVariant_filenametag_AAC_192();

    @Default(lngs = { "en" }, values = { "96kbit/s M4A-Audio" })
    String YoutubeVariant_name_M4A_96();

    @Default(lngs = { "en" }, values = { "96kbit" })
    String YoutubeVariant_filenametag_M4A_96();

    @Default(lngs = { "en" }, values = { "96kbit/s AAC-Audio" })
    String YoutubeVariant_name_AAC_96();

    @Default(lngs = { "en" }, values = { "96kbit" })
    String YoutubeVariant_filenametag_AAC_96();

    @Default(lngs = { "en" }, values = { "152kbit/s AAC-Audio" })
    String YoutubeVariant_name_AAC_152();

    @Default(lngs = { "en" }, values = { "152kbit" })
    String YoutubeVariant_filenametag_AAC_152();

    @Default(lngs = { "en" }, values = { "152kbit/s M4A-Audio" })
    String YoutubeVariant_name_M4A_152();

    @Default(lngs = { "en" }, values = { "152kbit" })
    String YoutubeVariant_filenametag_M4A_152();

    @Default(lngs = { "en" }, values = { "We need your help!" })
    String UploadSessionLogDialog_UploadSessionLogDialog_object_title2();

    @Default(lngs = { "en" }, values = { "A unknown problem occured while downloading a file. To solve this problem, we need more details about the reason. JDownloader is able to collect all required informations about the bug automatically. Please help us to fix that problem as soon as possible by sending these informations." })
    String UploadSessionLogDialog_UploadSessionLogDialog_object_msg2();

    @Default(lngs = { "en" }, values = { "I want to help! Continue." })
    String UploadSessionLogDialog_UploadSessionLogDialog_yes();

    @Default(lngs = { "en" }, values = { "(more...)" })
    String UploadSessionLogDialog_UploadSessionLogDialog_object_more();

    @Default(lngs = { "en" }, values = { "Video Codec (e.g. H264)" })
    String YoutubeHelper_getDescription_videoCodec();

    @Default(lngs = { "en" }, values = { "Resolution  (e.g. 1080p,1440x1080,...)" })
    String YoutubeHelper_getDescription_resolution();

    @Default(lngs = { "en" }, values = { "Audio Codec (e.g. Mp3,AAC,...)" })
    String YoutubeHelper_getDescription_audioCodec();

    @Default(lngs = { "en" }, values = { "Audio Bitrate  (e.g. 128kbit)" })
    String YoutubeHelper_getDescription_audioQuality();

}