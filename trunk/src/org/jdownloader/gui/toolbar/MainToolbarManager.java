package org.jdownloader.gui.toolbar;

import javax.swing.JPopupMenu;

import jd.controlling.IOEQ;
import jd.gui.swing.jdgui.components.toolbar.MainToolBar;
import jd.gui.swing.jdgui.components.toolbar.actions.AutoReconnectToggleAction;
import jd.gui.swing.jdgui.components.toolbar.actions.ClipBoardToggleAction;
import jd.gui.swing.jdgui.components.toolbar.actions.ExitToolbarAction;
import jd.gui.swing.jdgui.components.toolbar.actions.GlobalPremiumSwitchToggleAction;
import jd.gui.swing.jdgui.components.toolbar.actions.OpenDefaultDownloadFolderAction;
import jd.gui.swing.jdgui.components.toolbar.actions.PauseDownloadsAction;
import jd.gui.swing.jdgui.components.toolbar.actions.ReconnectAction;
import jd.gui.swing.jdgui.components.toolbar.actions.ShowSettingsAction;
import jd.gui.swing.jdgui.components.toolbar.actions.SilentModeToggleAction;
import jd.gui.swing.jdgui.components.toolbar.actions.StartDownloadsAction;
import jd.gui.swing.jdgui.components.toolbar.actions.StopDownloadsAction;
import jd.gui.swing.jdgui.components.toolbar.actions.UpdateAction;
import jd.gui.swing.jdgui.interfaces.View;
import jd.gui.swing.jdgui.menu.actions.KnowledgeAction;
import jd.gui.swing.jdgui.menu.actions.LatestChangesAction;
import jd.gui.swing.jdgui.menu.actions.RestartAction;
import jd.gui.swing.jdgui.menu.actions.SettingsAction;
import jd.plugins.DownloadLink;
import jd.plugins.FilePackage;

import org.appwork.exceptions.WTFException;
import org.appwork.scheduler.DelayedRunnable;
import org.appwork.utils.Application;
import org.jdownloader.controlling.contextmenu.ActionData;
import org.jdownloader.controlling.contextmenu.ContextMenuManager;
import org.jdownloader.controlling.contextmenu.MenuContainer;
import org.jdownloader.controlling.contextmenu.MenuContainerRoot;
import org.jdownloader.controlling.contextmenu.MenuExtenderHandler;
import org.jdownloader.controlling.contextmenu.MenuItemData;
import org.jdownloader.controlling.contextmenu.SeperatorData;
import org.jdownloader.gui.event.GUIEventSender;
import org.jdownloader.gui.event.GUIListener;
import org.jdownloader.gui.mainmenu.ChunksEditorLink;
import org.jdownloader.gui.mainmenu.ParalellDownloadsEditorLink;
import org.jdownloader.gui.mainmenu.ParallelDownloadsPerHostEditorLink;
import org.jdownloader.gui.mainmenu.SpeedlimitEditorLink;
import org.jdownloader.gui.mainmenu.action.AddLinksMenuAction;
import org.jdownloader.gui.mainmenu.action.LogSendAction;
import org.jdownloader.gui.mainmenu.container.CaptchaQuickSettingsContainer;
import org.jdownloader.gui.mainmenu.container.OptionalContainer;
import org.jdownloader.gui.toolbar.action.CaptchaDialogsToogleAction;
import org.jdownloader.gui.toolbar.action.CaptchaExchangeToogleAction;
import org.jdownloader.gui.toolbar.action.GenericDeleteSelectedToolbarAction;
import org.jdownloader.gui.toolbar.action.JAntiCaptchaToogleAction;
import org.jdownloader.gui.toolbar.action.MoveDownAction;
import org.jdownloader.gui.toolbar.action.MoveToBottomAction;
import org.jdownloader.gui.toolbar.action.MoveToTopAction;
import org.jdownloader.gui.toolbar.action.MoveUpAction;
import org.jdownloader.gui.toolbar.action.RemoteCaptchaToogleAction;
import org.jdownloader.gui.toolbar.action.ToolbarDeleteAction;
import org.jdownloader.gui.translate._GUI;
import org.jdownloader.gui.views.SelectionInfo;
import org.jdownloader.gui.views.components.packagetable.context.RenameAction;
import org.jdownloader.gui.views.downloads.action.MenuManagerAction;
import org.jdownloader.gui.views.downloads.context.submenu.DeleteMenuContainer;
import org.jdownloader.gui.views.linkgrabber.actions.AddContainerAction;

public class MainToolbarManager extends ContextMenuManager<FilePackage, DownloadLink> implements GUIListener {

    private static final MainToolbarManager INSTANCE = new MainToolbarManager();

    /**
     * get the only existing instance of DownloadListContextMenuManager. This is a singleton
     * 
     * @return
     */
    public static MainToolbarManager getInstance() {
        return MainToolbarManager.INSTANCE;
    }

    private DelayedRunnable updateDelayer;

    @Override
    public void setMenuData(MenuContainerRoot root) {
        super.setMenuData(root);
        // no delayer here.
        MainToolBar.getInstance().updateToolbar();
    }

    @Override
    public synchronized void registerExtender(MenuExtenderHandler handler) {
        super.registerExtender(handler);
        updateDelayer.resetAndStart();
    }

    @Override
    public void unregisterExtender(MenuExtenderHandler handler) {
        super.unregisterExtender(handler);
        updateDelayer.resetAndStart();
    }

    @Override
    public String getFileExtension() {
        return ".jdToolbar";
    }

    /**
     * Create a new instance of DownloadListContextMenuManager. This is a singleton class. Access the only existing instance by using
     * {@link #getInstance()}.
     */

    private MainToolbarManager() {
        super();
        updateDelayer = new DelayedRunnable(IOEQ.TIMINGQUEUE, 1000l, 2000) {

            @Override
            public void delayedrun() {
                MainToolBar.getInstance().updateToolbar();
            }

        };
        GUIEventSender.getInstance().addListener(this, true);

    }

    public JPopupMenu build(SelectionInfo<FilePackage, DownloadLink> si) {
        throw new WTFException("Not Supported");

    }

    public boolean isAcceleratorsEnabled() {
        return true;
    }

    private static final int VERSION = 0;

    public MenuContainerRoot createDefaultStructure() {
        MenuContainerRoot mr = new MenuContainerRoot();
        mr.setSource(VERSION);

        mr.add(StartDownloadsAction.class);

        mr.add(PauseDownloadsAction.class);
        mr.add(StopDownloadsAction.class);

        mr.add(new SeperatorData());
        mr.add(new MenuItemData(new ActionData(MoveToTopAction.class)));
        mr.add(new MenuItemData(new ActionData(MoveUpAction.class)));
        mr.add(new MenuItemData(new ActionData(MoveDownAction.class)));
        mr.add(new MenuItemData(new ActionData(MoveToBottomAction.class)));
        mr.add(new SeperatorData());
        mr.add(ClipBoardToggleAction.class);
        mr.add(AutoReconnectToggleAction.class);
        mr.add(GlobalPremiumSwitchToggleAction.class);
        mr.add(SilentModeToggleAction.class);
        mr.add(new SeperatorData());

        mr.add(ReconnectAction.class);
        mr.add(UpdateAction.class);
        if (!Application.isJared(MainToolbarManager.class)) {
            MenuContainer opt;
            mr.add(opt = new MenuContainer("Dialog Debug", "menu"));
            opt.add(ShowInputDialogDebugAction.class);

        }
        OptionalContainer opt;
        mr.add(opt = new OptionalContainer(false));
        opt.add(new MenuItemData(OpenDefaultDownloadFolderAction.class));
        opt.add(new MenuItemData(ShowSettingsAction.class));
        opt.add(new MenuItemData(ExitToolbarAction.class));
        opt.add(AddLinksMenuAction.class);
        opt.add(AddContainerAction.class);
        opt.add(RestartAction.class);
        opt.add(SettingsAction.class);
        opt.add(new ChunksEditorLink());

        opt.add(new ParalellDownloadsEditorLink());
        opt.add(new ParallelDownloadsPerHostEditorLink());
        //
        opt.add(new SpeedlimitEditorLink());
        opt.add(LatestChangesAction.class);
        opt.add(KnowledgeAction.class);
        opt.add(LogSendAction.class);
        opt.add(RenameAction.class);
        CaptchaQuickSettingsContainer ocr;
        opt.add(ocr = new CaptchaQuickSettingsContainer());
        ocr.add(CaptchaExchangeToogleAction.class);
        ocr.add(JAntiCaptchaToogleAction.class);
        ocr.add(RemoteCaptchaToogleAction.class);

        mr.add(createDeleteMenu());

        ocr.add(CaptchaDialogsToogleAction.class);
        return mr;
    }

    private MenuItemData createDeleteMenu() {
        DeleteMenuContainer delete = new DeleteMenuContainer();
        delete.setVisible(false);
        delete.add(ToolbarDeleteAction.class);
        delete.add(new ActionData(GenericDeleteSelectedToolbarAction.class).putSetup(GenericDeleteSelectedToolbarAction.DELETE_DISABLED, true));
        delete.add(new ActionData(GenericDeleteSelectedToolbarAction.class).putSetup(GenericDeleteSelectedToolbarAction.DELETE_FAILED, true));
        delete.add(new ActionData(GenericDeleteSelectedToolbarAction.class).putSetup(GenericDeleteSelectedToolbarAction.DELETE_FINISHED, true));
        delete.add(new ActionData(GenericDeleteSelectedToolbarAction.class).putSetup(GenericDeleteSelectedToolbarAction.DELETE_OFFLINE, true));
        // delete.add(new MenuItemData(new ActionData(DeleteSelectedAndFailedLinksAction.class)));
        // delete.add(new MenuItemData(new ActionData(DeleteSelectedFinishedLinksAction.class)));
        // delete.add(new MenuItemData(new ActionData(DeleteSelectedOfflineLinksAction.class)));
        return delete;
    }

    public void show() {

        new MenuManagerAction().actionPerformed(null);
    }

    @Override
    public String getName() {
        return _GUI._.MainToolbarManager_getName();
    }

    @Override
    public void onGuiMainTabSwitch(View oldView, View newView) {

        // MainToolBar.getInstance().updateToolbar();
    }

}
