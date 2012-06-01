package org.jdownloader.gui.views.linkgrabber.actions;

import java.awt.Dimension;
import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.JButton;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;

import jd.gui.swing.laf.LookAndFeelController;

import org.jdownloader.gui.translate._GUI;
import org.jdownloader.images.NewTheme;

public class AddOptionsAction extends AbstractAction {
    /**
     * 
     */
    private static final long serialVersionUID = -1041794723138925672L;
    private JButton           positionComp;

    {
        putValue(SMALL_ICON, NewTheme.I().getIcon("popupButton", -1));

    }

    public AddOptionsAction(JButton addLinks) {
        positionComp = addLinks;
    }

    public void actionPerformed(ActionEvent e) {
        JPopupMenu popup = new JPopupMenu();
        AddLinksAction ala = new AddLinksAction();
        ala.putValue(AbstractAction.NAME, _GUI._.AddOptionsAction_actionPerformed_addlinks());
        popup.add(new JMenuItem(ala));
        popup.add(new JMenuItem(new AddContainerAction().toContextMenuAction()));
        int[] insets = LookAndFeelController.getInstance().getLAFOptions().getPopupBorderInsets();

        Dimension pref = popup.getPreferredSize();
        // pref.width = positionComp.getWidth() + ((Component)
        // e.getSource()).getWidth() + insets[1] + insets[3];
        popup.setPreferredSize(pref);

        popup.show(positionComp, -insets[1] - 1, -popup.getPreferredSize().height + insets[2]);
    }

}
