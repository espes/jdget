/**
 * Copyright (c) 2009 - 2011 AppWork UG(haftungsbeschränkt) <e-mail@appwork.org>
 * 
 * This file is part of org.appwork.swing.exttable.columnmenu
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
import org.appwork.swing.exttable.ExtColumn;
import org.appwork.utils.locale._AWU;

/**
 * @author thomas
 * 
 */
public class LockColumnWidthAction extends AbstractAction {

    /**
     * 
     */
    private static final long serialVersionUID = 8571514195519414112L;
    private final ExtColumn<?> extColumn;

    /**
     * @param extColumn
     */
    public LockColumnWidthAction(final ExtColumn<?> extColumn) {
        super();
        putValue(Action.SMALL_ICON, AWUTheme.getInstance().getIcon("exttable/lockColumn", extColumn.getModel().getTable().getContextIconSize()));
        this.extColumn = extColumn;
        if(extColumn.isResizable()){
            putValue(Action.SELECTED_KEY, false);
            putValue(Action.NAME, _AWU.T.LockColumnWidthAction2());
         
        }else{
            putValue(Action.SELECTED_KEY, true);  
            putValue(Action.NAME, _AWU.T.unLockColumnWidthAction2()); 
        }
       
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
     */
    @Override
    public void actionPerformed(final ActionEvent e) {
        extColumn.setResizable(!extColumn.isResizable());
    }

}
