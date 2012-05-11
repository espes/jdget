package org.jdownloader.gui.views.downloads.table;

import java.util.ArrayList;

import javax.swing.JMenuItem;

import jd.controlling.packagecontroller.AbstractNode;
import jd.plugins.DownloadLink;
import jd.plugins.FilePackage;

import org.appwork.swing.exttable.ExtColumn;
import org.jdownloader.gui.menu.MenuContext;
import org.jdownloader.gui.views.SelectionInfo;

public class DownloadTablePropertiesContext extends MenuContext<ArrayList<JMenuItem>> {

    private ExtColumn<AbstractNode>                  clickedColumn;
    private SelectionInfo<FilePackage, DownloadLink> selectionInfo;

    public DownloadTablePropertiesContext(ArrayList<JMenuItem> popup, SelectionInfo<FilePackage, DownloadLink> si, ExtColumn<AbstractNode> column) {
        super(popup);

        this.selectionInfo = si;
        clickedColumn = column;

    }

    public SelectionInfo<FilePackage, DownloadLink> getSelectionInfo() {
        return selectionInfo;
    }

    public ExtColumn<AbstractNode> getClickedColumn() {
        return clickedColumn;
    }

}
