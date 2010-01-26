package org.appwork.utils.swing.table;

import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.LinkedHashMap;

import javax.swing.JCheckBoxMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.JTable;
import javax.swing.KeyStroke;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.TableColumnModelEvent;
import javax.swing.event.TableColumnModelListener;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;

import org.appwork.utils.logging.Log;
import org.appwork.utils.storage.DatabaseInterface;

/**
 * The ExtTable package is a tableframework that follows two main tasks:<br>
 * 1. Easy creating of tables<br>
 * 2. Implement extended features like column selection, database connection,
 * editing, easy rendering, sorting etc.
 * 
 * @author $Author: unknown$
 * 
 */
public class ExtTable extends JTable {

    /**
     * 
     */
    private static final long serialVersionUID = 2822230056021924679L;
    /**
     * Column background color if column is NOT selected
     */
    private Color columnBackground;
    /**
     * columnbackgroundcolor of colum is selected
     */
    private Color columnBackgroundSelected;
    /**
     * column textcolor if colum is NOT selected
     */
    private Color columnForeground;
    /**
     * COlumn textcolor if column is selected
     */
    private Color columnForegroundSelected;

    /**
     * Database interface used to store crossession data.
     */
    private DatabaseInterface database;
    /**
     * The underlaying datamodel
     */
    private ExtTableModel model;
    /**
     * TableID. Used to generate a key for saving internal data to database
     */
    private String tableID;

    final private ArrayList<ExtRowHighlighter> rowHighlighters;

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
    public ExtTable(ExtTableModel model, DatabaseInterface database, String id) {
        super(model);
        this.tableID = id;
        rowHighlighters = new ArrayList<ExtRowHighlighter>();
        this.model = model;
        this.database = database;
        model.setTable(this);
        // get defaultbackground and Foregroundcolors
        Component c = super.getCellRenderer(0, 0).getTableCellRendererComponent(this, "", true, false, 0, 0);
        columnBackgroundSelected = c.getBackground();
        columnForegroundSelected = c.getForeground();

        c = super.getCellRenderer(0, 0).getTableCellRendererComponent(this, "", false, false, 0, 0);
        columnBackground = c.getBackground();
        columnForeground = c.getForeground();
        createColumns();

        // Mouselistener for columnselection Menu and sort on click
        getTableHeader().addMouseListener(new MouseAdapter() {
            int columnPressed = 0;

            @Override
            public void mouseReleased(MouseEvent e) {
                if (getTableHeader().getCursor().getType() == Cursor.getDefaultCursor().getType()) {
                    if (e.getButton() == MouseEvent.BUTTON1) {
                        if (columnPressed != columnAtPoint(e.getPoint())) return;
                        int col = getExtColumnIndexByPoint(e.getPoint());
                        if (getExtTableModel().getExtColumn(col).isSortable(null)) getExtTableModel().getExtColumn(col).doSort(null);
                    }
                }
            }

            @Override
            public void mousePressed(MouseEvent e) {
                // only if we are not in resize mode
                if (getTableHeader().getCursor().getType() == Cursor.getDefaultCursor().getType()) {
                    if (e.getButton() == MouseEvent.BUTTON1) {
                        columnPressed = columnAtPoint(e.getPoint());
                    } else if (e.getButton() == MouseEvent.BUTTON3) {
                        columControlMenu().show(getTableHeader(), e.getX(), e.getY());
                    }
                }
            }

        });
        // mouselistener to display column header tooltips
        getTableHeader().addMouseMotionListener(new MouseAdapter() {
            @Override
            public void mouseMoved(MouseEvent e) {
                int col = getExtColumnIndexByPoint(e.getPoint());
                getTableHeader().setToolTipText(getExtTableModel().getExtColumn(col).getName());
            }
        });

        getTableHeader().setReorderingAllowed(true);
        getTableHeader().setResizingAllowed(true);
        setAutoResizeMode(JTable.AUTO_RESIZE_SUBSEQUENT_COLUMNS);
        setAutoscrolls(true);
        getTableHeader().setPreferredSize(new Dimension(getColumnModel().getTotalColumnWidth(), 19));
        // assures that the table is painted over the complete available high
        // This method is 1.6 only
        this.setFillsViewportHeight(true);

        getColumnModel().addColumnModelListener(new TableColumnModelListener() {

            public void columnAdded(TableColumnModelEvent e) {
            }

            public void columnMarginChanged(ChangeEvent e) {
            }

            public void columnMoved(TableColumnModelEvent e) {
                if (e == null) return;
                if (e.getFromIndex() == e.getToIndex()) return;
                TableColumnModel tcm = getColumnModel();
                for (int i = 0; i < tcm.getColumnCount(); i++) {
                    try {
                        ExtTable.this.database.put("EXTTABLE_" + tableID + "_POS_COL_" + i, getExtTableModel().getExtColumn(tcm.getColumn(i).getModelIndex()).getID());
                    } catch (Exception e1) {
                        Log.exception(e1);
                    }
                }

            }

            public void columnRemoved(TableColumnModelEvent e) {
            }

            public void columnSelectionChanged(ListSelectionEvent e) {
            }

        });

    }

    // Key selection
    protected boolean processKeyBinding(KeyStroke stroke, KeyEvent evt, int condition, boolean pressed) {
        if (!pressed) { return super.processKeyBinding(stroke, evt, condition, pressed); }
        switch (evt.getKeyCode()) {

        case KeyEvent.VK_UP:
            if (getSelectedRow() == 0) {
                if (getCellEditor() != null) {
                    getCellEditor().stopCellEditing();
                }
                changeSelection(getRowCount() - 1, 0, false, false);
                return true;
            }
            break;
        case KeyEvent.VK_DOWN:
            if (getSelectedRow() == (getRowCount() - 1)) {
                if (getCellEditor() != null) {
                    getCellEditor().stopCellEditing();
                }
                changeSelection(0, 0, false, false);
                return true;
            }
        case KeyEvent.VK_A:
            if (evt.isControlDown()) {
                if (getCellEditor() != null) {
                    getCellEditor().stopCellEditing();
                }
                this.getSelectionModel().setSelectionInterval(1, getRowCount() - 1);
                return true;
            }

        }
        return super.processKeyBinding(stroke, evt, condition, pressed);
    }

    /**
     * adds a row highlighter
     * 
     * @param highlighter
     */
    public void addRowHighlighter(ExtRowHighlighter highlighter) {
        removeRowHighlighter(highlighter);
        this.rowHighlighters.add(highlighter);

    }

    /**
     * Removes a rowhilighter
     * 
     * @param highlighter
     */
    public void removeRowHighlighter(ExtRowHighlighter highlighter) {
        this.rowHighlighters.remove(highlighter);
    }

    public void paintComponent(final Graphics g) {

        super.paintComponent(g);
        // highlighter
        // TODO: this might get slow for many rows
        if (this.getRowCount() == 0) return;
        final Rectangle visibleRect = this.getVisibleRect();
        Rectangle first, last;
        // get current width;
        first = this.getCellRect(0, 0, true);
        last = this.getCellRect(0, this.getColumnCount() - 1, true);
        final int width = last.x + last.width - first.x;
        for (ExtRowHighlighter rh : this.rowHighlighters) {

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

                    rh.paint((Graphics2D) g, 0, first.y, width, first.height);

                }

            }

        }
    }

    /**
     * create Columnselection popupmenu. It contains all available columns and
     * let's the user select. The menu does not autoclose on click.
     * 
     * @return
     */
    private JPopupMenu columControlMenu() {
        JPopupMenu popup = new JPopupMenu();
        JCheckBoxMenuItem[] mis = new JCheckBoxMenuItem[getExtTableModel().getColumnCount()];

        for (int i = 0; i < getExtTableModel().getColumnCount(); ++i) {
            final int j = i;
            final ExtCheckBoxMenuItem mi = new ExtCheckBoxMenuItem(getExtTableModel().getColumnName(i));
            mi.setHideOnClick(false);
            mis[i] = mi;
            if (i == 0) mi.setEnabled(false);
            mi.setSelected(getExtTableModel().isVisible(i));
            mi.addActionListener(new ActionListener() {

                public void actionPerformed(ActionEvent e) {
                    getExtTableModel().setVisible(j, mi.isSelected());
                    createColumns();
                    revalidate();
                    repaint();
                }

            });
            popup.add(mi);
        }
        return popup;
    }

    /**
     * Creates the columns based on the model
     */
    private void createColumns() {
        setAutoCreateColumnsFromModel(false);
        TableColumnModel tcm = getColumnModel();
        while (tcm.getColumnCount() > 0) {
            tcm.removeColumn(tcm.getColumn(0));
        }
        LinkedHashMap<String, TableColumn> columns = new LinkedHashMap<String, TableColumn>();
        for (int i = 0; i < getModel().getColumnCount(); ++i) {
            final int j = i;

            TableColumn tableColumn = new TableColumn(i);

            tableColumn.setHeaderRenderer(model.getExtColumn(j).getHeaderRenderer());
            // Save column width
            tableColumn.addPropertyChangeListener(new PropertyChangeListener() {
                public void propertyChange(PropertyChangeEvent evt) {
                    if (evt.getPropertyName().equals("width")) {
                        // TODO
                        try {
                            ExtTable.this.database.put("EXTTABLE_" + tableID + "_WIDTH_COL_" + model.getExtColumn(j).getID(), evt.getNewValue());
                        } catch (Exception e) {
                            Log.exception(e);
                        }

                    }
                }
            });
            if (model.getExtColumn(j).getMaxWidth() >= 0) tableColumn.setMaxWidth(model.getExtColumn(j).getMaxWidth());
            // Set stored columnwidth
            try {
                int w = ExtTable.this.database.get("EXTTABLE_" + tableID + "_WIDTH_COL_" + model.getExtColumn(j).getID(), tableColumn.getWidth());
                tableColumn.setPreferredWidth(w);

                if (!model.isVisible(i)) {
                    continue;
                }

            } catch (Exception e) {
                Log.exception(e);
            }
            columns.put(model.getExtColumn(j).getID(), tableColumn);
            // addColumn(tableColumn);
        }
        // restore column position
        int index = 0;
        while (true) {
            if (columns.isEmpty()) break;
            if (index < getModel().getColumnCount()) {
                String id;
                try {
                    id = this.database.get("EXTTABLE_" + tableID + "_POS_COL_" + index, "");

                    index++;
                    if (id != null) {
                        TableColumn item = columns.remove(id);

                        if (item != null) {
                            System.out.println(item);
                            addColumn(item);
                        }
                    }

                } catch (Exception e) {
                    Log.exception(e);
                }
            } else {
                for (TableColumn ritem : columns.values()) {
                    addColumn(ritem);
                }
                break;
            }
        }
    }

    /**
     * converts the colum index to model and returns the column's cell editor
     */
    @Override
    public TableCellEditor getCellEditor(int row, int column) {
        return model.getCelleditorByColumn(convertColumnIndexToModel(column));

    }

    /**
     * COnverts ther colum index to the current model and returns the column's
     * cellrenderer
     */
    @Override
    public TableCellRenderer getCellRenderer(int row, int column) {

        return model.getCellrendererByColumn(convertColumnIndexToModel(column));
    }

    /**
     * @return the {@link ExtTable#columnBackground}
     * @see ExtTable#columnBackground
     */
    public Color getColumnBackground() {
        return columnBackground;
    }

    /**
     * @return the {@link ExtTable#columnBackgroundSelected}
     * @see ExtTable#columnBackgroundSelected
     */
    public Color getColumnBackgroundSelected() {
        return columnBackgroundSelected;
    }

    /**
     * @return the {@link ExtTable#columnForeground}
     * @see ExtTable#columnForeground
     */
    public Color getColumnForeground() {
        return columnForeground;
    }

    /**
     * @return the {@link ExtTable#columnForegroundSelected}
     * @see ExtTable#columnForegroundSelected
     */
    public Color getColumnForegroundSelected() {
        return columnForegroundSelected;
    }

    public ExtTableModel getExtTableModel() {
        return model;
    }

    /**
     * Returns the original Celleditor given by the current LAF UI. Used to have
     * an reference to the LAF's default editor
     * 
     * @param row
     * @param column
     * @return
     */
    public TableCellEditor getLafCellEditor(int row, int column) {
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
    public TableCellRenderer getLafCellRenderer(int row, int column) {

        return super.getCellRenderer(row, column);
    }

    /**
     * returns the real column at the given point. Method converts the column to
     * the moduels colum
     * 
     * @param point
     * @return
     */
    public int getExtColumnIndexByPoint(Point point) {
        int x = columnAtPoint(point);
        return convertColumnIndexToModel(x);
    }

    /**
     * Returns the real column index at this point
     * 
     * @param point
     */
    public ExtColumn getExtColumnAtPoint(Point point) {

        int x = getExtColumnIndexByPoint(point);
        return ((ExtTableModel) getModel()).getExtColumn(x);

    }

    /*
     * (non-Javadoc)
     * 
     * @see java.awt.event.KeyListener#keyPressed(java.awt.event.KeyEvent)
     */
    // @Override
    // public void keyPressed(KeyEvent e) {
    // // TODO Auto-generated method stub
    // int leadSelectIndex, selectIndex;
    // switch (e.getKeyCode()) {
    // case KeyEvent.VK_UP:
    // // getSelectionModel().getMinSelectionIndex()
    // leadSelectIndex = Math.max(0, getSelectionModel().getLeadSelectionIndex()
    // - 1);
    // getSelectionModel().setLeadSelectionIndex(leadSelectIndex);
    // break;
    // case KeyEvent.VK_DOWN:
    // leadSelectIndex = Math.min(this.getRowCount() - 1,
    // getSelectionModel().getLeadSelectionIndex() + 1);
    // getSelectionModel().setLeadSelectionIndex(leadSelectIndex);
    // break;
    // case KeyEvent.VK_A:
    // if (e.isControlDown()) {
    //
    // }
    // break;
    // }
    // }

}
