package org.jdownloader.gui.views.downloads.context;

import java.awt.event.ActionEvent;
import java.util.ArrayList;

import javax.swing.JMenu;

import jd.gui.swing.jdgui.interfaces.ContextMenuAction;
import jd.plugins.DownloadLink;

import org.jdownloader.gui.translate._GUI;
import org.jdownloader.images.NewTheme;

public class PrioritySubMenu extends ContextMenuAction {

    public static String[]                PRIO_DESCS       = new String[] { _GUI._.gui_treetable_tooltip_priority_1(), _GUI._.gui_treetable_tooltip_priority0(), _GUI._.gui_treetable_tooltip_priority1(), _GUI._.gui_treetable_tooltip_priority2(), _GUI._.gui_treetable_tooltip_priority3() };
    private static final long             serialVersionUID = 4016589318975322111L;

    private final ArrayList<DownloadLink> links;
    private final int                     priority;

    public PrioritySubMenu(ArrayList<DownloadLink> links, int priority) {
        this.links = links;
        this.priority = priority;

        init();
    }

    @Override
    protected String getIcon() {
        return "prio_" + priority;
    }

    @Override
    protected String getName() {
        return PRIO_DESCS[priority + 1];
    }

    @Override
    public boolean isEnabled() {
        return links.size() != 1 || links.get(0).getPriority() != priority;
    }

    public void actionPerformed(ActionEvent e) {
        for (DownloadLink link : links) {
            link.setPriority(priority);
        }
    }

    public static JMenu createPrioMenu(final ArrayList<DownloadLink> links) {
        final JMenu prioPopup = new JMenu(_GUI._.gui_table_contextmenu_priority() + " (" + links.size() + ")");
        prioPopup.setIcon(NewTheme.I().getIcon("prio_0", 16));
        prioPopup.add(new PrioritySubMenu(links, 3));
        prioPopup.add(new PrioritySubMenu(links, 2));
        prioPopup.add(new PrioritySubMenu(links, 1));
        prioPopup.add(new PrioritySubMenu(links, 0));
        prioPopup.add(new PrioritySubMenu(links, -1));
        return prioPopup;
    }

}
