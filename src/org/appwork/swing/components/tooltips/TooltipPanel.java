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

import java.awt.Component;
import java.awt.Graphics;

import org.appwork.swing.MigPanel;

/**
 * @author thomas
 * 
 */
public class TooltipPanel extends MigPanel {

    /**
     * 
     */
    private static final long serialVersionUID = 1211919273097600217L;



    /**
     * @param string
     * @param string2
     * @param string3
     */
    public TooltipPanel(final String constraints, final String columns, final String rows) {
        super(constraints, columns, rows);

//        this.setBackground(null);
        this.setOpaque(false);

    }

    @Override
    protected void addImpl(final Component comp, final Object constraints, final int index) {
        super.addImpl(comp, constraints, index);

      

    }



    /**
     * Returns the current height of this component. This method is preferable
     * to writing <code>component.getBounds().height</code>, or
     * <code>component.getSize().height</code> because it doesn't cause any heap
     * allocations.
     * 
     * @return the current height of this component
     */
    // @Override
    // public int getHeight() {
    // return 40;
    // }
    //
    // @Override
    // public int getWidth() {
    // return 40;
    // }

    @Override
    public void paintComponents(final Graphics g) {
        super.paintComponents(g);

    }

    // @Override
    // public void repaint(final long tm, final int x, final int y, final int
    // width, final int height) {
    // // super.repaint(tm, x, y, width, height);
    // if (this.getParent() != null) {
    // this.getParent().repaint();
    // }
    // }

}
