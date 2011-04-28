package org.appwork.utils.swing.table.columns;

import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JProgressBar;

import org.appwork.utils.swing.table.ExtColumn;
import org.appwork.utils.swing.table.ExtDefaultRowSorter;
import org.appwork.utils.swing.table.ExtTable;
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
        this.bar = new JProgressBar();
        this.bar.setOpaque(false);
        this.bar.setStringPainted(true);
        this.bar.setBorder(BorderFactory.createEmptyBorder(1, 1, 1, 1));
        this.setRowSorter(new ExtDefaultRowSorter<E>() {

            @Override
            public int compare(final E o1, final E o2) {
                final long v1 = ExtProgressColumn.this.getValue(o1);
                final long v2 = ExtProgressColumn.this.getValue(o2);
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

    protected long getMax(final E value) {
        return 100;
    }

    @Override
    public JComponent getRendererComponent(final ExtTable<E> table, final E value, final boolean isSelected, final boolean hasFocus, final int row, final int column) {

        this.bar.setIndeterminate(false);
        // Normalize value and maxvalue to fit in the integer range
        long v = this.getValue(value);
        long m = this.getMax(value);
        final double factor = Math.max(v / (double) Integer.MAX_VALUE, m / (double) Integer.MAX_VALUE);

        if (factor >= 1.0) {
            v /= factor;
            m /= factor;
        }
        // take care to set the maximum before the value!!
        this.bar.setMaximum((int) m);
        this.bar.setValue((int) v);

        this.bar.setString(this.getString(value));
        if (isSelected) {
            this.bar.setForeground(this.getModel().getTable().getColumnForegroundSelected());
            this.bar.setBackground(this.getModel().getTable().getColumnBackgroundSelected());
        } else {
            this.bar.setForeground(this.getModel().getTable().getColumnForeground());
            this.bar.setBackground(this.getModel().getTable().getColumnBackground());
        }

        this.bar.setOpaque(true);
        this.bar.setEnabled(this.isEnabled(value));
        this.bar.setToolTipText(this.bar.getString());
        return this.bar;
    }

    abstract protected String getString(E value);

    abstract protected long getValue(E value);

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
