package org.appwork.swing.components.tooltips;


public class PanelToolTip extends ExtTooltip {

    /**
     * @param comp
     */
    public PanelToolTip(final TooltipPanel comp) {
        super();

        this.panel = comp;
        this.add(this.panel);

    }

    @Override
    public TooltipPanel createContent() {
        // TODO Auto-generated method stub
        return null;
    }

}
