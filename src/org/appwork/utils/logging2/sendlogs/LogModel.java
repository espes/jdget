package org.appwork.utils.logging2.sendlogs;

import java.text.DateFormat;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;

import org.appwork.swing.exttable.ExtColumn;
import org.appwork.swing.exttable.ExtDefaultRowSorter;
import org.appwork.swing.exttable.ExtTableModel;
import org.appwork.swing.exttable.columns.ExtCheckColumn;
import org.appwork.swing.exttable.columns.ExtTextColumn;

public class LogModel extends ExtTableModel<LogFolder> {

    public LogModel(final java.util.List<LogFolder> folders) {
        super("LogModel");
        tableData = folders;
        Collections.sort(folders, new Comparator<LogFolder>() {

            @Override
            public int compare(final LogFolder o1, final LogFolder o2) {
                return new Long(o2.getCreated()).compareTo(new Long(o1.getCreated()));
            }
        });
    }
 
    @Override
    protected void initColumns() {
        addColumn(new ExtCheckColumn<LogFolder>(T.T.LogModel_initColumns_x_()) {

            @Override
            protected boolean getBooleanValue(final LogFolder value) {
                return value.isSelected();
            }

            @Override
            public boolean isSortable(final LogFolder obj) {
                return false;
            }

            @Override
            public boolean isEditable(final LogFolder obj) {

                return true;
            }

            @Override
            protected void setBooleanValue(final boolean value, final LogFolder object) {
                object.setSelected(value);
            }
        });

        ExtTextColumn<LogFolder> sort;
        addColumn(sort = new ExtTextColumn<LogFolder>(T.T.LogModel_initColumns_time_()) {
            {
                setRowSorter(new ExtDefaultRowSorter<LogFolder>() {

                    @Override
                    public int compare(final LogFolder o1, final LogFolder o2) {
                        if (getSortOrderIdentifier() != ExtColumn.SORT_ASC) {
                            return new Long(o1.getCreated()).compareTo(new Long(o2.getCreated()));
                        } else {
                            return new Long(o2.getCreated()).compareTo(new Long(o1.getCreated()));
                        }

                    }

                });
            }

            @Override
            public boolean isSortable(final LogFolder obj) {
                return false;
            }

            @Override
            public String getStringValue(final LogFolder value) {

                final String from = DateFormat.getInstance().format(new Date(value.getCreated()));
                final String to = DateFormat.getInstance().format(new Date(value.getLastModified()));
                return T.T.LogModel_getStringValue_between_(from, to);
            }
        });

    }

}
