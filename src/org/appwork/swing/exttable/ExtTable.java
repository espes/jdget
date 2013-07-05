package org.appwork.swing.exttable;

import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.MouseInfo;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.EventObject;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

import javax.swing.CellEditor;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JTable;
import javax.swing.JViewport;
import javax.swing.KeyStroke;
import javax.swing.ScrollPaneConstants;
import javax.swing.SwingUtilities;
import javax.swing.ToolTipManager;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.TableColumnModelEvent;
import javax.swing.event.TableColumnModelListener;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;

import org.appwork.resources.AWUTheme;
import org.appwork.scheduler.DelayedRunnable;
import org.appwork.storage.Storage;
import org.appwork.swing.MigPanel;
import org.appwork.swing.components.tooltips.ExtTooltip;
import org.appwork.swing.components.tooltips.ToolTipController;
import org.appwork.swing.components.tooltips.ToolTipHandler;
import org.appwork.swing.exttable.columnmenu.ResetColumns;
import org.appwork.swing.exttable.columnmenu.SearchContextAction;
import org.appwork.utils.Application;
import org.appwork.utils.BinaryLogic;
import org.appwork.utils.logging.Log;
import org.appwork.utils.swing.EDTHelper;
import org.appwork.utils.swing.EDTRunner;

import sun.swing.SwingUtilities2;

/**
 * The ExtTable package is a tableframework that follows two main tasks:<br>
 * 1. Easy creating of tables<br>
 * 2. Implement extended features like column selection, database connection,
 * editing, easy rendering, sorting etc.
 * 
 * @author $Author: unknown$
 */
public class ExtTable<E> extends JTable implements ToolTipHandler, PropertyChangeListener {

    /**
     * 
     */
    private static final String                            DEFAULT_COLUMN_STORE = "";
    private static final long                              serialVersionUID     = 2822230056021924679L;
    // executer for renameclicks
    private static final ScheduledExecutorService          EXECUTER             = Executors.newSingleThreadScheduledExecutor();
    /**
     * Column background color if column is NOT selected
     */
    private final Color                                    columnBackground;
    /**
     * Column background color if column is selected
     */
    private final Color                                    columnBackgroundSelected;
    /**
     * Column textcolor if column is NOT selected
     */
    private final Color                                    columnForeground;
    /**
     * Column textcolor if column is selected
     */
    private final Color                                    columnForegroundSelected;

    /**
     * The underlaying datamodel
     */
    private final ExtTableModel<E>                         model;

    final private java.util.List<ExtOverlayRowHighlighter> rowHighlighters;
    /**
     * true if search is enabled
     */

    private boolean                                        searchEnabled        = false;
    private SearchDialog                                   searchDialog;
    private final ExtTableEventSender                      eventSender;
    private JComponent                                     columnButton         = null;
    private boolean                                        columnButtonVisible  = true;
    private int                                            verticalScrollPolicy;

    protected boolean                                      headerDragging;
    private ExtColumn<E>                                   lastTooltipCol;
    private int                                            lastTooltipRow;
    private ExtDataFlavor<E>                               flavor;
    private DelayedRunnable                                renameClickDelayer;
    private Runnable                                       clickDelayerRunable;
    private String                                         columnSaveID         = DEFAULT_COLUMN_STORE;

    /**
     * Create an Extended Table instance
     * 
     * @param model
     *            Databsemodel
     * @param database
     *            Information storage interface.
     * @param id
     *            Tableid used for storage
     */
    public ExtTable(final ExtTableModel<E> model) {
        super(model);
        if (model == null) { throw new NullPointerException("Model must not be null"); }
        this.flavor = new ExtDataFlavor<E>(this.getClass());
        this.eventSender = new ExtTableEventSender();
        ToolTipController.getInstance().register(this);
        ToolTipManager.sharedInstance().unregisterComponent(this);
        this.rowHighlighters = new ArrayList<ExtOverlayRowHighlighter>();
        this.model = model;
        // workaround
        setColumnModel(new ExtColumnModel(getColumnModel()));
        model.setTable(this);
        // final int suggestedRowHeight =
        // UIManager.getInt(ExtTable.SUGGESTEDROWHEIGHTPROPERTY);
        // if (suggestedRowHeight > 0) {
        // this.setRowHeight(suggestedRowHeight);

        this.setRowHeight(22);
        this.renameClickDelayer = new DelayedRunnable(ExtTable.EXECUTER, this.setupRenameClickInterval()) {
            @Override
            public void delayedrun() {

                if (ExtTable.this.clickDelayerRunable != null) {
                    if (ExtTable.this.getDropLocation() == null) {
                        ExtTable.this.clickDelayerRunable.run();
                    }
                    ExtTable.this.clickDelayerRunable = null;
                }
            }
        };
        setTableHeader(new JTableHeader(getColumnModel()) {

            /**
             * 
             */
            private static final long serialVersionUID = 6099615257824836337L;

            public Dimension getPreferredSize() {
                final Dimension ret = super.getPreferredSize();
                ret.height = 19;
                return ret;
            }
            // @Override
            // public void setPreferredSize(final Dimension preferredSize) {
            // int suggestedRowHeight =
            // UIManager.getInt(ExtTable.SUGGESTEDROWHEIGHTPROPERTY);
            // if (suggestedRowHeight > 0 && suggestedRowHeight >
            // preferredSize.getHeight()) {
            // if (suggestedRowHeight > 0) {
            // suggestedRowHeight += 8;
            // }
            // Log.L.info("Using SuggestedRowHeight of " + suggestedRowHeight +
            // " instead of setPreferredSize of " + ExtTable.this.rowHeight);
            // preferredSize.height = suggestedRowHeight;
            // }
            // this.getPreferredSize()
            // super.setPreferredSize(preferredSize);
            // }

        });
        this.createColumns();
        // get defaultbackground and Foregroundcolors
        Component c = super.getCellRenderer(0, 0).getTableCellRendererComponent(this, "", true, false, 0, 0);
        this.columnBackgroundSelected = c.getBackground();
        this.columnForegroundSelected = c.getForeground();

        c = super.getCellRenderer(0, 0).getTableCellRendererComponent(this, "", false, false, 0, 0);
        this.columnBackground = c.getBackground();
        this.columnForeground = c.getForeground();
        this.addPropertyChangeListener("dropLocation", this);
        // Mouselistener for columnselection Menu and sort on click
        getTableHeader().addMouseListener(new MouseAdapter() {

            @Override
            public void mouseClicked(final MouseEvent e) {
                if (e.getButton() == MouseEvent.BUTTON1) {

                    final int col = ExtTable.this.getExtColumnModelIndexByPoint(e.getPoint());
                    if (col == -1) { return; }

                    if (ExtTable.this.getModel().getExtColumnByModelIndex(col).isSortable(null)) {
                        final ExtColumn<E> oldColumn = ExtTable.this.getModel().getSortColumn();
                        final String oldIdentifier = oldColumn == null ? null : oldColumn.getSortOrderIdentifier();

                        if (!ExtTable.this.onHeaderSortClick(e, oldColumn, oldIdentifier, ExtTable.this.getModel().getExtColumnByModelIndex(col))) {
                            ExtTable.this.getModel().getExtColumnByModelIndex(col).doSort();
                        }
                        ExtTable.this.eventSender.fireEvent(new ExtTableEvent<MouseEvent>(ExtTable.this, ExtTableEvent.Types.SORT_HEADER_CLICK, e));

                    }

                }
            }

            @Override
            public void mousePressed(final MouseEvent e) {
                ExtTable.this.headerDragging = true;
                // only if we are not in resize mode
                if (ExtTable.this.getTableHeader().getCursor().getType() == Cursor.getDefaultCursor().getType()) {
                    if (e.getButton() == MouseEvent.BUTTON3) {
                        final JPopupMenu ccm = ExtTable.this.columnControlMenu(ExtTable.this.getExtColumnAtPoint(e.getPoint()));
                        ccm.show(ExtTable.this.getTableHeader(), e.getX(), e.getY());

                        if (ccm.getComponentCount() == 0) {
                            Toolkit.getDefaultToolkit().beep();
                        }
                    }

                }
            }

            @Override
            public void mouseReleased(final MouseEvent e) {
                ExtTable.this.headerDragging = false;
                try {

                    // this is a workaround. we dis
                    if (ExtTable.this.getTableHeader().getCursor().getType() == Cursor.getDefaultCursor().getType()) {
                        for (final MouseListener ms : ExtTable.this.getTableHeader().getMouseListeners()) {
                            if (ms instanceof javax.swing.plaf.basic.BasicTableHeaderUI.MouseInputHandler) {
                                // ((javax.swing.plaf.basic.BasicTableHeaderUI.MouseInputHandler)ms).
                                Field field;

                                field = javax.swing.plaf.basic.BasicTableHeaderUI.MouseInputHandler.class.getDeclaredField("otherCursor");
                                field.setAccessible(true);
                                field.set(ms, Cursor.getPredefinedCursor(Cursor.E_RESIZE_CURSOR));

                            }
                        }
                    }
                } catch (final Throwable e1) {
                    // TODO Auto-generated catch block
                    e1.printStackTrace();
                }
                // BasicTableHeaderUI.class.getField(name)
                // ((BasicTableHeaderUI) getTableHeader())
            }

        });
        // mouselistener to display column header tooltips
        getTableHeader().addMouseMotionListener(new MouseAdapter() {

            @Override
            public void mouseMoved(final MouseEvent e) {
                final int col = ExtTable.this.getExtColumnModelIndexByPoint(e.getPoint());
                if (col >= 0) {
                    ExtTable.this.getTableHeader().setToolTipText(ExtTable.this.getModel().getExtColumnByModelIndex(col).getHeaderTooltip());
                }

            }
        });

        getSelectionModel().addListSelectionListener(new ListSelectionListener() {

            public void valueChanged(final ListSelectionEvent e) {
                ExtTable.this.onSelectionChanged();
                ExtTable.this.eventSender.fireEvent(new ExtTableEvent<ArrayList<E>>(ExtTable.this, ExtTableEvent.Types.SELECTION_CHANGED));

            }

        });

        getTableHeader().setReorderingAllowed(true);
        getTableHeader().setResizingAllowed(true);
        setAutoResizeMode(JTable.AUTO_RESIZE_SUBSEQUENT_COLUMNS);
        setAutoscrolls(true);
        // getTableHeader().setPreferredSize(new
        // Dimension(getColumnModel().getTotalColumnWidth(), 19));
        // assures that the table is painted over the complete available high
        // This method is 1.6 only
        if (Application.getJavaVersion() >= Application.JAVA16) {
            setFillsViewportHeight(true);
        }
        // table should always try to get the full available height
        // this will cause Problems in dialogs. decrease this value if tables
        // are layouted too height
        setPreferredScrollableViewportSize(new Dimension(450, 20000));

        getColumnModel().addColumnModelListener(new TableColumnModelListener() {

            public void columnAdded(final TableColumnModelEvent e) {
            }

            public void columnMarginChanged(final ChangeEvent e) {
            }

            public void columnMoved(final TableColumnModelEvent e) {
                if (e == null) { return; }
                if (e.getFromIndex() == e.getToIndex()) { return; }
                final TableColumnModel tcm = ExtTable.this.getColumnModel();
                for (int i = 0; i < tcm.getColumnCount(); i++) {
                    try {
                        ExtTable.this.getStorage().put(getColumnStoreKey("POS_COL_", i), ExtTable.this.getModel().getExtColumnByModelIndex(tcm.getColumn(i).getModelIndex()).getID());
                    } catch (final Exception e1) {
                        Log.exception(e1);
                    }
                }

            }

            public void columnRemoved(final TableColumnModelEvent e) {
            }

            public void columnSelectionChanged(final ListSelectionEvent e) {
            }

        });

    }

    /**
     * @return
     */
    public String getColumnSaveID() {
        return columnSaveID;
    }

    public void setColumnSaveID(String columnSaveID) {
        if (columnSaveID == null) {
            columnSaveID = DEFAULT_COLUMN_STORE;
        }
        this.columnSaveID = columnSaveID;
    }

    /**
     * adds a row highlighter
     * 
     * @param highlighter
     */
    public void addRowHighlighter(final ExtOverlayRowHighlighter highlighter) {
        this.removeRowHighlighter(highlighter);
        this.rowHighlighters.add(highlighter);
    }

    /**
     * create Columnselection popupmenu. It contains all available columns and
     * let's the user select. The menu does not autoclose on click.
     * 
     * @param extColumn
     * 
     * @return
     */
    protected JPopupMenu columnControlMenu(final ExtColumn<E> extColumn) {
        JPopupMenu popup;
        if (extColumn == null) {
            // controlbutton
            popup = new JPopupMenu();
            for (int i = 0; i < this.getModel().getColumnCount(); i++) {
                this.getModel().getExtColumnByModelIndex(i).extendControlButtonMenu(popup);
            }
        } else {
            popup = extColumn.createHeaderPopup();
            if (popup == null) {
                popup = new JPopupMenu();
            }
        }

        for (int i = 0; i < this.getModel().getColumnCount(); ++i) {
            final int j = i;
            if (this.getModel().isHidable(i)) {
                final ExtCheckBoxMenuItem mi = new ExtCheckBoxMenuItem(this.getModel().getColumnName(i));
                mi.setHideOnClick(false);
                // mis[i] = mi;

                mi.setSelected(this.getModel().isVisible(i));
                mi.addActionListener(new ActionListener() {

                    public void actionPerformed(final ActionEvent e) {
                        ExtTable.this.getModel().setColumnVisible(j, mi.isSelected());
                    }

                });
                popup.add(mi);
            }
        }
        popup.add(new JSeparator());
        if (this.isSearchEnabled()) {
            popup.add(new JMenuItem(new SearchContextAction(this)));
        }
        popup.add(new JMenuItem(new ResetColumns(this)));

        return popup;
    }

    @Override
    protected void configureEnclosingScrollPane() {
        super.configureEnclosingScrollPane();
        this.reconfigureColumnButton();
    }

    /**
     * Creates the columns based on the model
     */
    void createColumns() {
        final TableColumnModel tcm = getColumnModel();

        while (tcm.getColumnCount() > 0) {
            tcm.removeColumn(tcm.getColumn(0));
        }
        final LinkedHashMap<String, TableColumn> columns = new LinkedHashMap<String, TableColumn>();
        for (int i = 0; i < this.getModel().getColumnCount(); ++i) {
            final int j = i;

            final TableColumn tableColumn = new TableColumn(i);

            this.model.getExtColumnByModelIndex(j).setTableColumn(tableColumn, true);

            tableColumn.setHeaderRenderer(this.model.getExtColumnByModelIndex(j).getHeaderRenderer(getTableHeader()) != null ? this.model.getExtColumnByModelIndex(j).getHeaderRenderer(getTableHeader()) : new ExtTableHeaderRenderer(this.model.getExtColumnByModelIndex(j), getTableHeader()));
            // Save column width

            if (!this.model.isVisible(i)) {
                continue;
            }
            columns.put(this.model.getExtColumnByModelIndex(j).getID(), tableColumn);
            // addColumn(tableColumn);
        }
        // restore column position
        int index = 0;
        while (true) {
            if (columns.isEmpty()) {
                break;
            }
            if (index < this.getModel().getColumnCount()) {
                String id;
                try {
                    id = getColumnStore("POS_COL_", index, "");

                    index++;
                    if (id != null) {
                        final TableColumn item = columns.remove(id);

                        if (item != null) {
                            addColumn(item);
                        }
                    }
                } catch (final Exception e) {
                    Log.exception(e);
                }
            } else {
                for (final TableColumn ritem : columns.values()) {
                    addColumn(ritem);
                }
                break;
            }
        }

    }

    // public boolean editCellAt(int row, int column, EventObject e){
    // if( super.editCellAt(row, column, e)){
    //
    // return true;
    // }
    // return false;
    // }

    /**
     * @param <T>
     * @param string
     * @param index
     * @param string2
     * @return
     */
    public <T> T getColumnStore(final String key, final Object key2, final T string2) {
        return getStorage().get(getColumnStoreKey(key, key2), getStorage().get(key + DEFAULT_COLUMN_STORE + key2, string2));
    }

    private JComponent createDefaultColumnButton() {
        final MigPanel p = new MigPanel("ins 0 2 0 0", "[grow,fill]", "[grow,fill]");

        final JButton button;

        button = new JButton(AWUTheme.I().getIcon("exttable/columnButton", -1));

        button.setBorderPainted(false);

        button.setContentAreaFilled(false);
        p.setBackground(null);
        p.setOpaque(false);
        button.addActionListener(new ActionListener() {

            public void actionPerformed(final ActionEvent event) {
                final JButton source = (JButton) event.getSource();
                final int x = source.getLocation().x;
                final int y = source.getLocation().y;

                final JPopupMenu ccm = ExtTable.this.columnControlMenu(null);
                ccm.show(source, x, y);
                if (ccm.getComponentCount() == 0) {
                    Toolkit.getDefaultToolkit().beep();
                }
            }

        });
        p.add(button, "width 12!,height 12!");
        return p;
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.appwork.swing.components.tooltips.ToolTipHandler#createExtTooltip()
     */
    @Override
    public ExtTooltip createExtTooltip(Point position) {
        if (position == null) {
            position = MouseInfo.getPointerInfo().getLocation();
            SwingUtilities.convertPointFromScreen(position, this);
        }
        final int row = this.getRowIndexByPoint(position);
        final ExtColumn<E> col = this.getExtColumnAtPoint(position);
        this.lastTooltipCol = col;
        this.lastTooltipRow = row;
        if (row < 0) { return null; }
        return col.createToolTip(position, this.getModel().getElementAt(row));
    }

    /**
     * By using {@link ExtColumn#setResizable(boolean)} you can lock the widths
     * of a column. Locked Columns can fu%& up resizing.( Imagine, you have
     * resizemode to LAST_column, and last_column is locked..) this doLayout
     * method checks if resizing of the resize column worked. If not, we
     * temporarily switch resizemode to AUTO_RESIZE_SUBSEQUENT_COLUMNS and
     * finally AUTO_RESIZE_ALL_COLUMNS. <br>
     * All in all, this helps to get a much better resizing
     */
    @Override
    public void doLayout() {
        final TableColumn resizeColumn = getTableHeader().getResizingColumn();
        if (resizeColumn == null) {
            super.doLayout();

            return;
        } else {
            final int orgResizeMode = getAutoResizeMode();
            final int beforeWidth = resizeColumn.getWidth();
            super.doLayout();

            if (resizeColumn.getWidth() - beforeWidth != 0) {
                setAutoResizeMode(JTable.AUTO_RESIZE_SUBSEQUENT_COLUMNS);

                resizeColumn.setWidth(beforeWidth);
                super.doLayout();

                if (resizeColumn.getWidth() - beforeWidth != 0) {
                    setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);

                    resizeColumn.setWidth(beforeWidth);
                    super.doLayout();

                    if (this.headerDragging && resizeColumn.getWidth() - beforeWidth != 0) {

                        Toolkit.getDefaultToolkit().beep();
                        getTableHeader().setCursor(null);
                        this.headerDragging = false;

                    }

                }

            } else {

            }
            this.saveWidthsRatio();
            setAutoResizeMode(orgResizeMode);
        }
    }

    private boolean dropLocationSame(final JTable.DropLocation a, final JTable.DropLocation b) {
        if (a == null && b == null) { return true; }
        if (a == null && b != null) { return false; }
        if (b == null && a != null) { return false; }
        if (a.isInsertColumn() != b.isInsertColumn()) { return false; }
        if (a.isInsertRow() != b.isInsertRow()) { return false; }
        if (a.getColumn() != b.getColumn()) { return false; }
        if (a.getRow() != b.getRow()) { return false; }
        return true;
    }

    @Override
    public boolean editCellAt(final int row, final int column, final EventObject e) {
        final boolean ret = super.editCellAt(row, column, e);
        if (ret) {
            // we want focus in the editor
            transferFocus();
        }
        return ret;
    }

    /* we do always create columsn ourself */
    @Override
    public boolean getAutoCreateColumnsFromModel() {
        return false;
    }

    /**
     * converts the colum index to model and returns the column's cell editor
     */
    @Override
    public TableCellEditor getCellEditor(final int row, final int columnIndex) {
        return this.model.getCelleditorByColumn(convertColumnIndexToModel(columnIndex));
    }

    /**
     * COnverts ther colum index to the current model and returns the column's
     * cellrenderer
     */
    @Override
    public TableCellRenderer getCellRenderer(final int row, final int column) {
        return this.model.getCellrendererByColumn(convertColumnIndexToModel(column));
    }

    /**
     * @return the {@link ExtTable#columnBackground}
     * @see ExtTable#columnBackground
     */
    public Color getColumnBackground() {
        return this.columnBackground;
    }

    /**
     * @return the {@link ExtTable#columnBackgroundSelected}
     * @see ExtTable#columnBackgroundSelected
     */
    public Color getColumnBackgroundSelected() {
        return this.columnBackgroundSelected;
    }

    public JComponent getColumnButton() {
        if (this.columnButton == null) {
            this.columnButton = this.createDefaultColumnButton();
        }
        return this.columnButton;
    }

    /**
     * @return the {@link ExtTable#columnForeground}
     * @see ExtTable#columnForeground
     */
    public Color getColumnForeground() {
        return this.columnForeground;
    }

    /**
     * @return the {@link ExtTable#columnForegroundSelected}
     * @see ExtTable#columnForegroundSelected
     */
    public Color getColumnForegroundSelected() {
        return this.columnForegroundSelected;
    }

    /**
     * @return the size of the contextmenu icons
     */
    public int getContextIconSize() {

        return 22;
    }

    /**
     * @return
     */
    public ExtDataFlavor<E> getDataFlavor() {
        // TODO Auto-generated method stub
        return this.flavor;
    }

    @Override
    @SuppressWarnings("unchecked")
    public Component getEditorComponent() {
        // update cell editor.
        if (this.getCellEditor() != null && this.getCellEditor() instanceof ExtColumn) {
            ((ExtColumn) this.getCellEditor()).getTableCellEditorComponent(this, getValueAt(getEditingRow(), getEditingColumn()), isCellSelected(getEditingRow(), getEditingColumn()), getEditingRow(), getEditingColumn(), true);
        }
        return editorComp;
    }

    /**
     * @return the eventSender
     */
    public ExtTableEventSender getEventSender() {
        return this.eventSender;
    }

    /**
     * Returns the real column index at this point
     * 
     * @param point
     */
    public ExtColumn<E> getExtColumnAtPoint(final Point point) {
        final int x = this.getExtColumnModelIndexByPoint(point);
        return this.getModel().getExtColumnByModelIndex(x);
    }

    /**
     * 
     * @param point
     * @return columnModel Index. use
     *         {@link ExtTableModel#getExtColumnByModelIndex(int)} to get the
     *         columninstance
     */
    public int getExtColumnModelIndexByPoint(final Point point) {
        final int x = columnAtPoint(point);
        // this.getColumnModel().get
        return convertColumnIndexToModel(x);
    }

    @SuppressWarnings("unchecked")
    public ExtTableModel<E> getModel() {
        return (ExtTableModel<E>) super.getModel();
    }

    /**
     * Returns the original Celleditor given by the current LAF UI. Used to have
     * an reference to the LAF's default editor
     * 
     * @param row
     * @param column
     * @return
     */
    public TableCellEditor getLafCellEditor(final int row, final int column) {
        return super.getCellEditor(row, column);

    }

    /**
     * Returns the original Cellrenderer given bei the current LAF UI Used to
     * have an reference to the LAF's default renderer
     * 
     * @param row
     * @param column
     * @return
     */
    public TableCellRenderer getLafCellRenderer(final int row, final int column) {
        return super.getCellRenderer(row, column);
    }

    /**
     * @return the rowHighlighters
     */
    public java.util.List<ExtOverlayRowHighlighter> getRowHighlighters() {
        return this.rowHighlighters;
    }

    /**
     * @param point
     * @return
     */
    public int getRowIndexByPoint(final Point point) {
        return rowAtPoint(point);

    }

    // @Override
    // public Point getToolTipLocation(final MouseEvent event) {
    // // this.toolTipPosition = event.getPoint();
    // return super.getToolTipLocation(event);
    // }

    /**
     * @return
     */
    public Storage getStorage() {
        if (this.getModel() == null) {
            new RuntimeException("TableID has to be initialized here");
        }
        return this.getModel().getStorage();
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.appwork.swing.components.tooltips.ToolTipHandler#updateTooltip(org
     * .appwork.swing.components.tooltips.ExtTooltip, java.awt.event.MouseEvent)
     */
    @Override
    public int getTooltipDelay(final Point mousePositionOnScreen) {
        return 0;
    }

    @Override
    public String getToolTipText(final MouseEvent event) {
        return null;
    }

    public boolean isColumnButtonVisible() {
        return this.columnButtonVisible;
    }

    /**
     * @return the searchEnabled
     */
    public boolean isSearchEnabled() {
        return this.searchEnabled;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.appwork.swing.components.tooltips.ToolTipHandler#
     * isTooltipDisabledUntilNextRefocus()
     */
    @Override
    public boolean isTooltipDisabledUntilNextRefocus() {
        // table has handle ech cell as an own component.

        return false;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.appwork.swing.components.tooltips.ToolTipHandler#
     * isTooltipWithoutFocusEnabled()
     */
    @Override
    public boolean isTooltipWithoutFocusEnabled() {
        // TODO Auto-generated method stub
        return false;
    }

    protected JPopupMenu onContextMenu(final JPopupMenu popup, final E contextObject, final List<E> selection, final ExtColumn<E> column, final MouseEvent mouseEvent) {
        return null;
    }

    /**
     * This method will be called when a doubleclick is performed on the object
     * <code>obj</code>
     * 
     * @param obj
     */
    protected boolean onDoubleClick(final MouseEvent e, final E obj) {
        return false;
    }

    /**
     * Override this to handle header sort clicks
     * 
     * @param e
     * @param oldIdentifier
     * @param oldColumn
     * @param newColumn
     * @param col
     * @return
     */
    protected boolean onHeaderSortClick(final MouseEvent e, final ExtColumn<E> oldColumn, final String oldIdentifier, final ExtColumn<E> newColumn) {
        // TODO Auto-generated method stub
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
    protected boolean onRenameClick(final MouseEvent e, final E obj) {
        return false;
    }

    /**
     * 
     */
    protected void onSelectionChanged() {

    }

    /**
     * @param selectedObjects
     * @param evt
     * @return
     */
    protected boolean onShortcutCopy(final List<E> selectedObjects, final KeyEvent evt) {
        return false;
    }

    /**
     * @param selectedObjects
     * @param evt
     * @return
     */
    protected boolean onShortcutCut(final List<E> selectedObjects, final KeyEvent evt) {
        return false;
    }

    /**
     * @param selectedObjects
     * @param evt
     * @param direct
     *            TODO
     * @return
     */
    protected boolean onShortcutDelete(final List<E> selectedObjects, final KeyEvent evt, final boolean direct) {
        return false;
    }

    /**
     * @param selectedObjects
     * @param evt
     * @return
     */
    protected boolean onShortcutPaste(final List<E> selectedObjects, final KeyEvent evt) {
        return false;
    }

    /**
     * @param selectedObjects
     * @param evt
     * @return
     */
    protected boolean onShortcutSearch(final List<E> selectedObjects, final KeyEvent evt) {

        if (this.isSearchEnabled() && hasFocus()) {
            this.startSearch();
            return true;
        }
        return false;
    }

    /**
     * @param e
     * @param obj
     */
    protected boolean onSingleClick(final MouseEvent e, final E obj) {
        return false;
    }

    @Override
    public void paintComponent(final Graphics g) {
        super.paintComponent(g);
        this.paintHighlighters(g);
    }

    /**
     * @param g
     */
    private void paintHighlighters(final Graphics g) {
        /*
         * highlighter TODO: this might get slow for many rows TODO: change
         * order? highlighting columns "overpaint" the text
         */
        if (getRowCount() == 0) { return; }
        final Rectangle visibleRect = getVisibleRect();
        Rectangle first, last;
        // get current width;
        first = getCellRect(0, 0, true);
        last = getCellRect(0, getColumnCount() - 1, true);
        final int width = last.x + last.width - first.x;

        for (final ExtOverlayRowHighlighter rh : this.rowHighlighters) {
            for (int i = 0; i < getRowCount(); i++) {
                first = getCellRect(i, 0, true);

                // skip if the row is not in visible rec
                if (first.y + first.height < visibleRect.y) {
                    continue;
                }
                if (first.y > visibleRect.y + visibleRect.height) {
                    continue;
                }
                if (rh.doHighlight(this, i)) {
                    rh.paint((Graphics2D) g, 0, first.y, width, first.height - 1);
                }
            }
        }

    }

    /**
     * Key selection
     */
    @SuppressWarnings("unchecked")
    @Override
    protected boolean processKeyBinding(final KeyStroke stroke, final KeyEvent evt, final int condition, final boolean pressed) {

        // ctrl + home or ctrl+end should scrol to top/bottom. If we would not
        // catch these events here, they would change the table selection
        // instead
        if (evt.isControlDown() && evt.getKeyCode() == KeyEvent.VK_HOME) { return false; }
        if (evt.isControlDown() && evt.getKeyCode() == KeyEvent.VK_END) { return false; }
        if (!pressed) { return super.processKeyBinding(stroke, evt, condition, pressed); }
        switch (evt.getKeyCode()) {
        case KeyEvent.VK_ESCAPE:
            clearSelection();
            return true;
        case KeyEvent.VK_X:
            if (evt.isControlDown() && !evt.isAltGraphDown() || evt.isMetaDown()) {
                ExtTable.this.eventSender.fireEvent(new ExtTableEvent<List<E>>(ExtTable.this, ExtTableEvent.Types.SHORTCUT_CUT, this.getModel().getSelectedObjects()));
                return this.onShortcutCut(this.getModel().getSelectedObjects(), evt);
            }
            break;
        case KeyEvent.VK_V:
            if (evt.isControlDown() && !evt.isAltGraphDown() || evt.isMetaDown()) {
                ExtTable.this.eventSender.fireEvent(new ExtTableEvent<List<E>>(ExtTable.this, ExtTableEvent.Types.SHORTCUT_PASTE, this.getModel().getSelectedObjects()));
                return this.onShortcutPaste(this.getModel().getSelectedObjects(), evt);
            }
            break;
        case KeyEvent.VK_C:
            if (evt.isControlDown() && !evt.isAltGraphDown() || evt.isMetaDown()) {
                ExtTable.this.eventSender.fireEvent(new ExtTableEvent<List<E>>(ExtTable.this, ExtTableEvent.Types.SHORTCUT_COPY, this.getModel().getSelectedObjects()));
                return this.onShortcutCopy(this.getModel().getSelectedObjects(), evt);
            }
            break;
        case KeyEvent.VK_DELETE:
            if (!evt.isAltDown()) {
                /* no ctrl+alt+del */
                ExtTable.this.eventSender.fireEvent(new ExtTableEvent<Object>(ExtTable.this, ExtTableEvent.Types.SHORTCUT_DELETE, this.getModel().getSelectedObjects(), BinaryLogic.containsSome(evt.getModifiers(), ActionEvent.SHIFT_MASK)));
                return this.onShortcutDelete(this.getModel().getSelectedObjects(), evt, BinaryLogic.containsSome(evt.getModifiers(), ActionEvent.SHIFT_MASK));
            }
            break;
        case KeyEvent.VK_BACK_SPACE:
            if ((evt.isControlDown() && !evt.isAltGraphDown() || evt.isMetaDown()) && !evt.isAltDown()) {
                /* no ctrl+alt+backspace = unix desktop restart */
                ExtTable.this.eventSender.fireEvent(new ExtTableEvent<Object>(ExtTable.this, ExtTableEvent.Types.SHORTCUT_DELETE, this.getModel().getSelectedObjects(), false));

                return this.onShortcutDelete(this.getModel().getSelectedObjects(), evt, false);
            }
            break;
        case KeyEvent.VK_F:
            if (evt.isControlDown() && !evt.isAltGraphDown() || evt.isMetaDown()) {
                ExtTable.this.eventSender.fireEvent(new ExtTableEvent<List<E>>(ExtTable.this, ExtTableEvent.Types.SHORTCUT_SEARCH, this.getModel().getSelectedObjects()));

                return this.onShortcutSearch(this.getModel().getSelectedObjects(), evt);
            }
            break;
        case KeyEvent.VK_UP:
            if (getSelectedRow() == 0 && !evt.isShiftDown()) {
                if (this.getCellEditor() != null) {
                    this.getCellEditor().stopCellEditing();
                }
                changeSelection(getRowCount() - 1, 0, false, false);
                return true;
            }
            break;
        case KeyEvent.VK_DOWN:
            if (getSelectedRow() == getRowCount() - 1 && !evt.isShiftDown()) {
                if (this.getCellEditor() != null) {
                    this.getCellEditor().stopCellEditing();
                }
                changeSelection(0, 0, false, false);
                return true;
            }
            break;
        case KeyEvent.VK_A:
            if (evt.isControlDown() && !evt.isAltGraphDown() || evt.isMetaDown()) {
                if (this.getCellEditor() != null) {
                    this.getCellEditor().stopCellEditing();
                }
                getSelectionModel().setSelectionInterval(0, getRowCount() - 1);
                return true;
            }
            break;
        case KeyEvent.VK_HOME:
            if (evt.isControlDown() && !evt.isAltGraphDown() || evt.isMetaDown() || evt.isShiftDown()) {
                if (this.getCellEditor() != null) {
                    this.getCellEditor().stopCellEditing();
                }
                if (getSelectedRow() != -1 && getRowCount() != 0) {
                    getSelectionModel().setSelectionInterval(0, getSelectedRows()[getSelectedRows().length - 1]);
                    /* to avoid selection by super.processKeyBinding */
                    return true;
                }
            } else {
                if (this.getCellEditor() != null) {
                    this.getCellEditor().stopCellEditing();
                }
                getSelectionModel().setSelectionInterval(0, 0);
            }
            break;
        case KeyEvent.VK_END:
            if (evt.isControlDown() && !evt.isAltGraphDown() || evt.isMetaDown() || evt.isShiftDown()) {
                if (this.getCellEditor() != null) {
                    this.getCellEditor().stopCellEditing();
                }
                if (getSelectedRow() != -1 && getRowCount() != 0) {
                    getSelectionModel().setSelectionInterval(getSelectedRow(), getRowCount() - 1);
                    /* to avoid selection by super.processKeyBinding */
                    return true;
                }
            } else {
                if (this.getCellEditor() != null) {
                    this.getCellEditor().stopCellEditing();
                }
                if (getRowCount() != 0) {
                    getSelectionModel().setSelectionInterval(getRowCount() - 1, getRowCount() - 1);
                }
            }
            break;
        }
        return super.processKeyBinding(stroke, evt, condition, pressed);
    }

    @SuppressWarnings("unchecked")
    @Override
    protected void processMouseEvent(final MouseEvent e) {
        if (e.getID() == MouseEvent.MOUSE_RELEASED) {
            if (e.isPopupTrigger() || e.getButton() == MouseEvent.BUTTON3) {
                final int row = rowAtPoint(e.getPoint());
                final E obj = this.getModel().getObjectbyRow(row);
                final ExtColumn<E> col = this.getExtColumnAtPoint(e.getPoint());

                if (obj == null || row == -1) {
                    /* no object under mouse, lets clear the selection */
                    clearSelection();
                    final JPopupMenu popup = this.onContextMenu(new JPopupMenu(), null, null, col, e);
                    this.eventSender.fireEvent(new ExtTableEvent<JPopupMenu>(this, ExtTableEvent.Types.CONTEXTMENU, popup));
                    if (popup != null && popup.getComponentCount() > 0) {
                        popup.show(ExtTable.this, e.getPoint().x, e.getPoint().y);
                        return;
                    }

                } else {
                    /* check if we need to select object */
                    if (!isRowSelected(row)) {
                        clearSelection();
                        addRowSelectionInterval(row, row);
                    }
                    final List<E> selected = this.getModel().getSelectedObjects();
                    final JPopupMenu popup = this.onContextMenu(new JPopupMenu(), obj, selected, col, e);

                    if (popup != null && popup.getComponentCount() > 0) {
                        popup.show(ExtTable.this, e.getPoint().x, e.getPoint().y);
                        return;
                    }

                }
            }
        } else if (e.getID() == MouseEvent.MOUSE_CLICKED) {

            if (e.getButton() == MouseEvent.BUTTON1 && e.getClickCount() == 2) {
                final int row = rowAtPoint(e.getPoint());
                final E obj = this.getModel().getObjectbyRow(row);
                final ExtColumn<E> col = this.getExtColumnAtPoint(e.getPoint());
                this.renameClickDelayer.stop();
                boolean ret = false;
                if (col != null) {
                    ret = col.onDoubleClick(e, obj);
                }
                if (obj != null && ret == false) {
                    ret = this.onDoubleClick(e, obj);
                    this.eventSender.fireEvent(new ExtTableEvent<E>(this, ExtTableEvent.Types.DOUBLECLICK, obj));
                }
                if (ret == true) { return; }
            } else if (e.getButton() == MouseEvent.BUTTON1 && e.getClickCount() == 1) {
                final int row = rowAtPoint(e.getPoint());
                final E obj = this.getModel().getObjectbyRow(row);
                final ExtColumn<E> col = this.getExtColumnAtPoint(e.getPoint());
                boolean ret = false;
                if (col != null) {

                    ret = col.onSingleClick(e, obj);
                }
                if (obj != null && ret == false) {
                    ret = this.onSingleClick(e, obj);
                }
                if (ret == true) { return; }
                if (this.clickDelayerRunable != null) {
                    this.renameClickDelayer.resetAndStart();
                }
            }
        } else if (e.getID() == MouseEvent.MOUSE_PRESSED) {
            if (rowAtPoint(e.getPoint()) < 0) {
                clearSelection();
            } else if (e.getClickCount() == 1 && e.getButton() == MouseEvent.BUTTON1) {
                final int[] slRows = getSelectedRows();
                final int row = rowAtPoint(e.getPoint());
                if (slRows.length == 1 && row == slRows[0]) {
                    // rename
                    final E obj = this.getModel().getObjectbyRow(row);
                    final ExtColumn<E> col = this.getExtColumnAtPoint(e.getPoint());

                    if (col != null) {

                        this.clickDelayerRunable = new Runnable() {

                            @Override
                            public void run() {
                                new EDTRunner() {

                                    @Override
                                    protected void runInEDT() {
                                        if (!getSelectionModel().isSelectedIndex(row)) {
                                            return;
                                        }

                                        if (!col.onRenameClick(e, obj)) {
                                            ExtTable.this.onRenameClick(e, obj);

                                        }
                                        // we have to dispatch this event. the
                                        // new
                                        // editor will
                                        // get this event as first mousevent.
                                        // textfields
                                        // for
                                        // example will get focused
                                        ExtTable.this.setDispatchComponent(e);

                                        final CellEditor ce = ExtTable.this.getCellEditor();
                                        if (ce != null) {
                                            ce.shouldSelectCell(e);

                                        }
                                    }
                                };

                            }
                        };

                    }

                }
            }
        }
        /*
         * moved this at the end of this function so it does not pre-interfere
         * with customized click handling (eg changing selection)
         */
        super.processMouseEvent(e);
    }

    /**
     * we listen on dropLocation-changes to maintain correct rowHighlighters!
     */
    @Override
    public void propertyChange(final PropertyChangeEvent evt) {
        if ("dropLocation".equals(evt.getPropertyName())) {
            final JTable.DropLocation oldLoc = (JTable.DropLocation) evt.getOldValue();
            final JTable.DropLocation newLoc = (JTable.DropLocation) evt.getNewValue();
            if (!this.dropLocationSame(oldLoc, newLoc)) {
                /* dropLocation changed, so lets refresh rowHighlighters */
                /* refresh highlighters during dnd */
                SwingUtilities.invokeLater(new Runnable() {

                    @Override
                    public void run() {
                        ExtTable.this.repaint();
                    }

                });
            }
        }
    }

    protected void reconfigureColumnButton() {
        final Container c1 = getParent();
        if (c1 instanceof JViewport) {
            final Container c2 = c1.getParent();
            if (c2 instanceof JScrollPane) {
                final JScrollPane scrollPane = (JScrollPane) c2;
                final JViewport viewport = scrollPane.getViewport();
                if (viewport == null || viewport.getView() != this) { return; }
                if (this.isColumnButtonVisible()) {
                    this.verticalScrollPolicy = scrollPane.getVerticalScrollBarPolicy();
                    scrollPane.setCorner(ScrollPaneConstants.UPPER_TRAILING_CORNER, this.getColumnButton());
                    scrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
                } else {
                    if (this.verticalScrollPolicy != 0) {
                        /* http://java.net/jira/browse/SWINGX-155 */
                        scrollPane.setVerticalScrollBarPolicy(this.verticalScrollPolicy);
                    }
                    try {
                        scrollPane.setCorner(ScrollPaneConstants.UPPER_TRAILING_CORNER, null);
                    } catch (final Throwable nothing) {
                    }
                }
            }
        }
    }

    /**
     * Removes a rowhilighter
     * 
     * @param highlighter
     */
    public void removeRowHighlighter(final ExtOverlayRowHighlighter highlighter) {
        this.rowHighlighters.remove(highlighter);
    }

    /**
     * Resets the columnwidth to their default value. If their is empty space
     * afterwards, the table will distribute the DELTA to all columns PLease
     * make sure to call {@link #updateColumns()} afterwards
     */
    public void resetColumnDimensions() {

        for (final ExtColumn<E> col : this.getModel().getColumns()) {
            // col.getTableColumn().setPreferredWidth(col.getDefaultWidth());
            try {
                this.getStorage().put(getColumnStoreKey("WIDTH_COL_", col.getID()), col.getDefaultWidth());
            } catch (final Exception e) {
                Log.exception(e);
            }
        }

    }

    /**
     * Resets the column locks to their default value. If their is empty space
     * afterwards, the table will distribute the DELTA to all columns PLease
     * make sure to call {@link #updateColumns()} afterwards
     */
    public void resetColumnLocks() {
        for (final ExtColumn<E> col : this.getModel().getColumns()) {
            // col.getTableColumn().setPreferredWidth(col.getDefaultWidth());
            this.getStorage().put("ColumnWidthLocked_" + getColumnSaveID() + col.getID(), !col.isDefaultResizable());

        }
    }

    // /**
    // * @param b
    // */
    // public synchronized void setAutoResizeFallbackEnabled(final boolean b) {
    //
    // if (b == this.autoResizeFallback) { return; }
    //
    // if (b) {
    // this.orgAutoResizeMode = this.getAutoResizeMode();
    // this.setAutoResizeMode(JTable.AUTO_RESIZE_SUBSEQUENT_COLUMNS);
    // this.autoResizeFallback = b;
    // } else {
    // this.autoResizeFallback = b;
    // if (this.orgAutoResizeMode >= 0) {
    // this.setAutoResizeMode(this.orgAutoResizeMode);
    // }
    // this.orgAutoResizeMode = -1;
    //
    // }
    //
    // }

    // @Override
    // public void setAutoResizeMode(final int mode) {
    //
    // if (this.autoResizeFallback) {
    // System.out.println("Keep mode: " + mode);
    // this.orgAutoResizeMode = mode;
    // } else {
    // System.out.println("Mode: " + mode);
    // super.setAutoResizeMode(mode);
    // }
    //
    // }

    /**
     * Resets the Order of the columns to their default PLease make sure to call
     * {@link #updateColumns()} afterwards
     */
    public void resetColumnOrder() {
        for (int i = 0; i < this.getModel().getColumns().size(); i++) {
            // col.getTableColumn().setPreferredWidth(col.getDefaultWidth());
            final ExtColumn<E> col = this.getModel().getColumns().get(i);
            try {
                this.getStorage().put(getColumnStoreKey("POS_COL_", i), col.getID());
            } catch (final Exception e1) {
                Log.exception(e1);
            }
        }

    }

    /**
     * Resets the Visibility of all columns to their Default value. PLease make
     * sure to call {@link #updateColumns()} afterwards
     */
    public void resetColumnVisibility() {
        for (final ExtColumn<E> col : this.getModel().getColumns()) {
            // col.getTableColumn().setPreferredWidth(col.getDefaultWidth());
            try {
                this.getStorage().put(getColumnStoreKey("VISABLE_COL_", col.getID()), col.isDefaultVisible());
            } catch (final Exception e) {
                Log.exception(e);
            }
        }

    }

    public void saveWidthsRatio() {
        for (int i = 0; i < getColumnCount(); i++) {
            final ExtColumn<E> col = this.getModel().getExtColumnByModelIndex(convertColumnIndexToModel(i));

            try {
                col.getTableColumn().setPreferredWidth(col.getTableColumn().getWidth());
                ExtTable.this.getStorage().put(getColumnStoreKey("WIDTH_COL_", col.getID()), col.getTableColumn().getWidth());
            } catch (final Exception e) {
                Log.exception(e);
            }

        }
    }

    public void scrollToRow(final int row, final int preferedXPosition) {

        if (row < 0) { return; }
        new EDTHelper<Object>() {

            @Override
            public Object edtRun() {
                final JViewport viewport = (JViewport) ExtTable.this.getParent();
                if (viewport == null) { return null; }
                final Rectangle rect = ExtTable.this.getCellRect(row, 0, true);

                final Rectangle viewRect = viewport.getViewRect();
                rect.width = viewRect.width;
                rect.height = viewRect.height;
                if (preferedXPosition < 0) {
                    rect.x = viewRect.x;
                } else {
                    rect.x = preferedXPosition;
                }

                scrollRectToVisible(rect);

                return null;
            }

        }.start();

    }

    public void scrollToSelection(final int preferedXPosition) {

        new EDTHelper<Object>() {

            @Override
            public Object edtRun() {
                final JViewport viewport = (JViewport) ExtTable.this.getParent();
                if (viewport == null) { return null; }
                final int[] sel = ExtTable.this.getSelectedRows();
                if (sel == null || sel.length == 0) { return null; }
                final Rectangle rect = ExtTable.this.getCellRect(sel[0], 0, true);
                final Rectangle rect2 = ExtTable.this.getCellRect(sel[sel.length - 1], 0, true);
                rect.height += rect2.y - rect.y;
                final Rectangle viewRect = viewport.getViewRect();
                rect.width = viewRect.width;
                if (preferedXPosition < 0) {
                    rect.x = viewRect.x;
                } else {
                    rect.x = preferedXPosition;
                }
                scrollRectToVisible(rect);
                return null;
            }

        }.start();
    }

    public void setColumnBottonVisibility(final boolean visible) {
        this.columnButtonVisible = visible;
        this.reconfigureColumnButton();
    }

    public void setColumnButton(final JComponent c) {
        this.columnButton = c;
        this.reconfigureColumnButton();
    }

    private void setDispatchComponent(final MouseEvent e) {
        final Component editorComponent = this.getEditorComponent();
        if (editorComponent != null) {
            final Point p = e.getPoint();
            final Point p2 = SwingUtilities.convertPoint(this, p, editorComponent);
            final Component dispatchComponent = SwingUtilities.getDeepestComponentAt(editorComponent, p2.x, p2.y);
            if (dispatchComponent != null) {
                SwingUtilities2.setSkipClickCount(dispatchComponent, e.getClickCount() - 1);
                final MouseEvent e2 = SwingUtilities.convertMouseEvent(this, e, dispatchComponent);
                dispatchComponent.dispatchEvent(e2);
            }
        }
    }

    //
    // private int viewIndexForColumn(final TableColumn aColumn) {
    // final TableColumnModel cm = this.getColumnModel();
    // for (int column = 0; column < cm.getColumnCount(); column++) {
    // if (cm.getColumn(column) == aColumn) { return column; }
    // }
    // return -1;
    // }

    /**
     * @param searchEnabled
     *            the searchEnabled to set
     */
    public void setSearchEnabled(final boolean searchEnabled) {
        this.searchEnabled = searchEnabled;
    }

    public void setTransferHandler(final ExtTransferHandler<E> newHandler) {

        newHandler.setTable(this);
        super.setTransferHandler(newHandler);

    }

    /**
     * @return
     */
    protected long setupRenameClickInterval() {
        return 500;
    }

    /**
     * Starts a Search Prozess. Usualy opens a Search Dialog
     */
    public synchronized void startSearch() {
        try {
            if (this.searchDialog != null && this.searchDialog.isShowing()) {
                this.searchDialog.requestFocus();
            } else {
                this.searchDialog = new SearchDialog(SearchDialog.NO_REGEX_FLAG, this) {

                    private static final long serialVersionUID = 2652101312418765845L;

                    @Override
                    public void actionPerformed(final ActionEvent e) {
                        final String ret = ExtTable.this.searchDialog.getReturnID();
                        if (ret != null) {
                            final int[] sel = ExtTable.this.getSelectedRows();
                            int startRow = -1;
                            if (sel != null & sel.length > 0) {
                                startRow = sel[sel.length - 1];
                            }
                            final E found = ExtTable.this.getModel().searchNextObject(startRow + 1, ret, ExtTable.this.searchDialog.isCaseSensitive(), ExtTable.this.searchDialog.isRegex());
                            ExtTable.this.getModel().setSelectedObject(found);
                            ExtTable.this.scrollToSelection(-1);
                        }
                    }

                };
            }
        } catch (final IOException e) {
            Log.exception(e);
        }
    }

    public void updateColumns() {
        new EDTRunner() {

            @Override
            protected void runInEDT() {
                ExtTable.this.createColumns();
                ExtTable.this.revalidate();
                ExtTable.this.repaint();
            }
        };
    }

    @Override
    public boolean updateTooltip(final ExtTooltip activeToolTip, final MouseEvent e) {
        final int row = this.getRowIndexByPoint(e.getPoint());
        final ExtColumn<E> col = this.getExtColumnAtPoint(e.getPoint());
        return this.lastTooltipCol != col || this.lastTooltipRow != row;
    }

    public String getColumnStoreKey(final String key1, final Object key2) {
        return key1 + getColumnSaveID() + key2;
    }

}
