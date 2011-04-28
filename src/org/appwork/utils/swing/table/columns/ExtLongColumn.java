package org.appwork.utils.swing.table.columns;


import javax.swing.BorderFactory;
import javax.swing.JComponent;

import org.appwork.utils.swing.renderer.RenderLabel;
import org.appwork.utils.swing.table.ExtColumn;
import org.appwork.utils.swing.table.ExtDefaultRowSorter;
import org.appwork.utils.swing.table.ExtTable;
import org.appwork.utils.swing.table.ExtTableModel;

public abstract class ExtLongColumn<E> extends ExtColumn<E> {

    private static final long serialVersionUID = -6917352290094392921L;
    private final RenderLabel label;

    
    public ExtLongColumn(final String name) {
        this(name, null);

    }
    public ExtLongColumn(String name, ExtTableModel<E> table) {
        super(name, table);

        label = new RenderLabel();
        label.setBorder(null);
        label.setOpaque(false);
        label.setBorder(BorderFactory.createEmptyBorder(0, 5, 0, 5));

        this.setRowSorter(new ExtDefaultRowSorter<E>() {
            @Override
            public int compare(E o1, E o2) {
                long l1=getLong(o1);
                long l2=getLong(o2);
                if (l1==l2) return 0;
                if (this.isSortOrderToggle()) {
                    return l1 > l2 ? -1 : 1;
                } else {
                    return l1 < l2 ? -1 : 1;
                }
            }

        });
    }

    protected abstract long getLong(E value);

    @Override
    public Object getCellEditorValue() {
        return null;
    }

    @Override
    public boolean isEditable(E obj) {
        return false;
    }

    @Override
    public boolean isEnabled(E obj) {
        return true;
    }

    @Override
    public boolean isSortable(Object obj) {
        return true;
    }

    @Override
    public void setValue(Object value, E object) {
    }

    @Override
    public JComponent getRendererComponent(ExtTable<E> table, E value, boolean isSelected, boolean hasFocus, int row, int column) {
        label.setText(getLong(value) + "");
        label.setEnabled(isEnabled(value));
        return label;
    }
}
