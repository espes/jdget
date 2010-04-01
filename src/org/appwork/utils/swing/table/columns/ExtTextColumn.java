package org.appwork.utils.swing.table.columns;

import java.awt.Component;

import javax.swing.JLabel;
import javax.swing.JTable;

import org.appwork.utils.swing.renderer.RenderLabel;
import org.appwork.utils.swing.table.ExtColumn;
import org.appwork.utils.swing.table.ExtDefaultRowSorter;
import org.appwork.utils.swing.table.ExtTableModel;

public abstract class ExtTextColumn extends ExtColumn {

    private static final long serialVersionUID = 2114805529462086691L;
    private RenderLabel label;

    public ExtTextColumn(String name, ExtTableModel table) {
        super(name, table);

        label = new RenderLabel();
        label.setBorder(null);
        label.setOpaque(false);
        prepareTableCellRendererComponent(label);

        this.setRowSorter(new ExtDefaultRowSorter() {

            @Override
            public int compare(Object o1, Object o2) {
                if (this.isSortOrderToggle()) {
                    return getStringValue(o1).compareTo(getStringValue(o2));
                } else {
                    return getStringValue(o2).compareTo(getStringValue(o1));
                }
            }

        });
    }

    /**
     * Should be overwritten to prepare the componente for the TableCellRenderer
     * (e.g. setting tooltips)
     */
    protected void prepareTableCellRendererComponent(JLabel jlr) {
    }

    protected abstract String getStringValue(Object value);

    @Override
    public Object getCellEditorValue() {
        return null;
    }

    @Override
    public boolean isEditable(Object obj) {
        return false;
    }

    @Override
    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
        label.setText(getStringValue(value));
        return label;
        // isSelected = false;
        // return ((ExtTable) table).getLafCellRenderer(row,
        // column).getTableCellRendererComponent(table, getStringValue(value),
        // isSelected, false, row, column);
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
        // TODO Auto-generated method stub
        return true;
    }

}
