/**
 * Copyright (c) 2009 - 2010 AppWork UG(haftungsbeschränkt) <e-mail@appwork.org>
 * 
 * This file is part of org.appwork.utils.swing.dialog
 * 
 * This software is licensed under the Artistic License 2.0,
 * see the LICENSE file or http://www.opensource.org/licenses/artistic-license-2.0.php
 * for details
 */
package org.appwork.utils.swing.dialog;

import java.awt.Cursor;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.ImageIcon;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.JTextPane;
import javax.swing.SwingConstants;

import net.miginfocom.swing.MigLayout;

import org.appwork.utils.BinaryLogic;
import org.appwork.utils.locale.Tl8;

public class LoginDialog extends AbstractDialog<String[]> implements KeyListener, MouseListener {

    private static final long serialVersionUID = 9206575398715006581L;
    public static final int REGISTER = 1 << 20;
    public static final int FORCE_REGISTER = 1 << 21;
    private String defaultMessage;
    private String message;
    private JTextPane messageArea;
    private JTextField login;
    private JPasswordField password;
    private boolean remember;
    private JCheckBox rem;
    private boolean register;
    private JLabel registerBtn;
    private JPasswordField rpassword;
    private JLabel rpasswordLabel;

    public LoginDialog(int flag, String title, String message, String defaultMessage, ImageIcon icon, String okOption, String cancelOption) {
        super(flag & 0xffffffff & (~Dialog.STYLE_SHOW_DO_NOT_DISPLAY_AGAIN), title, icon, okOption, cancelOption);
        // remove do not show again flag and convert to remember flag
        remember = BinaryLogic.containsAll(flag, Dialog.STYLE_SHOW_DO_NOT_DISPLAY_AGAIN);
        register = BinaryLogic.containsAll(flag, REGISTER);

        this.defaultMessage = defaultMessage;
        this.message = message;
    }

    @Override
    public JComponent layoutDialogContent() {
        JPanel cp = new JPanel(new MigLayout("ins 0,wrap 2", "[][fill,grow]"));
        messageArea = new JTextPane();
        messageArea.setBorder(null);
        messageArea.setBackground(null);
        messageArea.setOpaque(false);
        messageArea.setText(this.message);
        messageArea.setEditable(false);
        messageArea.putClientProperty("Synthetica.opaque", Boolean.FALSE);

        cp.add(messageArea, "spanx");
        cp.add(new JLabel(Tl8.LOGINDIALOG_LABEL_USERNAME.toString()), "alignx right");

        login = new JTextField();
        login.setBorder(BorderFactory.createEtchedBorder());
        login.setText(this.defaultMessage);
        login.addKeyListener(this);
        login.addMouseListener(this);
        cp.add(login, "pushy,growy");
        // password

        cp.add(new JLabel(Tl8.LOGINDIALOG_LABEL_PASSWORD.toString()), "alignx right");

        password = new JPasswordField();
        password.setBorder(BorderFactory.createEtchedBorder());
        password.addKeyListener(this);
        password.addMouseListener(this);
        cp.add(password, "pushy,growy");

        // register

        cp.add(rpasswordLabel = new JLabel(Tl8.LOGINDIALOG_LABEL_PASSWORD_REPEAT.toString()), "alignx right,hidemode 3");

        rpassword = new JPasswordField();
        rpassword.setBorder(BorderFactory.createEtchedBorder());
        rpassword.addKeyListener(this);
        rpassword.addMouseListener(this);
        cp.add(rpassword, "pushy,growy,hidemode 3");
        rpassword.setVisible(BinaryLogic.containsAll(this.flagMask, FORCE_REGISTER));
        rpasswordLabel.setVisible(BinaryLogic.containsAll(this.flagMask, FORCE_REGISTER));
        if (register) {
            registerBtn = new JLabel(Tl8.LOGINDIALOG_BUTTON_REGISTER.toString());
            registerBtn.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, cp.getBackground().darker().darker()));
            registerBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            registerBtn.addMouseListener(this);
            cp.add(registerBtn, "skip,split 2");
        } else {
            cp.add(Box.createHorizontalGlue(), "skip,split 2");
        }

        if (remember) {
            rem = new JCheckBox(Tl8.LOGINDIALOG_CHECKBOX_REMEMBER.toString());
            rem.setHorizontalTextPosition(SwingConstants.LEFT);
            rem.setHorizontalAlignment(SwingConstants.RIGHT);
            cp.add(rem, "pushx,growx,alignx right");

        }
        return cp;
    }

    @Override
    protected void packed() {
        login.selectAll();
        requestFocus();
        login.requestFocusInWindow();
    }

    /**
     * Returns an array login,password,repeated password,remember.<br>
     * repeated password is null if we are in login mode <br>
     * remember is null of the remember checkbox is unchecked
     * 
     * @return
     */
    public String[] getLogins() {
        if ((this.getReturnmask() & (Dialog.RETURN_OK | Dialog.RETURN_TIMEOUT)) == 0) { return null; }

        return new String[] { login.getText(), new String(password.getPassword()), rpassword.isVisible() ? new String(rpassword.getPassword()) : null, rem.isSelected() ? "yes" : null };
    }

    /**
     * returns if the "remember flag has been set"
     * 
     * @return
     */
    public boolean isRemember() {
        if ((this.getReturnmask() & (Dialog.RETURN_OK | Dialog.RETURN_TIMEOUT)) == 0) { return false; }

        return rem.isSelected();

    }

    public void keyPressed(KeyEvent e) {
        this.cancel();
    }

    public void keyReleased(KeyEvent e) {
    }

    public void keyTyped(KeyEvent e) {
    }

    public void mouseClicked(MouseEvent e) {
        if (e.getSource() == registerBtn) {
            if (rpassword.isVisible()) {
                rpassword.setVisible(false);
                rpasswordLabel.setVisible(false);
                registerBtn.setText(Tl8.LOGINDIALOG_BUTTON_REGISTER.toString());
            } else {
                rpassword.setVisible(true);
                rpasswordLabel.setVisible(true);
                registerBtn.setText(Tl8.LOGINDIALOG_BUTTON_LOGIN.toString());
            }

            this.pack();
        } else {
            this.cancel();
        }
    }

    public void mouseEntered(MouseEvent e) {
    }

    public void mouseExited(MouseEvent e) {
    }

    public void mousePressed(MouseEvent e) {
    }

    public void mouseReleased(MouseEvent e) {
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.appwork.utils.swing.dialog.AbstractDialog#getRetValue()
     */
    @Override
    protected String[] createReturnValue() {
        return getLogins();
    }

}
