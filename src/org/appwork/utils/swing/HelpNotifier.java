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
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;

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
    }

    @Override
    public void caretUpdate(final CaretEvent arg0) {
        if (this.field != null) {
            if (this.field.getDocument().getLength() == 0 || this.field.getText().equals(this.infoTxt)) {
                if (this.listener != null) {
                    this.listener.onHelpNotifyShown(this.field);
                }
            } else {
                if (this.listener != null) {
                    this.listener.onHelpNotifyHidden(this.field);
                }
            }
        }
    }

    @Override
    public void focusGained(final FocusEvent arg0) {
        if (this.field != null) {
            if (this.field.getText().equals(this.infoTxt)) {
                this.field.setText("");
                this.field.setForeground(this.defaultColor);
            }
        }

    }

    @Override
    public void focusLost(final FocusEvent arg0) {
        if (this.field != null) {
            if (this.field.getDocument().getLength() == 0 || this.field.getText().equals(this.infoTxt)) {
                this.field.setText(this.infoTxt);
                this.field.setForeground(this.watchColor);
            }
        }
    }

}
