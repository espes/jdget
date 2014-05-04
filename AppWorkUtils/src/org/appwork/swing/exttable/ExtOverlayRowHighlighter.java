package org.appwork.swing.exttable;

import java.awt.Color;
import java.awt.Graphics2D;

public abstract class ExtOverlayRowHighlighter {

    private Color borderColor;

    private Color contentColor;

    public ExtOverlayRowHighlighter(final Color borderColor, final Color contentColor) {
        this.borderColor = borderColor;
        this.contentColor = contentColor;
    }

    abstract public boolean doHighlight(ExtTable<?> extTable, int row);

    /**
     * @return the {@link ExtOverlayRowHighlighter#borderColor}
     * @see ExtOverlayRowHighlighter#borderColor
     */
    public Color getBorderColor() {
        return this.borderColor;
    }

    /**
     * @return the {@link ExtOverlayRowHighlighter#contentColor}
     * @see ExtOverlayRowHighlighter#contentColor
     */
    public Color getContentColor() {
        return this.contentColor;
    }

    /**
     * Overwrite this method for custom highlighters
     * 
     * @param g
     * @param x
     * @param y
     * @param width
     * @param height
     */
    public void paint(final Graphics2D g, final int x, final int y, final int width, final int height) {
        if (this.getBorderColor() != null) {
            g.setColor(this.getBorderColor());
            g.drawRect(0, y, width, height);

        }

        if (this.getContentColor() != null) {
            g.setColor(this.getContentColor());
            g.fillRect(0, y, width, height);
        }
    }

    /**
     * @param borderColor
     *            the {@link ExtOverlayRowHighlighter#borderColor} to set
     * @see ExtOverlayRowHighlighter#borderColor
     */
    public void setBorderColor(final Color borderColor) {
        this.borderColor = borderColor;
    }

    /**
     * @param contentColor
     *            the {@link ExtOverlayRowHighlighter#contentColor} to set
     * @see ExtOverlayRowHighlighter#contentColor
     */
    public void setContentColor(final Color contentColor) {
        this.contentColor = contentColor;
    }
}
