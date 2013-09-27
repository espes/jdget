/**
 * Copyright (c) 2009 - 2011 AppWork UG(haftungsbeschränkt) <e-mail@appwork.org>
 * 
 * This file is part of org.appwork.utils.swing.table
 * 
 * This software is licensed under the Artistic License 2.0,
 * see the LICENSE file or http://www.opensource.org/licenses/artistic-license-2.0.php
 * for details
 */
package org.appwork.swing.exttable;

import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.Graphics2D;

import javax.swing.BorderFactory;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JTable;
import javax.swing.UIManager;
import javax.swing.border.Border;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.JTableHeader;

import org.appwork.resources.AWUTheme;
import org.appwork.utils.logging.Log;

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

    private Color              focusForeground;
    private Color              focusBackground;
    private  Color        foregroundC;
    public Color getFocusForeground() {
        return focusForeground;
    }

    public void setFocusForeground(final Color focusForeground) {
        this.focusForeground = focusForeground;
    }

    public Color getForegroundC() {
        return foregroundC;
    }

    public void setForegroundC(final Color foregroundC) {
        this.foregroundC = foregroundC;
    }

    private  Color        backgroundC;
    private Border             focusBorder;
    public Color getFocusBackground() {
        return focusBackground;
    }

    public void setFocusBackground(final Color focusBackground) {
        this.focusBackground = focusBackground;
    }

    public Color getBackgroundC() {
        return backgroundC;
    }

    public void setBackgroundC(final Color backgroundC) {
        this.backgroundC = backgroundC;
    }

    private Border             cellBorder;
    private final ImageIcon    lockedWidth;

    /**
     * @param extColumn
     * @param jTableHeader
     */
    public ExtTableHeaderRenderer(final ExtColumn<?> extColumn, final JTableHeader header) {
        column = extColumn;
        // this.setHorizontalTextPosition(10);
        lockedWidth = AWUTheme.I().getIcon("exttable/widthLocked", -1);
        try {

            try {
                focusForeground = DefaultLookup.getColor(this, ui, "TableHeader.focusCellForeground");
                focusBackground = DefaultLookup.getColor(this, ui, "TableHeader.focusCellBackground");
            } catch (final NoSuchMethodError e) {
                // DefaultLookup is sun.swing, any may not be
                // available
                // e.gh. in 1.6.0_01-b06
                focusForeground = (Color) UIManager.get("TableHeader.focusCellForeground", getLocale());
                focusBackground = (Color) UIManager.get("TableHeader.focusCellBackground", getLocale());

            }

        } catch (final Throwable e) {
            Log.exception(e);
        }
        if (focusForeground == null) {
            focusForeground = header.getForeground();

        }
        if (focusBackground == null) {
            focusBackground = header.getBackground();
        }
        foregroundC = header.getForeground();
        backgroundC = header.getBackground();

        try {
            try {

                focusBorder = DefaultLookup.getBorder(this, ui, "TableHeader.focusCellBorder");

                cellBorder = DefaultLookup.getBorder(this, ui, "TableHeader.cellBorder");

            } catch (final NoSuchMethodError e) {
                // DefaultLookup is sun.swing, any may not be available
                // e.gh. in 1.6.0_01-b06

                focusBorder = (Border) UIManager.get("TableHeader.focusCellBorder", getLocale());

                cellBorder = (Border) UIManager.get("TableHeader.focusCellBackground", getLocale());

            }
        } catch (final Throwable e) {
            Log.exception(e);
            // avoid that the block above kills edt
        }
        if (focusBorder == null) {
            focusBorder = BorderFactory.createEmptyBorder(0, 10, 0, 10);
        }
        if (cellBorder == null) {
            cellBorder = BorderFactory.createEmptyBorder(0, 10, 0, 10);
        }
        setFont(header.getFont());

    }

    @Override
    public Component getTableCellRendererComponent(final JTable table, final Object value, final boolean isSelected, final boolean hasFocus, final int row, final int column) {
        setForeground(hasFocus ? focusForeground : foregroundC);
        setBackground(hasFocus ? focusBackground : backgroundC);
        // sort column is no current column
        if (this.column.getModel().getSortColumn() == null || this.column.getModel().getSortColumn() != this.column) {
            paintIcon = false;

        } else {
            paintIcon = true;

        }

        setText(value == null ? "" : value.toString());
        setBorder(hasFocus ? focusBorder : cellBorder);


        // this.setBackground(Color.RED);
        // this.setOpaque(true);
        // System.out.println(this.getPreferredSize());
        return this;
    }

    @Override
    public void paintComponent(final Graphics g) {
        boolean paintLock = false;
        if (!column.isResizable() && column.isPaintWidthLockIcon()) {
            paintLock = true;
        }
        final Border orgBorder = getBorder();
        final int widthDif = column.getWidth() - getPreferredSize().width;
        final boolean smallLockIcon = widthDif < lockedWidth.getIconWidth();
      
        try {
            if (paintLock && !smallLockIcon) {
                setBorder(BorderFactory.createCompoundBorder(BorderFactory.createEmptyBorder(0, 0, 0, lockedWidth.getIconWidth()), orgBorder));
            }
            super.paintComponent(g);
        } finally {
            setBorder(orgBorder);
        }
        try {
            if (paintIcon) {
                final int left = 2;
                final Icon icon = column.getModel().getSortColumn().getSortIcon();
                if (icon != null) {
                    final Graphics2D g2 = (Graphics2D) g;
                    // final Composite comp = g2.getComposite();
                    // g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER,
                    // 0.5f));
                    icon.paintIcon(this, g2, left, (getHeight() - icon.getIconHeight()) / 2);
                    // g2.setComposite(comp);
                }
            }

            if (paintLock) {

                // lockedWidth

                final Graphics2D g2 = (Graphics2D) g;
                // final Composite comp = g2.getComposite();
                // g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER,
                // 0.5f));
                if (smallLockIcon) {
                    g2.setColor(getBackground().darker());
//                    g2.setColor(Color.RED);
                    final int size = 6;
                    g2.fillPolygon(new int[] { getWidth(), getWidth()-size, getWidth() , getWidth() }, new int[] { getHeight(),getHeight(),getHeight()-size, getHeight() }, 4);
                } else {
                    lockedWidth.paintIcon(this, g2, getWidth() - lockedWidth.getIconWidth() - 2, (getHeight() - lockedWidth.getIconHeight()) / 2);
                }
                // g2.setComposite(comp);
            }
        } catch (final RuntimeException e) {
            Log.exception(e);
        }

    }

}