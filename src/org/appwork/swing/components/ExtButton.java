package org.appwork.swing.components;

import java.awt.Point;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.AbstractAction;
import javax.swing.JButton;
import javax.swing.JComponent;

import org.appwork.swing.action.BasicAction;
import org.appwork.swing.components.tooltips.ExtTooltip;
import org.appwork.swing.components.tooltips.ToolTipController;
import org.appwork.swing.components.tooltips.ToolTipHandler;
import org.appwork.swing.components.tooltips.TooltipFactory;
import org.appwork.swing.components.tooltips.TooltipTextDelegateFactory;

public class ExtButton extends JButton implements ToolTipHandler {

    /**
     * 
     */
    private static final long serialVersionUID = -7151290227825542967L;
    private TooltipFactory    tooltipFactory;
    private MouseAdapter      rollOverlistener;

    /**
     * @param autoDetectAction
     */
    public ExtButton(AbstractAction action) {
        super(action);

        this.tooltipFactory = new TooltipTextDelegateFactory(this);

        if (getToolTipText() != null) setTooltipsEnabled(true);

    }

    /**
     * 
     */
    public ExtButton() {
     this(null);
        // TODO Auto-generated constructor stub
    }

    public ExtButton setTooltipsEnabled(boolean b) {
        if (b) {
            ToolTipController.getInstance().register(this);
        } else {
            ToolTipController.getInstance().unregister(this);

        }
        return this;
    }

    public String getToolTipText() {
        String ret = super.getToolTipText();
        if (ret == null || "".equals(ret)) {
            if (getAction() instanceof BasicAction) {
                ret = ((BasicAction) getAction()).getTooltipText();
            }
        }
        return ret;

    }

    public TooltipFactory getTooltipFactory() {

        if (getAction() instanceof BasicAction) {
            TooltipFactory ret = ((BasicAction) getAction()).getTooltipFactory();
            if (ret != null) return ret;
        }
        return tooltipFactory;
    }

    public void setTooltipFactory(TooltipFactory tooltipFactory) {
        this.tooltipFactory = tooltipFactory;
        ToolTipController.getInstance().register(this);
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.appwork.swing.components.tooltips.ToolTipHandler#createExtTooltip
     * (java.awt.Point)
     */
    @Override
    public ExtTooltip createExtTooltip(Point mousePosition) {

        return getTooltipFactory().createTooltip();
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.appwork.swing.components.tooltips.ToolTipHandler#
     * isTooltipDisabledUntilNextRefocus()
     */
    @Override
    public boolean isTooltipDisabledUntilNextRefocus() {

        return true;
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.appwork.swing.components.tooltips.ToolTipHandler#updateTooltip(org
     * .appwork.swing.components.tooltips.ExtTooltip, java.awt.event.MouseEvent)
     */
    @Override
    public boolean updateTooltip(ExtTooltip activeToolTip, MouseEvent e) {
        return false;
    }

    @Override
    public void setToolTipText(final String text) {

        this.putClientProperty(JComponent.TOOL_TIP_TEXT_KEY, text);

        if (text == null || text.length() == 0) {
            ToolTipController.getInstance().unregister(this);
        } else {
            ToolTipController.getInstance().register(this);
        }
    }

    /**
     * @param b
     */
    public void setRolloverEffectEnabled(boolean b) {
        if (b) {
            if (rollOverlistener == null) {
                rollOverlistener = new MouseAdapter() {

                    @Override
                    public void mouseEntered(MouseEvent e) {
                        onRollOver();
                   
                    }

                    @Override
                    public void mouseExited(MouseEvent e) {
                        onRollOut();
                  
                    }

                };
            }
            addMouseListener(rollOverlistener);
            onRollOut();

        } else {
            if (rollOverlistener != null) {
                removeMouseListener(rollOverlistener);
                rollOverlistener = null;
            }
        }
    }

    /**
     * 
     */
    protected void onRollOut() {
        setContentAreaFilled(false);
        
    }

    /**
     * 
     */
    protected void onRollOver() {
        setContentAreaFilled(true);
        
    }
}
