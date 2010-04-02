package org.appwork.utils.swing.table.columns;

import java.awt.Component;

import javax.swing.BorderFactory;
import javax.swing.JTable;

import org.appwork.utils.swing.renderer.RenderLabel;
import org.appwork.utils.swing.table.ExtColumn;
import org.appwork.utils.swing.table.ExtDefaultRowSorter;
import org.appwork.utils.swing.table.ExtTableModel;

public abstract class ExtLongColumn extends ExtColumn {

    /**
     * 
     */
    private static final long serialVersionUID = -6917352290094392921L;
    private RenderLabel label;

    public ExtLongColumn(String name, ExtTableModel table) {
        super(name, table);
        this.label = new RenderLabel();
        label.setBorder(null);
        label.setOpaque(false);
        label.setBorder(BorderFactory.createEmptyBorder(0, 5, 0, 5));

        this.setRowSorter(new ExtDefaultRowSorter() {
            /**
             * sorts the icon by hashcode
             */
            @Override
            public int compare(Object o1, Object o2) {
                if (getLong(o1) == getLong(o2)) return 0;
                if (this.isSortOrderToggle()) {
                    return getLong(o1) > getLong(o2) ? -1 : 1;
                } else {
                    return getLong(o1) < getLong(o2) ? -1 : 1;
                }
            }

        });
    }

    abstract protected long getLong(Object o2);

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

        label.setText(getLong(value) + "");

        return label;
    }
}
