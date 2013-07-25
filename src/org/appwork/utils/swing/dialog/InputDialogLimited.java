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

    public InputDialogLimited(final int flag, final String title, final String message, final String defaultMessage, final ImageIcon icon, final String okOption, final String cancelOption, final int limit, final Pattern validCharsRegex) {
        super(flag, title, icon, okOption, cancelOption);

        this.defaultMessage = defaultMessage;
        this.message = message;
        this.limit = limit;
        this.validCharsRegex = validCharsRegex;

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

        contentpane.add(messageArea);

        input = new JTextFieldLimited(limit, validCharsRegex);
        input.setBorder(BorderFactory.createEtchedBorder());
        input.setText(defaultMessage);
        input.addKeyListener(this);
        input.addMouseListener(this);
        contentpane.add(input, "pushy,growy,w 450");

        return contentpane;
    }

    @Override
    protected void initFocus(final JComponent focus) {
        input.selectAll();
        
        input.requestFocusInWindow();
    }

 

    public String getReturnID() {
        if ((getReturnmask() & (Dialog.RETURN_OK | Dialog.RETURN_TIMEOUT)) == 0) { return null; }
        if (input == null || input.getText() == null) {
            return null;
        }
        return input.getText();
    }

    @Override
    protected String createReturnValue() {
        return getReturnID();
    }

    public void keyPressed(final KeyEvent e) {
        cancel();
    }

    public void keyReleased(final KeyEvent e) {
    }

    public void keyTyped(final KeyEvent e) {
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

}
