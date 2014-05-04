package org.appwork.swing.components;

import java.awt.Color;
import java.awt.Point;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.MouseEvent;

import javax.swing.JComponent;
import javax.swing.JTextArea;
import javax.swing.UIManager;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import org.appwork.swing.components.tooltips.ExtTooltip;
import org.appwork.swing.components.tooltips.ToolTipController;
import org.appwork.swing.components.tooltips.ToolTipHandler;
import org.appwork.swing.components.tooltips.TooltipFactory;
import org.appwork.swing.components.tooltips.TooltipTextDelegateFactory;
import org.appwork.utils.StringUtils;
import org.appwork.utils.swing.SwingUtils;

public class ExtTextArea extends JTextArea implements FocusListener, DocumentListener, ToolTipHandler {
    /**
     * 
     */
    private static final long serialVersionUID = -2215616004852131754L;
    protected Color           defaultColor;
    private Color             helpColor;
    private TooltipFactory    tooltipFactory;

    {
        tooltipFactory = new TooltipTextDelegateFactory(this);
        getDocument().addDocumentListener(this);
        addFocusListener(this);
        defaultColor = getForeground();
        helpColor = (Color) UIManager.get("TextField.disabledForeground");
        if (helpColor == null) {
            helpColor = Color.LIGHT_GRAY;
        }
    }
    private String            helpText         = null;

    /*
     * (non-Javadoc)
     * 
     * @see javax.swing.event.DocumentListener#changedUpdate(javax.swing.event.
     * DocumentEvent)
     */
    @Override
    public void changedUpdate(final DocumentEvent e) {
        onChanged();
    }

    public void focusGained(final FocusEvent arg0) {

        if (super.getText().equals(helpText)) {
            setText("");

        }
        setForeground(defaultColor);
    }

    public void focusLost(final FocusEvent arg0) {

        if (getDocument().getLength() == 0 || super.getText().equals(helpText)) {
            setText(helpText);
            setForeground(helpColor);
        }

    }

    public Color getHelpColor() {
        return helpColor;
    }

    public String getHelpText() {
        return helpText;
    }

    public void replaceSelection(final String content) {
        if ( super.getText().equals(helpText)&&StringUtils.isNotEmpty(content)) {
            super.setText("");
        }
        super.replaceSelection(content);
        setForeground(defaultColor);
    }
    @Override
    public String getText() {
        String ret = super.getText();
        if (ret.equals(helpText)) {
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
        onChanged();
    }

    public boolean isHelpTextVisible() {
        return helpText != null && helpText.equals(super.getText());
    }

    /**
     * 
     */
    public void onChanged() {

    }

    /*
     * (non-Javadoc)
     * 
     * @see javax.swing.event.DocumentListener#removeUpdate(javax.swing.event.
     * DocumentEvent)
     */
    @Override
    public void removeUpdate(final DocumentEvent e) {
        onChanged();
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
            setText(this.helpText);
            setForeground(helpColor);
        }

    }

    /**
     * if label mode is enabled, the textfield will act like a MUltiline jlabel
     * 
     * @param b
     */
    public void setLabelMode(final boolean b) {
        setEditable(!b);
        setFocusable(!b);
        setBorder(b ? null : new JTextArea().getBorder());
        SwingUtils.setOpaque(this, !b);
    }

    @Override
    public void setText(String t) {
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
    }

    public ExtTextArea setTooltipsEnabled(final boolean b) {
        if (b) {
            ToolTipController.getInstance().register(this);
        } else {
            ToolTipController.getInstance().unregister(this);

        }
        return this;
    }

    @Override
    public void setToolTipText(final String text) {

        putClientProperty(JComponent.TOOL_TIP_TEXT_KEY, text);

        setTooltipsEnabled(StringUtils.isNotEmpty(getToolTipText()));
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.appwork.swing.components.tooltips.ToolTipHandler#updateTooltip(org
     * .appwork.swing.components.tooltips.ExtTooltip, java.awt.event.MouseEvent)
     */
    @Override
    public int getTooltipDelay(final Point mousePositionOnScreen) {
        return 0;
    }

    @Override
    public boolean updateTooltip(final ExtTooltip activeToolTip, final MouseEvent e) {
        return false;
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.appwork.swing.components.tooltips.ToolTipHandler#createExtTooltip
     * (java.awt.Point)
     */
    @Override
    public ExtTooltip createExtTooltip(final Point mousePosition) {

        return getTooltipFactory().createTooltip();
    }

    public void setTooltipFactory(final TooltipFactory tooltipFactory) {
        this.tooltipFactory = tooltipFactory;
        ToolTipController.getInstance().register(this);
    }

    public TooltipFactory getTooltipFactory() {

        return tooltipFactory;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.appwork.swing.components.tooltips.ToolTipHandler#
     * isTooltipDisabledUntilNextRefocus()
     */
    @Override
    public boolean isTooltipDisabledUntilNextRefocus() {

        return true;
    }

    @Override
    public boolean isTooltipWithoutFocusEnabled() {
        // TODO Auto-generated method stub
        return true;
    }

}
