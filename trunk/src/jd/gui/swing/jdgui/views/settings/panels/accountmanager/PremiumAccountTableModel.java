package jd.gui.swing.jdgui.views.settings.panels.accountmanager;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JTable;
import javax.swing.table.JTableHeader;

import jd.SecondLevelLaunch;
import jd.controlling.AccountController;
import jd.controlling.AccountControllerEvent;
import jd.controlling.AccountControllerListener;
import jd.controlling.IOEQ;
import jd.controlling.accountchecker.AccountChecker;
import jd.controlling.accountchecker.AccountCheckerEventListener;
import jd.nutils.Formatter;
import jd.plugins.Account;
import jd.plugins.AccountInfo;
import jd.plugins.PluginForHost;

import org.appwork.scheduler.DelayedRunnable;
import org.appwork.swing.MigPanel;
import org.appwork.swing.exttable.ExtTableHeaderRenderer;
import org.appwork.swing.exttable.ExtTableModel;
import org.appwork.swing.exttable.columns.ExtCheckColumn;
import org.appwork.swing.exttable.columns.ExtComponentColumn;
import org.appwork.swing.exttable.columns.ExtDateColumn;
import org.appwork.swing.exttable.columns.ExtPasswordEditorColumn;
import org.appwork.swing.exttable.columns.ExtProgressColumn;
import org.appwork.swing.exttable.columns.ExtTextColumn;
import org.appwork.utils.formatter.TimeFormatter;
import org.appwork.utils.swing.EDTRunner;
import org.appwork.utils.swing.renderer.RendererMigPanel;
import org.jdownloader.gui.translate._GUI;
import org.jdownloader.images.NewTheme;

import sun.swing.SwingUtilities2;

public class PremiumAccountTableModel extends ExtTableModel<AccountEntry> implements AccountCheckerEventListener {

    private static final long                serialVersionUID       = 3120481189794897020L;

    private AccountManagerSettings           accountManagerSettings = null;

    private DelayedRunnable                  delayedFill;

    private volatile boolean                 checkRunning           = false;

    private DelayedRunnable                  delayedUpdate;

    private ExtComponentColumn<AccountEntry> details;

    public PremiumAccountTableModel(final AccountManagerSettings accountManagerSettings) {
        super("PremiumAccountTableModel2");
        this.accountManagerSettings = accountManagerSettings;
        delayedFill = new DelayedRunnable(IOEQ.TIMINGQUEUE, 250l) {

            @Override
            public void delayedrun() {
                _refill();
            }

        };
        delayedUpdate = new DelayedRunnable(IOEQ.TIMINGQUEUE, 250l) {

            @Override
            public void delayedrun() {
                _update();
            }

        };
        SecondLevelLaunch.ACCOUNTLIST_LOADED.executeWhenReached(new Runnable() {

            @Override
            public void run() {
                AccountController.getInstance().getBroadcaster().addListener(new AccountControllerListener() {

                    public void onAccountControllerEvent(AccountControllerEvent event) {
                        if (accountManagerSettings.isShown()) {
                            switch (event.getType()) {
                            case UPDATE:
                                /* just repaint */
                                delayedUpdate.run();
                                break;
                            default:
                                /* structure changed */
                                delayedFill.run();
                            }
                        }
                    }
                });
                AccountChecker.getInstance().getEventSender().addListener(PremiumAccountTableModel.this);
                if (AccountChecker.getInstance().isRunning()) {
                    onCheckStarted();
                }
                _refill();
            }
        });
    }

    public void fill() {
        delayedFill.run();
    }

    @Override
    protected void initColumns() {

        this.addColumn(new ExtCheckColumn<AccountEntry>(_GUI._.premiumaccounttablemodel_column_enabled()) {

            private static final long serialVersionUID = 1515656228974789237L;

            public ExtTableHeaderRenderer getHeaderRenderer(final JTableHeader jTableHeader) {

                final ExtTableHeaderRenderer ret = new ExtTableHeaderRenderer(this, jTableHeader) {

                    private static final long serialVersionUID = 3224931991570756349L;

                    @Override
                    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                        super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                        setIcon(NewTheme.I().getIcon("ok", 14));
                        setHorizontalAlignment(CENTER);
                        setText(null);
                        return this;
                    }

                };

                return ret;
            }

            @Override
            public int getMaxWidth() {
                return 30;
            }

            @Override
            public boolean isHidable() {
                return false;
            }

            @Override
            protected boolean getBooleanValue(AccountEntry value) {
                return value.getAccount().isEnabled();
            }

            @Override
            public boolean isEditable(AccountEntry obj) {
                return true;
            }

            @Override
            protected void setBooleanValue(boolean value, final AccountEntry object) {
                object.getAccount().setEnabled(value);
                AccountController.getInstance().saveDelayedRequest();
            }
        });
        this.addColumn(new ExtTextColumn<AccountEntry>(_GUI._.premiumaccounttablemodel_column_hoster()) {

            private static final long serialVersionUID = -3693931358975303164L;

            @Override
            public boolean isEnabled(AccountEntry obj) {
                return obj.getAccount().isEnabled();
            }

            @Override
            public boolean isHidable() {
                return false;
            }

            @Override
            protected Icon getIcon(AccountEntry value) {
                return value.getAccount().getDomainInfo().getFavIcon();
            }

            @Override
            public int getDefaultWidth() {
                return 120;
            }

            @Override
            public int getMinWidth() {
                return 30;
            }

            @Override
            protected String getTooltipText(AccountEntry obj) {
                return obj.getAccount().getHoster();
            }

            @Override
            public void configureRendererComponent(AccountEntry value, boolean isSelected, boolean hasFocus, int row, int column) {
                this.rendererIcon.setIcon(this.getIcon(value));
                String str = null;
                if (getWidth() > 60) str = this.getStringValue(value);
                if (str == null) {
                    str = "";
                }
                if (this.getTableColumn() != null) {
                    this.rendererField.setText(SwingUtilities2.clipStringIfNecessary(this.rendererField, this.rendererField.getFontMetrics(this.rendererField.getFont()), str, this.getTableColumn().getWidth() - this.rendererIcon.getPreferredSize().width - 5));
                } else {
                    this.rendererField.setText(str);
                }
            }

            @Override
            public String getStringValue(AccountEntry value) {
                return value.getAccount().getHoster();
            }

        });
        this.addColumn(new ExtTextColumn<AccountEntry>(_GUI._.premiumaccounttablemodel_column_user()) {

            private static final long serialVersionUID = -8070328156326837828L;

            @Override
            public boolean isHidable() {
                return false;
            }

            @Override
            public int getMaxWidth() {
                return 140;
            }

            @Override
            public int getDefaultWidth() {
                return getMinWidth();
            }

            @Override
            public int getMinWidth() {
                return 100;
            }

            @Override
            public boolean isEditable(AccountEntry obj) {
                return true;
            }

            @Override
            protected void setStringValue(String value, AccountEntry object) {
                object.getAccount().setUser(value);
                AccountController.getInstance().saveDelayedRequest();
            }

            @Override
            public String getStringValue(AccountEntry value) {
                return value.getAccount().getUser();
            }
        });
        this.addColumn(new ExtPasswordEditorColumn<AccountEntry>(_GUI._.premiumaccounttablemodel_column_password()) {
            private static final long serialVersionUID = 3180414754658474808L;

            @Override
            public boolean isHidable() {
                return false;
            }

            @Override
            public int getMaxWidth() {
                return 140;
            }

            @Override
            public int getDefaultWidth() {
                return 110;
            }

            @Override
            public int getMinWidth() {
                return 100;
            }

            @Override
            protected String getPlainStringValue(AccountEntry value) {
                return value.getAccount().getPass();
            }

            @Override
            protected void setStringValue(String value, AccountEntry object) {
                object.getAccount().setPass(value);
                AccountController.getInstance().saveDelayedRequest();
            }
        });

        this.addColumn(new ExtDateColumn<AccountEntry>(_GUI._.premiumaccounttablemodel_column_expiredate()) {
            private static final long serialVersionUID = 5067606909520874358L;

            @Override
            public boolean isEnabled(AccountEntry obj) {
                return obj.getAccount().isEnabled();
            }

            @Override
            public int getMaxWidth() {
                return 100;
            }

            @Override
            public int getDefaultWidth() {
                return getMinWidth();
            }

            @Override
            public int getMinWidth() {
                return 100;
            }

            @Override
            protected String getDateFormatString() {
                return _GUI._.PremiumAccountTableModel_getDateFormatString_();
            }

            @Override
            protected Date getDate(AccountEntry o2, Date date) {
                AccountInfo ai = o2.getAccount().getAccountInfo();
                if (ai == null) {
                    return null;
                } else {
                    if (ai.getValidUntil() <= 0) return null;
                    return new Date(ai.getValidUntil());
                }
            }
        });

        this.addColumn(new ExtProgressColumn<AccountEntry>(_GUI._.premiumaccounttablemodel_column_trafficleft()) {
            private static final long serialVersionUID = -8376056840172682617L;

            @Override
            public boolean isEnabled(AccountEntry obj) {
                return obj.getAccount().isEnabled();
            }

            @Override
            public int getMinWidth() {
                return 120;
            }

            protected boolean isIndeterminated(final AccountEntry value, final boolean isSelected, final boolean hasFocus, final int row, final int column) {
                if (checkRunning) { return AccountChecker.getInstance().contains(value.getAccount()); }
                if (value.getAccount().isValid() && value.getAccount().isEnabled() && value.getAccount().isTempDisabled()) return true;
                return false;

            }

            @Override
            protected String getString(AccountEntry ac, long current, long total) {
                AccountInfo ai = ac.getAccount().getAccountInfo();
                long timeout = -1;
                if (!ac.getAccount().isValid()) {
                    return "";
                } else if (ac.getAccount().isEnabled() && ac.getAccount().isTempDisabled() && ((timeout = ac.getAccount().getTmpDisabledTimeout() - System.currentTimeMillis()) > 0)) {
                    return _GUI._.premiumaccounttablemodel_column_trafficleft_tempdisabled(TimeFormatter.formatMilliSeconds(timeout, 0));
                } else if (ai == null) {
                    return "";
                } else {
                    // COL_PROGRESS = COL_PROGRESS_NORMAL;
                    if (ai.isUnlimitedTraffic()) {
                        return _GUI._.premiumaccounttablemodel_column_trafficleft_unlimited();
                    } else {
                        return Formatter.formatReadable(ai.getTrafficLeft()) + "/" + Formatter.formatReadable(ai.getTrafficMax());

                    }
                }
            }

            @Override
            protected long getMax(AccountEntry ac) {
                AccountInfo ai = ac.getAccount().getAccountInfo();
                if (!ac.getAccount().isValid()) {
                    return 100;
                } else if (ai == null) {
                    return 100;
                } else {
                    if (ai.isUnlimitedTraffic()) {
                        return 100;
                    } else {
                        return ai.getTrafficMax();
                    }
                }
            }

            @Override
            protected long getValue(AccountEntry ac) {
                AccountInfo ai = ac.getAccount().getAccountInfo();
                if (!ac.getAccount().isValid()) {
                    return 0;
                } else if (ai == null) {
                    return 0;
                } else {
                    if (ai.isUnlimitedTraffic()) {
                        return 100;
                    } else {
                        return ai.getTrafficLeft();
                    }
                }
            }
        });

        this.addColumn(details = new ExtComponentColumn<AccountEntry>(_GUI._.premiumaccounttablemodel_column_info()) {
            private JButton      button;
            private MigPanel     panel;
            private JButton      rbutton;
            private MigPanel     rpanel;
            private AccountEntry editing;

            {
                button = new JButton(_GUI._.premiumaccounttablemodel_column_info_button());

                panel = new RendererMigPanel("ins 2", "[]", "[18!]");
                panel.add(button);
                button.setOpaque(false);

                rbutton = new JButton(_GUI._.premiumaccounttablemodel_column_info_button());

                rbutton.addActionListener(new ActionListener() {

                    @Override
                    public void actionPerformed(ActionEvent e) {
                        if (editing != null) {
                            editing.showAccountInfoDialog();
                        }
                    }
                });

                rpanel = new MigPanel("ins 2", "[]", "[18!]");
                rpanel.add(rbutton);
                rbutton.setOpaque(false);
            }

            @Override
            public int getMaxWidth() {
                return panel.getPreferredSize().width;
            }

            @Override
            public boolean isEnabled(AccountEntry obj) {
                return obj.getAccount().isEnabled() && obj.isDetailsDialogSupported();
            }

            @Override
            public int getDefaultWidth() {
                return panel.getPreferredSize().width;
            }

            @Override
            public int getMinWidth() {
                return panel.getPreferredSize().width;
            }

            @Override
            public boolean isEditable(AccountEntry obj) {
                return super.isEditable(obj);
            }

            @Override
            public boolean onSingleClick(MouseEvent e, AccountEntry obj) {
                return super.onSingleClick(e, obj);
            }

            @Override
            protected JComponent getInternalEditorComponent(AccountEntry value, boolean isSelected, int row, int column) {
                return rpanel;
            }

            @Override
            protected JComponent getInternalRendererComponent(AccountEntry value, boolean isSelected, boolean hasFocus, int row, int column) {

                return panel;
            }

            @Override
            public void configureEditorComponent(AccountEntry value, boolean isSelected, int row, int column) {
                editing = value;
                // rbutton.setEnabled(isEnabled(value));
            }

            @Override
            public void configureRendererComponent(AccountEntry value, boolean isSelected, boolean hasFocus, int row, int column) {
                // button.setEnabled(isEnabled(value));
                ;
            }

            @Override
            public void resetEditor() {
                rpanel.setBackground(null);
                rpanel.setOpaque(false);
            }

            @Override
            public void resetRenderer() {
                panel.setBackground(null);
                panel.setOpaque(false);
            }

        });

    }

    public void onCheckStarted() {
        checkRunning = true;
    }

    public void onCheckStopped() {
        checkRunning = false;
        _update();
    }

    protected void _update() {
        if (accountManagerSettings.isShown()) {
            new EDTRunner() {
                @Override
                protected void runInEDT() {
                    PremiumAccountTableModel.this.getTable().repaint();
                }
            };
        }
    }

    protected void _refill() {
        if (accountManagerSettings.isShown()) {
            final java.util.List<AccountEntry> newtableData = new ArrayList<AccountEntry>(this.getRowCount());
            boolean hasDetailsButton = false;
            List<Account> accs = AccountController.getInstance().list(null);
            if (accs != null) {
                for (Account acc : accs) {
                    PluginForHost plugin = acc.getPlugin();
                    if (plugin == null) continue;
                    AccountEntry ae;
                    newtableData.add(ae = new AccountEntry(acc));
                    if (ae.isDetailsDialogSupported()) {
                        hasDetailsButton = true;
                    }
                }
            }
            setColumnVisible(details, hasDetailsButton);
            _fireTableStructureChanged(newtableData, true);
        }
    }
}
