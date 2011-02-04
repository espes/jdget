/**
 * Copyright (c) 2009 - 2010 AppWork UG(haftungsbeschränkt) <e-mail@appwork.org>
 * 
 * This file is part of org.appwork.utils.locale
 * 
 * This software is licensed under the Artistic License 2.0,
 * see the LICENSE file or http://www.opensource.org/licenses/artistic-license-2.0.php
 * for details
 */
package org.appwork.utils.locale;

/**
 * @author thomas
 * 
 */
public enum APPWORKUTILS implements Translate {
/*
* ###de_DE:Ok
*/
ABSTRACTDIALOG_BUTTON_OK("Ok"),
/*
* ###de_DE:Abbrechen
*/
ABSTRACTDIALOG_BUTTON_CANCEL("Cancel"),
/*
* ###de_DE:Nicht mehr anzeigen
*/
ABSTRACTDIALOG_STYLE_SHOW_DO_NOT_DISPLAY_AGAIN("Don't show this again"),
/*
* ###de_DE:Bitte bestätigen!
*/
DIALOG_CONFIRMDIALOG_TITLE("Please confirm!"),
/*
* ###de_DE:Bitte eingeben!
*/
DIALOG_INPUT_TITLE("Please enter!"),
/*
* ###de_DE:Bitte eingeben!
*/
DIALOG_PASSWORD_TITLE("Please enter!"),
/*
* ###de_DE:Nachricht
*/
DIALOG_MESSAGE_TITLE("Message"),
/*
* ###de_DE:Bitte eingeben
*/
DIALOG_SLIDER_TITLE("Please enter!"),
/*
* ###de_DE:Benutzername
*/
LOGINDIALOG_LABEL_USERNAME("Login"),
/*
* ###de_DE:Passwort
*/
LOGINDIALOG_LABEL_PASSWORD("Password"),
/*
* ###de_DE:Passwort wiederholen
*/
LOGINDIALOG_LABEL_PASSWORD_REPEAT("Repeat Password"),
/*
* ###de_DE:Neuen Benutzer anlegen
*/
LOGINDIALOG_BUTTON_REGISTER("Create new User"),
/*
* ###de_DE:Merken
*/
LOGINDIALOG_CHECKBOX_REMEMBER("Remember"),
/*
* ###de_DE:Anmelden
*/
LOGINDIALOG_BUTTON_LOGIN("Login"),
/*
* ###de_DE:Altes Passwort
*/
PASSWORDDIALOG_PASSWORDCHANGE_OLDPASSWORD("Old Password:"),
/*
* ###de_DE:Passwort bestätigen
*/
PASSWORDDIALOG_PASSWORDCHANGE_NEWPASSWORD_REPEAT("Confirm Password:"),
/*
* ###de_DE:Neues Passwort
*/
PASSWORDDIALOG_PASSWORDCHANGE_NEWPASSWORD("New Password:"),
/*
* ###de_DE:Suchen
*/
SEARCHDIALOG_BUTTON_FIND("Find"),
/*
* ###de_DE:Groß/Kleinschreibung
*/
SEARCHDIALOG_CHECKBOX_CASESENSITIVE("Case sensitive"),
/*
* ###de_DE:Reguläre Ausrücke
*/
SEARCHDIALOG_CHECKBOX_REGULAREXPRESSION("Regular Expressions"),
/*
* ###de_DE:Dieser Dialog hat einen Countdown und schließt sich in einigen
     * Sekunden. Klicken Sie hier um den Countdown abzubrechen.
*/
TIMERDIALOG_TOOLTIP_TIMERLABEL("This dialog has a countdown and closes after a few seconds. Click to cancel the countdown"),
/*
* ###de_DE:Einen Ordner nach oben
*/
DIALOG_FILECHOOSER_TOOLTIP_UPFOLDER("Switch to parent folder"),
/*
* ###de_DE:Zum Benutzerverzeichnis wechseln
*/
DIALOG_FILECHOOSER_TOOLTIP_HOMEFOLDER("Switch to Home"),
/*
* ###de_DE:Neuen Ordner erstellen
*/
DIALOG_FILECHOOSER_TOOLTIP_NEWFOLDER("Create new folder"),
/*
* ###de_DE:Detailansicht
*/
DIALOG_FILECHOOSER_TOOLTIP_DETAILS("Switch to detailed view"),
/*
* ###de_DE:Listenansicht
*/
DIALOG_FILECHOOSER_TOOLTIP_LIST("Switch to list view"),
/*
* ###de_DE:Tabelle durchsuchen
*/
EXTTABLE_SEARCH_DIALOG_TITLE("Search table"),
/*
* ###de_DE:Ein Fehler ist aufgetreten
*/
DIALOG_ERROR_TITLE("Error Occured"),
/*
* ###de_DE:Befehl -> -
*/
COMMANDLINEAPP_COMMAND("Command -> -"),
/*
* ###de_DE:Kopieren
*/
COPYCUTPASTE_COPY("Copy"),
/*
* ###de_DE:Ausschneiden
*/
COPYCUTPASTE_CUT("Cut"),
/*
* ###de_DE:Löschen
*/
COPYCUTPASTE_DELETE("Delete"),
/*
* ###de_DE:Einfügen
*/
COPYCUTPASTE_PASTE("Paste"),
/*
* ###de_DE:Markieren
*/
COPYCUTPASTE_SELECT("Select"),
/*
* ###de_DE:Bewegen Sie die Maus über Schaltflächen um hier Hilfe zu sehen
*/
Statusbar_Statusbar_tooltip("Move your mouse over interactive buttons to get help here"),
/*
* ###de_DE:Besuchen Sie unsere Homepage
*/
Statusbar_Statusbar_visiturl_tooltip("Visit our Homepage"),
/*
* ###de_DE:Fehlerdetails
*/
ExceptionDialog_layoutDialogContent_more_button("Error details"),
/*
* 
     * Grund für diesen Fehler:
*/
ExceptionDialog_layoutDialogContent_logLabel("This Error is cause by:"),
/*
* ###de_DE:Zugangsdaten eingeben
*/
AccountNew_AccountNew_title("Enter Logins"),
/*
* ###de_DE:Bitte geben Sie hier Ihre Zugangsdaten ein
*/
AccountNew_AccountNew_message("Please enter your logins here"),
/*
* ###de_DE:Benutzername
*/
AccountNew_layoutDialogContent_accountname("Username"),
/*
* ###de_DE:Passwort
*/
AccountNew_layoutDialogContent_password("Password"),
/*
* ###de_DE:Merken?
*/
AccountNew_layoutDialogContent_save("Remember"),
/*
* ###de_DE:Durchschnitt: %s/s |
*/
AppWorkUtils_Graph_getAverageSpeedString("Average: %s/s |", 1),
/*
* ###de_DE:Aktuell: %s/s
*/
AppWorkUtils_Graph_getSpeedString("Current: %s/s", 1);
// ENDOFENUMS
    /**
     * @return
     */
    public static String list() {
        final StringBuilder sb = new StringBuilder();
        sb.append("# APPWORK UTILS Locale: " + Loc.getLocale());

        int max = 0;
        for (final APPWORKUTILS entry : APPWORKUTILS.values()) {
            max = Math.max(entry.name().length(), max);
        }
        for (final APPWORKUTILS entry : APPWORKUTILS.values()) {
            sb.append("\r\nAPPWORKUTILS:::");
            sb.append(entry.name());

            sb.append("               ");
            for (int i = entry.name().length(); i < max; i++) {
                sb.append(" ");
            }
            sb.append(" = ");
            sb.append(Loc.L("APPWORKUTILS:::" + entry.name(), entry.defaultTranslation).replace("\r", "\\r").replace("\n", "\\n"));
        }
        return sb.toString();
    }

    public static void reset() {
        for (final APPWORKUTILS rsm : APPWORKUTILS.values()) {
            rsm.cache = null;
        }
    }

    /**
     * Stores the numbers of wildcards (<code>%s</code>) in this string.
     */
    private int    wildCardCount = 0;
    /**
     * Stores the DefaultTranslation.
     */
    private String defaultTranslation;

    /**
     * Stores the translated value or <code>null</code>, if it wasn't translated
     * yet.
     */
    private String cache         = null;

    private APPWORKUTILS(final String defaultString) {
        defaultTranslation = defaultString;
    }

    private APPWORKUTILS(final String defaultString, final int wildCards) {
        defaultTranslation = defaultString;
        wildCardCount = wildCards;
    }

    public String getDefaultTranslation() {
        return defaultTranslation;
    }

    public int getWildCardCount() {
        return wildCardCount;
    }

    public String s() {
        return toString();
    }

    public String s(final Object... args) {
        if (args != null && args.length > 0) {
            return Loc.LF("APPWORKUTILS:::" + name(), defaultTranslation, args);
        } else {
            if (cache != null) { return cache; }
            cache = Loc.L("APPWORKUTILS:::" + name(), defaultTranslation);
            return cache;
        }
    }

    @Override
    @Deprecated
    public String toString() {
        return this.s((Object[]) null);
    }
}