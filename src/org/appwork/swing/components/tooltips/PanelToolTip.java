package org.appwork.swing.components.tooltips;


public class PanelToolTip extends ExtTooltip {

    /**
     * 
     */
    private static final long serialVersionUID = 5839491704118559564L;

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

    /* (non-Javadoc)
     * @see org.appwork.swing.components.tooltips.ExtTooltip#toText()
     */
    @Override
    public String toText() {
        // TODO Auto-generated method stub
        return null;
    }

}
