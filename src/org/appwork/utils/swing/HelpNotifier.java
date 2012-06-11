/**
 * Copyright (c) 2009 - 2010 AppWork UG(haftungsbeschränkt) <e-mail@appwork.org>
 * 
 * This file is part of org.appwork.utils.swing
 * 
 * This software is licensed under the Artistic License 2.0,
 * see the LICENSE file or http://www.opensource.org/licenses/artistic-license-2.0.php
 * for details
 */
package org.appwork.utils.swing;

import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.beans.PropertyChangeListener;

import javax.swing.Action;
import javax.swing.TransferHandler;
import javax.swing.event.CaretEvent;
import javax.swing.event.CaretListener;
import javax.swing.text.JTextComponent;

/**
 * @author daniel
 * 
 */
public class HelpNotifier implements FocusListener, CaretListener {

    public static void register(final JTextComponent field, final HelpNotifierCallbackListener owner, final String helpText) {
        new HelpNotifier(field, helpText, owner);
    }

    private final JTextComponent               field;
    protected Color                            defaultColor = null;
    protected Color                            watchColor   = Color.GRAY;
    private String                             infoTxt      = null;
    private final HelpNotifierCallbackListener listener;

    private HelpNotifier(final JTextComponent field, final String helpTxt, final HelpNotifierCallbackListener listener) {
        this.field = field;
        this.listener = listener;
        this.field.setText(helpTxt);
        this.infoTxt = helpTxt;
        this.defaultColor = field.getForeground();
        this.focusLost(null);
        this.caretUpdate(null);
        this.field.addCaretListener(this);
        this.field.addFocusListener(this);
        this.field.getActionMap().put("paste", new Action() {

            @Override
            public void actionPerformed(final ActionEvent e) {
                if (field.getText().equals(HelpNotifier.this.infoTxt)) {
                    field.setText("");
                    field.setForeground(HelpNotifier.this.defaultColor);
                }
                TransferHandler.getPasteAction().actionPerformed(e);
            }

            @Override
            public void addPropertyChangeListener(final PropertyChangeListener listener) {
                // TODO Auto-generated method stub

            }

            @Override
            public Object getValue(final String key) {
                // TODO Auto-generated method stub
                return null;
            }

            @Override
            public boolean isEnabled() {
                return TransferHandler.getPasteAction().isEnabled();
            }

            @Override
            public void putValue(final String key, final Object value) {
                // TODO Auto-generated method stub

            }

            @Override
            public void removePropertyChangeListener(final PropertyChangeListener listener) {
                // TODO Auto-generated method stub

            }

            @Override
            public void setEnabled(final boolean b) {
                // TODO Auto-generated method stub

            }

        });
    }

    public void caretUpdate(final CaretEvent arg0) {
        if (this.field != null) {
            if (this.field.getDocument().getLength() == 0 || this.field.getText().equals(this.infoTxt)) {
                if (this.listener != null) {
                    this.listener.onHelpNotifyShown(this.field);
                }
            } else {
                if (this.listener != null) {
                    this.listener.onHelpNotifyHidden(this.field);
                    /*
                     * if user sets text with setText, we want default color
                     * again
                     */
                    this.field.setForeground(this.defaultColor);
                }
            }
        }
    }

    public void focusGained(final FocusEvent arg0) {
        if (this.field != null) {
            if (this.field.getText().equals(this.infoTxt)) {
                this.field.setText("");
                this.field.setForeground(this.defaultColor);
            }
        }

    }

    public void focusLost(final FocusEvent arg0) {
        if (this.field != null) {
            if (this.field.getDocument().getLength() == 0 || this.field.getText().equals(this.infoTxt)) {
                this.field.setText(this.infoTxt);
                this.field.setForeground(this.watchColor);
            }
        }
    }

}
