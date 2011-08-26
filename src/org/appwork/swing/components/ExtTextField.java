package org.appwork.swing.components;

import java.awt.Color;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;

import javax.swing.JTextField;
import javax.swing.UIManager;
import javax.swing.event.CaretEvent;
import javax.swing.event.CaretListener;

public class ExtTextField extends JTextField implements CaretListener, FocusListener {
    private Color  defaultColor;
    private Color  helpColor;

    {
        this.addCaretListener(this);
        this.addFocusListener(this);
        this.defaultColor = this.getForeground();
        this.helpColor = (Color) UIManager.get("TextField.disabledForeground");
        if (this.helpColor == null) {
            this.helpColor = Color.LIGHT_GRAY;
        }
    }
    private String helpText = null;

    public void caretUpdate(final CaretEvent arg0) {

    }

    public void focusGained(final FocusEvent arg0) {

        if (super.getText().equals(this.helpText)) {
            this.setText("");
            this.setForeground(this.defaultColor);
        }

    }

    public void focusLost(final FocusEvent arg0) {

        if (this.getDocument().getLength() == 0 || super.getText().equals(this.helpText)) {
            this.setText(this.helpText);
            this.setForeground(this.helpColor);
        }

    }

    public Color getHelpColor() {
        return this.helpColor;
    }

    public String getHelpText() {
        return this.helpText;
    }

    @Override
    public String getText() {
        String ret = super.getText();
        if (ret.equals(this.helpText)) {
            ret = "";
        }
        return ret;
    }

    public void setHelpColor(final Color helpColor) {
        this.helpColor = helpColor;
    }

    /**
     * @param addLinksDialog_layoutDialogContent_input_help
     */
    public void setHelpText(final String helpText) {
        this.helpText = helpText;
        if (this.getText().length() == 0) {
            this.setText(this.helpText);
            this.setForeground(this.helpColor);
        }

    }
}
