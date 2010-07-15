package org.appwork.utils.swing.table.columns;

import java.awt.Component;

import javax.swing.JProgressBar;
import javax.swing.JTable;

import org.appwork.utils.swing.table.ExtColumn;
import org.appwork.utils.swing.table.ExtDefaultRowSorter;
import org.appwork.utils.swing.table.ExtTableModel;

abstract public class ExtProgressColumn<E> extends ExtColumn<E> {
    private static final long serialVersionUID = -2473320164484034664L;
    protected JProgressBar bar;

    public ExtProgressColumn(String name, ExtTableModel<E> table) {
        super(name, table);
        bar = new JProgressBar(0, getMax());
        bar.setOpaque(false);
        bar.setStringPainted(true);

        this.setRowSorter(new ExtDefaultRowSorter<E>() {

            @Override
            public int compare(E o1, E o2) {
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
    public boolean isEditable(E obj) {
        
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
    public boolean isEnabled(E obj) {
        
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
    public boolean isSortable(E obj) {
        
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
    public void setValue(Object value, E object) {
        

    }

    @SuppressWarnings("unchecked")
    @Override
    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {

        bar.setIndeterminate(false);
        bar.setValue(getValue((E) value));

        bar.setString(getString((E) value));
        if (isSelected) {
            bar.setForeground(getModel().getTable().getColumnForegroundSelected());
            bar.setBackground(getModel().getTable().getColumnBackgroundSelected());
        } else {
            bar.setForeground(getModel().getTable().getColumnForeground());
            bar.setBackground(getModel().getTable().getColumnBackground());
        }

        bar.setOpaque(true);
        bar.setEnabled(isEnabled((E) value));
        return bar;
    }

    abstract protected int getValue(E value);

    abstract protected String getString(E value);

}
