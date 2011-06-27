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

            private final Date a    = new Date();
            private boolean    aset = false;
            private final Date b    = new Date();
            private boolean    bset = false;

            @Override
            public int compare(final E o1, final E o2) {
                Date tmp = ExtDateColumn.this.getDate(o1);
                if (tmp != null) {
                    this.a.setTime(tmp.getTime());
                    this.aset = true;
                } else {
                    this.aset = false;
                }
                tmp = ExtDateColumn.this.getDate(o2);
                if (tmp != null) {
                    this.b.setTime(tmp.getTime());
                    this.bset = true;
                } else {
                    this.bset = false;
                }
                if (this.aset == this.bset == false) { return 0; }
                if (!this.aset && this.bset) { return 1; }
                if (this.aset && !this.bset) { return -1; }
                if (this.isSortOrderToggle()) {
                    return this.a.compareTo(this.b);
                } else {
                    return this.b.compareTo(this.a);
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
