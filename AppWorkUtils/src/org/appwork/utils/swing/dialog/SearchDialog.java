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
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.JTextPane;
import javax.swing.text.JTextComponent;

import net.miginfocom.swing.MigLayout;

import org.appwork.resources.AWUTheme;
import org.appwork.storage.JSonStorage;
import org.appwork.uio.UIOManager;
import org.appwork.utils.locale._AWU;

public class SearchDialog extends AbstractDialog<String> implements KeyListener, MouseListener {

    private final String      message;
    private JTextPane         messageArea;
    private JTextComponent    input;

    private final JCheckBox   caseSensitive;

    private final JCheckBox   regularExpression;

    public SearchDialog(final int flag, final String title, final String message) {
        super(flag | UIOManager.BUTTONS_HIDE_CANCEL, title, AWUTheme.I().getIcon("dialog/find", 32), _AWU.T.SEARCHDIALOG_BUTTON_FIND(), null);

        caseSensitive = new JCheckBox(_AWU.T.SEARCHDIALOG_CHECKBOX_CASESENSITIVE());
        regularExpression = new JCheckBox(_AWU.T.SEARCHDIALOG_CHECKBOX_REGULAREXPRESSION());

        caseSensitive.setSelected(JSonStorage.getStorage("SearchDialog").get("caseSensitive", false));

        regularExpression.setSelected(JSonStorage.getStorage("SearchDialog").get("regularExpression", false));

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

    public String getReturnID() {
        if ((getReturnmask() & (Dialog.RETURN_OK | Dialog.RETURN_TIMEOUT)) == 0) { return null; }
        if (input.getText() == null || input.getText().equals("")) { return null; }
        try {
            JSonStorage.getStorage("SearchDialog").put("caseSensitive", caseSensitive.isSelected());

            JSonStorage.getStorage("SearchDialog").put("regularExpression", regularExpression.isSelected());
        } catch (final Exception e) {

            org.appwork.utils.logging.Log.exception(e);
        }
        return input.getText();
    }

    public boolean isCaseSensitive() {

        return caseSensitive.isSelected();
    }

    public boolean isRegex() {

        return regularExpression.isSelected();
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
    protected void initFocus(final JComponent focus) {
        input.selectAll();
     
        input.requestFocusInWindow();
    }
  

}
