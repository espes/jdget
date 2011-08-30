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

import java.awt.Graphics;

import javax.swing.JToolTip;

import org.appwork.app.gui.MigPanel;

/**
 * @author thomas
 * 
 */
public class TooltipPanel extends MigPanel {
    final static private JToolTip PROTOTYPE = new JToolTip();

    /**
     * @param string
     * @param string2
     * @param string3
     */
    public TooltipPanel(final String constraints, final String columns, final String rows) {
        super(constraints, columns, rows);

        this.setBackground(null);
        this.setOpaque(false);
        // this.setBorder(new JToolTip().getBorder());
        // this.setBackground(new JToolTip().getBackground());

    }

    @Override
    public void paint(final Graphics g) {

        // TooltipPanel.PROTOTYPE.setTipText("");
        TooltipPanel.PROTOTYPE.setSize(this.getWidth(), this.getHeight());
        TooltipPanel.PROTOTYPE.paint(g);
        super.paint(g);
    }

    // @Override
    // protected void paintBorder(final Graphics g) {
    // final JToolTip tip = new JToolTip();
    // // if (border != null) {
    // // tip.getBorder().paintBorder(tip, g, 0, 0, this.getWidth(),
    // // this.getHeight());
    // // }
    // }

}
