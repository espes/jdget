package org.jdownloader.gui.mainmenu.action;

import java.awt.event.ActionEvent;

import org.jdownloader.gui.translate._GUI;
import org.jdownloader.gui.views.SelectionInfo;
import org.jdownloader.gui.views.linkgrabber.actions.AddLinksAction;

public class AddLinksMenuAction extends AbstractMainMenuAction {

    /**
     * 
     */
    private static final long serialVersionUID = 1L;

    public AddLinksMenuAction(SelectionInfo<?, ?> selection) {
        setName(_GUI._.AddOptionsAction_actionPerformed_addlinks());
        setIconKey("add");

    }

    @Override
    public void actionPerformed(ActionEvent e) {
        new AddLinksAction().actionPerformed(e);
    }

    @Override
    public void setData(String data) {
    }

}
