package org.jdownloader.gui.views.downloads.columns;

import java.awt.Point;
import java.awt.event.MouseEvent;

import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.SwingUtilities;

import jd.controlling.linkcrawler.CrawledLink;
import jd.controlling.linkcrawler.CrawledPackage;
import jd.controlling.packagecontroller.AbstractNode;
import jd.controlling.packagecontroller.AbstractPackageChildrenNode;
import jd.controlling.packagecontroller.AbstractPackageNode;
import jd.plugins.DownloadLink;
import net.miginfocom.swing.MigLayout;

import org.appwork.app.gui.MigPanel;
import org.appwork.swing.components.tooltips.ExtTooltip;
import org.appwork.swing.components.tooltips.IconLabelToolTip;
import org.appwork.swing.components.tooltips.ToolTipController;
import org.appwork.swing.exttable.ExtColumn;
import org.appwork.swing.exttable.ExtDefaultRowSorter;
import org.appwork.utils.swing.renderer.RenderLabel;
import org.appwork.utils.swing.renderer.RendererMigPanel;
import org.jdownloader.DomainInfo;
import org.jdownloader.gui.translate._GUI;
import org.jdownloader.gui.views.downloads.HosterToolTip;
import org.jdownloader.images.NewTheme;

public class HosterColumn extends ExtColumn<AbstractNode> {

    /**
     * 
     */
    private static final long serialVersionUID   = 1L;
    private int               maxIcons           = 10;
    private MigPanel          panel;
    private RenderLabel[]     labels;
    private ImageIcon         moreIcon;
    private static int        DEFAULT_ICON_COUNT = 4;

    public HosterColumn() {
        super(_GUI._.HosterColumn_HosterColumn(), null);
        panel = new RendererMigPanel("ins 0 0 0 0", "[]", "[grow,fill]");
        labels = new RenderLabel[maxIcons + 1];

        // panel.add(Box.createGlue(), "pushx,growx");
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i <= maxIcons; i++) {
            labels[i] = new RenderLabel();
            // labels[i].setBorder(BorderFactory.createMatteBorder(1, 1, 1, 1,
            // Color.RED));
            labels[i].setOpaque(false);
            labels[i].setBackground(null);
            if (sb.length() > 0) sb.append("1");
            sb.append("[18!]");
            panel.add(labels[i]);

        }
        moreIcon = NewTheme.I().getIcon("more", -1);
        panel.setLayout(new MigLayout("ins 0 0 0 0", sb.toString(), "[]"));
        // panel.add(Box.createGlue(), "pushx,growx");
        setRowSorter(new ExtDefaultRowSorter<AbstractNode>() {

            /*
             * (non-Javadoc)
             * 
             * @see org.appwork.swing.exttable.ExtDefaultRowSorter#compare(java.lang .Object, java.lang.Object)
             */
            @Override
            public int compare(AbstractNode o1, AbstractNode o2) {
                final long l1 = getHosterCounter(o1);
                final long l2 = getHosterCounter(o2);
                if (l1 == l2) { return 0; }
                if (this.getSortOrderIdentifier() == ExtColumn.SORT_ASC) {
                    return l1 > l2 ? -1 : 1;
                } else {
                    return l1 < l2 ? -1 : 1;
                }
            }

        });

        resetRenderer();
    }

    @Override
    public Object getCellEditorValue() {
        return null;
    }

    @Override
    protected boolean isDefaultResizable() {
        return false;
    }

    @Override
    public boolean isEditable(AbstractNode obj) {
        return false;
    }

    @Override
    public boolean isEnabled(AbstractNode obj) {
        if (obj instanceof CrawledPackage) { return ((CrawledPackage) obj).getView().isEnabled(); }
        return obj.isEnabled();
    }

    public boolean isPaintWidthLockIcon() {
        return false;
    }

    @Override
    public boolean isSortable(AbstractNode obj) {
        return true;
    }

    @Override
    public void setValue(Object value, AbstractNode object) {

    }

    @Override
    public int getDefaultWidth() {
        return DEFAULT_ICON_COUNT * 19 + 7;
    }

    // @Override
    // public int getMaxWidth() {
    //
    // return 150;
    // }

    // public JToolTip createToolTip(final AbstractNode obj) {
    // if (obj instanceof DownloadLink) {
    // tip.setExtText(((DownloadLink) obj).getHost());
    // return tip;
    // } else if (obj instanceof FilePackage) {
    // tooltip.setObj(obj);
    // return tooltip;
    // }
    // return null;
    //
    // }

    public void configureRendererComponent(AbstractNode value, boolean isSelected, boolean hasFocus, int row, int column) {
        int width = getTableColumn().getWidth();
        int count = ((width - 6) / 19);
        if (value instanceof AbstractPackageNode) {
            int i = 0;
            DomainInfo[] icons = ((AbstractPackageNode<?, ?>) value).getView().getDomainInfos();
            for (DomainInfo link : icons) {
                if (i == maxIcons || i == count) {
                    labels[i].setIcon(moreIcon);
                    labels[i].setVisible(true);
                    break;
                }
                ImageIcon icon = link.getFavIcon();
                if (icon != null) {
                    labels[i].setVisible(true);
                    labels[i].setIcon(icon);
                    i++;
                }
            }
        } else if (value instanceof AbstractPackageChildrenNode) {
            DomainInfo dl = ((AbstractPackageChildrenNode<?>) value).getDomainInfo();
            if (dl != null && dl.getFavIcon() != null) {
                labels[0].setVisible(true);
                labels[0].setIcon(dl.getFavIcon());
            }
        }
    }

    @Override
    protected boolean onDoubleClick(MouseEvent e, AbstractNode value) {
        DomainInfo[] infos = null;
        if (value instanceof AbstractPackageNode) {
            infos = ((AbstractPackageNode<?, ?>) value).getView().getDomainInfos();
        }
        if (infos != null && infos.length > 0) {
            int width = getTableColumn().getWidth();
            int count = ((width - 6) / 19);
            // this.setResizable(true);
            // // workaround to REALLY bring this column to the desired width
            // getTableColumn().setMinWidth(Math.min(icons.length, maxIcons) *
            // 19 + 7);
            // SwingUtilities.invokeLater(new Runnable() {
            //
            // public void run() {
            // getTableColumn().setMinWidth(getMinWidth());
            // }
            // });
            if (infos.length > maxIcons || infos.length > count) {

                SwingUtilities.invokeLater(new Runnable() {

                    public void run() {
                        ToolTipController.getInstance().show(getModel().getTable().createExtTooltip(null));
                    }
                });
            }
        }
        return false;
    }

    @Override
    public ExtTooltip createToolTip(Point position, AbstractNode obj) {
        if (obj instanceof AbstractPackageChildrenNode) {
            DomainInfo di = ((AbstractPackageChildrenNode<?>) obj).getDomainInfo();
            return new IconLabelToolTip(di.getTld(), di.getFavIcon());
        } else if (obj instanceof AbstractPackageNode) { return new HosterToolTip(((AbstractPackageNode<?, ?>) obj).getView().getDomainInfos()); }
        return null;
    }

    @Override
    public JComponent getEditorComponent(AbstractNode value, boolean isSelected, int row, int column) {
        return null;
    }

    @Override
    public JComponent getRendererComponent(AbstractNode value, boolean isSelected, boolean hasFocus, int row, int column) {
        return panel;
    }

    @Override
    public void resetEditor() {
    }

    @Override
    public void resetRenderer() {
        for (int i = 0; i <= maxIcons; i++) {
            labels[i].setVisible(false);
        }
        this.panel.setOpaque(false);
        this.panel.setBackground(null);
    }

    @Override
    public void configureEditorComponent(AbstractNode value, boolean isSelected, int row, int column) {
    }

    private int getHosterCounter(AbstractNode value) {
        if (value instanceof AbstractPackageNode) {
            return ((AbstractPackageNode<?, ?>) value).getView().getDomainInfos().length;
        } else if (value instanceof CrawledLink) {
            return ((CrawledLink) value).getDomainInfo().getTld().hashCode();
        } else if (value instanceof DownloadLink) { return ((DownloadLink) value).getDomainInfo().getTld().hashCode(); }
        return 1;
    }

}