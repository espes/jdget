/**
 * Copyright (c) 2009 - 2011 AppWork UG(haftungsbeschränkt) <e-mail@appwork.org>
 * 
 * This file is part of org.appwork.utils.swing.table
 * 
 * This software is licensed under the Artistic License 2.0,
 * see the LICENSE file or http://www.opensource.org/licenses/artistic-license-2.0.php
 * for details
 */
package org.appwork.swing.exttable;

import javax.swing.event.TableColumnModelListener;
import javax.swing.table.DefaultTableColumnModel;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;

/**
 * @author thomas
 * 
 */
public class ExtColumnModel extends DefaultTableColumnModel {

    /**
     * 
     */
    private static final long serialVersionUID = 1L;

    /**
     * @param columnModel
     */
    public ExtColumnModel(final TableColumnModel org) {
        super();
        this.setColumnMargin(org.getColumnMargin());
        this.setColumnSelectionAllowed(org.getColumnSelectionAllowed());
        this.setSelectionModel(org.getSelectionModel());
        for (int i = 0; i < org.getColumnCount(); i++) {
            this.addColumn(org.getColumn(i));
        }
        if (org instanceof DefaultTableColumnModel) {
            for (final TableColumnModelListener cl : ((DefaultTableColumnModel) org).getColumnModelListeners()) {
                this.addColumnModelListener(cl);
            }
        }

    }

    @Override
    public TableColumn getColumn(final int columnIndex) {
        /*
         * Math.max(0, columnIndex)
         * 
         * WORKAROUND for -1 column access,Index out of Bound,Unknown why it
         * happens but this workaround seems to do its job
         */
        return super.getColumn(columnIndex < 0 ? 0 : columnIndex);
    }

}
