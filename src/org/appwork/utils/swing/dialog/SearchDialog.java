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
import java.io.IOException;

import javax.swing.BorderFactory;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.JTextPane;
import javax.swing.text.JTextComponent;

import net.miginfocom.swing.MigLayout;

import org.appwork.storage.ConfigInterface;
import org.appwork.utils.ImageProvider.ImageProvider;
import org.appwork.utils.locale.Tl8;

public class SearchDialog extends AbstractDialog implements KeyListener, MouseListener {

    private static final long serialVersionUID = 9206575398715006581L;

    private String message;
    private JTextPane messageArea;
    private JTextComponent input;

    private JCheckBox caseSensitive;

    private JCheckBox regularExpression;

    public SearchDialog(int flag, String title, String message) throws IOException {
        super(flag | Dialog.BUTTONS_HIDE_CANCEL, title, ImageProvider.getImageIcon("find", 32, 32), Tl8.SEARCHDIALOG_BUTTON_FIND.toString(), null);

        this.caseSensitive = new JCheckBox(Tl8.SEARCHDIALOG_CHECKBOX_CASESENSITIVE.toString());
        this.regularExpression = new JCheckBox(Tl8.SEARCHDIALOG_CHECKBOX_REGULAREXPRESSION.toString());

        try {
            caseSensitive.setSelected(ConfigInterface.getStorage("SearchDialog").get("caseSensitive", false));

            regularExpression.setSelected(ConfigInterface.getStorage("SearchDialog").get("regularExpression", false));
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        this.message = message;

        init();
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

        input = new JTextField();
        input.setBorder(BorderFactory.createEtchedBorder());

        input.addKeyListener(this);
        input.addMouseListener(this);
        contentpane.add(input, "pushy,growy");
        contentpane.add(regularExpression, "split 2, alignx right, pushx");
        contentpane.add(caseSensitive, " alignx right");
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
        if (input.getText() == null || input.getText().equals("")) return null;
        try {
            ConfigInterface.getStorage("SearchDialog").put("caseSensitive", this.caseSensitive.isSelected());

            ConfigInterface.getStorage("SearchDialog").put("regularExpression", this.regularExpression.isSelected());
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return input.getText();
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

    public boolean isCaseSensitive() {
        // TODO Auto-generated method stub
        return this.caseSensitive.isSelected();
    }

    public boolean isRegex() {
        // TODO Auto-generated method stub
        return this.regularExpression.isSelected();
    }

}
