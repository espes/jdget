package org.appwork.swing.exttable.columns;

import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;

import javax.swing.AbstractButton;
import javax.swing.ComboBoxModel;
import javax.swing.Icon;
import javax.swing.JComponent;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;

import org.appwork.resources.AWUTheme;
import org.appwork.swing.MigPanel;
import org.appwork.swing.action.BasicAction;
import org.appwork.swing.components.RadioBoxIcon;
import org.appwork.swing.exttable.ExtTableModel;
import org.appwork.utils.swing.renderer.RendererMigPanel;

import sun.swing.SwingUtilities2;

public abstract class ExtComboColumn<E, ModelType> extends ExtTextColumn<E> implements ActionListener {

    /**
     * @author Thomas
     * 
     */
    public static class RendererPanel extends RendererMigPanel {
        private boolean editable = false;
        private Icon    downIcon;         ;

        /**
         * @param downIcon
         */
        public RendererPanel() {
            super("ins 0", "[]0[grow,fill]", "[grow,fill]");

        }

        /**
         * @return
         */
        public Icon getIcon() {
            // TODO Auto-generated method stub
            return downIcon;
        }

        /*
         * (non-Javadoc)
         * 
         * @see javax.swing.JComponent#paintComponent(java.awt.Graphics)
         */
        @Override
        protected void paintComponent(final Graphics g) {
            // TODO Auto-generated method stub
            super.paintComponent(g);
            if (editable) {
                getIcon().paintIcon(this, g, getWidth() - 5 - getIcon().getIconWidth(), (getHeight() - getIcon().getIconHeight()) / 2);
            }
        }

        /**
         * @param editable
         */
        public void setEditable(final boolean editable) {
            this.editable = editable;

        }

        /**
         * @param object
         */
        public void setIcon(final Icon object) {
            downIcon = object;

        }
    }

    private static final long        serialVersionUID = 2114805529462086691L;
    private ComboBoxModel<ModelType> dataModel;

    protected RendererPanel          rendererPanel;
    private Icon                     iconDown;
    private Icon                     iconUp;

    public ExtComboColumn(final String name, final ComboBoxModel<ModelType> model) {
        this(name, null, model);

    }

    protected Icon createDropUpIcon() {
        return AWUTheme.I().getIcon("popUpLarge", -1);
    }

    protected Icon createDropDownIcon() {
        return AWUTheme.I().getIcon("popDownLarge", -1);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.appwork.swing.exttable.columns.ExtTextColumn#createEditorPanel()
     */
    @Override
    protected MigPanel createEditorPanel() {
        return new MigPanel("ins 0", "[]5[grow,fill]", "[grow,fill]") {
            /*
             * (non-Javadoc)
             * 
             * @see javax.swing.JComponent#paintComponent(java.awt.Graphics)
             */
            @Override
            protected void paintComponent(final Graphics g) {
                // TODO Auto-generated method stub
                super.paintComponent(g);
            }

            @Override
            public void requestFocus() {
                editorField.requestFocus();
            }

        };
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.appwork.swing.exttable.columns.ExtTextColumn#createRendererPanel()
     */
    @Override
    protected RendererMigPanel createRendererPanel() {
        iconDown = createDropDownIcon();
        iconUp = createDropUpIcon();
        return this.rendererPanel = new RendererPanel() {
            public Icon getIcon() {
                if (popup == null || !popup.isVisible()) { 
                    //
                    return iconDown; }
                return super.getIcon();
            }
        };
    }

    public ExtComboColumn(final String name, final ExtTableModel<E> table, final ComboBoxModel<ModelType> model) {
        super(name, table);
     
        this.dataModel = model;

    }

    @Override
    public void configureRendererComponent(final E value, final boolean isSelected, final boolean hasFocus, final int row, final int column) {
        // TODO Auto-generated method stub
        final ModelType selected = getSelectedItem(value);

        rendererPanel.setEditable(isEditable(value));
        rendererPanel.setIcon(editing == value ? iconUp : iconDown);
        rendererIcon.setIcon(this.modelItemToIcon(selected));

        String str = modelItemToString(selected);
        if (str == null) {
            // under substance, setting setText(null) somehow sets the label
            // opaque.
            str = "";
        }

        if (getTableColumn() != null) {
            rendererField.setText(SwingUtilities2.clipStringIfNecessary(rendererField, rendererField.getFontMetrics(rendererField.getFont()), str, getTableColumn().getWidth() - 18 - 5));
        } else {
            rendererField.setText(str);
        }

    }

    /**
     * @param selected
     * @return
     */
    protected Icon modelItemToIcon(final ModelType selected) {
        // TODO Auto-generated method stub
        return null;
    }

    public boolean onDoubleClick(final MouseEvent e, final E value) {
        if (!isEditable(value)) { return false; }

        final int row = getModel().getTable().rowAtPoint(new Point(e.getX(), e.getY()));
        // int column = getModel().getTable().columnAtPoint(new Point(e.getX(),
        // e.getY()));

        return startEdit(value, row);
    }

    @Override
    public boolean onRenameClick(final MouseEvent e, final E value) {
        if (!isEditable(value)) { return false; }

        final int row = getModel().getTable().rowAtPoint(new Point(e.getX(), e.getY()));
        // int column = getModel().getTable().columnAtPoint(new Point(e.getX(),
        // e.getY()));

        return startEdit(value, row);
    }

    public boolean onSingleClick(final MouseEvent e, final E value) {
        if (!isEditable(value)) { return false; }

        final int row = getModel().getTable().rowAtPoint(new Point(e.getX(), e.getY()));
        // int column = getModel().getTable().columnAtPoint(new Point(e.getX(),
        // e.getY()));

        return startEdit(value, row);
    }

    /**
     * @param value
     * @param row
     * @return
     */
    private long       lastHide = 0;
    private E          editing  = null;
    private JPopupMenu popup;

    protected boolean startEdit(final E value, final int row) {

        final long timeSinceLastHide = System.currentTimeMillis() - lastHide;
        if (timeSinceLastHide < 250 && editing == value) {
            //

            editing = null;
            repaint();
            return true;

        }
        editing = value;
        popup = new JPopupMenu() {
            public void setVisible(final boolean b) {

                super.setVisible(b);
                if (!b) {
                    lastHide = System.currentTimeMillis();
                    // editing = null;
                    // updateIcon(true);
                } else {
                    // updateIcon(false);
                }

            };

        };

        try {
            final ModelType selected = getSelectedItem(value);
            final ComboBoxModel<ModelType> dm = updateModel(dataModel, value);
            for (int i = 0; i < dm.getSize(); i++) {
                final ModelType o = dm.getElementAt(i);
                final JComponent bt = getPopupElement(o, selected.equals(o));
                if (bt instanceof AbstractButton) {
                    ((AbstractButton) bt).addActionListener(new ActionListener() {

                        @Override
                        public void actionPerformed(final ActionEvent e) {
                            setValue(o, value);
                            popup.setVisible(false);

                        }
                    });
                }
                popup.add(bt);
            }

            final Rectangle bounds = getModel().getTable().getCellRect(row, getIndex(), true);
            final Dimension pref = popup.getPreferredSize();
            popup.setPreferredSize(new Dimension(Math.max(pref.width, bounds.width), pref.height));
            popup.show(getModel().getTable(), bounds.x, bounds.y + bounds.height);
            return true;
        } catch (final Exception e1) {
            e1.printStackTrace();
        }
        return false;
    }

    /**
     * @param b
     * @param o2
     * @return
     */
    protected JComponent getPopupElement(final ModelType o, final boolean selected) {
        return new JMenuItem(new BasicAction(o.toString()) {
            {

                setName(modelItemToString(o));

                if (selected) {
                    setSmallIcon(RadioBoxIcon.TRUE);
                } else {
                    setSmallIcon(RadioBoxIcon.FALSE);
                }

            }

            @Override
            public void actionPerformed(final ActionEvent e) {

            }

        });
    }

    final protected Icon getIcon(final E value) {
        return modelItemToIcon(getSelectedItem(value));
    }

    @Override
    public String getStringValue(final E value) {

        return modelItemToString(getSelectedItem(value));
    }

    /**
     * @param value
     * @param comboBoxModel
     * @return
     */
    protected abstract ModelType getSelectedItem(final E object);

    protected abstract void setSelectedItem(final E object, final ModelType value);

    /**
     * @param selectedItem
     * @return
     */
    protected String modelItemToString(final ModelType selectedItem) {
        if (selectedItem == null) { return null; }
        return selectedItem.toString();
    }

    @Override
    protected String getTooltipText(final E obj) {
        // TODO Auto-generated method stub
        return super.getTooltipText(obj);
    }

    @Override
    public boolean isEditable(final E obj) {
        final ComboBoxModel<ModelType> mod = updateModel(dataModel, obj);
        return mod != null && mod.getSize() > 1;
    }

    @Override
    public boolean isEnabled(final E obj) {

        return true;
    }

    public boolean isCellEditable(final int rowIndex, final int columnIndex) {
        return false;
    }

    @Override
    public boolean isSortable(final E obj) {
        return true;
    }

    @Override
    final public void setValue(final Object value, final E object) {
        try {
            setSelectedItem(object, (ModelType) value);
        } catch (final Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * overwrite this method to implement different dropdown boxes
     * 
     * @param dataModel
     */
    public ComboBoxModel<ModelType> updateModel(final ComboBoxModel<ModelType> dataModel, final E value) {
        return dataModel;

    }

}
