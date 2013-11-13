package org.appwork.swing.components.tooltips;

import java.awt.Color;
import java.util.ArrayList;

import javax.swing.Icon;
import javax.swing.JLabel;

import org.appwork.utils.swing.SwingUtils;

public class MultiLineLabelTooltip extends ExtTooltip {
    public static class LabelInfo {
        private String text;

        public String getText() {
            return text;
        }

        public void setText(final String text) {
            this.text = text;
        }

        public Icon getIcon() {
            return icon;
        }

        public void setIcon(final Icon icon) {
            this.icon = icon;
        }

        public int getConstrains() {
            return constrains;
        }

        public void setConstrains(final int constrains) {
            this.constrains = constrains;
        }

        private Icon icon;

        /**
         * @param text
         * @param icon
         * @param constrains
         */
        public LabelInfo(final String text, final Icon icon, final int constrains) {
            super();
            this.text = text;
            this.icon = icon;
            this.constrains = constrains;
        }

        /**
         * @param description
         * @param icon2
         */
        public LabelInfo(final String text, final Icon icon) {
            this(text, icon, JLabel.LEADING);
        }

        private int constrains = JLabel.LEADING;
    }

    public MultiLineLabelTooltip(final LabelInfo... labels) {
        panel = new TooltipPanel("ins 3,wrap 1", "[grow,fill]", "[grow,fill]");

        for (final LabelInfo link : labels) {

            JLabel lbl;
            panel.add(lbl = new JLabel(link.getText(), link.getIcon(), link.getConstrains()));
            SwingUtils.setOpaque(lbl, false);
            lbl.setForeground(new Color(getConfig().getForegroundColor()));

        }
        panel.setOpaque(false);
        add(panel);
    }

    /**
     * @param lbls
     */
    public MultiLineLabelTooltip(final ArrayList<LabelInfo> lbls) {
        this(lbls.toArray(new LabelInfo[] {}));
    }

    @Override
    public TooltipPanel createContent() {

        return null;
    }

    @Override
    public String toText() {
        return null;
    }

}
