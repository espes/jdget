package org.appwork.swing.exttable.columns;

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.EventObject;

import javax.swing.JComponent;

import org.appwork.swing.exttable.ExtColumn;
import org.appwork.swing.exttable.ExtTable;

public abstract class ExtComponentColumn<T> extends ExtColumn<T> {

    /**
     * 
     */
    private static final long serialVersionUID = 1L;

    private MouseAdapter      listener;

    /**
     * @param name
     * @param table
     */
    public ExtComponentColumn(final String name) {
        super(name, null);

        this.listener = new MouseAdapter() {

            private int col = -1;
            private int row = -1;

            @Override
            public void mouseMoved(final MouseEvent e) {

                final ExtTable<T> table = ExtComponentColumn.this.getModel().getTable();
                final int col = table.columnAtPoint(e.getPoint());
                final int row = table.getRowIndexByPoint(e.getPoint());

                int modelIndex = table.getColumnModel().getColumn(col).getModelIndex();
                if (col != this.col || row != this.row) {
                    if (ExtComponentColumn.this.getModel().getExtColumnByModelIndex(modelIndex) == ExtComponentColumn.this) {
                        if (table.getEditingColumn() == col && table.getEditingRow() == row) {
                            /*
                             * we are still in same cell, no need to change
                             * anything
                             */
                        } else {
                            final int editing = table.getEditingColumn();
                            modelIndex = table.getColumnModel().getColumn(editing).getModelIndex();
                            if (ExtComponentColumn.this.getModel().getExtColumnByModelIndex(modelIndex) == ExtComponentColumn.this) {
                                /*
                                 * we are no longer in our editing column, stop
                                 * cell editing
                                 */
                                ExtComponentColumn.this.stopCellEditing();
                            }
                            ExtComponentColumn.this.onCellUpdate(col, row);
                        }
                    } else {
                        final int editing = table.getEditingColumn();
                        modelIndex = table.getColumnModel().getColumn(editing).getModelIndex();
                        if (ExtComponentColumn.this.getModel().getExtColumnByModelIndex(modelIndex) == ExtComponentColumn.this) {
                            /*
                             * we are no longer in our editing column, stop cell
                             * editing
                             */
                            ExtComponentColumn.this.stopCellEditing();
                        }
                    }
                    this.col = col;
                    this.row = row;
                }
            }
        };
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.appwork.utils.swing.table.ExtColumn#getCellEditorValue()
     */
    @Override
    public Object getCellEditorValue() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public JComponent getEditorComponent(final T value, final boolean isSelected, final int row, final int column) {

        return this.getInternalEditorComponent(value, isSelected, row, column);
    }

    /**
     * @param value
     * @param isSelected
     * @param row
     * @param column
     * @return
     */
    abstract protected JComponent getInternalEditorComponent(T value, boolean isSelected, int row, int column);

    /**
     * @param value
     * @param isSelected
     * @param hasFocus
     * @param row
     * @param column
     * @return
     */
    /**
     * @param value
     * @param isSelected
     * @param row
     * @param column
     * @return
     */

    abstract protected JComponent getInternalRendererComponent(T value, boolean isSelected, boolean hasFocus, int row, int column);

    @Override
    public final JComponent getRendererComponent(final T value, final boolean isSelected, final boolean hasFocus, final int row, final int column) {
        if (this.listener != null) {
            this.getModel().getTable().addMouseMotionListener(this.listener);
            this.listener = null;
        }
        return this.getInternalRendererComponent(value, isSelected, hasFocus, row, column);
    }

    @Override
    public boolean isCellEditable(final EventObject evt) {
        if (evt instanceof MouseEvent) { return false; }
        return true;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.appwork.utils.swing.table.ExtColumn#isEditable(java.lang.Object)
     */
    @Override
    public boolean isEditable(final T obj) {
        // TODO Auto-generated method stub
        return true;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.appwork.utils.swing.table.ExtColumn#isEnabled(java.lang.Object)
     */
    @Override
    public boolean isEnabled(final T obj) {
        // TODO Auto-generated method stub
        return true;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.appwork.utils.swing.table.ExtColumn#isSortable(java.lang.Object)
     */
    @Override
    public boolean isSortable(final T obj) {
        // TODO Auto-generated method stub
        return false;
    }

    /**
     * @param col
     * @param row
     */
    protected void onCellUpdate(final int col, final int row) {
        this.getModel().getTable().editCellAt(row, col);

    }

    /*
     * (non-Javadoc)
     * 
     * @see org.appwork.utils.swing.table.ExtColumn#setValue(java.lang.Object,
     * java.lang.Object)
     */
    @Override
    public void setValue(final Object value, final T object) {
        // TODO Auto-generated method stub

    }

}
