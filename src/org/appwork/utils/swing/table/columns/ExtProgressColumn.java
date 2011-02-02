package org.appwork.utils.swing.table.columns;

import java.awt.Component;

import javax.swing.JProgressBar;
import javax.swing.JTable;

import org.appwork.utils.swing.table.ExtColumn;
import org.appwork.utils.swing.table.ExtDefaultRowSorter;
import org.appwork.utils.swing.table.ExtTableModel;

abstract public class ExtProgressColumn<E> extends ExtColumn<E> {
    private static final long serialVersionUID = -2473320164484034664L;
    protected JProgressBar    bar;

    /**
     * 
     */
    public ExtProgressColumn(final String title) {
        this(title, null);
    }

    public ExtProgressColumn(final String name, final ExtTableModel<E> table) {
        super(name, table);
        this.bar = new JProgressBar(0, this.getMax());
        this.bar.setOpaque(false);
        this.bar.setStringPainted(true);

        this.setRowSorter(new ExtDefaultRowSorter<E>() {

            @Override
            public int compare(final E o1, final E o2) {
                int v1 = getValue(o1);
                int v2 = getValue(o2);
                if (v1 == v2) { return 0; }
                if (this.isSortOrderToggle()) {
                    return v1 > v2 ? -1 : 1;
                } else {
                    return v2 < v1 ? -1 : 1;
                }
            }

        });
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

    protected int getMax() {
        return 100;
    }

    abstract protected String getString(E value);

    @SuppressWarnings("unchecked")
    @Override
    public Component getTableCellRendererComponent(final JTable table, final Object value, final boolean isSelected, final boolean hasFocus, final int row, final int column) {

        this.bar.setIndeterminate(false);
        this.bar.setValue(this.getValue((E) value));

        this.bar.setString(this.getString((E) value));
        if (isSelected) {
            this.bar.setForeground(this.getModel().getTable().getColumnForegroundSelected());
            this.bar.setBackground(this.getModel().getTable().getColumnBackgroundSelected());
        } else {
            this.bar.setForeground(this.getModel().getTable().getColumnForeground());
            this.bar.setBackground(this.getModel().getTable().getColumnBackground());
        }

        this.bar.setOpaque(true);
        this.bar.setEnabled(this.isEnabled((E) value));
        return this.bar;
    }

    abstract protected int getValue(E value);

    /*
     * (non-Javadoc)
     * 
     * @see
     * com.rapidshare.rsmanager.gui.components.table.ExtColumn#isEditable(java
     * .lang.Object)
     */
    @Override
    public boolean isEditable(final E obj) {

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
    public boolean isEnabled(final E obj) {

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
    public boolean isSortable(final E obj) {

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
    public void setValue(final Object value, final E object) {

    }

}
