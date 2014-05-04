package org.appwork.swing.components;

import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.KeyListener;
import java.awt.event.MouseListener;
import java.util.concurrent.atomic.AtomicInteger;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPasswordField;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import org.appwork.app.gui.BasicGui;
import org.appwork.swing.MigPanel;

public class ExtPasswordField extends MigPanel implements FocusListener, DocumentListener, TextComponentInterface {

    /**
     * 
     */
    private static final long serialVersionUID = 9035297840443317147L;

    public static String      MASK             = "••••••••••";

    public static void main(final String[] args) {

        new BasicGui("ExtPasswordField") {

            @Override
            protected void layoutPanel() {
                final ExtPasswordField pw = new ExtPasswordField();
                final ExtPasswordField pwtext = new ExtPasswordField();
                pwtext.setPassword("thomas".toCharArray());
                final ExtPasswordField pwhelp = new ExtPasswordField();
                pwhelp.setName("pwhelp");
                final ExtPasswordField pwhelptext = new ExtPasswordField();
                pwhelptext.setPassword("thomas".toCharArray());
                pwhelp.setHelpText("Please give me a password");
                pwhelptext.setHelpText("BLABLA gimme a pw");
                final MigPanel p = new MigPanel("ins 0,wrap 2", "[][grow,fill]", "[]");
                getFrame().setContentPane(p);
                p.add(new JLabel("PW field"));
                p.add(pw);
                p.add(new JLabel("PW width help text"));
                p.add(pwhelp);
                p.add(new JLabel("PW field setpw"));
                p.add(pwtext);
                p.add(new JLabel("PW field setpw &helptext"));
                p.add(pwhelptext);
                p.add(new JButton(new AbstractAction() {
                    /**
                     * 
                     */
                    private static final long serialVersionUID = 7405750769257653425L;

                    {
                        putValue(Action.NAME, "Print");
                    }

                    @Override
                    public void actionPerformed(final ActionEvent e) {
                        System.out.println(new String(pw.getPassword()));
                        System.out.println(new String(pwhelp.getPassword()));
                        System.out.println(new String(pwtext.getPassword()));
                        System.out.println(new String(pwhelptext.getPassword()));
                    }
                }));

            }

            @Override
            protected void requestExit() {
                // TODO Auto-generated method stub

            }
        };
    }

    private final ExtTextField   renderer;

    private final JPasswordField editor;

    private boolean              rendererMode;

    private char[]               password = new char[] {};

    private String               mask     = null;
    private final AtomicInteger  modifier = new AtomicInteger(0);

    /**
     * @param constraints
     * @param columns
     * @param rows
     */
    public ExtPasswordField() {
        super("ins 0", "[grow,fill]", "[grow,fill]");
        renderer = new ExtTextField();
        editor = new JPasswordField();

        renderer.addFocusListener(this);
        editor.addFocusListener(this);
        this.add(renderer, "hidemode 3");
        this.add(editor, "hidemode 3");
        editor.setText("");
        // this.renderer.setBackground(Color.RED);
        renderer.setText("");
        editor.getDocument().addDocumentListener(this);
        renderer.setHelpText("");
        setRendererMode(true);

    }

    @Override
    public synchronized void addKeyListener(final KeyListener l) {
        renderer.addKeyListener(l);
        editor.addKeyListener(l);
    }

    @Override
    public synchronized void addMouseListener(final MouseListener l) {

        renderer.addMouseListener(l);
        editor.addMouseListener(l);
    }

    /*
     * (non-Javadoc)
     * 
     * @see javax.swing.event.DocumentListener#changedUpdate(javax.swing.event.
     * DocumentEvent)
     */
    @Override
    public void changedUpdate(final DocumentEvent e) {
        if (modifier.get() > 0) { return; }
        if (!equal(editor.getPassword(), getMask().toCharArray())) {
            onChanged();
        }

    }

    /**
     * @param pw
     * @param ca
     * @return
     */
    private boolean equal(final char[] pw, final char[] ca) {
        if (pw.length == ca.length) {
            for (int i = 0; i < pw.length; i++) {
                if (pw[i] != ca[i]) { return false; }
            }
            return true;
        } else {
            return false;
        }

    }

    /*
     * (non-Javadoc)
     * 
     * @see java.awt.event.FocusListener#focusGained(java.awt.event.FocusEvent)
     */
    @Override
    public void focusGained(final FocusEvent e) {
        if (e.getSource() == renderer) {
            setRendererMode(false);
            editor.requestFocus();
        } else {
            setEditorText(renderer.getText());
            editor.selectAll();
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.awt.event.FocusListener#focusLost(java.awt.event.FocusEvent)
     */
    @Override
    public void focusLost(final FocusEvent e) {
        if (e.getSource() == editor || e == null) {
            final char[] pw = editor.getPassword();
            final char[] ca = getMask().toCharArray();

            if (!equal(pw, ca)) {
                password = pw;
                setEditorText(getMask());
            }
            setRendererMode(true);
            setHelpText(getHelpText());
        }
    }

    public javax.swing.text.Document getDocument() {
        return editor.getDocument();
    }

    public Color getHelpColor() {
        return renderer.getHelpColor();
    }

    public String getHelpText() {
        return renderer.getHelpText();
    }

    /**
     * @return
     */
    protected String getMask() {
        // TODO Auto-generated method stub
        return mask != null ? mask : ExtPasswordField.MASK;
    }

    public char[] getPassword() {
        if (editor.isVisible()) {
            final char[] pw = editor.getPassword();
            final char[] ca = getMask().toCharArray();

            if (!equal(pw, ca)) {
                password = pw;
            }
        }
        return password;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.appwork.utils.swing.dialog.TextComponentInterface#getText()
     */
    @Override
    public String getText() {
        return getPassword() == null ? null : new String(getPassword());
    }

    /*
     * (non-Javadoc)
     * 
     * @see javax.swing.event.DocumentListener#insertUpdate(javax.swing.event.
     * DocumentEvent)
     */
    @Override
    public void insertUpdate(final DocumentEvent e) {
        if (modifier.get() > 0) { return; }
        if (!equal(editor.getPassword(), getMask().toCharArray())) {
            onChanged();
        }
        if(editor.getPassword().length>0&&!equal(editor.getPassword(), getMask().toCharArray())){
            renderer.setText(getMask());
        }else{
            renderer.setText("");
        }

    }

    public void onChanged() {

    }

    @Override
    public synchronized void removeKeyListener(final KeyListener l) {
        renderer.removeKeyListener(l);
        editor.removeKeyListener(l);
    }

    @Override
    public synchronized void removeMouseListener(final MouseListener l) {
        renderer.removeMouseListener(l);
        editor.removeMouseListener(l);
    }

    /*
     * (non-Javadoc)
     * 
     * @see javax.swing.event.DocumentListener#removeUpdate(javax.swing.event.
     * DocumentEvent)
     */
    @Override
    public void removeUpdate(final DocumentEvent e) {
        if (modifier.get() > 0) { return; }
        if (!equal(editor.getPassword(), getMask().toCharArray())) {
            onChanged();
        }

    }

    /*
     * (non-Javadoc)
     * 
     * @see org.appwork.swing.components.TextComponentInterface#selectAll()
     */
    @Override
    public void selectAll() {
        editor.selectAll();
    }

    private void setEditorText(final String text) {
        modifier.incrementAndGet();
        try {
            editor.setText(text);
        } finally {
            modifier.decrementAndGet();
        }
    }

    @Override
    public void setEnabled(final boolean b) {
        editor.setEnabled(b);
        renderer.setEnabled(b);
        super.setEnabled(b);
    }

    public void setHelpColor(final Color helpColor) {
        renderer.setHelpColor(helpColor);
    }

    /**
     * @param addLinksDialog_layoutDialogContent_input_help
     */
    public void setHelpText(final String helpText) {
        renderer.setHelpText(helpText);
        if (getHelpText() != null && (getPassword() == null || getPassword().length == 0 || getMask().equals(new String(getPassword())))) {
            renderer.setText(getHelpText());
            renderer.setForeground(getHelpColor());
        } else {
            renderer.setText(getMask());
            renderer.setForeground(renderer.getDefaultColor());
        }
        setRendererMode(rendererMode);

    }

    public void setMask(final String mask) {
        this.mask = mask;
    }

    public void setPassword(final char[] password) {
        this.password = password;
        setHelpText(getHelpText());
    }

    /**
     * @param b
     */
    private void setRendererMode(boolean b) {
        rendererMode = b;
        b &= getHelpText() != null;
        renderer.setVisible(b);
        editor.setVisible(!b);
        revalidate();
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.appwork.utils.swing.dialog.TextComponentInterface#setText(java.lang
     * .String)
     */
    @Override
    public void setText(final String text) {
        setPassword(text == null ? null : text.toCharArray());
    }

}
