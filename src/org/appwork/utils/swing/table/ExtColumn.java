package org.appwork.utils.swing.table;

import java.awt.Color;
import java.awt.Component;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.EventObject;
import java.util.regex.Pattern;

import javax.swing.AbstractCellEditor;
import javax.swing.JTable;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;

import org.appwork.utils.swing.EDTHelper;

/**
 * ExtColums define a single column of an extended Table. It contains all
 * columndetails including renderer
 * 
 * @author $Author: unknown$
 */
public abstract class ExtColumn<E> extends AbstractCellEditor implements TableCellEditor, TableCellRenderer {

    protected static Color         background         = null;
    protected static Color         backgroundselected = null;

    protected static Color         foreground         = null;
    protected static Color         foregroundselected = null;

    private static final long      serialVersionUID   = -2662459732650363059L;
    /**
     * If this colum is editable, this parameter says how many clicks are
     * required to start edit mode
     */
    private int                    clickcount         = 1;

    /**
     * The model this column belongs to
     */
    private ExtTableModel<E>       model;
    /**
     * The columns Title.
     */
    private final String           name;

    /**
     * A toggle to select the next sortingorder. ASC or DESC
     */
    private boolean                sortOrderToggle    = true;

    /**
     * Sorting algorithms run in an own thread
     */
    private Thread                 sortThread         = null;
    private TableCellRenderer      headerrenderer;
    private ExtDefaultRowSorter<E> rowSorter;

    /**
     * Create a new ExtColum.
     * 
     * @param name
     * @param table
     * @param database
     */
    public ExtColumn(final String name, final ExtTableModel<E> table) {
        this.name = name;
        this.model = table;
        try {
            this.headerrenderer = new ExtTableCellHeaderRenderer(this);
        } catch (final Throwable e) {
            e.printStackTrace();
            /* java 1.5 does not have DefaultTableCellHeaderRenderer */
            this.headerrenderer = null;
        }
        // sort function
        this.rowSorter = new ExtDefaultRowSorter<E>();
    }

    protected void doSort(final Object obj) {

        if (sortThread != null) { return; }

        sortThread = new Thread("TableSorter " + getID()) {
            @Override
            public void run() {
                // get selections before sorting
                final ArrayList<E> selections = model.getSelectedObjects();
                try {
                    // sort data
                    sortOrderToggle = !sortOrderToggle;
                    getModel().sort(ExtColumn.this, sortOrderToggle);

                } catch (final Exception e) {
                }
                // switch toggle

                sortThread = null;
                // Do this in EDT
                new EDTHelper<Object>() {

                    @Override
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

    public abstract Object getCellEditorValue();

    /**
     * @return the {@link ExtColumn#clickcount}
     * @see ExtColumn#clickcount
     */
    public int getClickcount() {
        return clickcount;
    }

    /**
     * @return
     */
    public int getDefaultWidth() {
        return 100;
    }

    /**
     * overwrite this to implement a custom renderer
     * 
     * @return
     */
    public TableCellRenderer getHeaderRenderer() {
        return headerrenderer;
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
     * @return
     */
    public int getMinWidth() {
        return 0;
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

    /**
     * Returns null or a sorting comperator for this column
     * 
     * @param sortToggle
     * @return
     */
    public ExtDefaultRowSorter<E> getRowSorter(final boolean sortOrderToggle) {
        rowSorter.setSortOrderToggle(sortOrderToggle);
        return this.rowSorter;
    }

    @SuppressWarnings("unchecked")
    public Component getTableCellEditorComponent(final JTable table, final Object value, final boolean isSelected, final int row, final int column) {
        return ((ExtTable<E>) table).getLafCellEditor(row, column).getTableCellEditorComponent(table, value, isSelected, row, column);
    }

    @SuppressWarnings("unchecked")
    public Component getTableCellRendererComponent(final JTable table, final Object value, final boolean isSelected, final boolean hasFocus, final int row, final int column) {
        return ((ExtTable<E>) table).getLafCellRenderer(row, column).getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
    }

    @Override
    public boolean isCellEditable(final EventObject evt) {
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
    public boolean isCellEditable(final int rowIndex, final int columnIndex) {
        final E obj = model.getValueAt(rowIndex, columnIndex);
        if (obj == null) { return false; }
        return isEditable(obj);
    }

    public boolean isDefaultVisible() {
        return true;
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
     * @return the {@link ExtColumn#sortOrderToggle}
     * @see ExtColumn#sortOrderToggle
     */
    protected boolean isSortOrderToggle() {
        return sortOrderToggle;
    }

    protected boolean matchSearch(final E object, final Pattern pattern) {
        return false;
    }

    /**
     * @param clickcount
     *            the {@link ExtColumn#clickcount} to set
     * @see ExtColumn#clickcount
     */
    public void setClickcount(final int clickcount) {
        this.clickcount = Math.max(0, clickcount);
    }

    /**
     * @param model
     *            the {@link ExtColumn#model} to set
     * @see ExtColumn#model
     */
    public void setModel(final ExtTableModel<E> model) {
        this.model = model;
    }

    /**
     * @param rowSorter
     *            the {@link ExtColumn#rowSorter} to set
     * @see ExtColumn#rowSorter
     */
    public void setRowSorter(final ExtDefaultRowSorter<E> rowSorter) {
        this.rowSorter = rowSorter;
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

    public void setValueAt(final Object value, final int rowIndex, final int columnIndex) {
        final E obj = model.getValueAt(rowIndex, columnIndex);
        if (obj == null) { return; }
        setValue(value, obj);
    }

    @Override
    public boolean shouldSelectCell(final EventObject anEvent) {
        return true;
    }

}
