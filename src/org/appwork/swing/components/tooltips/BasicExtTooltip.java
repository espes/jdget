/**
 * Copyright (c) 2009 - 2011 AppWork UG(haftungsbeschränkt) <e-mail@appwork.org>
 * 
 * This file is part of org.appwork.swing.components.tooltips
 * 
 * This software is licensed under the Artistic License 2.0,
 * see the LICENSE file or http://www.opensource.org/licenses/artistic-license-2.0.php
 * for details
 */
package org.appwork.swing.components.tooltips;

import java.awt.Color;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.swing.JComponent;
import javax.swing.JTextArea;

import org.appwork.utils.swing.SwingUtils;

/**
 * @author thomas
 * 
 */
public class BasicExtTooltip extends ExtTooltip implements PropertyChangeListener {

    private JTextArea        tf;
    private final JComponent component;

    /**
     * @param circledProgressBar
     */
    public BasicExtTooltip(final JComponent circledProgressBar) {
        super();
        this.component = circledProgressBar;
        System.out.println(this.component.getToolTipText());
        this.tf.setText(this.component.getToolTipText());

        this.component.addPropertyChangeListener(JComponent.TOOL_TIP_TEXT_KEY, this);

    }

    /*
     * (non-Javadoc)
     * 
     * @see org.appwork.swing.components.tooltips.ExtTooltip#createContent()
     */
    @Override
    public TooltipPanel createContent() {
        final TooltipPanel p = new TooltipPanel("ins 5,wrap 1", "[]", "[]");
        this.tf = new JTextArea();
        // this.tf.setEnabled(false);
        this.tf.setForeground(Color.WHITE);
        this.tf.setBackground(null);
        this.tf.setEditable(false);
        SwingUtils.setOpaque(this.tf, false);

        p.add(this.tf);
        return p;
    }

    @Override
    public void dispose() {
        this.component.removePropertyChangeListener(JComponent.TOOL_TIP_TEXT_KEY, this);
        super.dispose();

    }

    /*
     * (non-Javadoc)
     * 
     * @see java.beans.PropertyChangeListener#propertyChange(java.beans.
     * PropertyChangeEvent)
     */
    @Override
    public void propertyChange(final PropertyChangeEvent evt) {
        if (this.component.getToolTipText() == null || this.component.getToolTipText().length() == 0) {
            this.dispose();
        }
        this.tf.setText(this.component.getToolTipText());
        System.out.println(this.component.getToolTipText());
        this.pack();

    }

}
