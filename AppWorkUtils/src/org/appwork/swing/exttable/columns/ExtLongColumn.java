package org.appwork.swing.exttable.columns;

import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.SwingConstants;
import javax.swing.border.Border;

import org.appwork.swing.exttable.ExtColumn;
import org.appwork.swing.exttable.ExtDefaultRowSorter;
import org.appwork.swing.exttable.ExtTableModel;
import org.appwork.utils.swing.renderer.RenderLabel;

public abstract class ExtLongColumn<E> extends ExtColumn<E> {

    private static final long   serialVersionUID = -6917352290094392921L;
    protected final RenderLabel renderer;
    private final Border        defaultBorder    = BorderFactory.createEmptyBorder(0, 5, 0, 5);

    public ExtLongColumn(final String name) {
        this(name, null);

    }

    public ExtLongColumn(final String name, final ExtTableModel<E> table) {
        super(name, table);

        this.renderer = new RenderLabel();
        this.renderer.setHorizontalAlignment(SwingConstants.RIGHT);
        this.setRowSorter(new ExtDefaultRowSorter<E>() {
            @Override
            public int compare(final E o1, final E o2) {
                final long l1 = ExtLongColumn.this.getLong(o1);
                final long l2 = ExtLongColumn.this.getLong(o2);
                if (l1 == l2) { return 0; }
                if (this.getSortOrderIdentifier() == ExtColumn.SORT_ASC) {
                    return l1 > l2 ? -1 : 1;
                } else {
                    return l1 < l2 ? -1 : 1;
                }
            }

        });
    }

    @Override
    public void configureEditorComponent(final E value, final boolean isSelected, final int row, final int column) {
        // TODO Auto-generated method stub

    }

    @Override
    public void configureRendererComponent(final E value, final boolean isSelected, final boolean hasFocus, final int row, final int column) {
        this.renderer.setText(this.getLong(value) + "");
        this.renderer.setEnabled(this.isEnabled(value));

    }

    @Override
    public Object getCellEditorValue() {
        return null;
    }

    /**
     * @return
     */
    @Override
    public JComponent getEditorComponent(final E value, final boolean isSelected, final int row, final int column) {
        return null;
    }

    protected abstract long getLong(E value);

    /**
     * @return
     */
    @Override
    public JComponent getRendererComponent(final E value, final boolean isSelected, final boolean hasFocus, final int row, final int column) {
        return this.renderer;
    }

    @Override
    protected String getTooltipText(final E obj) {

        return this.getLong(obj) + "";
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
    public void resetEditor() {
        // TODO Auto-generated method stub

    }

    @Override
    public void resetRenderer() {

        this.renderer.setOpaque(false);
        this.renderer.setBorder(this.defaultBorder);

    }

    @Override
    public void setValue(final Object value, final E object) {
    }
}
