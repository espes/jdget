package org.jdownloader.gui.views.linkgrabber.actions;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.util.ArrayList;

import javax.swing.AbstractAction;
import javax.swing.JButton;
import javax.swing.JPopupMenu;

import jd.controlling.packagecontroller.AbstractNode;
import jd.gui.swing.laf.LookAndFeelController;

import org.jdownloader.gui.views.linkgrabber.LinkGrabberTable;
import org.jdownloader.gui.views.linkgrabber.contextmenu.RemoveAllAction;
import org.jdownloader.gui.views.linkgrabber.contextmenu.RemoveIncompleteArchives;
import org.jdownloader.gui.views.linkgrabber.contextmenu.RemoveNonSelectedAction;
import org.jdownloader.gui.views.linkgrabber.contextmenu.RemoveOfflineAction;
import org.jdownloader.gui.views.linkgrabber.contextmenu.RemoveSelectionAction;
import org.jdownloader.images.NewTheme;

public class RemoveOptionsAction extends AbstractAction {
    /**
     * 
     */
    private static final long serialVersionUID = 7579020566025178078L;
    private JButton           positionComp;
    private LinkGrabberTable  table;

    {
        putValue(SMALL_ICON, NewTheme.I().getIcon("popupButton", -1));

    }

    public RemoveOptionsAction(LinkGrabberTable table, JButton addLinks) {
        positionComp = addLinks;
        this.table = table;
    }

    public void actionPerformed(ActionEvent e) {
        JPopupMenu popup = new JPopupMenu();
        ArrayList<AbstractNode> selection = table.getExtTableModel().getSelectedObjects();
        popup.add(new RemoveAllAction().toContextMenuAction());
        popup.add(new RemoveSelectionAction(selection).toContextMenuAction());
        popup.add(new RemoveNonSelectedAction(table, selection).toContextMenuAction());
        popup.add(new RemoveOfflineAction().toContextMenuAction());
        popup.add(new RemoveIncompleteArchives(selection).toContextMenuAction());

        int[] insets = LookAndFeelController.getInstance().getLAFOptions().getPopupBorderInsets();

        Dimension pref = popup.getPreferredSize();
        // pref.width = positionComp.getWidth() + ((Component)
        // e.getSource()).getWidth() + insets[1] + insets[3];
        popup.setPreferredSize(pref);

        popup.show(positionComp, -insets[1] - 1, -popup.getPreferredSize().height + insets[2]);
    }
}
