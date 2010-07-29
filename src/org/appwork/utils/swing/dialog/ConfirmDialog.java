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

import java.awt.Dimension;

import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.JScrollPane;
import javax.swing.JTextPane;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;

import org.appwork.utils.BinaryLogic;
import org.appwork.utils.os.CrossSystem;

public class ConfirmDialog extends AbstractDialog<Object> {

    private static final long serialVersionUID = -7647771640756844691L;

    private JTextPane textField;
    private String message;

    public ConfirmDialog(int flag, String title, String message, ImageIcon icon, String okOption, String cancelOption) {
        super(flag, title, icon, okOption, cancelOption);
        this.message = message;
    }

    @Override
    public String toString() {
        return ("dialog-" + this.getTitle() + "_" + message).replaceAll("\\W", "_");
    }

    @Override
    public Dimension getPreferredSize() {
        if (!BinaryLogic.containsAll(this.flagMask, Dialog.STYLE_LARGE)) {
            return super.getPreferredSize();
        } else {
            return new Dimension(600, 450);
        }
    }

    @Override
    public JComponent layoutDialogContent() {
        textField = new JTextPane() {
            private static final long serialVersionUID = 1L;

            @Override
            public boolean getScrollableTracksViewportWidth() {
                return !BinaryLogic.containsAll(flagMask, Dialog.STYLE_LARGE);
            }
        };
        if (BinaryLogic.containsAll(this.flagMask, Dialog.STYLE_HTML)) {
            textField.setContentType("text/html");
            textField.addHyperlinkListener(new HyperlinkListener() {

                public void hyperlinkUpdate(HyperlinkEvent e) {
                    if (e.getEventType() == HyperlinkEvent.EventType.ACTIVATED) {
                        CrossSystem.openURL(e.getURL());
                    }
                }

            });
        } else {
            textField.setContentType("text");
            textField.setMaximumSize(new Dimension(450, 600));
        }

        textField.setText(this.message);
        textField.setEditable(false);
        textField.setBackground(null);
        textField.setOpaque(false);
        textField.putClientProperty("Synthetica.opaque", Boolean.FALSE);

        if (BinaryLogic.containsAll(this.flagMask, Dialog.STYLE_LARGE)) {
            JScrollPane sp = new JScrollPane(textField);
            return sp;
        } else {
            return textField;
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.appwork.utils.swing.dialog.AbstractDialog#getRetValue()
     */
    @Override
    public Object getRetValue() {
        // TODO Auto-generated method stub
        return null;
    }

}
