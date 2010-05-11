package org.appwork.utils.swing.table.columns;

import java.awt.Component;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.swing.BorderFactory;
import javax.swing.JTable;

import org.appwork.utils.swing.renderer.RenderLabel;
import org.appwork.utils.swing.table.ExtColumn;
import org.appwork.utils.swing.table.ExtDefaultRowSorter;
import org.appwork.utils.swing.table.ExtTableModel;

public abstract class ExtDateColumn<E> extends ExtColumn<E> {

    /**
     * 
     */
    private static final long serialVersionUID = -5812486934156037376L;
    private RenderLabel label;
    private SimpleDateFormat dateFormat;
    private Date date;
    protected String badDateText = "~";

    public ExtDateColumn(String name, ExtTableModel<E> table) {
        super(name, table);
        this.label = new RenderLabel();
        label.setBorder(null);
        label.setOpaque(false);
        label.setBorder(BorderFactory.createEmptyBorder(0, 5, 0, 5));
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

    public String getToolTip(E obj) {
        return null;
    }

    @SuppressWarnings("unchecked")
    @Override
    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
        date = getDate((E) value);
        if (date == null) {
            label.setText(setText((E) value, badDateText));
        } else {
            label.setText(setText((E) value, getDateFormat().format(date)));
        }
        label.setToolTipText(getToolTip((E) value));
        label.setEnabled(isEnabled((E) value));
        return label;
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
