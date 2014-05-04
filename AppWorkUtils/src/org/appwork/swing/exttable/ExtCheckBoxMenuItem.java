package org.appwork.swing.exttable;

import java.awt.AWTEvent;
import java.awt.EventQueue;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.InputEvent;
import java.awt.event.MouseEvent;

import javax.swing.AbstractAction;
import javax.swing.JCheckBoxMenuItem;

/**
 * This is an extended JCheckBoxMenuItem
 */
public class ExtCheckBoxMenuItem extends JCheckBoxMenuItem {

    /**
     * 
     */
    private static final long serialVersionUID = -2308936338542479539L;

 
    /**
     * Creates a new Menuitem By action
     * 
     * @param action
     */
    public ExtCheckBoxMenuItem(final AbstractAction action) {
        super(action);
    }

    /**
     * Creates a new MenuItem with name
     * 
     * @param name
     */
    public ExtCheckBoxMenuItem(final String name) {
        super(name);
    }
    /**
     * parameter that says if the underlaying popupmenu closes on click
     */
    private boolean hideOnClick = true;

    /**
     * @return the {@link ExtCheckBoxMenuItem#hideOnClick}
     * @see ExtCheckBoxMenuItem#hideOnClick
     */
    public boolean isHideOnClick() {
        return hideOnClick;
    }

    protected void processMouseEvent(final MouseEvent e) {
        if (!hideOnClick && e.getID() == MouseEvent.MOUSE_RELEASED) {
            setSelected(!isSelected());
            for (final ActionListener al : getActionListeners()) {
                
                int modifiers = 0;
                final AWTEvent currentEvent = EventQueue.getCurrentEvent();
                if (currentEvent instanceof InputEvent) {
                    modifiers = ((InputEvent) currentEvent).getModifiers();
                } else if (currentEvent instanceof ActionEvent) {
                    modifiers = ((ActionEvent) currentEvent).getModifiers();
                }
                al.actionPerformed(new ActionEvent(this, ActionEvent.ACTION_PERFORMED, getActionCommand(), EventQueue.getMostRecentEventTime(), modifiers));
                
              
            }
//            doClick(0);
        } else {
            super.processMouseEvent(e);
        }
    }

    /**
     * @param hideOnClick
     *            the {@link ExtCheckBoxMenuItem#hideOnClick} to set
     * @see ExtCheckBoxMenuItem#hideOnClick
     */
    public void setHideOnClick(final boolean hideOnClick) {
        this.hideOnClick = hideOnClick;
    }

}
