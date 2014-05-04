package org.jdownloader.gui.views.linkgrabber.properties;

import java.awt.Dimension;

import javax.swing.JPopupMenu;

import jd.controlling.linkcrawler.CrawledLink;
import jd.controlling.linkcrawler.CrawledPackage;
import jd.controlling.packagecontroller.AbstractNode;

import org.appwork.swing.MigPanel;
import org.jdownloader.gui.views.linkgrabber.LinkGrabberTable;
import org.jdownloader.settings.staticreferences.CFG_GUI;
import org.jdownloader.updatev2.gui.LAFOptions;

public class LinkgrabberProperties extends MigPanel {

    /**
     * 
     */
    private static final long   serialVersionUID = -195024600818162517L;
    private LinkGrabberTable    table;
    private LinkPropertiesPanel pkgPanel;
    private LinkPropertiesPanel linkPanel;

    public LinkgrabberProperties(LinkGrabberTable table) {
        super("ins 0", "[grow,fill]", "[grow,fill]");
        this.table = table;
        LAFOptions.getInstance().applyPanelBackground(this);
        pkgPanel = new PackagePropertiesPanel();
        linkPanel = new LinkPropertiesPanel();
        add(pkgPanel, "hidemode 3");
        add(linkPanel, "hidemode 3");

    }

    @Override
    public Dimension getPreferredSize() {
        Dimension ret = super.getPreferredSize();
        if (CFG_GUI.CFG.isPropertiesPanelHeightNormalized()) {
            ret.height = Math.max(pkgPanel.getPreferredSize().height, linkPanel.getPreferredSize().height);
        }
        return ret;
    }

    public void update(AbstractNode objectbyRow) {
        if (objectbyRow instanceof CrawledPackage) {
            CrawledPackage pkg = (CrawledPackage) objectbyRow;
            linkPanel.setVisible(false);
            pkgPanel.setVisible(true);
            pkgPanel.setSelectedItem(pkg);
        } else if (objectbyRow instanceof CrawledLink) {
            CrawledLink link = (CrawledLink) objectbyRow;
            linkPanel.setVisible(true);
            pkgPanel.setVisible(false);
            linkPanel.setSelectedItem(link);
        }
    }

    public void fillPopup(JPopupMenu pu) {

        if (linkPanel.isVisible()) {
            linkPanel.fillPopup(pu);
        } else {
            pkgPanel.fillPopup(pu);
        }

    }

    public void refreshAfterTabSwitch() {
        linkPanel.refresh();
        pkgPanel.refresh();
    }

    public void save() {
        if (linkPanel.isVisible()) {
            linkPanel.save();
        } else {
            pkgPanel.save();
        }
    }

}
