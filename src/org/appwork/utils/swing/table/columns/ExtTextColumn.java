package org.appwork.utils.swing.table.columns;

import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.regex.Pattern;

import javax.swing.BorderFactory;
import javax.swing.Icon;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JTextField;

import org.appwork.utils.swing.renderer.RenderLabel;
import org.appwork.utils.swing.table.ExtColumn;
import org.appwork.utils.swing.table.ExtDefaultRowSorter;
import org.appwork.utils.swing.table.ExtTable;
import org.appwork.utils.swing.table.ExtTableModel;

public abstract class ExtTextColumn<E> extends ExtColumn<E> implements ActionListener {

    private static final long serialVersionUID  = 2114805529462086691L;
    protected RenderLabel     label;
    protected Color           defaultForeground = null;
    private JTextField        text;

    /**
     * @param string
     */
    public ExtTextColumn(final String name) {
        this(name, null);

    }

    public ExtTextColumn(final String name, final ExtTableModel<E> table) {
        super(name, table);
        this.text = new JTextField();
        this.prepareTableCellEditorComponent(this.text);

        this.label = new RenderLabel();
        this.label.setBorder(BorderFactory.createEmptyBorder(0, 5, 0, 5));
        this.label.setOpaque(false);
        this.defaultForeground = this.label.getForeground();

        this.prepareTableCellRendererComponent(this.label);

        this.setRowSorter(new ExtDefaultRowSorter<E>() {

            @Override
            public int compare(final E o1, final E o2) {
                String o1s = ExtTextColumn.this.getStringValue(o1);
                String o2s = ExtTextColumn.this.getStringValue(o2);
                if (o1s == null) {
                    o1s = "";
                }
                if (o2s == null) {
                    o2s = "";
                }
                if (this.isSortOrderToggle()) {
                    return o1s.compareTo(o2s);
                } else {
                    return o2s.compareTo(o1s);
                }

            }

        });

    }

    public void actionPerformed(final ActionEvent e) {
        this.text.removeActionListener(this);
        this.fireEditingStopped();
    }

    @Override
    public Object getCellEditorValue() {
        return this.text.getText();
    }

    @Override
    public JComponent getEditorComponent(final ExtTable<E> table, final E value, final boolean isSelected, final int row, final int column) {
        this.text.removeActionListener(this);
        this.text.setText(this.getStringValue(value));
        this.text.addActionListener(this);
        return this.text;
    }

    /**
     * @param value
     * @return
     */
    protected Icon getIcon(final E value) {
        return null;
    }

    @Override
    public JComponent getRendererComponent(final ExtTable<E> table, final E value, final boolean isSelected, final boolean hasFocus, final int row, final int column) {
        this.prepareLabel(value);
        String str = this.getStringValue(value);
        if (str == null) {
            // under substance, setting setText(null) somehow sets the label
            // opaque.
            str = "";
        }
        this.label.setText(str);
        this.label.setToolTipText(this.getToolTip(value));
        this.label.setEnabled(this.isEnabled(value));
        this.label.setIcon(this.getIcon(value));

        return this.label;

    }

    protected abstract String getStringValue(E value);

    protected String getToolTip(final E obj) {
        final String v = this.getStringValue(obj);
        if (v != null && v.length() > 0) {
            return "<html>" + v.replaceAll("\r\n", "<br>") + "</html>";
        } else {
            return null;
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
    public boolean matchSearch(final E object, final Pattern pattern) {
        return pattern.matcher(this.getStringValue(object)).matches();
    }

    /**
     * @param value
     */
    protected void prepareLabel(final E value) {
    }

    /**
     * Should be overwritten to prepare the component for the TableCellEditor
     * (e.g. setting tooltips)
     */
    protected void prepareTableCellEditorComponent(final JTextField text) {
    }

    /**
     * Should be overwritten to prepare the componente for the TableCellRenderer
     * (e.g. setting tooltips)
     */
    protected void prepareTableCellRendererComponent(final JLabel jlr) {
    }

    /**
     * Override to save value after editing
     * 
     * @param value
     * @param object
     */
    protected void setStringValue(final String value, final E object) {

    }

    @Override
    public void setValue(final Object value, final E object) {
        this.setStringValue((String) value, object);
    }

}
