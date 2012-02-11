/**
 * Copyright (c) 2009 - 2011 AppWork UG(haftungsbeschränkt) <e-mail@appwork.org>
 * 
 * This file is part of org.appwork.utils.swing.table
 * 
 * This software is licensed under the Artistic License 2.0,
 * see the LICENSE file or http://www.opensource.org/licenses/artistic-license-2.0.php
 * for details
 */
package org.appwork.swing.exttable.columnmenu;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.Action;

import org.appwork.resources.AWUTheme;
import org.appwork.swing.exttable.ExtTable;
import org.appwork.utils.locale._AWU;

/**
 * @author thomas
 * 
 */
public class ResetColumns extends AbstractAction {

    /**
     * 
     */
    private static final long serialVersionUID = 5089651483570517886L;
    private final ExtTable<?> table;

    /**
     * @param extTable
     */
    public ResetColumns(final ExtTable<?> extTable) {
        super(_AWU.T.ResetColumnsAction());
        this.putValue(Action.SMALL_ICON, AWUTheme.getInstance().getIcon("exttable/resetColumns", extTable.getContextIconSize()));
        this.table = extTable;
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
     */
    @Override
    public void actionPerformed(final ActionEvent e) {
        this.table.resetColumnLocks();
        this.table.resetColumnDimensions();
        this.table.resetColumnOrder();
        this.table.resetColumnVisibility();
        this.table.updateColumns();

    }

}
