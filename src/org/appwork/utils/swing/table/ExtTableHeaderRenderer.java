/**
 * Copyright (c) 2009 - 2011 AppWork UG(haftungsbeschränkt) <e-mail@appwork.org>
 * 
 * This file is part of org.appwork.utils.swing.table
 * 
 * This software is licensed under the Artistic License 2.0,
 * see the LICENSE file or http://www.opensource.org/licenses/artistic-license-2.0.php
 * for details
 */
package org.appwork.utils.swing.table;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Polygon;
import java.awt.RenderingHints;

import javax.swing.BorderFactory;
import javax.swing.JTable;
import javax.swing.border.Border;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.JTableHeader;

import sun.swing.DefaultLookup;

/**
 * @author thomas
 * 
 */
public class ExtTableHeaderRenderer extends DefaultTableCellRenderer implements javax.swing.plaf.UIResource {

    /**
     * 
     */
    private static final long  serialVersionUID = 1L;
    private final ExtColumn<?> column;
    private boolean            paintIcon;
    private boolean            order;
    private final Border       defaultBorder;

    /**
     * @param extColumn
     */
    public ExtTableHeaderRenderer(final ExtColumn<?> extColumn) {
        this.column = extColumn;
        // this.setHorizontalTextPosition(10);
        this.defaultBorder = BorderFactory.createEmptyBorder(0, 10, 0, 10);
    }

    @Override
    public Component getTableCellRendererComponent(final JTable table, final Object value, final boolean isSelected, final boolean hasFocus, final int row, final int column) {
        if (table != null) {
            final JTableHeader header = table.getTableHeader();
            Color foreground;
            Color background = null;
            if (header != null) {
                foreground = null;

                if (hasFocus) {
                    foreground = DefaultLookup.getColor(this, this.ui, "TableHeader.focusCellForeground");
                    background = DefaultLookup.getColor(this, this.ui, "TableHeader.focusCellBackground");
                }
                if (foreground == null) {
                    foreground = header.getForeground();
                }
                if (background == null) {
                    background = header.getBackground();
                }
                this.setForeground(foreground);
                this.setBackground(background);
                this.setFont(header.getFont());

            }
        }

        // sort column is no current column
        if (this.column.getModel().getSortColumn() != this.column) {
            this.paintIcon = false;
        } else {
            this.paintIcon = true;
        }
        this.order = this.column.getModel().isSortOrderToggle();
        this.setText(value == null ? "" : value.toString());

        Border border = null;
        if (hasFocus) {
            border = DefaultLookup.getBorder(this, this.ui, "TableHeader.focusCellBorder");
        }
        if (border == null) {
            border = DefaultLookup.getBorder(this, this.ui, "TableHeader.cellBorder");
        }
        if (border == null) {
            border = this.defaultBorder;
        }
        this.setBorder(border);
        return this;
    }

    @Override
    public void paintComponent(final Graphics g) {
        super.paintComponent(g);
        if (this.paintIcon) {
            if (this.order) {
                final Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setStroke(new BasicStroke(1));

                g2.setColor(Color.black);
                final Polygon poly = new Polygon();

                poly.addPoint(this.getSize().width / 2, 2);
                poly.addPoint(this.getSize().width / 2 + 4, 4);
                poly.addPoint(this.getSize().width / 2 - 4, 4);

                g2.fill(poly);
            } else {
                final Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setStroke(new BasicStroke(1));

                g2.setColor(Color.black);
                final Polygon poly = new Polygon();

                poly.addPoint(this.getSize().width / 2, this.getSize().height - 2);
                poly.addPoint(this.getSize().width / 2 + 4, this.getSize().height - 4);
                poly.addPoint(this.getSize().width / 2 - 4, this.getSize().height - 4);

                g2.fill(poly);
            }
        }

    }

}