/**
 * Copyright (c) 2009 - 2011 AppWork UG(haftungsbeschränkt) <e-mail@appwork.org>
 * 
 * This file is part of org.appwork.utils.swing.table.test
 * 
 * This software is licensed under the Artistic License 2.0,
 * see the LICENSE file or http://www.opensource.org/licenses/artistic-license-2.0.php
 * for details
 */
package org.appwork.swing.exttable.test;

import javax.swing.Icon;

import org.appwork.resources.AWUTheme;
import org.appwork.swing.exttable.ExtTableModel;
import org.appwork.swing.exttable.columns.ExtTextColumn;

/**
 * @author thomas
 * 
 */
public class ExtTestModel extends ExtTableModel<TextObject> {

    /**
     * 
     */
    private static final long serialVersionUID = 1L;

    /**
     * @param id
     */
    public ExtTestModel() {
        super(ExtTestModel.class.getName() + "_" + 1);
        for (int i = 0; i < 100; i++) {
            this.addElement(new TextObject("a" + i, "b" + i, i + "c"));
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.appwork.utils.swing.table.ExtTableModel#initColumns()
     */
    @Override
    protected void initColumns() {
        this.addColumn(new ExtTextColumn<TextObject>("col 1") {

            private static final long serialVersionUID = 1L;

            @Override
            public int getDefaultWidth() {
                return 40;
            }

            @Override
            protected Icon getIcon(final TextObject value) {
                return AWUTheme.getInstance().getIcon("dialog/help", 14);
            }

            @Override
            public String getStringValue(final TextObject value) {
                return value.getA();
            }

        });
        this.addColumn(new ExtTextColumn<TextObject>("col 2") {

            private static final long serialVersionUID = 1L;

            @Override
            public int getDefaultWidth() {
                // TODO Auto-generated method stub
                return 80;
            }

            @Override
            public String getStringValue(final TextObject value) {
                return value.getB();
            }
        });
        this.addColumn(new ExtTextColumn<TextObject>("col 3") {

            private static final long serialVersionUID = 1L;

            @Override
            public int getDefaultWidth() {
                // TODO Auto-generated method stub
                return 120;
            }

            @Override
            public String getStringValue(final TextObject value) {
                return value.getC();
            }

            @Override
            protected boolean isDefaultResizable() {
                // TODO Auto-generated method stub
                return false;
            }
        });
        this.addColumn(new ExtTextColumn<TextObject>("col 4") {

            private static final long serialVersionUID = 1L;

            @Override
            public int getDefaultWidth() {
                // TODO Auto-generated method stub
                return 200;
            }

            @Override
            public String getStringValue(final TextObject value) {
                return value.getC() + value.getA();
            }

            @Override
            public boolean isDefaultVisible() {

                return false;
            }
        });
    }
}
