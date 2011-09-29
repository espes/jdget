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

import java.awt.Color;

import javax.swing.JTable;
import javax.swing.JTable.DropLocation;

/**
 * @author daniel
 * 
 */
public class DropHighlighter extends ExtOverlayRowHighlighter {

    public DropHighlighter(final Color borderColor, final Color contentColor) {
        super(borderColor, contentColor);
    }

    @Override
    public boolean doHighlight(final ExtTable<?> extTable, final int row) {
        final DropLocation dl = extTable.getDropLocation();
        if (dl == null) { return false; }
        final JTable.DropLocation dl2 = dl;
        if (dl2.isInsertRow()) { return false; }
        return dl.getRow() == row;
    }
}
