package org.jdownloader.gui.views.components.packagetable.context;

import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.List;

import jd.controlling.IOEQ;
import jd.controlling.linkchecker.LinkChecker;
import jd.controlling.linkcrawler.CheckableLink;
import jd.controlling.linkcrawler.CrawledLink;
import jd.controlling.packagecontroller.AbstractPackageChildrenNode;
import jd.controlling.packagecontroller.AbstractPackageNode;
import jd.plugins.DownloadLink;
import jd.plugins.DownloadLink.AvailableStatus;

import org.jdownloader.actions.SelectionAppAction;
import org.jdownloader.gui.translate._GUI;
import org.jdownloader.gui.views.SelectionInfo;

public class CheckStatusAction<PackageType extends AbstractPackageNode<ChildrenType, PackageType>, ChildrenType extends AbstractPackageChildrenNode<PackageType>> extends SelectionAppAction<PackageType, ChildrenType> {

    private static final long serialVersionUID = 6821943398259956694L;

    public CheckStatusAction(SelectionInfo<PackageType, ChildrenType> si) {
        super(si);
        setIconKey("ok");
        setName(_GUI._.gui_table_contextmenu_check());

    }

    public void actionPerformed(ActionEvent e) {
        if (!isEnabled()) return;
        IOEQ.add(new Runnable() {

            public void run() {
                List<ChildrenType> children = getSelection().getChildren();
                java.util.List<CheckableLink> checkableLinks = new ArrayList<CheckableLink>(children.size());
                for (AbstractPackageChildrenNode l : children) {
                    if (l instanceof DownloadLink) {
                        ((DownloadLink) l).setAvailableStatus(AvailableStatus.UNCHECKED);
                        checkableLinks.add(((DownloadLink) l));
                    } else if (l instanceof CrawledLink) {
                        ((CrawledLink) l).getDownloadLink().setAvailableStatus(AvailableStatus.UNCHECKED);
                        checkableLinks.add(((CrawledLink) l));
                    }
                }
                LinkChecker<CheckableLink> linkChecker = new LinkChecker<CheckableLink>(true);
                linkChecker.check(checkableLinks);
            }

        }, true);
    }

}