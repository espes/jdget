package org.jdownloader.gui.views.downloads.table;

import java.awt.Component;
import java.awt.event.MouseEvent;
import java.util.ArrayList;

import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.JSeparator;

import jd.controlling.packagecontroller.AbstractNode;
import jd.plugins.DownloadLink;
import jd.plugins.FilePackage;

import org.appwork.swing.exttable.ExtColumn;
import org.jdownloader.gui.menu.eventsender.MenuFactoryEvent;
import org.jdownloader.gui.menu.eventsender.MenuFactoryEventSender;
import org.jdownloader.gui.views.SelectionInfo;
import org.jdownloader.gui.views.components.packagetable.context.CheckStatusAction;
import org.jdownloader.gui.views.components.packagetable.context.PrioritySubMenu;
import org.jdownloader.gui.views.components.packagetable.context.SetCommentAction;
import org.jdownloader.gui.views.components.packagetable.context.SetDownloadPassword;
import org.jdownloader.gui.views.components.packagetable.context.URLEditorAction;
import org.jdownloader.gui.views.downloads.context.OpenInBrowserAction;
import org.jdownloader.gui.views.downloads.context.PackageNameAction;
import org.jdownloader.gui.views.downloads.context.SetDownloadFolderInDownloadTableAction;
import org.jdownloader.gui.views.downloads.contextmenumanager.DownloadListContextMenuManager;
import org.jdownloader.gui.views.linkgrabber.actions.AddContainerAction;
import org.jdownloader.gui.views.linkgrabber.actions.AddLinksAction;

public class DownloadTableContextMenuFactory {
    private static final DownloadTableContextMenuFactory INSTANCE = new DownloadTableContextMenuFactory();

    /**
     * get the only existing instance of DownloadTableContextMenuFactory. This is a singleton
     * 
     * @return
     */
    public static DownloadTableContextMenuFactory getInstance() {
        return DownloadTableContextMenuFactory.INSTANCE;
    }

    private DownloadListContextMenuManager manager;

    /**
     * Create a new instance of DownloadTableContextMenuFactory. This is a singleton class. Access the only existing instance by using
     * {@link #getInstance()}.
     */
    private DownloadTableContextMenuFactory() {
        manager = DownloadListContextMenuManager.getInstance();

    }

    public JPopupMenu create(DownloadsTable downloadsTable, JPopupMenu popup, AbstractNode contextObject, java.util.List<AbstractNode> selection, ExtColumn<AbstractNode> column, MouseEvent ev) {
        if (selection == null && contextObject == null) return emtpy(popup);

        SelectionInfo<FilePackage, DownloadLink> si = new SelectionInfo<FilePackage, DownloadLink>(contextObject, selection, ev, null, downloadsTable);
        si.setContextColumn(column);
        /* Properties */

        return manager.build(si);

        // if (si.isPackageContext()) {
        // Image back = (si.getFirstPackage().isExpanded() ? NewTheme.I().getImage("tree_package_open", 32) :
        // NewTheme.I().getImage("tree_package_closed", 32));
        // properties.setIcon(new ImageIcon(ImageProvider.merge(back, NewTheme.I().getImage("settings", 14), -16, 0, 6, 6)));
        //
        // } else if (si.isLinkContext()) {
        // Image back = (si.getLink().getIcon().getImage());
        // properties.setIcon(new ImageIcon(ImageProvider.merge(back, NewTheme.I().getImage("settings", 14), 0, 0, 6, 6)));
        //
        // }
        // for (Component mm : fillPropertiesMenu(si, column)) {
        // properties.add(mm);
        // }
        //
        // popup.add(properties);
        // popup.add(new JSeparator());
        //
        // /* Open file / folder */
        // if (CrossSystem.isOpenFileSupported()) {
        // if (si.isLinkContext() && new File(si.getContextLink().getFileOutput()).exists()) {
        // popup.add(new OpenFileAction(si));
        // }
        // popup.add(new OpenDirectoryAction(new File(si.getFirstPackage().getDownloadDirectory())));
        // }
        //
        // popup.add(new SortAction(si, column).toContextMenuAction());
        //
        // popup.add(new EnabledAction<FilePackage, DownloadLink>(si));
        // popup.add(new JSeparator());
        // popup.add(new ForceDownloadAction(si));
        //
        // popup.add(new StopsignAction(si));
        // popup.add(new JSeparator());
        //
        // /* addons */
        // int count = popup.getComponentCount();
        // MenuFactoryEventSender.getInstance().fireEvent(new MenuFactoryEvent(MenuFactoryEvent.Type.EXTEND, new DownloadTableContext(popup,
        // si, column)));
        // if (popup.getComponentCount() > count) {
        // popup.add(new JSeparator());
        // }
        //
        // /* other menu */
        // JMenu o = new JMenu(_GUI._.ContextMenuFactory_createPopup_other());
        // o.setIcon(NewTheme.I().getIcon("batch", 18));
        //
        // o.add(new ResumeAction(si));
        // o.add(new ResetAction(si));
        // o.add(new JSeparator());
        // o.add(new NewPackageAction(si));
        // o.add(new CreateDLCAction(si));
        //
        // popup.add(o);
        //
        // popup.add(new JSeparator());
        //
        // /* remove menu */
        // popup.add(new DeleteQuickAction(si));
        // JMenu m = new JMenu(_GUI._.ContextMenuFactory_createPopup_cleanup_only());
        // m.setIcon(NewTheme.I().getIcon("clear", 18));
        // // m.add(new DeleteSelectedLinks(si));
        // m.add(new DeleteDisabledSelectedLinks(si));
        // m.add(new DeleteSelectedAndFailedLinksAction(si));
        // m.add(new DeleteSelectedFinishedLinksAction(si));
        // m.add(new DeleteSelectedOfflineLinksAction(si));
        //
        // popup.add(m);
        //
        // return popup;
    }

    private JPopupMenu emtpy(JPopupMenu p) {

        p.add(new AddLinksAction().toContextMenuAction());
        p.add(new AddContainerAction().toContextMenuAction());

        return p;
    }

    public static java.util.List<Component> fillPropertiesMenu(SelectionInfo<FilePackage, DownloadLink> si, ExtColumn<AbstractNode> column) {

        java.util.List<Component> ret = new ArrayList<Component>();

        ret.add(new JMenuItem(new CheckStatusAction<FilePackage, DownloadLink>(si).toContextMenuAction()));

        OpenInBrowserAction openInBrowserAction = new OpenInBrowserAction(si);
        if (openInBrowserAction.isEnabled()) {
            ret.add(new JMenuItem(openInBrowserAction));
        }

        ret.add(new JMenuItem(new URLEditorAction<FilePackage, DownloadLink>(si)));

        ret.add(new JSeparator());

        if (si.isPackageContext()) {
            ret.add(new JMenuItem(new PackageNameAction(si)));
        }

        ret.add(new JMenuItem(new SetDownloadFolderInDownloadTableAction(si).toContextMenuAction()));
        ret.add(new JMenuItem(new SetDownloadPassword<FilePackage, DownloadLink>(si).toContextMenuAction()));
        ret.add(new JMenuItem(new SetCommentAction<FilePackage, DownloadLink>(si).toContextMenuAction()));

        ret.add(new PrioritySubMenu<FilePackage, DownloadLink>(si));
        MenuFactoryEventSender.getInstance().fireEvent(new MenuFactoryEvent(MenuFactoryEvent.Type.EXTEND, new DownloadTablePropertiesContext(ret, si, column)));
        return ret;
    }
}
