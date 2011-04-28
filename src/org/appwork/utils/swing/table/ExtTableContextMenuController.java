package org.appwork.utils.swing.table;

import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import javax.swing.JPopupMenu;


public abstract class ExtTableContextMenuController<T extends ExtTable<?>> implements MouseListener {

    protected T table;

    public ExtTableContextMenuController(T table) {
        this.table = table;
    }

    public void mouseClicked(MouseEvent e) {

    }

    public void mousePressed(MouseEvent e) {
    }

    public void mouseReleased(MouseEvent e) {

        if (e.isPopupTrigger() || e.getButton() == MouseEvent.BUTTON3) {
            int row = table.rowAtPoint(e.getPoint());
            Object obj = table.getExtTableModel().getObjectbyRow(row);
            if (obj == null || row == -1) {
                /* no object under mouse, lets clear the selection */
                table.clearSelection();
                JPopupMenu pu = getEmptyPopup();
                if (pu != null && pu.getComponentCount() > 0) pu.show(table, e.getPoint().x, e.getPoint().y);
                return;
            } else {
                /* check if we need to select object */
                if (!table.isRowSelected(row)) {
                    table.clearSelection();
                    table.addRowSelectionInterval(row, row);
                }
                JPopupMenu pu = getPopup();
                if (pu != null && pu.getComponentCount() > 0) pu.show(table, e.getPoint().x, e.getPoint().y);
            }
        }
    }

    protected abstract JPopupMenu getPopup();

    protected abstract JPopupMenu getEmptyPopup();

    public void mouseEntered(MouseEvent e) {
    }

    public void mouseExited(MouseEvent e) {
    }
}
