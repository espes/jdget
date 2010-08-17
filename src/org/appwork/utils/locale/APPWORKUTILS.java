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

    ABSTRACTDIALOG_STYLE_SHOW_DO_NOT_DISPLAY_AGAIN("Don't show this again"),
    /*
     * ###de_DE:Bitte bestätigen!
     */
    DIALOG_CONFIRMDIALOG_TITLE("Please confirm!"),
    /*
     * ###de_DE:Bitte eingeben
     */
    DIALOG_INPUT_TITLE("Please enter!"),
    /*
     * ###de_DE:Bitte eingeben
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
    LOGINDIALOG_LABEL_USERNAME("Login"),
    LOGINDIALOG_LABEL_PASSWORD("Password"),
    LOGINDIALOG_LABEL_PASSWORD_REPEAT("Repeat Password"),
    LOGINDIALOG_BUTTON_REGISTER("Create new User"),
    LOGINDIALOG_CHECKBOX_REMEMBER("Remember"),
    LOGINDIALOG_BUTTON_LOGIN("Login"),
    PASSWORDDIALOG_PASSWORDCHANGE_OLDPASSWORD("Old Password:"),
    PASSWORDDIALOG_PASSWORDCHANGE_NEWPASSWORD_REPEAT("Confirm Password:"),
    PASSWORDDIALOG_PASSWORDCHANGE_NEWPASSWORD("New Password:"),
    SEARCHDIALOG_BUTTON_FIND("Find"),
    SEARCHDIALOG_CHECKBOX_CASESENSITIVE("Case sensitive"),
    SEARCHDIALOG_CHECKBOX_REGULAREXPRESSION("Regular Expressions"),
    TIMERDIALOG_MESSAGE_COUNTDOWN_STARTING("Countdown starting..."),
    TIMERDIALOG_TOOLTIP_TIMERLABEL("This dialog has a countdown and closes after a few seconds. Click to cancel the countdown"),
    DIALOG_FILECHOOSER_TOOLTIP_UPFOLDER("Switch to parent folder"),
    DIALOG_FILECHOOSER_TOOLTIP_HOMEFOLDER("Switch to Home"),
    DIALOG_FILECHOOSER_TOOLTIP_NEWFOLDER("Create new folder"),
    DIALOG_FILECHOOSER_TOOLTIP_DETAILS("Switch to detailed view"),
    DIALOG_FILECHOOSER_TOOLTIP_LIST("Switch to list view"),
    EXTTABLE_SEARCH_DIALOG_TITLE("Search table"),
    DIALOG_ERROR_TITLE("Error Occured"),
    LOC_USE_LOCALE("Use translation: %s", 1);
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
    private int wildCardCount = 0;
    /**
     * Stores the DefaultTranslation.
     */
    private String defaultTranslation;

    /**
     * Stores the translated value or <code>null</code>, if it wasn't translated
     * yet.
     */
    private String cache = null;

    private APPWORKUTILS(final String defaultString) {
        this.defaultTranslation = defaultString;
    }

    private APPWORKUTILS(final String defaultString, final int wildCards) {
        this.defaultTranslation = defaultString;
        this.wildCardCount = wildCards;
    }

    public String getDefaultTranslation() {
        return this.defaultTranslation;
    }

    public int getWildCardCount() {
        return this.wildCardCount;
    }

    public String s() {
        return this.toString();
    }

    @Override
    @Deprecated
    public String toString() {

        return this.s((Object[]) null);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.appwork.utils.locale.Translate#toString(java.lang.Object[])
     */
    public String s(final Object... args) {
        if (args != null && args.length > 0) {
            return Loc.LF("APPWORKUTILS:::" + this.name(), this.defaultTranslation, args);
        } else {
            if (this.cache != null) { return this.cache; }
            this.cache = Loc.L("APPWORKUTILS:::" + this.name(), this.defaultTranslation);
            return this.cache;
        }
    }
}
