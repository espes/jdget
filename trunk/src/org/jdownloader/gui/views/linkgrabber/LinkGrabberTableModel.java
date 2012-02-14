package org.jdownloader.gui.views.linkgrabber;

import java.util.ArrayList;

import jd.controlling.linkcollector.LinkCollector;
import jd.controlling.linkcrawler.CrawledLink;
import jd.controlling.linkcrawler.CrawledLink.LinkState;
import jd.controlling.linkcrawler.CrawledPackage;
import jd.controlling.packagecontroller.AbstractNode;

import org.appwork.swing.exttable.ExtColumn;
import org.jdownloader.gui.views.components.packagetable.PackageControllerTableModel;
import org.jdownloader.gui.views.downloads.columns.AvailabilityColumn;
import org.jdownloader.gui.views.downloads.columns.CommentColumn;
import org.jdownloader.gui.views.downloads.columns.FileColumn;
import org.jdownloader.gui.views.downloads.columns.HosterColumn;
import org.jdownloader.gui.views.downloads.columns.PriorityColumn;
import org.jdownloader.gui.views.downloads.columns.SizeColumn;
import org.jdownloader.gui.views.linkgrabber.columns.DownloadFolderColumn;
import org.jdownloader.gui.views.linkgrabber.columns.UrlColumn;

public class LinkGrabberTableModel extends PackageControllerTableModel<CrawledPackage, CrawledLink> {

    private static final long                  serialVersionUID = -198189279671615981L;
    private static final LinkGrabberTableModel INSTANCE         = new LinkGrabberTableModel();

    public static LinkGrabberTableModel getInstance() {
        return INSTANCE;
    }

    private boolean autoConfirm;

    private LinkGrabberTableModel() {
        super(LinkCollector.getInstance(), "LinkGrabberTableModel");
    }

    public ArrayList<AbstractNode> sort(final ArrayList<AbstractNode> data, ExtColumn<AbstractNode> column) {
        ArrayList<AbstractNode> ret = super.sort(data, column);
        boolean autoConfirm = org.jdownloader.settings.staticreferences.CFG_LINKFILTER.LINKGRABBER_AUTO_CONFIRM_ENABLED.getValue() && ret.size() > 0;
        if (!autoConfirm) {
            for (CrawledLink l : this.getAllChildrenNodes(ret)) {
                if (l.getLinkState() != LinkState.OFFLINE) {
                    if (l.isAutoConfirmEnabled()) {
                        autoConfirm = true;
                        break;
                    }
                }
            }
        }
        // if (autoConfirm) {
        this.autoConfirm = autoConfirm;
        // }
        return ret;

    }

    public boolean isAutoConfirm() {
        return autoConfirm;
    }

    @Override
    protected void initColumns() {
        this.addColumn(new FileColumn());

        this.addColumn(new UrlColumn());
        this.addColumn(new DownloadFolderColumn());
        this.addColumn(new DownloadPasswordColumn());

        this.addColumn(new SizeColumn());
        this.addColumn(new HosterColumn());
        this.addColumn(new AvailabilityColumn() {
            @Override
            public boolean isDefaultVisible() {
                return true;
            }

        });
        // this.addColumn(new AddedDateColumn());
        this.addColumn(new PriorityColumn());
        this.addColumn(new CommentColumn() {
            @Override
            public boolean isDefaultVisible() {
                return false;
            }
        });
    }

}
