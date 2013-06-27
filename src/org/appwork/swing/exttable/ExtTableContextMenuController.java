package org.appwork.swing.exttable;

import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import javax.swing.JPopupMenu;

public abstract class ExtTableContextMenuController<T extends ExtTable<?>> implements MouseListener {

    protected T table;

    public ExtTableContextMenuController(final T table) {
        this.table = table;
    }

    protected abstract JPopupMenu getEmptyPopup();

    protected abstract JPopupMenu getPopup();

    public void mouseClicked(final MouseEvent e) {

    }

    public void mouseEntered(final MouseEvent e) {
    }

    public void mouseExited(final MouseEvent e) {
    }

    public void mousePressed(final MouseEvent e) {
    }

    public void mouseReleased(final MouseEvent e) {

        if (e.isPopupTrigger() || e.getButton() == MouseEvent.BUTTON3) {
            final int row = this.table.rowAtPoint(e.getPoint());
            final Object obj = this.table.getModel().getObjectbyRow(row);
            if (obj == null || row == -1) {
                /* no object under mouse, lets clear the selection */
                this.table.clearSelection();
                final JPopupMenu pu = this.getEmptyPopup();
                if (pu != null && pu.getComponentCount() > 0) {
                    pu.show(this.table, e.getPoint().x, e.getPoint().y);
                }
                return;
            } else {
                /* check if we need to select object */
                if (!this.table.isRowSelected(row)) {
                    this.table.clearSelection();
                    this.table.addRowSelectionInterval(row, row);
                }
                final JPopupMenu pu = this.getPopup();
                if (pu != null && pu.getComponentCount() > 0) {
                    pu.show(this.table, e.getPoint().x, e.getPoint().y);
                }
            }
        }
    }
}
