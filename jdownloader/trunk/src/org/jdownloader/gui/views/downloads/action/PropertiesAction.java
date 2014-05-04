package org.jdownloader.gui.views.downloads.action;

import java.awt.event.ActionEvent;

import jd.plugins.DownloadLink;
import jd.plugins.FilePackage;

import org.jdownloader.controlling.contextmenu.CustomizableTableContextAppAction;
import org.jdownloader.gui.IconKey;
import org.jdownloader.gui.translate._GUI;
import org.jdownloader.settings.staticreferences.CFG_GUI;

public class PropertiesAction extends CustomizableTableContextAppAction<FilePackage, DownloadLink> {

    public PropertiesAction() {
        super();
        setName(_GUI._.PropertiesAction_PropertiesAction());
        setIconKey(IconKey.ICON_BOTTOMBAR);

    }

    public boolean isVisible() {
        return super.isVisible() && !CFG_GUI.DOWNLOADS_TAB_PROPERTIES_PANEL_VISIBLE.isEnabled();
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        CFG_GUI.DOWNLOADS_TAB_PROPERTIES_PANEL_VISIBLE.setValue(true);
    }

}
