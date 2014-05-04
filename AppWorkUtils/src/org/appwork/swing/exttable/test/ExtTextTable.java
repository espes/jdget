package org.appwork.swing.exttable.test;

import java.awt.Color;

import javax.swing.DropMode;
import javax.swing.JScrollPane;
import javax.swing.JTable;

import org.appwork.app.gui.BasicGui;
import org.appwork.swing.exttable.ExtTable;
import org.appwork.swing.exttable.SelectionHighlighter;

public class ExtTextTable extends ExtTable<TextObject> {

    private static final long serialVersionUID = -6211879933096729574L;

    public static void main(final String[] args) {
        new BasicGui("testTable") {

            @Override
            protected void layoutPanel() {
                getFrame().add(new JScrollPane(new ExtTextTable()));
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
        setSearchEnabled(true);

        setDragEnabled(true);
        setDropMode(DropMode.ON);

        final DragDropHelper ddh = new DragDropHelper();
        this.setTransferHandler(ddh);
        addRowHighlighter(new SelectionHighlighter(null, new Color(10, 10, 10, 40)));
//        getModel().addExtComponentRowHighlighter(new ExtComponentRowHighlighter<TextObject>(Color.BLACK,Color.RED,null) {
//            
//            @Override
//            public boolean accept(ExtColumn<TextObject> column, TextObject value, boolean selected, boolean focus, int row) {
//                // TODO Auto-generated method stub
//                return selected;
//            }
//        });
        // this.setShowHorizontalLines(false);
      
        setAutoResizeMode(JTable.AUTO_RESIZE_LAST_COLUMN);
       

    }
}
