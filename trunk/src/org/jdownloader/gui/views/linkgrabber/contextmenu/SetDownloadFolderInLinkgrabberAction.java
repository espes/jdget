package org.jdownloader.gui.views.linkgrabber.contextmenu;

import java.io.File;
import java.util.List;

import jd.controlling.linkcollector.LinkCollector;
import jd.controlling.linkcollector.LinknameCleaner;
import jd.controlling.linkcrawler.CrawledLink;
import jd.controlling.linkcrawler.CrawledPackage;
import jd.controlling.linkcrawler.CrawledPackage.TYPE;

import org.appwork.utils.event.queue.Queue;
import org.appwork.utils.swing.dialog.DialogCanceledException;
import org.appwork.utils.swing.dialog.DialogClosedException;
import org.jdownloader.gui.translate._GUI;
import org.jdownloader.gui.views.DownloadFolderChooserDialog;
import org.jdownloader.gui.views.SelectionInfo;
import org.jdownloader.gui.views.components.packagetable.context.SetDownloadFolderAction;

public class SetDownloadFolderInLinkgrabberAction extends SetDownloadFolderAction<CrawledPackage, CrawledLink> {

    /**
     * 
     */
    private static final long serialVersionUID = -6632019767606316873L;

    public SetDownloadFolderInLinkgrabberAction() {

    }

    public SetDownloadFolderInLinkgrabberAction(SelectionInfo<CrawledPackage, CrawledLink> selectionInfo) {
        this();
        selection = selectionInfo;

    }

    protected File dialog(File path) throws DialogClosedException, DialogCanceledException {
        return DownloadFolderChooserDialog.open(path, true, _GUI._.OpenDownloadFolderAction_actionPerformed_object_(getSelection().getContextPackage().getName()));
    }

    @Override
    protected void set(CrawledPackage pkg, String absolutePath) {
        pkg.setDownloadFolder(absolutePath);
    }

    @Override
    protected CrawledPackage createNewByPrototype(SelectionInfo<CrawledPackage, CrawledLink> si, CrawledPackage entry) {
        final CrawledPackage pkg = new CrawledPackage();
        pkg.setExpanded(true);
        if (TYPE.NORMAL != entry.getType()) {
            final String pkgName = LinknameCleaner.cleanFileName(getSelection().getPackageView(entry).getChildren().get(0).getName(), false, true, LinknameCleaner.EXTENSION_SETTINGS.REMOVE_ALL, true);
            pkg.setName(pkgName);
        } else {
            pkg.setName(entry.getName());
        }
        pkg.setComment(entry.getComment());

        return pkg;
    }

    @Override
    protected void move(CrawledPackage pkg, List<CrawledLink> selectedLinksByPackage) {
        LinkCollector.getInstance().moveOrAddAt(pkg, selectedLinksByPackage, -1);
    }

    @Override
    protected Queue getQueue() {
        return LinkCollector.getInstance().getQueue();
    }

}
