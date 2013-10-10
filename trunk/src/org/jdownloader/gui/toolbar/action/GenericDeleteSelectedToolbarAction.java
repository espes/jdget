package org.jdownloader.gui.toolbar.action;

import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.List;

import jd.controlling.linkcollector.LinkCollector;
import jd.controlling.linkcrawler.CrawledLink;
import jd.controlling.linkcrawler.CrawledPackage;
import jd.controlling.packagecontroller.AbstractNode;
import jd.plugins.DownloadLink;
import jd.plugins.DownloadLink.AvailableStatus;
import jd.plugins.FilePackage;

import org.appwork.utils.swing.dialog.Dialog;
import org.appwork.utils.swing.dialog.DialogNoAnswerException;
import org.jdownloader.controlling.contextmenu.Customizer;
import org.jdownloader.gui.IconKey;
import org.jdownloader.gui.translate._GUI;
import org.jdownloader.gui.views.SelectionInfo;
import org.jdownloader.gui.views.components.packagetable.PackageControllerTable;
import org.jdownloader.gui.views.downloads.action.DeleteSelectedLinks;
import org.jdownloader.gui.views.downloads.table.DownloadsTable;
import org.jdownloader.gui.views.linkgrabber.LinkGrabberTable;
import org.jdownloader.plugins.FinalLinkState;

public class GenericDeleteSelectedToolbarAction extends SelectionBasedToolbarAction {

    public static final String DELETE_DISABLED   = "deleteDisabled";
    public static final String DELETE_FAILED     = "deleteFailed";
    public static final String DELETE_FINISHED   = "deleteFinished";
    public static final String DELETE_OFFLINE    = "deleteOffline";
    private List<AbstractNode> currentSelection;

    private boolean            deleteDisabled    = false;
    private boolean            onlySelectedItems = false;

    @Customizer(name = "Only Selected Links")
    public boolean isOnlySelectedItems() {
        return onlySelectedItems;
    }

    public void setOnlySelectedItems(boolean onlySelectedItems) {
        this.onlySelectedItems = onlySelectedItems;
    }

    private boolean            deleteFailed   = false;

    private boolean            deleteFinished = false;

    private boolean            deleteOffline  = false;
    private List<DownloadLink> filteredDownloadLinks;
    private List<CrawledLink>  filteredCrawledLinks;

    public GenericDeleteSelectedToolbarAction() {
        super();
        setIconKey(IconKey.ICON_DELETE);
        // DeleteDisabledSelectedLinks
        setName(_GUI._.DeleteDisabledSelectedLinksToolbarAction_object_());

        setEnabled(true);

    }

    @Override
    public void actionPerformed(ActionEvent e) {
        update();
        List<AbstractNode> lcurrentSelection = currentSelection;
        List<DownloadLink> lfilteredDownloadLinks = filteredDownloadLinks;
        List<CrawledLink> lfilteredCrawledLinks = filteredCrawledLinks;
        PackageControllerTable<?, ?> ltable = getTable();
        if (lcurrentSelection != null && ltable != null) {
            if (ltable instanceof DownloadsTable && lfilteredDownloadLinks != null) {
                SelectionInfo<FilePackage, DownloadLink> si = new SelectionInfo<FilePackage, DownloadLink>(null, lfilteredDownloadLinks, null, null, e, (DownloadsTable) ltable);
                if (si.getChildren().size() > 0) {
                    new DeleteSelectedLinks(si).deleteLinksRequest(si, getName() + "(" + si.getChildren().size() + ")" + "\r\n" + _GUI._.lit_are_you_sure());
                    return;
                }
            } else if (ltable instanceof LinkGrabberTable && lfilteredCrawledLinks != null && lfilteredCrawledLinks.size() > 0) {
                try {

                    Dialog.getInstance().showConfirmDialog(0, _GUI._.literally_are_you_sure(), getName() + "(" + lfilteredCrawledLinks.size() + ")" + "\r\n" + _GUI._.lit_are_you_sure(), null, _GUI._.literally_yes(), _GUI._.literall_no());
                    LinkCollector.getInstance().removeChildren(lfilteredCrawledLinks);

                } catch (DialogNoAnswerException e1) {
                }
                return;
            }
        }
        Dialog.getInstance().showErrorDialog(_GUI._.GenericDeleteSelectedToolbarAction_actionPerformed_nothing_to_delete_());
    }

    private void update() {

        if (isOnlySelectedItems()) {
            currentSelection = getTable().getModel().getSelectedObjects();
        } else {
            currentSelection = getTable().getModel().getElements();
        }
        filteredDownloadLinks = null;
        filteredCrawledLinks = null;
        PackageControllerTable<?, ?> ltable = getTable();

        if (currentSelection != null && ltable != null && currentSelection.size() > 0) {
            if (ltable instanceof DownloadsTable) {

                SelectionInfo<FilePackage, DownloadLink> si = new SelectionInfo<FilePackage, DownloadLink>(null, currentSelection, null, null, null, (DownloadsTable) ltable);

                List<DownloadLink> nodesToDelete = new ArrayList<DownloadLink>();
                for (DownloadLink dl : si.getChildren()) {
                    if (isDeleteDisabled() && !dl.isEnabled()) {
                        nodesToDelete.add(dl);
                        continue;
                    }
                    if (isDeleteFailed() && FinalLinkState.CheckFailed(dl.getFinalLinkState())) {
                        nodesToDelete.add(dl);
                        continue;
                    }
                    if (isDeleteFinished() && FinalLinkState.CheckFinished(dl.getFinalLinkState())) {
                        nodesToDelete.add(dl);
                        continue;
                    }
                    if (isDeleteOffline() && dl.getFinalLinkState() == FinalLinkState.OFFLINE) {
                        nodesToDelete.add(dl);
                        continue;
                    }
                }
                filteredDownloadLinks = nodesToDelete;

            } else if (ltable instanceof LinkGrabberTable) {

                SelectionInfo<CrawledPackage, CrawledLink> si = new SelectionInfo<CrawledPackage, CrawledLink>(null, currentSelection, null, null, null, (LinkGrabberTable) ltable);
                List<CrawledLink> filtered = new ArrayList<CrawledLink>();
                for (CrawledLink cl : si.getChildren()) {
                    if (isDeleteDisabled() && !cl.isEnabled()) {
                        filtered.add(cl);
                        continue;
                    }

                    if (isDeleteOffline() && cl.getDownloadLink().isAvailabilityStatusChecked() && cl.getDownloadLink().getAvailableStatus() == AvailableStatus.FALSE) {
                        filtered.add(cl);
                        continue;
                    }
                }
                filteredCrawledLinks = filtered;

            }
        }
    }

    @Override
    protected String createTooltip() {
        return getName();
    }

    @Customizer(name = "Include disabled Links")
    public boolean isDeleteDisabled() {
        return deleteDisabled;
    }

    @Customizer(name = "Include failed")
    public boolean isDeleteFailed() {
        return deleteFailed;
    }

    @Customizer(name = "Include finished Links")
    public boolean isDeleteFinished() {
        return deleteFinished;
    }

    @Customizer(name = "Include Offline Links")
    public boolean isDeleteOffline() {
        return deleteOffline;
    }

    public boolean isEnabled() {
        return enabled;
    }

    @Override
    protected void onSelectionUpdate(List<AbstractNode> list) {

    }

    public void setDeleteDisabled(boolean deleteDisabled) {
        this.deleteDisabled = deleteDisabled;
        updateName();
    }

    public void setDeleteFailed(boolean deleteFailed) {
        this.deleteFailed = deleteFailed;
        updateName();
    }

    public void setDeleteFinished(boolean deleteFinished) {
        this.deleteFinished = deleteFinished;
        updateName();
    }

    public void setDeleteOffline(boolean deleteOffline) {
        this.deleteOffline = deleteOffline;
        updateName();
    }

    private void updateName() {

        // if (isDeleteFailed() && isDeleteDisabled() && isDeleteFinished() && isDeleteOffline()) {
        // setName(_GUI._.ContextMenuFactory_createPopup_cleanup_only());
        // } else {
        StringBuilder sb = new StringBuilder();
        if (isOnlySelectedItems()) {
            sb.append(_GUI._.GenericDeleteSelectedToolbarAction_updateName_object_());
        } else {
            sb.append(_GUI._.GenericDeleteSelectedToolbarAction_updateName_object_all());
        }
        boolean first = true;
        if (isDeleteDisabled()) {
            if (!first) sb.append(" & ");
            sb.append(_GUI._.lit_disabled());
            first = false;
        }
        if (isDeleteFailed()) {
            if (!first) sb.append(" & ");
            first = false;
            sb.append(_GUI._.lit_failed());
        }
        if (isDeleteFinished()) {
            if (!first) sb.append(" & ");
            first = false;
            sb.append(_GUI._.lit_finished());
        }
        if (isDeleteOffline()) {
            if (!first) sb.append(" & ");
            first = false;
            sb.append(_GUI._.lit_offline());
        }
        setName(sb.toString());
        // }

    }

}
