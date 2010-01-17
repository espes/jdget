package org.appwork.utils.swing.table.columns;

import java.awt.Component;

import javax.swing.JTable;

import org.appwork.utils.formatter.SizeFormater;
import org.appwork.utils.storage.DatabaseInterface;
import org.appwork.utils.swing.renderer.RenderLabel;
import org.appwork.utils.swing.table.ExtColumn;
import org.appwork.utils.swing.table.ExtDefaultRowSorter;
import org.appwork.utils.swing.table.ExtTableModel;

public abstract class ExtFileSizeColumn extends ExtColumn {

    /**
     * 
     */
    private static final long serialVersionUID = -5812486934156037376L;
    protected RenderLabel label;

    public ExtFileSizeColumn(String name, ExtTableModel table, DatabaseInterface database) {
        super(name, table, database);
        this.label = new RenderLabel();
        label.setBorder(null);
        label.setOpaque(false);
        this.setRowSorter(new ExtDefaultRowSorter() {
            /**
             * sorts the icon by hashcode
             */
            @Override
            public int compare(Object o1, Object o2) {
                if (getBytes(o1) == getBytes(o2)) return 0;
                if (this.isSortOrderToggle()) {
                    return getBytes(o1) > getBytes(o2) ? -1 : 1;
                } else {
                    return getBytes(o1) < getBytes(o2) ? -1 : 1;
                }
            }

        });
    }

    abstract protected long getBytes(Object o2);

    @Override
    public Object getCellEditorValue() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public boolean isEditable(Object obj) {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public boolean isEnabled(Object obj) {
        // TODO Auto-generated method stub
        return true;
    }

    @Override
    public boolean isSortable(Object obj) {
        // TODO Auto-generated method stub
        return true;
    }

    @Override
    public void setValue(Object value, Object object) {
        // TODO Auto-generated method stub

    }

    @Override
    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {

        label.setText(SizeFormater.formatBytes(getBytes(value)));

        return label;
    }
}
