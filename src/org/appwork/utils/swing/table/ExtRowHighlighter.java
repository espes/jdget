package org.appwork.utils.swing.table;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.Rectangle2D;

public abstract class ExtRowHighlighter {
    private Color borderColor;

    /**
     * @return the {@link ExtRowHighlighter#borderColor}
     * @see ExtRowHighlighter#borderColor
     */
    public Color getBorderColor() {
        return borderColor;
    }

    /**
     * @param borderColor
     *            the {@link ExtRowHighlighter#borderColor} to set
     * @see ExtRowHighlighter#borderColor
     */
    public void setBorderColor(Color borderColor) {
        this.borderColor = borderColor;
    }

    /**
     * @return the {@link ExtRowHighlighter#contentColor}
     * @see ExtRowHighlighter#contentColor
     */
    public Color getContentColor() {
        return contentColor;
    }

    /**
     * @param contentColor
     *            the {@link ExtRowHighlighter#contentColor} to set
     * @see ExtRowHighlighter#contentColor
     */
    public void setContentColor(Color contentColor) {
        this.contentColor = contentColor;
    }

    private Color contentColor;

    public ExtRowHighlighter(Color borderColor, Color contentColor) {

        this.borderColor = borderColor;
        this.contentColor = contentColor;
    }

    abstract public boolean doHighlight(ExtTable extTable, int row);

    /**
     * Overwrite this method for custom highlighters
     * 
     * @param g
     * @param i
     * @param y
     * @param width
     * @param height
     */
    public void paint(Graphics2D g, int x, int y, int width, int height) {

        if (getBorderColor() != null) {
            g.setColor(getBorderColor());

            g.draw(new Rectangle2D.Float(0, y, width, height));
        }
        if (getContentColor() != null) {
            g.setColor(getContentColor());
            g.fill(new Rectangle2D.Float(0, y, width, height));
        }
    }
}
