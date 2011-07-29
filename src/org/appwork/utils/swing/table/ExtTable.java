package org.appwork.utils.swing.table;

import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;

import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JToolTip;
import javax.swing.JViewport;
import javax.swing.KeyStroke;
import javax.swing.ScrollPaneConstants;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.TableColumnModelEvent;
import javax.swing.event.TableColumnModelListener;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;

import org.appwork.app.gui.MigPanel;
import org.appwork.resources.AWUTheme;
import org.appwork.storage.JSonStorage;
import org.appwork.utils.Application;
import org.appwork.utils.BinaryLogic;
import org.appwork.utils.logging.Log;
import org.appwork.utils.swing.EDTHelper;

/**
 * The ExtTable package is a tableframework that follows two main tasks:<br>
 * 1. Easy creating of tables<br>
 * 2. Implement extended features like column selection, database connection,
 * editing, easy rendering, sorting etc.
 * 
 * @author $Author: unknown$
 */
public class ExtTable<E> extends JTable {

    private static final long                  serialVersionUID    = 2822230056021924679L;
    /**
     * Column background color if column is NOT selected
     */
    private final Color                        columnBackground;
    /**
     * Column background color if column is selected
     */
    private final Color                        columnBackgroundSelected;
    /**
     * Column textcolor if column is NOT selected
     */
    private final Color                        columnForeground;
    /**
     * Column textcolor if column is selected
     */
    private final Color                        columnForegroundSelected;

    /**
     * The underlaying datamodel
     */
    private final ExtTableModel<E>             model;
    /**
     * TableID. Used to generate a key for saving internal data to database
     */
    private final String                       tableID;

    final private ArrayList<ExtRowHighlighter> rowHighlighters;
    /**
     * true if search is enabled
     */
    private boolean                            searchEnabled       = false;
    private SearchDialog                       searchDialog;
    private final ExtTableEventSender          eventSender;
    private JComponent                         columnButton        = null;
    private boolean                            columnButtonVisible = true;
    private int                                verticalScrollPolicy;

    private E                                  toolTipObject;
    private JToolTip                           tooltip;

    /**
     * @param downloadTableModel
     */
    public ExtTable(final ExtTableModel<E> tableModel) {
        this(tableModel, tableModel.getModelID() + "-Table");
    }

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
    public ExtTable(final ExtTableModel<E> model, final String id) {
        super(model);
        this.eventSender = new ExtTableEventSender();
        this.tableID = id;
        this.rowHighlighters = new ArrayList<ExtRowHighlighter>();
        this.model = model;
        // workaround
        this.setColumnModel(new ExtColumnModel(this.getColumnModel()));

        model.setTable(this);
        this.createColumns();
        // get defaultbackground and Foregroundcolors
        Component c = super.getCellRenderer(0, 0).getTableCellRendererComponent(this, "", true, false, 0, 0);
        this.columnBackgroundSelected = c.getBackground();
        this.columnForegroundSelected = c.getForeground();

        c = super.getCellRenderer(0, 0).getTableCellRendererComponent(this, "", false, false, 0, 0);
        this.columnBackground = c.getBackground();
        this.columnForeground = c.getForeground();

        // Mouselistener for columnselection Menu and sort on click
        this.getTableHeader().addMouseListener(new MouseAdapter() {
            int columnPressed = 0;

            @Override
            public void mousePressed(final MouseEvent e) {

                // only if we are not in resize mode
                if (ExtTable.this.getTableHeader().getCursor().getType() == Cursor.getDefaultCursor().getType()) {
                    if (e.getButton() == MouseEvent.BUTTON1) {
                        this.columnPressed = ExtTable.this.columnAtPoint(e.getPoint());
                    } else if (e.getButton() == MouseEvent.BUTTON3) {
                        final JPopupMenu ccm = ExtTable.this.columnControlMenu(ExtTable.this.getExtColumnAtPoint(e.getPoint()));
                        ccm.show(ExtTable.this.getTableHeader(), e.getX(), e.getY());

                        if (ccm.getComponentCount() == 0) {
                            Toolkit.getDefaultToolkit().beep();
                        }
                    }
                }
            }

            @SuppressWarnings("unchecked")
            @Override
            public void mouseReleased(final MouseEvent e) {
                if (ExtTable.this.getTableHeader().getCursor().getType() == Cursor.getDefaultCursor().getType()) {
                    if (e.getButton() == MouseEvent.BUTTON1) {

                        if (this.columnPressed != ExtTable.this.columnAtPoint(e.getPoint())) { return; }
                        final int col = ExtTable.this.getExtColumnIndexByPoint(e.getPoint());
                        if (col == -1) { return; }

                        if (ExtTable.this.getExtTableModel().getExtColumn(col).isSortable(null)) {
                            final ExtColumn<E> oldColumn = ExtTable.this.getExtTableModel().getSortColumn();
                            final String oldIdentifier = oldColumn == null ? null : oldColumn.getSortOrderIdentifier();

                            ExtTable.this.getExtTableModel().getExtColumn(col).doSort();
                            ExtTable.this.eventSender.fireEvent(new ExtTableEvent<MouseEvent>(ExtTable.this, ExtTableEvent.Types.SORT_HEADER_CLICK, e));
                            ExtTable.this.onHeaderSortClick(e, oldColumn, oldIdentifier);
                        }
                    }
                }
            }

        });
        // mouselistener to display column header tooltips
        this.getTableHeader().addMouseMotionListener(new MouseAdapter() {

            @Override
            public void mouseMoved(final MouseEvent e) {
                final int col = ExtTable.this.getExtColumnIndexByPoint(e.getPoint());
                if (col >= 0) {
                    ExtTable.this.getTableHeader().setToolTipText(ExtTable.this.getExtTableModel().getExtColumn(col).getName());
                }

            }
        });

        this.getSelectionModel().addListSelectionListener(new ListSelectionListener() {

            @SuppressWarnings("unchecked")
            public void valueChanged(final ListSelectionEvent e) {
                ArrayList<E> sel = ExtTable.this.getExtTableModel().getSelectedObjects();
                if (sel != null && sel.size() == 0) {
                    sel = null;
                }
                ExtTable.this.onSelectionChanged(sel);
                ExtTable.this.eventSender.fireEvent(new ExtTableEvent<ArrayList<E>>(ExtTable.this, ExtTableEvent.Types.SELECTION_CHANGED, sel));

            }

        });

        this.getTableHeader().setReorderingAllowed(true);
        this.getTableHeader().setResizingAllowed(true);
        this.setAutoResizeMode(JTable.AUTO_RESIZE_SUBSEQUENT_COLUMNS);
        this.setAutoscrolls(true);
        this.getTableHeader().setPreferredSize(new Dimension(this.getColumnModel().getTotalColumnWidth(), 19));
        // assures that the table is painted over the complete available high
        // This method is 1.6 only
        if (Application.getJavaVersion() >= 16000000) {
            this.setFillsViewportHeight(true);
        }

        this.getColumnModel().addColumnModelListener(new TableColumnModelListener() {

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
                        JSonStorage.getStorage("ExtTable_" + ExtTable.this.tableID).put("POS_COL_" + i, ExtTable.this.getExtTableModel().getExtColumn(tcm.getColumn(i).getModelIndex()).getID());
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
     * adds a row highlighter
     * 
     * @param highlighter
     */
    public void addRowHighlighter(final ExtRowHighlighter highlighter) {
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
            for (int i = 0; i < this.getExtTableModel().getColumnCount(); i++) {
                this.getExtTableModel().getExtColumn(i).extendControlButtonMenu(popup);
            }
        } else {
            popup = extColumn.createHeaderPopup();
            if (popup == null) {
                popup = new JPopupMenu();
            }
        }

        for (int i = 0; i < this.getExtTableModel().getColumnCount(); ++i) {
            final int j = i;
            if (this.getExtTableModel().isHidable(i)) {
                final ExtCheckBoxMenuItem mi = new ExtCheckBoxMenuItem(this.getExtTableModel().getColumnName(i));
                mi.setHideOnClick(false);
                // mis[i] = mi;

                mi.setSelected(this.getExtTableModel().isVisible(i));
                mi.addActionListener(new ActionListener() {

                    public void actionPerformed(final ActionEvent e) {
                        ExtTable.this.getExtTableModel().setColumnVisible(j, mi.isSelected());
                    }

                });
                popup.add(mi);
            }
        }
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
        final TableColumnModel tcm = this.getColumnModel();

        while (tcm.getColumnCount() > 0) {
            tcm.removeColumn(tcm.getColumn(0));
        }
        final LinkedHashMap<String, TableColumn> columns = new LinkedHashMap<String, TableColumn>();
        for (int i = 0; i < this.getModel().getColumnCount(); ++i) {
            final int j = i;

            final TableColumn tableColumn = new TableColumn(i);
            this.model.getExtColumn(j).setTableColumn(tableColumn);
            tableColumn.setHeaderRenderer(this.model.getExtColumn(j).getHeaderRenderer(this.getTableHeader()) != null ? this.model.getExtColumn(j).getHeaderRenderer(this.getTableHeader()) : new ExtTableHeaderRenderer(this.model.getExtColumn(j), this.getTableHeader()));
            // Save column width
            tableColumn.addPropertyChangeListener(new PropertyChangeListener() {
                public void propertyChange(final PropertyChangeEvent evt) {
                    if (evt.getPropertyName().equals("width")) {
                        try {
                            JSonStorage.getStorage("ExtTable_" + ExtTable.this.tableID).put("WIDTH_COL_" + ExtTable.this.model.getExtColumn(j).getID(), (Integer) evt.getNewValue());
                        } catch (final Exception e) {
                            Log.exception(e);
                        }
                    }
                }
            });
            if (this.model.getExtColumn(j).getMaxWidth() >= 0) {
                tableColumn.setMaxWidth(this.model.getExtColumn(j).getMaxWidth());
            }
            if (this.model.getExtColumn(j).getMinWidth() >= 0) {
                tableColumn.setMinWidth(this.model.getExtColumn(j).getMinWidth());
            }

            // Set stored columnwidth
            int w = this.model.getExtColumn(j).getDefaultWidth();
            try {
                w = JSonStorage.getStorage("ExtTable_" + this.tableID).get("WIDTH_COL_" + this.model.getExtColumn(j).getID(), w);
            } catch (final Exception e) {
                Log.exception(e);
            } finally {
                tableColumn.setPreferredWidth(w);
            }
            if (!this.model.isVisible(i)) {
                continue;
            }
            columns.put(this.model.getExtColumn(j).getID(), tableColumn);
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
                    id = JSonStorage.getStorage("ExtTable_" + this.tableID).get("POS_COL_" + index, "");

                    index++;
                    if (id != null) {
                        final TableColumn item = columns.remove(id);

                        if (item != null) {
                            this.addColumn(item);
                        }
                    }
                } catch (final Exception e) {
                    Log.exception(e);
                }
            } else {
                for (final TableColumn ritem : columns.values()) {
                    this.addColumn(ritem);
                }
                break;
            }
        }

    }

    private JComponent createDefaultColumnButton() {
        final MigPanel p = new MigPanel("ins 0 2 0 0", "[grow,fill]", "[grow,fill]");

        final JButton button;

        button = new JButton(AWUTheme.I().getIcon("columnButton", -1));

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

    @Override
    public JToolTip createToolTip() {
        if (this.tooltip == null) {
            System.out.println("NULLTIP");
        }
        return this.tooltip;
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
    public TableCellEditor getCellEditor(final int row, final int column) {
        return this.model.getCelleditorByColumn(this.convertColumnIndexToModel(column));
    }

    /**
     * COnverts ther colum index to the current model and returns the column's
     * cellrenderer
     */
    @Override
    public TableCellRenderer getCellRenderer(final int row, final int column) {
        return this.model.getCellrendererByColumn(this.convertColumnIndexToModel(column));
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
        final int x = this.getExtColumnIndexByPoint(point);
        return this.getExtTableModel().getExtColumn(x);
    }

    /**
     * returns the real column at the given point. Method converts the column to
     * the moduels colum
     * 
     * @param point
     * @return
     */
    public int getExtColumnIndexByPoint(final Point point) {
        final int x = this.columnAtPoint(point);
        // this.getColumnModel().get
        return this.convertColumnIndexToModel(x);
    }

    public ExtTableModel<E> getExtTableModel() {
        return this.model;
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
    public ArrayList<ExtRowHighlighter> getRowHighlighters() {
        return this.rowHighlighters;
    }

    /**
     * @param point
     * @return
     */
    public int getRowIndexByPoint(final Point point) {
        return this.rowAtPoint(point);

    }

    /**
     * @return the tableID
     */
    public String getTableID() {
        return this.tableID;
    }

    @Override
    public String getToolTipText(final MouseEvent event) {
        /*
         * 
         * this method gets called very often. it is not a good idea to create
         * the tooltip here.
         */

        final int row = this.getRowIndexByPoint(event.getPoint());
        if (row < 0) { return null; }
        final ExtColumn<E> col = this.getExtColumnAtPoint(event.getPoint());
        this.toolTipObject = this.getExtTableModel().getElementAt(row);
        this.tooltip = col.createToolTip(this.toolTipObject);
        if (this.tooltip != null) {
            this.tooltip.setComponent(this);
        }
        return this.tooltip != null ? "" + col.hashCode() + "_" + row : null;
    }

    // @Override
    // public Point getToolTipLocation(final MouseEvent event) {
    // // this.toolTipPosition = event.getPoint();
    // return super.getToolTipLocation(event);
    // }

    public boolean isColumnButtonVisible() {
        return this.columnButtonVisible;
    }

    /**
     * @return the searchEnabled
     */
    public boolean isSearchEnabled() {
        return this.searchEnabled;
    }

    protected JPopupMenu onContextMenu(final JPopupMenu popup, final E contextObject, final ArrayList<E> selection, final ExtColumn<E> column) {
        return null;
    }

    /**
     * This method will be called when a doubleclick is performed on the object
     * <code>obj</code>
     * 
     * @param obj
     */
    protected void onDoubleClick(final MouseEvent e, final E obj) {
    }

    /**
     * Override this to handle header sort clicks
     * 
     * @param e
     * @param oldIdentifier
     * @param oldColumn
     */
    protected void onHeaderSortClick(final MouseEvent e, final ExtColumn<E> oldColumn, final String oldIdentifier) {
        // TODO Auto-generated method stub

    }

    protected void onSelectionChanged(final ArrayList<E> selected) {
    }

    /**
     * @param selectedObjects
     * @param evt
     * @return
     */
    protected boolean onShortcutCopy(final ArrayList<E> selectedObjects, final KeyEvent evt) {
        return false;
    }

    /**
     * @param selectedObjects
     * @param evt
     * @return
     */
    protected boolean onShortcutCut(final ArrayList<E> selectedObjects, final KeyEvent evt) {
        return false;
    }

    /**
     * @param selectedObjects
     * @param evt
     * @param direct
     *            TODO
     * @return
     */
    protected boolean onShortcutDelete(final ArrayList<E> selectedObjects, final KeyEvent evt, final boolean direct) {
        return false;
    }

    /**
     * @param selectedObjects
     * @param evt
     * @return
     */
    protected boolean onShortcutPaste(final ArrayList<E> selectedObjects, final KeyEvent evt) {
        return false;
    }

    /**
     * @param selectedObjects
     * @param evt
     * @return
     */
    protected boolean onShortcutSearch(final ArrayList<E> selectedObjects, final KeyEvent evt) {

        if (this.isSearchEnabled() && this.hasFocus()) {
            this.startSearch();
            return true;
        }
        return false;
    }

    /**
     * @param e
     * @param obj
     */
    protected void onSingleClick(final MouseEvent e, final E obj) {

    }

    @Override
    public void paintComponent(final Graphics g) {
        super.paintComponent(g);
        /*
         * highlighter TODO: this might get slow for many rows TODO: change
         * order? highlighting columns "overpaint" the text
         */
        if (this.getRowCount() == 0) { return; }
        final Rectangle visibleRect = this.getVisibleRect();
        Rectangle first, last;
        // get current width;
        first = this.getCellRect(0, 0, true);
        last = this.getCellRect(0, this.getColumnCount() - 1, true);
        final int width = last.x + last.width - first.x;

        for (final ExtRowHighlighter rh : this.rowHighlighters) {
            for (int i = 0; i < this.getRowCount(); i++) {
                first = this.getCellRect(i, 0, true);

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
        if (!pressed) { return super.processKeyBinding(stroke, evt, condition, pressed); }

        switch (evt.getKeyCode()) {
        case KeyEvent.VK_ESCAPE:
            this.clearSelection();
            return true;
        case KeyEvent.VK_X:
            if (evt.isControlDown() || evt.isMetaDown()) {
                ExtTable.this.eventSender.fireEvent(new ExtTableEvent<ArrayList<E>>(ExtTable.this, ExtTableEvent.Types.SHORTCUT_CUT, this.getExtTableModel().getSelectedObjects()));
                return this.onShortcutCut(this.getExtTableModel().getSelectedObjects(), evt);
            }
            break;
        case KeyEvent.VK_V:

            if (evt.isControlDown() || evt.isMetaDown()) {
                ExtTable.this.eventSender.fireEvent(new ExtTableEvent<ArrayList<E>>(ExtTable.this, ExtTableEvent.Types.SHORTCUT_PASTE, this.getExtTableModel().getSelectedObjects()));
                return this.onShortcutPaste(this.getExtTableModel().getSelectedObjects(), evt);
            }
            break;
        case KeyEvent.VK_C:
            if (evt.isControlDown() || evt.isMetaDown()) {
                ExtTable.this.eventSender.fireEvent(new ExtTableEvent<ArrayList<E>>(ExtTable.this, ExtTableEvent.Types.SHORTCUT_COPY, this.getExtTableModel().getSelectedObjects()));
                return this.onShortcutCopy(this.getExtTableModel().getSelectedObjects(), evt);

            }
            break;
        case KeyEvent.VK_DELETE:
            if (!evt.isAltDown()) {
                /* no ctrl+alt+del */
                ExtTable.this.eventSender.fireEvent(new ExtTableEvent<Object>(ExtTable.this, ExtTableEvent.Types.SHORTCUT_DELETE, this.getExtTableModel().getSelectedObjects(), BinaryLogic.containsSome(evt.getModifiers(), ActionEvent.SHIFT_MASK)));
                return this.onShortcutDelete(this.getExtTableModel().getSelectedObjects(), evt, BinaryLogic.containsSome(evt.getModifiers(), ActionEvent.SHIFT_MASK));
            }
            break;
        case KeyEvent.VK_BACK_SPACE:
            if ((evt.isControlDown() || evt.isMetaDown()) && !evt.isAltDown()) {
                /* no ctrl+alt+backspace = unix desktop restart */
                ExtTable.this.eventSender.fireEvent(new ExtTableEvent<Object>(ExtTable.this, ExtTableEvent.Types.SHORTCUT_DELETE, this.getExtTableModel().getSelectedObjects(), false));

                return this.onShortcutDelete(this.getExtTableModel().getSelectedObjects(), evt, false);
            }
            break;
        case KeyEvent.VK_F:
            if (evt.isControlDown() || evt.isMetaDown()) {
                ExtTable.this.eventSender.fireEvent(new ExtTableEvent<ArrayList<E>>(ExtTable.this, ExtTableEvent.Types.SHORTCUT_SEARCH, this.getExtTableModel().getSelectedObjects()));

                return this.onShortcutSearch(this.getExtTableModel().getSelectedObjects(), evt);
            }
            break;
        case KeyEvent.VK_UP:
            if (this.getSelectedRow() == 0 && !evt.isShiftDown()) {
                if (this.getCellEditor() != null) {
                    this.getCellEditor().stopCellEditing();
                }
                this.changeSelection(this.getRowCount() - 1, 0, false, false);
                return true;
            }
            break;
        case KeyEvent.VK_DOWN:
            if (this.getSelectedRow() == this.getRowCount() - 1 && !evt.isShiftDown()) {
                if (this.getCellEditor() != null) {
                    this.getCellEditor().stopCellEditing();
                }
                this.changeSelection(0, 0, false, false);
                return true;
            }
            break;
        case KeyEvent.VK_A:
            if (evt.isControlDown() || evt.isMetaDown()) {
                if (this.getCellEditor() != null) {
                    this.getCellEditor().stopCellEditing();
                }
                this.getSelectionModel().setSelectionInterval(0, this.getRowCount() - 1);
                return true;
            }
            break;
        case KeyEvent.VK_HOME:
            if (evt.isControlDown() || evt.isMetaDown() || evt.isShiftDown()) {
                if (this.getCellEditor() != null) {
                    this.getCellEditor().stopCellEditing();
                }
                if (this.getSelectedRow() != -1 && this.getRowCount() != 0) {
                    this.getSelectionModel().setSelectionInterval(0, this.getSelectedRows()[this.getSelectedRows().length - 1]);
                    /* to avoid selection by super.processKeyBinding */
                    return true;
                }
            } else {
                if (this.getCellEditor() != null) {
                    this.getCellEditor().stopCellEditing();
                }
                this.getSelectionModel().setSelectionInterval(0, 0);
            }
            break;
        case KeyEvent.VK_END:
            if (evt.isControlDown() || evt.isMetaDown() || evt.isShiftDown()) {
                if (this.getCellEditor() != null) {
                    this.getCellEditor().stopCellEditing();
                }
                if (this.getSelectedRow() != -1 && this.getRowCount() != 0) {
                    this.getSelectionModel().setSelectionInterval(this.getSelectedRow(), this.getRowCount() - 1);
                    /* to avoid selection by super.processKeyBinding */
                    return true;
                }
            } else {
                if (this.getCellEditor() != null) {
                    this.getCellEditor().stopCellEditing();
                }
                if (this.getRowCount() != 0) {
                    this.getSelectionModel().setSelectionInterval(this.getRowCount() - 1, this.getRowCount() - 1);
                }
            }
            break;
        }
        return super.processKeyBinding(stroke, evt, condition, pressed);
    }

    @SuppressWarnings("unchecked")
    @Override
    protected void processMouseEvent(final MouseEvent e) {

        super.processMouseEvent(e);

        if (e.getID() == MouseEvent.MOUSE_RELEASED) {
            if (e.isPopupTrigger() || e.getButton() == MouseEvent.BUTTON3) {
                final int row = this.rowAtPoint(e.getPoint());
                final E obj = this.getExtTableModel().getObjectbyRow(row);
                final ExtColumn<E> col = this.getExtColumnAtPoint(e.getPoint());
                // System.out.println(row);
                if (obj == null || row == -1) {
                    /* no object under mouse, lets clear the selection */
                    this.clearSelection();
                    final JPopupMenu popup = this.onContextMenu(new JPopupMenu(), null, null, col);
                    this.eventSender.fireEvent(new ExtTableEvent<JPopupMenu>(this, ExtTableEvent.Types.CONTEXTMENU, popup));
                    if (popup != null && popup.getComponentCount() > 0) {
                        popup.show(ExtTable.this, e.getPoint().x, e.getPoint().y);
                    }
                    return;
                } else {
                    /* check if we need to select object */
                    if (!this.isRowSelected(row)) {
                        this.clearSelection();
                        this.addRowSelectionInterval(row, row);
                    }
                    final ArrayList<E> selected = this.getExtTableModel().getSelectedObjects();
                    final JPopupMenu popup = this.onContextMenu(new JPopupMenu(), obj, selected, col);

                    if (popup != null && popup.getComponentCount() > 0) {
                        popup.show(ExtTable.this, e.getPoint().x, e.getPoint().y);
                    }
                }

            } else if (e.getButton() == MouseEvent.BUTTON1 && e.getClickCount() == 2) {
                final int row = this.rowAtPoint(e.getPoint());
                final E obj = this.getExtTableModel().getObjectbyRow(row);
                // System.out.println(row);
                if (obj != null) {
                    this.onDoubleClick(e, obj);
                    this.eventSender.fireEvent(new ExtTableEvent<E>(this, ExtTableEvent.Types.DOUBLECLICK, obj));
                }
            } else if (e.getButton() == MouseEvent.BUTTON1 && e.getClickCount() == 1) {
                final int row = this.rowAtPoint(e.getPoint());
                final E obj = this.getExtTableModel().getObjectbyRow(row);
                // System.out.println(row);
                if (obj != null) {
                    this.onSingleClick(e, obj);
                }
            }
        } else if (e.getID() == MouseEvent.MOUSE_PRESSED) {
            if (this.rowAtPoint(e.getPoint()) < 0) {
                this.clearSelection();

            }
        }
    }

    protected void reconfigureColumnButton() {
        final Container c1 = this.getParent();
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
    public void removeRowHighlighter(final ExtRowHighlighter highlighter) {
        this.rowHighlighters.remove(highlighter);
    }

    public void scrollToRow(final int row) {
        System.out.println("Scrol " + row);
        if (row < 0) { return; }
        new EDTHelper<Object>() {

            @Override
            public Object edtRun() {
                final JViewport viewport = (JViewport) ExtTable.this.getParent();
                if (viewport == null) { return null; }
                final Rectangle rect = ExtTable.this.getCellRect(row, 0, true);
                final Point pt = viewport.getViewPosition();
                rect.setLocation(rect.x - pt.x, rect.y - pt.y);

                viewport.scrollRectToVisible(rect);
                System.out.println("Scrol " + rect);
                return null;
            }

        }.start();

    }

    protected void scrollToSelection() {

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
                final Point pt = viewport.getViewPosition();
                rect.setLocation(rect.x - pt.x, rect.y - pt.y);
                viewport.scrollRectToVisible(rect);
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

    /**
     * @param searchEnabled
     *            the searchEnabled to set
     */
    public void setSearchEnabled(final boolean searchEnabled) {
        this.searchEnabled = searchEnabled;
    }

    /**
     * 
     */
    private synchronized void startSearch() {
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
                            final E found = ExtTable.this.getExtTableModel().searchNextObject(startRow + 1, ret, ExtTable.this.searchDialog.isCaseSensitive(), ExtTable.this.searchDialog.isRegex());
                            ExtTable.this.getExtTableModel().setSelectedObject(found);
                            ExtTable.this.scrollToSelection();
                        }
                    }

                };
            }
        } catch (final IOException e) {
            Log.exception(e);
        }
    }

}
