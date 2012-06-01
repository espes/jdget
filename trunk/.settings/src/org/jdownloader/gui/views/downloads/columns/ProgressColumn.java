package org.jdownloader.gui.views.downloads.columns;

import java.awt.Color;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.SwingUtilities;
import javax.swing.Timer;

import jd.controlling.packagecontroller.AbstractNode;
import jd.controlling.proxy.ProxyBlock;
import jd.controlling.proxy.ProxyController;
import jd.gui.swing.laf.LookAndFeelController;
import jd.plugins.DownloadLink;
import jd.plugins.FilePackage;
import jd.plugins.LinkStatus;
import jd.plugins.PluginForHost;
import jd.plugins.PluginProgress;

import org.appwork.swing.components.multiprogressbar.MultiProgressBar;
import org.appwork.swing.components.multiprogressbar.Range;
import org.appwork.swing.components.tooltips.ExtTooltip;
import org.appwork.swing.components.tooltips.PanelToolTip;
import org.appwork.swing.components.tooltips.ToolTipController;
import org.appwork.swing.components.tooltips.TooltipPanel;
import org.appwork.swing.exttable.columns.ExtProgressColumn;
import org.appwork.utils.swing.SwingUtils;
import org.jdownloader.gui.translate._GUI;

public class ProgressColumn extends ExtProgressColumn<AbstractNode> {

    /**
     * 
     */
    private static final long serialVersionUID = 1L;

    private ProxyBlock        block            = null;

    public ProgressColumn() {
        super(_GUI._.ProgressColumn_ProgressColumn());

    }

    @Override
    public boolean isEnabled(AbstractNode obj) {

        return obj.isEnabled();
    }

    protected boolean onDoubleClick(final MouseEvent e, final AbstractNode obj) {
        SwingUtilities.invokeLater(new Runnable() {

            public void run() {
                ToolTipController.getInstance().show(getModel().getTable().createExtTooltip(null));
            }
        });
        return false;
    }

    public ExtTooltip createToolTip(final Point position, final AbstractNode obj) {
        TooltipPanel panel = new TooltipPanel("ins 0,wrap 1", "[grow,fill]", "[][grow,fill]");
        final MultiProgressBar mpb = new MultiProgressBar(1000);
        mpb.setForeground(new Color(LookAndFeelController.getInstance().getLAFOptions().getTooltipForegroundColor()));

        updateRanges(obj, mpb);

        JLabel lbl = new JLabel(_GUI._.ProgressColumn_createToolTip_object_());
        lbl.setForeground(new Color(LookAndFeelController.getInstance().getLAFOptions().getTooltipForegroundColor()));
        SwingUtils.toBold(lbl);
        panel.add(lbl);
        mpb.setBorder(BorderFactory.createMatteBorder(1, 1, 1, 1, new Color(LookAndFeelController.getInstance().getLAFOptions().getTooltipForegroundColor())));
        panel.add(mpb, "width 300!,height 24!");

        return new PanelToolTip(panel) {
            /**
             * 
             */
            private static final long serialVersionUID = 1036923322222455495L;
            private Timer             timer;

            /**
             * 
             */
            public void onShow() {
                this.timer = new Timer(1000, new ActionListener() {

                    public void actionPerformed(ActionEvent e) {
                        updateRanges(obj, mpb);
                        repaint();
                    }

                });
                timer.start();
            }

            /**
             * 
             */
            public void onHide() {
                timer.stop();
            }
        };
    }

    public void updateRanges(final AbstractNode obj, final MultiProgressBar mpb) {
        if (obj instanceof DownloadLink) {
            mpb.getModel().setMaximum(((DownloadLink) obj).getDownloadMax());
            ArrayList<Range> ranges = new ArrayList<Range>();

            long[] chunks = ((DownloadLink) obj).getChunksProgress();
            if (chunks != null) {
                long part = ((DownloadLink) obj).getDownloadMax() / chunks.length;
                for (int i = 0; i < chunks.length; i++) {
                    ranges.add(new Range(i * part, chunks[i]));
                }
                mpb.getModel().setRanges(ranges.toArray(new Range[] {}));
            }
        } else if (obj instanceof FilePackage) {
            synchronized (obj) {
                long size = ((FilePackage) obj).getView().getSize();
                mpb.getModel().setMaximum(size);
                ArrayList<Range> ranges = new ArrayList<Range>();

                List<DownloadLink> children = ((FilePackage) obj).getChildren();
                int count = children.size();

                long all = 0;
                for (int i = 0; i < count; i++) {
                    ranges.add(new Range(all, all + children.get(i).getDownloadCurrent()));
                    all += children.get(i).getDownloadSize();
                }
                mpb.getModel().setRanges(ranges.toArray(new Range[] {}));
            }
        }
    }

    @Override
    public int getMinWidth() {

        return 30;
    }

    public boolean isPaintWidthLockIcon() {
        return false;
    }

    @Override
    public int getDefaultWidth() {
        return 100;
    }

    @Override
    protected String getString(AbstractNode value) {
        if (value instanceof FilePackage) {
            return null;
        } else {
            DownloadLink dLink = (DownloadLink) value;
            PluginProgress progress;
            if (dLink.getDefaultPlugin() == null) {
                return _GUI._.gui_treetable_error_plugin();
            } else if ((progress = dLink.getPluginProgress()) != null && !(progress.getProgressSource() instanceof PluginForHost)) { return (progress.getPercent() + " %"); }
        }
        return null;
    }

    @Override
    protected long getMax(AbstractNode value) {
        if (value instanceof FilePackage) {
            FilePackage fp = (FilePackage) value;
            if (fp.getView().isFinished()) {
                return 100;
            } else {
                return (Math.max(1, fp.getView().getSize()));
            }
        } else {
            DownloadLink dLink = (DownloadLink) value;
            PluginProgress progress = null;
            if (dLink.getDefaultPlugin() == null) {
                return 100;
            } else if ((progress = dLink.getPluginProgress()) != null && !(progress.getProgressSource() instanceof PluginForHost)) {
                return (progress.getTotal());
            } else if (dLink.getLinkStatus().isFinished()) {
                return 100;
            } else if (block != null && !dLink.getLinkStatus().isPluginActive() && !dLink.getLinkStatus().hasStatus(LinkStatus.TEMP_IGNORE) && dLink.isEnabled()) {
                return block.getBlockedUntil();
            } else if (dLink.getDownloadCurrent() > 0 || dLink.getDownloadSize() > 0) { return (dLink.getDownloadSize());

            }
        }
        return 100;
    }

    @Override
    protected void prepareGetter(AbstractNode value) {
        if (value instanceof DownloadLink) {
            DownloadLink dLink = (DownloadLink) value;
            block = ProxyController.getInstance().getHostIPBlockTimeout(dLink.getHost());
            if (block == null) block = ProxyController.getInstance().getHostBlockedTimeout(dLink.getHost());
        } else {
            block = null;
        }
    }

    @Override
    protected long getValue(AbstractNode value) {
        if (value instanceof FilePackage) {
            FilePackage fp = (FilePackage) value;
            if (fp.getView().isFinished()) {
                return 100;
            } else {
                return (fp.getView().getDone());
            }

        } else {
            DownloadLink dLink = (DownloadLink) value;
            PluginProgress progress = null;
            if (dLink.getDefaultPlugin() == null) {
                return -1;
            } else if ((progress = dLink.getPluginProgress()) != null && !(progress.getProgressSource() instanceof PluginForHost)) {
                return (progress.getCurrent());
            } else if (dLink.getLinkStatus().isFinished()) {
                return 100;
            } else if (block != null && !dLink.getLinkStatus().isPluginActive() && !dLink.getLinkStatus().hasStatus(LinkStatus.TEMP_IGNORE) && dLink.isEnabled()) {
                return block.getBlockedTimeout();
            } else if (dLink.getDownloadCurrent() > 0 || dLink.getDownloadSize() > 0) { return (dLink.getDownloadCurrent()); }
        }
        return -1;
    }

}
