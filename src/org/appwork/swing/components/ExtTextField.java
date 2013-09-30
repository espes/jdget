package org.appwork.swing.components;

import java.awt.Color;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;

import javax.swing.AbstractAction;
import javax.swing.JPopupMenu;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.UIManager;
import javax.swing.event.CaretEvent;
import javax.swing.event.CaretListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import org.appwork.app.gui.copycutpaste.ContextMenuAdapter;
import org.appwork.utils.StringUtils;
import org.appwork.utils.swing.SwingUtils;

public class ExtTextField extends JTextField implements CaretListener, FocusListener, DocumentListener, ContextMenuAdapter, TextComponentInterface {
    /**
     * 
     */
    private static final long serialVersionUID     = -3625278218179478516L;
    protected Color           defaultColor;
    private Color             helpColor;

    {
        addCaretListener(this);
        addFocusListener(this);
        defaultColor = getForeground();
        helpColor = (Color) UIManager.get("TextField.disabledForeground");
        if (helpColor == null) {
            helpColor = Color.LIGHT_GRAY;
        }
        getDocument().addDocumentListener(this);

    }
    private String            helpText             = null;
    private boolean           setting;
    private boolean           clearHelpTextOnFocus = true;
    private boolean           helperEnabled        = true;

    public void caretUpdate(final CaretEvent arg0) {

    }

    /*
     * (non-Javadoc)
     * 
     * @see javax.swing.event.DocumentListener#changedUpdate(javax.swing.event.
     * DocumentEvent)
     */
    @Override
    public void changedUpdate(final DocumentEvent e) {
        if (!isHelperEnabled()) {
            onChanged();
            return;
        }
        if (!setting) {
            onChanged();
        }
    }

    public void focusGained(final FocusEvent arg0) {
        if (!isHelperEnabled()) { return; }
        if (super.getText().equals(helpText)) {
            if (isClearHelpTextOnFocus()) {
                setText("");
            } else {
                selectAll();
            }

        }
        setForeground(defaultColor);

    }

    public void focusLost(final FocusEvent arg0) {
        if (!isHelperEnabled()) { return; }
        if (getDocument().getLength() == 0 || super.getText().equals(helpText)) {
            setText(helpText);
            setForeground(helpColor);
        }

    }

    /**
     * @return
     */
    public Color getDefaultColor() {
        // TODO Auto-generated method stub
        return defaultColor;
    }

    public Color getHelpColor() {
        return helpColor;
    }

    public String getHelpText() {
        return helpText;
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.appwork.app.gui.copycutpaste.ContextMenuAdapter#getPopupMenu(org.
     * appwork.app.gui.copycutpaste.CutAction,
     * org.appwork.app.gui.copycutpaste.CopyAction,
     * org.appwork.app.gui.copycutpaste.PasteAction,
     * org.appwork.app.gui.copycutpaste.DeleteAction,
     * org.appwork.app.gui.copycutpaste.SelectAction)
     */
    @Override
    public JPopupMenu getPopupMenu(final AbstractAction cutAction, final AbstractAction copyAction, final AbstractAction pasteAction, final AbstractAction deleteAction, final AbstractAction selectAction) {
        final JPopupMenu menu = new JPopupMenu();

        menu.add(cutAction);
        menu.add(copyAction);
        menu.add(pasteAction);
        menu.add(deleteAction);
        menu.add(selectAction);
        return menu;
    }


    public void replaceSelection(final String content) {
        if (isHelperEnabled() && super.getText().equals(helpText)&&StringUtils.isNotEmpty(content)) {
            super.setText("");
        }
        super.replaceSelection(content);
        setForeground(defaultColor);
    }
    @Override
    public String getText() {
        String ret = super.getText();
        if (!isHelperEnabled()) { return ret; }
        if (ret.equals(helpText) && getForeground() == helpColor) {
            ret = "";
        }
        return ret;
    }

    /*
     * (non-Javadoc)
     * 
     * @see javax.swing.event.DocumentListener#insertUpdate(javax.swing.event.
     * DocumentEvent)
     */
    @Override
    public void insertUpdate(final DocumentEvent e) {
        if (!isHelperEnabled()) {
            onChanged();
            return;
        }
        if (!setting) {
            onChanged();
        }
    }

    public boolean isClearHelpTextOnFocus() {
        return clearHelpTextOnFocus;
    }

    public boolean isHelperEnabled() {
        return helperEnabled;
    }

    /**
     * 
     */
    public void onChanged() {
        // TODO Auto-generated method stub
    }

    /*
     * (non-Javadoc)
     * 
     * @see javax.swing.event.DocumentListener#removeUpdate(javax.swing.event.
     * DocumentEvent)
     */
    @Override
    public void removeUpdate(final DocumentEvent e) {
        if (!isHelperEnabled()) {
            onChanged();
            return;
        }
        if (!setting) {
            onChanged();
        }
    }

    /**
     * @param b
     */
    public void setClearHelpTextOnFocus(final boolean b) {
        clearHelpTextOnFocus = b;

    }

    public void setHelpColor(final Color helpColor) {
        this.helpColor = helpColor;
    }

    public void setHelperEnabled(final boolean helperEnabled) {
        this.helperEnabled = helperEnabled;
    }

    /**
     * @param addLinksDialog_layoutDialogContent_input_help
     */
    public void setHelpText(final String helpText) {
        final String old = this.helpText;
        this.helpText = helpText;
        if (this.getText().length() == 0 || this.getText().equals(old)) {
            setText(this.helpText);
            setForeground(helpColor);
        }
    }

    public void setLabelMode(final boolean b) {
        setEditable(!b);
        setFocusable(!b);
        setBorder(b ? null : new JTextArea().getBorder());
        SwingUtils.setOpaque(this, !b);
    }

    @Override
    public void setText(String t) {
        if(setting)return;
        if (!isHelperEnabled()) {
            super.setText(t);
            return;
        }
        setting = true;
        try {
            if (!hasFocus() && helpText != null && (t == null || t.length() == 0)) {
                t = helpText;
            }

            super.setText(t);
            if (helpText != null) {
                if (helpText.equals(t)) {
                    setForeground(helpColor);
                } else {

                    setForeground(defaultColor);
                }
            }
        } finally {
            setting = false;
        }
    }

}
