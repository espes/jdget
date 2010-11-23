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

import javax.swing.JTextField;
import javax.swing.event.CaretEvent;
import javax.swing.event.CaretListener;

/**
 * @author daniel
 * 
 */
public class HelpNotifier implements FocusListener, CaretListener {

    private JTextField           field;
    protected Color              defaultColor = null;
    protected Color              watchColor   = Color.GRAY;
    private String               infoTxt      = null;
    private HelpNotifierListener listener;

    public HelpNotifier(JTextField field, String helpTxt, HelpNotifierListener listener) {
        this.field = field;
        this.listener = listener;
        this.field.setText(helpTxt);
        this.infoTxt = helpTxt;
        this.defaultColor = field.getForeground();        
        focusLost(null);
        caretUpdate(null);
        this.field.addCaretListener(this);
        this.field.addFocusListener(this);
    }

    @Override
    public void caretUpdate(CaretEvent arg0) {
        if (field != null) {
            if (field.getDocument().getLength() == 0 || field.getText().equals(infoTxt)) {
                if (listener != null) {
                    listener.helpNotifier_Shown(field);
                }
            } else {
                if (listener != null) {
                    listener.helpNotifier_Hidden(field);
                }
            }
        }
    }

    @Override
    public void focusGained(FocusEvent arg0) {
        if (field != null) {
            if (field.getText().equals(infoTxt)) {
                field.setText("");
                field.setForeground(defaultColor);
            }
        }

    }

    @Override
    public void focusLost(FocusEvent arg0) {
        if (field != null) {
            if (field.getDocument().getLength() == 0 || field.getText().equals(infoTxt)) {
                field.setText(infoTxt);
                field.setForeground(watchColor);
            }
        }
    }

}
