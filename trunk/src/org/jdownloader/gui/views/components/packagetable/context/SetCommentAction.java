package org.jdownloader.gui.views.components.packagetable.context;

import java.awt.event.ActionEvent;

import jd.controlling.IOEQ;
import jd.controlling.linkcrawler.CrawledLink;
import jd.controlling.linkcrawler.CrawledPackage;
import jd.controlling.packagecontroller.AbstractNode;
import jd.controlling.packagecontroller.AbstractPackageChildrenNode;
import jd.controlling.packagecontroller.AbstractPackageNode;
import jd.plugins.DownloadLink;
import jd.plugins.FilePackage;

import org.appwork.utils.StringUtils;
import org.appwork.utils.swing.dialog.Dialog;
import org.appwork.utils.swing.dialog.DialogCanceledException;
import org.appwork.utils.swing.dialog.DialogClosedException;
import org.jdownloader.actions.SelectionAppAction;
import org.jdownloader.gui.translate._GUI;
import org.jdownloader.gui.views.SelectionInfo;

public class SetCommentAction<PackageType extends AbstractPackageNode<ChildrenType, PackageType>, ChildrenType extends AbstractPackageChildrenNode<PackageType>> extends SelectionAppAction<FilePackage, DownloadLink> {

    public SetCommentAction(SelectionInfo<FilePackage, DownloadLink> si) {
        super(si);
        setName(_GUI._.SetCommentAction_SetCommentAction_object_());
        setIconKey("list");
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        String def = null;
        for (AbstractNode n : getSelection().getRawSelection()) {
            if (n instanceof DownloadLink) {
                def = ((DownloadLink) n).getComment();
            } else if (n instanceof CrawledLink) {
                def = ((CrawledLink) n).getDownloadLink().getComment();
            } else if (n instanceof FilePackage) {
                def = ((FilePackage) n).getComment();
            } else if (n instanceof CrawledPackage) {
                def = ((CrawledPackage) n).getComment();
            }
            if (!StringUtils.isEmpty(def)) break;
        }

        try {
            final String comment = Dialog.getInstance().showInputDialog(Dialog.STYLE_LARGE | Dialog.STYLE_HIDE_ICON, _GUI._.SetCommentAction_actionPerformed_dialog_title_(), "", def, null, null, null);
            IOEQ.add(new Runnable() {

                @Override
                public void run() {
                    for (AbstractNode n : getSelection().getRawSelection()) {
                        if (n instanceof DownloadLink) {
                            ((DownloadLink) n).setComment(comment);
                        } else if (n instanceof CrawledLink) {
                            ((CrawledLink) n).getDownloadLink().setComment(comment);
                        } else if (n instanceof FilePackage) {
                            ((FilePackage) n).setComment(comment);
                        } else if (n instanceof CrawledPackage) {
                            ((CrawledPackage) n).setComment(comment);
                        }
                    }
                }
            }, true);

        } catch (DialogClosedException e1) {
        } catch (DialogCanceledException e1) {
        }
    }
}
