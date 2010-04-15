package org.appwork.utils.swing.table.columns;

import java.awt.Component;

import javax.swing.BorderFactory;
import javax.swing.JTable;

import org.appwork.utils.formatter.SizeFormater;
import org.appwork.utils.swing.renderer.RenderLabel;
import org.appwork.utils.swing.table.ExtColumn;
import org.appwork.utils.swing.table.ExtDefaultRowSorter;
import org.appwork.utils.swing.table.ExtTableModel;

public abstract class ExtFileSizeColumn<E> extends ExtColumn<E> {

    /**
     * 
     */
    private static final long serialVersionUID = -5812486934156037376L;
    protected RenderLabel label;

    public ExtFileSizeColumn(String name, ExtTableModel<E> table) {
        super(name, table);
        this.label = new RenderLabel();
        label.setBorder(null);
        label.setOpaque(false);
        label.setBorder(BorderFactory.createEmptyBorder(0, 5, 0, 5));

        this.setRowSorter(new ExtDefaultRowSorter<E>() {
            /**
             * sorts the icon by hashcode
             */
            @Override
            public int compare(E o1, E o2) {
                if (getBytes(o1) == getBytes(o2)) return 0;
                if (this.isSortOrderToggle()) {
                    return getBytes(o1) > getBytes(o2) ? -1 : 1;
                } else {
                    return getBytes(o1) < getBytes(o2) ? -1 : 1;
                }
            }

        });
    }

    abstract protected long getBytes(E o2);

    @Override
    public Object getCellEditorValue() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public boolean isEditable(E obj) {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public boolean isEnabled(E obj) {
        // TODO Auto-generated method stub
        return true;
    }

    @Override
    public boolean isSortable(E obj) {
        // TODO Auto-generated method stub
        return true;
    }

    @Override
    public void setValue(Object value, E object) {
        // TODO Auto-generated method stub

    }

    @SuppressWarnings("unchecked")
    @Override
    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {

        label.setText(SizeFormater.formatBytes(getBytes((E) value)));

        return label;
    }
}
