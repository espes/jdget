package org.appwork.utils.swing.table.test;

import java.awt.Dimension;

import javax.swing.JScrollPane;

import org.appwork.app.gui.BasicGui;
import org.appwork.utils.swing.table.ExtTable;

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
        this.setSearchEnabled(true);
    }

    @Override
    public Dimension getPreferredSize() {
        final Dimension ret = super.getPreferredSize();
        System.out.println(ret);
        return ret;
    }
}
