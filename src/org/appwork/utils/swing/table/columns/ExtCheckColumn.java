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

import javax.swing.JCheckBox;
import javax.swing.JTable;
import javax.swing.SwingConstants;

import org.appwork.utils.swing.renderer.RendererCheckBox;
import org.appwork.utils.swing.table.ExtColumn;
import org.appwork.utils.swing.table.ExtDefaultRowSorter;
import org.appwork.utils.swing.table.ExtTableModel;

public abstract class ExtCheckColumn<E> extends ExtColumn<E> implements ActionListener {

    private static final long      serialVersionUID = -5391898292508477789L;

    private final RendererCheckBox checkBoxRend;
    private final JCheckBox        checkBoxEdit;

    /**
     * @param string
     */
    public ExtCheckColumn(final String string) {
        this(string, null);
    }

    public ExtCheckColumn(final String name, final ExtTableModel<E> table) {
        super(name, table);

        this.checkBoxRend = new RendererCheckBox();
        this.checkBoxRend.setHorizontalAlignment(SwingConstants.CENTER);

        this.checkBoxEdit = new JCheckBox();
        this.checkBoxEdit.setHorizontalAlignment(SwingConstants.CENTER);

        this.setRowSorter(new ExtDefaultRowSorter<E>() {
            @Override
            public int compare(final E o1, final E o2) {
                final boolean b1 = ExtCheckColumn.this.getBooleanValue(o1);
                final boolean b2 = ExtCheckColumn.this.getBooleanValue(o2);

                if (b1 == b2) { return 0; }
                if (this.isSortOrderToggle()) {
                    return b1 && !b2 ? -1 : 1;
                } else {
                    return !b1 && b2 ? -1 : 1;
                }
            }

        });
    }

    public void actionPerformed(final ActionEvent e) {
        this.checkBoxEdit.removeActionListener(this);
        this.fireEditingStopped();
    }

    protected abstract boolean getBooleanValue(E value);

    @Override
    public final Object getCellEditorValue() {
        return this.checkBoxEdit.isSelected();
    }

    @Override
    protected int getMaxWidth() {
        return 70;
    }

    @Override
    public int getMinWidth() {
        return 30;
    }

    @SuppressWarnings("unchecked")
    @Override
    public Component getTableCellEditorComponent(final JTable table, final Object value, final boolean isSelected, final int row, final int column) {
        this.checkBoxEdit.removeActionListener(this);
        this.checkBoxEdit.setSelected(this.getBooleanValue((E) value));
        this.checkBoxEdit.addActionListener(this);
        return this.checkBoxEdit;
    }

    @SuppressWarnings("unchecked")
    @Override
    public final Component getTableCellRendererComponent(final JTable table, final Object value, final boolean isSelected, final boolean hasFocus, final int row, final int column) {
        this.checkBoxRend.setSelected(this.getBooleanValue((E) value));
        return this.checkBoxRend;
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

    protected abstract void setBooleanValue(boolean value, E object);

    @Override
    public final void setValue(final Object value, final E object) {
        this.setBooleanValue((Boolean) value, object);
    }

}
