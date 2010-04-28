package org.appwork.utils.swing.table.columns;

import java.awt.Component;
import java.util.regex.Pattern;

import javax.swing.BorderFactory;
import javax.swing.Icon;
import javax.swing.JLabel;
import javax.swing.JTable;

import org.appwork.utils.swing.renderer.RenderLabel;
import org.appwork.utils.swing.table.ExtColumn;
import org.appwork.utils.swing.table.ExtDefaultRowSorter;
import org.appwork.utils.swing.table.ExtTableModel;

public abstract class ExtTextColumn<E> extends ExtColumn<E> {

    private static final long serialVersionUID = 2114805529462086691L;
    private RenderLabel label;

    public ExtTextColumn(String name, ExtTableModel<E> table) {
        super(name, table);

        label = new RenderLabel();
        label.setBorder(BorderFactory.createEmptyBorder(0, 5, 0, 5));
        label.setOpaque(false);
        prepareTableCellRendererComponent(label);

        this.setRowSorter(new ExtDefaultRowSorter<E>() {

            @Override
            public int compare(E o1, E o2) {
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

    protected abstract String getStringValue(E value);

    @Override
    public Object getCellEditorValue() {
        return null;
    }

    @Override
    public boolean isEditable(E obj) {
        return false;
    }

    public String getToolTip(E obj) {
        return null;
    }

    @SuppressWarnings("unchecked")
    @Override
    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
        label.setText(getStringValue((E) value));
        label.setToolTipText(getToolTip((E) value));
        label.setIcon(getIcon((E) value));
        return label;

        // isSelected = false;
        // return ((ExtTable) table).getLafCellRenderer(row,
        // column).getTableCellRendererComponent(table, getStringValue(value),
        // isSelected, false, row, column);
    }

    /**
     * @param value
     * @return
     */
    protected Icon getIcon(E value) {
        // TODO Auto-generated method stub
        return null;
    }

    protected boolean matchSearch(E object, Pattern pattern) {

        return pattern.matcher(getStringValue(object)).matches();

    }

    @Override
    public void setValue(Object value, E object) {
    }

    @Override
    public boolean isEnabled(E obj) {
        // TODO Auto-generated method stub
        return true;
    }

    @Override
    public boolean isSortable(E obj) {
        // TODO Auto-generated method stub
        return true;
    }

}
