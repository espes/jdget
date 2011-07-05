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

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JPopupMenu;
import javax.swing.SwingConstants;

import org.appwork.utils.locale.APPWORKUTILS;
import org.appwork.utils.swing.EDTRunner;
import org.appwork.utils.swing.renderer.RendererCheckBox;
import org.appwork.utils.swing.table.ExtColumn;
import org.appwork.utils.swing.table.ExtDefaultRowSorter;
import org.appwork.utils.swing.table.ExtTableModel;

public abstract class ExtCheckColumn<E> extends ExtColumn<E> implements ActionListener {

    private static final long        serialVersionUID = -5391898292508477789L;

    protected final RendererCheckBox renderer;
    protected final JCheckBox        editor;

    /**
     * @param string
     */
    public ExtCheckColumn(final String string) {
        this(string, null);
    }

    public ExtCheckColumn(final String name, final ExtTableModel<E> table) {
        super(name, table);

        this.renderer = new RendererCheckBox();

        this.editor = new JCheckBox();

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
        this.editor.removeActionListener(this);
        this.stopCellEditing();

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

    @Override
    public JPopupMenu createHeaderPopup() {

        final JPopupMenu ret = new JPopupMenu();
        boolean allenabled = true;
        boolean editable = false;
        for (int i = 0; i < this.getModel().size(); i++) {
            if (this.isEditable(this.getModel().getElementAt(i))) {
                editable = true;

                if (this.isEditable(this.getModel().getElementAt(i)) && !this.getBooleanValue(this.getModel().getElementAt(i))) {
                    allenabled = false;
                    break;
                }
            }
        }
        if (!editable) {
            // column is not editable
            return null;
        }
        final JPopupMenu menu = ret;
        if (allenabled) {

            final JCheckBox cb = new JCheckBox(APPWORKUTILS.T.extttable_disable_all());
            cb.setSelected(true);
            cb.addActionListener(new ActionListener() {

                @Override
                public void actionPerformed(final ActionEvent e) {
                    ExtCheckColumn.this.setEnabledAll(false);
                    menu.setVisible(false);

                }
            });
            ret.add(cb);

        } else {
            final JCheckBox cb = new JCheckBox(APPWORKUTILS.T.extttable_enabled_all());
            cb.setSelected(false);
            cb.addActionListener(new ActionListener() {

                @Override
                public void actionPerformed(final ActionEvent e) {
                    ExtCheckColumn.this.setEnabledAll(true);
                    menu.setVisible(false);
                }
            });
            ret.add(cb);
        }

        return ret;
    }

    @Override
    public void extendControlButtonMenu(final JPopupMenu popup) {
        // TODO Auto-generated method stub
        super.extendControlButtonMenu(popup);
    }

    protected abstract boolean getBooleanValue(E value);

    @Override
    public final Object getCellEditorValue() {
        return this.editor.isSelected();
    }

    @Override
    public int getClickcount() {

        return 1;
    }

    @Override
    public JComponent getEditorComponent(final E value, final boolean isSelected, final int row, final int column) {
        // TODO Auto-generated method stub
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

    @Override
    public JComponent getRendererComponent(final E value, final boolean isSelected, final boolean hasFocus, final int row, final int column) {

        return this.renderer;
    }

    @Override
    protected String getTooltipText(final E obj) {

        return this.getBooleanValue(obj) ? APPWORKUTILS.T.active() : APPWORKUTILS.T.inactive();
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
        this.editor.setOpaque(false);
        this.editor.putClientProperty("Synthetica.opaque", Boolean.FALSE);

    }

    @Override
    public void resetRenderer() {
        this.renderer.setHorizontalAlignment(SwingConstants.CENTER);
        this.renderer.setOpaque(false);
        this.renderer.putClientProperty("Synthetica.opaque", Boolean.FALSE);

    }

    protected abstract void setBooleanValue(boolean value, E object);

    /**
     * @param b
     */
    protected void setEnabledAll(final boolean b) {
        for (int i = 0; i < this.getModel().size(); i++) {
            if (this.isEditable(this.getModel().getElementAt(i))) {
                this.setBooleanValue(b, this.getModel().getElementAt(i));
            }
        }
        new EDTRunner() {

            @Override
            protected void runInEDT() {
                ExtCheckColumn.this.getModel().fireTableDataChanged();
            }
        };
    }

    @Override
    public final void setValue(final Object value, final E object) {
        this.setBooleanValue((Boolean) value, object);
    }

}
