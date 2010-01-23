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

import org.appwork.utils.storage.DatabaseInterface;
import org.appwork.utils.swing.table.ExtColumn;
import org.appwork.utils.swing.table.ExtDefaultRowSorter;
import org.appwork.utils.swing.table.ExtTableModel;

public abstract class ExtComboColumn extends ExtColumn implements ActionListener {

    private static final long serialVersionUID = 2114805529462086691L;

    private JComboBox comboBoxRend;
    private JComboBox comboBoxEdit;
    private int selection;
    protected DefaultListCellRenderer renderer;

    private ComboBoxModel dataModel;

    public ExtComboColumn(String name, ExtTableModel table, DatabaseInterface database, ComboBoxModel model) {
        super(name, table, database);
        if (model == null) model = new DefaultComboBoxModel();
        dataModel = model;
        comboBoxRend = new JComboBox(dataModel) {
            public void addActionListener(ActionListener l) {
                listenerList.add(ActionListener.class, l);
            }
        };

        comboBoxEdit = new JComboBox(dataModel);
        comboBoxEdit.setBorder(BorderFactory.createEmptyBorder(3, 0, 3, 0));
        comboBoxRend.setBorder(BorderFactory.createEmptyBorder(3, 0, 3, 0));

        this.setRowSorter(new ExtDefaultRowSorter() {

            @Override
            public int compare(Object o1, Object o2) {
                if (getComboBoxItem(o1) == getComboBoxItem(o2)) return 0;
                if (this.isSortOrderToggle()) {
                    return getComboBoxItem(o1) > getComboBoxItem(o2) ? 1 : -1;
                } else {
                    return getComboBoxItem(o2) > getComboBoxItem(o1) ? 1 : -1;

                }
            }
        });
    }

    public void setRenderer() {
        comboBoxRend.setRenderer(renderer);
        comboBoxEdit.setRenderer(renderer);
    }

    protected abstract int getComboBoxItem(Object value);

    @Override
    public boolean isEditable(Object obj) {
        return false;
    }

    @Override
    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
        selection = getComboBoxItem(value);
        if (selection < 0) { return super.getTableCellRendererComponent(table, "", false, hasFocus, row, column); }
        comboBoxRend.setModel(updateModel(dataModel, value));

        comboBoxEdit.removeActionListener(this);
        comboBoxEdit.setToolTipText(getTooltip(value));
        comboBoxRend.setSelectedIndex(getComboBoxItem(value));
        comboBoxEdit.addActionListener(this);

        comboBoxRend.setEnabled(isEnabled(value));
        return comboBoxRend;
    }

    /**
     * overwrite this method to implement different dropdown boxes
     * 
     * @param dataModel
     */
    public ComboBoxModel updateModel(ComboBoxModel dataModel, Object value) {
        return dataModel;

    }

    @Override
    public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
        selection = getComboBoxItem(value);
        if (selection < 0) { return super.getTableCellEditorComponent(table, "", false, row, column); }
        comboBoxEdit.setModel(updateModel(dataModel, value));
        comboBoxEdit.removeActionListener(this);
        comboBoxEdit.setToolTipText(getTooltip(value));
        comboBoxEdit.setSelectedIndex(selection);
        comboBoxEdit.addActionListener(this);
        comboBoxEdit.setEnabled(isEnabled(value));
        return comboBoxEdit;
    }

    public String getTooltip(Object value) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void setValue(Object value, Object object) {
    }

    @Override
    public boolean isEnabled(Object obj) {
        // TODO Auto-generated method stub
        return true;
    }

    @Override
    public boolean isSortable(Object obj) {
        return false;
    }

    @Override
    public Object getCellEditorValue() {
        return comboBoxEdit.getSelectedIndex();
    }

    public void actionPerformed(ActionEvent e) {

        comboBoxEdit.removeActionListener(this);
        this.stopCellEditing();
    }

}
