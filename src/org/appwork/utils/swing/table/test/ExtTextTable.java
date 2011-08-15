package org.appwork.utils.swing.table.test;

import java.awt.Color;
import java.awt.Dimension;

import javax.swing.DropMode;
import javax.swing.JScrollPane;

import org.appwork.app.gui.BasicGui;
import org.appwork.utils.swing.table.ExtTable;
import org.appwork.utils.swing.table.SelectionHighlighter;

public class ExtTextTable extends ExtTable<TextObject> {

    private static final long serialVersionUID = -6211879933096729574L;

    public static void main(final String[] args) {
        new BasicGui("testTable") {

            @Override
            protected void layoutPanel() {
                this.getFrame().add(new JScrollPane(new ExtTextTable()));
            }

            @Override
            protected void requestExit() {
                System.exit(1);
            }

        };
    }

    /**
     * @param tableModel
     */
    public ExtTextTable() {
        super(new ExtTestModel());
        // this.setSearchEnabled(true);#
        System.out.println("Compiled");
        this.setDragEnabled(true);
        this.setDropMode(DropMode.ON);

        final DragDropHelper ddh = new DragDropHelper();
        this.setTransferHandler(ddh);
        this.addRowHighlighter(new SelectionHighlighter(null, new Color(10, 10, 10, 40)));

    }

    @Override
    public Dimension getPreferredSize() {
        final Dimension ret = super.getPreferredSize();
        System.out.println(ret);
        return ret;
    }
}
