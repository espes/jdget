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
public class SearchContextAction extends AbstractAction {
    /**
     * 
     */
    private static final long serialVersionUID = 9198500488284652091L;
    private final ExtTable<?> table;

    public SearchContextAction(final ExtTable<?> extTable) {
        super(_AWU.T.SearchContextAction());
        this.putValue(Action.SMALL_ICON, AWUTheme.getInstance().getIcon("exttable/findmenu", extTable.getContextIconSize()));
        this.table = extTable;
    }

    /**
     * @param extTable
     */

    /*
     * (non-Javadoc)
     * 
     * @see
     * java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
     */
    @Override
    public void actionPerformed(final ActionEvent e) {
        this.table.startSearch();

    }

}
