package org.appwork.app.gui.copycutpaste;

import javax.swing.JPopupMenu;

public interface ContextMenuAdapter {

    public JPopupMenu getPopupMenu(CutAction cutAction, CopyAction copyAction, PasteAction pasteAction, DeleteAction deleteAction, SelectAction selectAction);
}
