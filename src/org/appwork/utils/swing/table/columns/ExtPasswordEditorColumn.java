/**
 * Copyright (c) 2009 - 2011 AppWork UG(haftungsbeschränkt) <e-mail@appwork.org>
 * 
 * This file is part of org.appwork.utils.swing.table
 * 
 * This software is licensed under the Artistic License 2.0,
 * see the LICENSE file or http://www.opensource.org/licenses/artistic-license-2.0.php
 * for details
 */
package org.appwork.utils.swing.table.columns;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JTable;
import javax.swing.JTextField;

import org.appwork.utils.swing.table.ExtTableModel;

/**
 * @author daniel
 * 
 */
public abstract class ExtPasswordEditorColumn<E> extends ExtTextColumn<E> implements ActionListener {

    private static final long serialVersionUID = -3107569347493659178L;
    private final JTextField  text;

    public ExtPasswordEditorColumn(final String name, final ExtTableModel<E> table) {
        super(name, table);

        this.text = new JTextField();
        this.prepareTableCellEditorComponent(this.text);

        this.setClickcount(2);
    }

    public void actionPerformed(final ActionEvent e) {
        this.text.removeActionListener(this);
        this.fireEditingStopped();
    }

    @Override
    public final Object getCellEditorValue() {
        return this.text.getText();
    }

    @Override
    protected String getStringValue(E value) {
        return "******";
    }

    protected abstract String getPlainStringValue(E value);

    @SuppressWarnings("unchecked")
    @Override
    public final Component getTableCellEditorComponent(final JTable table, final Object value, final boolean isSelected, final int row, final int column) {
        this.text.removeActionListener(this);
        this.text.setText(this.getPlainStringValue((E) value));
        this.text.addActionListener(this);
        return this.text;
    }

    @Override
    public boolean isEditable(final E obj) {
        return true;
    }

    /**
     * Should be overwritten to prepare the component for the TableCellEditor
     * (e.g. setting tooltips)
     */
    protected void prepareTableCellEditorComponent(final JTextField text) {
    }

    protected abstract void setStringValue(String value, E object);

    @Override
    public final void setValue(final Object value, final E object) {
        this.setStringValue((String) value, object);
    }

}
