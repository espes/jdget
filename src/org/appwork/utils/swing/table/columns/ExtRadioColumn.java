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

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JComponent;
import javax.swing.JRadioButton;
import javax.swing.SwingConstants;

import org.appwork.utils.swing.table.ExtColumn;
import org.appwork.utils.swing.table.ExtDefaultRowSorter;
import org.appwork.utils.swing.table.ExtTableModel;

/**
 * @author daniel
 * 
 */
public abstract class ExtRadioColumn<E> extends ExtColumn<E> implements ActionListener {

    private static final long  serialVersionUID = -5391898292508477789L;

    private final JRadioButton renderer;
    private final JRadioButton editor;

    public ExtRadioColumn(final String name, final ExtTableModel<E> table) {
        super(name, table);

        this.renderer = new JRadioButton();

        this.editor = new JRadioButton();

        this.setRowSorter(new ExtDefaultRowSorter<E>() {
            @Override
            public int compare(final E o1, final E o2) {
                final boolean b1 = ExtRadioColumn.this.getBooleanValue(o1);
                final boolean b2 = ExtRadioColumn.this.getBooleanValue(o2);

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
        this.editor.removeActionListener(this);
        this.fireEditingStopped();
    }

    @Override
    public void configureEditorComponent(final E value, final boolean isSelected, final int row, final int column) {
        this.editor.removeActionListener(this);
        this.editor.setSelected(this.getBooleanValue(value));
        this.editor.addActionListener(this);

    }

    @Override
    public final void configureRendererComponent(final E value, final boolean isSelected, final boolean hasFocus, final int row, final int column) {
        this.renderer.setSelected(this.getBooleanValue(value));

    }

    protected abstract boolean getBooleanValue(E value);

    @Override
    public final Object getCellEditorValue() {
        return this.editor.isSelected();
    }

    /**
     * @return
     */
    @Override
    public JComponent getEditorComponent(final E value, final boolean isSelected, final int row, final int column) {
        return this.editor;
    }

    @Override
    protected int getMaxWidth() {
        return 70;
    }

    @Override
    public int getMinWidth() {
        return 30;
    }

    /**
     * @return
     */
    @Override
    public JComponent getRendererComponent(final E value, final boolean isSelected, final boolean hasFocus, final int row, final int column) {
        return this.renderer;
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
    public void resetEditor() {
        this.editor.setHorizontalAlignment(SwingConstants.CENTER);

    }

    @Override
    public void resetRenderer() {
        this.renderer.setHorizontalAlignment(SwingConstants.CENTER);

    }

    protected abstract void setBooleanValue(boolean value, E object);

    @Override
    public final void setValue(final Object value, final E object) {
        this.setBooleanValue((Boolean) value, object);
    }

}
