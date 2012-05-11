package org.jdownloader.gui.views.downloads.context;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import jd.controlling.downloadcontroller.DownloadController;
import jd.plugins.DownloadLink;
import jd.plugins.FilePackage;

import org.jdownloader.gui.views.SelectionInfo;
import org.jdownloader.gui.views.components.packagetable.context.SetDownloadFolderAction;

public class SetDownloadFolderInDownloadTableAction extends SetDownloadFolderAction<FilePackage, DownloadLink> {

    /**
     * 
     */

    public SetDownloadFolderInDownloadTableAction(SelectionInfo<FilePackage, DownloadLink> si) {
        super(si);

    }

    @Override
    protected void move(FilePackage pkg, List<DownloadLink> selectedLinksByPackage) {
        DownloadController.getInstance().moveOrAddAt(pkg, selectedLinksByPackage, -1);
    }

    @Override
    protected FilePackage createNewByPrototype(SelectionInfo<FilePackage, DownloadLink> si, FilePackage entry) {
        final FilePackage pkg = FilePackage.getInstance();
        pkg.setExpanded(true);
        pkg.setCreated(System.currentTimeMillis());
        pkg.setName(entry.getName());
        pkg.setComment(entry.getComment());
        pkg.setPasswordList(new ArrayList<String>(Arrays.asList(entry.getPasswordList())));
        pkg.getProperties().putAll(entry.getProperties());

        return pkg;
    }

    @Override
    protected void set(FilePackage pkg, String absolutePath) {
        pkg.setDownloadDirectory(absolutePath);
    }

}
