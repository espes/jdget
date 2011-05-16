package org.appwork.utils.swing.table.columns;

import java.util.regex.Pattern;

import javax.swing.JComponent;

import org.appwork.utils.swing.table.ExtColumn;
import org.appwork.utils.swing.table.ExtDefaultRowSorter;
import org.appwork.utils.swing.table.ExtTable;
import org.appwork.utils.swing.table.ExtTableModel;

public abstract class ExtCompoundColumn<T> extends ExtColumn<T> {

    /**
     * 
     */
    private static final long serialVersionUID = 1L;
    private ExtColumn<T>      editor;
    private T                 editing;

    public ExtCompoundColumn(final String name) {
        this(name, null);
    }

    /**
     * @param name
     * @param table
     */
    public ExtCompoundColumn(final String name, final ExtTableModel<T> table) {
        super(name, table);
        this.setRowSorter(new ExtDefaultRowSorter<T>() {

            @Override
            public int compare(final T o1, final T o2) {
                String o1s = ExtCompoundColumn.this.getSortString(o1);
                String o2s = ExtCompoundColumn.this.getSortString(o2);
                if (o1s == null) {
                    o1s = "";
                }
                if (o2s == null) {
                    o2s = "";
                }
                if (this.isSortOrderToggle()) {
                    return o1s.compareTo(o2s);
                } else {
                    return o2s.compareTo(o1s);
                }

            }

        });

    }

    @Override
    public Object getCellEditorValue() {
        return this.editor.getCellEditorValue();
    }

    @Override
    public JComponent getEditorComponent(final ExtTable<T> table, final T value, final boolean isSelected, final int row, final int column) {
        this.editing = value;
        this.editor = this.selectColumn(this.editing);

        return this.editor.getEditorComponent(table, value, isSelected, row, column);
    }

    @Override
    public JComponent getRendererComponent(final ExtTable<T> table, final T value, final boolean isSelected, final boolean hasFocus, final int row, final int column) {
        return this.selectColumn(value).getRendererComponent(table, value, isSelected, hasFocus, row, column);
    }

    @Override
    public ExtDefaultRowSorter<T> getRowSorter(final boolean sortOrderToggle) {
        return super.getRowSorter(sortOrderToggle);
    }

    /**
     * @param o1
     * @return
     */
    public abstract String getSortString(T o1);

    @Override
    public boolean isEditable(final T obj) {
        return false;
    }

    @Override
    public boolean isEnabled(final T obj) {
        return false;
    }

    @Override
    public boolean isSortable(final T obj) {
        return false;
    }

    @Override
    public boolean matchSearch(final T object, final Pattern pattern) {
        return this.selectColumn(object).matchSearch(object, pattern);
    }

    /**
     * @param object
     * @return
     */
    abstract public ExtColumn<T> selectColumn(T object);

    @Override
    public void setModel(final ExtTableModel<T> model) {
        this.setModelToCompounds(model);
        super.setModel(model);
    }

    /**
     * @param model
     */
    public abstract void setModelToCompounds(ExtTableModel<T> model);

    @Override
    public void setValue(final Object value, final T object) {
        this.selectColumn(object).setValue(value, object);
    }
}
