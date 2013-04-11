package org.jdownloader.gui.views.downloads.action;

import java.awt.event.ActionEvent;
import java.io.File;
import java.util.List;

import jd.controlling.IOEQ;
import jd.controlling.downloadcontroller.DownloadController;
import jd.plugins.DownloadLink;
import jd.plugins.FilePackage;

import org.appwork.uio.UIOManager;
import org.appwork.utils.swing.dialog.OKCancelCloseUserIODefinition.CloseReason;
import org.jdownloader.actions.AppAction;
import org.jdownloader.gui.translate._GUI;
import org.jdownloader.gui.views.SelectionInfo;
import org.jdownloader.gui.views.downloads.overviewpanel.AggregatedNumbers;
import org.jdownloader.gui.views.downloads.table.DownloadsTableModel;

public class ClearDownloadListAction extends AppAction {
    /**
     * 
     */
    private static final long serialVersionUID = 6027982395476716687L;

    public ClearDownloadListAction() {
        setIconKey("clear");
        putValue(SHORT_DESCRIPTION, _GUI._.ClearAction_tt_());
    }

    public void actionPerformed(ActionEvent e) {
        final List<DownloadLink> nodesToDelete = DownloadsTableModel.getInstance().getAllChildrenNodes();
        AggregatedNumbers agg = new AggregatedNumbers(new SelectionInfo<FilePackage, DownloadLink>(null, nodesToDelete));
        final ConfirmDeleteLinksDialogInterface d = UIOManager.I().show(ConfirmDeleteLinksDialogInterface.class, new ConfirmDeleteLinksDialog(_GUI._.ClearDownloadListAction_actionPerformed_(agg.getLinkCount(), DownloadController.getInstance().getChildrenCount() - agg.getLinkCount()), agg.getLoadedBytesString()));

        if (d.getCloseReason() == CloseReason.OK) {
            IOEQ.add(new Runnable() {

                public void run() {

                    DownloadController.getInstance().removeChildren(nodesToDelete);

                    if (d.isDeleteFilesFromDiskEnabled()) {
                        for (DownloadLink dl : nodesToDelete) {
                            if (dl.getLinkStatus().isFinished()) {
                                new File(dl.getFileOutput()).delete();
                            }
                        }
                    }
                }

            }, true);
        }

    }

}
