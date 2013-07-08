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
import java.util.HashMap;

import javax.swing.JComponent;
import javax.swing.border.Border;
import javax.swing.text.JTextComponent;

/**
 * @author thomas Highlighterclass which can be added to ExtTableModel.
 *         columnrenderers will set the for and background of their component
 *         according to this highlighter
 * @see ExtOverlayRowHighlighter
 */
public abstract class ExtComponentRowHighlighter<E> {

    protected Color  foreground;

    protected Color  background;

    protected Border border;

    private boolean  bgMixEnabled;

    private boolean  fgMixEnabled;

    public static Color mixColors(final Color ca, final Color cb) {

        int r, g, b;
        final int aa = ca.getAlpha();
        final int ba = cb.getAlpha();
        r = (ca.getRed() * aa + cb.getRed() * ba) / (aa + ba);
        g = (ca.getGreen() * aa + cb.getGreen() * ba) / (aa + ba);
        b = (ca.getBlue() * aa + cb.getBlue() * ba) / (aa + ba);

        return new Color(r, g, b);

    }

    public ExtComponentRowHighlighter(final Color foreground, final Color background, final Border border) {
        super();
        this.foreground = foreground;
        this.background = background;

        bgMixEnabled = background != null && background.getAlpha() < 255;
        fgMixEnabled = foreground != null && foreground.getAlpha() < 255;
        this.border = border;
    }

    HashMap<String, Color> cache = new HashMap<String, Color>();

    /**
     * @param foreground2
     * @return
     */
    protected Color getForeground(final Color current) {
        if (!fgMixEnabled) {
            return foreground;
        }
        if (current == null) {

        return null; }
        final String id = current + "_" + foreground;

        Color cached = cache.get(id);
        if (cached != null) { return cached; }

        cached = mixColors(current, foreground);
        cache.put(id, cached);
        return cached;
    }

    /**
     * @param background2
     * @return
     */
    protected Color getBackground(final Color current) {
        if (!bgMixEnabled) {
            return background;
        }
        if (current == null) {

        return null; }
        final String id = current + "_" + background;

        Color cached = cache.get(id);
        if (cached != null) { return cached; }

        cached = mixColors(current, background);
        cache.put(id, cached);
        return cached;
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
                final Color bg = getBackground(comp.getBackground());
                comp.setBackground(bg);
                comp.setOpaque(true);
                // important for synthetica textcomponents
                if (comp instanceof JTextComponent) {
                    comp.putClientProperty("Synthetica.opaque", Boolean.FALSE);
                }
            }
            if (this.foreground != null) {
                comp.setForeground(getForeground(comp.getForeground()));
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

    /**
     * the higher the priority the later the highlighter will be painted
     * 
     * @return
     */
    public int getPriority() {
        return 0;
    }
}
