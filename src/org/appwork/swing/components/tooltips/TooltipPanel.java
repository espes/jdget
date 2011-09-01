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
import java.awt.Insets;
import java.awt.Point;
import java.awt.Rectangle;

import javax.swing.JComponent;
import javax.swing.RepaintManager;
import javax.swing.SwingUtilities;

import org.appwork.app.gui.MigPanel;

import com.sun.java.swing.SwingUtilities3;

/**
 * @author thomas
 * 
 */
public class TooltipPanel extends MigPanel {

    class DelegateRepaintManager extends RepaintManager {

        /**
         * @param c
         */
        public DelegateRepaintManager() {
            super();

        }

        @Override
        public void addDirtyRegion(final JComponent c, final int x, final int y, final int w, final int h) {
            Rectangle rec;
            SwingUtilities.convertRectangle(c, rec = new Rectangle(w, h), TooltipPanel.this);
            final Point p = SwingUtilities.convertPoint(c, x, y, TooltipPanel.this.getParent());
            // Tooltip border insets
            final Insets insets = TooltipPanel.this.getParent().getInsets();
            TooltipPanel.this.repaint();
            TooltipPanel.this.getParent().repaint();
            System.out.println(c.getName());
        }
    }

    /**
     * @param string
     * @param string2
     * @param string3
     */
    public TooltipPanel(final String constraints, final String columns, final String rows) {
        super(constraints, columns, rows);

        this.setBackground(null);
        this.setOpaque(false);

    }

    @Override
    protected void addImpl(final Component comp, final Object constraints, final int index) {
        super.addImpl(comp, constraints, index);

        // if (comp instanceof JComponent) {
        // this.delegateRepaintManager((JComponent) comp);
        // }

    }

    /**
     * 
     * This Tooltip panel will not be added and layouted for real in the
     * tooltip. We just paint it in the tooltip container. Thus the repaint
     * event queue is broken, and we have to delegate repaint requests to the
     * underlaying Tooltip parent
     * 
     * @param comp
     */
    private void delegateRepaintManager(final JComponent comp) {

        final RepaintManager old = RepaintManager.currentManager(comp);

        final DelegateRepaintManager rp = (DelegateRepaintManager) (old instanceof DelegateRepaintManager ? old : new DelegateRepaintManager());
        SwingUtilities3.setDelegateRepaintManager(comp, rp);

        for (final Component c : comp.getComponents()) {
            if (c instanceof JComponent) {
                this.delegateRepaintManager((JComponent) c);
            }
        }

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
