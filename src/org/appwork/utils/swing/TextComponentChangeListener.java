package org.appwork.utils.swing;

import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.JTextComponent;

public abstract class TextComponentChangeListener implements DocumentListener {

    /**
     * @param txtUser
     */
    public TextComponentChangeListener(final JTextComponent txt) {
        txt.getDocument().addDocumentListener(this);
        // TODO Auto-generated constructor stub
    }

    /*
     * (non-Javadoc)
     * 
     * @see javax.swing.event.DocumentListener#changedUpdate(javax.swing.event.
     * DocumentEvent)
     */

    public void changedUpdate(final DocumentEvent e) {
        this.onChanged(e);
    }

    /*
     * (non-Javadoc)
     * 
     * @see javax.swing.event.DocumentListener#insertUpdate(javax.swing.event.
     * DocumentEvent)
     */

    public void insertUpdate(final DocumentEvent e) {
        this.onChanged(e);
    }

    /**
     * @param e
     */
    protected abstract void onChanged(DocumentEvent e);

    /*
     * (non-Javadoc)
     * 
     * @see javax.swing.event.DocumentListener#removeUpdate(javax.swing.event.
     * DocumentEvent)
     */
   
    public void removeUpdate(final DocumentEvent e) {
        this.onChanged(e);
    }

}
