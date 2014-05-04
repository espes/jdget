package org.appwork.swing.exttable;

import java.awt.Color;

public class SelectionHighlighter extends ExtOverlayRowHighlighter {

    public SelectionHighlighter(Color borderColor, Color contentColor) {
        super(borderColor, contentColor);
    }

    @Override
    public boolean doHighlight(ExtTable<?> extTable, int row) {
        return extTable.isRowSelected(row) ;
    }

}
