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

import java.awt.Color;

import javax.swing.JComponent;
import javax.swing.border.Border;
import javax.swing.text.JTextComponent;

/**
 * @author thomas Highlighterclass which can be added to ExtTableModel.
 *         columnrenderers will set the for and background of their component
 *         according to this highlighter
 * @see ExtRowHighlighter
 */
public abstract class ExtComponentRowHighlighter<E> {

    private Color  foreground;

    private Color  background;

    private Border border;

    public ExtComponentRowHighlighter(final Color foreground, final Color background, final Border border) {
        super();
        this.foreground = foreground;
        this.background = background;
        this.border = border;
    }

    /**
     * @param column
     * @param value
     * @param selected
     * @param focus
     * @param row
     * @return
     */
    public abstract boolean accept(ExtColumn<E> column, E value, boolean selected, boolean focus, int row);

    /**
     * @return the background
     */
    public Color getBackground() {
        return this.background;
    }

    /**
     * @return the border
     */
    public Border getBorder() {
        return this.border;
    }

    /**
     * @return the foreground
     */
    public Color getForeground() {
        return this.foreground;
    }

    public boolean highlight(final ExtColumn<E> column, final JComponent comp, final E value, final boolean selected, final boolean focus, final int row) {

        if (this.accept(column, value, selected, focus, row)) {

            if (this.background != null) {

                comp.setBackground(this.background);

                comp.setOpaque(true);
                // important for synthetica textcomponents
                if (comp instanceof JTextComponent) {
                    comp.putClientProperty("Synthetica.opaque", Boolean.FALSE);
                }
            }
            if (this.foreground != null) {
                comp.setForeground(this.foreground);
            }
            if (this.border != null) {
                comp.setBorder(this.border);
            }
            return true;
        }
        return false;
    }

    /**
     * @param background
     *            the background to set
     */
    public void setBackground(final Color background) {
        this.background = background;
    }

    /**
     * @param border
     *            the border to set
     */
    public void setBorder(final Border border) {
        this.border = border;
    }

    /**
     * @param foreground
     *            the foreground to set
     */
    public void setForeground(final Color foreground) {
        this.foreground = foreground;
    }
}
