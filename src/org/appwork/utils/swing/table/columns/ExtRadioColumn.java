/**
 * Copyright (c) 2009 - 2011 AppWork UG(haftungsbeschränkt) <e-mail@appwork.org>
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
import javax.swing.JRadioButton;
import javax.swing.JTable;

import org.appwork.utils.swing.renderer.RendererCheckBox;
import org.appwork.utils.swing.table.ExtColumn;
import org.appwork.utils.swing.table.ExtDefaultRowSorter;
import org.appwork.utils.swing.table.ExtTableModel;

/**
 * @author daniel
 *
 */
public abstract class ExtRadioColumn<E> extends ExtColumn<E> implements ActionListener {

    private static final long      serialVersionUID = -5391898292508477789L;

    private final JRadioButton checkBoxRend;
    private final JRadioButton        checkBoxEdit;

    public ExtRadioColumn(String name, ExtTableModel<E> table) {
        super(name, table);

        checkBoxRend = new JRadioButton();
        checkBoxRend.setHorizontalAlignment(RendererCheckBox.CENTER);

        checkBoxEdit = new JRadioButton();
        checkBoxEdit.setHorizontalAlignment(RendererCheckBox.CENTER);

        this.setRowSorter(new ExtDefaultRowSorter<E>() {
            @Override
            public int compare(E o1, E o2) {
                boolean b1 = getBooleanValue(o1);
                boolean b2 = getBooleanValue(o2);

                if (b1 == b2) return 0;
                if (this.isSortOrderToggle()) {
                    return b1 && !b2 ? -1 : 1;
                } else {
                    return !b1 && b2 ? -1 : 1;
                }
            }

        });
    }

    protected abstract boolean getBooleanValue(E value);

    protected abstract void setBooleanValue(boolean value, E object);

    @Override
    protected int getMaxWidth() {
        return 70;
    }

    @Override
    public int getMinWidth() {
        return 30;
    }

    @Override
    public final Object getCellEditorValue() {
        return checkBoxEdit.isSelected();
    }

    @SuppressWarnings("unchecked")
    @Override
    public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
        checkBoxEdit.removeActionListener(this);
        checkBoxEdit.setSelected(getBooleanValue((E) value));
        checkBoxEdit.addActionListener(this);
        return checkBoxEdit;
    }

    @SuppressWarnings("unchecked")
    @Override
    public final Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
        checkBoxRend.setSelected(getBooleanValue((E) value));
        return checkBoxRend;
    }

    @Override
    public boolean isEditable(E obj) {
        return false;
    }

    public boolean isEnabled(E obj) {
        return true;
    }

    @Override
    public boolean isSortable(E obj) {
        return true;
    }

    @Override
    public final void setValue(Object value, E object) {
        setBooleanValue((Boolean) value, object);
    }

    public void actionPerformed(ActionEvent e) {
        checkBoxEdit.removeActionListener(this);
        this.fireEditingStopped();
    }

}

