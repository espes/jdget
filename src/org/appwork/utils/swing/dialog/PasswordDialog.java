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

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JScrollPane;
import javax.swing.JTextPane;
import javax.swing.text.JTextComponent;

import net.miginfocom.swing.MigLayout;

import org.appwork.utils.BinaryLogic;
import org.appwork.utils.locale._AWU;

public class PasswordDialog extends AbstractDialog<String> implements KeyListener, MouseListener {

    private static final long serialVersionUID = 9206575398715006581L;
    private String            message;
    private JTextPane         messageArea;
    private JTextComponent    input1;
    private JTextComponent    input2;
    private JTextComponent    input3;

    public PasswordDialog(int flag, String title, String message, ImageIcon icon, String okOption, String cancelOption) {
        super(flag, title, icon, okOption, cancelOption);
        this.message = message;
    }

    @Override
    public JComponent layoutDialogContent() {
        JPanel contentpane = new JPanel(new MigLayout("ins 0,wrap 2", "[fill,grow]"));
        messageArea = new JTextPane();
        messageArea.setBorder(null);
        messageArea.setBackground(null);
        messageArea.setOpaque(false);
        messageArea.setText(this.message);
        messageArea.setEditable(false);
        messageArea.putClientProperty("Synthetica.opaque", Boolean.FALSE);

        contentpane.add("span 2", messageArea);
        contentpane.add(new JLabel(_AWU.T.PASSWORDDIALOG_PASSWORDCHANGE_OLDPASSWORD()));
        if (BinaryLogic.containsAll(flagMask, Dialog.STYLE_LARGE)) {
            input1 = new JPasswordField();
            input1.addKeyListener(this);
            input1.addMouseListener(this);
            contentpane.add(new JScrollPane(input1), "height 20:60:n,pushy,growy,w 250");
        } else {
            input1 = new JPasswordField();
            input1.setBorder(BorderFactory.createEtchedBorder());
            input1.addKeyListener(this);
            input1.addMouseListener(this);
            contentpane.add(input1, "pushy,growy,w 250");
        }
        contentpane.add(new JLabel(_AWU.T.PASSWORDDIALOG_PASSWORDCHANGE_NEWPASSWORD()));
        if (BinaryLogic.containsAll(flagMask, Dialog.STYLE_LARGE)) {
            input2 = new JPasswordField();
            input2.addKeyListener(this);
            input2.addMouseListener(this);
            contentpane.add(new JScrollPane(input2), "height 20:60:n,pushy,growy,w 250");
        } else {
            input2 = new JPasswordField();
            input2.setBorder(BorderFactory.createEtchedBorder());
            input2.addKeyListener(this);
            input2.addMouseListener(this);
            contentpane.add(input2, "pushy,growy,w 250");
        }
        contentpane.add(new JLabel(_AWU.T.PASSWORDDIALOG_PASSWORDCHANGE_NEWPASSWORD_REPEAT()));
        if (BinaryLogic.containsAll(flagMask, Dialog.STYLE_LARGE)) {
            input3 = new JPasswordField();
            input3.addKeyListener(this);
            input3.addMouseListener(this);
            contentpane.add(new JScrollPane(input3), "height 20:60:n,pushy,growy,w 250");
        } else {
            input3 = new JPasswordField();
            input3.setBorder(BorderFactory.createEtchedBorder());
            input3.addKeyListener(this);
            input3.addMouseListener(this);
            contentpane.add(input3, "pushy,growy,w 250");
        }

        return contentpane;
    }

    @Override
    protected void packed() {
        input1.selectAll();
        requestFocus();
        input1.requestFocusInWindow();
    }

    public void keyPressed(KeyEvent e) {
        this.cancel();
    }

    public void keyReleased(KeyEvent e) {
    }

    public void keyTyped(KeyEvent e) {
    }

    public void mouseClicked(MouseEvent e) {
        this.cancel();
    }

    public void mouseEntered(MouseEvent e) {
    }

    public void mouseExited(MouseEvent e) {
    }

    public void mousePressed(MouseEvent e) {
    }

    public void mouseReleased(MouseEvent e) {
    }

    @Override
    protected String createReturnValue() {
        if ((this.getReturnmask() & (Dialog.RETURN_OK | Dialog.RETURN_TIMEOUT)) == 0) { return null; }
        return new String(((JPasswordField) input1).getPassword()) + ";" + new String(((JPasswordField) input2).getPassword()) + ";" + new String(((JPasswordField) input3).getPassword());
    }

}
