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

import java.awt.event.ActionListener;

import javax.swing.JTextField;

import org.appwork.utils.swing.table.ExtTableModel;

/**
 * @author daniel
 * 
 */
public abstract class ExtPasswordEditorColumn<E> extends ExtTextColumn<E> implements ActionListener {

    private static final long serialVersionUID = -3107569347493659178L;

    public ExtPasswordEditorColumn(final String name, final ExtTableModel<E> table) {
        super(name, table);

    }

    protected abstract String getPlainStringValue(E value);

    @Override
    protected String getStringValue(final E value) {
        return "******";
    }

    @Override
    public boolean isEditable(final E obj) {
        return true;
    }

    /**
     * Should be overwritten to prepare the component for the TableCellEditor
     * (e.g. setting tooltips)
     */
    @Override
    protected void prepareTableCellEditorComponent(final JTextField text) {
    }

    @Override
    protected abstract void setStringValue(String value, E object);

}
