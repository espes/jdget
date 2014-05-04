package org.appwork.utils.swing;

import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.JTextComponent;

public abstract class TextComponentChangeListener implements DocumentListener {

    public TextComponentChangeListener(final JTextComponent txt) {
        txt.getDocument().addDocumentListener(this);
    }

    protected abstract void onChanged(DocumentEvent e);

    public void changedUpdate(final DocumentEvent e) {
        this.onChanged(e);
    }

    public void insertUpdate(final DocumentEvent e) {
        this.onChanged(e);
    }

    public void removeUpdate(final DocumentEvent e) {
        this.onChanged(e);
    }

}
