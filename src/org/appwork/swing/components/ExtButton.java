package org.appwork.swing.components;

import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.InputMap;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.KeyStroke;

import org.appwork.swing.action.BasicAction;
import org.appwork.swing.components.tooltips.ExtTooltip;
import org.appwork.swing.components.tooltips.ToolTipController;
import org.appwork.swing.components.tooltips.ToolTipHandler;
import org.appwork.swing.components.tooltips.TooltipFactory;
import org.appwork.swing.components.tooltips.TooltipTextDelegateFactory;
import org.appwork.utils.StringUtils;

public class ExtButton extends JButton implements ToolTipHandler {

    /**
     * 
     */
    private static final long serialVersionUID = -7151290227825542967L;
    private TooltipFactory    tooltipFactory;
    private MouseAdapter      rollOverlistener;

    /**
     * 
     */
    public ExtButton() {
        this(null);
        // TODO Auto-generated constructor stub
    }

    /**
     * @param autoDetectAction
     */
    public ExtButton(final AbstractAction action) {
        super(action);
        this.tooltipFactory = new TooltipTextDelegateFactory(this);

        if (!StringUtils.isEmpty(getToolTipText())) {
            this.setTooltipsEnabled(true);
        }

        if (action instanceof BasicAction) {
            if (((BasicAction) action).getTooltipFactory() != null) {
                tooltipFactory = ((BasicAction) action).getTooltipFactory();
                setTooltipsEnabled(true);
            }
            if (!StringUtils.isEmpty(((BasicAction) action).getTooltipText())) {
                setTooltipsEnabled(true);
            }
            InputMap inputmap = getInputMap(JButton.WHEN_IN_FOCUSED_WINDOW);
            KeyStroke keystroke = (KeyStroke) action.getValue(Action.ACCELERATOR_KEY);
            if (keystroke != null) {
                inputmap.put(keystroke, ((BasicAction) action).getShortCutString());
                setTooltipsEnabled(true);
                StringBuilder tt = new StringBuilder();
                if (!StringUtils.isEmpty(getToolTipText())) {
                    tt.append(getToolTipText());
                }
                if (tt.length() > 0) tt.append(" ");
                tt.append("[");
                tt.append(((BasicAction) action).getShortCutString());
                tt.append("]");

                setToolTipText(tt.toString());
                getActionMap().put(((BasicAction) action).getShortCutString(), new AbstractAction() {

                    @Override
                    public void actionPerformed(ActionEvent e) {

                        doClick();
                    }
                });
            }
        }

    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.appwork.swing.components.tooltips.ToolTipHandler#createExtTooltip
     * (java.awt.Point)
     */
    @Override
    public ExtTooltip createExtTooltip(final Point mousePosition) {

        return this.getTooltipFactory().createTooltip();
    }

    public TooltipFactory getTooltipFactory() {

        if (this.getAction() instanceof BasicAction) {
            final TooltipFactory ret = ((BasicAction) this.getAction()).getTooltipFactory();
            if (ret != null) { return ret; }
        }
        return this.tooltipFactory;
    }

    @Override
    public String getToolTipText() {
        String ret = super.getToolTipText();
        if (ret == null || "".equals(ret)) {
            if (this.getAction() instanceof BasicAction) {
                ret = ((BasicAction) this.getAction()).getTooltipText();
            }
        }
        return ret;

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

    @Override
    public boolean isTooltipWithoutFocusEnabled() {
        // TODO Auto-generated method stub
        return true;
    }

    /**
     * 
     */
    protected void onRollOut() {
        this.setContentAreaFilled(false);

    }

    /**
     * 
     */
    protected void onRollOver() {
        this.setContentAreaFilled(true);

    }

    /**
     * @param b
     */
    public void setRolloverEffectEnabled(final boolean b) {
        if (b) {
            if (this.rollOverlistener == null) {
                this.rollOverlistener = new MouseAdapter() {

                    @Override
                    public void mouseEntered(final MouseEvent e) {
                        ExtButton.this.onRollOver();

                    }

                    @Override
                    public void mouseExited(final MouseEvent e) {
                        ExtButton.this.onRollOut();

                    }

                };
            }
            this.addMouseListener(this.rollOverlistener);
            this.onRollOut();

        } else {
            if (this.rollOverlistener != null) {
                this.removeMouseListener(this.rollOverlistener);
                this.rollOverlistener = null;
            }
        }
    }

    public void setTooltipFactory(final TooltipFactory tooltipFactory) {
        this.tooltipFactory = tooltipFactory;
        ToolTipController.getInstance().register(this);
    }

    public ExtButton setTooltipsEnabled(final boolean b) {
        if (b) {
            ToolTipController.getInstance().register(this);
        } else {
            ToolTipController.getInstance().unregister(this);

        }
        return this;
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

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.appwork.swing.components.tooltips.ToolTipHandler#updateTooltip(org
     * .appwork.swing.components.tooltips.ExtTooltip, java.awt.event.MouseEvent)
     */
    @Override
    public int getTooltipDelay(Point mousePositionOnScreen) {  return 0;    }  @Override public boolean updateTooltip(final ExtTooltip activeToolTip, final MouseEvent e) {
        return false;
    }

    /* (non-Javadoc)
     * @see org.appwork.swing.components.tooltips.ToolTipHandler#getTooltipDelay()
     */
    
}
