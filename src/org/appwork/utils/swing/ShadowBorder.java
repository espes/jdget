package org.appwork.utils.swing;

import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.Insets;

import javax.swing.border.AbstractBorder;

public class ShadowBorder extends AbstractBorder {

    /**
     * 
     */
    private static final long serialVersionUID = 1L;

    private int               shadowWidth      = 3;

    private Color             color            = Color.GRAY;

    public ShadowBorder() {
        this(3);
    }

    public ShadowBorder(final int width) {
        this(width, Color.GRAY);
    }

    public ShadowBorder(final int width, final Color color) {
        this.shadowWidth = width;
        this.color = color;

    }

    @Override
    public Insets getBorderInsets(final Component c) {
        return new Insets(0, 0, this.shadowWidth + 1, this.shadowWidth + 1);
    }

    @Override
    public Insets getBorderInsets(final Component c, final Insets insets) {
        insets.top = 1;
        insets.left = 1;
        insets.bottom = this.shadowWidth + 1;
        insets.right = this.shadowWidth + 1;
        return insets;
    }

    @Override
    public boolean isBorderOpaque() {
        return true;
    }

    @Override
    public void paintBorder(final Component c, final Graphics g, final int x, final int y, final int width, final int height) {
        final Color oldColor = g.getColor();
        int x1, y1, x2, y2;
        g.setColor(this.color);

        g.drawRect(x, y, width - this.shadowWidth - 1, height - this.shadowWidth - 1);
        final int alphaSteps = this.color.getAlpha() / (this.shadowWidth + 1);

        for (int i = 0; i <= this.shadowWidth; i++) {
            // bottom shadow
            g.setColor(new Color(g.getColor().getRed(), g.getColor().getGreen(), g.getColor().getBlue(), g.getColor().getAlpha() - alphaSteps));
            x1 = x + i + this.shadowWidth;
            y1 = y + height - this.shadowWidth + i;
            x2 = x + width + i - this.shadowWidth;
            y2 = y1;
            g.drawLine(x1, y1, x2, y2);

            // right shadow
            x1 = x + width - this.shadowWidth + i;
            y1 = y + i + this.shadowWidth;
            x2 = x1;
            y2 = y + height + i - this.shadowWidth - 1;
            g.drawLine(x1, y1, x2, y2);

        }

        g.setColor(oldColor);
    }
}
