package org.appwork.utils.swing.table.columns;

import javax.swing.JComponent;
import javax.swing.JSpinner;
import javax.swing.SpinnerModel;
import javax.swing.SwingConstants;

import org.appwork.utils.swing.table.ExtDefaultRowSorter;
import org.appwork.utils.swing.table.ExtTable;
import org.appwork.utils.swing.table.ExtTableModel;

public abstract class ExtSpinnerColumn<E> extends ExtTextColumn<E> {
    /**
     * 
     */
    private static final long serialVersionUID = 1L;

    private JSpinner          editor;
    private String            editorFormat;

    public ExtSpinnerColumn(final String name) {
        this(name, null);

    }

    public ExtSpinnerColumn(final String name, final ExtTableModel<E> table) {
        super(name, table);

        this.editor = new JSpinner();
        this.editor.setBorder(null);

        this.init();
        this.label.setHorizontalAlignment(SwingConstants.RIGHT);
        this.setRowSorter(new ExtDefaultRowSorter<E>() {

            @Override
            public int compare(final E o1, final E o2) {

                final float _1 = ExtSpinnerColumn.this.getNumber(o1).floatValue();
                final float _2 = ExtSpinnerColumn.this.getNumber(o2).floatValue();

                if (this.isSortOrderToggle()) {
                    return _1 == _2 ? 0 : _1 < _2 ? -1 : 1;
                } else {
                    return _1 == _2 ? 0 : _1 > _2 ? -1 : 1;
                }

            }

        });
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.appwork.utils.swing.table.ExtColumn#getCellEditorValue()
     */
    @Override
    public Object getCellEditorValue() {
        // TODO Auto-generated method stub
        return this.editor.getValue();
    }

    @Override
    public JComponent getEditorComponent(final ExtTable<E> table, final E value, final boolean isSelected, final int row, final int column) {
        final SpinnerModel m = this.getModel(value);
        if (m != this.editor.getModel()) {
            this.editor.setModel(m);
        }

        final String f = this.getFormat(value);
        if (!f.equals(this.editorFormat)) {
            this.editor.setEditor(new JSpinner.NumberEditor(this.editor, f));
            this.editorFormat = f;
        }
        this.editor.setValue(this.getNumber(value));
        return this.editor;
    }

    protected abstract String getFormat(E value);

    /**
     * @param value
     * @return
     */
    protected abstract SpinnerModel getModel(E value);

    /**
     * @param value
     * @return
     */
    abstract protected Number getNumber(final E value);

    /**
     * 
     */
    protected void init() {
        // TODO Auto-generated method stub

    }

    /*
     * (non-Javadoc)
     * 
     * @see org.appwork.utils.swing.table.ExtColumn#isEditable(java.lang.Object)
     */
    @Override
    public boolean isEditable(final E obj) {
        // TODO Auto-generated method stub
        return true;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.appwork.utils.swing.table.ExtColumn#isEnabled(java.lang.Object)
     */
    @Override
    public boolean isEnabled(final E obj) {
        // TODO Auto-generated method stub
        return true;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.appwork.utils.swing.table.ExtColumn#isSortable(java.lang.Object)
     */
    @Override
    public boolean isSortable(final E obj) {
        // TODO Auto-generated method stub
        return true;
    }

    /**
     * @param value
     * @param object
     */
    abstract protected void setNumberValue(Number value, E object);

    @Override
    final public void setValue(final Object value, final E object) {
        // TODO Auto-generated method stub
        this.setNumberValue((Number) value, object);
    }

}
