package org.appwork.utils.swing.table.columns;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.appwork.utils.swing.table.ExtDefaultRowSorter;
import org.appwork.utils.swing.table.ExtTableModel;

public abstract class ExtDateColumn<E> extends ExtTextColumn<E> {

    /**
     * 
     */
    private static final long serialVersionUID = -5812486934156037376L;

    private SimpleDateFormat  dateFormat;
    protected Date            date;
    protected String          badDateText      = "~";

    /**
     * @param string
     */
    public ExtDateColumn(final String string) {
        this(string, null);
    }

    public ExtDateColumn(final String name, final ExtTableModel<E> table) {
        super(name, table);

        this.dateFormat = new SimpleDateFormat("dd.MM.yy HH:mm");
        this.setRowSorter(new ExtDefaultRowSorter<E>() {

            private long a = 0;
            private long b = 0;

            @Override
            public int compare(final E o1, final E o2) {
                Date tmp = ExtDateColumn.this.getDate(o1);
                if (tmp != null) {
                    this.a = tmp.getTime();
                } else {
                    this.a = 0;
                }
                tmp = ExtDateColumn.this.getDate(o2);
                if (tmp != null) {
                    this.b = tmp.getTime();
                } else {
                    this.b = 0;
                }
                if (this.a == this.b) { return 0; }
                if (this.isSortOrderToggle()) {
                    return this.a > this.b ? -1 : 1;
                } else {
                    return this.b > this.a ? -1 : 1;
                }
            }

        });
        this.init();
    }

    @Override
    public Object getCellEditorValue() {

        return null;
    }

    /**
     * Returns the Date or null of there ois no valid date
     * 
     * @param o2
     * @return
     */
    abstract protected Date getDate(E o2);

    /**
     * Override this method to use a custom dateformat
     * 
     * @return
     */
    public DateFormat getDateFormat() {
        return this.dateFormat;
    }

    @Override
    public String getStringValue(final E value) {
        this.date = this.getDate(value);
        if (this.date == null) {
            return this.setText(value, this.badDateText);
        } else {
            return this.setText(value, this.getDateFormat().format(this.date));
        }

    }

    /**
     * 
     */
    protected void init() {

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

    /**
     * @param value
     * @param badDateText2
     * @return
     */
    protected String setText(final E value, final String badDateText2) {

        return badDateText2;
    }

}
