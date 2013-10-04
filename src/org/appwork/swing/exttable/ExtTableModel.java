package org.appwork.swing.exttable;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.regex.Pattern;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableCellEditor;

import org.appwork.exceptions.WTFException;
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
    public static final String                                  SORT_ORDER_ID_KEY   = "SORT_ORDER_ID";
    /**
     * 
     */
    public static final String                                  SORTCOLUMN_KEY      = "SORTCOLUMN";
    /**
     * 
     */
    private static final long                                   serialVersionUID    = 939549808899567618L;
    /**
     * complete table structure has changed
     */
    protected static final int                                  UPDATE_STRUCTURE    = 1;
    /**
     * Column instances
     */
    protected java.util.List<ExtColumn<E>>                      columns             = new ArrayList<ExtColumn<E>>();

    /**
     * Modelid to have an seperate key for database savong
     */
    private final String                                        modelID;

    /**
     * the table that uses this model
     */
    private ExtTable<E>                                         table               = null;

    /**
     * a list of objects. Each object represents one table row
     */
    protected List<E>                                           tableData           = new ArrayList<E>();

    protected ExtColumn<E>                                      sortColumn;

    private final java.util.List<ExtComponentRowHighlighter<E>> extComponentRowHighlighters;

    private final ImageIcon                                     iconAsc;
    private final ImageIcon                                     iconDesc;
    private final PropertyChangeListener                        replaceDelayer;
    private List<E>                                             delayedNewTableData = null;
    private boolean                                             debugTableModel     = false;

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
        this.iconAsc = AWUTheme.I().getIcon("exttable/sortAsc", -1);
        this.iconDesc = AWUTheme.I().getIcon("exttable/sortDesc", -1);
        final ExtColumn<E> defSortColumn = this.getDefaultSortColumn();
        String columnId = defSortColumn == null ? null : defSortColumn.getID();
        String columnSortMode = null;
        if (this.isSortStateSaverEnabled()) {
            columnId = this.getStorage().get(ExtTableModel.SORTCOLUMN_KEY, columnId);
            columnSortMode = this.getStorage().get(ExtTableModel.SORT_ORDER_ID_KEY, null);
            /*
             * restore sortMode by using shared StringObject so that String ==
             * String works
             */
            if (columnSortMode != null) {
                if (columnSortMode.equals(ExtColumn.SORT_ASC)) {
                    columnSortMode = ExtColumn.SORT_ASC;
                } else if (columnSortMode.equals(ExtColumn.SORT_DESC)) {
                    columnSortMode = ExtColumn.SORT_DESC;
                }
            }
        }
        if (columnId != null) {
            for (final ExtColumn<E> col : this.columns) {
                if (col.getID().equals(columnId)) {
                    col.setSortOrderIdentifier(columnSortMode);
                    this.sortColumn = col;
                    break;
                }
            }
        }
        /**
         * we use this PropertyChangeListener to avoid tableRefresh while the
         * table is in editing mode
         **/
        this.replaceDelayer = new PropertyChangeListener() {

            @Override
            public void propertyChange(final PropertyChangeEvent evt) {
                if ("tableCellEditor".equalsIgnoreCase(evt.getPropertyName()) && evt.getNewValue() == null) {
                    /*
                     * tableCellEditor is null again, now we can refresh the
                     * TableData and Selection
                     */
                    final ExtTable<E> ltable = ExtTableModel.this.getTable();
                    if (ltable != null) {
                        ltable.removePropertyChangeListener(this);
                    }

                    ExtTableModel.this._replaceTableData(ExtTableModel.this.delayedNewTableData, false);
                }
            }

        };
    }

    public void _fireTableStructureChanged(final List<E> newtableData, final boolean refreshSort) {
        if (this.isDebugTableModel() && SwingUtilities.isEventDispatchThread()) {
            Log.exception(new WTFException("_fireTableStructureChanged inside EDT! "));
        }
        if (refreshSort) {
            this._replaceTableData(this.refreshSort(newtableData), true);
        } else {
            this._replaceTableData(newtableData, true);
        }
    }

    /**
     * this replaces the tables Data and Selection
     * 
     * checkEditing tells if we should check for table Editing mode
     * 
     * if it is false, we will replace the data for sure
     * 
     * if it is true, we check whether the table is in editing mode. if so we
     * add a propertychange listener and temporarily save the new data and
     * selection
     * 
     * @param newtableData
     * @param selection
     * @param checkEditing
     */
    protected void _replaceTableData(final List<E> newtableData, final boolean checkEditing) {
        if (newtableData == null) { return; }
        new EDTRunner() {

            @Override
            protected void runInEDT() {
                final ExtTable<E> ltable = ExtTableModel.this.getTable();
                final boolean replaceNow = !checkEditing || ltable == null || !ltable.isEditing();
                if (replaceNow) {
                    if (ltable != null) {
                        /* replace now */
                        /* clear delayed TableData and Selection */
                        ExtTableModel.this.delayedNewTableData = null;
                        ltable.removePropertyChangeListener(ExtTableModel.this.replaceDelayer);
                        /* replace TableData and set Selection */
                        final LinkedHashSet<E> selection = new LinkedHashSet<E>(ExtTableModel.this.getSelectedObjects());
                        final ListSelectionModel s = ltable.getSelectionModel();
                        final boolean adjusting = s.getValueIsAdjusting();
                        int leadIndex = s.getLeadSelectionIndex();
                        int anchorIndex = s.getAnchorSelectionIndex();
                        final E leadObject = leadIndex >= 0 ? ExtTableModel.this.getObjectbyRow(leadIndex) : null;
                        final E anchorObject = anchorIndex >= 0 ? ExtTableModel.this.getObjectbyRow(anchorIndex) : null;
                        if (ExtTableModel.this.isDebugTableModel()) {
                            System.out.println("before:leadIndex=" + leadIndex + "->" + leadObject + "|anchorIndex=" + anchorIndex + "->" + anchorObject);
                        }
                        ExtTableModel.this.setTableData(newtableData);
                        ExtTableModel.this.fireTableStructureChanged();
                        if (ExtTableModel.this.getRowCount() > 0) {
                            if (selection != null) {
                                /*
                                 * restore selection, first we remove all
                                 * vanished objects, then set the remaining ones
                                 */
                                selection.retainAll(newtableData);
                                ExtTableModel.this.setSelectedObjects(new ArrayList<E>(selection));

                                if (leadObject != null) {
                                    /* check if our leadObject does still exist */
                                    leadIndex = ExtTableModel.this.getRowforObject(leadObject);
                                } else {
                                    leadIndex = -1;
                                }
                                if (anchorObject != null) {
                                    /*
                                     * check if our anchorObject does still
                                     * exist
                                     */
                                    anchorIndex = ExtTableModel.this.getRowforObject(anchorObject);
                                } else {
                                    anchorIndex = -1;
                                }
                                if (ExtTableModel.this.isDebugTableModel()) {
                                    System.out.println("after:leadIndex=" + leadIndex + "->" + leadObject + "|anchorIndex=" + anchorIndex + "->" + anchorObject);
                                }
                                if (leadIndex >= 0 && anchorIndex >= 0) {
                                    /*
                                     * sort begin/end so we can loop through the
                                     * items
                                     */
                                    int begin = leadIndex;
                                    int end = anchorIndex;
                                    if (end < begin) {
                                        begin = anchorIndex;
                                        end = leadIndex;
                                    }
                                    /* check if we have holes in our Selection */
                                    boolean selectionHole = false;
                                    for (int index = begin; index <= end; index++) {
                                        if (s.isSelectedIndex(index) == false) {
                                            selectionHole = true;
                                            break;
                                        }
                                    }
                                    /*
                                     * only set adjusting if we have a
                                     * lead/anchor
                                     */
                                    s.setValueIsAdjusting(adjusting);
                                    if (selectionHole == false) {
                                        if (ExtTableModel.this.isDebugTableModel()) {
                                            System.out.println("No holes in selection: from " + begin + " to " + end);
                                        }
                                        s.setAnchorSelectionIndex(anchorIndex);
                                        s.setLeadSelectionIndex(leadIndex);
                                    } else {
                                        if (leadIndex > anchorIndex) {
                                            if (ExtTableModel.this.isDebugTableModel()) {
                                                System.out.println("Holes in selection from " + begin + " to " + end + "!Use leadIndex " + leadIndex);
                                            }
                                            s.setAnchorSelectionIndex(leadIndex);
                                            s.setLeadSelectionIndex(leadIndex);
                                        } else {
                                            if (ExtTableModel.this.isDebugTableModel()) {
                                                System.out.println("Holes in selection from " + begin + " to " + end + "!Use anchorIndex " + anchorIndex);
                                            }
                                            s.setAnchorSelectionIndex(anchorIndex);
                                            s.setLeadSelectionIndex(anchorIndex);
                                        }
                                    }

                                }
                            }
                        }
                    } else {
                        ExtTableModel.this.setTableData(newtableData);
                        ExtTableModel.this.fireTableStructureChanged();
                    }
                } else {
                    /* replace later because table is in editing mode */
                    /* set delayed TableData and Selection */
                    if (ltable != null) {
                        ExtTableModel.this.delayedNewTableData = newtableData;
                        ltable.removePropertyChangeListener(ExtTableModel.this.replaceDelayer);
                        ltable.addPropertyChangeListener(ExtTableModel.this.replaceDelayer);
                    }
                }
            }

        };
    }

    public void addAllElements(final Collection<E> entries) {

        final java.util.List<E> newdata = new ArrayList<E>(this.getTableData());
        for (final E n : entries) {
            newdata.add(n);
        }
        this._fireTableStructureChanged(newdata, true);
    }

    /**
     * @param files
     */
    public void addAllElements(final E... files) {
        if (files == null || files.length == 0) { return; }
        final java.util.List<E> newdata = new ArrayList<E>(this.getTableData());
        for (final E n : files) {
            newdata.add(n);
        }
        this._fireTableStructureChanged(newdata, true);
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
        final java.util.List<E> newdata = new ArrayList<E>(this.getTableData());
        newdata.add(at);
        this._fireTableStructureChanged(newdata, true);
    }

    public synchronized void addExtComponentRowHighlighter(final ExtComponentRowHighlighter<E> h) {
        this.extComponentRowHighlighters.add(h);
        Collections.sort(this.extComponentRowHighlighters, new Comparator<ExtComponentRowHighlighter<E>>() {

            @Override
            public int compare(final ExtComponentRowHighlighter<E> o1, final ExtComponentRowHighlighter<E> o2) {
                // TODO Auto-generated method stub
                return new Integer(o1.getPriority()).compareTo(new Integer(o2.getPriority()));
            }
        });
    }

    /**
     * 
     */
    public void clear() {
        ExtTableModel.this._replaceTableData(new ArrayList<E>(), true);
    }

    /**
     * clears all selection models
     */
    public void clearSelection() {
        final ExtTable<E> ltable = this.getTable();
        if (ltable == null) { return; }
        ltable.getSelectionModel().clearSelection();
        ltable.getColumnModel().getSelectionModel().clearSelection();
    }

    /**
     * @param at
     * @return
     */
    public boolean contains(final E at) {
        return this.getTableData().contains(at);
    }

    public int countSelectedObjects() {
        final ExtTable<E> ltable = this.getTable();
        if (ltable == null) { return 0; }
        return ltable.getSelectedRowCount();
    }

    /**
     * Returns the Celleditor for the given column
     * 
     * @param modelColumnIndex
     * @return
     */
    public TableCellEditor getCelleditorByColumn(final int modelColumnIndex) {
        /*
         * Math.max(0, columnIndex)
         * 
         * WORKAROUND for -1 column access,Index out of Bound,Unknown why it
         * happens but this workaround seems to do its job
         */
        return this.getExtColumnByModelIndex(modelColumnIndex);
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

    public java.util.List<ExtColumn<E>> getColumns() {
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
        final List<E> ltableData = this.getTableData();
        if (i >= 0 && i < ltableData.size()) { return ltableData.get(i); }
        return null;
    }

    /**
     * @return
     */
    public java.util.List<E> getElements() {
        return new ArrayList<E>(this.getTableData());
    }

    /**
     * Returns the Columninstance
     * 
     * @param columnIndex
     *            Model Index. NOT Real index
     * @return
     */
    public ExtColumn<E> getExtColumnByModelIndex(final int modelColumnIndex) {
        return this.columns.get(Math.max(0, modelColumnIndex));
    }

    /**
     * @param i
     * @return
     */
    public ExtColumn<E> getExtColumnByViewIndex(final int viewColumnIndex) {
        return this.getExtColumnByModelIndex(this.getTable().convertColumnIndexToModel(viewColumnIndex));
    }

    /**
     * @return a list of all ExtComponentRowHighlighters
     */
    public java.util.List<ExtComponentRowHighlighter<E>> getExtComponentRowHighlighters() {
        // TODO Auto-generated method stub
        return this.extComponentRowHighlighters;
    }

    /**
     * @return
     */
    public int getExtViewColumnCount() {
        // TODO Auto-generated method stub
        return this.getTable().getColumnCount();
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
        return this.getElementAt(index);
    }

    /**
     * Returns how many rows the model contains
     * 
     * @see #tableData
     */
    public int getRowCount() {
        return this.getTableData().size();
    }

    /**
     * Returns the row index for a given Object
     * 
     * @param o
     * @return
     */
    public int getRowforObject(final E o) {
        return this.getTableData().indexOf(o);
    }

    /**
     * Returns all selected Objects
     * 
     * @return
     */
    public List<E> getSelectedObjects() {
        return this.getSelectedObjects(-1);
    }

    public List<E> getSelectedObjects(final int maxItems) {
        final ExtTable<E> ltable = this.getTable();
        if (ltable == null) { return new ArrayList<E>(0); }
        final int[] rows = new EDTHelper<int[]>() {

            @Override
            public int[] edtRun() {
                return ltable.getSelectedRows();
            }
        }.getReturnValue();
        final List<E> ret = new ArrayList<E>(rows.length);
        final List<E> data = this.getTableData();
        for (final int row : rows) {
            if (maxItems >= 0 && ret.size() > maxItems) {
                break;
            }
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
        return JSonStorage.getPlainStorage("ExtTable_" + this.getModelID());
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
    public List<E> getTableData() {
        return this.tableData;
    }

    /**
     * returns a copy of current objects in tablemodel
     * 
     * @return
     */
    public List<E> getTableObjects() {
        return new ArrayList<E>(this.getTableData());
    }

    /**
     * Returns the object for row 'rowIndex'. IN ExtTableModel, each row is
     * represented By one single object. the ExtColums renderer just renders
     * each object in its way
     */
    public E getValueAt(final int rowIndex, final int columnIndex) {
        return this.getElementAt(rowIndex);
    }

    public boolean hasSelectedObjects() {
        final ExtTable<E> ltable = this.getTable();
        if (ltable == null) { return false; }
        return ltable.getSelectedRows().length > 0;
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
     * @return the debugTableModel
     */
    public boolean isDebugTableModel() {
        return this.debugTableModel;
    }

    /**
     * checks if this column is allowed to be hidden
     * 
     * @param column
     * @return
     */
    public boolean isHidable(final int column) {
        final ExtColumn<E> col = this.getExtColumnByModelIndex(column);
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
        final ExtColumn<E> col = this.getExtColumnByModelIndex(column);
        try {
            return col.isVisible(this.getTable().getColumnStore("VISABLE_COL_", col.getID(), col.isDefaultVisible()));
        } catch (final Exception e) {
            Log.exception(e);
            return true;
        }
    }

    /**
     * call this for drag&drop or cut&paste
     * 
     * @param transferData
     * @param dropRow
     * @return
     */
    public boolean move(final java.util.List<E> transferData, final int dropRow) {
        try {
            final java.util.List<E> newdata = new ArrayList<E>(this.getTableData().size());
            final List<E> before = new ArrayList<E>(this.getTableData().subList(0, dropRow));
            final List<E> after = new ArrayList<E>(this.getTableData().subList(dropRow, this.getTableData().size()));
            before.removeAll(transferData);
            after.removeAll(transferData);
            newdata.addAll(before);
            newdata.addAll(transferData);
            newdata.addAll(after);

            this._fireTableStructureChanged(newdata, true);
            return true;
        } catch (final Throwable t) {
            t.printStackTrace();
        }
        return false;

    }

    /**
     * Restores the sort order according to {@link #getSortColumn()} and
     * {@link #getSortOrderIdentifier() == ExtColumn.SORT_ASC}
     * 
     * 
     */
    public void refreshSort() {
        this._fireTableStructureChanged(this.getTableObjects(), true);
    }

    public List<E> refreshSort(final List<E> data) {
        if (this.isDebugTableModel() && SwingUtilities.isEventDispatchThread()) {
            Log.exception(new WTFException("refreshSort inside EDT! "));
        }
        try {
            boolean sameTable = false;
            if (data == this.tableData) {
                sameTable = true;
            }
            final List<E> ret = this.sort(data, this.sortColumn);
            if (this.isDebugTableModel() && this.tableData == ret && sameTable) {
                Log.exception(new WTFException("WARNING: sorting on live backend!"));
            }
            if (ret == null) { return data; }
            return ret;
        } catch (final NullPointerException e) {
            Log.exception(e);
            return data;
        }
    }

    /**
     * @param selectedObjects
     */
    public void removeAll(final java.util.List<E> selectedObjects) {
        final java.util.List<E> tmp = new ArrayList<E>(this.getTableData());
        tmp.removeAll(selectedObjects);
        this._fireTableStructureChanged(tmp, true);
    }

    /**
     * @param startRow
     * @param ret
     * @param caseSensitive
     * @param regex
     * @return
     */
    public E searchNextObject(final int startRow, final String ret, final boolean caseSensitive, final boolean regex) {

        final Pattern p;
        if (!regex) {
            final String[] pats = ret.split("\\*");
            final StringBuilder pattern = new StringBuilder();
            for (final String pp : pats) {
                if (pattern.length() > 0) {
                    pattern.append(".*?");
                }
                pattern.append(Pattern.quote(pp));
            }
            p = Pattern.compile(".*?" + pattern.toString() + ".*?", caseSensitive == false ? Pattern.CASE_INSENSITIVE : 0 | Pattern.DOTALL);
        } else {
            p = Pattern.compile(".*?" + ret + ".*?", caseSensitive == false ? Pattern.CASE_INSENSITIVE : 0 | Pattern.DOTALL);
        }
        final List<E> ltableData = this.getTableData();
        for (int i = startRow; i < ltableData.size(); i++) {
            for (int c = 0; c < this.columns.size(); c++) {
                if (this.columns.get(c).matchSearch(ltableData.get(i), p)) { return ltableData.get(i); }
            }

        }
        for (int i = 0; i < startRow; i++) {
            for (int c = 0; c < this.columns.size(); c++) {
                if (this.columns.get(c).matchSearch(ltableData.get(i), p)) { return ltableData.get(i); }
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
            this.getTable().getStorage().put(this.getTable().getColumnStoreKey("VISABLE_COL_", column.getID()), visible);
        } catch (final Exception e) {
            Log.exception(e);
        }
        this.getTable().updateColumns();

    }

    /**
     * Sets the column visible or invisible. This information is stored in
     * database interface for cross session use.
     * 
     * 
     * @param column
     * @param visible
     */
    public void setColumnVisible(final int modelColumnIndex, final boolean visible) {

        this.setColumnVisible(this.getExtColumnByModelIndex(modelColumnIndex), visible);

    }

    /**
     * @param debugTableModel
     *            the debugTableModel to set
     */
    public void setDebugTableModel(final boolean debugTableModel) {
        this.debugTableModel = debugTableModel;
    }

    /**
     * @param latest
     */
    public void setSelectedObject(final E latest) {
        final ExtTable<E> ltable = ExtTableModel.this.getTable();
        if (ltable == null) { return; }
        new EDTHelper<Object>() {
            @Override
            public Object edtRun() {
                ExtTableModel.this.clearSelection();
                if (latest == null) { return null; }
                final int row = ExtTableModel.this.getRowforObject(latest);
                if (row >= 0) {
                    ltable.addRowSelectionInterval(row, row);
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
    public void setSelectedObjects(final List<E> selections) {
        final ExtTable<E> ltable = ExtTableModel.this.getTable();
        if (ltable == null || selections == null || selections.size() == 0) { return; }
        new EDTHelper<Object>() {
            @Override
            public Object edtRun() {
                ExtTableModel.this.clearSelection();
                if (selections == null || selections.size() == 0) { return null; }
                // Transform to rowindex list
                final java.util.List<Integer> selectedRows = new ArrayList<Integer>();
                int rowIndex = -1;
                for (final E obj : selections) {
                    rowIndex = ExtTableModel.this.getRowforObject(obj);
                    if (rowIndex >= 0) {
                        selectedRows.add(rowIndex);
                    }
                }
                Collections.sort(selectedRows);
                for (final Integer row : selectedRows) {
                    ltable.addRowSelectionInterval(row, row);
                }
                return null;
            }
        }.start();
    }

    public void setSortColumn(final ExtColumn<E> e) {
        this.sortColumn = e;
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

    protected void setTableData(final List<E> data) {
        this.tableData = data;
    }

    // TODO docu
    @Override
    public void setValueAt(final Object value, final int rowIndex, final int columnIndex) {
        this.columns.get(columnIndex).setValueAt(value, rowIndex, columnIndex);
    }

    /**
     * Sorts given modeldata with the column's rowsorter
     * 
     **/
    public List<E> sort(final List<E> data, final ExtColumn<E> column) {
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
        } else {
            try {
                this.getStorage().put(ExtTableModel.SORT_ORDER_ID_KEY, (String) null);
                this.getStorage().put(ExtTableModel.SORTCOLUMN_KEY, (String) null);
            } catch (final Exception e) {
                Log.exception(e);
            }
        }
        return data;
    }


  
}
