package org.jdownloader.gui.views.linkgrabber.contextmenu;

import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.List;

import jd.controlling.linkcollector.LinkCollector;
import jd.controlling.linkcrawler.CrawledLink;
import jd.controlling.linkcrawler.CrawledLink.LinkState;
import jd.controlling.linkcrawler.CrawledPackage;
import jd.controlling.packagecontroller.AbstractPackageChildrenNodeFilter;

import org.appwork.utils.event.queue.QueueAction;
import org.appwork.utils.swing.dialog.Dialog;
import org.appwork.utils.swing.dialog.DialogNoAnswerException;
import org.jdownloader.actions.SelectionAppAction;
import org.jdownloader.gui.translate._GUI;
import org.jdownloader.gui.views.SelectionInfo;

public class RemoveOfflineAction extends SelectionAppAction<CrawledPackage, CrawledLink> {

    /**
     * 
     */
    private static final long serialVersionUID = -6341297356888158708L;

    public RemoveOfflineAction(SelectionInfo<CrawledPackage, CrawledLink> selection) {
        super(selection);
        setName(_GUI._.RemoveOfflineAction_RemoveOfflineAction_object_());
        setIconKey("remove_offline");
    }

    public void actionPerformed(ActionEvent e) {
        if (!isEnabled()) return;
        try {
            Dialog.getInstance().showConfirmDialog(0, _GUI._.literally_are_you_sure(), _GUI._.ClearAction_actionPerformed_offline_msg(), null, _GUI._.literally_yes(), _GUI._.literall_no());
            LinkCollector.getInstance().getQueue().add(new QueueAction<Void, RuntimeException>() {

                @Override
                protected Void run() throws RuntimeException {
                    List<CrawledLink> offline = null;
                    if (getSelection() != null) {
                        offline = new ArrayList<CrawledLink>();
                        for (CrawledLink link : getSelection().getChildren()) {
                            if (LinkState.OFFLINE.equals(link.getLinkState())) offline.add(link);
                        }
                    } else {
                        offline = LinkCollector.getInstance().getChildrenByFilter(new AbstractPackageChildrenNodeFilter<CrawledLink>() {

                            public boolean acceptNode(CrawledLink node) {
                                return LinkState.OFFLINE.equals(node.getLinkState());
                            }

                            public int returnMaxResults() {
                                return -1;
                            }

                        });
                    }
                    LinkCollector.getInstance().removeChildren(offline);
                    return null;
                }
            });
        } catch (DialogNoAnswerException e1) {
        }
    }

    @Override
    public boolean isEnabled() {
        if (super.isEnabled()) {
            if (getSelection() != null && getSelection().getChildren().size() > 0) {
                for (CrawledLink link : getSelection().getChildren()) {
                    if (LinkState.OFFLINE.equals(link.getLinkState())) return true;
                }
                return false;
            } else {
                return true;
            }
        }
        return false;
    }
}
