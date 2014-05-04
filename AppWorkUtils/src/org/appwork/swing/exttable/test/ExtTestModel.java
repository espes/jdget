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

import java.io.File;

import javax.swing.Icon;

import org.appwork.resources.AWUTheme;
import org.appwork.swing.exttable.ExtTableModel;
import org.appwork.swing.exttable.columns.ExtFileBrowser;
import org.appwork.swing.exttable.columns.ExtTextColumn;
import org.appwork.utils.swing.dialog.Dialog;
import org.appwork.utils.swing.dialog.DialogCanceledException;
import org.appwork.utils.swing.dialog.DialogClosedException;
import org.appwork.utils.swing.dialog.ExtFileChooserDialog;
import org.appwork.utils.swing.dialog.FileChooserSelectionMode;
import org.appwork.utils.swing.dialog.FileChooserType;

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

        // this.addColumn(new ExtProgressColumn<TextObject>("BAR") {
        //
        // @Override
        // protected String getString(final TextObject value) {
        // // TODO Auto-generated method stub
        // return value.getRand() + " Tooltip";
        // }
        //
        // @Override
        // protected long getValue(final TextObject value) {
        // // TODO Auto-generated method stub
        // return value.getRand();
        // }
        //
        // @Override
        // protected boolean isIndeterminated(final TextObject value, final
        // boolean isSelected, final boolean hasFocus, final int row, final int
        // column) {
        // // TODO Auto-generated method stub
        // return isSelected;
        // }
        // });
        // this.addColumn(new ExtCircleProgressColumn<TextObject>("PIE") {
        //
        // @Override
        // protected String getString(final TextObject value) {
        // // TODO Auto-generated method stub
        // return null;
        // }
        //
        // @Override
        // protected long getValue(final TextObject value) {
        // // TODO Auto-generated method stub
        // return value.getRand();
        // }
        //
        // @Override
        // protected boolean isIndeterminated(final TextObject value, final
        // boolean isSelected, final boolean hasFocus, final int row, final int
        // column) {
        // // TODO Auto-generated method stub
        // return true;
        // }
        // });
        addColumn(new ExtFileBrowser<TextObject>("Browse me") {

            /**
             * 
             */
            private static final long serialVersionUID = -7233073890074043200L;

            @Override
            public File getFile(TextObject o) {
                // TODO Auto-generated method stub
                return o.getFile();
            }

            @Override
            protected void setFile(TextObject object, File newFile) {
                object.setFile(newFile);

            }

            @Override
            public File browse(TextObject object) {

                ExtFileChooserDialog d = new ExtFileChooserDialog(0, "Choose file", null, null);
                d.setFileSelectionMode(FileChooserSelectionMode.FILES_AND_DIRECTORIES);

                d.setType(FileChooserType.OPEN_DIALOG);
                d.setMultiSelection(false);
                try {
                    Dialog.I().showDialog(d);
                } catch (DialogClosedException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                } catch (DialogCanceledException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
                return d.getSelectedFile();
            }

        });
        this.addColumn(new ExtTextColumn<TextObject>("EDIT ME") {

            private static final long serialVersionUID = 1L;

            @Override
            public int getDefaultWidth() {

                return 40;
            }

            @Override
            public boolean isEnabled(TextObject obj) {
                return false;
            }

            @Override
            public boolean isEditable(final TextObject obj) {
                return true;
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
