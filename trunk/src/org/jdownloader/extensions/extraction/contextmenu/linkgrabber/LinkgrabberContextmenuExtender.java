package org.jdownloader.extensions.extraction.contextmenu.linkgrabber;

import org.jdownloader.controlling.contextmenu.ActionData;
import org.jdownloader.controlling.contextmenu.ContextMenuManager;
import org.jdownloader.controlling.contextmenu.MenuContainerRoot;
import org.jdownloader.controlling.contextmenu.MenuExtenderHandler;
import org.jdownloader.controlling.contextmenu.MenuItemData;
import org.jdownloader.controlling.contextmenu.SeperatorData;
import org.jdownloader.extensions.extraction.ExtractionExtension;
import org.jdownloader.extensions.extraction.contextmenu.ArchivesSubMenu;
import org.jdownloader.extensions.extraction.contextmenu.downloadlist.CleanupSubMenu;
import org.jdownloader.extensions.extraction.contextmenu.downloadlist.action.AutoExtractEnabledToggleAction;
import org.jdownloader.extensions.extraction.contextmenu.downloadlist.action.CleanupAutoDeleteFilesEnabledToggleAction;
import org.jdownloader.extensions.extraction.contextmenu.downloadlist.action.CleanupAutoDeleteLinksEnabledToggleAction;
import org.jdownloader.extensions.extraction.contextmenu.downloadlist.action.SetExtractPasswordAction;
import org.jdownloader.extensions.extraction.contextmenu.downloadlist.action.SetExtractToAction;
import org.jdownloader.extensions.extraction.contextmenu.downloadlist.action.ValidateArchivesAction;
import org.jdownloader.gui.views.linkgrabber.contextmenu.AddonLGSubMenuLink;

public class LinkgrabberContextmenuExtender implements MenuExtenderHandler {

    private ExtractionExtension extension;

    public ExtractionExtension getExtension() {
        return extension;
    }

    public void setExtension(ExtractionExtension extension) {
        this.extension = extension;
    }

    public LinkgrabberContextmenuExtender(ExtractionExtension extractionExtension) {
        this.extension = extractionExtension;
    }

    @Override
    public void updateMenuModel(ContextMenuManager manager, MenuContainerRoot mr) {
        int addonLinkIndex = 0;
        for (int i = 0; i < mr.getItems().size(); i++) {
            if (mr.getItems().get(i) instanceof AddonLGSubMenuLink) {
                addonLinkIndex = i;
                break;
            }
        }

        ArchivesSubMenu root;
        mr.getItems().add(addonLinkIndex, root = new ArchivesSubMenu());
        root.add(new MenuItemData(new ActionData(ValidateArchivesAction.class)));
        root.add(new SeperatorData());
        root.add(new MenuItemData(new ActionData(AutoExtractEnabledToggleAction.class)));
        root.add(new MenuItemData(new ActionData(SetExtractToAction.class)));
        root.add(new MenuItemData(new ActionData(SetExtractPasswordAction.class)));
        CleanupSubMenu cleanup = new CleanupSubMenu();
        root.add(cleanup);
        cleanup.add(new MenuItemData(new ActionData(CleanupAutoDeleteFilesEnabledToggleAction.class)));
        cleanup.add(new MenuItemData(new ActionData(CleanupAutoDeleteLinksEnabledToggleAction.class)));

    }
}
