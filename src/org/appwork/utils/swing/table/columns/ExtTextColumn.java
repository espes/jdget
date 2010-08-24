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
    protected RenderLabel     label;

    public ExtTextColumn(final String name, final ExtTableModel<E> table) {
        super(name, table);

        this.label = new RenderLabel();
        this.label.setBorder(BorderFactory.createEmptyBorder(0, 5, 0, 5));
        this.label.setOpaque(false);
        this.prepareTableCellRendererComponent(this.label);

        this.setRowSorter(new ExtDefaultRowSorter<E>() {

            @Override
            public int compare(final E o1, final E o2) {
                if (this.isSortOrderToggle()) {
                    return ExtTextColumn.this.getStringValue(o1).compareTo(ExtTextColumn.this.getStringValue(o2));
                } else {
                    return ExtTextColumn.this.getStringValue(o2).compareTo(ExtTextColumn.this.getStringValue(o1));
                }
            }

        });

    }

    @Override
    public Object getCellEditorValue() {
        return null;
    }

    /**
     * @param value
     * @return
     */
    protected Icon getIcon(final E value) {

        return null;
    }

    protected abstract String getStringValue(E value);

    @SuppressWarnings("unchecked")
    @Override
    public Component getTableCellRendererComponent(final JTable table, final Object value, final boolean isSelected, final boolean hasFocus, final int row, final int column) {
        this.prepareLabel((E) value);
        this.label.setText(this.getStringValue((E) value));
        this.label.setToolTipText(this.getToolTip((E) value));
        this.label.setEnabled(this.isEnabled((E) value));
        this.label.setIcon(this.getIcon((E) value));

        return this.label;

    }

    public String getToolTip(final E obj) {
        final String v = this.getStringValue(obj);
        if (v != null) {
            return "<html>" + v.replaceAll("\r\n", "<br>") + "</html>";
        } else {
            return v;
        }
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

        return true;
    }

    @Override
    protected boolean matchSearch(final E object, final Pattern pattern) {

        return pattern.matcher(this.getStringValue(object)).matches();

    }

    /**
     * @param value
     */
    protected void prepareLabel(final E value) {
        // TODO Auto-generated method stub

    }

    /**
     * Should be overwritten to prepare the componente for the TableCellRenderer
     * (e.g. setting tooltips)
     */
    protected void prepareTableCellRendererComponent(final JLabel jlr) {
    }

    @Override
    public void setValue(final Object value, final E object) {
    }

}
