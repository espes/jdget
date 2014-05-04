/**
 * Copyright (c) 2009 - 2013 AppWork UG(haftungsbeschränkt) <e-mail@appwork.org>
 * 
 * This file is part of org.appwork.app.gui.copycutpaste
 * 
 * This software is licensed under the Artistic License 2.0,
 * see the LICENSE file or http://www.opensource.org/licenses/artistic-license-2.0.php
 * for details
 */
package org.appwork.app.gui.copycutpaste;

import java.awt.AWTEvent;
import java.awt.Component;
import java.awt.Point;
import java.awt.event.AWTEventListener;
import java.awt.event.MouseEvent;

import javax.swing.AbstractAction;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JPopupMenu;
import javax.swing.MenuSelectionManager;
import javax.swing.SwingUtilities;
import javax.swing.text.JTextComponent;

/**
 * @author thomas
 * 
 */
public class CopyPasteSupport implements AWTEventListener {
    private long startupTime = System.currentTimeMillis();

    public long getStartupTime() {
        return startupTime;
    }

    private long lastMouseEvent = -1;

    public long getLastMouseEvent() {
        return lastMouseEvent;
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.appwork.swing.event.EDTBasicListener#onAWTEventAfterDispatch(java
     * .awt.AWTEvent)
     */
    @Override
    public void eventDispatched(final AWTEvent event) {
        if (!(event instanceof MouseEvent)) { return; }
        lastMouseEvent = System.currentTimeMillis();
        final MouseEvent mouseEvent = (MouseEvent) event;

        if (!mouseEvent.isPopupTrigger() && mouseEvent.getButton() != MouseEvent.BUTTON3) { return; }
        if (mouseEvent.getComponent() == null) { return; }
        // get deepest component
        // Component c = null;
        final Point point = mouseEvent.getPoint();

        Component c;
        if (mouseEvent.getSource() instanceof JDialog) {
            c = SwingUtilities.getDeepestComponentAt((JDialog) mouseEvent.getSource(), (int) point.getX(), (int) point.getY());
        } else if (mouseEvent.getSource() instanceof JFrame) {
            final Component source = ((JFrame) mouseEvent.getSource()).getContentPane();
            point.x -= (source.getLocationOnScreen().x - ((JFrame) mouseEvent.getSource()).getLocationOnScreen().x);
            point.y -= (source.getLocationOnScreen().y - ((JFrame) mouseEvent.getSource()).getLocationOnScreen().y);
            c = SwingUtilities.getDeepestComponentAt(source, (int) point.getX(), (int) point.getY());
        } else if (mouseEvent.getSource() instanceof Component) {
            c = (Component) mouseEvent.getSource();
        } else {

            return;
        }

        // Check if deepest component is a textcomponent
        if (!(c instanceof JTextComponent) && !(c instanceof ContextMenuAdapter)) { return; }
        if (MenuSelectionManager.defaultManager().getSelectedPath().length > 0) { return; }
 
        JPopupMenu menu;
        final JTextComponent t = (JTextComponent) c;
        t.requestFocus();
        if (c instanceof ContextMenuAdapter) {
            menu = ((ContextMenuAdapter) c).getPopupMenu(createCutAction(t), createCopyAction(t), createPasteAction(t), createDeleteAction(t), createSelectAction(t));
            if (menu == null) { return; }
        } else {

            // create menu
            menu = new JPopupMenu();
      
            menu.add(createCutAction(t));
            menu.add(createCopyAction(t));
            menu.add(createPasteAction(t));
            menu.add(createDeleteAction(t));
            menu.add(createSelectAction(t));
        }
        final Point pt = SwingUtilities.convertPoint(mouseEvent.getComponent(), mouseEvent.getPoint(), c);
        menu.show(c, pt.x, pt.y);

    }

    protected AbstractAction createSelectAction(final JTextComponent t) {
        return new SelectAction(t);
    }

    protected AbstractAction createDeleteAction(final JTextComponent t) {
        return new DeleteAction(t);
    }

    protected AbstractAction createPasteAction(final JTextComponent t) {
        return new PasteAction(t);
    }

    protected AbstractAction createCopyAction(final JTextComponent t) {
        return new CopyAction(t);
    }

    protected AbstractAction createCutAction(final JTextComponent t) {
        return new CutAction(t);
    }

}
