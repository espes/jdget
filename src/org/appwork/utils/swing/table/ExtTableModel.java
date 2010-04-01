package org.appwork.utils.swing.table;

import java.util.ArrayList;
import java.util.Collections;

import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableCellEditor;

import org.appwork.storage.ConfigInterface;
import org.appwork.utils.logging.Log;
import org.appwork.utils.swing.EDTHelper;

public abstract class ExtTableModel extends AbstractTableModel {

    /**
     * 
     */
    private static final long serialVersionUID = 939549808899567618L;
    /**
     * complete table structure has changed
     */
    protected static final int UPDATE_STRUCTURE = 1;
    /**
     * Column instances
     */
    protected ArrayList<ExtColumn> columns = new ArrayList<ExtColumn>();

    /**
     * Modelid to have an seperate key for database savong
     */
    private String modelID;

    /**
     * the table that uses this model
     */
    private ExtTable table = null;

    /**
     * a list of objects. Each object represents one table row
     */
    protected ArrayList<Object> tableData = new ArrayList<Object>();
    private ExtColumn sortColumn;
    private boolean sortOrderToggle = true;

    /**
     * Create a new ExtTableModel.
     * 
     * @param database
     *            databaseinterface instance to store internal tabledata across
     *            sessions
     * @param id
     *            storageID.
     */
    public ExtTableModel(String id) {
        super();

        this.modelID = id;
        initColumns();
    }

    /**
     * Add a new Column to the model
     * 
     * @param e
     * @see #columns
     */
    public void addColumn(ExtColumn e) {
        e.setModel(this);
        columns.add(e);
    }

    /**
     * clears all selection models
     */
    public void clearSelection() {
        table.getSelectionModel().clearSelection();
        table.getColumnModel().getSelectionModel().clearSelection();
    }

    /**
     * Returns all selected Objects
     * 
     * @return
     */
    public ArrayList<Object> getSelectedObjects() {
        ArrayList<Object> ret = new ArrayList<Object>();
        int[] rows = table.getSelectedRows();
        for (int row : rows) {
            Object elem = getValueAt(row, 0);
            if (elem != null) ret.add(elem);
        }
        return ret;
    }

    /**
     * returns a copy of current objects in tablemodel
     * 
     * @return
     */
    public ArrayList<Object> getTableObjects() {
        ArrayList<Object> ret = new ArrayList<Object>();
        ret.addAll(tableData);
        return ret;
    }

    /**
     * Sets the current selection to the given objects
     * 
     * @param selections
     */
    public void setSelectedObjects(final ArrayList<Object> selections) {
        new EDTHelper<Object>() {
            @Override
            public Object edtRun() {
                if (selections == null) {
                    clearSelection();
                    return null;
                }
                if (selections.size() == 0) return null;
                // Transform to rowindex list
                ArrayList<Integer> selectedRows = new ArrayList<Integer>();
                int rowIndex = -1;
                for (Object obj : selections) {
                    rowIndex = getRowforObject(obj);
                    if (rowIndex >= 0) selectedRows.add(rowIndex);
                }
                Collections.sort(selectedRows);
                for (Integer row : selectedRows) {
                    table.addRowSelectionInterval(row, row);
                }
                return null;
            }
        }.start();
    }

    /**
     * Adds a new column at the given index
     * 
     * @param e
     * @param index
     * @see #addColumn(ExtColumn)
     */
    public void addColumn(ExtColumn e, int index) {
        e.setModel(this);
        columns.add(index, e);
    }

    /**
     * Returns the Celleditor for the given column
     * 
     * @param convertColumnIndexToModel
     * @return
     */
    public TableCellEditor getCelleditorByColumn(int columnIndex) {
        return columns.get(columnIndex);
    }

    /**
     * Returns the Cellrenderer for this column
     * 
     * @param columnIndex
     * @return
     */
    public ExtColumn getCellrendererByColumn(int columnIndex) {
        return columns.get(columnIndex);
    }

    @Override
    public Class<?> getColumnClass(int columnIndex) {
        return Object.class;
    }

    /**
     * @return Returns the number of columns defined (incl. invisible columns)
     */
    public int getColumnCount() {
        return columns.size();
    }

    /**
     * @return the columns headername
     * @see ExtColumn#getName()
     */
    @Override
    public String getColumnName(int column) {
        return columns.get(column).getName();
    }

    /**
     * Returns the Columninstance
     * 
     * @param columnIndex
     * @return
     */
    public ExtColumn getExtColumn(int columnIndex) {
        return columns.get(columnIndex);
    }

    /**
     * Returns how many rows the model contains
     * 
     * @see #tableData
     */
    public int getRowCount() {
        return tableData.size();
    }

    /**
     * @return the {@link ExtTableModel#table}
     * @see ExtTableModel#table
     */
    public ExtTable getTable() {
        return table;
    }

    /**
     * @return the {@link ExtTableModel#tableData}
     * @see ExtTableModel#tableData
     */
    public ArrayList<Object> getTableData() {
        return tableData;
    }

    /**
     * Returns the object for row 'rowIndex'. IN ExtTableModel, each row is
     * represented By one single object. the ExtColums renderer just renders
     * each object in its way
     */
    public Object getValueAt(int rowIndex, int columnIndex) {
        try {
            return tableData.get(rowIndex);
        } catch (IndexOutOfBoundsException e) {
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
    public boolean isCellEditable(int rowIndex, int columnIndex) {
        return columns.get(columnIndex).isCellEditable(rowIndex, columnIndex);
    }

    /**
     * Retrieves visible information form database interface to determine if the
     * column is visible or not
     * 
     * @param column
     * @return
     */
    public boolean isVisible(int column) {

        ExtColumn col = getExtColumn(column);
        try {
            return ConfigInterface.getStorage("ExtTableModel_" + modelID).get("VISABLE_COL_" + col.getName(), col.defaultVisible());
        } catch (Exception e) {
            Log.exception(e);
            return true;
        }
    }

    /**
     * Sets the table in which the model is used. This method should only be
     * used internally.
     * 
     * @param table
     */
    protected void setTable(ExtTable table) {
        this.table = table;
    }

    // TODO docu
    @Override
    public void setValueAt(Object value, int rowIndex, int columnIndex) {
        columns.get(columnIndex).setValueAt(value, rowIndex, columnIndex);
    }

    /**
     * Sets the column visible or invisible. This information is stored in
     * database interface for cross session use.
     * 
     * 
     * @param column
     * @param visible
     */
    public void setVisible(int column, boolean visible) {
        ExtColumn col = getExtColumn(column);
        try {
            ConfigInterface.getStorage("ExtTableModel_" + modelID).put("VISABLE_COL_" + col.getName(), visible);
        } catch (Exception e) {
            Log.exception(e);
        }

    }

    /**
     * Returns the object that represents the row
     * 
     * @param index
     * @return
     */
    public Object getObjectbyRow(int index) {

        synchronized (tableData) {
            if (index >= 0 && index < tableData.size()) return tableData.get(index);
            return null;
        }
    }

    /**
     * Returns the row index for a given Object
     * 
     * @param o
     * @return
     */
    public int getRowforObject(Object o) {
        synchronized (tableData) {
            return tableData.indexOf(o);
        }
    }

    /**
     * Sorts the model with the column's rowsorter
     * 
     * @param column
     * @param sortOrderToggle
     */
    public void sort(ExtColumn column, boolean sortOrderToggle) {
        this.sortColumn = column;
        this.sortOrderToggle = sortOrderToggle;
        Collections.sort(getTableData(), column.getRowSorter(sortOrderToggle));
    }

    /**
     * Restores the sort order according to {@link #getSortColumn()} and
     * {@link #isSortOrderToggle()}
     * 
     * 
     */
    public void refreshSort() {
        sort(sortColumn == null ? this.getExtColumn(0) : sortColumn, sortOrderToggle);
    }

    /**
     * Returns the current sortOrderToggle
     * 
     * @return the {@link ExtTableModel#sortOrderToggle}
     * @see ExtTableModel#sortOrderToggle
     */
    public boolean isSortOrderToggle() {
        return sortOrderToggle;
    }

    /**
     * Returns the currently row sort column
     * 
     * @return the {@link ExtTableModel#sortColumn}
     * @see ExtTableModel#sortColumn
     */
    public ExtColumn getSortColumn() {
        return sortColumn;
    }
}
