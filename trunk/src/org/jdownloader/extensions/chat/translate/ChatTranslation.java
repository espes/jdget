package org.jdownloader.extensions.chat.translate;

import org.appwork.txtresource.Default;
import org.appwork.txtresource.Defaults;
import org.appwork.txtresource.TranslateInterface;

@Defaults(lngs = { "en" })
public interface ChatTranslation extends TranslateInterface {

    @Default(lngs = { "en" }, values = { "Loading Message of the day" })
    String jd_plugins_optional_jdchat_JDChat_topic_default();

    @Default(lngs = { "en" }, values = { "Close Tab" })
    String jd_plugins_optional_jdchat_closeTab();

    @Default(lngs = { "en" }, values = { "Your wished nickname?" })
    String plugins_optional_jdchat_enternick();

    @Default(lngs = { "en" }, values = { "Message of the day" })
    String jd_plugins_optional_jdchat_JDChat_topic_tooltip();

    @Default(lngs = { "en" }, values = { "Chat" })
    String jd_plugins_optional_jdchat_jdchat();

    @Default(lngs = { "en" }, values = { "JD Support Chat" })
    String jd_plugins_optional_jdchat_JDChatView_tooltip();

    @Default(lngs = { "en" }, values = { "JD Support Chat" })
    String jd_plugins_optional_jdchat_JDChatView_title();

    @Default(lngs = { "en", "de" }, values = { "Your name", "Dein Name" })
    String settings_nick();

    @Default(lngs = { "en", "de" }, values = { "Contact JDownloader Developers and Supporters", "Kontaktiere JDownloader Entwickler oder Supporter." })
    String description();

    @Default(lngs = { "en", "de" }, values = { "Enable userlist colors", "Benutzerfarben aktivieren" })
    String settings_enabled_userlist_colors();

    @Default(lngs = { "en", "de" }, values = { "Userlist position", "Position der Benutzerliste" })
    String settings_userlist_position();

    @Default(lngs = { "en", "de" }, values = { "Right side", "Rechte Seite" })
    String settings_userlist_position_right();

    @Default(lngs = { "en", "de" }, values = { "Left side", "Linke Seite" })
    String settings_userlist_position_left();

    @Default(lngs = { "en", "de" }, values = { "Perform this actions after entering chat:", "Nach dem Beitreten diese Aktionen ausführen:" })
    String settings_perform();

    @Default(lngs = { "en", "de" }, values = { "%s1 Channel", "%s1 Kanal" })
    String gui_tab_title(String currentChannel);

    @Default(lngs = { "en", "de" }, values = { "Close Chat", "Chat schließen" })
    String ChatExtensionGuiToggleAction_selected();

    @Default(lngs = { "en", "de" }, values = { "Close the Chat Tab", "Den Chat Reiter schließen" })
    String ChatExtensionGuiToggleAction_selected_tt();

    @Default(lngs = { "en", "de" }, values = { "Open Chat", "Chat öffnen" })
    String ChatExtensionGuiToggleAction_deselected();

    @Default(lngs = { "en", "de" }, values = { "Open the Chat Tab", "Den Chat Reiter öffnen" })
    String ChatExtensionGuiToggleAction_deselected_tt();
}