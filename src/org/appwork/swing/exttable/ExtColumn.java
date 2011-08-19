package org.appwork.swing.exttable;

import java.awt.Color;
import java.awt.Component;
import java.awt.Rectangle;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.EventObject;
import java.util.regex.Pattern;

import javax.swing.AbstractCellEditor;
import javax.swing.BorderFactory;
import javax.swing.Icon;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JComponent;
import javax.swing.JPopupMenu;
import javax.swing.JSeparator;
import javax.swing.JTable;
import javax.swing.JToolTip;
import javax.swing.border.Border;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;

import org.appwork.swing.exttable.columnmenu.LockColumnWidthAction;
import org.appwork.utils.logging.Log;
import org.appwork.utils.swing.EDTHelper;
import org.appwork.utils.swing.EDTRunner;

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
    protected static final Border  DEFAULT_BORDER     = BorderFactory.createEmptyBorder(0, 5, 0, 5);
    /**
     * If this colum is editable, this parameter says how many clicks are
     * required to start edit mode
     */
    private int                    clickcount         = 2;

    /**
     * The model this column belongs to
     */
    private ExtTableModel<E>       model;
    /**
     * The columns Title.
     */
    private final String           name;

    /**
     * Sorting algorithms run in an own thread
     */
    private static Thread          sortThread         = null;
    private static Object          sortLOCK           = new Object();

    private ExtDefaultRowSorter<E> rowSorter;
    private String                 id;
    private TableColumn            tableColumn;
    protected ExtToolTip           tip;

    private String                 sortOrderIdentifier;

    public static final String     SORT_DESC          = "DESC";
    public static final String     SORT_ASC           = "ASC";

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
        this.sortOrderIdentifier = null;
        this.tip = new ExtToolTip();
        if (this.model != null) {
            this.id = this.getClass().getSuperclass().getSimpleName() + "." + this.getClass().getName() + "." + (this.model.getColumnCount() + 1);
        }
        // sort function
        this.rowSorter = new ExtDefaultRowSorter<E>();

    }

    /**
     * @param value
     * @param isSelected
     * @param hasFocus
     * @param row
     */
    protected void adaptHighlighters(final E value, final JComponent comp, final boolean isSelected, final boolean hasFocus, final int row) {

        try {
            for (final ExtComponentRowHighlighter<E> rh : this.getModel().getExtComponentRowHighlighters()) {
                if (rh.highlight(this, comp, value, isSelected, hasFocus, row)) {
                    break;
                }
            }
        } catch (final Throwable e) {
            Log.exception(e);
        }
    }

    abstract public void configureEditorComponent(final E value, final boolean isSelected, final int row, final int column);

    public void configureEditorHighlighters(final JComponent component, final E value, final boolean isSelected, final int row) {
        this.adaptHighlighters(value, component, isSelected, true, row);

    }

    abstract public void configureRendererComponent(final E value, final boolean isSelected, final boolean hasFocus, final int row, final int column);

    public void configureRendererHighlighters(final JComponent component, final E value, final boolean isSelected, final boolean hasFocus, final int row) {
        this.adaptHighlighters(value, component, isSelected, hasFocus, row);

    }

    /**
     * @return
     */
    public JPopupMenu createHeaderPopup() {
        if (this.getMinWidth() == this.getMaxWidth() && this.getMaxWidth() > 0) {
            // resize is not possible anyway
            return null;
        } else {
            final JPopupMenu ret = new JPopupMenu();
            LockColumnWidthAction action;
            ret.add(new JCheckBoxMenuItem(action = new LockColumnWidthAction(this)));
            ret.add(new JSeparator());
            return ret;
        }

    }

    public JToolTip createToolTip(final E obj) {
        final String txt = this.getTooltipText(obj);
        if (txt == null || txt.length() == 0) { return null; }

        this.tip.setExtText(txt);
        return this.tip;
    }

    public void doSort() {
        final String newID = ExtColumn.this.getNextSortIdentifier();

        System.out.println("Sort: " + newID);
        synchronized (ExtColumn.sortLOCK) {
            if (ExtColumn.sortThread != null && ExtColumn.sortThread.isAlive()) { return; }

            ExtColumn.sortThread = new Thread("TableSorter " + this.getID()) {
                @Override
                public void run() {
                    try {
                        // get selections before sorting
                        ArrayList<E> data = ExtColumn.this.model.getElements();
                        try {
                            // sort data

                            ExtColumn.this.setSortOrderIdentifier(newID);
                            data = ExtColumn.this.getModel().sort(data, ExtColumn.this);
                        } catch (final Exception e) {
                        }
                        final ArrayList<E> newData = data;
                        // switch toggle

                        // Do this in EDT
                        new EDTHelper<Object>() {

                            @Override
                            public Object edtRun() {
                                final ArrayList<E> selections = ExtColumn.this.model.getSelectedObjects();
                                ExtColumn.this.model.tableData = newData;
                                // inform model about structure change
                                ExtColumn.this.model.fireTableStructureChanged();
                                // restore selection
                                ExtColumn.this.model.setSelectedObjects(selections);
                                ExtColumn.this.getModel().getTable().getTableHeader().repaint();
                                return null;
                            }
                        }.waitForEDT();
                    } finally {
                        synchronized (ExtColumn.sortLOCK) {
                            ExtColumn.sortThread = null;
                        }
                    }
                }
            };
            ExtColumn.sortThread.start();
        }
    }

    /**
     * @param popup
     */
    public void extendControlButtonMenu(final JPopupMenu popup) {
        // TODO Auto-generated method stub

    }

    /**
     * @return the column bounds
     */
    public Rectangle getBounds() {
        final Rectangle first = this.getModel().getTable().getCellRect(0, this.getIndex(), true);
        final Rectangle last = this.getModel().getTable().getCellRect(this.getModel().size() - 1, this.getIndex(), true);
        first.height = last.y + last.height - first.y;
        return first;
    }

    public abstract Object getCellEditorValue();

    /**
     * @return the {@link ExtColumn#clickcount}
     * @see ExtColumn#clickcount
     */
    public int getClickcount() {
        return this.clickcount;
    }

    /**
     * @return
     */
    public int getDefaultWidth() {
        return 100;
    }

    /**
     * @param value
     *            TODO
     * @param isSelected
     *            TODO
     * @param row
     *            TODO
     * @param column
     *            TODO
     * @return
     */
    abstract public JComponent getEditorComponent(E value, boolean isSelected, int row, int column);

    /**
     * @param jTableHeader
     * @return
     */
    public ExtTableHeaderRenderer getHeaderRenderer(final JTableHeader jTableHeader) {
        // TODO Auto-generated method stub
        return null;
    }

    /**
     * The storageID for this column. Override this if you have a selfdefined
     * column class which is used by several of your columns.
     * 
     * @return
     */
    public String getID() {

        return this.id;

    }

    /**
     * returns the real visible columnindex
     * 
     * @return
     */
    public int getIndex() {
        return this.getModel().getTable().convertColumnIndexToView(this.tableColumn.getModelIndex());

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
        return 10;
    }

    /**
     * @return the {@link ExtColumn#model}
     * @see ExtColumn#model
     */
    public ExtTableModel<E> getModel() {
        return this.model;
    }

    /**
     * @return the {@link ExtColumn#name}
     * @see ExtColumn#name
     */
    public String getName() {
        return this.name;
    }

    /**
     * @return
     */
    protected String getNextSortIdentifier() {
        return this.getModel().getNextSortIdentifier(this.getSortOrderIdentifier());

    }

    /**
     * @param value
     *            TODO
     * @param isSelected
     *            TODO
     * @param hasFocus
     *            TODO
     * @param row
     *            TODO
     * @param column
     *            TODO
     * @return
     */
    abstract public JComponent getRendererComponent(E value, boolean isSelected, boolean hasFocus, int row, int column);

    /**
     * Returns null or a sorting comperator for this column
     * 
     * @param sortToggle
     * @return
     */
    public ExtDefaultRowSorter<E> getRowSorter() {
        this.rowSorter.setSortOrderIdentifier(this.getSortOrderIdentifier());
        return this.rowSorter;
    }

    /**
     * @return
     */
    public Icon getSortIcon() {
        return this.getModel().getSortIcon(this.getSortOrderIdentifier());

    }

    public String getSortOrderIdentifier() {
        return this.sortOrderIdentifier;
    }

    @SuppressWarnings("unchecked")
    @Override
    final public Component getTableCellEditorComponent(final JTable table, final Object value, final boolean isSelected, final int row, final int column) {
        final JComponent ret = this.getEditorComponent((E) value, isSelected, row, column);

        this.resetEditor();
        this.configureEditorHighlighters(ret, (E) value, isSelected, row);
        this.configureEditorComponent((E) value, isSelected, row, column);
        ret.setEnabled(this.isEnabled((E) value));

        return ret;
    }

    @SuppressWarnings("unchecked")
    @Override
    final public Component getTableCellRendererComponent(final JTable table, final Object value, final boolean isSelected, final boolean hasFocus, final int row, final int column) {
        final JComponent ret = this.getRendererComponent((E) value, isSelected, hasFocus, row, column);
        this.resetRenderer();
        this.configureRendererHighlighters(ret, (E) value, isSelected, hasFocus, row);
        this.configureRendererComponent((E) value, isSelected, hasFocus, row, column);
        ret.setEnabled(this.isEnabled((E) value));

        return ret;
    }

    /**
     * Returns the intern TableColumn
     * 
     * @return
     */
    public TableColumn getTableColumn() {
        return this.tableColumn;
    }

    /**
     * @param obj
     * @return
     */
    protected String getTooltipText(final E value) {
        // TODO Auto-generated method stub
        return null;
    }

    public int getWidth() {
        return this.tableColumn.getWidth();
    }

    @Override
    public boolean isCellEditable(final EventObject evt) {
        if (evt instanceof MouseEvent) { return ((MouseEvent) evt).getClickCount() >= this.getClickcount() && this.getClickcount() > 0; }
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
        final E obj = this.model.getValueAt(rowIndex, columnIndex);
        if (obj == null) { return false; }

        return this.isEditable(obj, this.isEnabled(obj));
    }

    /**
     * @return
     */
    protected boolean isDefaultResizable() {

        return true;
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
     * override this to enable cell editing if the cell is disabled
     * 
     * @param obj
     * @param enabled
     * @return if the row with obj is editable
     */
    protected boolean isEditable(final E obj, final boolean enabled) {

        return enabled && this.isEditable(obj);
    }

    /**
     * returns if the cell defined by this column and the object is enabled or
     * disabled
     * 
     * @param obj
     * @return
     */
    abstract public boolean isEnabled(E obj);

    /**
     * returns if this column is allowed to be hidden
     * 
     * @return
     */
    public boolean isHidable() {
        return true;
    }

    /**
     * If you want to use only an icon in the table header, you can override
     * this and let the method return false. This only works if
     * {@link #getHeaderIcon()} returns an icon
     * 
     * @return
     */
    public boolean isPaintHeaderText() {

        return true;
    }

    public boolean isResizable() {
        return !this.getModel().getStorage().get("ColumnWidthLocked_" + this.getID(), !this.isDefaultResizable());
    }

    /**
     * returns true if this column is sortable. if the call origin is an object,
     * the object is passed in obj parameter. if the caller origin is the column
     * header, obj is null
     * 
     * @param obj
     * @return
     */
    abstract public boolean isSortable(E obj);

    public boolean matchSearch(final E object, final Pattern pattern) {
        return false;
    }

    /**
     * 
     */
    public abstract void resetEditor();

    /**
     * 
     */
    public abstract void resetRenderer();

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
        this.id = this.getClass().getSuperclass().getSimpleName() + "." + this.getClass().getName() + "." + model.getColumnCount();
        System.out.println(this.id);
    }

    public void setResizable(final boolean resizeAllowed) {

        this.getModel().getStorage().put("ColumnWidthLocked_" + this.getID(), !resizeAllowed);
        this.updateColumnGui();
        // if
        // (!this.getModel().getExtColumnByViewIndex(this.getModel().getExtViewColumnCount()
        // - 1).isResizable()) {
        // this.getModel().getTable().setAutoResizeFallbackEnabled(true);
        //
        // } else {
        // this.getModel().getTable().setAutoResizeFallbackEnabled(false);
        //
        // }
        new EDTRunner() {

            @Override
            protected void runInEDT() {
                ExtColumn.this.getModel().getTable().getTableHeader().repaint();
                ExtColumn.this.getModel().getTable().revalidate();
            }
        };
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
     * @param string
     */
    public void setSortOrderIdentifier(final String id) {
        this.sortOrderIdentifier = id;
    }

    /**
     * Sets the real tableColumn.
     * 
     * @param tableColumn
     */
    public void setTableColumn(final TableColumn tableColumn) {
        this.tableColumn = tableColumn;
        // Set stored columnwidth
        int w = ExtColumn.this.getDefaultWidth();
        try {
            w = ExtColumn.this.getModel().getStorage().get("WIDTH_COL_" + ExtColumn.this.getID(), w);
        } catch (final Exception e) {
            Log.exception(e);
        } finally {
            ExtColumn.this.tableColumn.setPreferredWidth(w);
            tableColumn.setWidth(w);
        }
        this.updateColumnGui();

    }

    public void setTip(final ExtToolTip tip) {
        this.tip = tip;
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
        final E obj = this.model.getValueAt(rowIndex, columnIndex);
        if (obj == null) { return; }
        this.setValue(value, obj);
    }

    @Override
    public boolean shouldSelectCell(final EventObject anEvent) {
        return true;
    }

    /**
     * 
     */
    private void updateColumnGui() {

        new EDTRunner() {

            @Override
            protected void runInEDT() {

                if (ExtColumn.this.isResizable()) {
                    ExtColumn.this.getModel().getTable().saveWidthsRatio();

                    ExtColumn.this.tableColumn.setMaxWidth(ExtColumn.this.getMaxWidth() < 0 ? Integer.MAX_VALUE : ExtColumn.this.getMaxWidth());
                    ExtColumn.this.tableColumn.setMinWidth(ExtColumn.this.getMinWidth() < 0 ? 15 : ExtColumn.this.getMinWidth());
                    ExtColumn.this.tableColumn.setResizable(true);

                } else {
                    ExtColumn.this.tableColumn.setResizable(false);
                    ExtColumn.this.tableColumn.setMaxWidth(ExtColumn.this.tableColumn.getWidth());
                    ExtColumn.this.tableColumn.setMinWidth(ExtColumn.this.tableColumn.getWidth());
                }

            }
        };

    }

}
