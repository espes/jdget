//    jDownloader - Downloadmanager
//    Copyright (C) 2009  JD-Team support@jdownloader.org
//
//    This program is free software: you can redistribute it and/or modify
//    it under the terms of the GNU General Public License as published by
//    the Free Software Foundation, either version 3 of the License, or
//    (at your option) any later version.
//
//    This program is distributed in the hope that it will be useful,
//    but WITHOUT ANY WARRANTY; without even the implied warranty of
//    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
//    GNU General Public License for more details.
//
//    You should have received a copy of the GNU General Public License
//    along with this program.  If not, see <http://www.gnu.org/licenses/>.

package jd.gui.swing.dialog;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;

import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;

import jd.controlling.AccountController;
import jd.controlling.accountchecker.AccountChecker;
import jd.controlling.accountchecker.AccountChecker.AccountCheckJob;
import jd.gui.UserIO;
import jd.gui.swing.jdgui.views.settings.panels.accountmanager.BuyAction;
import jd.plugins.Account;
import jd.plugins.PluginForHost;
import net.miginfocom.swing.MigLayout;

import org.appwork.swing.components.searchcombo.SearchComboBox;
import org.appwork.utils.swing.HelpNotifier;
import org.appwork.utils.swing.HelpNotifierCallbackListener;
import org.appwork.utils.swing.dialog.AbstractDialog;
import org.appwork.utils.swing.dialog.Dialog;
import org.appwork.utils.swing.dialog.DialogCanceledException;
import org.appwork.utils.swing.dialog.DialogClosedException;
import org.appwork.utils.swing.dialog.ProgressDialog;
import org.appwork.utils.swing.dialog.ProgressDialog.ProgressGetter;
import org.jdownloader.DomainInfo;
import org.jdownloader.gui.translate._GUI;
import org.jdownloader.images.NewTheme;
import org.jdownloader.plugins.controller.host.HostPluginController;
import org.jdownloader.plugins.controller.host.LazyHostPlugin;

public class AddAccountDialog extends AbstractDialog<Integer> {

    private static final long serialVersionUID = -2099080199110932990L;

    public static void showDialog(final PluginForHost pluginForHost, Account acc) {
        final AddAccountDialog dialog = new AddAccountDialog(pluginForHost, acc);

        try {
            Dialog.getInstance().showDialog(dialog);
            final Account ac = new Account(dialog.getUsername(), dialog.getPassword());
            ac.setHoster(dialog.getHoster().getDisplayName());

            if (!addAccount(ac)) {
                showDialog(dialog.getHoster().getPrototype(), ac);
            }
        } catch (DialogClosedException e) {
            e.printStackTrace();
        } catch (DialogCanceledException e) {

            e.printStackTrace();
        }

    }

    public static boolean addAccount(final Account ac) throws DialogClosedException, DialogCanceledException {
        try {
            checkAccount(ac);

        } catch (DialogClosedException e) {

            throw e;
        } catch (DialogCanceledException e) {
            throw e;

        } catch (Throwable e) {
            Dialog.getInstance().showExceptionDialog(_GUI._.accountdialog_check_failed(), _GUI._.accountdialog_check_failed_msg(), e);
        }

        if (ac.getAccountInfo() != null) {
            if (ac.getAccountInfo().isExpired()) {
                Dialog.getInstance().showConfirmDialog(0, _GUI._.accountdialog_check_expired_title(), _GUI._.accountdialog_check_expired(ac.getAccountInfo().getStatus()), null, _GUI._.accountdialog_check_expired_renew(), null);

                AccountController.getInstance().addAccount(ac.getDomainInfo().findPlugin(), ac);
                return true;

            } else if (!ac.isValid()) {

                Dialog.getInstance().showMessageDialog(_GUI._.accountdialog_check_invalid(ac.getAccountInfo().getStatus()));
                return false;
            } else {
                Dialog.getInstance().showMessageDialog(_GUI._.accountdialog_check_valid(ac.getAccountInfo().getStatus()));
                AccountController.getInstance().addAccount(ac.getDomainInfo().findPlugin(), ac);
                return true;

            }
        } else {
            return false;
        }
    }

    public static ProgressDialog checkAccount(final Account ac) throws Throwable {
        ProgressDialog pd = new ProgressDialog(new ProgressGetter() {

            public void run() throws Exception {
                ac.setHoster(ac.getHoster());
                AccountCheckJob job = AccountChecker.getInstance().check(ac, true);
                job.waitChecked();
            }

            public String getString() {
                return null;
            }

            public int getProgress() {
                return -1;
            }
        }, 0, _GUI._.accountdialog_check(), _GUI._.accountdialog_check_msg(), DomainInfo.getInstance(ac.getHoster()).getFavIcon());
        try {

            Dialog.getInstance().showDialog(pd);
        } catch (DialogCanceledException e) {
            if (pd.getThrowable() == null) {
                throw e;
            } else {
                throw pd.getThrowable();
            }

        }
        return pd;
    }

    private SearchComboBox<LazyHostPlugin> hoster;

    private JTextField                     name;

    JPasswordField                         pass;
    private final PluginForHost            plugin;

    private Account                        defaultAccount;
    private static String                  EMPTYPW = "                 ";

    private AddAccountDialog(final PluginForHost plugin, Account acc) {
        super(UserIO.NO_ICON, _GUI._.jd_gui_swing_components_AccountDialog_title(), null, null, null);
        this.defaultAccount = acc;
        this.plugin = plugin;
    }

    @Override
    protected Integer createReturnValue() {
        return this.getReturnmask();
    }

    public LazyHostPlugin getHoster() {
        return (LazyHostPlugin) this.hoster.getSelectedItem();
    }

    public String getPassword() {
        if (this.pass == null) return null;
        if (EMPTYPW.equals(new String(this.pass.getPassword()))) return null;
        return new String(this.pass.getPassword());
    }

    public String getUsername() {
        if (_GUI._.jd_gui_swing_components_AccountDialog_help_username().equals(this.name.getText())) return null;
        return this.name.getText();
    }

    @Override
    public JComponent layoutDialogContent() {
        final List<LazyHostPlugin> plugins = HostPluginController.getInstance().list();

        // final HostPluginWrapper[] array = plugins.toArray(new
        // HostPluginWrapper[plugins.size()]);

        hoster = new SearchComboBox<LazyHostPlugin>(plugins) {

            @Override
            protected Icon getIconForValue(LazyHostPlugin value) {
                if (value == null) return null;
                return DomainInfo.getInstance(value.getDisplayName()).getFavIcon();
            }

            @Override
            protected String getTextForValue(LazyHostPlugin value) {
                if (value == null) return "";
                return value.getDisplayName();
            }
        };

        if (this.plugin != null) {
            try {
                LazyHostPlugin lazyp = plugin.getLazyP();
                if (lazyp != null) this.hoster.setSelectedItem(lazyp);
            } catch (final Exception e) {
            }
        } else {
            LazyHostPlugin plg = HostPluginController.getInstance().get("netload.in");
            if (plg != null) {
                try {
                    hoster.setSelectedItem(plg);
                } catch (final Exception e) {
                }
            }
        }

        final JButton link = new JButton(NewTheme.I().getIcon("money", 16));
        link.setToolTipText(_GUI._.gui_menu_action_premium_buy_name());
        link.addActionListener(new ActionListener() {
            public void actionPerformed(final ActionEvent e) {
                new BuyAction(getHoster()).actionPerformed(null);
            }
        });
        link.setFocusable(false);
        final JPanel panel = new JPanel(new MigLayout("ins 0, wrap 2", "[][grow,fill]"));
        panel.add(new JLabel(_GUI._.jd_gui_swing_components_AccountDialog_hoster()));
        panel.add(this.hoster, "split 2,height 24!");
        panel.add(link, "height 24!");

        panel.add(new JLabel(_GUI._.jd_gui_swing_components_AccountDialog_name()));
        panel.add(this.name = new JTextField());

        HelpNotifier.register(name, new HelpNotifierCallbackListener() {

            public void onHelpNotifyShown(JComponent c) {
                checkOK();
            }

            public void onHelpNotifyHidden(JComponent c) {
                checkOK();
            }
        }, _GUI._.jd_gui_swing_components_AccountDialog_help_username());

        panel.add(new JLabel(_GUI._.jd_gui_swing_components_AccountDialog_pass()));
        panel.add(this.pass = new JPasswordField(), "");
        HelpNotifier.register(pass, new HelpNotifierCallbackListener() {

            public void onHelpNotifyShown(JComponent c) {
                checkOK();
            }

            public void onHelpNotifyHidden(JComponent c) {
                checkOK();
            }
        }, EMPTYPW);
        if (defaultAccount != null) {
            name.setText(defaultAccount.getUser());
        }
        checkOK();
        return panel;
    }

    private void checkOK() {
        boolean ok = getPassword() != null || getUsername() != null;
        this.okButton.setEnabled(ok);
    }

    @Override
    protected void packed() {
        super.packed();
        hoster.requestFocus();
    }

}