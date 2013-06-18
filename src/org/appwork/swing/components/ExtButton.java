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
import org.appwork.utils.KeyUtils;
import org.appwork.utils.StringUtils;

public class ExtButton extends JButton implements ToolTipHandler {

    /**
     * 
     */
    private static final long serialVersionUID = -7151290227825542967L;
    private TooltipFactory    tooltipFactory;
    private MouseAdapter      rollOverlistener;
    private KeyStroke         accelerator;

    /**
     * 
     */
    public ExtButton() {
        this(null);
        // TODO Auto-generated constructor stub
    }

    protected void actionPropertyChanged(final Action action, final String propertyName) {
        super.actionPropertyChanged(action, propertyName);

        if (propertyName == Action.ACCELERATOR_KEY) {
            setAccelerator((KeyStroke) action.getValue(Action.ACCELERATOR_KEY));
        }
    }

    /**
     * @param autoDetectAction
     */
    public ExtButton(final AbstractAction action) {
        super(action);
        tooltipFactory = new TooltipTextDelegateFactory(this);

        if (!StringUtils.isEmpty(getToolTipText())) {
            setTooltipsEnabled(true);
        }

        if (action instanceof BasicAction) {

            if (((BasicAction) action).getTooltipFactory() != null) {
                tooltipFactory = ((BasicAction) action).getTooltipFactory();
                setTooltipsEnabled(true);
            }
            if (!StringUtils.isEmpty(((BasicAction) action).getTooltipText())) {
                setTooltipsEnabled(true);
            }

            setAccelerator((KeyStroke) action.getValue(Action.ACCELERATOR_KEY));
        }

    }

    public void setAccelerator(final KeyStroke newAccelerator) {

        final InputMap inputmap = getInputMap(JButton.WHEN_IN_FOCUSED_WINDOW);
        if (accelerator != null) {
            inputmap.remove(newAccelerator);
          
            getActionMap().remove(KeyUtils.getShortcutString(accelerator, true));
            accelerator = null;
            setTooltipsEnabled(StringUtils.isNotEmpty(getToolTipText()));
        

        }
        if (newAccelerator != null) {
            accelerator = newAccelerator;

            final String shortcutString = KeyUtils.getShortcutString(newAccelerator, true);
            inputmap.put(newAccelerator, shortcutString);

            setTooltipsEnabled(true);
            getActionMap().put(shortcutString, new AbstractAction() {

                @Override
                public void actionPerformed(final ActionEvent e) {

                    doClick();
                }
            });
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

        return getTooltipFactory().createTooltip();
    }

    public TooltipFactory getTooltipFactory() {

        if (getAction() instanceof BasicAction) {
            final TooltipFactory ret = ((BasicAction) getAction()).getTooltipFactory();
            if (ret != null) { return ret; }
        }
        return tooltipFactory;
    }

    @Override
    public String getToolTipText() {
        String ret = super.getToolTipText();
        if (ret == null || "".equals(ret)) {
            if (getAction() instanceof BasicAction) {
                ret = ((BasicAction) getAction()).getTooltipText();
            }
        }

        if (accelerator != null) {
            if (ret == null) {
                ret = "";
            }
            if (ret.length() > 0) {
                ret += " ";
            }
            ret += "[" + KeyUtils.getShortcutString(accelerator, true) + "]";
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
        setContentAreaFilled(false);

    }

    /**
     * 
     */
    protected void onRollOver() {
        setContentAreaFilled(true);

    }

    /**
     * @param b
     */
    public void setRolloverEffectEnabled(final boolean b) {
        if (b) {
            if (rollOverlistener == null) {
                rollOverlistener = new MouseAdapter() {

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
            addMouseListener(rollOverlistener);
            onRollOut();

        } else {
            if (rollOverlistener != null) {
                removeMouseListener(rollOverlistener);
                rollOverlistener = null;
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

        putClientProperty(JComponent.TOOL_TIP_TEXT_KEY, text);

        setTooltipsEnabled(StringUtils.isNotEmpty(getToolTipText()));
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.appwork.swing.components.tooltips.ToolTipHandler#updateTooltip(org
     * .appwork.swing.components.tooltips.ExtTooltip, java.awt.event.MouseEvent)
     */
    @Override
    public int getTooltipDelay(final Point mousePositionOnScreen) {
        return 0;
    }

    @Override
    public boolean updateTooltip(final ExtTooltip activeToolTip, final MouseEvent e) {
        return false;
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.appwork.swing.components.tooltips.ToolTipHandler#getTooltipDelay()
     */

}
