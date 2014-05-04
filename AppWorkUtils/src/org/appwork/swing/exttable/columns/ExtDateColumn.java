package org.appwork.swing.exttable.columns;

import java.text.DateFormat;
import java.text.FieldPosition;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.appwork.swing.exttable.ExtColumn;
import org.appwork.swing.exttable.ExtDefaultRowSorter;
import org.appwork.swing.exttable.ExtTableModel;
import org.appwork.utils.locale._AWU;

public abstract class ExtDateColumn<E> extends ExtTextColumn<E> {

    /**
     * 
     */
    private static final long serialVersionUID = -5812486934156037376L;

    private SimpleDateFormat  dateFormat;
    protected Date            date;
    protected String          badDateText      = "~";

    private StringBuffer      sb;

    /**
     * @param string
     */
    public ExtDateColumn(final String string) {
        this(string, null);
    }

    public ExtDateColumn(final String name, final ExtTableModel<E> table) {
        super(name, table);

        this.date = new Date();
        this.sb = new StringBuffer();
        try {
            this.dateFormat = createDateFormat();
        } catch (final Exception e) {
            this.dateFormat = new SimpleDateFormat("dd.MM.yy HH:mm") {

                /**
                 * 
                 */
                private static final long serialVersionUID = 1L;

                @Override
                public StringBuffer format(final Date date, final StringBuffer toAppendTo, final FieldPosition pos) {
                    ExtDateColumn.this.sb.setLength(0);
                    return super.format(date, ExtDateColumn.this.sb, pos);
                }
            };
        }

        setRowSorter(new ExtDefaultRowSorter<E>() {

            private long       a    = 0;
            private long       b    = 0;
            private final Date date = new Date();

            @Override
            public int compare(final E o1, final E o2) {
                Date tmp = ExtDateColumn.this.getDate(o1, this.date);
                if (tmp != null) {
                    this.a = tmp.getTime();
                } else {
                    this.a = 0;
                }
                tmp = ExtDateColumn.this.getDate(o2, this.date);
                if (tmp != null) {
                    this.b = tmp.getTime();
                } else {
                    this.b = 0;
                }
                if (this.a == this.b) { return 0; }
                if (getSortOrderIdentifier() != ExtColumn.SORT_ASC) {
                    return this.a > this.b ? -1 : 1;
                } else {
                    return this.b > this.a ? -1 : 1;
                }
            }

        });
        this.init();
    }

    /**
     * @return
     */
    protected SimpleDateFormat createDateFormat() {
       return new SimpleDateFormat(this.getDateFormatString()) {

            /**
             * 
             */
            private static final long serialVersionUID = 1L;

            @Override
            public StringBuffer format(final Date date, final StringBuffer toAppendTo, final FieldPosition pos) {
                ExtDateColumn.this.sb.setLength(0);
                return super.format(date, ExtDateColumn.this.sb, pos);
            }
        };
    }

    /**
     * @param value
     * @return the String which will be shown if there is no valid (null) date
     */
    protected String getBadDateText(final E value) {
        return this.badDateText;
    }

    @Override
    public Object getCellEditorValue() {

        return null;
    }

    /**
     * Returns the Date or null of there ois no valid date
     * 
     * @param o2
     * @param date
     *            TODO
     * @return
     */
    abstract protected Date getDate(E o2, Date date);

    /**
     * Override this method to use a custom dateformat
     * 
     * @return
     */
    public DateFormat getDateFormat() {
        return this.dateFormat;
    }

    /**
     * @return The dateformat used for this column
     */
    protected String getDateFormatString() {

        return _AWU.T.extdatecolumn_dateandtimeformat();
    }

    @Override
    public String getStringValue(final E value) {
        final Date d = this.getDate(value, this.date);
        if (d != null) {
            this.date = d;
        }
        if (d == null) {
            return this.setText(value, this.getBadDateText(value));
        } else {
            return this.setText(value, this.getDateFormat().format(d));
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
