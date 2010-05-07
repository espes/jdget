package org.appwork.utils.swing.table;

import java.util.ArrayList;
import java.util.Collections;
import java.util.regex.Pattern;

import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableCellEditor;

import org.appwork.storage.ConfigInterface;
import org.appwork.utils.logging.Log;
import org.appwork.utils.swing.EDTHelper;

public abstract class ExtTableModel<E> extends AbstractTableModel {

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
    protected ArrayList<ExtColumn<E>> columns = new ArrayList<ExtColumn<E>>();

    /**
     * Modelid to have an seperate key for database savong
     */
    private String modelID;

    /**
     * the table that uses this model
     */
    private ExtTable<E> table = null;

    /**
     * a list of objects. Each object represents one table row
     */
    protected ArrayList<E> tableData = new ArrayList<E>();
    private ExtColumn<E> sortColumn;
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

        String columnId = ConfigInterface.getStorage("ExtTableModel_" + modelID).get("SORTCOLUMN", this.columns.get(0).getID());
        for (ExtColumn<E> col : columns) {
            if (col.getID().equals(columnId)) {
                sortColumn = col;
                break;
            }
        }
        sortOrderToggle = ConfigInterface.getStorage("ExtTableModel_" + modelID).get("SORTORDER", false);
        this.refreshSort();
    }

    /**
     * Add a new Column to the model
     * 
     * @param e
     * @see #columns
     */
    public void addColumn(ExtColumn<E> e) {
        e.setModel(this);
        columns.add(e);
    }

    @SuppressWarnings("unchecked")
    public <T extends ExtColumn<E>> T getColumnByClass(Class<T> clazz) {
        try {
            for (ExtColumn<?> column : columns) {
                if (column.getClass().equals(clazz)) return (T) column;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
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
    public ArrayList<E> getSelectedObjects() {
        ArrayList<E> ret = new ArrayList<E>();
        int[] rows = table.getSelectedRows();
        for (int row : rows) {
            E elem = (E) getValueAt(row, 0);
            if (elem != null) ret.add(elem);
        }
        return ret;
    }

    /**
     * returns a copy of current objects in tablemodel
     * 
     * @return
     */
    public ArrayList<E> getTableObjects() {
        ArrayList<E> ret = new ArrayList<E>();
        ret.addAll(tableData);
        return ret;
    }

    /**
     * @param latest
     */
    public void setSelectedObject(final E latest) {

        new EDTHelper<Object>() {
            @Override
            public Object edtRun() {
                if (latest == null) {
                    clearSelection();
                    return null;
                }
                clearSelection();
                int row = getRowforObject(latest);
                table.addRowSelectionInterval(row, row);
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
                for (E obj : selections) {
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
    public void addColumn(ExtColumn<E> e, int index) {
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
    public ExtColumn<E> getCellrendererByColumn(int columnIndex) {
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
    public ExtColumn<E> getExtColumn(int columnIndex) {
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
    public ExtTable<E> getTable() {
        return table;
    }

    /**
     * @return the {@link ExtTableModel#tableData}
     * @see ExtTableModel#tableData
     */
    public ArrayList<E> getTableData() {
        return tableData;
    }

    /**
     * Returns the object for row 'rowIndex'. IN ExtTableModel, each row is
     * represented By one single object. the ExtColums renderer just renders
     * each object in its way
     */
    public E getValueAt(int rowIndex, int columnIndex) {
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

        ExtColumn<E> col = getExtColumn(column);
        try {
            return ConfigInterface.getStorage("ExtTableModel_" + modelID).get("VISABLE_COL_" + col.getName(), col.isDefaultVisible());
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
    protected void setTable(ExtTable<E> table) {
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
        ExtColumn<E> col = getExtColumn(column);
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
    public E getObjectbyRow(int index) {

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
    public int getRowforObject(E o) {
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
    public void sort(ExtColumn<E> column, boolean sortOrderToggle) {
        this.sortColumn = column;
        this.sortOrderToggle = sortOrderToggle;

        try {
            ConfigInterface.getStorage("ExtTableModel_" + modelID).put("SORTCOLUMN", column.getID());
            ConfigInterface.getStorage("ExtTableModel_" + modelID).put("SORTORDER", sortOrderToggle);
        } catch (Exception e) {
            Log.exception(e);
        }
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
    public ExtColumn<E> getSortColumn() {
        return sortColumn;
    }

    /**
     * @param startRow
     * @param ret
     * @param caseSensitive
     * @param regex
     * @return
     */
    public E searchNextObject(int startRow, String ret, boolean caseSensitive, boolean regex) {

        Pattern p;
        if (!regex) {
            String[] pats = ret.split("\\*");
            StringBuilder pattern = new StringBuilder();
            for (String pp : pats) {
                if (pattern.length() > 0) {
                    pattern.append(".*?");
                }
                pattern.append(Pattern.quote(pp));
            }
            p = Pattern.compile(".*?" + pattern.toString() + ".*?", caseSensitive ? Pattern.CASE_INSENSITIVE : 0 | Pattern.DOTALL);
        } else {
            p = Pattern.compile(".*?" + ret + ".*?", caseSensitive ? Pattern.CASE_INSENSITIVE : 0 | Pattern.DOTALL);
        }

        for (int i = startRow; i < tableData.size(); i++) {
            for (int c = 0; c < this.columns.size(); c++) {
                if (columns.get(c).matchSearch(tableData.get(i), p)) { return tableData.get(i); }
            }

        }
        for (int i = 0; i < startRow; i++) {
            for (int c = 0; c < this.columns.size(); c++) {
                if (columns.get(c).matchSearch(tableData.get(i), p)) { return tableData.get(i); }
            }

        }
        return null;

    }

    /**
     * @param selectedObjects
     */
    @SuppressWarnings("unchecked")
    public void removeAll(ArrayList<E> selectedObjects) {

        final ArrayList<E> tmp = (ArrayList<E>) tableData.clone();
        tmp.removeAll(selectedObjects);

        new EDTHelper<Object>() {
            @Override
            public Object edtRun() {
                final ArrayList<E> selection = ExtTableModel.this.getSelectedObjects();
                tableData = tmp;
                refreshSort();
                fireTableStructureChanged();

                setSelectedObjects(selection);
                return null;
            }
        }.start();

    }

    /**
     * 
     */
    public void clear() {
        new EDTHelper<Object>() {
            @Override
            public Object edtRun() {
                tableData.clear();
                fireTableStructureChanged();
                return null;
            }
        }.start();
    }

    /**
     * @return
     */
    public int size() {
        // TODO Auto-generated method stub
        return tableData.size();
    }

    /**
     * @param i
     * @return
     */
    public E getElementAt(int i) {
        // TODO Auto-generated method stub
        return tableData.get(i);
    }

    /**
     * @param at
     * @return
     */
    public boolean contains(E at) {
        // TODO Auto-generated method stub
        return tableData.contains(at);
    }

    /**
     * @param at
     */
    public void addElement(final E at) {
        new EDTHelper<Object>() {
            @Override
            public Object edtRun() {
                final ArrayList<E> selection = ExtTableModel.this.getSelectedObjects();
                tableData.add(at);
                refreshSort();
                fireTableStructureChanged();

                setSelectedObjects(selection);
                return null;
            }
        }.start();
    }

    /**
     * @param files
     */
    @SuppressWarnings("unchecked")
    public void addAllElements(ArrayList<E> files) {
        final ArrayList<E> tmp = (ArrayList<E>) files.clone();
        new EDTHelper<Object>() {
            @Override
            public Object edtRun() {
                final ArrayList<E> selection = ExtTableModel.this.getSelectedObjects();
                tableData.addAll(tmp);
                refreshSort();
                fireTableStructureChanged();
                setSelectedObjects(selection);
                return null;
            }
        }.start();

    }

    /**
     * @return
     */
    @SuppressWarnings("unchecked")
    public ArrayList<E> getElements() {
        // TODO Auto-generated method stub
        return (ArrayList<E>) tableData.clone();
    }

}
