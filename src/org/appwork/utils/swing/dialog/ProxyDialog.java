package org.appwork.utils.swing.dialog;

import java.awt.event.ActionEvent;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.event.CaretEvent;
import javax.swing.event.CaretListener;

import net.miginfocom.swing.MigLayout;

import org.appwork.scheduler.DelayedRunnable;
import org.appwork.swing.MigPanel;
import org.appwork.swing.components.ExtCheckBox;
import org.appwork.swing.components.ExtPasswordField;
import org.appwork.swing.components.ExtTextArea;
import org.appwork.swing.components.ExtTextField;
import org.appwork.utils.BinaryLogic;
import org.appwork.utils.StringUtils;
import org.appwork.utils.locale._AWU;
import org.appwork.utils.logging.Log;
import org.appwork.utils.net.httpconnection.HTTPProxy;
import org.appwork.utils.net.httpconnection.HTTPProxy.TYPE;
import org.appwork.utils.swing.EDTRunner;

public class ProxyDialog extends AbstractDialog<HTTPProxy> implements CaretListener {

    public static void main(final String[] args) throws UnsupportedEncodingException, DialogClosedException, DialogCanceledException {
        // SyntheticaHelper.init();
        Dialog.getInstance().showDialog(new ProxyDialog(HTTPProxy.NONE, "No Connection to the Internet. Please check your Connection settings!"));
    }

    private JComboBox                cmbType;
    private ExtTextField             txtHost;
    private ExtTextField             txtPort;
    private ExtTextField             txtUser;

    private ExtPasswordField         txtPass;
    private final String[]           types        = new String[] { _AWU.T.ProxyDialog_http(), _AWU.T.ProxyDialog_socks5(), _AWU.T.ProxyDialog_socks4(), _AWU.T.ProxyDialog_direct() };
    private JLabel                   lblUser;
    private JLabel                   lblPass;
    private JLabel                   lblPort;
    private JLabel                   lblHost;
    private DelayedRunnable          delayer;
    private TYPE                     type;
    private final HTTPProxy          proxy;
    private ExtTextArea              desc;
    private final String             message;
    private ExtCheckBox              cbAuth;
    private boolean                  authRequired = false;

    private ScheduledExecutorService executer;

    public ProxyDialog(final HTTPProxy usedProxy, final String message) {
        super(Dialog.STYLE_HIDE_ICON, _AWU.T.proxydialog_title(), null, _AWU.T.lit_save(), _AWU.T.ABSTRACTDIALOG_BUTTON_CANCEL());
        this.proxy = usedProxy;
        this.message = message;
    }

    @Override
    public void actionPerformed(final ActionEvent e) {
        if (e.getSource() == this.cmbType) {

            switch (this.cmbType.getSelectedIndex()) {
            case 0:
                // http
                this.txtPass.setEnabled(true);
                this.lblPass.setEnabled(true);
                this.txtPort.setEnabled(true);
                this.lblUser.setEnabled(true);
                this.txtUser.setEnabled(true);
                this.lblPort.setEnabled(true);

                if (StringUtils.isEmpty(this.txtPort.getText())) {
                    this.txtPort.setText("8080");
                }
                break;
            case 1:
                // socks5
                this.txtPass.setEnabled(true);
                this.lblPass.setEnabled(true);
                this.txtPort.setEnabled(true);
                this.lblUser.setEnabled(true);
                this.txtUser.setEnabled(true);
                this.lblPort.setEnabled(true);
                if (StringUtils.isEmpty(this.txtPort.getText())) {
                    this.txtPort.setText("1080");
                }
                break;
            case 2:
                // socks4
                this.txtPass.setEnabled(false);
                this.lblPass.setEnabled(false);
                this.txtPort.setEnabled(true);
                this.lblUser.setEnabled(true);
                this.txtUser.setEnabled(true);
                this.lblPort.setEnabled(true);
                if (StringUtils.isEmpty(this.txtPort.getText())) {
                    this.txtPort.setText("1080");
                }
                break;
            case 3:
                // direct
                this.txtPass.setEnabled(false);
                this.lblPass.setEnabled(false);
                this.txtPort.setEnabled(false);
                this.lblUser.setEnabled(false);
                this.txtUser.setEnabled(false);
                this.lblPort.setEnabled(false);
                break;
            default:
                this.txtPass.setEnabled(false);
                this.lblPass.setEnabled(false);
                this.lblUser.setEnabled(true);
                this.txtUser.setEnabled(true);
                this.lblPort.setEnabled(true);
                if (StringUtils.isEmpty(this.txtPort.getText())) {
                    this.txtPort.setText("1080");
                }
            }
            this.cbAuth.updateDependencies();

        } else {
            super.actionPerformed(e);
        }
    }

    /**
     * update okayButton enabled status, check if host/port(valid number) or
     * host is given
     */
    public void caretUpdate(final CaretEvent e) {
        boolean enable = false;
        try {
            if (this.cmbType.getSelectedIndex() != 2) {
                if (this.txtHost.getDocument().getLength() > 0 && this.txtPort.getDocument().getLength() > 0) {
                    try {
                        final int port = Integer.parseInt(this.txtPort.getText());
                        if (port > 0 && port < 65535) {
                            enable = true;
                        }
                    } catch (final Throwable ee) {
                    }
                }
            } else {
                if (this.txtHost.getDocument().getLength() > 0) {
                    enable = true;
                }
            }
        } finally {
            this.okButton.setEnabled(enable);
        }
    }

    /**
     * returns HTTPProxy for given settings
     */
    @Override
    protected HTTPProxy createReturnValue() {
        final int mask = this.getReturnmask();
        if (BinaryLogic.containsSome(mask, Dialog.RETURN_CLOSED)) { return null; }
        if (BinaryLogic.containsSome(mask, Dialog.RETURN_CANCEL)) { return null; }
        try {

            HTTPProxy.TYPE type = null;
            if (this.cmbType.getSelectedIndex() == 0) {
                type = HTTPProxy.TYPE.HTTP;
            } else if (this.cmbType.getSelectedIndex() == 1) {
                type = HTTPProxy.TYPE.SOCKS5;
            } else if (this.cmbType.getSelectedIndex() == 2) {
                type = HTTPProxy.TYPE.SOCKS4;
            } else if (this.cmbType.getSelectedIndex() == 3) {
                type = HTTPProxy.TYPE.DIRECT;
                return HTTPProxy.parseHTTPProxy("direct://" + this.txtHost.getText());
            } else {
                return null;
            }
            final HTTPProxy ret = new HTTPProxy(type, this.txtHost.getText(), Integer.parseInt(this.txtPort.getText().trim()));

            ret.setPass(this.txtPass.getText());
            ret.setUser(this.txtUser.getText());
            return ret;
        } catch (final Throwable e) {
            Log.exception(e);
            return null;
        }
    }

    @Override
    public void dispose() {
        try {
            super.dispose();
            this.delayer.stop();
        } finally {
            try {
                this.executer.shutdown();
            } catch (final Throwable e) {
            }
        }
    }

    /**
     * @return
     */
    public String getHost() {
        // TODO Auto-generated method stub
        return this.txtHost.getText();
    }

    /**
     * @return
     */
    public String getPass() {
        // TODO txtP-generated method stub
        return this.txtPass.getText();
    }

    /**
     * @return
     */
    public int getPort() {
        try {
            return Integer.parseInt(this.txtPort.getText());
        } catch (final Exception e) {
            return -1;
        }
    }

    /**
     * @return
     */
    public HTTPProxy getProxy() {
        final HTTPProxy ret = new HTTPProxy(this.getType());
        ret.setHost(this.getHost());
        ret.setPort(this.getPort());
        if (this.isAuthEnabled()) {
            ret.setUser(this.getUser());
            ret.setPass(this.getPass());
        }
        return ret;
    }

    /**
     * @return
     */
    public TYPE getType() {
        switch (this.cmbType.getSelectedIndex()) {
        case 0:
            // http
            return TYPE.HTTP;

        case 1:
            // socks5
            return TYPE.SOCKS5;
        case 2:
            // socks4
            return TYPE.SOCKS4;
        case 3:
            if (StringUtils.isEmpty(this.txtHost.getText())) {
                return TYPE.NONE;
            } else {
                return TYPE.DIRECT;
            }
        }
        return TYPE.NONE;

    }

    /**
     * @return
     */
    public String getUser() {
        // TODO Auto-generated method stub
        return this.txtUser.getText();
    }

    /**
     * @return
     */
    private boolean isAuthEnabled() {

        return this.cbAuth.isSelected();
    }

    public boolean isAuthRequired() {
        return this.authRequired;
    }

    @Override
    public JComponent layoutDialogContent() {
        final JPanel panel = new JPanel(new MigLayout("ins 0, wrap 4", "[][grow 10,fill][][grow 3,fill]"));
        this.desc = new ExtTextArea();
        this.desc.setText(this.message);
        this.desc.setLabelMode(true);
        this.cmbType = new JComboBox(this.types);
        this.cmbType.addActionListener(this);
        this.lblHost = new JLabel(_AWU.T.ProxyDialog_hostport());
        this.desc.setFont(this.lblHost.getFont());
        this.txtHost = new ExtTextField() {
            @Override
            public void onChanged() {

                if (ProxyDialog.this.delayer != null) {
                    ProxyDialog.this.delayer.resetAndStart();
                }

            }

        };
        this.executer = Executors.newSingleThreadScheduledExecutor();
        this.delayer = new DelayedRunnable(this.executer, 2000) {

            @Override
            public void delayedrun() {
                new EDTRunner() {

                    @Override
                    protected void runInEDT() {
                        ProxyDialog.this.set(ProxyDialog.this.txtHost.getText());

                    }
                };

            }

        };
        this.txtHost.addCaretListener(this);
        this.lblPort = new JLabel(":");
        this.txtPort = new ExtTextField();

        this.txtPort.setText("8080");
        this.txtPort.addCaretListener(this);
        this.lblUser = new JLabel(_AWU.T.ProxyDialog_username());
        this.txtUser = new ExtTextField();

        this.lblPass = new JLabel(_AWU.T.ProxyDialog_password());
        this.txtPass = new ExtPasswordField();

        ;
        this.cbAuth = new ExtCheckBox(this.txtUser, this.lblPass, this.txtPass, this.lblUser);

        this.txtHost.setHelpText(_AWU.T.ProxyDialog_hostport_help());
        this.txtUser.setHelpText(_AWU.T.ProxyDialog_username_help());
        this.txtPass.setHelpText(_AWU.T.ProxyDialog_password_help());

        final JLabel lblCheckBox = new JLabel(_AWU.T.ProxyDialog_requires_auth());
        final MigPanel cbPanel = new MigPanel("ins 0", "[][grow]", "[]");

        cbPanel.add(this.cbAuth);
        cbPanel.add(lblCheckBox);
        // Layout#

        panel.add(this.desc, "spanx,pushx,growx,gapbottom 10");
        panel.add(new JLabel(_AWU.T.ProxyDialog_type()), "gapleft 10");
        panel.add(this.cmbType, "spanx");
        panel.add(this.lblHost, "gapleft 10");
        panel.add(this.txtHost);
        panel.add(this.lblPort);
        panel.add(this.txtPort, "shrinkx");
        panel.add(cbPanel, "spanx,gaptop 5,gapleft 5");
        panel.add(this.lblUser, "gapleft 10");
        panel.add(this.txtUser, "spanx");

        panel.add(this.lblPass, "gapleft 10");
        panel.add(this.txtPass, "spanx");

        this.okButton.setEnabled(true);
        this.registerFocus(this.txtPort);
        this.registerFocus(this.txtUser);
        this.registerFocus(this.txtHost);
        // set(ClipboardMonitoring.getINSTANCE().getCurrentContent());
        this.set(this.proxy);
        this.cbAuth.setSelected(this.isAuthRequired());
        this.cbAuth.updateDependencies();
        return panel;
    }

    /**
     * @param txtPort2
     */
    private void registerFocus(final JTextField field) {
        field.addFocusListener(new FocusListener() {

            @Override
            public void focusGained(final FocusEvent e) {
                field.selectAll();
            }

            @Override
            public void focusLost(final FocusEvent e) {
                // TODO Auto-generated method stub

            }
        });

    }

    /**
     * @param proxy2
     */
    private void set(final HTTPProxy p) {
        if (p == null) { return; }
        new EDTRunner() {
            /*
             * (non-Javadoc)
             * 
             * @see org.appwork.utils.swing.EDTRunner#runInEDT()
             */
            @Override
            protected void runInEDT() {
                ProxyDialog.this.txtUser.setText(p.getUser());
                switch (p.getType()) {
                case DIRECT:
                case NONE:
                    ProxyDialog.this.cmbType.setSelectedIndex(3);
                    ProxyDialog.this.txtHost.setText(p.getLocalIP() == null ? "" : p.getLocalIP().getHostAddress());

                    break;
                case HTTP:
                    ProxyDialog.this.cmbType.setSelectedIndex(0);
                    ProxyDialog.this.txtHost.setText(p.getHost());
                    ProxyDialog.this.txtPort.setText(p.getPort() + "");
                    ProxyDialog.this.txtUser.setText(p.getUser());
                    break;

                case SOCKS4:
                    ProxyDialog.this.cmbType.setSelectedIndex(2);
                    ProxyDialog.this.txtHost.setText(p.getHost());
                    ProxyDialog.this.txtPort.setText(p.getPort() + "");
                    break;
                case SOCKS5:
                    ProxyDialog.this.cmbType.setSelectedIndex(1);
                    ProxyDialog.this.txtHost.setText(p.getHost());
                    ProxyDialog.this.txtPort.setText(p.getPort() + "");
                    ProxyDialog.this.txtUser.setText(p.getUser());
                    break;
                }
            }
        };
    }

    protected void set(final String text) {

        final int carPos = this.txtHost.getCaretPosition();
        String myText = text;
        if (myText.endsWith(":")) { return; }
        for (int i = 0; i < 2; i++) {
            try {
                final URL url = new URL(myText);
                this.txtHost.setText(url.getHost());
                if (url.getPort() > 0) {
                    this.txtPort.setText(url.getPort() + "");
                }
                final String userInfo = url.getUserInfo();
                if (userInfo != null) {
                    final int in = userInfo.indexOf(":");
                    if (in >= 0) {
                        this.txtUser.setText(userInfo.substring(0, in));
                        this.txtPass.setText(userInfo.substring(in + 1));
                    } else {
                        this.txtUser.setText(userInfo);
                    }
                }
                return;
            } catch (final MalformedURLException e) {
                if (text.contains(":")) {
                    myText = "http://" + myText;
                }
            }
        }

        this.txtHost.setCaretPosition(carPos);

    }

    /**
     * @param b
     */
    public void setAuthRequired(final boolean b) {
        this.authRequired = b;
        if (this.cbAuth != null) {
            new EDTRunner() {

                @Override
                protected void runInEDT() {
                    ProxyDialog.this.cbAuth.setSelected(b);

                }
            };
        }

    }

}
