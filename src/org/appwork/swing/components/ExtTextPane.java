/**
 * Copyright (c) 2009 - 2011 AppWork UG(haftungsbeschränkt) <e-mail@appwork.org>
 * 
 * This file is part of org.appwork.swing.components
 * 
 * This software is licensed under the Artistic License 2.0,
 * see the LICENSE file or http://www.opensource.org/licenses/artistic-license-2.0.php
 * for details
 */
package org.appwork.swing.components;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.Shape;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;

import javax.swing.ImageIcon;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextPane;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import org.appwork.utils.swing.SwingUtils;

/**
 * @author thomas
 * 
 */
public class ExtTextPane extends JTextPane implements DocumentListener, FocusListener {
    /**
     * 
     */
    private static final long serialVersionUID = -8609711635557381385L;
    private Color       defaultColor;
    private Color       helpColor;

    {

        this.getDocument().addDocumentListener(this);
        this.addFocusListener(this);
        this.defaultColor = this.getForeground();
        this.helpColor = (Color) UIManager.get("TextField.disabledForeground");
        if (this.helpColor == null) {
            this.helpColor = Color.LIGHT_GRAY;
        }
    }
    private String      helpText = null;
    protected ImageIcon badgeIcon;

    /*
     * (non-Javadoc)
     * 
     * @see javax.swing.event.DocumentListener#changedUpdate(javax.swing.event.
     * DocumentEvent)
     */
    @Override
    public void changedUpdate(final DocumentEvent e) {
        this.onChanged();
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

    /*
     * (non-Javadoc)
     * 
     * @see javax.swing.event.DocumentListener#insertUpdate(javax.swing.event.
     * DocumentEvent)
     */
    @Override
    public void insertUpdate(final DocumentEvent e) {
        this.onChanged();
    }

    public boolean isHelpTextVisible() {
        return this.helpText != null && this.helpText.equals(super.getText());
    }

    /**
     * 
     */
    public void onChanged() {
        // TODO Auto-generated method stub

    }

    public void paintBadge(final Graphics g) {
        if (this.badgeIcon != null) {
            if (this.getParent().getParent() instanceof JScrollPane) {
                final Point rec = SwingUtilities.convertPoint(this, new Point(0, 0), this.getParent().getParent());
                g.translate(-rec.x, -rec.y);
                g.drawImage(this.badgeIcon.getImage(), (int) (this.getParent().getParent().getWidth() - this.badgeIcon.getIconWidth() / 1.5), (int) (this.getParent().getParent().getHeight() - this.badgeIcon.getIconHeight() / 1.5), null);
                g.translate(rec.x, rec.y);
            } else {
                g.drawImage(this.badgeIcon.getImage(), (int) (this.getWidth() - this.badgeIcon.getIconWidth() / 1.5), (int) (this.getHeight() - this.badgeIcon.getIconHeight() / 1.5), null);

            }
        }
    }

    @Override
    public void paintComponent(final Graphics g) {
        super.paintComponent(g);
        final Shape clp = g.getClip();
        g.setClip(null);
        this.paintBadge(g);
        g.setClip(clp);

    }

    /*
     * (non-Javadoc)
     * 
     * @see javax.swing.event.DocumentListener#removeUpdate(javax.swing.event.
     * DocumentEvent)
     */
    @Override
    public void removeUpdate(final DocumentEvent e) {
        this.onChanged();
    }

    /**
     * @param icon
     */
    public void setBadgeIcon(final ImageIcon icon) {
        this.badgeIcon = icon;
        this.repaint();

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

    /**
     * if label mode is enabled, the textfield will act like a MUltiline jlabel
     * 
     * @param b
     */
    public void setLabelMode(final boolean b) {
        this.setEditable(!b);
        this.setFocusable(!b);
        this.setBorder(b ? null : new JTextArea().getBorder());
        SwingUtils.setOpaque(this, !b);
    }

    @Override
    public void setText(String t) {
        if (!this.hasFocus() && this.helpText != null && (t == null || t.length() == 0)) {
            t = this.helpText;
        }

        super.setText(t);
        if (this.helpText != null) {
            if (this.helpText.equals(t)) {
                this.setForeground(this.helpColor);
            } else {

                this.setForeground(this.defaultColor);
            }
        }
    }
}
