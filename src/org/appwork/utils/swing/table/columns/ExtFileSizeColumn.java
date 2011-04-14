package org.appwork.utils.swing.table.columns;

import java.text.DecimalFormat;
import java.text.FieldPosition;

import javax.swing.BorderFactory;
import javax.swing.JComponent;

import org.appwork.utils.swing.renderer.RenderLabel;
import org.appwork.utils.swing.table.ExtColumn;
import org.appwork.utils.swing.table.ExtDefaultRowSorter;
import org.appwork.utils.swing.table.ExtTable;
import org.appwork.utils.swing.table.ExtTableModel;

public abstract class ExtFileSizeColumn<E> extends ExtColumn<E> {

    /**
     * 
     */
    private static final long serialVersionUID = -5812486934156037376L;
    protected RenderLabel     label;
    protected long            sizeValue;
    private StringBuffer      sb;
    private DecimalFormat     formatter;

    public ExtFileSizeColumn(final String name, final ExtTableModel<E> table) {
        super(name, table);
        this.label = new RenderLabel();
        this.label.setBorder(null);
        this.label.setOpaque(false);
        this.label.setBorder(BorderFactory.createEmptyBorder(0, 5, 0, 5));

        this.setRowSorter(new ExtDefaultRowSorter<E>() {
            /**
             * sorts the icon by hashcode
             */
            @Override
            public int compare(final E o1, final E o2) {
                final long s1 = ExtFileSizeColumn.this.getBytes(o1);
                final long s2 = ExtFileSizeColumn.this.getBytes(o2);
                if (s1 == s2) { return 0; }
                if (this.isSortOrderToggle()) {
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

    abstract protected long getBytes(E o2);

    @Override
    public Object getCellEditorValue() {
        return null;
    }

    /**
     * @return
     */
    protected String getInvalidValue() {
        return "";
    }

    private String getSizeString(final long fileSize) {
        if (fileSize >= 1024 * 1024 * 1024 * 1024l) { return this.formatter.format(fileSize / (1024 * 1024 * 1024 * 1024.0)) + " TiB"; }
        if (fileSize >= 1024 * 1024 * 1024l) { return this.formatter.format(fileSize / (1024 * 1024 * 1024.0)) + " GiB"; }
        if (fileSize >= 1024 * 1024l) { return this.formatter.format(fileSize / (1024 * 1024.0)) + " MiB"; }
        if (fileSize >= 1024l) { return this.formatter.format(fileSize / 1024.0) + " KiB"; }
        return fileSize + " B";
    }

    @SuppressWarnings("unchecked")
    @Override
    public JComponent getRendererComponent(final ExtTable<E> table, final E value, final boolean isSelected, final boolean hasFocus, final int row, final int column) {
        if ((this.sizeValue = this.getBytes((E) value)) < 0) {
            this.label.setText(this.getInvalidValue());
        } else {
            this.label.setText(this.getSizeString(this.sizeValue));
        }
        this.label.setEnabled(this.isEnabled((E) value));
        return this.label;
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
    public void setValue(final Object value, final E object) {

    }
}
