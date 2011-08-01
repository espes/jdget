package org.appwork.utils.swing.table;

import java.util.ArrayList;
import java.util.Collections;
import java.util.regex.Pattern;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableCellEditor;

import org.appwork.resources.AWUTheme;
import org.appwork.storage.JSonStorage;
import org.appwork.storage.Storage;
import org.appwork.utils.logging.Log;
import org.appwork.utils.swing.EDTHelper;
import org.appwork.utils.swing.EDTRunner;

/**
 * NOTE about Selection issues: in case you use fireTableXYEvents, it will clear
 * the SelectionModel and you will loose your current ongoing Selection! you
 * have to save anchor/lead of selectionmodel, then fire event, restore saved
 * selections and then set anchor/lead again
 * 
 * @author daniel
 * 
 * @param <E>
 */
public abstract class ExtTableModel<E> extends AbstractTableModel {

    /**
     * 
     */
    public static final String                             SORT_ORDER_ID_KEY = "SORT_ORDER_ID";
    /**
     * 
     */
    public static final String                             SORTCOLUMN_KEY    = "SORTCOLUMN";
    /**
     * 
     */
    private static final long                              serialVersionUID  = 939549808899567618L;
    /**
     * complete table structure has changed
     */
    protected static final int                             UPDATE_STRUCTURE  = 1;
    /**
     * Column instances
     */
    protected ArrayList<ExtColumn<E>>                      columns           = new ArrayList<ExtColumn<E>>();

    /**
     * Modelid to have an seperate key for database savong
     */
    private final String                                   modelID;

    /**
     * the table that uses this model
     */
    private ExtTable<E>                                    table             = null;

    /**
     * a list of objects. Each object represents one table row
     */
    protected ArrayList<E>                                 tableData         = new ArrayList<E>();

    protected ExtColumn<E>                                 sortColumn;

    private final ArrayList<ExtComponentRowHighlighter<E>> extComponentRowHighlighters;

    private final ImageIcon                                iconAsc;
    private final ImageIcon                                iconDesc;

    /**
     * Create a new ExtTableModel.
     * 
     * @param database
     *            databaseinterface instance to store internal tabledata across
     *            sessions
     * @param id
     *            storageID.
     */
    public ExtTableModel(final String id) {
        super();
        this.extComponentRowHighlighters = new ArrayList<ExtComponentRowHighlighter<E>>();
        this.modelID = id;
        this.initColumns();
        this.iconAsc = AWUTheme.I().getIcon("sortAsc", -1);
        this.iconDesc = AWUTheme.I().getIcon("sortDesc", -1);
        final ExtColumn<E> defSortColumn = this.getDefaultSortColumn();
        String columnId = defSortColumn == null ? null : defSortColumn.getID();
        if (this.isSortStateSaverEnabled()) {
            columnId = this.getStorage().get(ExtTableModel.SORTCOLUMN_KEY, columnId);
        }
        if (columnId != null) {
            for (final ExtColumn<E> col : this.columns) {
                if (col.getID().equals(columnId)) {
                    this.sortColumn = col;
                    break;
                }
            }
        }

        try {
            this.refreshSort();
        } catch (final NullPointerException e) {
            // sortcolumn can be null now.

            Log.exception(e);
        }

    }

    public void _fireTableStructureChanged(ArrayList<E> newtableData, final boolean refreshSort) {
        if (refreshSort) {
            newtableData = this.refreshSort(newtableData);
        }
        final ArrayList<E> newdata = newtableData;
        final ArrayList<E> selection = new EDTHelper<ArrayList<E>>() {

            @Override
            public ArrayList<E> edtRun() {
                return ExtTableModel.this.getSelectedObjects();
            }

        }.getReturnValue();
        if (selection != null) {
            selection.retainAll(newdata);
        }
        new EDTRunner() {
            @Override
            protected void runInEDT() {
                final int anchor = ExtTableModel.this.getTable().getSelectionModel().getAnchorSelectionIndex();
                final int lead = ExtTableModel.this.getTable().getSelectionModel().getLeadSelectionIndex();
                ExtTableModel.this.tableData = newdata;
                ExtTableModel.this.fireTableStructureChanged();
                if (ExtTableModel.this.tableData.size() > 0) {
                    ExtTableModel.this.setSelectedObjects(selection);
                    ExtTableModel.this.getTable().getSelectionModel().setAnchorSelectionIndex(anchor);
                    ExtTableModel.this.getTable().getSelectionModel().setLeadSelectionIndex(lead);
                }
            }
        };
    }

    /**
     * @param files
     */
    public void addAllElements(final E... files) {
        new EDTHelper<Object>() {
            @Override
            public Object edtRun() {
                final ArrayList<E> selection = ExtTableModel.this.getSelectedObjects();
                for (final E e : files) {
                    ExtTableModel.this.tableData.add(e);
                }

                ExtTableModel.this.refreshSort();
                ExtTableModel.this.fireTableStructureChanged();
                ExtTableModel.this.setSelectedObjects(selection);
                return null;
            }
        }.start();

    }

    /**
     * Add a new Column to the model
     * 
     * @param e
     * @see #columns
     */
    public void addColumn(final ExtColumn<E> e) {
        e.setModel(this);
        this.columns.add(e);
    }

    /**
     * Adds a new column at the given index
     * 
     * @param e
     * @param index
     * @see #addColumn(ExtColumn)
     */
    public void addColumn(final ExtColumn<E> e, final int index) {
        e.setModel(this);
        this.columns.add(index, e);
    }

    /**
     * @param at
     */
    public void addElement(final E at) {
        new EDTHelper<Object>() {
            @Override
            public Object edtRun() {
                final ArrayList<E> selection = ExtTableModel.this.getSelectedObjects();
                ExtTableModel.this.tableData.add(at);
                ExtTableModel.this.refreshSort();
                ExtTableModel.this.fireTableStructureChanged();
                ExtTableModel.this.setSelectedObjects(selection);
                return null;
            }
        }.start();
    }

    public synchronized void addExtComponentRowHighlighter(final ExtComponentRowHighlighter<E> h) {

        this.extComponentRowHighlighters.add(h);
    }

    /**
     * 
     */
    public void clear() {
        new EDTHelper<Object>() {
            @Override
            public Object edtRun() {
                ExtTableModel.this.tableData = new ArrayList<E>();
                ExtTableModel.this.fireTableStructureChanged();
                return null;
            }
        }.start();
    }

    /**
     * clears all selection models
     */
    public void clearSelection() {
        if (this.table == null) { return; }
        this.table.getSelectionModel().clearSelection();
        this.table.getColumnModel().getSelectionModel().clearSelection();
    }

    /**
     * @param at
     * @return
     */
    public boolean contains(final E at) {
        return this.tableData.contains(at);
    }

    /**
     * Returns the Celleditor for the given column
     * 
     * @param convertColumnIndexToModel
     * @return
     */
    public TableCellEditor getCelleditorByColumn(final int columnIndex) {
        /*
         * Math.max(0, columnIndex)
         * 
         * WORKAROUND for -1 column access,Index out of Bound,Unknown why it
         * happens but this workaround seems to do its job
         */
        return this.columns.get(Math.max(0, columnIndex));
    }

    /**
     * Returns the Cellrenderer for this column
     * 
     * @param columnIndex
     * @return
     */
    public ExtColumn<E> getCellrendererByColumn(final int columnIndex) {
        /*
         * Math.max(0, columnIndex)
         * 
         * WORKAROUND for -1 column access,Index out of Bound,Unknown why it
         * happens but this workaround seems to do its job
         */
        return this.columns.get(Math.max(0, columnIndex));
    }

    @SuppressWarnings("unchecked")
    public <T extends ExtColumn<E>> T getColumnByClass(final Class<T> clazz) {
        try {
            for (final ExtColumn<?> column : this.columns) {
                if (column.getClass().equals(clazz)) { return (T) column; }
            }
        } catch (final Exception e) {
            Log.exception(e);
        }
        return null;
    }

    @Override
    public Class<?> getColumnClass(final int columnIndex) {
        return Object.class;
    }

    /**
     * @return Returns the number of columns defined (incl. invisible columns)
     */
    public int getColumnCount() {
        return this.columns.size();
    }

    /**
     * @return the columns headername
     * @see ExtColumn#getName()
     */
    @Override
    public String getColumnName(final int column) {
        /*
         * Math.max(0, columnIndex)
         * 
         * WORKAROUND for -1 column access,Index out of Bound,Unknown why it
         * happens but this workaround seems to do its job
         */
        return this.columns.get(Math.max(0, column)).getName();
    }

    public ArrayList<ExtColumn<E>> getColumns() {
        return this.columns;
    }

    /**
     * @return the default sort column. override
     *         {@link #isSortStateSaverEnabled()} to return to default sort on
     *         each session
     */
    protected ExtColumn<E> getDefaultSortColumn() {
        for (final ExtColumn<E> c : this.columns) {
            if (c.isSortable(null)) { return c; }
        }
        return null;
    }

    /**
     * @param i
     * @return
     */
    public E getElementAt(final int i) {
        return this.tableData.get(i);
    }

    /**
     * @return
     */
    public ArrayList<E> getElements() {
        return new ArrayList<E>(this.tableData);
    }

    /**
     * Returns the Columninstance
     * 
     * @param columnIndex
     * @return
     */
    public ExtColumn<E> getExtColumn(final int columnIndex) {
        return this.columns.get(Math.max(0, columnIndex));
    }

    /**
     * @return a list of all ExtComponentRowHighlighters
     */
    public ArrayList<ExtComponentRowHighlighter<E>> getExtComponentRowHighlighters() {
        // TODO Auto-generated method stub
        return this.extComponentRowHighlighters;
    }

    /**
     * @return the modelID
     */
    public String getModelID() {
        return this.modelID;
    }

    /**
     * @param sortOrderIdentifier
     */
    public String getNextSortIdentifier(final String sortOrderIdentifier) {
        if (sortOrderIdentifier == null || sortOrderIdentifier.equals(ExtColumn.SORT_ASC)) {
            return ExtColumn.SORT_DESC;
        } else {
            return ExtColumn.SORT_ASC;
        }

    }

    /**
     * Returns the object that represents the row
     * 
     * @param index
     * @return
     */
    public E getObjectbyRow(final int index) {

        synchronized (this.tableData) {
            if (index >= 0 && index < this.tableData.size()) { return this.tableData.get(index); }
            return null;
        }
    }

    /**
     * Returns how many rows the model contains
     * 
     * @see #tableData
     */
    public int getRowCount() {
        return this.tableData.size();
    }

    /**
     * Returns the row index for a given Object
     * 
     * @param o
     * @return
     */
    public int getRowforObject(final E o) {
        synchronized (this.tableData) {
            return this.tableData.indexOf(o);
        }
    }

    /**
     * Returns all selected Objects
     * 
     * @return
     */
    public ArrayList<E> getSelectedObjects() {
        if (this.table == null) { return new ArrayList<E>(0); }
        final int[] rows = this.table.getSelectedRows();
        final ArrayList<E> ret = new ArrayList<E>(rows.length);
        final ArrayList<E> data = this.tableData;
        for (final int row : rows) {
            if (row >= data.size() || row < 0) {
                continue;
            }
            final E elem = data.get(row);
            if (elem != null) {
                ret.add(elem);
            }
        }
        return ret;
    }

    /**
     * Returns the currently row sort column
     * 
     * @return the {@link ExtTableModel#sortColumn}
     * @see ExtTableModel#sortColumn
     */
    public ExtColumn<E> getSortColumn() {
        return this.sortColumn;
    }

    /**
     * @param sortOrderIdentifier
     * @return
     */
    public Icon getSortIcon(final String sortOrderIdentifier) {
        if (sortOrderIdentifier == null || sortOrderIdentifier == ExtColumn.SORT_ASC) {
            return this.iconAsc;
        } else {
            return this.iconDesc;
        }
    }

    /**
     * @return
     */
    protected Storage getStorage() {
        return JSonStorage.getStorage("ExtTableModel_" + this.modelID);
    }

    /**
     * @return the {@link ExtTableModel#table}
     * @see ExtTableModel#table
     */
    public ExtTable<E> getTable() {
        return this.table;
    }

    /**
     * @return the {@link ExtTableModel#tableData}
     * @see ExtTableModel#tableData
     */
    public ArrayList<E> getTableData() {
        return this.tableData;
    }

    /**
     * returns a copy of current objects in tablemodel
     * 
     * @return
     */
    public ArrayList<E> getTableObjects() {
        final ArrayList<E> ret = new ArrayList<E>(this.tableData);
        return ret;
    }

    /**
     * Returns the object for row 'rowIndex'. IN ExtTableModel, each row is
     * represented By one single object. the ExtColums renderer just renders
     * each object in its way
     */
    public E getValueAt(final int rowIndex, final int columnIndex) {
        try {
            return this.tableData.get(rowIndex);
        } catch (final IndexOutOfBoundsException e) {
            return null;
        }
    }

    /**
     * Initialize the colums.
     * 
     * e.g.: this.addColumn(new NameColumn("Name", this));<br>
     * this.addColumn(new UploadDateColumn("Upload date", this));<br>
     * this.addColumn(new FilesizeColumn("Size", this));<br>
     * 
     */
    protected abstract void initColumns();

    /**
     * @return if the cell is editable. This information is stored in the
     *         ExtColumn instance.
     * @see ExtColumn#isCellEditable(int, int)
     */
    @Override
    public boolean isCellEditable(final int rowIndex, final int columnIndex) {
        return this.columns.get(columnIndex).isCellEditable(rowIndex, columnIndex);
    }

    /**
     * checks if this column is allowed to be hidden
     * 
     * @param column
     * @return
     */
    public boolean isHidable(final int column) {
        final ExtColumn<E> col = this.getExtColumn(column);
        try {
            return col.isHidable();
        } catch (final Exception e) {
            Log.exception(e);
            return true;
        }
    }

    protected boolean isSortStateSaverEnabled() {
        return true;
    }

    /**
     * Retrieves visible information form database interface to determine if the
     * column is visible or not
     * 
     * @param column
     * @return
     */
    public boolean isVisible(final int column) {
        final ExtColumn<E> col = this.getExtColumn(column);
        try {
            return JSonStorage.getStorage("ExtTableModel_" + this.modelID).get("VISABLE_COL_" + col.getName(), col.isDefaultVisible());
        } catch (final Exception e) {
            Log.exception(e);
            return true;
        }
    }

    /**
     * Restores the sort order according to {@link #getSortColumn()} and
     * {@link #getSortOrderIdentifier() == ExtColumn.SORT_ASC}
     * 
     * 
     */
    /* better sort outside edt and then set new,sorted table data */
    @Deprecated
    public void refreshSort() {
        this.refreshSort(this.getTableData());
    }

    public ArrayList<E> refreshSort(final ArrayList<E> data) {
        return this.sort(data, this.sortColumn);
    }

    /**
     * @param selectedObjects
     */
    public void removeAll(final ArrayList<E> selectedObjects) {
        final ArrayList<E> tmp = new ArrayList<E>(this.tableData);
        tmp.removeAll(selectedObjects);
        final ArrayList<E> selection = new EDTHelper<ArrayList<E>>() {

            @Override
            public ArrayList<E> edtRun() {
                return ExtTableModel.this.getSelectedObjects();
            }

        }.getReturnValue();
        if (selection != null) {
            selection.removeAll(selectedObjects);
        }
        new EDTHelper<Object>() {
            @Override
            public Object edtRun() {
                ExtTableModel.this.tableData = tmp;
                ExtTableModel.this.refreshSort();
                ExtTableModel.this.fireTableStructureChanged();
                if (ExtTableModel.this.tableData.size() > 0) {
                    ExtTableModel.this.setSelectedObjects(selection);
                }
                return null;
            }
        }.start();

    }

    /**
     * @param startRow
     * @param ret
     * @param caseSensitive
     * @param regex
     * @return
     */
    public E searchNextObject(final int startRow, final String ret, final boolean caseSensitive, final boolean regex) {

        Pattern p;
        if (!regex) {
            final String[] pats = ret.split("\\*");
            final StringBuilder pattern = new StringBuilder();
            for (final String pp : pats) {
                if (pattern.length() > 0) {
                    pattern.append(".*?");
                }
                pattern.append(Pattern.quote(pp));
            }
            p = Pattern.compile(".*?" + pattern.toString() + ".*?", caseSensitive ? Pattern.CASE_INSENSITIVE : 0 | Pattern.DOTALL);
        } else {
            p = Pattern.compile(".*?" + ret + ".*?", caseSensitive ? Pattern.CASE_INSENSITIVE : 0 | Pattern.DOTALL);
        }

        for (int i = startRow; i < this.tableData.size(); i++) {
            for (int c = 0; c < this.columns.size(); c++) {
                if (this.columns.get(c).matchSearch(this.tableData.get(i), p)) { return this.tableData.get(i); }
            }

        }
        for (int i = 0; i < startRow; i++) {
            for (int c = 0; c < this.columns.size(); c++) {
                if (this.columns.get(c).matchSearch(this.tableData.get(i), p)) { return this.tableData.get(i); }
            }

        }
        return null;

    }

    /**
     * @param column
     * @param b
     */
    public void setColumnVisible(final ExtColumn<E> column, final boolean visible) {

        try {
            JSonStorage.getStorage("ExtTableModel_" + this.modelID).put("VISABLE_COL_" + column.getName(), visible);
        } catch (final Exception e) {
            Log.exception(e);
        }
        new EDTRunner() {

            @Override
            protected void runInEDT() {
                ExtTableModel.this.getTable().createColumns();
                ExtTableModel.this.getTable().revalidate();
                ExtTableModel.this.getTable().repaint();
            }
        };

    }

    /**
     * Sets the column visible or invisible. This information is stored in
     * database interface for cross session use.
     * 
     * 
     * @param column
     * @param visible
     */
    public void setColumnVisible(final int column, final boolean visible) {

        this.setColumnVisible(this.getExtColumn(column), visible);

    }

    /**
     * @param latest
     */
    public void setSelectedObject(final E latest) {
        if (this.table == null) { return; }
        new EDTHelper<Object>() {
            @Override
            public Object edtRun() {
                if (ExtTableModel.this.table == null) { return null; }
                ExtTableModel.this.clearSelection();
                if (latest == null) { return null; }
                final int row = ExtTableModel.this.getRowforObject(latest);
                if (row >= 0) {
                    ExtTableModel.this.table.addRowSelectionInterval(row, row);
                }
                return null;
            }
        }.start();
    }

    /**
     * Sets the current selection to the given objects
     * 
     * @param selections
     */
    public void setSelectedObjects(final ArrayList<E> selections) {
        if (this.table == null) { return; }
        if (selections == null || selections.size() == 0) { return; }
        new EDTHelper<Object>() {
            @Override
            public Object edtRun() {
                if (ExtTableModel.this.table == null) { return null; }
                if (selections == null || selections.size() == 0) {
                    ExtTableModel.this.clearSelection();
                    return null;
                }
                // Transform to rowindex list
                final ArrayList<Integer> selectedRows = new ArrayList<Integer>();
                int rowIndex = -1;
                for (final E obj : selections) {
                    rowIndex = ExtTableModel.this.getRowforObject(obj);
                    if (rowIndex >= 0) {
                        selectedRows.add(rowIndex);
                    }
                }
                Collections.sort(selectedRows);
                for (final Integer row : selectedRows) {
                    ExtTableModel.this.table.addRowSelectionInterval(row, row);
                }
                return null;
            }
        }.start();
    }

    /**
     * Sets the table in which the model is used. This method should only be
     * used internally.
     * 
     * @param table
     */
    protected void setTable(final ExtTable<E> table) {
        this.table = table;
    }

    // TODO docu
    @Override
    public void setValueAt(final Object value, final int rowIndex, final int columnIndex) {
        this.columns.get(columnIndex).setValueAt(value, rowIndex, columnIndex);
    }

    /**
     * @return
     */
    public int size() {
        return this.tableData.size();
    }

    /**
     * Sorts given modeldata with the column's rowsorter
     * 
     **/
    public ArrayList<E> sort(final ArrayList<E> data, final ExtColumn<E> column) {
        this.sortColumn = column;

        if (column != null) {
            final String id = column.getSortOrderIdentifier();

            try {
                this.getStorage().put(ExtTableModel.SORT_ORDER_ID_KEY, id);
                this.getStorage().put(ExtTableModel.SORTCOLUMN_KEY, column.getID());
            } catch (final Exception e) {
                Log.exception(e);
            }
            Collections.sort(data, column.getRowSorter());
        }
        return data;
    }
}
