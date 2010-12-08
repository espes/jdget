package org.appwork.utils.swing.input;

import java.awt.Color;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;

import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

public class JInputTextField extends javax.swing.JTextField implements DocumentListener, FocusListener {
    /**
     * 
     */
    private static final long serialVersionUID = 1L;
    private final Color       defaultColor;
    private Color             infoColor        = Color.GRAY;
    private boolean           changed;
    private String            infoText;

    public JInputTextField() {
        super();
        defaultColor = getForeground();
        getDocument().addDocumentListener(this);
        addFocusListener(this);

    }

    /**
     * @param s
     */
    public JInputTextField(final String helpText) {
        this();
        infoText = helpText;
        focusLost(null);
    }

    /*
     * (non-Javadoc)
     * 
     * @see javax.swing.event.DocumentListener#changedUpdate(javax.swing.event.
     * DocumentEvent)
     */
    
    public void changedUpdate(final DocumentEvent e) {
        changed = true;

        setForeground(defaultColor);
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.awt.event.FocusListener#focusGained(java.awt.event.FocusEvent)
     */
   
    public void focusGained(final FocusEvent e) {

        if (!changed) {
            setText("");
            setForeground(defaultColor);
        }

    }

    /*
     * (non-Javadoc)
     * 
     * @see java.awt.event.FocusListener#focusLost(java.awt.event.FocusEvent)
     */
  
    public void focusLost(final FocusEvent e) {
        if (getDocument().getLength() == 0 || getText().equals(getInfoText())) {
            setText(getInfoText());
            changed = false;
            setForeground(infoColor);
        }
    }

    public Color getInfoColor() {

        return infoColor;
    }

    /**
     * @return
     */
    protected String getInfoText() {
        // TODO Auto-generated method stub
        return infoText;
    }

    @Override
    public String getText() {
        if (!changed) { return ""; }
        return super.getText();
    }

    /*
     * (non-Javadoc)
     * 
     * @see javax.swing.event.DocumentListener#insertUpdate(javax.swing.event.
     * DocumentEvent)
     */
  
    public void insertUpdate(final DocumentEvent e) {
        changed = true;
        setForeground(defaultColor);
    }

    /*
     * (non-Javadoc)
     * 
     * @see javax.swing.event.DocumentListener#removeUpdate(javax.swing.event.
     * DocumentEvent)
     */
 
    public void removeUpdate(final DocumentEvent e) {
        changed = true;
        setForeground(defaultColor);
    }

    public void setInfoColor(final Color infoColor) {
        this.infoColor = infoColor;
    }

    public void setInfoText(final String infoText) {
        this.infoText = infoText;
    }
}
