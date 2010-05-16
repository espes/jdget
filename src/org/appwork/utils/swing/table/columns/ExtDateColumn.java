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

    private SimpleDateFormat dateFormat;
    private Date date;
    protected String badDateText = "~";

    public ExtDateColumn(String name, ExtTableModel<E> table) {
        super(name, table);

        dateFormat = new SimpleDateFormat("dd.MM.yy HH:mm");
        this.setRowSorter(new ExtDefaultRowSorter<E>() {

            private Date a = new Date();
            private boolean aset = false;
            private Date b = new Date();
            private boolean bset = false;

            @Override
            public int compare(E o1, E o2) {
                Date tmp = getDate(o1);
                if (tmp != null) {
                    a.setTime(tmp.getTime());
                    aset = true;
                } else {
                    aset = false;
                }
                tmp = getDate(o2);
                if (tmp != null) {
                    b.setTime(tmp.getTime());
                    bset = true;
                } else {
                    bset = false;
                }
                if (aset == bset == false) return 0;
                if (!aset && bset) return 1;
                if (aset && !bset) return -1;
                if (this.isSortOrderToggle()) {
                    return a.compareTo(b);
                } else {
                    return b.compareTo(a);
                }
            }

        });
        init();
    }

    /**
     * 
     */
    protected void init() {
        // TODO Auto-generated method stub

    }

    /**
     * Returns the Date or null of there ois no valid date
     * 
     * @param o2
     * @return
     */
    abstract protected Date getDate(E o2);

    @Override
    public Object getCellEditorValue() {

        return null;
    }

    @Override
    public boolean isEditable(E obj) {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public boolean isEnabled(E obj) {
        // TODO Auto-generated method stub
        return true;
    }

    @Override
    public boolean isSortable(E obj) {
        // TODO Auto-generated method stub
        return true;
    }

    @Override
    public void setValue(Object value, E object) {
        // TODO Auto-generated method stub

    }

    public String getStringValue(E value) {
        date = getDate(value);
        if (date == null) {
            return setText(value, badDateText);
        } else {
            return setText(value, getDateFormat().format(date));
        }

    }

    /**
     * @param value
     * @param badDateText2
     * @return
     */
    protected String setText(E value, String badDateText2) {
        // TODO Auto-generated method stub
        return badDateText2;
    }

    /**
     * @param format
     * @return
     */

    /**
     * Override this method to use a custom dateformat
     * 
     * @return
     */
    public DateFormat getDateFormat() {
        return dateFormat;
    }
}
