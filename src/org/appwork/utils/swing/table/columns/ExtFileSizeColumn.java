package org.appwork.utils.swing.table.columns;

import java.text.DecimalFormat;
import java.text.FieldPosition;

import javax.swing.JComponent;
import javax.swing.SwingConstants;

import org.appwork.utils.swing.renderer.RenderLabel;
import org.appwork.utils.swing.table.ExtColumn;
import org.appwork.utils.swing.table.ExtDefaultRowSorter;
import org.appwork.utils.swing.table.ExtTableModel;

public abstract class ExtFileSizeColumn<E> extends ExtColumn<E> {

    /**
     * 
     */
    private static final long serialVersionUID = -5812486934156037376L;
    protected RenderLabel     renderer;
    protected long            sizeValue;
    private StringBuffer      sb;
    private DecimalFormat     formatter;

    /**
     * @param createtablemodel_column_size
     */
    public ExtFileSizeColumn(final String name) {
        this(name, null);
    }

    public ExtFileSizeColumn(final String name, final ExtTableModel<E> table) {
        super(name, table);
        this.renderer = new RenderLabel();
        this.renderer.setHorizontalAlignment(SwingConstants.RIGHT);
        this.setRowSorter(new ExtDefaultRowSorter<E>() {
            /**
             * sorts the icon by hashcode
             */
            @Override
            public int compare(final E o1, final E o2) {
                final long s1 = ExtFileSizeColumn.this.getBytes(o1);
                final long s2 = ExtFileSizeColumn.this.getBytes(o2);
                if (s1 == s2) { return 0; }
                if (this.getSortOrderIdentifier() != ExtColumn.SORT_ASC) {
                    return s1 > s2 ? -1 : 1;
                } else {
                    return s1 < s2 ? -1 : 1;
                }
            }

        });

        this.sb = new StringBuffer();

        this.formatter = new DecimalFormat("0.00") {

            /**
             * 
             */
            private static final long serialVersionUID = 1L;

            @Override
            public StringBuffer format(final double number, final StringBuffer result, final FieldPosition pos) {
                ExtFileSizeColumn.this.sb.setLength(0);
                return super.format(number, ExtFileSizeColumn.this.sb, pos);
            }
        };
    }

    @Override
    public void configureEditorComponent(final E value, final boolean isSelected, final int row, final int column) {
        // TODO Auto-generated method stub

    }

    @Override
    public void configureRendererComponent(final E value, final boolean isSelected, final boolean hasFocus, final int row, final int column) {
        if ((this.sizeValue = this.getBytes(value)) < 0) {
            this.renderer.setText(this.getInvalidValue());
        } else {
            this.renderer.setText(this.getSizeString(this.sizeValue));
        }

    }

    abstract protected long getBytes(E o2);

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

    /**
     * @return
     */
    protected String getInvalidValue() {
        return "";
    }

    /**
     * @return
     */
    @Override
    public JComponent getRendererComponent(final E value, final boolean isSelected, final boolean hasFocus, final int row, final int column) {
        return this.renderer;
    }

    private String getSizeString(final long fileSize) {
        if (fileSize >= 1024 * 1024 * 1024 * 1024l) { return this.formatter.format(fileSize / (1024 * 1024 * 1024 * 1024.0)) + " TiB"; }
        if (fileSize >= 1024 * 1024 * 1024l) { return this.formatter.format(fileSize / (1024 * 1024 * 1024.0)) + " GiB"; }
        if (fileSize >= 1024 * 1024l) { return this.formatter.format(fileSize / (1024 * 1024.0)) + " MiB"; }
        if (fileSize >= 1024l) { return this.formatter.format(fileSize / 1024.0) + " KiB"; }
        return fileSize + " B";
    }

    @Override
    protected String getTooltipText(final E value) {
        if ((this.sizeValue = this.getBytes(value)) < 0) {
            return this.getInvalidValue();
        } else {
            return this.getSizeString(this.sizeValue);
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
    public void resetEditor() {
        // TODO Auto-generated method stub

    }

    @Override
    public void resetRenderer() {

        this.renderer.setOpaque(false);
        this.renderer.setBorder(ExtColumn.DEFAULT_BORDER);

    }

    @Override
    public void setValue(final Object value, final E object) {

    }
}
