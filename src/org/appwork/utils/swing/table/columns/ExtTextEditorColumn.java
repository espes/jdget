/**
 * Copyright (c) 2009 - 2010 AppWork UG(haftungsbeschränkt) <e-mail@appwork.org>
 * 
 * This file is part of org.appwork.utils.swing.table.columns
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

public abstract class ExtTextEditorColumn<E> extends ExtTextColumn<E> implements ActionListener {

    private static final long serialVersionUID = -3107569347493659178L;
    private final JTextField  text;

    public ExtTextEditorColumn(String name, ExtTableModel<E> table) {
        super(name, table);

        text = new JTextField();
        prepareTableCellEditorComponent(text);

        setClickcount(2);
    }

    @Override
    public boolean isEditable(E obj) {
        return true;
    }

    /**
     * Should be overwritten to prepare the componente for the TableCellEditor
     * (e.g. setting tooltips)
     */
    protected void prepareTableCellEditorComponent(JTextField text) {
    }

    @Override
    public final Object getCellEditorValue() {
        return text.getText();
    }

    @SuppressWarnings("unchecked")
    @Override
    public final Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
        text.removeActionListener(this);
        text.setText(getStringValue((E) value));
        text.addActionListener(this);
        return text;
    }

  
    public void actionPerformed(ActionEvent e) {
        text.removeActionListener(this);
        this.fireEditingStopped();
    }

}
