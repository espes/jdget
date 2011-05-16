package org.appwork.utils.swing.table.columns;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.ComboBoxModel;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.ListCellRenderer;

import org.appwork.utils.swing.table.ExtColumn;
import org.appwork.utils.swing.table.ExtDefaultRowSorter;
import org.appwork.utils.swing.table.ExtTable;
import org.appwork.utils.swing.table.ExtTableModel;

public abstract class ExtComboColumn<E> extends ExtColumn<E> implements ActionListener {

    private static final long serialVersionUID = 2114805529462086691L;

    private JComboBox         comboBoxRend;
    private JComboBox         comboBoxEdit;
    private int               selection;

    private ComboBoxModel     dataModel;

    public ExtComboColumn(final String name, final ComboBoxModel model) {
        this(name, null, model);

    }

    public ExtComboColumn(final String name, final ExtTableModel<E> table, ComboBoxModel model) {
        super(name, table);
        if (model == null) {
            model = new DefaultComboBoxModel();
        }
        this.dataModel = model;
        this.comboBoxRend = new JComboBox(this.dataModel) {
            private static final long serialVersionUID = -7223814300276557968L;

            @Override
            public void addActionListener(final ActionListener l) {
                this.listenerList.add(ActionListener.class, l);
            }
        };

        this.comboBoxEdit = new JComboBox(this.dataModel);

        // comboBoxEdit.setRenderer(new DefaultCellEditor(comboBox))

        this.comboBoxEdit.setBorder(BorderFactory.createCompoundBorder(this.comboBoxEdit.getBorder(), BorderFactory.createEmptyBorder(1, 1, 1, 1)));
        this.comboBoxRend.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createEmptyBorder(1, 1, 1, 1), this.comboBoxRend.getBorder()));
        this.comboBoxEdit.putClientProperty("JComboBox.isTableCellEditor", Boolean.TRUE);

        this.comboBoxRend.putClientProperty("JComboBox.isTableCellEditor", Boolean.TRUE);
        this.setRowSorter(new ExtDefaultRowSorter<E>() {

            @Override
            public int compare(final E o1, final E o2) {
                if (ExtComboColumn.this.getComboBoxItem(o1) == ExtComboColumn.this.getComboBoxItem(o2)) { return 0; }
                if (this.isSortOrderToggle()) {
                    return ExtComboColumn.this.getComboBoxItem(o1) > ExtComboColumn.this.getComboBoxItem(o2) ? 1 : -1;
                } else {
                    return ExtComboColumn.this.getComboBoxItem(o2) > ExtComboColumn.this.getComboBoxItem(o1) ? 1 : -1;

                }
            }
        });
    }

    public void actionPerformed(final ActionEvent e) {

        this.comboBoxEdit.removeActionListener(this);
        this.stopCellEditing();
    }

    @Override
    public Object getCellEditorValue() {
        return this.comboBoxEdit.getSelectedIndex();
    }

    protected abstract int getComboBoxItem(E value);

    @Override
    public JComponent getEditorComponent(final ExtTable<E> table, final E value, final boolean isSelected, final int row, final int column) {
        this.selection = this.getComboBoxItem(value);

        this.comboBoxEdit.setModel(this.updateModel(this.dataModel, value));
        this.comboBoxEdit.removeActionListener(this);
        this.comboBoxEdit.setToolTipText(this.getTooltip(value));
        this.comboBoxEdit.setSelectedIndex(this.selection);
        this.comboBoxEdit.addActionListener(this);
        this.comboBoxEdit.setEnabled(this.isEnabled(value));

        this.comboBoxEdit.setOpaque(false);
        this.comboBoxRend.setOpaque(false);
        return this.comboBoxEdit;
    }

    /**
     * @return
     */
    public ListCellRenderer getRenderer() {
        return this.comboBoxRend.getRenderer();
    }

    @Override
    public JComponent getRendererComponent(final ExtTable<E> table, final E value, final boolean isSelected, final boolean hasFocus, final int row, final int column) {
        this.selection = this.getComboBoxItem(value);

        this.comboBoxRend.setModel(this.updateModel(this.dataModel, value));

        this.comboBoxEdit.removeActionListener(this);
        this.comboBoxEdit.setToolTipText(this.getTooltip(value));
        this.comboBoxRend.setSelectedIndex(this.getComboBoxItem(value));
        this.comboBoxEdit.addActionListener(this);

        this.comboBoxRend.setEnabled(this.isEnabled(value));

        return this.comboBoxRend;
    }

    public String getTooltip(final Object value) {

        return null;
    }

    @Override
    public boolean isEditable(final E obj) {
        return false;
    }

    @Override
    public boolean isEnabled(final E obj) {
        return true;
    }

    @Override
    public boolean isSortable(final E obj) {
        return false;
    }

    public void setRenderer(final ListCellRenderer renderer) {
        this.comboBoxRend.setRenderer(renderer);
        this.comboBoxEdit.setRenderer(renderer);
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
