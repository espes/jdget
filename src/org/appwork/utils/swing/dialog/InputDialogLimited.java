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
import java.util.regex.Pattern;

import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JTextPane;
import javax.swing.text.JTextComponent;

import net.miginfocom.swing.MigLayout;

import org.appwork.utils.swing.JTextFieldLimited;

/**
 * @author daniel
 * 
 */
public class InputDialogLimited extends AbstractDialog<String> implements KeyListener, MouseListener {

    private String defaultMessage;
    private String message;
    private JTextPane messageArea;
    private JTextComponent input;
    private int limit = 0;
    private Pattern validCharsRegex = null;

    public InputDialogLimited(int flag, String title, String message, String defaultMessage, ImageIcon icon, String okOption, String cancelOption, int limit, Pattern validCharsRegex) {
        super(flag, title, icon, okOption, cancelOption);

        this.defaultMessage = defaultMessage;
        this.message = message;
        this.limit = limit;
        this.validCharsRegex = validCharsRegex;

    }

    @Override
    public JComponent layoutDialogContent() {
        JPanel contentpane = new JPanel(new MigLayout("ins 0,wrap 1", "[fill,grow]"));
        messageArea = new JTextPane();
        messageArea.setBorder(null);
        messageArea.setBackground(null);
        messageArea.setOpaque(false);
        messageArea.setText(this.message);
        messageArea.setEditable(false);
        messageArea.putClientProperty("Synthetica.opaque", Boolean.FALSE);

        contentpane.add(messageArea);

        input = new JTextFieldLimited(limit, validCharsRegex);
        input.setBorder(BorderFactory.createEtchedBorder());
        input.setText(this.defaultMessage);
        input.addKeyListener(this);
        input.addMouseListener(this);
        contentpane.add(input, "pushy,growy,w 450");

        return contentpane;
    }

    @Override
    protected void packed() {
        input.selectAll();
        requestFocus();
        input.requestFocusInWindow();
    }

    public String getReturnID() {
        if ((this.getReturnmask() & (Dialog.RETURN_OK | Dialog.RETURN_TIMEOUT)) == 0) { return null; }
        if (input == null || input.getText() == null) return null;
        return input.getText();
    }

    @Override
    protected String createReturnValue() {
        return getReturnID();
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

}
