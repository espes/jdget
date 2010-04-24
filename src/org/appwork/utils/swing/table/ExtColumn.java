package org.appwork.utils.swing.table;

import java.awt.Color;
import java.awt.Component;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.EventObject;

import javax.swing.AbstractCellEditor;
import javax.swing.JTable;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;

import org.appwork.utils.swing.EDTHelper;

/**
 * ExtColums define a single column of an extended Table. It contains all
 * columdetails including renderer
 * 
 * @author $Author: unknown$
 * 
 */
public abstract class ExtColumn<E> extends AbstractCellEditor implements TableCellEditor, TableCellRenderer {

    protected static Color background = null;
    protected static Color backgroundselected = null;

    protected static Color foreground = null;

    protected static Color foregroundselected = null;
    /**
     * 
     */
    private static final long serialVersionUID = -2662459732650363059L;
    /**
     * If this colum is editable, this parameter says how many clicks are
     * required to start edit mode
     */
    private int clickcount = 1;

    /**
     * The model this column belongs to
     */
    private ExtTableModel<E> model;
    /**
     * The columns Title.
     */
    private String name;

    /**
     * A toggle to select the next sortingorder. ASC or DESC
     */
    private boolean sortOrderToggle = true;

    /**
     * Sorting algorithms run in an own thread
     */
    private Thread sortThread = null;
    private TableCellRenderer headerrenderer;
    private ExtDefaultRowSorter<E> rowSorter;

    /**
     * Create a new ExtColum.
     * 
     * @param name
     * @param table
     * @param database
     */
    public ExtColumn(String name, ExtTableModel<E> table) {
        this.name = name;
        this.model = table;

        this.headerrenderer = new ExtTableCellHeaderRenderer(this);
        // sort function
        rowSorter = new ExtDefaultRowSorter<E>();

    }

    public boolean isDefaultVisible() {
        return true;
    }

    protected void doSort(final Object obj) {

        if (sortThread != null) return;

        sortThread = new Thread("TableSorter " + getID()) {
            public void run() {
                // get selections before sorting
                final ArrayList<E> selections = model.getSelectedObjects();
                try {
                    // sort data
                    sortOrderToggle = !sortOrderToggle;
                    getModel().sort(ExtColumn.this, sortOrderToggle);

                } catch (Exception e) {
                }
                // switch toggle

                sortThread = null;
                // Do this in EDT
                new EDTHelper<Object>() {

                    public Object edtRun() {
                        // inform model about structure change
                        model.fireTableStructureChanged();
                        // restore selection
                        model.setSelectedObjects(selections);

                        return null;
                    }
                }.start();
            }
        };
        sortThread.start();
    }

    /**
     * @return the {@link ExtColumn#sortOrderToggle}
     * @see ExtColumn#sortOrderToggle
     */
    protected boolean isSortOrderToggle() {
        return sortOrderToggle;
    }

    public abstract Object getCellEditorValue();

    /**
     * @return the {@link ExtColumn#clickcount}
     * @see ExtColumn#clickcount
     */
    public int getClickcount() {
        return clickcount;
    }

    /**
     * The storageID for this column
     * 
     * @return
     */
    public String getID() {
        return getClass().getSimpleName() + this.name;
    }

    /**
     * Should be overwritten when there should be a maximal width for this
     * column (e.g. for checkboxes)
     */
    protected int getMaxWidth() {
        return -1;
    }

    /**
     * @return the {@link ExtColumn#model}
     * @see ExtColumn#model
     */
    public ExtTableModel<E> getModel() {
        return model;
    }

    /**
     * @return the {@link ExtColumn#name}
     * @see ExtColumn#name
     */
    public String getName() {
        return name;
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * javax.swing.table.TableCellEditor#getTableCellEditorComponent(javax.swing
     * .JTable, java.lang.Object, boolean, int, int)
     */

    @SuppressWarnings("unchecked")
    public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
        // TODO Auto-generated method stub
        return ((ExtTable<E>) table).getLafCellEditor(row, column).getTableCellEditorComponent(table, value, isSelected, row, column);
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * javax.swing.table.TableCellRenderer#getTableCellRendererComponent(javax
     * .swing.JTable, java.lang.Object, boolean, boolean, int, int)
     */

    @SuppressWarnings("unchecked")
    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
        return ((ExtTable<E>) table).getLafCellRenderer(row, column).getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
    }

    public boolean isCellEditable(EventObject evt) {
        if (evt instanceof MouseEvent) { return ((MouseEvent) evt).getClickCount() >= clickcount && clickcount > 0; }
        return true;
    }

    /**
     * Returns if the cell is editable. Do NOT override this. Use
     * {@link #isEditable(Object)} instead
     * 
     * @param rowIndex
     * @param columnIndex
     * @return
     */
    public boolean isCellEditable(int rowIndex, int columnIndex) {
        E obj = model.getValueAt(rowIndex, columnIndex);
        if (obj == null) return false;
        return isEditable(obj);
    }

    /**
     * returns true if the column is editable for the object obj
     * 
     * @param obj
     * @return
     */
    public abstract boolean isEditable(E obj);

    /**
     * returns if the cell defined by this column and the object is enabled or
     * disabled
     * 
     * @param obj
     * @return
     */
    abstract public boolean isEnabled(E obj);

    /**
     * returns true if this column is sortable. if the call origin is an object,
     * the object is passed in obj parameter. if the caller origin is the column
     * header, obj is null
     * 
     * @param obj
     * @return
     */
    abstract public boolean isSortable(E obj);

    /**
     * @param clickcount
     *            the {@link ExtColumn#clickcount} to set
     * @see ExtColumn#clickcount
     */
    public void setClickcount(int clickcount) {
        this.clickcount = Math.max(0, clickcount);
    }

    /**
     * @param model
     *            the {@link ExtColumn#model} to set
     * @see ExtColumn#model
     */
    public void setModel(ExtTableModel<E> model) {
        this.model = model;
    }

    /**
     * USe this method to catch changed values.
     * 
     * @param value
     *            the new value
     * @param object
     *            the concerned object
     */
    public abstract void setValue(Object value, E object);

    public void setValueAt(Object value, int rowIndex, int columnIndex) {
        E obj = model.getValueAt(rowIndex, columnIndex);
        if (obj == null) return;
        setValue(value, obj);
    }

    public boolean shouldSelectCell(EventObject anEvent) {
        return true;
    }

    /**
     * overwrite this to implement a custom renderer
     * 
     * @return
     */
    public TableCellRenderer getHeaderRenderer() {
        // TODO Auto-generated method stub
        return headerrenderer;
    }

    /**
     * Returns null or a sorting comperator for this column
     * 
     * @param sortToggle
     * @return
     */
    public ExtDefaultRowSorter<E> getRowSorter(boolean sortOrderToggle) {
        rowSorter.setSortOrderToggle(sortOrderToggle);
        return this.rowSorter;
    }

    /**
     * @param rowSorter
     *            the {@link ExtColumn#rowSorter} to set
     * @see ExtColumn#rowSorter
     */
    public void setRowSorter(ExtDefaultRowSorter<E> rowSorter) {
        this.rowSorter = rowSorter;
    }

    /**
     * @return
     */
    public int getDefaultWidth() {
        // TODO Auto-generated method stub
        return 100;
    }

    /**
     * @return
     */
    public int getMinWidth() {
        // TODO Auto-generated method stub
        return 0;
    }

}
