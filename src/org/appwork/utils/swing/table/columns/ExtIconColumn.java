package org.appwork.utils.swing.table.columns;

import java.awt.Component;

import javax.swing.BorderFactory;
import javax.swing.Icon;
import javax.swing.JTable;

import org.appwork.utils.swing.renderer.RenderLabel;
import org.appwork.utils.swing.table.ExtColumn;
import org.appwork.utils.swing.table.ExtDefaultRowSorter;
import org.appwork.utils.swing.table.ExtTableModel;

/**
 * Single icon column
 */
public abstract class ExtIconColumn<E> extends ExtColumn<E> {

    private RenderLabel label;

    public ExtIconColumn(String name, ExtTableModel<E> table) {
        super(name, table);

        label = new RenderLabel();
        label.setOpaque(false);
        label.setHorizontalAlignment(RenderLabel.CENTER);
        label.setBorder(BorderFactory.createEmptyBorder(0, 5, 0, 5));
        initIcons();

        this.setRowSorter(new ExtDefaultRowSorter<E>() {

            /**
             * sorts the icon by hashcode
             */
            @Override
            public int compare(E o1, E o2) {
                Icon ic1 = getIcon(o1);
                Icon ic2 = getIcon(o2);
                int h1 = ic1 == null ? 0 : ic1.hashCode();
                int h2 = ic2 == null ? 0 : ic2.hashCode();
                if (h1 == h2) return 0;
                if (this.isSortOrderToggle()) {
                    return h1 > h2 ? -1 : 1;
                } else {
                    return h2 < h1 ? -1 : 1;
                }
            }

        });
    }

    protected String getToolTip(E obj) {
        return null;
    }

    protected void initIcons() {
    }

    private static final long serialVersionUID = -5751315870107507714L;

    /**
     * Returns the icon for o1;
     */
    protected abstract Icon getIcon(E value);

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
    public boolean isSortable(E obj) {
        return true;
    }

    @Override
    public void setValue(Object value, E object) {
    }

    /**
     * Sets max width to 30. overwrite to set other maxsizes
     */
    @Override
    protected int getMaxWidth() {
        return 30;
    }

    @Override
    public int getMinWidth() {
        return 30;
    }

    @SuppressWarnings("unchecked")
    @Override
    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
        label.setIcon(getIcon((E) value));
        label.setToolTipText(getToolTip((E) value));
        label.setEnabled(isEnabled((E) value));
        return label;
    }

}
