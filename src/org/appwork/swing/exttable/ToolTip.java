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

import javax.swing.JTextPane;

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
    private JTextPane         tf;

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
        this.tf = new JTextPane();
        // this.tf.setEnabled(false);
        this.tf.setForeground(new Color(this.getConfig().getForegroundColor()));
        this.tf.setBackground(null);
        this.tf.setEditable(false);
     
        SwingUtils.setOpaque(this.tf, false);

        p.add(this.tf);
        return p;
    }

    /**
     * @param txt
     */

    public void setTipText(final String txt) {
        System.out.println("Set "+txt);
        if (txt.startsWith("<html>")) {
            tf.setContentType("text/html");
        } else {
            tf.setContentType("text/plain");
        }
        this.tf.setText(txt);
     
    
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
