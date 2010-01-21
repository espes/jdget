package org.appwork.utils.swing.table.columns;

import java.awt.Component;

import javax.swing.JProgressBar;
import javax.swing.JTable;

import org.appwork.utils.storage.DatabaseInterface;
import org.appwork.utils.swing.table.ExtColumn;
import org.appwork.utils.swing.table.ExtDefaultRowSorter;
import org.appwork.utils.swing.table.ExtTable;
import org.appwork.utils.swing.table.ExtTableModel;

abstract public class ExtProgressColumn extends ExtColumn {
    private static final long serialVersionUID = -2473320164484034664L;
    protected JProgressBar bar;

    public ExtProgressColumn(String name, ExtTableModel table, DatabaseInterface database) {
        super(name, table, database);
        bar = new JProgressBar(0, getMax());
        bar.setOpaque(false);
        bar.setStringPainted(true);
        this.setRowSorter(new ExtDefaultRowSorter() {

            @Override
            public int compare(Object o1, Object o2) {
                if (getValue(o1) == getValue(o2)) return 0;
                if (this.isSortOrderToggle()) {
                    return getValue(o1) > getValue(o2) ? -1 : 1;
                } else {
                    return getValue(o1) < getValue(o2) ? -1 : 1;
                }
            }

        });
    }

    protected int getMax() {
        return 100;
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * com.rapidshare.rsmanager.gui.components.table.ExtColumn#getCellEditorValue
     * ()
     */
    @Override
    public Object getCellEditorValue() {
        // TODO Auto-generated method stub
        return null;
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * com.rapidshare.rsmanager.gui.components.table.ExtColumn#isEditable(java
     * .lang.Object)
     */
    @Override
    public boolean isEditable(Object obj) {
        // TODO Auto-generated method stub
        return false;
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * com.rapidshare.rsmanager.gui.components.table.ExtColumn#isEnabled(java
     * .lang.Object)
     */
    @Override
    public boolean isEnabled(Object obj) {
        // TODO Auto-generated method stub
        return true;
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * com.rapidshare.rsmanager.gui.components.table.ExtColumn#isSortable(java
     * .lang.Object)
     */
    @Override
    public boolean isSortable(Object obj) {
        // TODO Auto-generated method stub
        return true;
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * com.rapidshare.rsmanager.gui.components.table.ExtColumn#setValue(java
     * .lang.Object, java.lang.Object)
     */
    @Override
    public void setValue(Object value, Object object) {
        // TODO Auto-generated method stub

    }

    @Override
    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {

        bar.setValue(getValue(value));
        bar.setString(getString(value));
        if (isSelected) {
            bar.setForeground(((ExtTable) table).getColumnForegroundSelected());
            bar.setBackground(((ExtTable) table).getColumnBackgroundSelected());
        } else {
            bar.setForeground(((ExtTable) table).getColumnForeground());
            bar.setBackground(((ExtTable) table).getColumnBackground());
        }

        bar.setOpaque(true);

        return bar;
    }

    abstract protected int getValue(Object value);

    abstract protected String getString(Object value);

}
