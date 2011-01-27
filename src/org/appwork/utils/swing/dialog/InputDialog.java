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
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.JTextPane;
import javax.swing.text.JTextComponent;

import net.miginfocom.swing.MigLayout;

import org.appwork.utils.BinaryLogic;
import org.appwork.utils.logging.Log;

public class InputDialog extends AbstractDialog<String> implements KeyListener, MouseListener {

    private static final long serialVersionUID = 9206575398715006581L;
    protected String          defaultMessage;
    protected String          message;

    private JTextPane         messageArea;

    private JTextComponent    input;

    public InputDialog(final int flag, final String title, final String message, final String defaultMessage, final ImageIcon icon, final String okOption, final String cancelOption) {
        super(flag, title, icon, okOption, cancelOption);
        Log.L.fine("Dialog    [" + okOption + "][" + cancelOption + "]\r\nflag:  " + Integer.toBinaryString(flag) + "\r\ntitle: " + title + "\r\nmsg:   \r\n" + message + "\r\ndef:   \r\n" + defaultMessage);

        this.defaultMessage = defaultMessage;
        this.message = message;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.appwork.utils.swing.dialog.AbstractDialog#getRetValue()
     */
    @Override
    protected String createReturnValue() {
        return getReturnID();
    }

    public String getDefaultMessage() {
        return defaultMessage;
    }

    public String getMessage() {
        return message;
    }

    public String getReturnID() {
        if ((getReturnmask() & (Dialog.RETURN_OK | Dialog.RETURN_TIMEOUT)) == 0) { return null; }
        if (input == null || input.getText() == null) { return null; }
        if (input instanceof JPasswordField) { return new String(((JPasswordField) input).getPassword()); }
        return input.getText();
    }

    public void keyPressed(final KeyEvent e) {
        cancel();
    }

    public void keyReleased(final KeyEvent e) {
    }

    public void keyTyped(final KeyEvent e) {
    }

    @Override
    public JComponent layoutDialogContent() {
        final JPanel contentpane = new JPanel(new MigLayout("ins 0,wrap 1", "[fill,grow]"));
        messageArea = new JTextPane();
        messageArea.setBorder(null);
        messageArea.setBackground(null);
        messageArea.setOpaque(false);
        messageArea.setText(message);
        messageArea.setEditable(false);
        messageArea.putClientProperty("Synthetica.opaque", Boolean.FALSE);
        if (BinaryLogic.containsAll(flagMask, Dialog.STYLE_HTML)) {
            messageArea.setContentType("text/html");
        }
        contentpane.add(messageArea);
        if (BinaryLogic.containsAll(flagMask, Dialog.STYLE_LARGE)) {
            input = new JTextPane();

            input.setText(defaultMessage);
            input.addKeyListener(this);
            input.addMouseListener(this);
            contentpane.add(new JScrollPane(input), "height 20:60:n,pushy,growy,w 450");
        } else {
            input = BinaryLogic.containsAll(flagMask, Dialog.STYLE_PASSWORD) ? new JPasswordField() : new JTextField();
            input.setBorder(BorderFactory.createEtchedBorder());
            input.setText(defaultMessage);
            input.addKeyListener(this);
            input.addMouseListener(this);
            contentpane.add(input, "pushy,growy,w 450");
        }

        return contentpane;
    }

    public void mouseClicked(final MouseEvent e) {
        cancel();
    }

    public void mouseEntered(final MouseEvent e) {
    }

    public void mouseExited(final MouseEvent e) {
    }

    public void mousePressed(final MouseEvent e) {
    }

    public void mouseReleased(final MouseEvent e) {
    }

    @Override
    protected void packed() {
        input.selectAll();
        requestFocus();
        input.requestFocusInWindow();
    }

    public void setDefaultMessage(final String defaultMessage) {
        this.defaultMessage = defaultMessage;
        if (input != null) {
            input.setText(defaultMessage);
        }
    }

    public void setMessage(final String message) {
        this.message = message;
    }

}
