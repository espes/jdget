package org.jdownloader.gui.translate;

import java.net.URL;

import org.appwork.txtresource.Default;
import org.appwork.txtresource.Defaults;
import org.appwork.txtresource.Description;
import org.appwork.txtresource.TranslateInterface;
import org.appwork.utils.net.httpconnection.HTTPProxy;

@Defaults(lngs = { "en" })
public interface GuiTranslation extends TranslateInterface {
    @Default(lngs = { "en" }, values = { "About JDownloader" })
    String action_aboutaction();

    @Default(lngs = { "en" }, values = { "-" })
    String action_aboutaction_accelerator();

    @Default(lngs = { "en" }, values = { "-" })
    String action_aboutaction_mnemonics();

    @Default(lngs = { "en" }, values = { "About JDownloader" })
    String action_aboutaction_tooltip();

    @Default(lngs = { "en" }, values = { "Add Premium Account" })
    String action_add_premium_account();

    @Default(lngs = { "en" }, values = { "-" })
    String action_add_premium_account_accelerator();

    @Default(lngs = { "en" }, values = { "-" })
    String action_add_premium_account_mnemonic();

    @Default(lngs = { "en" }, values = { "Add a new Premium Account" })
    String action_add_premium_account_tooltip();

    @Default(lngs = { "en" }, values = { "Add Container" })
    String action_addcontainer();

    @Default(lngs = { "en" }, values = { "-" })
    String action_addcontainer_accelerator();

    @Default(lngs = { "en" }, values = { "-" })
    String action_addcontainer_mnemonic();

    String action_addcontainer_tooltip();

    @Default(lngs = { "en" }, values = { "Add link" })
    String action_addurl();

    String action_addurl_accelerator();

    String action_addurl_mnemonic();

    String action_addurl_tooltip();

    @Default(lngs = { "en" }, values = { "Backup" })
    String action_backup();

    String action_backup_accelerator();

    String action_backup_mnemonic();

    String action_backup_tooltip();

    @Default(lngs = { "en" }, values = { "Backup Linklist" })
    String action_backuplinklist();

    String action_backuplinklist_accelerator();

    String action_backuplinklist_mnemonic();

    String action_backuplinklist_tooltip();

    @Default(lngs = { "en" }, values = { "Buy Premium Account" })
    String action_buy_premium_account();

    @Default(lngs = { "en" }, values = { "-" })
    String action_buy_premium_account_accelerator();

    @Default(lngs = { "en" }, values = { "-" })
    String action_buy_premium_account_mnemonic();

    @Default(lngs = { "en" }, values = { "Buy a new Premium Account" })
    String action_buy_premium_account_tooltip();

    @Default(lngs = { "en" }, values = { "Changelog" })
    String action_changelog();

    String action_changelog_accelerator();

    String action_changelog_mnemonic();

    String action_changelog_tooltip();

    // actionm_action_//
    @Default(lngs = { "en" }, values = { "Clear" })
    String action_clearlinkgrabber();

    @Default(lngs = { "en" }, values = { "-" })
    String action_clearlinkgrabber_accelerator();

    @Default(lngs = { "en" }, values = { "-" })
    String action_clearlinkgrabber_mnemonic();

    @Default(lngs = { "en" }, values = { "Clear Linkgrabber" })
    String action_clearlinkgrabber_tooltip();

    @Default(lngs = { "en" }, values = { "Enable/Disable Clipboard Observer" })
    String action_clipboard_observer();

    @Default(lngs = { "en" }, values = { "ctrl+c" })
    String action_clipboard_observer_accelerator();

    @Default(lngs = { "en" }, values = { "c" })
    String action_clipboard_observer_mnemonics();

    @Default(lngs = { "en" }, values = { "Enable or Disable Clipboard Observer." })
    String action_clipboard_observer_tooltip();

    @Default(lngs = { "en" }, values = { "Exit" })
    String action_exit();

    String action_exit_accelerator();

    String action_exit_mnemonic();

    String action_exit_tooltip();

    @Default(lngs = { "en" }, values = { "Help" })
    String action_help();

    String action_help_accelerator();

    String action_help_mnemonic();

    String action_help_tooltip();

    // action_//
    @Default(lngs = { "en" }, values = { "Add all" })
    String action_linkgrabber_addall();

    @Default(lngs = { "en" }, values = { "-" })
    String action_linkgrabber_addall_accelerator();

    @Default(lngs = { "en" }, values = { "-" })
    String action_linkgrabber_addall_mnemonic();

    @Default(lngs = { "en" }, values = { "Add all" })
    String action_linkgrabber_addall_tooltip();

    @Default(lngs = { "en" }, values = { "Log" })
    String action_log();

    @Default(lngs = { "en" }, values = { "-" })
    String action_log_accelerator();

    @Default(lngs = { "en" }, values = { "-" })
    String action_log_mnemonic();

    @Default(lngs = { "en" }, values = { "Show Log" })
    String action_log_tooltip();

    @Default(lngs = { "en" }, values = { "Move down" })
    String action_move_down();

    @Default(lngs = { "en" }, values = { "ctrl+DOWN" })
    String action_move_down_accelerator();

    @Default(lngs = { "en" }, values = { "-" })
    String action_move_down_mnemonic();

    @Default(lngs = { "en" }, values = { "Move selected links down" })
    String action_move_down_tooltip();

    @Default(lngs = { "en" }, values = { "Move to Bottom" })
    String action_move_to_bottom();

    @Default(lngs = { "en" }, values = { "ctrl+shift+DOWN" })
    String action_move_to_bottom_accelerator();

    @Default(lngs = { "en" }, values = { "-" })
    String action_move_to_bottom_mnemonics();

    @Default(lngs = { "en" }, values = { "Move selected links to bottom" })
    String action_move_to_bottom_tooltip();

    @Default(lngs = { "en" }, values = { "Move to Top" })
    String action_move_to_top();

    @Default(lngs = { "en" }, values = { "ctrl+shift+UP" })
    String action_move_to_top_accelerator();

    @Default(lngs = { "en" }, values = { "-" })
    String action_move_to_top_mnemonic();

    @Default(lngs = { "en" }, values = { "Move selected Links to Top" })
    String action_move_to_top_tooltip();

    @Default(lngs = { "en" }, values = { "Move up" })
    String action_move_up();

    @Default(lngs = { "en" }, values = { "ctrl+UP" })
    String action_move_up_accelerator();

    @Default(lngs = { "en" }, values = { "-" })
    String action_move_up_mnemonic();

    @Default(lngs = { "en" }, values = { "Move selected links up" })
    String action_move_up_tooltip();

    @Default(lngs = { "en" }, values = { "Open Download Directory" })
    String action_open_dlfolder();

    @Default(lngs = { "en" }, values = { "-" })
    String action_open_dlfolder_accelerator();

    @Default(lngs = { "en" }, values = { "-" })
    String action_open_dlfolder_mnemonic();

    @Default(lngs = { "en" }, values = { "Open the default download destination" })
    String action_open_dlfolder_tooltip();

    @Default(lngs = { "en" }, values = { "Password Manager" })
    String action_passwordlist();

    @Default(lngs = { "en" }, values = { "-" })
    String action_passwordlist_accelerator();

    @Default(lngs = { "en" }, values = { "-" })
    String action_passwordlist_mnemonics();

    @Default(lngs = { "en" }, values = { "Open Archive Passwordmanager" })
    String action_passwordlist_tooltip();

    @Default(lngs = { "en" }, values = { "Pause Downloads" })
    String action_pause();

    @Default(lngs = { "en" }, values = { "ctrl+b" })
    String action_pause_accelerator();

    @Default(lngs = { "en" }, values = { "b" })
    String action_pause_mnemonic();

    @Default(lngs = { "en" }, values = { "Pause Downloads. This sets global speed limit to 10 kb/s" })
    String action_pause_tooltip();

    // String action_//
    @Default(lngs = { "en" }, values = { "Plugin Accounts" })
    String action_plugin_accounts();

    @Default(lngs = { "en" }, values = { "-" })
    String action_plugin_accounts_accelerator();

    @Default(lngs = { "en" }, values = { "-" })
    String action_plugin_accounts_mnemonic();

    @Default(lngs = { "en" }, values = { "Plugin Accounts" })
    String action_plugin_accounts_tooltip();

    // String action_//
    @Default(lngs = { "en" }, values = { "Plugin Settings" })
    String action_plugin_config();

    @Default(lngs = { "en" }, values = { "-" })
    String action_plugin_config_accelerator();

    @Default(lngs = { "en" }, values = { "-" })
    String action_plugin_config_mnemonic();

    @Default(lngs = { "en" }, values = { "tooltip" })
    String action_plugin_config_tooltip();

    // String action_//
    @Default(lngs = { "en" }, values = { "Enable/Disable Premium" })
    String action_plugin_enable_premium();

    @Default(lngs = { "en" }, values = { "-" })
    String action_plugin_enable_premium_accelerator();

    @Default(lngs = { "en" }, values = { "-" })
    String action_plugin_enable_premium_mnemonic();

    @Default(lngs = { "en" }, values = { "tooltip" })
    String action_plugin_enable_premium_tooltip();

    @Default(lngs = { "en" }, values = { "Buy Account" })
    String action_plugin_premium_buyAccount();

    @Default(lngs = { "en" }, values = { "-" })
    String action_plugin_premium_buyAccount_accelerator();

    @Default(lngs = { "en" }, values = { "-" })
    String action_plugin_premium_buyAccount_mnemonic();

    @Default(lngs = { "en" }, values = { "tooltip" })
    String action_plugin_premium_buyAccount_tooltip();

    // String action_//
    @Default(lngs = { "en" }, values = { "Premium Info" })
    String action_plugin_premium_info();

    @Default(lngs = { "en" }, values = { "-" })
    String action_plugin_premium_info_accelerator();

    @Default(lngs = { "en" }, values = { "-" })
    String action_plugin_premium_info_mnemonic();

    @Default(lngs = { "en" }, values = { "tooltip" })
    String action_plugin_premium_info_tooltip();

    // String action_//
    @Default(lngs = { "en" }, values = { "No Accounts" })
    String action_plugin_premium_noAccounts();

    @Default(lngs = { "en" }, values = { "-" })
    String action_plugin_premium_noAccounts_accelerator();

    @Default(lngs = { "en" }, values = { "-" })
    String action_plugin_premium_noAccounts_mnemonic();

    @Default(lngs = { "en" }, values = { "tooltip" })
    String action_plugin_premium_noAccounts_tooltip();

    @Default(lngs = { "en" }, values = { "Premium Manager" })
    String action_premium_manager();

    @Default(lngs = { "en" }, values = { "ctrl+a" })
    String action_premium_manager_accelerator();

    @Default(lngs = { "en" }, values = { "a" })
    String action_premium_manager_mnemonic();

    @Default(lngs = { "en" }, values = { "Show Premium Manager" })
    String action_premium_manager_tooltip();

    // action_//
    @Default(lngs = { "en" }, values = { "Refresh Accounts" })
    String action_premium_refresh();

    @Default(lngs = { "en" }, values = { "-" })
    String action_premium_refresh_accelerator();

    @Default(lngs = { "en" }, values = { "-" })
    String action_premium_refresh_mnemonic();

    @Default(lngs = { "en" }, values = { "tooltip" })
    String action_premium_refresh_tooltip();

    // action_//
    @Default(lngs = { "en" }, values = { "Remove Accounts" })
    String action_premium_remove_account();

    @Default(lngs = { "en" }, values = { "-" })
    String action_premium_remove_account_accelerator();

    @Default(lngs = { "en" }, values = { "-" })
    String action_premium_remove_account_mnemonic();

    @Default(lngs = { "en" }, values = { "tooltip" })
    String action_premium_remove_account_tooltip();

    @Default(lngs = { "en" }, values = { "Enable/Disable Premium" })
    String action_premium_toggle();

    @Default(lngs = { "en" }, values = { "-" })
    String action_premium_toggle_accelerator();

    @Default(lngs = { "en" }, values = { "-" })
    String action_premium_toggle_mnemonic();

    @Default(lngs = { "en" }, values = { "Enable or disable all Premium Accounts" })
    String action_premium_toggle_tooltip();

    @Default(lngs = { "en" }, values = { "%s1 Account(s)" })
    String action_premiumview_removeacc_accs(Object s1);

    @Default(lngs = { "en" }, values = { "Remove selected?" })
    String action_premiumview_removeacc_ask();

    @Default(lngs = { "en" }, values = { "Refresh IP" })
    String action_reconnect_invoke();

    @Default(lngs = { "en" }, values = { "ctrl+i" })
    String action_reconnect_invoke_accelerator();

    @Default(lngs = { "en" }, values = { "i" })
    String action_reconnect_invoke_mnemonic();

    @Default(lngs = { "en" }, values = { "Perform a Reconnect, to get a new dynamic IP" })
    String action_reconnect_invoke_tooltip();

    @Default(lngs = { "en" }, values = { "Enable/Disable Autoreconnect" })
    String action_reconnect_toggle();

    @Default(lngs = { "en" }, values = { "ctrl+r" })
    String action_reconnect_toggle_accelerator();

    @Default(lngs = { "en" }, values = { "r" })
    String action_reconnect_toggle_mnemonic();

    @Default(lngs = { "en" }, values = { "Enable or Disable Auto-Reconnection" })
    String action_reconnect_toggle_tooltip();

    // action_
    @Default(lngs = { "en" }, values = { "Disable Links" })
    String action_remove_disabled_links();

    @Default(lngs = { "en" }, values = { "-" })
    String action_remove_disabled_links_accelerator();

    @Default(lngs = { "en" }, values = { "-" })
    String action_remove_disabled_links_mnemonic();

    @Default(lngs = { "en" }, values = { "tooltip" })
    String action_remove_disabled_links_tooltip();

    // action_//
    @Default(lngs = { "en" }, values = { "Remove Dupes" })
    String action_remove_dupe_links();

    @Default(lngs = { "en" }, values = { "-" })
    String action_remove_dupe_links_accelerator();

    @Default(lngs = { "en" }, values = { "-" })
    String action_remove_dupe_links_mnemonic();

    @Default(lngs = { "en" }, values = { "tooltip" })
    String action_remove_dupe_links_tooltip();

    // action_//
    @Default(lngs = { "en" }, values = { "Remove failed links" })
    String action_remove_failed_links();

    @Default(lngs = { "en" }, values = { "-" })
    String action_remove_failed_links_accelerator();

    @Default(lngs = { "en" }, values = { "-" })
    String action_remove_failed_links_mnemonic();

    @Default(lngs = { "en" }, values = { "tooltip" })
    String action_remove_failed_links_tooltip();

    @Default(lngs = { "en" }, values = { "Remove Links" })
    String action_remove_links();

    String action_remove_links_accelerator();

    String action_remove_links_mnemonic();

    String action_remove_links_tooltip();

    // action_//
    @Default(lngs = { "en" }, values = { "Remove offline links" })
    String action_remove_offline_links();

    @Default(lngs = { "en" }, values = { "-" })
    String action_remove_offline_links_accelerator();

    @Default(lngs = { "en" }, values = { "-" })
    String action_remove_offline_links_mnemonic();

    @Default(lngs = { "en" }, values = { "tooltip" })
    String action_remove_offline_links_tooltip();

    @Default(lngs = { "en" }, values = { "Remove Packages" })
    String action_remove_packages();

    String action_remove_packages_accelerator();

    String action_remove_packages_mnemonic();

    String action_remove_packages_tooltip();

    // action_//
    @Default(lngs = { "en" }, values = { "Restart" })
    String action_restart();

    @Default(lngs = { "en" }, values = { "-" })
    String action_restart_accelerator();

    @Default(lngs = { "en" }, values = { "-" })
    String action_restart_mnemonic();

    @Default(lngs = { "en" }, values = { "Restart JDownloader" })
    String action_restart_tooltip();

    @Default(lngs = { "en" }, values = { "" })
    String action_seperator();

    @Default(lngs = { "en" }, values = { "Settings" })
    String action_settings();

    @Default(lngs = { "en" }, values = { "ctrl+s" })
    String action_settings_accelerator();

    // action_//
    @Default(lngs = { "en" }, values = { "Settings" })
    String action_settings_menu();

    @Default(lngs = { "en" }, values = { "-" })
    String action_settings_menu_accelerator();

    @Default(lngs = { "en" }, values = { "-" })
    String action_settings_menu_mnemonic();

    @Default(lngs = { "en" }, values = { "Open Settings panel" })
    String action_settings_menu_tooltip();

    @Default(lngs = { "en" }, values = { "s" })
    String action_settings_mnemonic();

    @Default(lngs = { "en" }, values = { "Show Settings" })
    String action_settings_tooltip();

    @Default(lngs = { "en" }, values = { "Start Downloads" })
    String action_start_downloads();

    @Default(lngs = { "en" }, values = { "ctrl+p" })
    String action_start_downloads_accelerator();

    @Default(lngs = { "en" }, values = { "p" })
    String action_start_downloads_mnemonic();

    @Default(lngs = { "en" }, values = { "Check for Updates" })
    String action_start_update();

    @Default(lngs = { "en" }, values = { "-" })
    String action_start_update_accelerator();

    @Default(lngs = { "en" }, values = { "-" })
    String action_start_update_mnemonic();

    @Default(lngs = { "en" }, values = { "Check if there are uninstalled updates." })
    String action_start_update_tooltip();

    @Default(lngs = { "en" }, values = { "Stop Downloads" })
    String action_stop_downloads();

    @Default(lngs = { "en" }, values = { "ctrl+s" })
    String action_stop_downloads_accelerator();

    @Default(lngs = { "en" }, values = { "s" })
    String action_stop_downloads_mnemonic();

    @Default(lngs = { "en" }, values = { "Stops all running Downloads" })
    String action_stop_downloads_tooltip();

    @Default(lngs = { "en" }, values = { "Stop Marker" })
    String action_stopsign();

    @Default(lngs = { "en" }, values = { "-" })
    String action_stopsign_accelerator();

    @Default(lngs = { "en" }, values = { "-" })
    String action_stopsign_mnemonic();

    @Default(lngs = { "en" }, values = { "Activate the Stop marker on current selection" })
    String action_stopsign_tooltip();

    @Default(lngs = { "en" }, values = { "Use Logins" })
    String authtablemodel_column_enabled();

    @Default(lngs = { "en" }, values = { "Host/URL" })
    String authtablemodel_column_host();

    @Default(lngs = { "en" }, values = { "...enter Domain here" })
    String authtablemodel_column_host_help();

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

    @Default(lngs = { "en" }, values = { "...enter Username here" })
    String authtablemodel_column_username_help();

    @Default(lngs = { "en" }, values = { "Download started" })
    String ballon_download_finished_started();

    @Default(lngs = { "en" }, values = { "Download stopped" })
    String ballon_download_finished_stopped();

    @Default(lngs = { "en" }, values = { "Download" })
    String ballon_download_title();

    @Default(lngs = { "en" }, values = { "Autostart downloads in few seconds..." })
    String controller_downloadautostart();

    @Default(lngs = { "en" }, values = { "Do you really want to disable all premium accounts?" })
    String dialogs_premiumstatus_global_message();

    @Default(lngs = { "en" }, values = { "Disable Premium?" })
    String dialogs_premiumstatus_global_title();

    @Default(lngs = { "en" }, values = { "Extension Modules" })
    String extensionManager_title();

    @Default(lngs = { "en" }, values = { "Enabled/Disabled" })
    String extensiontablemodel_column_enabled();

    @Default(lngs = { "en" }, values = { "Linklist Backup failed! Check %s1 for rights!" })
    String gui_backup_finished_failed(Object s1);

    @Default(lngs = { "en" }, values = { "Linklist Backup successful! (%s1)" })
    String gui_backup_finished_success(Object s1);

    @Default(lngs = { "en" }, values = { "Backup" })
    String gui_balloon_backup_title();

    @Default(lngs = { "en" }, values = { "Cancel" })
    String gui_btn_cancel();

    @Default(lngs = { "en" }, values = { "No" })
    String gui_btn_no();

    @Default(lngs = { "en" }, values = { "Browse" })
    String gui_btn_select();

    @Default(lngs = { "en" }, values = { "Settings" })
    String gui_btn_settings();

    @Default(lngs = { "en" }, values = { "Yes" })
    String gui_btn_yes();

    @Default(lngs = { "en" }, values = { "Enter Captcha to continue!" })
    String gui_captchaWindow_askForInput(String hoster);

    @Default(lngs = { "en" }, values = { "DefaultProxy" })
    String gui_column_defaultproxy();

    @Default(lngs = { "en" }, values = { "Host/IP" })
    String gui_column_host();

    @Default(lngs = { "en" }, values = { "Password" })
    String gui_column_pass();

    @Default(lngs = { "en" }, values = { "Plugin" })
    String gui_column_plugin();

    @Default(lngs = { "en" }, values = { "Port" })
    String gui_column_port();

    @Default(lngs = { "en" }, values = { "Proxystatus" })
    String gui_column_proxystatus();

    @Default(lngs = { "en" }, values = { "Proxytype" })
    String gui_column_proxytype();

    @Default(lngs = { "en" }, values = { "Settings" })
    String gui_column_settings();

    @Default(lngs = { "en" }, values = { "Activate" })
    String gui_column_status();

    @Default(lngs = { "en" }, values = { "TOS" })
    String gui_column_tos();

    @Default(lngs = { "en" }, values = { "Use" })
    String gui_column_use();

    @Default(lngs = { "en" }, values = { "Username" })
    String gui_column_user();

    @Default(lngs = { "en" }, values = { "Connections" })
    String gui_column_proxyconnects();

    @Default(lngs = { "en" }, values = { "Version" })
    String gui_column_version();

    @Default(lngs = { "en" }, values = { "Size of Captcha Dialogs" })
    String gui_config_barrierfree_captchasize();

    @Default(lngs = { "en" }, values = { "Disable automatic CAPTCHA" })
    String gui_config_captcha_jac_disable();

    @Default(lngs = { "en" }, values = { "Captcha settings" })
    String gui_config_captcha_settings();

    @Default(lngs = { "en" }, values = { "Display Threshold" })
    String gui_config_captcha_train_level();

    @Default(lngs = { "en" }, values = { "Countdown for CAPTCHA window" })
    String gui_config_captcha_train_show_timeout();

    @Default(lngs = { "en" }, values = { "Let Reconnects interrupt resumeable downloads" })
    String gui_config_download_autoresume();

    @Default(lngs = { "en" }, values = { "Max. Buffersize[KB]" })
    String gui_config_download_buffersize2();

    @Default(lngs = { "en" }, values = { "SFV/CRC check when possible" })
    String gui_config_download_crc();

    @Default(lngs = { "en" }, values = { "Download Control" })
    String gui_config_download_download_tab();

    @Default(lngs = { "en" }, values = { "Reconnection IP-Check" })
    String gui_config_download_ipcheck();

    @Default(lngs = { "en" }, values = { "Use balanced IP-Check" })
    String gui_config_download_ipcheck_balance();

    @Default(lngs = { "en" }, values = { "Disable IP-Check" })
    String gui_config_download_ipcheck_disable();

    @Default(lngs = { "en" }, values = { "External IP Check Interval [min]" })
    String gui_config_download_ipcheck_externalinterval2();

    @Default(lngs = { "en" }, values = { "Allowed IPs" })
    String gui_config_download_ipcheck_mask();

    @Default(lngs = { "en" }, values = { "IP Filter RegEx" })
    String gui_config_download_ipcheck_regex();

    @Default(lngs = { "en" }, values = { "Please enter Regex for IPCheck here" })
    String gui_config_download_ipcheck_regex_default();

    @Default(lngs = { "en" }, values = { "Check IP online" })
    String gui_config_download_ipcheck_website();

    @Default(lngs = { "en" }, values = { "Please enter Website for IPCheck here" })
    String gui_config_download_ipcheck_website_default();

    @Default(lngs = { "en" }, values = { "Speed of pause in KiB/s" })
    String gui_config_download_pausespeed();

    @Default(lngs = { "en" }, values = { "Do not start new links if reconnect requested" })
    String gui_config_download_preferreconnect();

    @Default(lngs = { "en" }, values = { "Maximum of simultaneous downloads per host" })
    String gui_config_download_simultan_downloads_per_host();

    @Default(lngs = { "en" }, values = { "File writing" })
    String gui_config_download_write();

    @Default(lngs = { "en" }, values = { "Create Subfolder with packagename if possible" })
    String gui_config_general_createsubfolders();

    @Default(lngs = { "en" }, values = { "Create sub-folders after adding links" })
    String gui_config_general_createsubfoldersbefore();

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

    @Default(lngs = { "en" }, values = { "Barrier-Free" })
    String gui_config_gui_barrierfree();

    @Default(lngs = { "en" }, values = { "Container (RSDF,DLC,CCF,..)" })
    String gui_config_gui_container();

    @Default(lngs = { "en" }, values = { "Enable Windowdecoration" })
    String gui_config_gui_decoration();

    @Default(lngs = { "en" }, values = { "Feel" })
    String gui_config_gui_feel();

    @Default(lngs = { "en" }, values = { "Font Size [%]" })
    String gui_config_gui_font_size();

    @Default(lngs = { "en" }, values = { "Language" })
    String gui_config_gui_language();

    @Default(lngs = { "en" }, values = { "General Linkgrabber Settings" })
    String gui_config_gui_linggrabber();

    @Default(lngs = { "en" }, values = { "Linkfilter" })
    String gui_config_gui_linggrabber_ignorelist();

    @Default(lngs = { "en" }, values = { "LinkGrabber" })
    String gui_config_gui_linkgrabber();

    @Default(lngs = { "en" }, values = { "Performance" })
    String gui_config_gui_performance();

    @Default(lngs = { "en" }, values = { "Style (Restart required)" })
    String gui_config_gui_plaf();

    @Default(lngs = { "en" }, values = { "Dialog Information has been reseted." })
    String gui_config_gui_resetdialogs_message();

    @Default(lngs = { "en" }, values = { "Reset" })
    String gui_config_gui_resetdialogs_short();

    @Default(lngs = { "en" }, values = { "Reset Dialog Information" })
    String gui_config_gui_resetdialogs2();

    @Default(lngs = { "en" }, values = { "Look" })
    String gui_config_gui_view();

    @Default(lngs = { "en" }, values = { "Enable Click'n'Load Support" })
    String gui_config_linkgrabber_cnl2();

    @Default(lngs = { "en" }, values = { "Put Linkgrabberbuttons above table" })
    String gui_config_linkgrabber_controlposition();

    @Default(lngs = { "en" }, values = { "Filter Type" })
    String gui_config_linkgrabber_filter_type();

    @Default(lngs = { "en" }, values = { "Filter Rules" })
    String gui_config_linkgrabber_ignorelist();

    @Default(lngs = { "en" }, values = { "Show infopanel on linkgrabber" })
    String gui_config_linkgrabber_infopanel_onlinkgrab();

    @Default(lngs = { "en" }, values = { "Check linkinfo and onlinestatus" })
    String gui_config_linkgrabber_onlincheck();

    @Default(lngs = { "en" }, values = { "(Autopackager)Replace dots and _ with spaces?" })
    String gui_config_linkgrabber_replacechars();

    @Default(lngs = { "en" }, values = { "General Reconnect Settings" })
    String gui_config_reconnect_shared();

    @Default(lngs = { "en" }, values = { "Reload Download Container" })
    String gui_config_reloadcontainer();

    @Default(lngs = { "en" }, values = { "Show detailed container information on load" })
    String gui_config_showContainerOnLoadInfo();

    @Default(lngs = { "en" }, values = { "Auto open Link Containers (dlc,ccf,...)" })
    String gui_config_simple_container();

    @Default(lngs = { "en" }, values = { "Miscellaneous" })
    String gui_config_various();

    @Default(lngs = { "en" }, values = { "Add a URL(s). JDownloader will load and parse them for further links." })
    String gui_dialog_addurl_message();

    @Default(lngs = { "en" }, values = { "Parse URL(s)" })
    String gui_dialog_addurl_okoption_parse();

    @Default(lngs = { "en" }, values = { "Add URL(s)" })
    String gui_dialog_addurl_title();

    @Default(lngs = { "en" }, values = { "Message" })
    String gui_dialogs_message_title();

    @Default(lngs = { "en" }, values = { "[Plugin disabled]" })
    String gui_downloadlink_plugindisabled();

    @Default(lngs = { "en" }, values = { "Delete selected links?" })
    String gui_downloadlist_delete();

    @Default(lngs = { "en" }, values = { "%s1 files" })
    String gui_downloadlist_delete_files(Object s1);

    @Default(lngs = { "en" }, values = { "%s1 links" })
    String gui_downloadlist_delete_links(Object s1);

    @Default(lngs = { "en" }, values = { "%s1 links" })
    String gui_downloadlist_delete_size_packagev2(Object s1);

    @Default(lngs = { "en" }, values = { "Delete links from downloadlist and disk?" })
    String gui_downloadlist_delete2();

    @Default(lngs = { "en" }, values = { "Reset selected downloads?" })
    String gui_downloadlist_reset();

    @Default(lngs = { "en" }, values = { "Stopping current downloads..." })
    String gui_downloadstop();

    @Default(lngs = { "en" }, values = { "Load DLC file" })
    String gui_filechooser_loaddlc();

    @Default(lngs = { "en" }, values = { "Downloadlink" })
    String gui_fileinfopanel_link();

    @Default(lngs = { "en" }, values = { "Chunks" })
    String gui_fileinfopanel_linktab_chunks();

    @Default(lngs = { "en" }, values = { "Comment" })
    String gui_fileinfopanel_linktab_comment();

    @Default(lngs = { "en" }, values = { "ETA: %s1" })
    String gui_fileinfopanel_linktab_eta2(Object s1);

    @Default(lngs = { "en" }, values = { "Linkname" })
    String gui_fileinfopanel_linktab_name();

    @Default(lngs = { "en" }, values = { "Password" })
    String gui_fileinfopanel_linktab_password();

    @Default(lngs = { "en" }, values = { "Save to" })
    String gui_fileinfopanel_linktab_saveto();

    @Default(lngs = { "en" }, values = { "Speed: %s1/s" })
    String gui_fileinfopanel_linktab_speed(Object s1);

    @Default(lngs = { "en" }, values = { "Status" })
    String gui_fileinfopanel_linktab_status();

    @Default(lngs = { "en" }, values = { "URL" })
    String gui_fileinfopanel_linktab_url();

    @Default(lngs = { "en" }, values = { "Package" })
    String gui_fileinfopanel_packagetab();

    @Default(lngs = { "en" }, values = { "Post Processing" })
    String gui_fileinfopanel_packagetab_chb_postProcessing();

    @Default(lngs = { "en" }, values = { "Enable Post Processing for this FilePackage, like extracting or merging." })
    String gui_fileinfopanel_packagetab_chb_postProcessing_toolTip();

    @Default(lngs = { "en" }, values = { "Comment" })
    String gui_fileinfopanel_packagetab_lbl_comment();

    @Default(lngs = { "en" }, values = { "%s1 File(s)" })
    String gui_fileinfopanel_packagetab_lbl_files(Object s1);

    @Default(lngs = { "en" }, values = { "Package Name" })
    String gui_fileinfopanel_packagetab_lbl_name();

    @Default(lngs = { "en" }, values = { "Archive Password" })
    String gui_fileinfopanel_packagetab_lbl_password();

    @Default(lngs = { "en" }, values = { "Archive Password(auto)" })
    String gui_fileinfopanel_packagetab_lbl_password2();

    @Default(lngs = { "en" }, values = { "Save to" })
    String gui_fileinfopanel_packagetab_lbl_saveto();

    @Default(lngs = { "en" }, values = { "Aborted" })
    String gui_linkgrabber_aborted();

    @Default(lngs = { "en" }, values = { "Adding %s1 link(s) to LinkGrabber" })
    String gui_linkgrabber_adding(Object s1);

    @Default(lngs = { "en" }, values = { "Already on Download List" })
    String gui_linkgrabber_alreadyindl();

    @Default(lngs = { "en" }, values = { "Grabbed %s1 link(s) in %s2 Package(s)" })
    String gui_linkgrabber_finished(Object s1, Object s2);

    @Default(lngs = { "en" }, values = { "Package / Filename" })
    String gui_linkgrabber_header_packagesfiles();

    @Default(lngs = { "en" }, values = { "%s1 offline" })
    String gui_linkgrabber_packageofflinepercent(Object s1);

    @Default(lngs = { "en" }, values = { "All online" })
    String gui_linkgrabber_packageonlineall();

    @Default(lngs = { "en" }, values = { "%s1 online" })
    String gui_linkgrabber_packageonlinepercent(Object s1);

    @Default(lngs = { "en" }, values = { "Use Subdirectory" })
    String gui_linkgrabber_packagetab_chb_useSubdirectory();

    @Default(lngs = { "en" }, values = { "Comment" })
    String gui_linkgrabber_packagetab_lbl_comment();

    @Default(lngs = { "en" }, values = { "Package Name" })
    String gui_linkgrabber_packagetab_lbl_name();

    @Default(lngs = { "en" }, values = { "Archive Password" })
    String gui_linkgrabber_packagetab_lbl_password();

    @Default(lngs = { "en" }, values = { "Archive Password(auto)" })
    String gui_linkgrabber_packagetab_lbl_password2();

    @Default(lngs = { "en" }, values = { "Save to" })
    String gui_linkgrabber_packagetab_lbl_saveto();

    @Default(lngs = { "en" }, values = { "LinkGrabber operations pending..." })
    String gui_linkgrabber_pc_linkgrabber();

    @Default(lngs = { "en" }, values = { "Clear linkgrabber list?" })
    String gui_linkgrabberv2_lg_clear_ask();

    @Default(lngs = { "en" }, values = { "Continue with selected package(s)" })
    String gui_linkgrabberv2_lg_continueselected();

    @Default(lngs = { "en" }, values = { "Continue with selected link(s)" })
    String gui_linkgrabberv2_lg_continueselectedlinks();

    @Default(lngs = { "en" }, values = { "Keep only selected Hoster" })
    String gui_linkgrabberv2_onlyselectedhoster();

    @Default(lngs = { "en" }, values = { "Split by hoster" })
    String gui_linkgrabberv2_splithoster();

    @Default(lngs = { "en" }, values = { "New Package Name" })
    String gui_linklist_editpackagename_message();

    @Default(lngs = { "en" }, values = { "Name of the new package" })
    String gui_linklist_newpackage_message();

    @Default(lngs = { "en" }, values = { "Set download password" })
    String gui_linklist_setpw_message();

    @Default(lngs = { "en" }, values = { "Upload failed" })
    String gui_logDialog_warning_uploadFailed();

    @Default(lngs = { "en" }, values = { "Please describe your Problem/Bug/Question!" })
    String gui_logger_askQuestion();

    @Default(lngs = { "en" }, values = { "Please send this loglink to your supporter" })
    String gui_logupload_message();

    @Default(lngs = { "en" }, values = { "About" })
    String gui_menu_about();

    @Default(lngs = { "en" }, values = { "Pause downloads. Limits global speed to %s1 KiB/s" })
    String gui_menu_action_break2_desc(Object s1);

    @Default(lngs = { "en" }, values = { "action.premium.buy" })
    String gui_menu_action_premium_buy_name();

    @Default(lngs = { "en" }, values = { "Your Reconnect is not configured correct" })
    String gui_menu_action_reconnect_notconfigured_tooltip();

    @Default(lngs = { "en" }, values = { "Auto reconnect. Get a new IP by resetting your internet connection" })
    String gui_menu_action_reconnectauto_desc();

    @Default(lngs = { "en" }, values = { "Manual reconnect. Get a new IP by resetting your internet connection" })
    String gui_menu_action_reconnectman_desc();

    @Default(lngs = { "en" }, values = { "Add links" })
    String gui_menu_addlinks();

    @Default(lngs = { "en" }, values = { "Premium" })
    String gui_menu_premium();

    @Default(lngs = { "en" }, values = { "Clean up" })
    String gui_menu_remove();

    @Default(lngs = { "en" }, values = { "Backup" })
    String gui_menu_save();

    @Default(lngs = { "en" }, values = { "%s1 - %s2 account(s) -- At the moment it may be that no premium traffic is left." })
    String gui_premiumstatus_expired_maybetraffic_tooltip(Object s1, Object s2);

    @Default(lngs = { "en" }, values = { "%s1 - %s2 account(s) -- At the moment no premium traffic is available." })
    String gui_premiumstatus_expired_traffic_tooltip(Object s1, Object s2);

    @Default(lngs = { "en" }, values = { "%s1 - %s2 account(s) -- You can download up to %s3 today." })
    String gui_premiumstatus_traffic_tooltip(Object s1, Object s2, Object s3);

    @Default(lngs = { "en" }, values = { "%s1 -- Unlimited traffic! You can download as much as you want to." })
    String gui_premiumstatus_unlimited_traffic_tooltip(Object s1);

    @Default(lngs = { "en" }, values = { "Do you want to reconnect your internet connection?" })
    String gui_reconnect_confirm();

    @Default(lngs = { "en" }, values = { "Advanced Settings" })
    String gui_settings_advanced_title();

    @Default(lngs = { "en" }, values = { "Max. Con." })
    String gui_statusbar_maxChunks();

    @Default(lngs = { "en" }, values = { "Max. Dls." })
    String gui_statusbar_sim_ownloads();

    @Default(lngs = { "en" }, values = { "Max. Speed" })
    String gui_statusbar_speed();

    @Default(lngs = { "en" }, values = { "Open in browser" })
    String gui_table_contextmenu_browselink();

    @Default(lngs = { "en" }, values = { "Check Online Status" })
    String gui_table_contextmenu_check();

    @Default(lngs = { "en" }, values = { "Copy URL" })
    String gui_table_contextmenu_copyLink();

    @Default(lngs = { "en" }, values = { "Copy Password" })
    String gui_table_contextmenu_copyPassword();

    @Default(lngs = { "en" }, values = { "Delete from list" })
    String gui_table_contextmenu_deletelist2();

    @Default(lngs = { "en" }, values = { "Delete from list and disk" })
    String gui_table_contextmenu_deletelistdisk2();

    @Default(lngs = { "en" }, values = { "Disable" })
    String gui_table_contextmenu_disable();

    @Default(lngs = { "en" }, values = { "Create DLC" })
    String gui_table_contextmenu_dlc();

    @Default(lngs = { "en" }, values = { "Open Directory" })
    String gui_table_contextmenu_downloaddir();

    @Default(lngs = { "en" }, values = { "Edit Directory" })
    String gui_table_contextmenu_editdownloaddir();

    @Default(lngs = { "en" }, values = { "Change Package Name" })
    String gui_table_contextmenu_editpackagename();

    @Default(lngs = { "en" }, values = { "Enable" })
    String gui_table_contextmenu_enable();

    @Default(lngs = { "en" }, values = { "Filter" })
    String gui_table_contextmenu_filetype();

    @Default(lngs = { "en" }, values = { "More" })
    String gui_table_contextmenu_more();

    @Default(lngs = { "en" }, values = { "Move into new Package" })
    String gui_table_contextmenu_newpackage();

    @Default(lngs = { "en" }, values = { "Open File" })
    String gui_table_contextmenu_openfile();

    @Default(lngs = { "en" }, values = { "Sort Packages" })
    String gui_table_contextmenu_packagesort();

    @Default(lngs = { "en" }, values = { "Priority" })
    String gui_table_contextmenu_priority();

    @Default(lngs = { "en" }, values = { "Properties" })
    String gui_table_contextmenu_prop();

    @Default(lngs = { "en" }, values = { "Remove" })
    String gui_table_contextmenu_remove();

    @Default(lngs = { "en" }, values = { "Reset" })
    String gui_table_contextmenu_reset();

    @Default(lngs = { "en" }, values = { "Resume" })
    String gui_table_contextmenu_resume();

    @Default(lngs = { "en" }, values = { "Set download password" })
    String gui_table_contextmenu_setdlpw();

    @Default(lngs = { "en" }, values = { "Sort" })
    String gui_table_contextmenu_sort();

    @Default(lngs = { "en" }, values = { "Set Stopmark" })
    String gui_table_contextmenu_stopmark_set();

    @Default(lngs = { "en" }, values = { "Unset Stopmark" })
    String gui_table_contextmenu_stopmark_unset();

    @Default(lngs = { "en" }, values = { "Force download" })
    String gui_table_contextmenu_tryforce();

    @Default(lngs = { "en" }, values = { "Drop after '%s1'" })
    String gui_table_draganddrop_after(Object s1);

    @Default(lngs = { "en" }, values = { "Drop before '%s1'" })
    String gui_table_draganddrop_before(Object s1);

    @Default(lngs = { "en" }, values = { "Insert at the end of Package '%s1'" })
    String gui_table_draganddrop_insertinpackageend(Object s1);

    @Default(lngs = { "en" }, values = { "Insert at the beginning of Package '%s1'" })
    String gui_table_draganddrop_insertinpackagestart(Object s1);

    @Default(lngs = { "en" }, values = { "Insert before '%s1'" })
    String gui_table_draganddrop_movepackagebefore(Object s1);

    @Default(lngs = { "en" }, values = { "Insert after '%s1'" })
    String gui_table_draganddrop_movepackageend(Object s1);

    @Default(lngs = { "en" }, values = { "Add at top" })
    String gui_taskpanes_download_linkgrabber_config_addattop();

    @Default(lngs = { "en" }, values = { "Start Automatically" })
    String gui_taskpanes_download_linkgrabber_config_autostart();

    @Default(lngs = { "en" }, values = { "Start after adding" })
    String gui_taskpanes_download_linkgrabber_config_startofter();

    @Default(lngs = { "en" }, values = { "Copy" })
    String gui_textcomponent_context_copy();

    @Default(lngs = { "en" }, values = { "ctrl C" })
    String gui_textcomponent_context_copy_acc();

    @Default(lngs = { "en" }, values = { "Cut" })
    String gui_textcomponent_context_cut();

    @Default(lngs = { "en" }, values = { "ctrl X" })
    String gui_textcomponent_context_cut_acc();

    @Default(lngs = { "en" }, values = { "Delete" })
    String gui_textcomponent_context_delete();

    @Default(lngs = { "en" }, values = { "DELETE" })
    String gui_textcomponent_context_delete_acc();

    @Default(lngs = { "en" }, values = { "Paste" })
    String gui_textcomponent_context_paste();

    @Default(lngs = { "en" }, values = { "ctrl V" })
    String gui_textcomponent_context_paste_acc();

    @Default(lngs = { "en" }, values = { "Select all" })
    String gui_textcomponent_context_selectall();

    @Default(lngs = { "en" }, values = { "ctrl A" })
    String gui_textcomponent_context_selectall_acc();

    @Default(lngs = { "en" }, values = { "Max. Connections/File" })
    String gui_tooltip_statusbar_max_chunks();

    @Default(lngs = { "en" }, values = { "Maximum simultaneous Downloads [1..20]" })
    String gui_tooltip_statusbar_simultan_downloads();

    @Default(lngs = { "en" }, values = { "Speed Limit (KiB/s) [0 = Infinite]" })
    String gui_tooltip_statusbar_speedlimiter();

    @Default(lngs = { "en" }, values = { "if selected, links will get added and started automatically" })
    String gui_tooltips_linkgrabber_autostart();

    @Default(lngs = { "en" }, values = { "Is selected, download starts after adding new links" })
    String gui_tooltips_linkgrabber_startlinksafteradd();

    @Default(lngs = { "en" }, values = { "if selected, new links will be added at top of your downloadlist" })
    String gui_tooltips_linkgrabber_topOrBottom();

    @Default(lngs = { "en" }, values = { "Added date" })
    String gui_treetable_added();

    @Default(lngs = { "en" }, values = { "Plugin error" })
    String gui_treetable_error_plugin();

    @Default(lngs = { "en" }, values = { "Finished date" })
    String gui_treetable_finished();

    @Default(lngs = { "en" }, values = { "Size" })
    String gui_treetable_header_size();

    @Default(lngs = { "en" }, values = { "Host" })
    String gui_treetable_hoster();

    @Default(lngs = { "en" }, values = { "Loaded" })
    String gui_treetable_loaded();

    @Default(lngs = { "en" }, values = { "Package / Filename" })
    String gui_treetable_name();

    @Default(lngs = { "en" }, values = { "Progress" })
    String gui_treetable_progress();

    @Default(lngs = { "en" }, values = { "Proxy" })
    String gui_treetable_proxy();

    @Default(lngs = { "en" }, values = { "Remaining" })
    String gui_treetable_remaining();

    @Default(lngs = { "en" }, values = { "FileSize" })
    String gui_treetable_size();

    @Default(lngs = { "en" }, values = { "Status" })
    String gui_treetable_status();

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

    @Default(lngs = { "en" }, values = { "After Installation, JDownloader will update to the latest version." })
    String installer_gui_message();

    @Default(lngs = { "en" }, values = { "JDownloader Installation" })
    String installer_gui_title();

    @Default(lngs = { "en" }, values = { "Warning! JD cannot write to %s1. Check rights!" })
    String installer_nowriteDir_warning(Object s1);

    @Default(lngs = { "en" }, values = { "Warning! JD is installed in %s1. This causes errors." })
    String installer_vistaDir_warning(Object s1);

    @Default(lngs = { "en" }, values = { "File" })
    String jd_gui_skins_simple_simplegui_menubar_filemenu();

    @Default(lngs = { "en" }, values = { "Links" })
    String jd_gui_skins_simple_simplegui_menubar_linksmenu();

    @Default(lngs = { "en" }, values = { "Contributors" })
    String jd_gui_swing_components_AboutDialog_contributers();

    @Default(lngs = { "en" }, values = { "Copy" })
    String jd_gui_swing_components_AboutDialog_copy();

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

    @Default(lngs = { "en" }, values = { "Hoster:" })
    String jd_gui_swing_components_AccountDialog_hoster();

    @Default(lngs = { "en" }, values = { "Name:" })
    String jd_gui_swing_components_AccountDialog_name();

    @Default(lngs = { "en" }, values = { "Pass:" })
    String jd_gui_swing_components_AccountDialog_pass();

    @Default(lngs = { "en" }, values = { "Add new Account" })
    String jd_gui_swing_components_AccountDialog_title();

    @Default(lngs = { "en" }, values = { "Click here to close this Balloon and open JD" })
    String jd_gui_swing_components_Balloon_toolTip();

    @Default(lngs = { "en" }, values = { "Close Tab" })
    String jd_gui_swing_components_JDCloseAction_closeTab();

    @Default(lngs = { "en" }, values = { "Close %s1" })
    String jd_gui_swing_components_JDCollapser_closetooltip(Object s1);

    @Default(lngs = { "en" }, values = { "Change Columns" })
    String jd_gui_swing_components_table_JDTable_columnControl();

    @Default(lngs = { "en" }, values = { "IP:" })
    String jd_gui_swing_dialog_ProxyDialog_hostip();

    @Default(lngs = { "en" }, values = { "Host/Port:" })
    String jd_gui_swing_dialog_ProxyDialog_hostport();

    @Default(lngs = { "en" }, values = { "HTTP" })
    String jd_gui_swing_dialog_ProxyDialog_http();

    @Default(lngs = { "en" }, values = { "Local IP" })
    String jd_gui_swing_dialog_ProxyDialog_localip();

    @Default(lngs = { "en" }, values = { "Password:" })
    String jd_gui_swing_dialog_ProxyDialog_password();

    @Default(lngs = { "en" }, values = { "Socks5" })
    String jd_gui_swing_dialog_ProxyDialog_socks5();

    @Default(lngs = { "en" }, values = { "Add new Proxy" })
    String jd_gui_swing_dialog_ProxyDialog_title();

    @Default(lngs = { "en" }, values = { "Type:" })
    String jd_gui_swing_dialog_ProxyDialog_type();

    @Default(lngs = { "en" }, values = { "Username:" })
    String jd_gui_swing_dialog_ProxyDialog_username();

    @Default(lngs = { "en" }, values = { "Which hoster are you interested in?" })
    String jd_gui_swing_jdgui_actions_ActionController_buy_message();

    @Default(lngs = { "en" }, values = { "Buy Premium" })
    String jd_gui_swing_jdgui_actions_ActionController_buy_title();

    @Default(lngs = { "en" }, values = { "Continue" })
    String jd_gui_swing_jdgui_actions_ActionController_continue();

    @Default(lngs = { "en" }, values = { "Stop after current Downloads" })
    String jd_gui_swing_jdgui_actions_ActionController_toolbar_control_stopmark_tooltip();

    @Default(lngs = { "en" }, values = { "Enter Encryption Password" })
    String jd_gui_swing_jdgui_menu_actions_BackupLinkListAction_password();

    @Default(lngs = { "en" }, values = { "Do you really want to remove all completed DownloadLinks?" })
    String jd_gui_swing_jdgui_menu_actions_CleanupDownload_message();

    @Default(lngs = { "en" }, values = { "Do you really want to remove all completed FilePackages?" })
    String jd_gui_swing_jdgui_menu_actions_CleanupPackages_message();

    @Default(lngs = { "en" }, values = { "Do you really want to remove all disabled DownloadLinks?" })
    String jd_gui_swing_jdgui_menu_actions_RemoveDisabledAction_message();

    @Default(lngs = { "en" }, values = { "Do you really want to remove all duplicated DownloadLinks?" })
    String jd_gui_swing_jdgui_menu_actions_RemoveDupesAction_message();

    @Default(lngs = { "en" }, values = { "Do you really want to remove all failed DownloadLinks?" })
    String jd_gui_swing_jdgui_menu_actions_RemoveFailedAction_message();

    @Default(lngs = { "en" }, values = { "Do you really want to remove all offline DownloadLinks?" })
    String jd_gui_swing_jdgui_menu_actions_RemoveOfflineAction_message();

    @Default(lngs = { "en" }, values = { "This option needs a JDownloader restart." })
    String jd_gui_swing_jdgui_settings_ConfigPanel_restartquestion();

    @Default(lngs = { "en" }, values = { "Restart NOW!" })
    String jd_gui_swing_jdgui_settings_ConfigPanel_restartquestion_ok();

    @Default(lngs = { "en" }, values = { "Restart required!" })
    String jd_gui_swing_jdgui_settings_ConfigPanel_restartquestion_title();

    @Default(lngs = { "en" }, values = { "Extensions" })
    String jd_gui_swing_jdgui_settings_panels_ConfigPanelAddons_addons_title();

    @Default(lngs = { "en" }, values = { "Version: %s1" })
    String jd_gui_swing_jdgui_settings_panels_ConfigPanelAddons_version(Object s1);

    @Default(lngs = { "en" }, values = { "JAntiCaptcha" })
    String jd_gui_swing_jdgui_settings_panels_ConfigPanelCaptcha_captcha_title();

    @Default(lngs = { "en" }, values = { "Size of Captcha in percent:" })
    String jd_gui_swing_jdgui_settings_panels_ConfigPanelCaptcha_captchaSize();

    @Default(lngs = { "en" }, values = { "Plugins" })
    String jd_gui_swing_jdgui_settings_panels_ConfigPanelPlugin_plugins_title();

    @Default(lngs = { "en" }, values = { "Use all Hosts" })
    String jd_gui_swing_jdgui_settings_panels_ConfigPanelPlugin_useAll();

    @Default(lngs = { "en" }, values = { "You disabled the IP-Check. This will increase the reconnection times dramatically!\r\n\r\nSeveral further modules like Reconnect Recorder are disabled." })
    String jd_gui_swing_jdgui_settings_panels_downloadandnetwork_advanced_ipcheckdisable_warning_message();

    @Default(lngs = { "en" }, values = { "IP-Check disabled!" })
    String jd_gui_swing_jdgui_settings_panels_downloadandnetwork_advanced_ipcheckdisable_warning_title();

    @Default(lngs = { "en" }, values = { "Download & Network" })
    String jd_gui_swing_jdgui_settings_panels_downloadandnetwork_general_download_title();

    @Default(lngs = { "en" }, values = { "Advanced" })
    String jd_gui_swing_jdgui_settings_panels_gui_advanced_gui_advanced_title();

    @Default(lngs = { "en" }, values = { "User Interface" })
    String jd_gui_swing_jdgui_settings_panels_gui_General_gui_title();

    @Default(lngs = { "en" }, values = { "Linkgrabber" })
    String jd_gui_swing_jdgui_settings_panels_gui_Linkgrabber_gui_linkgrabber_title();

    @Default(lngs = { "en" }, values = { "Open new packages by default" })
    String jd_gui_swing_jdgui_settings_panels_gui_Linkgrabber_newpackages();

    @Default(lngs = { "en" }, values = { "Automatic" })
    String jd_gui_swing_jdgui_settings_panels_gui_Linkgrabber_newpackages_automatic();

    @Default(lngs = { "en" }, values = { "Collapsed" })
    String jd_gui_swing_jdgui_settings_panels_gui_Linkgrabber_newpackages_collapsed();

    @Default(lngs = { "en" }, values = { "Expanded" })
    String jd_gui_swing_jdgui_settings_panels_gui_Linkgrabber_newpackages_expanded();

    @Default(lngs = { "en" }, values = { "Description" })
    String jd_gui_swing_jdgui_settings_panels_gui_ToolbarController_column_desc();

    @Default(lngs = { "en" }, values = { "Hotkey" })
    String jd_gui_swing_jdgui_settings_panels_gui_ToolbarController_column_hotkey();

    @Default(lngs = { "en" }, values = { "Name" })
    String jd_gui_swing_jdgui_settings_panels_gui_ToolbarController_column_name();

    @Default(lngs = { "en" }, values = { "Use" })
    String jd_gui_swing_jdgui_settings_panels_gui_ToolbarController_column_use();

    @Default(lngs = { "en" }, values = { "Toolbar Manager" })
    String jd_gui_swing_jdgui_settings_panels_gui_ToolbarController_toolbarController_title();

    @Default(lngs = { "en" }, values = { "Read TOS" })
    String jd_gui_swing_jdgui_settings_panels_hoster_columns_TosColumn_read();

    @Default(lngs = { "en" }, values = { "Archive passwords" })
    String jd_gui_swing_jdgui_settings_panels_PasswordList_general_title();

    @Default(lngs = { "en" }, values = { "HTAccess logins" })
    String jd_gui_swing_jdgui_settings_panels_passwords_PasswordListHTAccess_general_title();

    @Default(lngs = { "en" }, values = { "Always select the premium account with the most traffic left for downloading" })
    String jd_gui_swing_jdgui_settings_panels_premium_Premium_accountSelection();

    @Default(lngs = { "en" }, values = { "Advanced Settings" })
    String jd_gui_swing_jdgui_settings_panels_premium_Premium_settings();

    @Default(lngs = { "en" }, values = { "Accounts" })
    String jd_gui_swing_jdgui_settings_panels_premium_Premium_title2();

    @Default(lngs = { "en" }, values = { "Cash" })
    String jd_gui_swing_jdgui_settings_panels_premium_PremiumJTableModel_cash();

    @Default(lngs = { "en" }, values = { "Enabled" })
    String jd_gui_swing_jdgui_settings_panels_premium_PremiumJTableModel_enabled();

    @Default(lngs = { "en" }, values = { "ExpireDate" })
    String jd_gui_swing_jdgui_settings_panels_premium_PremiumJTableModel_expiredate();

    @Default(lngs = { "en" }, values = { "Number of Files" })
    String jd_gui_swing_jdgui_settings_panels_premium_PremiumJTableModel_filesnum();

    @Default(lngs = { "en" }, values = { "Hoster" })
    String jd_gui_swing_jdgui_settings_panels_premium_PremiumJTableModel_hoster();

    @Default(lngs = { "en" }, values = { "Password" })
    String jd_gui_swing_jdgui_settings_panels_premium_PremiumJTableModel_pass();

    @Default(lngs = { "en" }, values = { "PremiumPoints" })
    String jd_gui_swing_jdgui_settings_panels_premium_PremiumJTableModel_premiumpoints();

    @Default(lngs = { "en" }, values = { "Status" })
    String jd_gui_swing_jdgui_settings_panels_premium_PremiumJTableModel_status();

    @Default(lngs = { "en" }, values = { "Trafficleft" })
    String jd_gui_swing_jdgui_settings_panels_premium_PremiumJTableModel_trafficleft();

    @Default(lngs = { "en" }, values = { "TrafficShare" })
    String jd_gui_swing_jdgui_settings_panels_premium_PremiumJTableModel_trafficshare();

    @Default(lngs = { "en" }, values = { "Used Space" })
    String jd_gui_swing_jdgui_settings_panels_premium_PremiumJTableModel_usedspace();

    @Default(lngs = { "en" }, values = { "User" })
    String jd_gui_swing_jdgui_settings_panels_premium_PremiumJTableModel_user();

    @Default(lngs = { "en" }, values = { "Account" })
    String jd_gui_swing_jdgui_settings_panels_premium_PremiumTableRenderer_account();

    @Default(lngs = { "en" }, values = { "Expired" })
    String jd_gui_swing_jdgui_settings_panels_premium_PremiumTableRenderer_expired();

    @Default(lngs = { "en" }, values = { "Invalid Account" })
    String jd_gui_swing_jdgui_settings_panels_premium_PremiumTableRenderer_invalidAccount();

    @Default(lngs = { "en" }, values = { "No Traffic Left" })
    String jd_gui_swing_jdgui_settings_panels_premium_PremiumTableRenderer_noTrafficLeft();

    @Default(lngs = { "en" }, values = { "Unknown" })
    String jd_gui_swing_jdgui_settings_panels_premium_PremiumTableRenderer_unknown();

    @Default(lngs = { "en" }, values = { "Unlimited" })
    String jd_gui_swing_jdgui_settings_panels_premium_PremiumTableRenderer_unlimited();

    @Default(lngs = { "en" }, values = { "Reconnection" })
    String jd_gui_swing_jdgui_settings_panels_reconnect_Advanced_reconnect_advanced_title();

    @Default(lngs = { "en" }, values = { "Settings" })
    String jd_gui_swing_jdgui_views_configurationview_tab_title();

    @Default(lngs = { "en" }, values = { "All options and settings for JDownloader" })
    String jd_gui_swing_jdgui_views_configurationview_tab_tooltip();

    @Default(lngs = { "en" }, values = { "Stop" })
    String jd_gui_swing_jdgui_views_downloads_contextmenu_StopAction_name();

    @Default(lngs = { "en" }, values = { "Unknown FileSize" })
    String jd_gui_swing_jdgui_views_downloadview_Columns_ProgressColumn_unknownFilesize();

    @Default(lngs = { "en" }, values = { "Download" })
    String jd_gui_swing_jdgui_views_downloadview_tab_title();

    @Default(lngs = { "en" }, values = { "Downloadlist and Progress" })
    String jd_gui_swing_jdgui_views_downloadview_tab_tooltip();

    @Default(lngs = { "en" }, values = { "Extracting" })
    String jd_gui_swing_jdgui_views_downloadview_TableRenderer_extract();

    @Default(lngs = { "en" }, values = { "Download failed" })
    String jd_gui_swing_jdgui_views_downloadview_TableRenderer_failed();

    @Default(lngs = { "en" }, values = { "Download finished" })
    String jd_gui_swing_jdgui_views_downloadview_TableRenderer_finished();

    @Default(lngs = { "en" }, values = { "Loading from" })
    String jd_gui_swing_jdgui_views_downloadview_TableRenderer_loadingFrom();

    @Default(lngs = { "en" }, values = { "Plugin missing" })
    String jd_gui_swing_jdgui_views_downloadview_TableRenderer_missing();

    @Default(lngs = { "en" }, values = { "Loading with Premium" })
    String jd_gui_swing_jdgui_views_downloadview_TableRenderer_premium();

    @Default(lngs = { "en" }, values = { "Resumable download" })
    String jd_gui_swing_jdgui_views_downloadview_TableRenderer_resume();

    @Default(lngs = { "en" }, values = { "Stopmark is set" })
    String jd_gui_swing_jdgui_views_downloadview_TableRenderer_stopmark();

    @Default(lngs = { "en" }, values = { "Download complete in" })
    String jd_gui_swing_jdgui_views_info_DownloadInfoPanel_eta();

    @Default(lngs = { "en" }, values = { "Links(s)" })
    String jd_gui_swing_jdgui_views_info_DownloadInfoPanel_links();

    @Default(lngs = { "en" }, values = { "Package(s)" })
    String jd_gui_swing_jdgui_views_info_DownloadInfoPanel_packages();

    @Default(lngs = { "en" }, values = { "Progress" })
    String jd_gui_swing_jdgui_views_info_DownloadInfoPanel_progress();

    @Default(lngs = { "en" }, values = { "Total size" })
    String jd_gui_swing_jdgui_views_info_DownloadInfoPanel_size();

    @Default(lngs = { "en" }, values = { "Downloadspeed" })
    String jd_gui_swing_jdgui_views_info_DownloadInfoPanel_speed();

    @Default(lngs = { "en" }, values = { "filtered Links(s)" })
    String jd_gui_swing_jdgui_views_info_LinkGrabberInfoPanel_filteredlinks();

    @Default(lngs = { "en" }, values = { "Links(s)" })
    String jd_gui_swing_jdgui_views_info_LinkGrabberInfoPanel_links();

    @Default(lngs = { "en" }, values = { "Package(s)" })
    String jd_gui_swing_jdgui_views_info_LinkGrabberInfoPanel_packages();

    @Default(lngs = { "en" }, values = { "Total size" })
    String jd_gui_swing_jdgui_views_info_LinkGrabberInfoPanel_size();

    @Default(lngs = { "en" }, values = { "Upload Log" })
    String jd_gui_swing_jdgui_views_info_LogInfoPanel_upload();

    @Default(lngs = { "en" }, values = { "Linkgrabber" })
    String jd_gui_swing_jdgui_views_linkgrabberview_tab_title();

    @Default(lngs = { "en" }, values = { "Collect, add and select links and URLs" })
    String jd_gui_swing_jdgui_views_linkgrabberview_tab_tooltip();

    @Default(lngs = { "en" }, values = { "Log" })
    String jd_gui_swing_jdgui_views_log_tab_title();

    @Default(lngs = { "en" }, values = { "See or Upload the Log" })
    String jd_gui_swing_jdgui_views_log_tab_tooltip();

    @Default(lngs = { "en" }, values = { "Hoster %s1" })
    String jd_gui_swing_menu_HosterMenu(Object s1);

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

    @Default(lngs = { "en" }, values = { "Chart is loading or not available" })
    String plugins_config_premium_chartapi_caption_error2();

    @Default(lngs = { "en" }, values = { "List of all HTAccess passwords. Each line one password." })
    String plugins_http_htaccess();

    @Default(lngs = { "en" }, values = { "List of all passwords. Each line one password." })
    String plugins_optional_jdunrar_config_passwordlist();

    @Default(lngs = { "en" }, values = { "Choose Plugin" })
    String pluginsettings_combo_label();

    @Default(lngs = { "en" }, values = { "Account" })
    String premiumaccounttablemodel_account();

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

    @Default(lngs = { "en" }, values = { "" })
    String premiumaccounttablemodel_column_trafficleft_invalid();

    @Default(lngs = { "en" }, values = { "" })
    String premiumaccounttablemodel_column_trafficleft_unchecked();

    @Default(lngs = { "en" }, values = { "Unlimited" })
    String premiumaccounttablemodel_column_trafficleft_unlimited();

    @Default(lngs = { "en" }, values = { "Username" })
    String premiumaccounttablemodel_column_user();

    @Default(lngs = { "en" }, values = { "Minimize" })
    String ProgressControllerDialog_minimize();

    @Default(lngs = { "en" }, values = { "Max repeats (-1 = no limit)" })
    String reconnect_retries();

    @Default(lngs = { "en" }, values = { "Timeout for ip change [sec]" })
    String reconnect_waitforip();

    @Default(lngs = { "en" }, values = { "First IP check wait time (sec)" })
    String reconnect_waittimetofirstipcheck();

    @Default(lngs = { "en" }, values = { "Add" })
    String settings_accountmanager_add();

    @Default(lngs = { "en" }, values = { "Buy" })
    String settings_accountmanager_buy();

    @Default(lngs = { "en" }, values = { "Delete" })
    String settings_accountmanager_delete();

    @Default(lngs = { "en" }, values = { "Account Information" })
    String settings_accountmanager_info();

    @Default(lngs = { "en" }, values = { "Premiumzone" })
    String settings_accountmanager_premiumzone();

    @Default(lngs = { "en" }, values = { "Refresh" })
    String settings_accountmanager_refresh();

    @Default(lngs = { "en" }, values = { "Renew / Buy new Account" })
    String settings_accountmanager_renew();

    @Default(lngs = { "en" }, values = { "Add" })
    String settings_auth_add();

    @Default(lngs = { "en" }, values = { "Delete" })
    String settings_auth_delete();

    @Default(lngs = { "en" }, values = { "Others" })
    String settings_linkgrabber_filter_others();

    @Default(lngs = { "en" }, values = { "Add" })
    String settings_linkgrabber_filter_action_add();

    @Default(lngs = { "en" }, values = { "Disable all" })
    String settings_linkgrabber_filter_action_all();

    @Default(lngs = { "en" }, values = { "Disable selected" })
    String settings_linkgrabber_filter_action_disable();

    @Default(lngs = { "en" }, values = { "Enable selected" })
    String settings_linkgrabber_filter_action_enable();

    @Default(lngs = { "en" }, values = { "Enable all" })
    String settings_linkgrabber_filter_action_enable_all();

    @Default(lngs = { "en" }, values = { "Delete" })
    String settings_linkgrabber_filter_action_remove();

    @Default(lngs = { "en" }, values = { "Linkfilter test on %s1" })
    String settings_linkgrabber_filter_action_test_title(String filter);

    @Default(lngs = { "en" }, values = { "Blacklist (Ignore the following links. Allow all others)" })
    String settings_linkgrabber_filter_blackorwhite_black();

    @Default(lngs = { "en" }, values = { "If you use a Blacklist, you can set up rules for links you do not want to download here. All links matching the following rules will not be grabbed by JDownloader." })
    String settings_linkgrabber_filter_blackorwhite_black_description();

    @Default(lngs = { "en" }, values = { "Whitelist (Allow the following links. Ignore all others)" })
    String settings_linkgrabber_filter_blackorwhite_white();

    @Default(lngs = { "en" }, values = { "If you use a Whitelist, JDownloader will not grab any links at all by default. You have to define rules for each linktyp you want to download. A Whitelist needs at least one entry to get active. \r\nOnly the following links will be accepted:" })
    String settings_linkgrabber_filter_blackorwhite_white_description();

    @Default(lngs = { "en" }, values = { "Advanced Mode (Filter is a full Regular Expression)" })
    String settings_linkgrabber_filter_columns_advanced();

    @Default(lngs = { "en" }, values = { "Action" })
    String settings_linkgrabber_filter_columns_blackwhite();

    @Default(lngs = { "en" }, values = { "contains not" })
    String settings_linkgrabber_filter_columns_blackwhite_contains_not();

    @Default(lngs = { "en" }, values = { "contains" })
    String settings_linkgrabber_filter_columns_blackwhite_contains();

    @Default(lngs = { "en" }, values = { "Case sensitivy (Filter is case sensitive)" })
    String settings_linkgrabber_filter_columns_case();

    @Default(lngs = { "en" }, values = { "Enable/Disable" })
    String settings_linkgrabber_filter_columns_enabled();

    @Default(lngs = { "en" }, values = { "QuickFilter Enable/Disable" })
    String settings_linkgrabber_filter_columns_quick_enabled();

    @Default(lngs = { "en" }, values = { "Filter" })
    String settings_linkgrabber_filter_columns_regex();

    @Default(lngs = { "en" }, values = { "Link Type" })
    String settings_linkgrabber_filter_columns_type();

    @Default(lngs = { "en" }, values = { "Enter any URL to do a filter test run..." })
    String settings_linkgrabber_filter_test_helpurl();

    @Default(lngs = { "en" }, values = { "Filename" })
    String settings_linkgrabber_filter_types_filename();

    @Default(lngs = { "en" }, values = { "Host" })
    String settings_linkgrabber_filter_types_plugin();

    @Default(lngs = { "en" }, values = { "Link (URL)" })
    String settings_linkgrabber_filter_types_url();

    @Default(lngs = { "en" }, values = { "Are you sure that you want to exit JDownloader?" })
    String sys_ask_rlyclose();

    @Default(lngs = { "en" }, values = { "Are you sure that you want to restart JDownloader?" })
    String sys_ask_rlyrestart();

    @Default(lngs = { "en" }, values = { "This will restart JDownloader and do a FULL-Update. Continue?" })
    String sys_ask_rlyrestore();

    @Default(lngs = { "en" }, values = { "Upload of logfile failed!" })
    String sys_warning_loguploadfailed();

    @Default(lngs = { "en" }, values = { "If the file already exists:" })
    String system_download_triggerfileexists();

    @Default(lngs = { "en" }, values = { "Ask for each file" })
    String system_download_triggerfileexists_ask();

    @Default(lngs = { "en" }, values = { "Ask for each package" })
    String system_download_triggerfileexists_askpackage();

    @Default(lngs = { "en" }, values = { "Overwrite" })
    String system_download_triggerfileexists_overwrite();

    @Default(lngs = { "en" }, values = { "Auto rename" })
    String system_download_triggerfileexists_rename();

    @Default(lngs = { "en" }, values = { "Skip Link" })
    String system_download_triggerfileexists_skip();

    @Default(lngs = { "en" }, values = { ".*(error|failed).*" })
    String userio_errorregex();

    @Default(lngs = { "en" }, values = { "Please enter!" })
    String userio_input_title();

    @Default(lngs = { "en" }, values = { "is" })
    String settings_linkgrabber_filter_columns_blackwhite_is();

    @Default(lngs = { "en" }, values = { "is not" })
    String settings_linkgrabber_filter_columns_blackwhite_is_not();

    @Default(lngs = { "en" }, values = { "Direct (No Proxy)" })
    String gui_column_host_no_proxy();

    @Default(lngs = { "en" }, values = { "%s1 (direct)" })
    String gui_column_host_direct(String host);

    @Default(lngs = { "en" }, values = { "No Proxy" })
    String gui_column_proxytype_no_proxy();

    @Default(lngs = { "en" }, values = { "Direct WAN IP" })
    String gui_column_proxytype_wanip();

    @Default(lngs = { "en" }, values = { "HTTP Proxy" })
    String gui_column_proxytype_http();

    @Default(lngs = { "en" }, values = { "Socks 5 Proxy" })
    String gui_column_proxytype_socks5();

    @Default(lngs = { "en" }, values = { "Use for Proxy Rotation" })
    String gui_column_proxytype_rotation_check();

    @Default(lngs = { "en" }, values = { "Set Defaultproxy here" })
    String gui_column_proxytype_default();

    @Default(lngs = { "en" }, values = { "Direct Gateway" })
    String gui_column_proxytype_direct();

    @Default(lngs = { "en" }, values = { "If you have several external gateways, you can use each like a different external ip." })
    String gui_column_proxytype_direct_tt();

    @Default(lngs = { "en" }, values = { "No Proxy! Use the default direct connection." })
    String gui_column_proxytype_no_proxy_tt();

    @Default(lngs = { "en" }, values = { "Hypertext Transfer Protocol (HTTP Proxy)" })
    String gui_column_proxytype_http_tt();

    @Default(lngs = { "en" }, values = { "SOCKS-5-Protocol Proxy Server " })
    String gui_column_proxytype_socks_tt();

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

    @Default(lngs = { "en" }, values = { "Enter Password..." })
    String jd_gui_swing_components_AccountDialog_help_password();

    @Default(lngs = { "en" }, values = { "Really remove %s1 account(s)?" })
    String account_remove_action_title(int num);

    @Default(lngs = { "en" }, values = { "Really remove %s1" })
    String account_remove_action_msg(String string);

    @Default(lngs = { "en" }, values = { "This value is not allowed for %s1.%s2!\r\n%s3" })
    String AdvancedConfigmanager_error_validator(String configname, String key, String error);

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

    @Default(lngs = { "en" }, values = { "Bytes Left" })
    String RemainingColumn_RemainingColumn();

    @Default(lngs = { "en" }, values = { "Priority" })
    String PriorityColumn_PriorityColumn();

    @Default(lngs = { "en" }, values = { "Bytes Loaded" })
    String LoadedColumn_LoadedColumn();

    @Default(lngs = { "en" }, values = { "Download Order" })
    String ListOrderIDColumn_ListOrderIDColumn();

    @Default(lngs = { "en" }, values = { "Hoster" })
    String HosterColumn_HosterColumn();

    @Description("HTML can be used here")
    @Default(lngs = { "en" }, values = { "<u>Package: %s1</u><br><br>" })
    String HosterColumn_getToolTip_packagetitle(String name);

    @Description("HTML can be used here")
    @Default(lngs = { "en" }, values = { "<u>Link: %s1</u><br><br>" })
    String HosterColumn_getToolTip_linktitle(String name);

    @Description("HTML can be used here")
    @Default(lngs = { "en" }, values = { "<b>%s1</b><br>" })
    String HosterColumn_getToolTip_hoster(String hoster);

    @Description("HTML can be used here")
    @Default(lngs = { "en" }, values = { "<img src=\"%s1\"></img>&nbsp;&nbsp;&nbsp;&nbsp;<b>%s2</b><br>" })
    String HosterColumn_getToolTip_hoster_img(URL url, String hoster);

    @Default(lngs = { "en" }, values = { "Active Task" })
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

    @Default(lngs = { "en" }, values = { "Top" })
    String BottomBar_BottomBar_totop();

    @Default(lngs = { "en" }, values = { "Move selected Links & Packages to top" })
    String BottomBar_BottomBar_totop_tooltip();

    @Default(lngs = { "en" }, values = { "Up" })
    String BottomBar_BottomBar_moveup();

    @Default(lngs = { "en" }, values = { "Move selected Links & Packages up" })
    String BottomBar_BottomBar_moveup_tooltip();

    @Default(lngs = { "en" }, values = { "Down" })
    String BottomBar_BottomBar_movedown();

    @Default(lngs = { "en" }, values = { "Move selected Links & Packages down" })
    String BottomBar_BottomBar_movedown_tooltip();

    @Default(lngs = { "en" }, values = { "Bottom" })
    String BottomBar_BottomBar_tobottom();

    @Default(lngs = { "en" }, values = { "Move selected Links & Packages to bottom" })
    String BottomBar_BottomBar_tobottom_tooltip();

    @Default(lngs = { "en" }, values = { "Connection" })
    String ConnectionColumn_ConnectionColumn();

    @Default(lngs = { "en" }, values = { "%s1" })
    String ConnectionColumn_getStringValue_pluginonly(HTTPProxy currentProxy);

    @Default(lngs = { "en" }, values = { "%s2/%s3 Chunks %s1" })
    String ConnectionColumn_getStringValue_withchunks(HTTPProxy currentProxy, int currentChunks, int maxChunks);

    @Default(lngs = { "en" }, values = { "%s1 download(s) running (%s2 connections)" })
    String BottomBar_actionPerformed_running_downloads(int activeDownloads, int incommingConnections);

    @Default(lngs = { "en" }, values = { "Sorted View!" })
    String sorted();

    @Default(lngs = { "en" }, values = { "Click Column Title to undo." })
    String sorted_tiny();

    @Default(lngs = { "en" }, values = { "Sorted by '%s1'-Column" })
    String DownloadsTable_actionPerformed_sortwarner_title(String column);

    @Default(lngs = { "en" }, values = { "Your Download list is not in download order any more. \r\nClick twice on the highlighted column header,\r\nto return to default (Top-Down) order." })
    String DownloadsTable_actionPerformed_sortwarner_text();

    @Default(lngs = { "en" }, values = { "'%s1' - Package" })
    String PackagePropertiesDialog_PackagePropertiesDialog(String name);

    @Default(lngs = { "en" }, values = { "Tip: Renaming Packages" })
    String DownloadsTable_editCellAt_filepackage_title();

    @Default(lngs = { "en" }, values = { "Renaming a Package is very simple. \r\nJust double click on a Package, enter the new name, and confim with <ENTER>" })
    String DownloadsTable_editCellAt_filepackage_msg();

    @Default(lngs = { "en" }, values = { "Comment" })
    String CommentColumn_CommentColumn_();

    @Default(lngs = { "en" }, values = { "Save" })
    String PackagePropertiesDialog_PackagePropertiesDialog_save();

    @Default(lngs = { "en" }, values = { "Name" })
    String PackagePropertiesDialog_layoutDialogContent_name();

    @Default(lngs = { "en" }, values = { "Comment" })
    String PackagePropertiesDialog_layoutDialogContent_comment();

    @Default(lngs = { "en" }, values = { "Archive Passwords" })
    String PackagePropertiesDialog_layoutDialogContent_passwords();

    @Default(lngs = { "en" }, values = { "Extract after download" })
    String PackagePropertiesDialog_layoutDialogContent_extract();

    @Default(lngs = { "en" }, values = { "New Password for %s1" })
    String PackagePropertiesDialog_actionPerformed_newpassword_(String name);

    @Default(lngs = { "en" }, values = { "Save to" })
    String PackagePropertiesDialog_layoutDialogContent_downloadfolder();

    @Default(lngs = { "en" }, values = { "Create Folder" })
    String EditLinkOrPackageAction_showFilePackageDialog_downloaddir_doesnotexist_();

    @Default(lngs = { "en" }, values = { "Download Directory %s1 does not exist." })
    String EditLinkOrPackageAction_showFilePackageDialog_downloaddir_doesnotexist_msg(String path);

    @Default(lngs = { "en" }, values = { "Create it" })
    String EditLinkOrPackageAction_showFilePackageDialog_create();

    @Default(lngs = { "en" }, values = { "Link: '%s1'" })
    String LinkPropertiesDialog_LinkPropertiesDialog(String name);

    @Default(lngs = { "en" }, values = { "Save" })
    String LinkPropertiesDialog_LinkPropertiesDialog_save();

    @Default(lngs = { "en" }, values = { "Filename" })
    String LinkPropertiesDialog_layoutDialogContent_name();

    @Default(lngs = { "en" }, values = { "Link Comment" })
    String LinkPropertiesDialog_layoutDialogContent_comment();

    @Default(lngs = { "en" }, values = { "Md5 Checksum" })
    String LinkPropertiesDialog_layoutDialogContent_md5();

    @Default(lngs = { "en" }, values = { "Sha1 Checksum" })
    String LinkPropertiesDialog_layoutDialogContent_sha1();

    @Default(lngs = { "en" }, values = { "Checksum Test" })
    String LinkPropertiesDialog_focusGained_();

    @Default(lngs = { "en" }, values = { "Checksums are used to find download errors (crc errors).\r\nDo not enter invalid checksums here, or your download will fail." })
    String LinkPropertiesDialog_focusGained_msg();

    @Default(lngs = { "en" }, values = { "Reset Filename to default" })
    String LinkPropertiesDialog_layoutDialogContent_reset_filename_tt();

    @Default(lngs = { "en" }, values = { "Download Password" })
    String LinkPropertiesDialog_layoutDialogContent_downloadpassword();

    @Default(lngs = { "en" }, values = { "%s1" })
    String LinkPropertiesDialog_layoutDialogContent_size(String formatBytes);

    @Default(lngs = { "en" }, values = { "%s1" })
    String LinkPropertiesDialog_layoutDialogContent_hoster(String host);

    @Default(lngs = { "en" }, values = { "Online -  Download is possible" })
    String LinkPropertiesDialog_layoutDialogContent_available();

    @Default(lngs = { "en" }, values = { "Offline -  Download not available" })
    String LinkPropertiesDialog_layoutDialogContent_notavalable();

    @Default(lngs = { "en" }, values = { "Onlinestatus not checked" })
    String LinkPropertiesDialog_layoutDialogContent_unchecked();

    @Default(lngs = { "en" }, values = { "Click here to add this link or package to your download list." })
    String ConfirmAction_ConfirmAction_tooltip();

    @Default(lngs = { "en" }, values = { "Views" })
    String LinkGrabberSideBarHeader_LinkGrabberSideBarHeader();

    @Default(lngs = { "en" }, values = { "Add Downloads" })
    String AddLinksAction_();

    @Default(lngs = { "en" }, values = { "Restore %s1 filtered Links" })
    String RestoreFilteredLinksAction_(int x);

    @Default(lngs = { "en" }, values = { "Clears the list" })
    String ClearAction_tt_();

    @Default(lngs = { "en" }, values = { "Add to Download List" })
    String ConfirmAction_ConfirmAction_();

    @Default(lngs = { "en" }, values = { "Hoster" })
    String LinkGrabberSidebar_LinkGrabberSidebar_hosterfilter();

    @Default(lngs = { "en" }, values = { "File Types" })
    String LinkGrabberSidebar_LinkGrabberSidebar_extensionfilter();

    @Default(lngs = { "en" }, values = { "Customized" })
    String LinkGrabberSidebar_LinkGrabberSidebar_customfilter();

    @Default(lngs = { "en" }, values = { "Quick Settings" })
    String LinkGrabberSidebar_LinkGrabberSidebar_settings();

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
    String ClearAction_actionPerformed_();

    @Default(lngs = { "en" }, values = { "Do you really want to remove all links from Linkgrabber?" })
    String ClearAction_actionPerformed_msg();

    @Default(lngs = { "en" }, values = { "Do you really want to remove all offline links from Linkgrabber?" })
    String ClearAction_actionPerformed_offline_msg();

    @Default(lngs = { "en" }, values = { "Do you really want to remove all selected links from Linkgrabber?" })
    String ClearAction_actionPerformed_selected_msg();

    @Default(lngs = { "en" }, values = { "Do you really want to remove all not-selected links from Linkgrabber?" })
    String ClearAction_actionPerformed_notselected_msg();

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

    @Default(lngs = { "en" }, values = { "Force Download now!" })
    String ConfirmAction_ConfirmAction_forced();

    @Default(lngs = { "en" }, values = { "Force Download now!" })
    String ConfirmAllAction_ConfirmAllAction_force();

    @Default(lngs = { "en" }, values = { "Add to Download List" })
    String ConfirmAllAction_ConfirmAllAction_();

    @Default(lngs = { "en" }, values = { "Add this Link or Package to Download List" })
    String ConfirmSingleNodeAction_ConfirmSingleNodeAction_tt();

    @Default(lngs = { "en" }, values = { "Analyse and Add Links" })
    String AddLinksDialog_AddLinksDialog_();

    @Default(lngs = { "en" }, values = { "Continue" })
    String AddLinksDialog_AddLinksDialog_confirm();

    @Default(lngs = { "en" }, values = { "JDownloader helps you to parse text or websites for links. Enter Links, URLs, Websites, or text below, \r\nchoose a Download Destination, and click 'Start Analyse'. \r\nAll 'downloadable' Files will be listed in the the Linkgrabber view afterwards." })
    String AddLinksDialog_layoutDialogContent_description();

    @Default(lngs = { "en" }, values = { "Enter Links, URLs, Websites, or any other text here..." })
    String AddLinksDialog_layoutDialogContent_input_help();

    @Default(lngs = { "en" }, values = { "Toggle Help Text visibility" })
    String AddLinksDialog_mouseClicked();

    @Default(lngs = { "en" }, values = { "Please choose Download Destination here." })
    String AddLinksDialog_layoutDialogContent_save_tt();

    @Default(lngs = { "en" }, values = { "Browse" })
    String literally_browse();

    @Default(lngs = { "en" }, values = { "Choose a Package Name for the Downloads above. If empty, JDownloader will create Packages based on the filenames" })
    String AddLinksDialog_layoutDialogContent_package_tt();

    @Default(lngs = { "en" }, values = { "Enter a Package name, or leave empty for auto mode" })
    String AddLinksDialog_layoutDialogContent_packagename_help();

    @Default(lngs = { "en" }, values = { "Archives" })
    String AddLinksDialog_layoutDialogContent_Extractionheader();

    @Default(lngs = { "en" }, values = { "Extract after Download" })
    String AddLinksDialog_createExtracOptionsPanel_autoextract();

    @Default(lngs = { "en" }, values = { "Enter the archive's extraction password" })
    String AddLinksDialog_createExtracOptionsPanel_password();

    @Default(lngs = { "en" }, values = { "Extract" })
    String AddLinksDialog_layoutDialogContent_autoextract();

    @Default(lngs = { "en" }, values = { "Enable this option to extract all found archives after download" })
    String AddLinksDialog_layoutDialogContent_autoextract_tooltip();

    @Default(lngs = { "en" }, values = { "Disable this option if you do not want to extract the archives after download" })
    String AddLinksDialog_layoutDialogContent_autoextract_tooltip_enabled();

    @Default(lngs = { "en" }, values = { "Start Deep Link Analyse" })
    String ConfirmOptionsAction_actionPerformed_deep();

    @Default(lngs = { "en" }, values = { "Start Normal Link Analyse" })
    String ConfirmOptionsAction_actionPerformed_normale();

    @Default(lngs = { "en" }, values = { "Save files to" })
    String AddLinksDialog_actionPerformed_browse();

    @Default(lngs = { "en" }, values = { "Please enter Urls, Links, Websites, or plain text" })
    String AddLinksDialog_validateForm_input_missing();

    @Default(lngs = { "en" }, values = { "Please make sure to enter a valid Download Destination." })
    String AddLinksDialog_validateForm_folder_invalid_missing();

    @Default(lngs = { "en" }, values = { "Please enter a valid Download Folder" })
    String AddLinksDialog_layoutDialogContent_help_destination();

    @Default(lngs = { "en" }, values = { "Create Folder" })
    String AddLinksDialog_layoutDialogContent_mkdir();

    @Default(lngs = { "en" }, values = { "Creating Folder failed" })
    String AddLinksDialog_actionPerformed_mkdir_failed();

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

    @Default(lngs = { "en" }, values = { "Reconnect Wizard" })
    String ReconnectPluginController_autoFind_();

    @Default(lngs = { "en" }, values = { "This wizard will help you to find the correct reconnection settings for your internet connection.\r\nYou can start this wizard at any later time from the file->settings->reconection panel.\r\nThis Wizard will try to perform several Reconnects to optimize your settings.\r\n\r\nContinue now?" })
    String ReconnectPluginController_autoFind_msg();

    @Default(lngs = { "en" }, values = { "Read more..." })
    String literally_read_more();

    @Default(lngs = { "en" }, values = { "...testing %s1" })
    String ReconnectPluginController_run_testing(String name);

    @Default(lngs = { "en" }, values = { "JDownloader now tries to find the correct settings to perform a reconnect.\r\nThis might take a few minutes. Please be patient!" })
    String ReconnectPluginController_autoFind_dialog_msg();

    @Default(lngs = { "en" }, values = { "Test" })
    String ReconnectTestAction();

    @Default(lngs = { "en" }, values = { "Reconnect" })
    String ReconnectDialog_ReconnectDialog_();

    @Default(lngs = { "en" }, values = { "Duration: " })
    String ReconnectDialog_layoutDialogContent_duration();

    @Default(lngs = { "en" }, values = { "Old IP: " })
    String ReconnectDialog_layoutDialogContent_old();

    @Default(lngs = { "en" }, values = { "New IP: " })
    String ReconnectDialog_layoutDialogContent_new();

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

    @Default(lngs = { "en" }, values = { "JDownloader found %s1 working Reconnect Script(s). \r\nTo improve Reconnect Timings, you should do some automatic tests runs. These tests would take about %s2. \r\nIf you skip this step, your current Reconnect Settings will need about %s3 to perform a successfull Reconnect." })
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

    @Default(lngs = { "en" }, values = { "Optimizing..." })
    String AutoDetectAction_run_optimize();

    @Default(lngs = { "en" }, values = { "Optimizing  %s1" })
    String AutoDetectAction_run_optimize(String name);

    @Default(lngs = { "en" }, values = { "Offline - Waiting for a internet connection!" })
    String LiveaheaderDetection_wait_for_online();

    @Default(lngs = { "en" }, values = { "This Wizard will scan your Network configuration, and try to find Reconnect Scripts from our online Database.\r\nThis may take between a few seconds, and several minutes.\r\nOnly Dynamic IP DSL Connections via a Router can be autodetected." })
    String AutoSetupAction_tt();

    @Default(lngs = { "en" }, values = { "Current External IP: %s1\r\n\r\nClick here do a Reconnect and get a new External IP." })
    String ReconnectTestAction_tt_(String ip);

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

    @Default(lngs = { "en" }, values = { "Name" })
    String FilterRuleDialog_layoutDialogContent_lbl_name();

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

    @Default(lngs = { "en" }, values = { "Custom" })
    String FilterRuleDialog_createTypeFilter_mime_custom();

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

    @Default(lngs = { "en" }, values = { "... then ..." })
    String FilterRuleDialog_layoutDialogContent_then();

    @Default(lngs = { "en" }, values = { "Condition Name" })
    String FilterRuleDialog_layoutDialogContent_name();

    @Default(lngs = { "en" }, values = { "Drop File or Url - no further crawling or download" })
    String FilterRuleDialog_layoutDialogContent_deny();

    @Default(lngs = { "en" }, values = { "Accept File or Url - add to linkgrabber or continue crawling" })
    String FilterRuleDialog_layoutDialogContent_accept();

    @Default(lngs = { "en" }, values = { "Action" })
    String settings_linkgrabber_filter_columns_then();

    @Default(lngs = { "en" }, values = { "add it to Linkgrabber." })
    String FilterTableModel_initColumns_accept_link();

    @Default(lngs = { "en" }, values = { "drop it." })
    String FilterTableModel_initColumns_deny_link();

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

    @Default(lngs = { "en" }, values = { "\r\n%s1 %s2" })
    String FilterRule_toString_name2(String name, String string);

    @Default(lngs = { "en" }, values = { "Hoster URL %s1" })
    String FilterRule_toString_hoster(String string);

    @Default(lngs = { "en" }, values = { "\r\nURL(%s1) %s2" })
    String FilterRule_toString_hoster2(String a, String string);

    @Default(lngs = { "en" }, values = { "Source URL %s1" })
    String FilterRule_toString_source(String string);

    @Default(lngs = { "en" }, values = { ", %s1" })
    String FilterRule_toString_comma(String string);

    @Default(lngs = { "en" }, values = { " and %s1" })
    String FilterRule_toString_and(String string);

    @Default(lngs = { "en" }, values = { "Filter Links if, " })
    String settings_linkgrabber_filter_columns_condition();

    @Default(lngs = { "en" }, values = { "Filename" })
    String FilterTableModel_initColumns_filename();

    @Default(lngs = { "en" }, values = { "Filesize" })
    String FilterTableModel_initColumns_filesize();

    @Default(lngs = { "en" }, values = { "Filetype" })
    String FilterTableModel_initColumns_filetype();

    @Default(lngs = { "en" }, values = { "Hoster" })
    String FilterTableModel_initColumns_hoster();

    @Default(lngs = { "en" }, values = { "Source" })
    String FilterTableModel_initColumns_source();

    @Default(lngs = { "en" }, values = { "Size %s1" })
    String FilterRule_toString_size(String string);

    @Default(lngs = { "en" }, values = { "\r\nSize(%s1) %s2" })
    String FilterRule_toString_size2(String string, String string2);

    @Default(lngs = { "en" }, values = { "File %s1" })
    String FilterRule_toString_type(String string);

    @Default(lngs = { "en" }, values = { "\r\n%s1-File %s2" })
    String FilterRule_toString_type2(String ext, String string);

    @Default(lngs = { "en" }, values = { "Archive" })
    String FiletypeFilter_toString_archives();

    @Default(lngs = { "en" }, values = { "Audio" })
    String FiletypeFilter_toString_audio();

    @Default(lngs = { "en" }, values = { "Image" })
    String FiletypeFilter_toString_image();

    @Default(lngs = { "en" }, values = { "Video" })
    String FiletypeFilter_toString_video();

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

    @Default(lngs = { "en" }, values = { "Delete" })
    String literally_delete();

    @Default(lngs = { "en" }, values = { "Remove" })
    String literally_remove();

    @Default(lngs = { "en" }, values = { "Download Directory" })
    String PackagizerFilterRuleDialog_layoutDialogContent_dest();

    @Default(lngs = { "en" }, values = { "Priority" })
    String PackagizerFilterRuleDialog_layoutDialogContent_priority();

    @Default(lngs = { "en" }, values = { "Package Name" })
    String PackagizerFilterRuleDialog_layoutDialogContent_packagename();

    @Default(lngs = { "en" }, values = { "Extract Archives" })
    String PackagizerFilterRuleDialog_layoutDialogContent_extract();

    @Default(lngs = { "en" }, values = { "Chunks/Connections" })
    String PackagizerFilterRuleDialog_layoutDialogContent_chunks();

    @Default(lngs = { "en" }, values = { "... then set" })
    String PackagizerFilterRuleDialog_layoutDialogContent_then();

    @Default(lngs = { "en" }, values = { "Hoster Domain" })
    String PackagizerFilterRuleDialog_createVariablesMenu_hoster();

    @Default(lngs = { "en" }, values = { "Source Domain" })
    String PackagizerFilterRuleDialog_createVariablesMenu_source();

    @Default(lngs = { "en" }, values = { "Date or Time" })
    String PackagizerFilterRuleDialog_createVariablesMenu_date();

    @Default(lngs = { "en" }, values = { "Dynamic Variables" })
    String PackagizerFilterRuleDialog_createVariablesMenu_menu();

    @Default(lngs = { "en" }, values = { "Complete Final Filename" })
    String PackagizerFilterRuleDialog_createVariablesMenu_filename_complete();

    @Default(lngs = { "en" }, values = { "Original Filename Wildcard(*) #%s1" })
    String PackagizerFilterRuleDialog_createVariablesMenu_filename(int i);

    @Default(lngs = { "en" }, values = { "Hoster Url Wildcard(*) #%s1" })
    String PackagizerFilterRuleDialog_createVariablesMenu_hoster(int i);

    @Default(lngs = { "en" }, values = { "Source Url Wildcard(*) #%s1" })
    String PackagizerFilterRuleDialog_createVariablesMenu_source(int i);

    @Default(lngs = { "en" }, values = { "Packagename" })
    String PackagizerFilterRuleDialog_createVariablesMenu_packagename();

    @Default(lngs = { "en" }, values = { "Auto extract Archives after download finished." })
    String PackagizerFilterRuleDialog_layoutDialogContent_extract_desc();

    @Default(lngs = { "en" }, values = { "Auto Start Package" })
    String PackagizerFilterRuleDialog_layoutDialogContent_autostart();

    @Default(lngs = { "en" }, values = { "Auto Accept Package" })
    String PackagizerFilterRuleDialog_layoutDialogContent_autoadd();

    @Default(lngs = { "en" }, values = { "Auto Confirm Links in Linkgrabber and add them to the Download View." })
    String PackagizerFilterRuleDialog_layoutDialogContent_add_desc();

    @Default(lngs = { "en" }, values = { "Start Downloads immediately" })
    String PackagizerFilterRuleDialog_layoutDialogContent_start_desc();

    @Default(lngs = { "en" }, values = { "If this rule matches, set download destination ..." })
    String settings_linkgrabber_filter_columns_dest();

    @Default(lngs = { "en" }, values = { "Download files to %s1." })
    String FilterTableModel_packagizer_tt_dest(String downloadDestination);

    @Default(lngs = { "en" }, values = { "If this rule matches, set Download Priority to ..." })
    String settings_linkgrabber_filter_columns_priority();

    @Default(lngs = { "en" }, values = { "If this rule matches, set Packagename to ..." })
    String settings_linkgrabber_filter_columns_packagename();

    @Default(lngs = { "en" }, values = { "Use %s1 as packagename if possible." })
    String FilterTableModel_packagizer_tt_packagename(String packageName);

    @Default(lngs = { "en" }, values = { "If this rule matches, set Chunks to ..." })
    String settings_linkgrabber_filter_columns_chunks();

    @Default(lngs = { "en" }, values = { "If this rule matches, extract archives after Download ..." })
    String settings_linkgrabber_filter_columns_extract();

    @Default(lngs = { "en" }, values = { "Extract Archives after Download" })
    String FilterTableModel_packagizer_tt_autoextract();

    @Default(lngs = { "en" }, values = { "If this rule matches, move Links to Download View automatically ..." })
    String settings_linkgrabber_filter_columns_add();

    @Default(lngs = { "en" }, values = { "Move Links do Download View automatically" })
    String FilterTableModel_packagizer_tt_autoadd();

    @Default(lngs = { "en" }, values = { "If this rule matches, start download after Link has moved to Download View..." })
    String settings_linkgrabber_filter_columns_start();

    @Default(lngs = { "en" }, values = { "Start download after Link has moved to Download View." })
    String FilterTableModel_packagizer_tt_autostart();

    @Default(lngs = { "en" }, values = { "Add Downloadlinks or import Container files" })
    String BottomBar_BottomBar_add();

    @Default(lngs = { "en" }, values = { "Clean up downloadlist" })
    String BottomBar_BottomBar_cleanup();

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

    @Default(lngs = { "en" }, values = { "Enabled" })
    String LinkgrabberFilter_LinkgrabberFilter_enable();

    @Default(lngs = { "en" }, values = { "Enabled" })
    String PackagizerFilter_PackagizerFilter_enable();

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

    @Default(lngs = { "en" }, values = { "Remove selected Hosters" })
    String DropHosterAction_DropHosterAction_();

    @Default(lngs = { "en" }, values = { "Keep only selected Hosters" })
    String KeepOnlyAction_KeepOnlyAction_();

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

    @Default(lngs = { "en" }, values = { "Enter username..." })
    String BuyAndAddPremiumAccount_layoutDialogContent_username(String name);

    @Default(lngs = { "en" }, values = { "Enter password..." })
    String BuyAndAddPremiumAccount_layoutDialogContent_pass();

    @Default(lngs = { "en" }, values = { "Max. sim. Downloads per Hoster" })
    String ParalellDownloadsEditor_ParallelDownloadsPerHostEditor_();

    @Default(lngs = { "en" }, values = { "Enable/Disable this option" })
    String AbstractConfigPanel_addPair_enabled();

    @Default(lngs = { "en" }, values = { "Enter Filename Filter Expression..." })
    String SearchField_SearchField_helptext();

    @Default(lngs = { "en" }, values = { "Start Downloads" })
    String ConfirmAction_ConfirmAction_context_add_and_start();

    @Default(lngs = { "en" }, values = { "Add to Download List" })
    String ConfirmAction_ConfirmAction_context_add();

    @Default(lngs = { "en" }, values = { "Open in Browser" })
    String OpenUrlAction_OpenUrlAction_();

    @Default(lngs = { "en" }, values = { "Link/File" })
    String ContextMenuFactory_createPopup_link();

    @Default(lngs = { "en" }, values = { "Edit URL" })
    String EditUrlAction_EditUrlAction_();

    @Default(lngs = { "en" }, values = { "Edit Filename" })
    String EditFilenameAction_EditFilenameAction_();

    @Default(lngs = { "en" }, values = { "Package" })
    String ContextMenuFactory_createPopup_pkg();

    @Default(lngs = { "en" }, values = { "Rename Package" })
    String RenamePackageAction_RenamePackageAction_();

    @Default(lngs = { "en" }, values = { "Save to" })
    String LinkGrabberTableModel_initColumns_folder();

    @Default(lngs = { "en" }, values = { "Download from" })
    String LinkGrabberTableModel_initColumns_url();

    @Default(lngs = { "en" }, values = { "Click to open Url in browser" })
    String UrlColumn_UrlColumn_open_tt_();

    @Default(lngs = { "en" }, values = { "Click to choose a new Download Folder" })
    String DownloadFolderColiumn_edit_tt_();

    @Default(lngs = { "en" }, values = { "Click here to open the Download Folder" })
    String DownloadFolderColiumn_open_tt_();

    @Default(lngs = { "en" }, values = { "Move to new Package" })
    String MergeToPackageAction_MergeToPackageAction_();

    @Default(lngs = { "en" }, values = { "Check online status" })
    String FileCheckAction_FileCheckAction_();

    @Default(lngs = { "en" }, values = { "Download Directory" })
    String ContextMenuFactory_createPopup_downloaddir();

    @Default(lngs = { "en" }, values = { "Choose Download Directory" })
    String SetDownloadFolderAction_SetDownloadFolderAction_();

    @Default(lngs = { "en" }, values = { "Open Download Directory" })
    String OpenDownloadFolderAction_OpenDownloadFolderAction_();

    @Default(lngs = { "en" }, values = { "Enabled" })
    String EnabledAction_EnabledAction_object_();

    @Default(lngs = { "en" }, values = { "Set Download Password" })
    String SetDownloadPassword_SetDownloadPassword_();

    @Default(lngs = { "en" }, values = { "Download Password" })
    String DownloadPasswordColumn_DownloadPasswordColumn_object_();

    @Default(lngs = { "en" }, values = { "Remove" })
    String ContextMenuFactory_createPopup_cleanup();

    @Default(lngs = { "en" }, values = { "Offline Links" })
    String RemoveOfflineAction_RemoveOfflineAction_object_();

    @Default(lngs = { "en" }, values = { "All Links" })
    String RemoveAllAction_RemoveAllAction_object_();

    @Default(lngs = { "en" }, values = { "Incomplete Split-Archives" })
    String RemoveIncompleteArchives_RemoveIncompleteArchives_object_();

    @Default(lngs = { "en" }, values = { "Selected Links" })
    String RemoveSelectionAction_RemoveSelectionAction_object_();

    @Default(lngs = { "en" }, values = { "Keep only selected Links" })
    String RemoveNonSelectedAction_RemoveNonSelectedAction_object_();

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

    @Default(lngs = { "en" }, values = { "Filtered" })
    String ResultTableModel_initColumns_status_();

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

    @Default(lngs = { "en" }, values = { "Show %s1 offline Links" })
    String LinkGrabberPanel_LinkGrabberPanel_offline_(int i);

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

    @Default(lngs = { "en" }, values = { "Show/Hide" })
    String EnabledAllAction_EnabledAllAction_object_();

    @Default(lngs = { "en" }, values = { "Hide All" })
    String DisableAllAction_DisableAllAction_object_();

    @Default(lngs = { "en" }, values = { "%s1 - Views" })
    String Header_setEnabled_enabled_tt(String string);

    @Default(lngs = { "en" }, values = { "No %s1 - Views available" })
    String Header_setEnabled_disabled_tt(String text);

    @Default(lngs = { "en" }, values = { "Custom Views" })
    String CustomFilterHeader_setEnabled_enabled_tt(String text);

    @Default(lngs = { "en" }, values = { "No custom Views available" })
    String CustomFilterHeader_setEnabled_disabled_tt(String text);

    @Default(lngs = { "en" }, values = { "Choose Icon for this Filter" })
    String ConditionDialog_layoutDialogContent_object_();

    @Default(lngs = { "en" }, values = { "Show/Hide Sidebar" })
    String LinkGrabberPanel_LinkGrabberPanel_btn_showsidebar_tt_up();

    @Default(lngs = { "en" }, values = { "Sidebar Hidden!" })
    String LinkGrabberPanel_onConfigValueModified_title_();

    @Default(lngs = { "en" }, values = { "The Sidebar is hidden now. Click again to show it.\r\n\r\nWARNING: Views and Settings are still active! Check advanced Settings to disable the sidebar and all it's features completely." })
    String LinkGrabberPanel_onConfigValueModified_msg_();

    @Default(lngs = { "en" }, values = { "Show Selection" })
    String EnabledAllAction_EnabledAllAction_object_show();

    @Default(lngs = { "en" }, values = { "Hide Selection" })
    String EnabledAllAction_EnabledAllAction_object_hide();

    @Default(lngs = { "en" }, values = { "Filename" })
    String PackagizerFilterRuleDialog_layoutDialogContent_filename();

    @Default(lngs = { "en" }, values = { "Enter Package Name Pattern..." })
    String PackagizerFilterRuleDialog_layoutDialogContent_packagename_help_();

    @Default(lngs = { "en" }, values = { "Enter Filename Pattern..." })
    String PackagizerFilterRuleDialog_layoutDialogContent_filename_help_();

    @Default(lngs = { "en" }, values = { "Enter absolute or relative Path..." })
    String PackagizerFilterRuleDialog_layoutDialogContent_dest_help();

    @Default(lngs = { "en" }, values = { "Original Filename" })
    String PackagizerFilterRuleDialog_createVariablesMenu_filename_org();

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

    @Default(lngs = { "en" }, values = { "Regular Expressions" })
    String ConditionDialog_actionPerformed_regex_();

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

    @Default(lngs = { "en" }, values = { "New Version found!" })
    String JDUpdater_start_updater_update_title();

    @Default(lngs = { "en" }, values = { "JDownloader has to restart now to perform an update." })
    String JDUpdater_start_updater_update_msg();

    @Default(lngs = { "en" }, values = { "Update & Restart now!" })
    String JDUpdater_start_restart_update_now_();

    @Default(lngs = { "en", }, values = { "%s1 update(s) are ready for installation. A Restart is required to complete installation." })
    String updates_are_ready_for_install_now(int size);

    @Default(lngs = { "en", }, values = { "%s1 update(s) are ready for installation. A Restart is required to complete installation." })
    String updates_ready_for_install(int i);

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

    @Default(lngs = { "en" }, values = { "Validate Split Archives" })
    String ValidateArchiveAction_ValidateArchiveAction_object_();

    @Default(lngs = { "en" }, values = { "%s1/%s2 online" })
    String AvailabilityColumn_getStringValue_object_(int i, int size);

    @Default(lngs = { "en" }, values = { "Priority" })
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

    @Default(lngs = { "en" }, values = { "Test" })
    String PackagizerFilterRuleDialog_PackagizerFilterRuleDialog_testbutton_();

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

    @Default(lngs = { "en" }, values = { "~ not set ~" })
    String PackagizerSingleTestTableModel_getStringValue_notset_();

    @Default(lngs = { "en" }, values = { "Priority" })
    String PackagizerSingleTestTableModel_initColumns_priority_();

    @Default(lngs = { "en" }, values = { "Package Name" })
    String PackagizerSingleTestTableModel_initColumns_packagename_();

    @Default(lngs = { "en" }, values = { "File Name" })
    String PackagizerSingleTestTableModel_initColumns_filename_();

    @Default(lngs = { "en" }, values = { "File Name" })
    String searchcategory_filename();

    @Default(lngs = { "en" }, values = { "Hoster" })
    String searchcategory_hoster();

    @Default(lngs = { "en" }, values = { "Package Name" })
    String searchcategory_package();

    @Default(lngs = { "en" }, values = { "Running Downloads" })
    String downloadview_running();

    @Default(lngs = { "en" }, values = { "All Downloads" })
    String downloadview_all();

    @Default(lngs = { "en" }, values = { "Failed Downloads" })
    String downloadview_failed();

    @Default(lngs = { "en" }, values = { "Successful Downloads" })
    String downloadview_successful();

    @Default(lngs = { "en" }, values = { "Please enter the file name you are looking for..." })
    String searchcategory_filename_help();

    @Default(lngs = { "en" }, values = { "Please enter the domain you are looking for..." })
    String searchcategory_hoster_help();

    @Default(lngs = { "en" }, values = { "Please enter the package name you are looking for..." })
    String searchcategory_package_help();

    @Default(lngs = { "en" }, values = { "Choose a View to filter the download list..." })
    String PseudoCombo_PseudoCombo_tt_();

}