package org.appwork.utils.swing.table.columns;

import java.awt.Component;

import javax.swing.Icon;
import javax.swing.JTable;

import org.appwork.utils.storage.DatabaseInterface;
import org.appwork.utils.swing.renderer.RenderLabel;
import org.appwork.utils.swing.table.ExtColumn;
import org.appwork.utils.swing.table.ExtDefaultRowSorter;
import org.appwork.utils.swing.table.ExtTableModel;


/**
 * Single icon column
 * 
 * @author $Author: unknown$
 * 
 */
public abstract class ExtIconColumn extends ExtColumn {

    private RenderLabel label;

    public ExtIconColumn(String name, ExtTableModel table, DatabaseInterface database) {
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
                if (getIcon(o1).hashCode() == getIcon(o2).hashCode()) return 0;
                if (this.isSortOrderToggle()) {
                    return getIcon(o1).hashCode() > getIcon(o2).hashCode() ? -1 : 1;
                } else {
                    return getIcon(o1).hashCode() < getIcon(o2).hashCode() ? -1 : 1;
                }
            }

        });
    }

    /**
     * 
     */
    private static final long serialVersionUID = -5751315870107507714L;

    /**
     * Returns the icon for o1;
     * 
     * @param o1
     * @return
     */
    abstract protected Icon getIcon(Object o1);

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

    /**
     * Sets max width to 30. overwrite to set other maxsizes
     */
    protected int getMaxWidth() {
        return 30;
    }

    @Override
    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {

        label.setIcon(getIcon(value));

        return label;
    }

}
