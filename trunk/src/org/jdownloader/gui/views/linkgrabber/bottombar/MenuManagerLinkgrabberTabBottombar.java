package org.jdownloader.gui.views.linkgrabber.bottombar;

import org.jdownloader.actions.AbstractContextMenuAction;
import org.jdownloader.controlling.contextmenu.ActionData;
import org.jdownloader.controlling.contextmenu.MenuContainer;
import org.jdownloader.controlling.contextmenu.MenuContainerRoot;
import org.jdownloader.controlling.contextmenu.MenuItemData;
import org.jdownloader.controlling.contextmenu.SeperatorData;
import org.jdownloader.gui.IconKey;
import org.jdownloader.gui.translate._GUI;
import org.jdownloader.gui.views.downloads.MenuManagerDownloadTabBottomBar;
import org.jdownloader.gui.views.downloads.bottombar.AbstractBottomBarMenuManager;
import org.jdownloader.gui.views.downloads.bottombar.DeleteContainer;
import org.jdownloader.gui.views.downloads.bottombar.HorizontalBoxItem;
import org.jdownloader.gui.views.downloads.bottombar.QuickSettingsMenuContainer;
import org.jdownloader.gui.views.linkgrabber.LinkgrabberOverviewPanelToggleAction;
import org.jdownloader.gui.views.linkgrabber.LinkgrabberPropertiesToggleAction;
import org.jdownloader.gui.views.linkgrabber.LinkgrabberSearchMenuItem;
import org.jdownloader.gui.views.linkgrabber.LinkgrabberSidebarToggleAction;
import org.jdownloader.gui.views.linkgrabber.actions.ClearLinkgrabberAction;
import org.jdownloader.gui.views.linkgrabber.actions.ConfirmAllAction;
import org.jdownloader.gui.views.linkgrabber.actions.ConfirmSelectionBarAction;
import org.jdownloader.gui.views.linkgrabber.actions.GenericDeleteFromLinkgrabberAction;
import org.jdownloader.gui.views.linkgrabber.actions.ResetAction;
import org.jdownloader.gui.views.linkgrabber.contextmenu.ClearFilteredLinksAction;
import org.jdownloader.gui.views.linkgrabber.contextmenu.ConfirmSelectionContextAction.AutoStartOptions;
import org.jdownloader.gui.views.linkgrabber.contextmenu.RemoveIncompleteArchives;

public class MenuManagerLinkgrabberTabBottombar extends AbstractBottomBarMenuManager {
    private static final MenuManagerLinkgrabberTabBottombar INSTANCE = new MenuManagerLinkgrabberTabBottombar();

    /**
     * get the only existing instance of DownloadListContextMenuManager. This is a singleton
     * 
     * @return
     */
    public static MenuManagerLinkgrabberTabBottombar getInstance() {
        return INSTANCE;
    }

    @Override
    public String getFileExtension() {
        return ".jdLGBottomBar";
    }

    @Override
    public synchronized MenuContainerRoot getMenuData() {
        return super.getMenuData();
    }

    @Override
    protected String getStorageKey() {
        return "LinkgrabberTabBottomBar";
    }

    @Override
    public String getName() {
        return _GUI._.gui_config_menumanager_linkgrabberBottom();
    }

    public MenuContainerRoot createDefaultStructure() {
        MenuContainerRoot mr = new MenuContainerRoot();

        MenuManagerDownloadTabBottomBar.fillAddLinks(mr);
        //

        mr.add(setOptional(new MenuItemData(new ActionData(ResetAction.class))));
        mr.add((new MenuItemData(new ActionData(ClearLinkgrabberAction.class))));

        DeleteContainer delete = new DeleteContainer();

        delete.add(setIconKey(new ActionData(GenericDeleteFromLinkgrabberAction.class).putSetup(GenericDeleteFromLinkgrabberAction.DELETE_DISABLED, true), IconKey.ICON_REMOVE_DISABLED));

        delete.add(setIconKey(new ActionData(GenericDeleteFromLinkgrabberAction.class).putSetup(GenericDeleteFromLinkgrabberAction.DELETE_OFFLINE, true), IconKey.ICON_REMOVE_OFFLINE));

        //

        // delete.add(RemoveAllVisibleCrawledLinksAction.class);
        // delete.add(ToolbarDeleteAction.class);
        // delete.add(setIconKey(new
        // ActionData(GenericDeleteSelectedToolbarAction.class).putSetup(GenericDeleteSelectedToolbarAction.DELETE_DISABLED, true),
        // IconKey.ICON_REMOVE_DISABLED));
        // delete.add(setIconKey(new
        // ActionData(GenericDeleteSelectedToolbarAction.class).putSetup(GenericDeleteSelectedToolbarAction.DELETE_FAILED, true),
        // IconKey.ICON_REMOVE_FAILED));
        // delete.add(setIconKey(new
        // ActionData(GenericDeleteSelectedToolbarAction.class).putSetup(GenericDeleteSelectedToolbarAction.DELETE_OFFLINE, true),
        // IconKey.ICON_REMOVE_OFFLINE));
        delete.add(org.jdownloader.gui.views.linkgrabber.actions.RemoveNonSelectedAction.class);
        delete.add(new SeperatorData());
        delete.add(new ActionData(RemoveIncompleteArchives.class).putSetup(AbstractContextMenuAction.ITEM_VISIBLE_FOR_EMPTY_SELECTION, true));

        delete.add(new ActionData(ClearFilteredLinksAction.class));
        delete.add(new SeperatorData());
        delete.add(setName(new ActionData(ResetAction.class), _GUI._.ResetPopupAction_ResetPopupAction_()));

        mr.add(delete);
        //
        mr.add(new LinkgrabberSearchMenuItem());
        mr.add(new HorizontalBoxItem());
        mr.add(AddFilteredStuffAction.class);

        mr.add(new LeftRightDividerItem());

        mr.add(new AutoConfirmMenuLink());
        mr.add(new ConfirmMenuItem());
        //
        MenuContainer all = new MenuContainer(_GUI._.ConfirmOptionsAction_actionPerformed_all(), "confirmAll");
        MenuContainer selected = new MenuContainer(_GUI._.ConfirmOptionsAction_actionPerformed_selected(), "confirmSelectedLinks");
        all.add(new ActionData(ConfirmAllAction.class).putSetup(ConfirmAllAction.AUTO_START, false));
        all.add(new ActionData(ConfirmAllAction.class).putSetup(ConfirmAllAction.AUTO_START, true));

        // KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, InputEvent.CTRL_DOWN_MASK)
        selected.add(new ActionData(ConfirmSelectionBarAction.class).putSetup(ConfirmSelectionBarAction.AUTO_START, AutoStartOptions.DISABLED.toString()));
        selected.add(new ActionData(ConfirmSelectionBarAction.class).putSetup(ConfirmSelectionBarAction.AUTO_START, AutoStartOptions.ENABLED.toString()));
        MenuContainer popup = new MenuContainer("", null);
        popup.add(all);
        popup.add(selected);
        mr.add(popup);

        QuickSettingsMenuContainer quicksettings = new QuickSettingsMenuContainer();

        quicksettings.add(AddAtTopToggleAction.class);
        quicksettings.add(AutoConfirmToggleAction.class);
        quicksettings.add(AutoStartToggleAction.class);
        quicksettings.add(setOptional(LinkFilterToggleAction.class));

        quicksettings.add(new SeperatorData());
        quicksettings.add((LinkgrabberOverviewPanelToggleAction.class));
        quicksettings.add((LinkgrabberPropertiesToggleAction.class));

        quicksettings.add((LinkgrabberSidebarToggleAction.class));
        quicksettings.add(new SeperatorData());
        quicksettings.add(BottomBarMenuManagerAction.class);
        mr.add(quicksettings);
        return mr;
    }
}
