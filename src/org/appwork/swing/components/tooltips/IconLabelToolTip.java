package org.appwork.swing.components.tooltips;

import java.awt.Color;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JLabel;

import org.appwork.utils.swing.SwingUtils;

public class IconLabelToolTip extends ExtTooltip {

    /**
     * 
     */
    private static final long serialVersionUID = 1437567673004968332L;

    private String    name;

    private Icon icon;

    protected JLabel    label;

    /**
     * @param host
     * @param hosterIcon
     */
    public IconLabelToolTip(final String name, final Icon icon2) {
        this.name = name;
        icon = icon2;
        label.setText(name);
        label.setIcon(icon2);
    }

    @Override
    public TooltipPanel createContent() {
        final TooltipPanel ret = new TooltipPanel("ins 0", "[grow,fill]", "[]");
        label = new JLabel();
        SwingUtils.setOpaque(label, false);
        label.setForeground(new Color(getConfig().getForegroundColor()));
        ret.add(label);
        return ret;
    }

    public Icon getIcon() {
        return icon;
    }

    @Override
    public String getName() {
        return name;
    }

    public void setIcon(final ImageIcon icon) {
        this.icon = icon;

        label.setIcon(icon);
        this.repaint();
    }

    @Override
    public void setName(final String name) {
        this.name = name;
        label.setText(name);
        this.repaint();
    }

    /* (non-Javadoc)
     * @see org.appwork.swing.components.tooltips.ExtTooltip#toText()
     */
    @Override
    public String toText() {
        // TODO Auto-generated method stub
        return label.getText();
    }
}
