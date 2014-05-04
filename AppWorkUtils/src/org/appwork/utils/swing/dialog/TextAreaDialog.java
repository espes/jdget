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

import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextArea;

import net.miginfocom.swing.MigLayout;

import org.appwork.resources.AWUTheme;
import org.appwork.utils.logging.Log;

public class TextAreaDialog extends AbstractDialog<String> {

    protected final String    message;

    protected final String    def;

    protected JTextArea       txtArea;

    public TextAreaDialog(final String title, final String message, final String def) {
        super(0, title, AWUTheme.I().getIcon("dialog/info", 32), null, null);

        Log.L.fine("Dialog \r\ntitle: " + title + "\r\nmsg:   \r\n" + message + "\r\ndef:   \r\n" + def);

        this.message = message;
        this.def = def;
    }

    @Override
    protected String createReturnValue() {
        if ((this.getReturnmask() & (Dialog.RETURN_OK | Dialog.RETURN_TIMEOUT)) == 0) { return null; }
        return this.txtArea.getText();
    }

    @Override
    public JComponent layoutDialogContent() {
        final JPanel panel = new JPanel(new MigLayout("ins 0, wrap 1", "[grow, fill]", "[]5[]"));
        panel.add(new JLabel(this.message));
        panel.add(this.txtArea = new JTextArea(this.def), "h 100!");
        return panel;
    }

}
