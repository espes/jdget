package org.appwork.swing.exttable.columns;

import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.ComboBoxModel;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.ListCellRenderer;
import javax.swing.border.CompoundBorder;

import org.appwork.swing.exttable.ExtColumn;
import org.appwork.swing.exttable.ExtDefaultRowSorter;
import org.appwork.swing.exttable.ExtTableModel;
import org.appwork.utils.swing.renderer.RendererComboBox;

public abstract class ExtComboColumn<E> extends ExtTextColumn<E> implements ActionListener {

    private static final long    serialVersionUID = 2114805529462086691L;

    protected JComboBox          editor;

    private ComboBoxModel        dataModel;

    private DefaultComboBoxModel emptyModel;

    protected JComboBox          renderer;

    protected CompoundBorder     border;

    private Color defaultColor;

    public ExtComboColumn(final String name, final ComboBoxModel model) {
        this(name, null, model);

    }

    public ExtComboColumn(final String name, final ExtTableModel<E> table, ComboBoxModel model) {
        super(name, table);
        this.emptyModel = new DefaultComboBoxModel();
        if (model == null) {
            model = this.emptyModel;
        }
        defaultColor = new JLabel().getForeground();
        this.dataModel = model;
        this.renderer = new RendererComboBox();
        this.renderer.putClientProperty("JComboBox.isTableCellEditor", Boolean.TRUE);
        // comboBoxEdit.setRenderer(new DefaultCellEditor(comboBox))

        // this.comboBoxRendereer.setBorder(BorderFactory.createCompoundBorder(this.comboBoxRendereer.getBorder(),
        // BorderFactory.createEmptyBorder(2, 2, 2, 2)));

        this.editor = new JComboBox(model);
        this.editor.putClientProperty("JComboBox.isTableCellEditor", Boolean.TRUE);
        this.border = BorderFactory.createCompoundBorder(BorderFactory.createEmptyBorder(1, 1, 2, 1), this.editor.getBorder());

        // comboBoxEdit.setRenderer(new DefaultCellEditor(comboBox))

        this.setRowSorter(new ExtDefaultRowSorter<E>() {

            @Override
            public int compare(final E o1, final E o2) {
                if (ExtComboColumn.this.getSelectedIndex(o1) == ExtComboColumn.this.getSelectedIndex(o2)) { return 0; }
                if (this.getSortOrderIdentifier() == ExtColumn.SORT_ASC) {
                    return ExtComboColumn.this.getSelectedIndex(o1) > ExtComboColumn.this.getSelectedIndex(o2) ? 1 : -1;
                } else {
                    return ExtComboColumn.this.getSelectedIndex(o2) > ExtComboColumn.this.getSelectedIndex(o1) ? 1 : -1;

                }
            }
        });
    }

    @Override
    public void actionPerformed(final ActionEvent e) {

        this.editor.removeActionListener(this);
        this.stopCellEditing();
    }

    @Override
    public void configureEditorComponent(final E value, final boolean isSelected, final int row, final int column) {
        final int selection = this.getSelectedIndex(value);
        this.editor.removeActionListener(this);

        final ComboBoxModel newModel = this.updateModel(this.dataModel, value);
        newModel.setSelectedItem(newModel.getElementAt(selection));
        if (this.editor.getModel() != newModel) {
            this.editor.setModel(newModel);
        }

        // this.comboBoxEdit.setSelectedIndex(selection);
        this.editor.addActionListener(this);
        // this.comboBoxEdit.setEnabled(this.isEnabled(value));

    }

    @Override
    public void configureRendererComponent(final E value, final boolean isSelected, final boolean hasFocus, final int row, final int column) {
        // TODO Auto-generated method stub
        if (!this.isEnabled(value) || !this.isEditable(value)) {
            super.configureRendererComponent(value, isSelected, hasFocus, row, column);
        } else {

            this.renderer.getModel().setSelectedItem(this.updateModel(this.dataModel, value).getElementAt(this.getSelectedIndex(value)));
        }
    }

    @Override
    public Object getCellEditorValue() {
        return this.editor.getSelectedIndex();
    }

    public ComboBoxModel getDataModel() {
        return this.dataModel;
    }

    @Override
    public JComponent getEditorComponent(final E value, final boolean isSelected, final int row, final int column) {
        // TODO Auto-generated method stub
        return this.editor;
    }

    /**
     * @return
     */
    public ListCellRenderer getRenderer() {
        return this.editor.getRenderer();
    }

    @Override
    public JComponent getRendererComponent(final E value, final boolean isSelected, final boolean hasFocus, final int row, final int column) {
        // TODO Auto-generated method stub
        if (!this.isEnabled(value) || !this.isEditable(value)) { return super.getRendererComponent(value, isSelected, hasFocus, row, column); }
        return this.renderer;
    }

    protected abstract int getSelectedIndex(E value);

    @Override
    public String getStringValue(final E value) {
        // TODO Auto-generated method stub
        return this.updateModel(this.dataModel, value).getElementAt(this.getSelectedIndex(value)) + "";
    }

    @Override
    protected String getTooltipText(final E obj) {
        // TODO Auto-generated method stub
        return super.getTooltipText(obj);
    }

    @Override
    public boolean isEditable(final E obj) {
        return this.dataModel.getSize() > 1;
    }

    @Override
    public boolean isEnabled(final E obj) {
        return true;
    }

    // @Override
    // protected String getStringValue(final E value) {
    // return this.updateModel(this.dataModel,
    // value).getElementAt(this.getComboBoxItem(value)) + "";
    // }

    @Override
    public boolean isSortable(final E obj) {
        return true;
    }

    @Override
    public void resetEditor() {
        this.editor.setBorder(this.border);

    }

    @Override
    public void resetRenderer() {
        super.resetRenderer();
        this.renderer.setBorder(border);
        this.editor.setBorder(border);
        this.renderer.setOpaque(true);
        this.renderer.setBackground(null);
        renderer.setOpaque(false);
        renderer.setForeground(defaultColor);
        editor.setForeground(defaultColor);
        editor.setOpaque(false);

    }

    public void setRenderer(final ListCellRenderer renderer) {
        this.renderer.setRenderer(renderer);
        this.editor.setRenderer(renderer);

    }

    /**
     * @param value
     */
    protected abstract void setSelectedIndex(int value, E object);

    @Override
    final public void setValue(final Object value, final E object) {
        this.setSelectedIndex((Integer) value, object);
    }

    /**
     * overwrite this method to implement different dropdown boxes
     * 
     * @param dataModel
     */
    public ComboBoxModel updateModel(final ComboBoxModel dataModel, final E value) {
        return dataModel;

    }

}
