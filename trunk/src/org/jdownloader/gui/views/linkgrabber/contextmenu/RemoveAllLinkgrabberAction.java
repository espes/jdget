package org.jdownloader.gui.views.linkgrabber.contextmenu;

import java.awt.event.ActionEvent;

import jd.controlling.linkcrawler.CrawledLink;
import jd.controlling.linkcrawler.CrawledPackage;

import org.jdownloader.actions.AppAction;
import org.jdownloader.gui.translate._GUI;
import org.jdownloader.gui.views.SelectionInfo;
import org.jdownloader.gui.views.linkgrabber.actions.ClearLinkgrabberAction;

public class RemoveAllLinkgrabberAction extends AppAction {
    /**
     * 
     */
    private static final long serialVersionUID = 841782078416257540L;

    public RemoveAllLinkgrabberAction(SelectionInfo<CrawledPackage, CrawledLink> selection) {
        setName(_GUI._.RemoveAllLinkgrabberAction_RemoveAllLinkgrabberAction_object_());
        setIconKey("clear");
    }

    public void actionPerformed(ActionEvent e) {
        new ClearLinkgrabberAction().actionPerformed(e);
    }

}
