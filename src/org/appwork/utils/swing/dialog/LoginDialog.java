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

import org.appwork.utils.BinaryLogic;
import org.appwork.utils.ImageProvider.ImageProvider;
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
            return password;
        }

        public String getUsername() {
            return username;
        }

        public boolean isSave() {
            return save;
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
        this(flag, APPWORKUTILS.AccountNew_AccountNew_title.s(), APPWORKUTILS.AccountNew_AccountNew_message.s(), ImageProvider.getImageIcon("login", 32, 32));
    }

    public LoginDialog(final int flag, final String title, final String message, final ImageIcon icon) {
        super(flag & 0xffffffff & ~Dialog.STYLE_SHOW_DO_NOT_DISPLAY_AGAIN, title, icon, null, null);
        rememberDisabled = BinaryLogic.containsAll(flag, LoginDialog.DISABLE_REMEMBER);
        this.message = message;
    }

    private JLabel addSettingName(final String name) {
        final JLabel lbl = new JLabel(name);
        lbl.setForeground(titleColor);
        return lbl;
    }

    @Override
    public void caretUpdate(final CaretEvent e) {
        if (accid.getText().length() == 0) {
            okButton.setEnabled(false);
        } else {
            okButton.setEnabled(true);
        }

    }

    @Override
    protected LoginData createReturnValue() {
        if ((getReturnmask() & (Dialog.RETURN_OK | Dialog.RETURN_TIMEOUT)) == 0) { return null; }
        return new LoginData(accid.getText(), new String(pass.getPassword()), save.isSelected());
    }

    @Override
    public JComponent layoutDialogContent() {

        final JPanel contentpane = new JPanel();
        titleColor = getBackground().darker().darker();
        accid = new JTextField(10);
        accid.addCaretListener(this);
        pass = new JPasswordField(10);
        save = new JCheckBox();
        if (rememberDisabled) {
            save.setEnabled(false);
        }

        contentpane.setLayout(new MigLayout("ins 5, wrap 2", "[]10[grow,fill]", "[][]"));
        contentpane.add(new JLabel(message), "spanx");
        contentpane.add(addSettingName(APPWORKUTILS.AccountNew_layoutDialogContent_accountname.s()));
        contentpane.add(accid, "sizegroup g1,width 100:250:n");
        contentpane.add(addSettingName(APPWORKUTILS.AccountNew_layoutDialogContent_password.s()));
        contentpane.add(pass, "sizegroup g1");
        contentpane.add(addSettingName(APPWORKUTILS.AccountNew_layoutDialogContent_save.s()));
        contentpane.add(save, "sizegroup g1");
        accid.setText(preUser);
        pass.setText(prePass);
        save.setSelected(preSave);
        return contentpane;
    }

    @Override
    protected void packed() {
        super.packed();
        setResizable(false);
        accid.selectAll();
        requestFocus();
        accid.requestFocusInWindow();
    }

    public void setPasswordDefault(final String password) {
        prePass = password;
    }

    public void setRememberDefault(final boolean preSave) {
        this.preSave = preSave;
    }

    public void setUsernameDefault(final String user) {
        preUser = user;

    }

}
