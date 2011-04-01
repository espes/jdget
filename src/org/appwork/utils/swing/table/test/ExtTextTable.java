package org.appwork.utils.swing.table.test;

import java.awt.Dimension;

import javax.swing.JScrollPane;

import org.appwork.app.gui.BasicGui;
import org.appwork.utils.swing.table.ExtTable;

public class ExtTextTable extends ExtTable<TextObject> {

    public static void main(final String[] args) {

        final BasicGui gui = new BasicGui("testTable") {

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
        this.setSearchEnabled(true);
        // TODO Auto-generated constructor stub
    }

    @Override
    public Dimension getPreferredSize() {
        final Dimension ret = super.getPreferredSize();
        System.out.println(ret);
        return ret;
    }
}
