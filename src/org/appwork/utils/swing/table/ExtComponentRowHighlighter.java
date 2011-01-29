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
import java.util.HashMap;

import javax.swing.JComponent;
import javax.swing.border.Border;

/**
 * @author thomas Highlighterclass which can be added to ExtTableModel.
 *         columnrenderers will set the for and background of their component
 *         according to this highlighter
 * @see ExtRowHighlighter
 */
public abstract class ExtComponentRowHighlighter<E> {
    private class Restore {
        private final Color   background;
        private final Color   foreground;
        private final Border  border;
        private final boolean opaque;

        /**
         * @param background
         * @param foreground
         * @param border
         * @param opaque
         */
        public Restore(final Color background, final Color foreground, final Border border, final boolean opaque) {
            this.background = background;
            this.foreground = foreground;
            this.border = border;
            this.opaque = opaque;
        }

    }

    private Color                              foreground;

    private Color                              background;

    private Border                             border;

    private final HashMap<JComponent, Restore> map = new HashMap<JComponent, Restore>();

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
        final Restore restore = this.saveRestoreInfo(comp);
        if (this.accept(column, value, selected, focus, row)) {

            if (this.background != null) {

                comp.setBackground(this.background);

                comp.setOpaque(true);
            }
            if (this.foreground != null) {
                comp.setForeground(this.foreground);
            }
            if (this.border != null) {
                comp.setBorder(this.border);
            }
            return true;
        } else {

            comp.setBackground(restore.background);

            comp.setForeground(restore.foreground);

            comp.setBorder(restore.border);

            comp.setOpaque(restore.opaque);
        }
        return false;
    }

    /**
     * @param comp
     */
    private Restore saveRestoreInfo(final JComponent comp) {
        Restore ret = this.map.get(comp);
        if (ret == null) {
            ret = new Restore(comp.getBackground(), comp.getForeground(), comp.getBorder(), comp.isOpaque());
            this.map.put(comp, ret);
        }
        return ret;

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
