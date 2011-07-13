package org.appwork.utils.swing.table.columns;

import javax.swing.Icon;
import javax.swing.JComponent;
import javax.swing.SwingConstants;

import org.appwork.utils.swing.renderer.RenderLabel;
import org.appwork.utils.swing.table.ExtColumn;
import org.appwork.utils.swing.table.ExtDefaultRowSorter;
import org.appwork.utils.swing.table.ExtTableModel;

/**
 * Single icon column
 */
public abstract class ExtIconColumn<E> extends ExtColumn<E> {

    private RenderLabel       renderer;

    private static final long serialVersionUID = -5751315870107507714L;

    public ExtIconColumn(final String name) {
        this(name, null);
    }

    public ExtIconColumn(final String name, final ExtTableModel<E> table) {
        super(name, table);

        this.renderer = new RenderLabel();

        this.initIcons();

        this.setRowSorter(new ExtDefaultRowSorter<E>() {

            /**
             * sorts the icon by hashcode
             */
            @Override
            public int compare(final E o1, final E o2) {
                final Icon ic1 = ExtIconColumn.this.getIcon(o1);
                final Icon ic2 = ExtIconColumn.this.getIcon(o2);
                final int h1 = ic1 == null ? 0 : ic1.hashCode();
                final int h2 = ic2 == null ? 0 : ic2.hashCode();
                if (h1 == h2) { return 0; }
                if (this.getSortOrderIdentifier() == ExtColumn.SORT_ASC) {
                    return h1 > h2 ? -1 : 1;
                } else {
                    return h2 < h1 ? -1 : 1;
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
        this.renderer.setIcon(this.getIcon(value));

    }

    @Override
    public Object getCellEditorValue() {
        return null;
    }

    @Override
    public int getDefaultWidth() {

        return 30;
    }

    /**
     * @return
     */
    @Override
    public JComponent getEditorComponent(final E value, final boolean isSelected, final int row, final int column) {
        return null;
    }

    /**
     * Returns the icon for o1;
     */
    protected abstract Icon getIcon(E value);

    /**
     * Sets max width to 30. overwrite to set other maxsizes
     */
    @Override
    protected int getMaxWidth() {
        return this.getDefaultWidth();
    }

    @Override
    public int getMinWidth() {
        return this.getDefaultWidth();
    }

    /**
     * @return
     */
    @Override
    public JComponent getRendererComponent(final E value, final boolean isSelected, final boolean hasFocus, final int row, final int column) {
        return this.renderer;
    }

    @Override
    protected String getTooltipText(final E obj) {
        return null;
    }

    protected void initIcons() {
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
        this.renderer.setHorizontalAlignment(SwingConstants.CENTER);
        this.renderer.setBorder(ExtColumn.DEFAULT_BORDER);
    }

    @Override
    public void setValue(final Object value, final E object) {
    }

}
