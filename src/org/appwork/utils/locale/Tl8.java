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
public enum Tl8 implements Translate {

    ABSTRACTDIALOG_BUTTON_OK("Ok"),
    ABSTRACTDIALOG_BUTTON_CANCEL("Cancel"),
    ABSTRACTDIALOG_STYLE_SHOW_DO_NOT_DISPLAY_AGAIN("Don't show this again"),
    DIALOG_CONFIRMDIALOG_TITLE("Please confirm!"),
    DIALOG_INPUT_TITLE("Please enter!"),
    DIALOG_PASSWORD_TITLE("Please enter!"),
    DIALOG_MESSAGE_TITLE("Message"),
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

    LOC_USE_LOCALE("Use Language: %s");

    /**
     * Stores the DefaultTranslation.
     */
    private String defaultTranslation;
    /**
     * Stores the translated value or <code>null</code>, if it wasn't translated
     * yet.
     */
    private String cache = null;

    private Tl8(String defaultString) {
        this.defaultTranslation = defaultString;
    }

    @Override
    public String s() {
        return toString();
    }

    @Override
    public String toString() {
        return toString((Object[]) null);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.appwork.utils.locale.Translate#toString(java.lang.Object[])
     */
    public String toString(Object... args) {
        if (args != null && args.length > 0) {
            return Loc.LF("APPWORKUTILS:::" + name(), defaultTranslation, args);
        } else {
            if (cache != null) return cache;
            cache = Loc.L("APPWORKUTILS:::" + name(), defaultTranslation);
            return cache;
        }
    }

    public static void reset() {
        for (Tl8 rsm : Tl8.values()) {
            rsm.cache = null;
        }
    }

    /**
     * @return
     */
    public static String list() {
        StringBuilder sb = new StringBuilder();
        sb.append("# APPWORK UTILS Locale: " + Loc.getLocale());

        int max = 0;
        for (Tl8 entry : Tl8.values()) {
            max = Math.max(entry.name().length(), max);
        }
        for (Tl8 entry : Tl8.values()) {
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
}
