package org.appwork.swing.exttable.columns;

import java.text.DecimalFormat;

import javax.swing.JComponent;

import org.appwork.swing.exttable.ExtColumn;
import org.appwork.swing.exttable.ExtDefaultRowSorter;
import org.appwork.swing.exttable.ExtTableModel;
import org.appwork.utils.swing.renderer.RenderLabel;

public abstract class ExtEuroColumn<E> extends ExtColumn<E> {

    private static final long   serialVersionUID = 3468695684952592989L;
    private RenderLabel         renderer;
    final private DecimalFormat format           = new DecimalFormat("0.00");

    public ExtEuroColumn(final String name, final ExtTableModel<E> table) {
        super(name, table);
        this.renderer = new RenderLabel();

        this.setRowSorter(new ExtDefaultRowSorter<E>() {
            /**
             * sorts the icon by hashcode
             */
            @Override
            public int compare(final Object o1, final Object o2) {
                if (ExtEuroColumn.this.getCent(o1) == ExtEuroColumn.this.getCent(o2)) { return 0; }
                if (this.getSortOrderIdentifier() == ExtColumn.SORT_ASC) {
                    return ExtEuroColumn.this.getCent(o1) > ExtEuroColumn.this.getCent(o2) ? -1 : 1;
                } else {
                    return ExtEuroColumn.this.getCent(o1) < ExtEuroColumn.this.getCent(o2) ? -1 : 1;
                }
            }

        });
    }

    @Override
    public void configureRendererComponent(final E value, final boolean isSelected, final boolean hasFocus, final int row, final int column) {

        try {
            this.renderer.setText(this.format.format(this.getCent(value) / 100.0f) + " €");
        } catch (final Exception e) {
            this.renderer.setText(this.format.format("0.0f") + " €");
        }

    }

    @Override
    public Object getCellEditorValue() {
        return null;
    }

    abstract protected long getCent(Object o2);

    /**
     * @return
     */
    @Override
    public JComponent getEditorComponent(final E value, final boolean isSelected, final int row, final int column) {
        return null;
    }

    /**
     * @return
     */
    @Override
    public JComponent getRendererComponent(final E value, final boolean isSelected, final boolean hasFocus, final int row, final int column) {
        return this.renderer;
    }

    @Override
    public boolean isEditable(final Object obj) {
        return false;
    }

    @Override
    public boolean isEnabled(final Object obj) {
        return true;
    }

    @Override
    public boolean isSortable(final Object obj) {
        return true;
    }

    @Override
    public void resetEditor() {
        // TODO Auto-generated method stub

    }

    @Override
    public void resetRenderer() {

        this.renderer.setOpaque(false);
        this.renderer.setBorder(ExtColumn.DEFAULT_BORDER);

    }

    @Override
    public void setValue(final Object value, final Object object) {
    }
}
