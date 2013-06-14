package org.jdownloader.gui.jdtrayicon;

import javax.swing.JPopupMenu;

import jd.gui.swing.jdgui.components.toolbar.actions.AutoReconnectToggleAction;
import jd.gui.swing.jdgui.components.toolbar.actions.ClipBoardToggleAction;
import jd.gui.swing.jdgui.components.toolbar.actions.GlobalPremiumSwitchToggleAction;
import jd.gui.swing.jdgui.components.toolbar.actions.StartDownloadsAction;
import jd.gui.swing.jdgui.components.toolbar.actions.StopDownloadsAction;
import jd.gui.swing.jdgui.menu.actions.ExitAction;
import jd.plugins.DownloadLink;
import jd.plugins.FilePackage;

import org.appwork.exceptions.WTFException;
import org.jdownloader.controlling.contextmenu.ActionData;
import org.jdownloader.controlling.contextmenu.ContextMenuManager;
import org.jdownloader.controlling.contextmenu.MenuContainerRoot;
import org.jdownloader.controlling.contextmenu.MenuItemData;
import org.jdownloader.controlling.contextmenu.MenuItemProperty;
import org.jdownloader.controlling.contextmenu.SeperatorData;
import org.jdownloader.gui.jdtrayicon.actions.ChunksEditorLink;
import org.jdownloader.gui.jdtrayicon.actions.ParalellDownloadsEditorLink;
import org.jdownloader.gui.jdtrayicon.actions.ParallelDownloadsPerHostEditorLink;
import org.jdownloader.gui.jdtrayicon.actions.SpeedlimitEditorLink;
import org.jdownloader.gui.jdtrayicon.actions.TrayMenuManagerAction;
import org.jdownloader.gui.jdtrayicon.actions.TrayOpenDefaultDownloadDirectory;
import org.jdownloader.gui.jdtrayicon.actions.TrayPauseAction;
import org.jdownloader.gui.jdtrayicon.actions.TrayReconnectAction;
import org.jdownloader.gui.jdtrayicon.actions.TrayUpdateAction;
import org.jdownloader.gui.jdtrayicon.translate._TRAY;
import org.jdownloader.gui.views.SelectionInfo;

public class TrayIconMenuManager extends ContextMenuManager<FilePackage, DownloadLink> {

    private static final TrayIconMenuManager INSTANCE = new TrayIconMenuManager();

    /**
     * get the only existing instance of DownloadListContextMenuManager. This is a singleton
     * 
     * @return
     */
    public static TrayIconMenuManager getInstance() {
        return TrayIconMenuManager.INSTANCE;
    }

    /**
     * Create a new instance of DownloadListContextMenuManager. This is a singleton class. Access the only existing instance by using
     * {@link #getInstance()}.
     */

    private TrayIconMenuManager() {
        super();

    }

    public JPopupMenu build(SelectionInfo<FilePackage, DownloadLink> si) {
        throw new WTFException("Not Supported");

    }

    public boolean supportsProperty(MenuItemProperty property) {
        switch (property) {

        case HIDE_IF_DISABLED:
        case HIDE_IF_OPENFILE_IS_UNSUPPORTED:
        case HIDE_IF_OUTPUT_NOT_EXISTING:
        case LINK_CONTEXT:
        case PACKAGE_CONTEXT:
            return false;

        default:
            return true;
        }

    }

    private static final int VERSION = 0;

    public MenuContainerRoot createDefaultStructure() {
        MenuContainerRoot mr = new MenuContainerRoot();
        mr.setSource(VERSION);
        // mr.add()

        mr.add(new MenuItemData(new ActionData(StartDownloadsAction.class), MenuItemProperty.HIDE_IF_DOWNLOADS_ARE_RUNNING));
        mr.add(new MenuItemData(new ActionData(StopDownloadsAction.class), MenuItemProperty.HIDE_IF_DOWNLOADS_ARE_NOT_RUNNING));
        mr.add(new MenuItemData(new ActionData(TrayPauseAction.class), MenuItemProperty.HIDE_IF_DOWNLOADS_ARE_NOT_RUNNING));
        mr.add(new MenuItemData(new ActionData(TrayUpdateAction.class)));
        mr.add(new MenuItemData(new ActionData(TrayReconnectAction.class)));
        mr.add(new MenuItemData(new ActionData(TrayOpenDefaultDownloadDirectory.class)));

        mr.add(new MenuItemData(new ActionData(GlobalPremiumSwitchToggleAction.class)));
        mr.add(new MenuItemData(new ActionData(ClipBoardToggleAction.class)));
        mr.add(new MenuItemData(new ActionData(AutoReconnectToggleAction.class)));
        mr.add(new SeperatorData());
        mr.add(new ChunksEditorLink());
        mr.add(new ParalellDownloadsEditorLink());
        mr.add(new ParallelDownloadsPerHostEditorLink());
        mr.add(new SpeedlimitEditorLink());
        mr.add(new SeperatorData());

        mr.add(new MenuItemData(new ActionData(TrayMenuManagerAction.class)));

        mr.add(new SeperatorData());
        mr.add(new MenuItemData(ExitAction.class, MenuItemProperty.HIDE_ON_MAC));

        return mr;
    }

    @Override
    public String getFileExtension() {
        return ".jdtray";
    }

    @Override
    public String getName() {
        return _TRAY._.TrayMenuManager_getName();
    }

}
