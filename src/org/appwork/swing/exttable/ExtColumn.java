package org.appwork.swing.exttable;

import java.awt.Color;
import java.awt.Component;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.MouseEvent;
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
import javax.swing.JViewport;
import javax.swing.border.Border;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;

import org.appwork.swing.components.tooltips.ExtTooltip;
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

    protected static Color           background             = null;
    protected static Color           backgroundselected     = null;

    protected static Color           foreground             = null;
    protected static Color           foregroundselected     = null;

    private static final long        serialVersionUID       = -2662459732650363059L;
    protected static final Border    DEFAULT_BORDER         = BorderFactory.createEmptyBorder(0, 5, 0, 5);
    /**
     * If this colum is editable, this parameter says how many clicks are
     * required to start edit mode
     */
    private int                      clickcount             = 2;

    /**
     * The model this column belongs to
     */
    private ExtTableModel<E>         model;
    /**
     * The columns Title.
     */
    private final String             name;

    /**
     * Sorting algorithms run in an own thread
     */
    private static Thread            sortThread             = null;
    private static Object            sortLOCK               = new Object();

    protected ExtDefaultRowSorter<E> rowSorter;
    private String                   id;
    private TableColumn              tableColumn;

    private String                   sortOrderIdentifier;
    private final ToolTip            tooltip;
    private boolean                  editableProgrammaticly = false;

    public static final String       SORT_DESC              = "DESC";
    public static final String       SORT_ASC               = "ASC";
    protected volatile boolean       modifying              = false;

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

        this.generateID();
        // sort function
        this.rowSorter = new ExtDefaultRowSorter<E>();
        this.tooltip = new ToolTip();
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

    /**
     * @param value
     * @param isSelected
     * @param row
     * @param column
     */
    protected void configureCurrentlyEditingComponent(final E value, final boolean isSelected, final int row, final int column) {
        // TODO Auto-generated method stub

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

    public ExtTooltip createToolTip(final Point position, final E obj) {
        final String txt = this.getTooltipText(obj);
        if (txt == null || txt.length() == 0) { return null; }

        this.tooltip.setTipText(txt);
        return this.tooltip;
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
                        final Integer sel = new EDTHelper<Integer>() {

                            @Override
                            public Integer edtRun() {
                                final JViewport viewport = (JViewport) getModel().getTable().getParent();
                                if (viewport == null) { return 0; }
                                final Rectangle rec = viewport.getViewRect();
                                return getModel().getTable().rowAtPoint(new Point(0, (int) (rec.getY() + 15)));

                            }
                        }.getReturnValue();
                        
                        final Rectangle view = new EDTHelper<Rectangle>() {

                            @Override
                            public Rectangle edtRun() {
                                final JViewport viewport = (JViewport) getModel().getTable().getParent();
                                if (viewport == null) { return null; }
                                final Rectangle rec = viewport.getViewRect();
                                return rec;

                            }
                        }.getReturnValue(); 
                     
                        final E selObject = getModel().getObjectbyRow(sel.intValue());
                        final java.util.List<E> data = ExtColumn.this.model.getElements();
                        try {
                            // sort data
                            ExtColumn.this.setSortOrderIdentifier(newID);
                            ExtColumn.this.getModel().setSortColumn(ExtColumn.this);
                        } catch (final Exception e) {
                        }
                        ExtColumn.this.getModel()._fireTableStructureChanged(data, true);

                        new EDTHelper<Object>() {

                            @Override
                            public Object edtRun() {
                                ExtColumn.this.getModel().getTable().getTableHeader().repaint();

                                if (getModel().getTable().getSelectedRowCount() > 0) {
                                    getModel().getTable().scrollToSelection(view.x);
                                } else {
                                    // scroll to 0,
                                    // getModel().getRowforObject(selObject)
                                    getModel().getTable().scrollToRow(0,view.x);

                                }
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

    private void generateID() {
        this.id = this.getClass().getSuperclass().getSimpleName() + "." + this.getClass().getName();
    }

    /**
     * @return the column bounds
     */
    public Rectangle getBounds() {
        final Rectangle first = this.getModel().getTable().getCellRect(0, this.getIndex(), true);
        final Rectangle last = this.getModel().getTable().getCellRect(this.getModel().getRowCount() - 1, this.getIndex(), true);
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

    /**
     * @param table
     * @param value
     * @param isSelected
     * @param row
     * @param column
     * @param editing
     *            set editing to true, if the cellrenderer should not update the
     *            editing value. for example a text in a textfiled. in this case
     *            the renderer only updates the rest. colors, borders...
     * @return
     */
    public Component getTableCellEditorComponent(final JTable table, final E value, final boolean isSelected, final int row, final int column, final boolean editing) {
        try {
            this.modifying = true;
            final JComponent ret = this.getEditorComponent(value, isSelected, row, column);
            this.resetEditor();
            this.configureEditorHighlighters(ret, value, isSelected, row);
            if (editing) {
                // while editing, we call a different method, which can be used
                // to
                // update colors, borders, layouts, but not the editing value
                // itself.
                this.configureCurrentlyEditingComponent(value, isSelected, row, column);
            } else {
                this.configureEditorComponent(value, isSelected, row, column);
            }
            ret.setEnabled(this.getModel().getTable().isEnabled() && this.isEnabled(value));
            return ret;
        } finally {
            this.modifying = false;
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    final public Component getTableCellEditorComponent(final JTable table, final Object value, final boolean isSelected, final int row, final int column) {
        return this.getTableCellEditorComponent(table, (E) value, isSelected, row, column, false);

    }

    @SuppressWarnings("unchecked")
    @Override
    final public Component getTableCellRendererComponent(final JTable table, final Object value, final boolean isSelected, final boolean hasFocus, final int row, final int column) {
        try {
            this.modifying = true;
            final JComponent ret = this.getRendererComponent((E) value, isSelected, hasFocus, row, column);
            this.resetRenderer();
            this.configureRendererHighlighters(ret, (E) value, isSelected, hasFocus, row);
            this.configureRendererComponent((E) value, isSelected, hasFocus, row, column);
            ret.setEnabled(this.getModel().getTable().isEnabled() && this.isEnabled((E) value));
            return ret;
        } finally {
            this.modifying = false;
        }
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
        if (this.editableProgrammaticly) { return true; }
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

    public boolean isModifying() {
        return this.modifying;
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

    /**
     * @return if the column header paints the locked icon
     */
    public boolean isPaintWidthLockIcon() {
        return true;
    }

    public boolean isResizable() {
        return !this.getModel().getTable().getColumnStore("ColumnWidthLocked_" , this.getID(), !this.isDefaultResizable());
    }

    /**
     * @see #shouldSelectCell(EventObject)
     * @param anEvent
     * @return
     */
    public boolean isSelectRowWhenEditing(final EventObject anEvent) {
        return true;
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

    /**
     * @param boolean1
     * @return true if the column is visible
     */
    public boolean isVisible(final boolean savedValue) {
        return savedValue;
    }

    public boolean matchSearch(final E object, final Pattern pattern) {
        return false;
    }

    /**
     * return true if you dont want to forward to following onDoubleClick
     * listener
     * 
     * This method will be called when a doubleclick is performed on the object
     * <code>obj</code>
     * 
     * @param obj
     */
    public boolean onDoubleClick(final MouseEvent e, final E obj) {
        return false;
    }

    /**
     * This method will be called if the user does a windows typic rename click
     * order. Means: click on a already selected single row
     * 
     * @param e
     * @param obj
     * @return
     */
    public boolean onRenameClick(final MouseEvent e, final E obj) {
        return false;
    }

    /**
     * return true if you dont want to forward to following onSingleClick
     * listener
     * 
     * @param e
     * @param obj
     */ 
    public boolean onSingleClick(final MouseEvent e, final E obj) {
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
        this.generateID();
    }

    public void setResizable(final boolean resizeAllowed) {

        this.getModel().getStorage().put(getModel().getTable().getColumnStoreKey("ColumnWidthLocked_" ,this.getID()), !resizeAllowed);
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
    public void setTableColumn(final TableColumn tableColumn, final boolean updateSize) {
        this.tableColumn = tableColumn;
        if (updateSize) {
            // Set stored columnwidth
            int w = ExtColumn.this.getDefaultWidth();
            try {
                w = ExtColumn.this.getModel().getTable().getColumnStore("WIDTH_COL_" , ExtColumn.this.getID(), w);
            } catch (final Exception e) {
                Log.exception(e);
            } finally {
                ExtColumn.this.tableColumn.setPreferredWidth(w);
                tableColumn.setWidth(w);
            }
            this.updateColumnGui();
        }

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

    /**
     * By default. if we start an editor, the row will be selected. If you want
     * the framework not to select the current editing row, overwrite this
     * method to return false
     */

    @Override
    public boolean shouldSelectCell(final EventObject anEvent) {
        return this.isSelectRowWhenEditing(anEvent);
    }

    public void startEditing(final E obj) {
        new EDTRunner() {

            @Override
            protected void runInEDT() {
                ExtColumn.this.editableProgrammaticly = true;
                try {
                    ExtColumn.this.getModel().getTable().editCellAt(ExtColumn.this.getModel().getTable().getModel().getRowforObject(obj), ExtColumn.this.getIndex());
            
            
                } finally {
                    ExtColumn.this.editableProgrammaticly = false;
                }
            }
        };

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
