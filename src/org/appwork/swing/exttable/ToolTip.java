/**
 * Copyright (c) 2009 - 2011 AppWork UG(haftungsbeschränkt) <e-mail@appwork.org>
 * 
 * This file is part of org.appwork.swing.exttable
 * 
 * This software is licensed under the Artistic License 2.0,
 * see the LICENSE file or http://www.opensource.org/licenses/artistic-license-2.0.php
 * for details
 */
package org.appwork.swing.exttable;

import java.awt.Color;

import javax.swing.JLabel;

import org.appwork.swing.components.tooltips.ExtTooltip;
import org.appwork.swing.components.tooltips.TooltipPanel;
import org.appwork.utils.swing.SwingUtils;

/**
 * @author thomas
 * 
 */
public class ToolTip extends ExtTooltip {

    /**
     * 
     */
    private static final long serialVersionUID = -7756738003708525595L;
    private JLabel            tf;

    public ToolTip() {
        super();
    }

    @Override
    public void onShow() {
        // TODO Auto-generated method stub
        super.onShow();

    }

    /*
     * (non-Javadoc)
     * 
     * @see org.appwork.swing.components.tooltips.ExtTooltip#createContent()
     */
    @Override
    public TooltipPanel createContent() {
        final TooltipPanel p = new TooltipPanel("ins 2,wrap 1", "[]", "[]");
        tf = new JLabel();
        // this.tf.setEnabled(false);
        tf.setForeground(new Color(getConfig().getForegroundColor()));
        tf.setBackground(null);

        SwingUtils.setOpaque(tf, false);

        p.add(tf);

        return p;
    }

    /**
     * @param txt
     */

    public void setTipText(String txt) {
        if (txt != null) {
            if (txt.contains("\r") || txt.contains("\n") && !txt.startsWith("<html>")) {
                txt = "<html>" + txt.replaceAll("[\r\n]{1,2}", "<br>") + "</html>";
            }
        }

        tf.setText(txt);
        panel.invalidate();

    }

    /**
     * DO NOT USE. use #toText instead
     */
    @Deprecated
    public String getTipText() {
        return null;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.appwork.swing.components.tooltips.ExtTooltip#toText()
     */
    @Override
    public String toText() {

        return tf.getText();
    }

}
