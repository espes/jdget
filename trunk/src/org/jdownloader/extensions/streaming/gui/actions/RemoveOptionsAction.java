package org.jdownloader.extensions.streaming.gui.actions;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.util.ArrayList;

import javax.swing.AbstractAction;
import javax.swing.JButton;
import javax.swing.JPopupMenu;

import jd.controlling.packagecontroller.AbstractNode;
import jd.gui.swing.laf.LookAndFeelController;

import org.jdownloader.extensions.streaming.gui.MediaArchiveTable;
import org.jdownloader.gui.views.downloads.table.DownloadsTable;
import org.jdownloader.images.NewTheme;

public class RemoveOptionsAction extends AbstractAction {
    /**
     * 
     */
    private static final long serialVersionUID = 7579020566025178078L;
    private JButton           positionComp;
    private DownloadsTable    table;

    {
        putValue(SMALL_ICON, NewTheme.I().getIcon("clear", 20));

    }

    public RemoveOptionsAction(DownloadsTable table, JButton addLinks) {
        positionComp = addLinks;
        this.table = table;
    }

    public RemoveOptionsAction(MediaArchiveTable table2) {
    }

    public void actionPerformed(ActionEvent e) {
        JPopupMenu popup = new JPopupMenu();
        java.util.List<AbstractNode> selection = table.getExtTableModel().getSelectedObjects();

        // popup.add(new CleanupDownloads());
        // popup.add(new CleanupPackages());
        // popup.addSeparator();
        // popup.add(new RemoveDupesAction());
        // popup.add(new RemoveDisabledAction());
        // popup.add(new RemoveOfflineAction());
        // popup.add(new RemoveFailedAction());
        int[] insets = LookAndFeelController.getInstance().getLAFOptions().getPopupBorderInsets();

        Dimension pref = popup.getPreferredSize();
        // pref.width = positionComp.getWidth() + ((Component)
        // e.getSource()).getWidth() + insets[1] + insets[3];
        popup.setPreferredSize(pref);

        popup.show(positionComp, -insets[1] - 1, -popup.getPreferredSize().height + insets[2]);
    }
}
