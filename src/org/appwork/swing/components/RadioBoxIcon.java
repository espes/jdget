package org.appwork.swing.components;

import java.awt.Component;
import java.awt.Graphics;

import javax.swing.Icon;
import javax.swing.JRadioButton;

import org.appwork.utils.ImageProvider.ImageProvider;

public final class RadioBoxIcon implements Icon {
    public static final RadioBoxIcon FALSE     = new RadioBoxIcon(false);
    public static final RadioBoxIcon TRUE      = new RadioBoxIcon(true);
    public static final RadioBoxIcon UNDEFINED = new RadioBoxIcon(true, false);
    private JRadioButton                cb;
    private Icon                     internalIcon;
    private int                      size;

    public RadioBoxIcon(final boolean selected, final boolean enabled) {

        cb = new JRadioButton() {
            {
                setSelected(true);
            }

            @Override
            public int getWidth() {
                return getPreferredSize().width;
            }

            @Override
            public int getHeight() {
                return getPreferredSize().height;
            }

            @Override
            public boolean isVisible() {
                return true;
            }
        };
    
        ;
        cb.setSelected(selected);
        // we need this workaround.
        // if we would use cb.paint(g); for every paintIcon call, this might habe sideeffects on the LAF painter.
        size = 14;

        internalIcon = ImageProvider.toImageIcon(this);

        if (!enabled) {
            internalIcon = ImageProvider.getDisabledIcon(internalIcon);
        }

    }

    public RadioBoxIcon(final boolean selected) {
        this(selected, true);
    }

    @Override
    public void paintIcon(final Component c, Graphics g, final int x, final int y) {
        if (internalIcon != null) {
            internalIcon.paintIcon(c, g, x, y);

            return;
        }
        // g.setColor(Color.RED);
        // g.drawRect(0, 0, 14, 14);
        g = g.create(x, y, 14, 14);
        // g.translate(x, y);
        g.translate(-4, -4);
        cb.paint(g);
        g.dispose();
        // g.translate(4, 4);
        // g.translate(-x, -y);

        // g.dispose();
        // g.translate(0, -10);

    }

    @Override
    public int getIconWidth() {
        return size;
    }

    @Override
    public int getIconHeight() {
        return size;
    }
}