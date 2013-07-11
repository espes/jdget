package org.jdownloader.gui.views.downloads.action;

import java.awt.event.ActionEvent;

import jd.controlling.downloadcontroller.DownloadController;
import jd.plugins.DownloadLink;
import jd.plugins.FilePackage;

import org.jdownloader.actions.SelectionAppAction;
import org.jdownloader.gui.translate._GUI;
import org.jdownloader.gui.views.SelectionInfo;

public class DeleteSelectedLinks extends SelectionAppAction<FilePackage, DownloadLink> {

    public DeleteSelectedLinks(SelectionInfo<FilePackage, DownloadLink> si) {

        super(si);
        setName(_GUI._.DeleteAllAction_DeleteAllAction_object_());
        setIconKey("remove");
    }

    public void actionPerformed(ActionEvent e) {
        if (!isEnabled()) return;
        DownloadController.deleteLinksRequest(getSelection(), _GUI._.RemoveSelectionAction_actionPerformed_());
    }

}
