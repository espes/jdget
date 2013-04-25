package org.jdownloader.gui.views.linkgrabber.contextmenu;

import java.awt.Image;
import java.awt.event.MouseEvent;
import java.util.ArrayList;

import javax.swing.ImageIcon;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.JSeparator;

import jd.controlling.linkcrawler.CrawledLink;
import jd.controlling.linkcrawler.CrawledPackage;
import jd.controlling.packagecontroller.AbstractNode;

import org.appwork.storage.config.JsonConfig;
import org.appwork.swing.exttable.ExtColumn;
import org.appwork.utils.ImageProvider.ImageProvider;
import org.jdownloader.gui.menu.eventsender.MenuFactoryEvent;
import org.jdownloader.gui.menu.eventsender.MenuFactoryEventSender;
import org.jdownloader.gui.translate._GUI;
import org.jdownloader.gui.views.SelectionInfo;
import org.jdownloader.gui.views.components.packagetable.context.CheckStatusAction;
import org.jdownloader.gui.views.components.packagetable.context.EnabledAction;
import org.jdownloader.gui.views.components.packagetable.context.PrioritySubMenu;
import org.jdownloader.gui.views.components.packagetable.context.SetCommentAction;
import org.jdownloader.gui.views.components.packagetable.context.SetDownloadPassword;
import org.jdownloader.gui.views.components.packagetable.context.URLEditorAction;
import org.jdownloader.gui.views.linkgrabber.LinkGrabberPanel;
import org.jdownloader.gui.views.linkgrabber.LinkGrabberTable;
import org.jdownloader.gui.views.linkgrabber.actions.AddContainerAction;
import org.jdownloader.gui.views.linkgrabber.actions.AddLinksAction;
import org.jdownloader.gui.views.linkgrabber.actions.ConfirmAction;
import org.jdownloader.gui.views.linkgrabber.addlinksdialog.LinkgrabberSettings;
import org.jdownloader.images.NewTheme;

public class ContextMenuFactory {

    private LinkGrabberTable table;
    private LinkGrabberPanel panel;

    public ContextMenuFactory(LinkGrabberTable linkGrabberTable, LinkGrabberPanel linkGrabberPanel) {
        this.table = linkGrabberTable;
        this.panel = linkGrabberPanel;
    }

    public JPopupMenu createPopup(AbstractNode context, java.util.List<AbstractNode> selection, ExtColumn<AbstractNode> column, MouseEvent event) {

        SelectionInfo<CrawledPackage, CrawledLink> si = new SelectionInfo<CrawledPackage, CrawledLink>(context, selection, event, null, table);
        si.setContextColumn(column);
        JPopupMenu p = new JPopupMenu();
        JMenu m;

        if (selection != null && selection.size() > 0) {
            p.add(new ConfirmAction(si.isShiftDown(), si));
            p.add(new JSeparator());
        }

        if (selection == null || selection.size() == 0 || JsonConfig.create(LinkgrabberSettings.class).isContextMenuAddLinksActionAlwaysVisible()) {
            p.add(new AddLinksAction());
            p.add(new AddContainerAction());
            if (selection == null || selection.size() == 0) { return p; }
            p.add(new JSeparator());
        }

        JMenu properties = new JMenu(_GUI._.ContextMenuFactory_createPopup_properties_package());
        p.add(properties);
        p.add(new JSeparator());
        if (si.isPackageContext()) {
            Image back = (si.getPackage().isExpanded() ? NewTheme.I().getImage("tree_package_open", 32) : NewTheme.I().getImage("tree_package_closed", 32));
            properties.setIcon(new ImageIcon(ImageProvider.merge(back, NewTheme.I().getImage("settings", 14), -16, 0, 6, 6)));
        } else if (si.isLinkContext()) {
            Image back = (si.getLink().getDownloadLink().getIcon().getImage());
            properties.setIcon(new ImageIcon(ImageProvider.merge(back, NewTheme.I().getImage("settings", 14), 0, 0, 6, 6)));

        }
        for (JMenuItem mm : fillPropertiesMenu(si, column)) {
            properties.add(mm);
        }
        p.add(new SortAction(si));
        p.add(new EnabledAction(si));

        p.add(new JSeparator());
        if (si.isLinkContext()) {
            p.add(new OpenUrlAction(si.getLink()));
            p.add(new JSeparator());
        }
        // addons
        int count = p.getComponentCount();
        MenuFactoryEventSender.getInstance().fireEvent(new MenuFactoryEvent(MenuFactoryEvent.Type.EXTEND, new LinkgrabberTableContext(p, si, column)));
        if (p.getComponentCount() > count) p.add(new JSeparator());

        // others
        JMenu o = new JMenu(_GUI._.ContextMenuFactory_createPopup_other());
        o.setIcon(NewTheme.I().getIcon("batch", 18));
        o.add(new CreateDLCAction(si));
        o.add(new MergeToPackageAction(si));
        o.add(new SplitPackagesByHost(si));

        p.add(o);
        p.add(new JSeparator());
        /* remove menu */
        p.add(new RemoveSelectionLinkgrabberAction(si));
        m = new JMenu(_GUI._.ContextMenuFactory_linkgrabber_createPopup_cleanup());
        m.setIcon(NewTheme.I().getIcon("clear", 18));

        m.add(new RemoveAllLinkgrabberAction());
        m.add(new RemoveNonSelectedAction(si));
        m.add(new RemoveOfflineAction());
        m.add(new RemoveIncompleteArchives(si));
        m.add(new JSeparator());
        m.add(new ResetPopupAction(panel));
        p.add(m);

        return p;
    }

    public static java.util.List<JMenuItem> fillPropertiesMenu(SelectionInfo<CrawledPackage, CrawledLink> si, ExtColumn<AbstractNode> column) {

        java.util.List<JMenuItem> ret = new ArrayList<JMenuItem>();
        ret.add(new JMenuItem(new CheckStatusAction<CrawledPackage, CrawledLink>(si)));

        ret.add(new JMenuItem(new URLEditorAction(si)));
        ret.add(new JMenuItem(new SetDownloadFolderInLinkgrabberAction(si)));
        ret.add(new JMenuItem(new SetDownloadPassword(si)));
        ret.add(new JMenuItem(new SetCommentAction(si)));
        ret.add(new PrioritySubMenu(si));
        MenuFactoryEventSender.getInstance().fireEvent(new MenuFactoryEvent(MenuFactoryEvent.Type.EXTEND, new LinkgrabberTablePropertiesContext(ret, si, column)));
        return ret;
    }

}
