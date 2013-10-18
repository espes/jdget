package org.jdownloader.gui.views.linkgrabber.bottombar;

import java.awt.event.ActionEvent;

import org.jdownloader.actions.AppAction;
import org.jdownloader.actions.CachableInterface;
import org.jdownloader.gui.IconKey;
import org.jdownloader.gui.translate._GUI;

public class BottomBarMenuManagerAction extends AppAction implements CachableInterface {

    public BottomBarMenuManagerAction() {

        setName(_GUI._.BottomBarMenuManagerAction_BottomBarMenuManagerAction());
        setIconKey(IconKey.ICON_EDIT);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        MenuManagerLinkgrabberTabBottombar.getInstance().openGui();

    }

    @Override
    public void setData(String data) {
    }

}
