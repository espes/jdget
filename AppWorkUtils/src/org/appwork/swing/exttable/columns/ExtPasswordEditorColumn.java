/**
 * Copyright (c) 2009 - 2011 AppWork UG(haftungsbeschränkt) <e-mail@appwork.org>
 * 
 * This file is part of org.appwork.utils.swing.table
 * 
 * This software is licensed under the Artistic License 2.0,
 * see the LICENSE file or http://www.opensource.org/licenses/artistic-license-2.0.php
 * for details
 */
package org.appwork.swing.exttable.columns;

import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.util.regex.Pattern;

import javax.swing.JComponent;

import org.appwork.swing.exttable.ExtTableModel;
import org.appwork.utils.locale._AWU;

/**
 * @author daniel
 * 
 */
public abstract class ExtPasswordEditorColumn<E> extends ExtTextColumn<E> implements ActionListener {

    private static final long   serialVersionUID = -3107569347493659178L;
    private static final String BLINDTEXT        = "******";

    public ExtPasswordEditorColumn(final String name) {
        this(name, null);

    }

    public ExtPasswordEditorColumn(final String name, final ExtTableModel<E> table) {
        super(name, table);
        this.editorField.addFocusListener(new FocusListener() {

            @Override
            public void focusGained(final FocusEvent e) {
                ExtPasswordEditorColumn.this.editorField.selectAll();

            }

            @Override
            public void focusLost(final FocusEvent e) {
                // TODO Auto-generated method stub

            }
        });

    }

    /**
     * @return
     */
    @Override
    public JComponent getEditorComponent(final E value, final boolean isSelected, final int row, final int column) {
        return this.editorField;
    }

    protected abstract String getPlainStringValue(E value);

    @Override
    public String getStringValue(final E value) {
        return this.hasPassword(value) ? ExtPasswordEditorColumn.BLINDTEXT : "";
    }

    @Override
    protected String getTooltipText(final E obj) {

        return _AWU.T.extpasswordeditorcolumn_tooltip();
    }

    /**
     * @param value
     * @return
     */
    private boolean hasPassword(final E value) {
        final String pw = this.getPlainStringValue(value);
        return pw != null && pw.length() > 0;
    }

    @Override
    public boolean isEditable(final E obj) {
        return true;
    }

    @Override
    public boolean matchSearch(final E object, final Pattern pattern) {
        return false;
    }

    @Override
    protected abstract void setStringValue(String value, E object);

    @Override
    public void setValue(final Object value, final E object) {
        if (!value.toString().equals(ExtPasswordEditorColumn.BLINDTEXT)) {
            this.setStringValue((String) value, object);
        }
    }

}
