package org.appwork.utils.swing.table;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Polygon;
import java.awt.RenderingHints;

import javax.swing.JLabel;
import javax.swing.JTable;

import sun.swing.table.DefaultTableCellHeaderRenderer;

public class ExtTableCellHeaderRenderer extends DefaultTableCellHeaderRenderer {

    private static final long serialVersionUID = 1984070302981234250L;

    private ExtColumn<?> column;

    private boolean order;
    private boolean paintIcon;

    public ExtTableCellHeaderRenderer(ExtColumn<?> extColumn) {
        this.column = extColumn;
        ((JLabel) this).setHorizontalAlignment(JLabel.LEFT);

    }

    @Override
    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int col) {
        // sort column is no current column
        if (column.getModel().getSortColumn() != column) {
            this.paintIcon = false;
        } else {
            this.paintIcon = true;
        }
        order = column.getModel().isSortOrderToggle();
        // empty borders do not work here. to have a left side border, we add
        // spaces here
        return super.getTableCellRendererComponent(table, "   " + value, isSelected, hasFocus, row, col);

    }

    @Override
    public void paintComponent(final Graphics g) {

        super.paintComponent(g);
        if (paintIcon) {
            if (order) {
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setStroke(new BasicStroke(1));

                g2.setColor(Color.black);
                Polygon poly = new Polygon();

                poly.addPoint(getSize().width / 2, 2);
                poly.addPoint(getSize().width / 2 + 4, 4);
                poly.addPoint(getSize().width / 2 - 4, 4);

                g2.fill(poly);
            } else {
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setStroke(new BasicStroke(1));

                g2.setColor(Color.black);
                Polygon poly = new Polygon();

                poly.addPoint(getSize().width / 2, getSize().height - 2);
                poly.addPoint(getSize().width / 2 + 4, getSize().height - 4);
                poly.addPoint(getSize().width / 2 - 4, getSize().height - 4);

                g2.fill(poly);
            }
        }

    }
}
