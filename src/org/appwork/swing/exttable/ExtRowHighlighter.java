package org.appwork.swing.exttable;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.Rectangle2D;

public abstract class ExtRowHighlighter {

    private Color borderColor;

    private Color contentColor;

    public ExtRowHighlighter(final Color borderColor, final Color contentColor) {
        this.borderColor = borderColor;
        this.contentColor = contentColor;
    }

    abstract public boolean doHighlight(ExtTable<?> extTable, int row);

    /**
     * @return the {@link ExtRowHighlighter#borderColor}
     * @see ExtRowHighlighter#borderColor
     */
    public Color getBorderColor() {
        return this.borderColor;
    }

    /**
     * @return the {@link ExtRowHighlighter#contentColor}
     * @see ExtRowHighlighter#contentColor
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
            g.draw(new Rectangle2D.Float(0, y, width, height));
        }
        if (this.getContentColor() != null) {
            g.setColor(this.getContentColor());
            g.fill(new Rectangle2D.Float(0, y, width, height));
        }
    }

    /**
     * @param borderColor
     *            the {@link ExtRowHighlighter#borderColor} to set
     * @see ExtRowHighlighter#borderColor
     */
    public void setBorderColor(final Color borderColor) {
        this.borderColor = borderColor;
    }

    /**
     * @param contentColor
     *            the {@link ExtRowHighlighter#contentColor} to set
     * @see ExtRowHighlighter#contentColor
     */
    public void setContentColor(final Color contentColor) {
        this.contentColor = contentColor;
    }
}
