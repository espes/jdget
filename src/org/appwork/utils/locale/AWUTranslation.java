/**
 * Copyright (c) 2009 - 2011 AppWork UG(haftungsbeschränkt) <e-mail@appwork.org>
 * 
 * This file is part of org.appwork.utils.locale
 * 
 * This software is licensed under the Artistic License 2.0,
 * see the LICENSE file or http://www.opensource.org/licenses/artistic-license-2.0.php
 * for details
 */
package org.appwork.utils.locale;

import org.appwork.txtresource.Default;
import org.appwork.txtresource.Defaults;
import org.appwork.txtresource.TranslateInterface;

/**
 * @author thomas
 * 
 */
@Defaults(lngs = { "en", "de" })
public interface AWUTranslation extends TranslateInterface {

    @Default(lngs = { "en", "de" }, values = { "Cancel", "Abbrechen" })
    String ABSTRACTDIALOG_BUTTON_CANCEL();

    @Default(lngs = { "en", "de" }, values = { "Ok", "Ok" })
    String ABSTRACTDIALOG_BUTTON_OK();

    @Default(lngs = { "en", "de" }, values = { "Don't show this again", "Nicht mehr anzeigen" })
    String ABSTRACTDIALOG_STYLE_SHOW_DO_NOT_DISPLAY_AGAIN();

    @Default(lngs = { "en", "de" }, values = { "Please enter your logins here", "Bitte geben Sie hier Ihre Zugangsdaten ein" })
    String AccountNew_AccountNew_message();

    @Default(lngs = { "en", "de" }, values = { "Enter Logins", "Zugangsdaten eingeben" })
    String AccountNew_AccountNew_title();

    @Default(lngs = { "en", "de" }, values = { "Username", "Benutzername" })
    String AccountNew_layoutDialogContent_accountname();

    @Default(lngs = { "en", "de" }, values = { "Password", "Passwort" })
    String AccountNew_layoutDialogContent_password();

    @Default(lngs = { "en", "de" }, values = { "Remember", "Merken?" })
    String AccountNew_layoutDialogContent_save();

    @Default(lngs = { "en", "de" }, values = { "Enabled", "Aktiviert" })
    String active();

    @Default(lngs = { "en", "de" }, values = { "Ø %s1/s |", "Ø %s1/s |" })
    String AppWorkUtils_Graph_getAverageSpeedString(String speed);

    @Default(lngs = { "en", "de" }, values = { "Current: %s1/s", "Aktuell: %s1/s" })
    String AppWorkUtils_Graph_getSpeedString(String speed);

    @Default(lngs = { "en", "de" }, values = { "Command -> -", " Befehl -> -" })
    String COMMANDLINEAPP_COMMAND();

    @Default(lngs = { "en", "de" }, values = { "Connecting...", "Verbinden..." })
    String connecting();

    @Default(lngs = { "en", "de" }, values = { "Copy", "Kopieren" })
    String COPYCUTPASTE_COPY();

    @Default(lngs = { "en", "de" }, values = { "Cut", "Ausschneiden" })
    String COPYCUTPASTE_CUT();

    @Default(lngs = { "en", "de" }, values = { "Delete", "Löschen" })
    String COPYCUTPASTE_DELETE();

    @Default(lngs = { "en", "de" }, values = { "Paste", "Einfügen" })
    String COPYCUTPASTE_PASTE();

    @Default(lngs = { "en", "de" }, values = { "Select", "Markieren" })
    String COPYCUTPASTE_SELECT();

    @Default(lngs = { "en", "de" }, values = { "Please open this link in your browser: ", "Bitte öffnen Sie diesen Link in Ihrem Browser:" })
    String crossSystem_open_url_failed_msg();

    @Default(lngs = { "en", "de" }, values = { "Please confirm!", "Bitte bestätigen!" })
    String DIALOG_CONFIRMDIALOG_TITLE();

    @Default(lngs = { "en", "de" }, values = { "Error Occured", "Ein Fehler ist aufgetreten" })
    String DIALOG_ERROR_TITLE();

    @Default(lngs = { "en", "de" }, values = { "Switch to detailed view", "Detailansicht" })
    String DIALOG_FILECHOOSER_TOOLTIP_DETAILS();

    @Default(lngs = { "en", "de" }, values = { "Switch to Home", "Zum Benutzerverzeichnis wechseln" })
    String DIALOG_FILECHOOSER_TOOLTIP_HOMEFOLDER();

    @Default(lngs = { "en", "de" }, values = { "Switch to list view", "Listenansicht" })
    String DIALOG_FILECHOOSER_TOOLTIP_LIST();

    @Default(lngs = { "en", "de" }, values = { "Create new folder", "Neuen Ordner erstellen" })
    String DIALOG_FILECHOOSER_TOOLTIP_NEWFOLDER();

    @Default(lngs = { "en", "de" }, values = { "Switch to parent folder", "Einen Ordner nach oben" })
    String DIALOG_FILECHOOSER_TOOLTIP_UPFOLDER();

    @Default(lngs = { "en", "de" }, values = { "Please enter!", "Bitte eingeben!" })
    String DIALOG_INPUT_TITLE();

    @Default(lngs = { "en", "de" }, values = { "Message", "Nachricht" })
    String DIALOG_MESSAGE_TITLE();

    @Default(lngs = { "en", "de" }, values = { "Please enter!", "Bitte eingeben!" })
    String DIALOG_PASSWORD_TITLE();

    @Default(lngs = { "en", "de" }, values = { "Please enter!", "Bitte eingeben" })
    String DIALOG_SLIDER_TITLE();

    @Default(lngs = { "en", "de" }, values = { "Downloading file", "Lade Datei herunter" })
    String download_msg();

    @Default(lngs = { "en", "de" }, values = { "Download", "Download" })
    String download_title();

    @Default(lngs = { "en", "de" }, values = { "This Error is cause by:", "Grund für diesen Fehler:" })
    String ExceptionDialog_layoutDialogContent_logLabel();

    @Default(lngs = { "en", "de" }, values = { "Error details", "Fehlerdetails" })
    String ExceptionDialog_layoutDialogContent_more_button();

    @Default(lngs = { "en", "de" }, values = { "Enter Password", "Passwort eingeben" })
    String extpasswordeditorcolumn_help();

    @Default(lngs = { "en", "de" }, values = { "Your Password", "Ihr Passwort" })
    String extpasswordeditorcolumn_tooltip();

    @Default(lngs = { "en", "de" }, values = { "Search table", "Tabelle durchsuchen" })
    String EXTTABLE_SEARCH_DIALOG_TITLE();

    @Default(lngs = { "en", "de" }, values = { "Disable all", "Alle deaktivieren" })
    String extttable_disable_all();

    @Default(lngs = { "en", "de" }, values = { "Enable all", "Alle aktivieren" })
    String extttable_enabled_all();

    @Default(lngs = { "en", "de" }, values = { "Choose editor for Filetype '%s1'", "Editor für %s1-Dateien wählen" })
    String fileditcontroller_geteditor_for(String fileType);

    @Default(lngs = { "en", "de" }, values = { "Do you want to open the '%s1'-file in your default editor?", "Wollen Sie die '%s1'-Datei im Standard Editor bearbeiten?" })
    String fileeditcontroller_dialog_message(String extension);

    @Default(lngs = { "en", "de" }, values = { "Use default editor?", "Standard Programm nutzen?" })
    String fileeditcontroller_dialog_title();

    @Default(lngs = { "en", "de" }, values = { "%s1-Editor (Application)", "%s1-Editor (Anwendung)" })
    String fileeditcontroller_exechooser_description(String type);

    @Default(lngs = { "en", "de" }, values = { "Disabled", "Deaktiviert" })
    String inactive();

    @Default(lngs = { "en", "de" }, values = { "(px)W:%s1; H:%s2", "(px)B:%s1; H:%s2" })
    String Layover_size(int width, int height);

    @Default(lngs = { "en", "de" }, values = { "%s1 B", "%s1 B" })
    String literally_byte(long fileSize);

    @Default(lngs = { "en", "de" }, values = { "%s1 GB", "%s1 GB" })
    String literally_gibibyte(String format);

    @Default(lngs = { "en", "de" }, values = { "%s1 KB", "%s1 KB" })
    String literally_kibibyte(String format);

    @Default(lngs = { "en", "de" }, values = { "%s1 MB", "%s1 MB" })
    String literally_mebibyte(String format);

    @Default(lngs = { "en", "de" }, values = { "%s1 TB", "%s1 TB" })
    String literally_tebibyte(String format);

    @Default(lngs = { "en", "de" }, values = { "Lock Columnwidth", "Spaltenbreite festsetzen" })
    String LockColumnWidthAction();

    @Default(lngs = { "en", "de" }, values = { "Login", "Anmelden" })
    String LOGINDIALOG_BUTTON_LOGIN();

    @Default(lngs = { "en", "de" }, values = { "Create new User", "Neuen Benutzer anlegen" })
    String LOGINDIALOG_BUTTON_REGISTER();

    @Default(lngs = { "en", "de" }, values = { "Remember", "Merken" })
    String LOGINDIALOG_CHECKBOX_REMEMBER();

    @Default(lngs = { "en", "de" }, values = { "Password", "Passwort" })
    String LOGINDIALOG_LABEL_PASSWORD();

    @Default(lngs = { "en", "de" }, values = { "Repeat Password", "Passwort wiederholen" })
    String LOGINDIALOG_LABEL_PASSWORD_REPEAT();

    @Default(lngs = { "en", "de" }, values = { "Login", "Benutzername" })
    String LOGINDIALOG_LABEL_USERNAME();

    @Default(lngs = { "en", "de" }, values = { "No", "Nein" })
    String NO();

    @Default(lngs = { "en", "de" }, values = { "New Password:", "Neues Passwort" })
    String PASSWORDDIALOG_PASSWORDCHANGE_NEWPASSWORD();

    @Default(lngs = { "en", "de" }, values = { "Confirm Password:", "Passwort bestätigen" })
    String PASSWORDDIALOG_PASSWORDCHANGE_NEWPASSWORD_REPEAT();

    @Default(lngs = { "en", "de" }, values = { "Old Password:", "Altes Passwort" })
    String PASSWORDDIALOG_PASSWORDCHANGE_OLDPASSWORD();

    @Default(lngs = { "en", "de" }, values = { "Browse", "Auswählen" })
    String pathchooser_browselabel();

    @Default(lngs = { "en", "de" }, values = { "Choose path!", "Pfad auswählen!" })
    String pathchooser_dialog_title();

    @Default(lngs = { "en", "de" }, values = { "Enter a path...", "Bitte Pfad eingeben..." })
    String pathchooser_helptext();

    @Default(lngs = { "en", "de" }, values = { "Please wait tuntil download has finished", "Bitte warten bis der Download beendet ist" })
    String please_wait();

    @Default(lngs = { "en", "de" }, values = { "%s3 %s1/%s2", "%s3 %s1/%s2" })
    String progress(String loaded, String total, double d);

    @Default(lngs = { "en", "de" }, values = { "Direct %s1", "Direkt %s1" })
    String proxy_direct(String ip);

    @Default(lngs = { "en", "de" }, values = { "%s1:%s2 (Http Proxy)", "%s1:%s2 (Http Proxy)" })
    String proxy_http(String host, int port);

    @Default(lngs = { "en", "de" }, values = { "Direct", "Direkt" })
    String proxy_none();

    @Default(lngs = { "en", "de" }, values = { "%s1:%s2 (Socks4 Proxy)", "%s1:%s2 (Socks4 Proxy)" })
    String proxy_socks4(String host, int port);

    @Default(lngs = { "en", "de" }, values = { "%s1:%s2 (Socks5 Proxy)", "%s1:%s2 (Socks5 Proxy)" })
    String proxy_socks5(String host, int port);

    @Default(lngs = { "en", "de" }, values = { "Reset Columns", "Spalten zurücksetzen" })
    String ResetColumnsAction();

    @Default(lngs = { "en", "de" }, values = { "No options available", "Keine Auswahl verfügbar" })
    String searchbox_model_empty();

    @Default(lngs = { "en", "de" }, values = { "Search Table", "Tabelle durchsuchen" })
    String SearchContextAction();

    @Default(lngs = { "en", "de" }, values = { "Find", "Suchen" })
    String SEARCHDIALOG_BUTTON_FIND();

    @Default(lngs = { "en", "de" }, values = { "Case sensitive", "Groß/Kleinschreibung" })
    String SEARCHDIALOG_CHECKBOX_CASESENSITIVE();

    @Default(lngs = { "en", "de" }, values = { "Regular Expressions", "Reguläre Ausrücke" })
    String SEARCHDIALOG_CHECKBOX_REGULAREXPRESSION();

    @Default(lngs = { "en", "de" }, values = { "alt", "Alt" })
    String ShortCuts_key_alt();

    @Default(lngs = { "en", "de" }, values = { "alt Gr", "Alt Gr" })
    String ShortCuts_key_altGr();

    @Default(lngs = { "en", "de" }, values = { "button1", "button1" })
    String ShortCuts_key_button1();

    @Default(lngs = { "en", "de" }, values = { "button2", "button2" })
    String ShortCuts_key_button2();

    @Default(lngs = { "en", "de" }, values = { "button3", "button3" })
    String ShortCuts_key_button3();

    @Default(lngs = { "en", "de" }, values = { "ctrl", "Strg" })
    String ShortCuts_key_ctrl();

    @Default(lngs = { "en", "de" }, values = { "meta", "meta" })
    String ShortCuts_key_meta();

    @Default(lngs = { "en", "de" }, values = { "shift", "Shift" })
    String ShortCuts_key_shift();

    @Default(lngs = { "en", "de" }, values = { "Move your mouse over interactive buttons to get help here", "Bewegen Sie die Maus über Schaltflächen um hier Hilfe zu sehen" })
    String Statusbar_Statusbar_tooltip();

    @Default(lngs = { "en", "de" }, values = { "Visit our Homepage", "Besuchen Sie unsere Homepage" })
    String Statusbar_Statusbar_visiturl_tooltip();

    @Default(lngs = { "en", "de" }, values = { "This dialog has a countdown and closes after a few seconds. Click to cancel the countdown", "Dieser Dialog hat einen Countdown und schließt sich in einigen Sekunden. Klicken Sie hier um den Countdown abzubrechen." })
    String TIMERDIALOG_TOOLTIP_TIMERLABEL();

    @Default(lngs = { "en", "de" }, values = { "Yes", "Ja" })
    String YES();

}
