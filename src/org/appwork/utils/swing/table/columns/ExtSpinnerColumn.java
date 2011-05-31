package org.appwork.utils.swing.table.columns;

import javax.swing.JComponent;
import javax.swing.JSpinner;
import javax.swing.JSpinner.DefaultEditor;
import javax.swing.JSpinner.NumberEditor;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingConstants;

import org.appwork.utils.reflection.Clazz;
import org.appwork.utils.swing.table.ExtDefaultRowSorter;
import org.appwork.utils.swing.table.ExtTableModel;

public abstract class ExtSpinnerColumn<E> extends ExtTextColumn<E> {
    /**
     * 
     */
    private static final long  serialVersionUID = 1L;

    private JSpinner           editor;

    private NumberEditor       intEditor;

    private NumberEditor       longEditor;

    private NumberEditor       byteEditor;

    private NumberEditor       shortEditor;

    private NumberEditor       doubleEditor;

    private NumberEditor       floatEditor;

    private SpinnerNumberModel intModel;

    private SpinnerNumberModel longModel;

    private SpinnerNumberModel byteModel;

    private SpinnerNumberModel shortModel;

    private SpinnerNumberModel doubleModel;

    private SpinnerNumberModel floatModel;

    public ExtSpinnerColumn(final String name) {
        this(name, null);

    }

    public ExtSpinnerColumn(final String name, final ExtTableModel<E> table) {
        super(name, table);

        this.editor = new JSpinner();

        this.intEditor = new JSpinner.NumberEditor(this.editor, "#");
        this.longEditor = this.intEditor;
        this.byteEditor = this.intEditor;
        this.shortEditor = this.intEditor;
        this.doubleEditor = new JSpinner.NumberEditor(this.editor, "###.##");
        this.floatEditor = this.doubleEditor;

        this.intModel = new SpinnerNumberModel(0, Integer.MIN_VALUE, Integer.MAX_VALUE, 1);
        this.longModel = new SpinnerNumberModel(0l, Long.MIN_VALUE, Long.MAX_VALUE, 1l);
        this.byteModel = new SpinnerNumberModel((byte) 0, Byte.MIN_VALUE, Byte.MAX_VALUE, (byte) 1);
        this.shortModel = new SpinnerNumberModel((short) 0, Short.MIN_VALUE, Short.MAX_VALUE, (short) 1);
        this.doubleModel = new SpinnerNumberModel(0.0d, Long.MIN_VALUE, Long.MAX_VALUE, 1.0d);
        this.floatModel = new SpinnerNumberModel(0.0f, Long.MIN_VALUE, Long.MAX_VALUE, 1.0f);

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

    @Override
    public void configureEditorComponent(final E value, final boolean isSelected, final int row, final int column) {

        final Number n = this.getNumber(value);
        this.editor.setModel(this.getModel(value, n));
        this.editor.setEditor(this.getEditor(value, n, this.editor));
        this.editor.setValue(n);

    }

    public NumberEditor getByteEditor() {
        return this.byteEditor;
    }

    public SpinnerNumberModel getByteModel() {
        return this.byteModel;
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

    public NumberEditor getDoubleEditor() {
        return this.doubleEditor;
    }

    public SpinnerNumberModel getDoubleModel() {
        return this.doubleModel;
    }

    /**
     * @param value
     * @param n
     * @param editor2
     * @return
     */
    protected DefaultEditor getEditor(final E value, final Number n, final JSpinner editor2) {
        if (Clazz.isDouble(n.getClass())) {
            return this.getDoubleEditor();
        } else if (Clazz.isFloat(n.getClass())) {
            return this.getFloatEditor();
        } else if (Clazz.isLong(n.getClass())) {
            return this.getLongEditor();
        } else if (Clazz.isInteger(n.getClass())) {
            return this.getIntEditor();
        } else if (Clazz.isShort(n.getClass())) {
            return this.getShortEditor();
        } else if (Clazz.isByte(n.getClass())) {
            return this.getByteEditor();
        } else {
            return this.getLongEditor();
        }

    }

    /**
     * @return
     */
    @Override
    public JComponent getEditorComponent(final E value, final boolean isSelected, final int row, final int column) {
        return this.editor;
    }

    public NumberEditor getFloatEditor() {
        return this.floatEditor;
    }

    public SpinnerNumberModel getFloatModel() {
        return this.floatModel;
    }

    public NumberEditor getIntEditor() {
        return this.intEditor;
    }

    public SpinnerNumberModel getIntModel() {
        return this.intModel;
    }

    public NumberEditor getLongEditor() {
        return this.longEditor;
    }

    public SpinnerNumberModel getLongModel() {
        return this.longModel;
    }

    /**
     * @param value
     * @param b
     *            TODO
     * @return
     */
    protected SpinnerNumberModel getModel(final E value, final Number n) {
        if (Clazz.isDouble(n.getClass())) {
            return this.getDoubleModel();
        } else if (Clazz.isFloat(n.getClass())) {
            return this.getFloatModel();
        } else if (Clazz.isLong(n.getClass())) {
            return this.getLongModel();
        } else if (Clazz.isInteger(n.getClass())) {
            return this.getIntModel();
        } else if (Clazz.isShort(n.getClass())) {
            return this.getShortModel();
        } else if (Clazz.isByte(n.getClass())) {
            return this.getByteModel();
        } else {
            return this.getLongModel();
        }
    }

    /**
     * @param value
     * @return
     */
    abstract protected Number getNumber(final E value);

    /**
     * @return
     */
    @Override
    public JComponent getRendererComponent(final E value, final boolean isSelected, final boolean hasFocus, final int row, final int column) {
        return this.renderer;
    }

    public NumberEditor getShortEditor() {
        return this.shortEditor;
    }

    public SpinnerNumberModel getShortModel() {
        return this.shortModel;
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

    @Override
    public void resetEditor() {
        this.editor.setBorder(null);
    }

    @Override
    public void resetRenderer() {

        super.resetRenderer();
        this.renderer.setHorizontalAlignment(SwingConstants.RIGHT);
    }

    public void setByteEditor(final NumberEditor byteEditor) {
        this.byteEditor = byteEditor;
    }

    public void setByteModel(final SpinnerNumberModel byteModel) {
        this.byteModel = byteModel;
    }

    public void setDoubleEditor(final NumberEditor doubleEditor) {
        this.doubleEditor = doubleEditor;
    }

    public void setDoubleModel(final SpinnerNumberModel doubleModel) {
        this.doubleModel = doubleModel;
    }

    public void setFloatEditor(final NumberEditor floatEditor) {
        this.floatEditor = floatEditor;
    }

    public void setFloatModel(final SpinnerNumberModel floatModel) {
        this.floatModel = floatModel;
    }

    public void setIntEditor(final NumberEditor intEditor) {
        this.intEditor = intEditor;
    }

    public void setIntModel(final SpinnerNumberModel intModel) {
        this.intModel = intModel;
    }

    public void setLongEditor(final NumberEditor longEditor) {
        this.longEditor = longEditor;
    }

    public void setLongModel(final SpinnerNumberModel longModel) {
        this.longModel = longModel;
    }

    /**
     * @param value
     * @param object
     */
    abstract protected void setNumberValue(Number value, E object);

    public void setShortEditor(final NumberEditor shortEditor) {
        this.shortEditor = shortEditor;
    }

    public void setShortModel(final SpinnerNumberModel shortModel) {
        this.shortModel = shortModel;
    }

    @Override
    final public void setValue(final Object value, final E object) {
        // TODO Auto-generated method stub
        this.setNumberValue((Number) value, object);
    }

}
