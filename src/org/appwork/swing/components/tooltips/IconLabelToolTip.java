package org.appwork.swing.components.tooltips;

import java.awt.Color;

import javax.swing.ImageIcon;
import javax.swing.JLabel;

import org.appwork.utils.swing.SwingUtils;

public class IconLabelToolTip extends ExtTooltip {

    private String    name;

    private ImageIcon icon;

    private JLabel    label;

    /**
     * @param host
     * @param hosterIcon
     */
    public IconLabelToolTip(final String name, final ImageIcon icon) {
        this.name = name;
        this.icon = icon;
        this.label.setText(name);
        this.label.setIcon(icon);
    }

    @Override
    public TooltipPanel createContent() {
        final TooltipPanel ret = new TooltipPanel("ins 0", "[grow,fill]", "[]");
        this.label = new JLabel();
        SwingUtils.setOpaque(this.label, false);
        this.label.setForeground(new Color(this.getConfig().getForegroundColor()));
        ret.add(this.label);
        return ret;
    }

    public ImageIcon getIcon() {
        return this.icon;
    }

    @Override
    public String getName() {
        return this.name;
    }

    public void setIcon(final ImageIcon icon) {
        this.icon = icon;

        this.label.setIcon(icon);
        this.repaint();
    }

    @Override
    public void setName(final String name) {
        this.name = name;
        this.label.setText(name);
        this.repaint();
    }
}
