package jd.gui.swing.dialog;

import java.awt.event.ActionEvent;

import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.event.CaretEvent;
import javax.swing.event.CaretListener;

import jd.controlling.JDLogger;
import net.miginfocom.swing.MigLayout;

import org.appwork.utils.BinaryLogic;
import org.appwork.utils.net.httpconnection.HTTPProxy;
import org.appwork.utils.swing.dialog.AbstractDialog;
import org.appwork.utils.swing.dialog.Dialog;
import org.jdownloader.gui.translate._GUI;
import org.jdownloader.images.NewTheme;

public class ProxyDialog extends AbstractDialog<HTTPProxy> implements CaretListener {

    private JComboBox      cmbType;
    private JTextField     txtHost;
    private JTextField     txtPort;
    private JTextField     txtUser;
    private JTextField     txtPass;

    private final String[] types = new String[] { _GUI._.jd_gui_swing_dialog_ProxyDialog_http(), _GUI._.jd_gui_swing_dialog_ProxyDialog_socks5(), _GUI._.jd_gui_swing_dialog_ProxyDialog_socks4() };
    private JLabel         lblUser;
    private JLabel         lblPass;
    private JLabel         lblPort;
    private JLabel         lblHost;

    public ProxyDialog() {
        super(0, _GUI._.jd_gui_swing_dialog_ProxyDialog_title(), NewTheme.I().getIcon("proxy_rotate", 32), null, null);
    }

    @Override
    public JComponent layoutDialogContent() {
        JPanel panel = new JPanel(new MigLayout("ins 0, wrap 4", "[][grow 10,fill][][grow 3,fill]"));

        panel.add(new JLabel(_GUI._.jd_gui_swing_dialog_ProxyDialog_type()));
        panel.add(cmbType = new JComboBox(types), "spanx");
        cmbType.addActionListener(this);
        panel.add(lblHost = new JLabel(_GUI._.jd_gui_swing_dialog_ProxyDialog_hostport()));
        panel.add(txtHost = new JTextField());
        txtHost.addCaretListener(this);
        panel.add(lblPort = new JLabel(":"));
        panel.add(txtPort = new JTextField(), "shrinkx");
        txtPort.addCaretListener(this);

        panel.add(lblUser = new JLabel(_GUI._.jd_gui_swing_dialog_ProxyDialog_username()));
        panel.add(txtUser = new JTextField(), "spanx");

        panel.add(lblPass = new JLabel(_GUI._.jd_gui_swing_dialog_ProxyDialog_password()));
        panel.add(txtPass = new JTextField(), "spanx");
        this.okButton.setEnabled(false);
        return panel;
    }

    @Override
    public void actionPerformed(final ActionEvent e) {
        if (e.getSource() == cmbType) {
            if (cmbType.getSelectedIndex() == 2) {
                txtPass.setVisible(false);
                lblPass.setVisible(false);
            } else {
                txtPass.setVisible(true);
                lblPass.setVisible(true);
            }
        } else {
            super.actionPerformed(e);
        }
    }

    /**
     * returns HTTPProxy for given settings
     */
    @Override
    protected HTTPProxy createReturnValue() {
        final int mask = getReturnmask();
        if (BinaryLogic.containsSome(mask, Dialog.RETURN_CLOSED)) return null;
        if (BinaryLogic.containsSome(mask, Dialog.RETURN_CANCEL)) return null;
        try {

            HTTPProxy.TYPE type = null;
            if (cmbType.getSelectedIndex() == 0) {
                type = HTTPProxy.TYPE.HTTP;
            } else if (cmbType.getSelectedIndex() == 1) {
                type = HTTPProxy.TYPE.SOCKS5;
            } else if (cmbType.getSelectedIndex() == 2) {
                type = HTTPProxy.TYPE.SOCKS4;
            } else {
                return null;
            }
            HTTPProxy ret = new HTTPProxy(type, txtHost.getText(), Integer.parseInt(txtPort.getText().trim()));

            ret.setPass(txtPass.getText());
            ret.setUser(txtUser.getText());

            return ret;
        } catch (final Throwable e) {
            JDLogger.exception(e);
            return null;
        }
    }

    /**
     * update okayButton enabled status, check if host/port(valid number) or
     * host is given
     */
    public void caretUpdate(CaretEvent e) {
        boolean enable = false;
        try {
            if (cmbType.getSelectedIndex() != 2) {
                if (txtHost.getDocument().getLength() > 0 && txtPort.getDocument().getLength() > 0) {
                    try {
                        int port = Integer.parseInt(txtPort.getText());
                        if (port > 0 && port < 65535) {
                            enable = true;
                        }
                    } catch (final Throwable ee) {
                    }
                }
            } else {
                if (txtHost.getDocument().getLength() > 0) {
                    enable = true;
                }
            }
        } finally {
            this.okButton.setEnabled(enable);
        }
    }

}