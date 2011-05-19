/**
 * Copyright (c) 2009 - 2011 AppWork UG(haftungsbeschränkt) <e-mail@appwork.org>
 * 
 * This file is part of org.appwork.utils.swing.dialog
 * 
 * This software is licensed under the Artistic License 2.0,
 * see the LICENSE file or http://www.opensource.org/licenses/artistic-license-2.0.php
 * for details
 */
package org.appwork.utils.swing.dialog;

import java.awt.Color;
import java.awt.event.ActionListener;

import javax.swing.ImageIcon;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.event.CaretEvent;
import javax.swing.event.CaretListener;

import net.miginfocom.swing.MigLayout;

import org.appwork.resources.AWUTheme;
import org.appwork.utils.BinaryLogic;
import org.appwork.utils.locale.APPWORKUTILS;
import org.appwork.utils.swing.dialog.LoginDialog.LoginData;

/**
 * @author thomas
 * 
 */
public class LoginDialog extends AbstractDialog<LoginData> implements ActionListener, CaretListener {
    public static class LoginData {
        private final String  username;
        private final String  password;
        private final boolean save;

        public LoginData(final String username, final String password, final boolean save) {
            super();
            this.username = username;
            this.password = password;
            this.save = save;
        }

        public String getPassword() {
            return this.password;
        }

        public String getUsername() {
            return this.username;
        }

        public boolean isSave() {
            return this.save;
        }
    }

    private static final long serialVersionUID = 4425873806383799500L;
    public static final int   DISABLE_REMEMBER = 1 << 20;

    public static void main(final String[] args) {
        try {
            Dialog.getInstance().showDialog(new LoginDialog(0));
        } catch (final DialogClosedException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (final DialogCanceledException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    private JTextField     accid;
    private JPasswordField pass;
    private Color          titleColor;
    private String         preUser;
    private String         prePass;
    private boolean        preSave = false;
    private JCheckBox      save;
    private final boolean  rememberDisabled;
    private final String   message;

    public LoginDialog(final int flag) {
        this(flag, APPWORKUTILS.T.AccountNew_AccountNew_title(), APPWORKUTILS.T.AccountNew_AccountNew_message(), AWUTheme.I().getIcon("login", 32));
    }

    public LoginDialog(final int flag, final String title, final String message, final ImageIcon icon) {
        super(flag & 0xffffffff & ~Dialog.STYLE_SHOW_DO_NOT_DISPLAY_AGAIN, title, icon, null, null);
        this.rememberDisabled = BinaryLogic.containsAll(flag, LoginDialog.DISABLE_REMEMBER);
        this.message = message;
    }

    private JLabel addSettingName(final String name) {
        final JLabel lbl = new JLabel(name);
        lbl.setForeground(this.titleColor);
        return lbl;
    }

    public void caretUpdate(final CaretEvent e) {
        if (this.accid.getText().length() == 0) {
            this.okButton.setEnabled(false);
        } else {
            this.okButton.setEnabled(true);
        }

    }

    @Override
    protected LoginData createReturnValue() {
        if ((this.getReturnmask() & (Dialog.RETURN_OK | Dialog.RETURN_TIMEOUT)) == 0) { return null; }
        return new LoginData(this.accid.getText(), new String(this.pass.getPassword()), this.save.isSelected());
    }

    @Override
    public JComponent layoutDialogContent() {

        final JPanel contentpane = new JPanel();
        this.titleColor = this.getBackground().darker().darker();
        this.accid = new JTextField(10);
        this.accid.addCaretListener(this);
        this.pass = new JPasswordField(10);
        this.save = new JCheckBox();
        if (this.rememberDisabled) {
            this.save.setEnabled(false);
        }

        contentpane.setLayout(new MigLayout("ins 5, wrap 2", "[]10[grow,fill]", "[][]"));
        contentpane.add(new JLabel(this.message), "spanx");
        contentpane.add(this.addSettingName(APPWORKUTILS.T.AccountNew_layoutDialogContent_accountname()));
        contentpane.add(this.accid, "sizegroup g1,width 100:250:n");
        contentpane.add(this.addSettingName(APPWORKUTILS.T.AccountNew_layoutDialogContent_password()));
        contentpane.add(this.pass, "sizegroup g1");
        contentpane.add(this.addSettingName(APPWORKUTILS.T.AccountNew_layoutDialogContent_save()));
        contentpane.add(this.save, "sizegroup g1");
        this.accid.setText(this.preUser);
        this.pass.setText(this.prePass);
        this.save.setSelected(this.preSave);
        return contentpane;
    }

    @Override
    protected void packed() {
        super.packed();
        this.setResizable(false);
        this.accid.selectAll();
        this.requestFocus();
        this.accid.requestFocusInWindow();
    }

    public void setPasswordDefault(final String password) {
        this.prePass = password;
    }

    public void setRememberDefault(final boolean preSave) {
        this.preSave = preSave;
    }

    public void setUsernameDefault(final String user) {
        this.preUser = user;

    }

}
