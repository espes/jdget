package org.appwork.swing.exttable;

import java.awt.AWTEvent;
import java.awt.EventQueue;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.InputEvent;
import java.awt.event.MouseEvent;

import javax.swing.AbstractAction;
import javax.swing.JMenuItem;

/**
 * This is an extended JCheckBoxMenuItem
 */
public class ExtMenuItem extends JMenuItem {

    /**
     * 
     */
    private static final long serialVersionUID = -2308936338542479539L;

 
    /**
     * Creates a new Menuitem By action
     * 
     * @param action
     */
    public ExtMenuItem(final AbstractAction action) {
        super(action);
    }

    /**
     * Creates a new MenuItem with name
     * 
     * @param name
     */
    public ExtMenuItem(final String name) {
        super(name);
    }
    /**
     * parameter that says if the underlaying popupmenu closes on click
     */
    private boolean hideOnClick = true;

    /**
     * @return the {@link ExtMenuItem#hideOnClick}
     * @see ExtMenuItem#hideOnClick
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
     *            the {@link ExtMenuItem#hideOnClick} to set
     * @see ExtMenuItem#hideOnClick
     */
    public void setHideOnClick(final boolean hideOnClick) {
        this.hideOnClick = hideOnClick;
    }

}
