package org.appwork.utils.swing.table.columns;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.ComboBoxModel;
import javax.swing.DefaultComboBoxModel;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JComboBox;
import javax.swing.JTable;
import javax.swing.ListCellRenderer;

import org.appwork.utils.swing.table.ExtColumn;
import org.appwork.utils.swing.table.ExtDefaultRowSorter;
import org.appwork.utils.swing.table.ExtTableModel;

public abstract class ExtComboColumn<E> extends ExtColumn<E> implements ActionListener {

    private static final long         serialVersionUID = 2114805529462086691L;

    private JComboBox                 comboBoxRend;
    private JComboBox                 comboBoxEdit;
    private int                       selection;
    protected DefaultListCellRenderer renderer;

    private ComboBoxModel             dataModel;

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
        this.comboBoxEdit.setBorder(BorderFactory.createEmptyBorder(3, 0, 3, 0));
        this.comboBoxRend.setBorder(BorderFactory.createEmptyBorder(3, 0, 3, 0));
        this.comboBoxEdit.putClientProperty("JComboBox.isTableCellEditor", Boolean.TRUE);
        final ListCellRenderer t = this.comboBoxEdit.getRenderer();

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

    @SuppressWarnings("unchecked")
    @Override
    public Component getTableCellEditorComponent(final JTable table, final Object value, final boolean isSelected, final int row, final int column) {
        this.selection = this.getComboBoxItem((E) value);
        if (this.selection < 0) { return super.getTableCellEditorComponent(table, "", false, row, column); }
        this.comboBoxEdit.setModel(this.updateModel(this.dataModel, value));
        this.comboBoxEdit.removeActionListener(this);
        this.comboBoxEdit.setToolTipText(this.getTooltip(value));
        this.comboBoxEdit.setSelectedIndex(this.selection);
        this.comboBoxEdit.addActionListener(this);
        this.comboBoxEdit.setEnabled(this.isEnabled(value));
        this.adaptRowHighlighters((E) value, this.comboBoxEdit, isSelected, true, row);
        this.adaptRowHighlighters((E) value, this.comboBoxRend, isSelected, true, row);
        this.comboBoxEdit.setOpaque(false);
        this.comboBoxRend.setOpaque(false);
        return this.comboBoxEdit;
    }

    @SuppressWarnings("unchecked")
    @Override
    public Component getTableCellRendererComponent(final JTable table, final Object value, final boolean isSelected, final boolean hasFocus, final int row, final int column) {
        this.selection = this.getComboBoxItem((E) value);
        if (this.selection < 0) { return super.getTableCellRendererComponent(table, "", false, hasFocus, row, column); }
        this.comboBoxRend.setModel(this.updateModel(this.dataModel, value));

        this.comboBoxEdit.removeActionListener(this);
        this.comboBoxEdit.setToolTipText(this.getTooltip(value));
        this.comboBoxRend.setSelectedIndex(this.getComboBoxItem((E) value));
        this.comboBoxEdit.addActionListener(this);

        this.comboBoxRend.setEnabled(this.isEnabled(value));
        this.adaptRowHighlighters((E) value, this.comboBoxRend, isSelected, hasFocus, row);
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
    public boolean isEnabled(final Object obj) {

        return true;
    }

    @Override
    public boolean isSortable(final Object obj) {
        return false;
    }

    public void setRenderer() {
        this.comboBoxRend.setRenderer(this.renderer);
        this.comboBoxEdit.setRenderer(this.renderer);
    }

    @Override
    public void setValue(final Object value, final Object object) {
    }

    /**
     * overwrite this method to implement different dropdown boxes
     * 
     * @param dataModel
     */
    public ComboBoxModel updateModel(final ComboBoxModel dataModel, final Object value) {
        return dataModel;

    }

}
