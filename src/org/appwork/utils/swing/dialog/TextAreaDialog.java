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

import java.io.IOException;

import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextArea;

import net.miginfocom.swing.MigLayout;

import org.appwork.utils.ImageProvider.ImageProvider;

public class TextAreaDialog extends AbstractDialog<String> {

    private static final long serialVersionUID = 5129590048597691591L;

    private String message;

    private String def;

    private JTextArea txtArea;

    public TextAreaDialog(String title, String message, String def) throws IOException {
        super(0, title, ImageProvider.getImageIcon("info", 32, 32), null, null);
        this.message = message;
        this.def = def;
    }

    @Override
    public JComponent layoutDialogContent() {
        JPanel panel = new JPanel(new MigLayout("ins 0, wrap 1", "[grow, fill]", "[]5[]"));
        panel.add(new JLabel(message));
        panel.add(txtArea = new JTextArea(def), "h 100!");
        return panel;
    }

    public String getResult() {
        return txtArea.getText();
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.appwork.utils.swing.dialog.AbstractDialog#getRetValue()
     */
    @Override
    protected String createReturnValue() {
        return getResult();
    }

}
