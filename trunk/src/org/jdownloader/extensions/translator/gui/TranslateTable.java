package org.jdownloader.extensions.translator.gui;

import java.awt.Color;
import java.awt.event.MouseEvent;
import java.util.ArrayList;

import javax.swing.JPopupMenu;

import jd.gui.swing.jdgui.BasicJDTable;

import org.appwork.swing.exttable.ExtColumn;
import org.appwork.swing.exttable.ExtOverlayRowHighlighter;
import org.appwork.swing.exttable.ExtTable;
import org.jdownloader.extensions.translator.TranslateEntry;
import org.jdownloader.extensions.translator.gui.actions.SetDefaultAction;

/**
 * Table for all entries
 * 
 * @author thomas
 * 
 */

public class TranslateTable extends BasicJDTable<TranslateEntry> {
    private ExtOverlayRowHighlighter rhDefault;

    @Override
    protected JPopupMenu onContextMenu(JPopupMenu popup, TranslateEntry contextObject, ArrayList<TranslateEntry> selection, ExtColumn<TranslateEntry> column, MouseEvent ev) {

        popup.add(new SetDefaultAction(selection));

        return popup;
    }

    public TranslateTable(TranslateTableModel tableModel) {

        super(tableModel);
        this.setSearchEnabled(true);

        int opacity = 20;
        addRowHighlighter(new ExtOverlayRowHighlighter(null, new Color(255, 0, 0, opacity)) {
            @Override
            public boolean doHighlight(ExtTable<?> extTable, int row) {
                return getExtTableModel().getObjectbyRow(row).hasErrors();
            }
        });
        addRowHighlighter(new ExtOverlayRowHighlighter(null, new Color(255, 80, 0, opacity)) {
            @Override
            public boolean doHighlight(ExtTable<?> extTable, int row) {
                return getExtTableModel().getObjectbyRow(row).isMissing();
            }
        });
        addRowHighlighter(new ExtOverlayRowHighlighter(null, new Color(255, 165, 0, opacity)) {
            @Override
            public boolean doHighlight(ExtTable<?> extTable, int row) {
                return getExtTableModel().getObjectbyRow(row).isWrongLength();
            }
        });

        addRowHighlighter(new ExtOverlayRowHighlighter(null, new Color(0, 255, 120, opacity)) {
            @Override
            public boolean doHighlight(ExtTable<?> extTable, int row) {
                TranslateTableModel t = (TranslateTableModel) getExtTableModel();
                TranslateEntry e = getExtTableModel().getObjectbyRow(row);
                return (e.isOK(false));
            }
        });

    }
    // @Override
    // protected void onDoubleClick(MouseEvent e, TranslateEntry obj) {
    // super.onDoubleClick(e, obj);
    // }

}
