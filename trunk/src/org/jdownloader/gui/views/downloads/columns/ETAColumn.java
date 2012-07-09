package org.jdownloader.gui.views.downloads.columns;

import java.awt.event.ActionEvent;
import java.awt.event.MouseEvent;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.JSeparator;
import javax.swing.SwingConstants;

import jd.controlling.packagecontroller.AbstractNode;
import jd.controlling.proxy.ProxyBlock;
import jd.controlling.proxy.ProxyController;
import jd.nutils.Formatter;
import jd.plugins.DownloadLink;
import jd.plugins.FilePackage;
import jd.plugins.LinkStatus;
import jd.plugins.PluginForHost;
import jd.plugins.PluginProgress;

import org.appwork.storage.config.JsonConfig;
import org.appwork.swing.exttable.columnmenu.LockColumnWidthAction;
import org.appwork.swing.exttable.columns.ExtTextColumn;
import org.appwork.utils.swing.dialog.Dialog;
import org.appwork.utils.swing.dialog.DialogCanceledException;
import org.appwork.utils.swing.dialog.DialogClosedException;
import org.jdownloader.DomainInfo;
import org.jdownloader.actions.AppAction;
import org.jdownloader.gui.translate._GUI;
import org.jdownloader.images.NewTheme;
import org.jdownloader.premium.PremiumInfoDialog;
import org.jdownloader.settings.GraphicalUserInterfaceSettings;
import org.jdownloader.translate._JDT;

public class ETAColumn extends ExtTextColumn<AbstractNode> {

    /**
     * 
     */
    private static final long serialVersionUID = 1L;

    private ImageIcon         download;
    private ImageIcon         wait;
    private ImageIcon         icon2Use;

    private ImageIcon         ipwait;

    @Override
    public int getDefaultWidth() {
        return 80;
    }

    public boolean isPaintWidthLockIcon() {
        return false;
    }

    @Override
    public boolean isEnabled(AbstractNode obj) {
        return obj.isEnabled();
    }

    // @Override
    // public int getMaxWidth() {
    //
    // return 85;line
    // }
    @Override
    protected boolean isDefaultResizable() {
        return false;
    }

    public ETAColumn() {
        super(_GUI._.ETAColumn_ETAColumn());
        rendererField.setHorizontalAlignment(SwingConstants.RIGHT);
        this.download = NewTheme.I().getIcon("download", 16);
        this.wait = NewTheme.I().getIcon("wait", 16);
        this.ipwait = NewTheme.I().getIcon("auto-reconnect", 16);
    }

    @Override
    protected Icon getIcon(AbstractNode value) {
        icon2Use = null;
        if (value instanceof DownloadLink) {
            DownloadLink dlLink = ((DownloadLink) value);
            if (dlLink.isEnabled()) {
                if (dlLink.getLinkStatus().hasStatus(LinkStatus.DOWNLOADINTERFACE_IN_PROGRESS)) {
                    icon2Use = download;
                } else {
                    getWaitingTimeout(dlLink);
                }
            }
        }
        return icon2Use;
    }

    public JPopupMenu createHeaderPopup() {

        final JPopupMenu ret = new JPopupMenu();
        LockColumnWidthAction action;
        ret.add(new JCheckBoxMenuItem(action = new LockColumnWidthAction(this)));

        ret.add(new JCheckBoxMenuItem(new AppAction() {
            {
                setName(_GUI._.literall_premium_alert());
                setSmallIcon(wait);
                setSelected(JsonConfig.create(GraphicalUserInterfaceSettings.class).isPremiumAlertETAColumnEnabled());
            }

            @Override
            public void actionPerformed(ActionEvent e) {
                JsonConfig.create(GraphicalUserInterfaceSettings.class).setPremiumAlertETAColumnEnabled(!JsonConfig.create(GraphicalUserInterfaceSettings.class).isPremiumAlertETAColumnEnabled());
            }
        }));
        ret.add(new JSeparator());
        return ret;

    }

    protected boolean onSingleClick(final MouseEvent e, final AbstractNode value) {
        if (value instanceof DownloadLink) {
            if (JsonConfig.create(GraphicalUserInterfaceSettings.class).isPremiumAlertETAColumnEnabled()) {
                DownloadLink dlLink = (DownloadLink) value;
                if (!dlLink.getLinkStatus().hasStatus(LinkStatus.TEMP_IGNORE) && !dlLink.getLinkStatus().isFinished()) {
                    PluginForHost plugin = dlLink.getDefaultPlugin();
                    if (plugin == null || !plugin.isPremiumEnabled()) {
                        /* no account support yet for this plugin */
                        return false;
                    }
                    ProxyBlock timeout = null;
                    if ((timeout = ProxyController.getInstance().getHostIPBlockTimeout(dlLink.getHost())) != null && timeout.getLink() == dlLink) {
                        try {
                            Dialog.getInstance().showDialog(new PremiumInfoDialog(DomainInfo.getInstance(((DownloadLink) value).getHost()), _GUI._.TaskColumn_onSingleClick_object_(((DownloadLink) value).getHost()), "TaskColumnReconnect") {
                                protected String getDescription(DomainInfo info2) {
                                    return _GUI._.TaskColumn_getDescription_object_(info2.getTld());
                                }
                            });
                            return true;
                        } catch (DialogClosedException e1) {
                            e1.printStackTrace();
                        } catch (DialogCanceledException e1) {
                            e1.printStackTrace();
                        }
                    }
                }
            }
        }
        return false;
    }

    private long getWaitingTimeout(DownloadLink dlLink) {
        if (dlLink.isEnabled()) {
            long time;
            if (dlLink.getLinkStatus().hasStatus(LinkStatus.ERROR_TEMPORARILY_UNAVAILABLE) && (time = dlLink.getLinkStatus().getRemainingWaittime()) > 0) {
                icon2Use = wait;
                return time;
            }
            if (!dlLink.getLinkStatus().hasStatus(LinkStatus.TEMP_IGNORE) && !dlLink.getLinkStatus().isFinished()) {
                ProxyBlock timeout = null;
                if ((timeout = ProxyController.getInstance().getHostIPBlockTimeout(dlLink.getHost())) != null && timeout.getLink() == dlLink) {
                    icon2Use = ipwait;
                    return timeout.getBlockedTimeout();
                }
                if ((timeout = ProxyController.getInstance().getHostBlockedTimeout(dlLink.getHost())) != null && timeout.getLink() == dlLink) {
                    icon2Use = wait;
                    return timeout.getBlockedTimeout();
                }
            }
            PluginProgress progress = null;
            if ((progress = dlLink.getPluginProgress()) != null) {
                icon2Use = progress.getIcon();
                long eta = progress.getETA();
                if (eta >= 0) return eta;
                return Math.max(0, progress.getCurrent());
            }
        }
        return -1;
    }

    @Override
    public String getStringValue(AbstractNode value) {
        if (value instanceof DownloadLink) {
            DownloadLink dlLink = ((DownloadLink) value);
            if (dlLink.isEnabled()) {
                if (dlLink.getLinkStatus().hasStatus(LinkStatus.DOWNLOADINTERFACE_IN_PROGRESS)) {
                    long speed = dlLink.getDownloadSpeed();
                    if (speed > 0) {
                        if (dlLink.getDownloadSize() < 0) {
                            return _JDT._.gui_download_filesize_unknown() + " \u221E";
                        } else {
                            long remainingBytes = (dlLink.getDownloadSize() - dlLink.getDownloadCurrent());
                            long eta = remainingBytes / speed;
                            return Formatter.formatSeconds(eta);
                        }
                    } else {
                        return _JDT._.gui_download_create_connection();
                    }
                } else {
                    long ret = getWaitingTimeout(dlLink);
                    if (ret > 0) return Formatter.formatSeconds(ret / 1000);
                }
            }
        } else if (value instanceof FilePackage) {
            long eta = ((FilePackage) value).getView().getETA();
            if (eta > 0) {
                return Formatter.formatSeconds(eta);
            } else if (eta == Integer.MIN_VALUE) {
                /*
                 * no size known, no eta,show infinite symbol
                 */
                return "\u221E";
            }
        }
        return null;
    }
}
