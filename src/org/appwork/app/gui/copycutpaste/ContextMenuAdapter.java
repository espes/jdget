package org.appwork.app.gui.copycutpaste;

import javax.swing.AbstractAction;
import javax.swing.JPopupMenu;

public interface ContextMenuAdapter {

    public JPopupMenu getPopupMenu(AbstractAction cut, AbstractAction copy, AbstractAction paste, AbstractAction delete, AbstractAction select);
}
