package org.jdownloader.gui.views.components.packagetable.context;

import java.awt.event.ActionEvent;
import java.util.List;

import jd.controlling.IOEQ;
import jd.controlling.linkcrawler.CrawledLink;
import jd.controlling.packagecontroller.AbstractNode;
import jd.controlling.packagecontroller.AbstractPackageChildrenNode;
import jd.controlling.packagecontroller.AbstractPackageNode;
import jd.plugins.DownloadLink;

import org.appwork.utils.swing.dialog.Dialog;
import org.appwork.utils.swing.dialog.DialogNoAnswerException;
import org.jdownloader.actions.AppAction;
import org.jdownloader.gui.translate._GUI;
import org.jdownloader.gui.views.SelectionInfo;
import org.jdownloader.images.NewTheme;

public class SetDownloadPassword<PackageType extends AbstractPackageNode<ChildrenType, PackageType>, ChildrenType extends AbstractPackageChildrenNode<PackageType>> extends AppAction {

    /**
     * 
     */
    private static final long                        serialVersionUID = -8280535886054721277L;
    private SelectionInfo<PackageType, ChildrenType> si;

    public SetDownloadPassword(SelectionInfo<PackageType, ChildrenType> si) {
        this.si = si;
        setName(_GUI._.SetDownloadPassword_SetDownloadPassword_());
        setIconKey("password");
    }

    public void actionPerformed(ActionEvent e) {
        if (!isEnabled()) return;
        final List<ChildrenType> newSelection = si.getChildren();
        String defaultPW = "";
        AbstractNode node = newSelection.get(0);
        if (node instanceof DownloadLink) {
            defaultPW = ((DownloadLink) node).getDownloadPassword();
        } else if (node instanceof CrawledLink) {
            defaultPW = ((CrawledLink) node).getDownloadLink().getDownloadPassword();
        }
        try {
            final String newPW = Dialog.getInstance().showInputDialog(0, _GUI._.SetDownloadPassword_SetDownloadPassword_(), _GUI._.jd_gui_userio_defaulttitle_input(), defaultPW, NewTheme.I().getIcon("password", 32), null, null);
            IOEQ.add(new Runnable() {

                public void run() {
                    for (AbstractNode l : newSelection) {
                        DownloadLink dl = null;
                        if (l instanceof CrawledLink) {
                            dl = ((CrawledLink) l).getDownloadLink();
                        } else if (l instanceof DownloadLink) {
                            dl = (DownloadLink) l;
                        }
                        if (dl != null) dl.setDownloadPassword(newPW);
                    }

                }

            });
        } catch (DialogNoAnswerException e1) {
        }
    }

    @Override
    public boolean isEnabled() {
        return !si.isEmpty();
    }

}
