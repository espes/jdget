package org.appwork.utils.swing.table.columns;

import java.awt.Component;
import java.text.DecimalFormat;

import javax.swing.BorderFactory;
import javax.swing.JTable;

import org.appwork.utils.swing.renderer.RenderLabel;
import org.appwork.utils.swing.table.ExtColumn;
import org.appwork.utils.swing.table.ExtDefaultRowSorter;
import org.appwork.utils.swing.table.ExtTableModel;

public abstract class ExtEuroColumn<E> extends ExtColumn<E> {

    private static final long serialVersionUID = 3468695684952592989L;
    private RenderLabel label;
    final private DecimalFormat format = new DecimalFormat("0.00");

    public ExtEuroColumn(String name, ExtTableModel<E> table) {
        super(name, table);
        this.label = new RenderLabel();
        label.setBorder(null);
        label.setOpaque(false);
        label.setBorder(BorderFactory.createEmptyBorder(0, 5, 0, 5));

        this.setRowSorter(new ExtDefaultRowSorter<E>() {
            /**
             * sorts the icon by hashcode
             */
            @Override
            public int compare(Object o1, Object o2) {
                if (getCent(o1) == getCent(o2)) return 0;
                if (this.isSortOrderToggle()) {
                    return getCent(o1) > getCent(o2) ? -1 : 1;
                } else {
                    return getCent(o1) < getCent(o2) ? -1 : 1;
                }
            }

        });
    }

    abstract protected long getCent(Object o2);

    @Override
    public Object getCellEditorValue() {
        return null;
    }

    @Override
    public boolean isEditable(Object obj) {
        return false;
    }

    @Override
    public boolean isEnabled(Object obj) {
        return true;
    }

    @Override
    public boolean isSortable(Object obj) {
        return true;
    }

    @Override
    public void setValue(Object value, Object object) {
    }

    @Override
    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {

        try {
            label.setText(format.format(getCent(value) / 100.0f) + " €");
        } catch (Exception e) {
            label.setText(format.format("0.0f") + " €");
        }
        label.setEnabled(isEnabled(value));
        return label;
    }
}
