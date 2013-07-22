package org.appwork.swing.exttable.columns;

import java.awt.Dimension;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;

import javax.swing.AbstractButton;
import javax.swing.ComboBoxModel;
import javax.swing.Icon;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;

import net.miginfocom.swing.MigLayout;

import org.appwork.resources.AWUTheme;
import org.appwork.swing.action.BasicAction;
import org.appwork.swing.exttable.ExtTableModel;
import org.appwork.utils.swing.renderer.RenderLabel;

import sun.swing.SwingUtilities2;

public abstract class ExtComboColumn<E, ModelType> extends ExtTextColumn<E> implements ActionListener {

    private static final long        serialVersionUID = 2114805529462086691L;
    private ComboBoxModel<ModelType> dataModel;

    public ExtComboColumn(final String name, final ComboBoxModel<ModelType> model) {
        this(name, null, model);

    }

    public ExtComboColumn(final String name, final ExtTableModel<E> table, final ComboBoxModel<ModelType> model) {
        super(name, table);
        renderer.removeAll();
        renderer.setLayout(new MigLayout("ins 0", "[grow,fill]0[12]5", "[grow,fill]"));

        rendererField = new RenderLabel() {

            /**
             * 
             */
            private static final long serialVersionUID = 1L;

            @Override
            public void setText(final String text) {
                if (text != null && text.equals(getText())) { return; }
                if (text == null && getText() == null) { return; }
                super.setText(text);
            }

        };

        renderer.add(rendererField);
        renderer.add(rendererIcon);
        this.dataModel = model;

    }

    @Override
    public void configureRendererComponent(final E value, final boolean isSelected, final boolean hasFocus, final int row, final int column) {
        // TODO Auto-generated method stub
        if (isEditable(value)) {
            rendererIcon.setIcon(this.getIcon(value));
        } else {
            rendererIcon.setIcon(null);
        }

        String str = this.getStringValue(value);
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
    protected boolean startEdit(final E value, final int row) {
        final JPopupMenu popup = new JPopupMenu();
        try {
            final ModelType selected = getSelectedItem(value);
            final ComboBoxModel<ModelType> dm = updateModel(dataModel,value);
            for (int i = 0; i < dm.getSize(); i++) {
                final ModelType o = dm.getElementAt(i);
                final AbstractButton bt = getPopupElement(o, selected == o);
                bt.addActionListener(new ActionListener() {

                    @Override
                    public void actionPerformed(final ActionEvent e) {
                        setValue(o, value);
                        popup.setVisible(false);

                    }
                });
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
    protected AbstractButton getPopupElement(final ModelType o, final boolean selected) {
        return new JMenuItem(new BasicAction(o.toString()) {
            {

                setName(modelItemToString(o));

                if (selected) {
                    setSmallIcon(AWUTheme.I().getIcon("enable", 16));
                }

            }

            @Override
            public void actionPerformed(final ActionEvent e) {

            }

        });
    }

    final protected Icon getIcon(final E value) {
        return AWUTheme.I().getIcon("popdownButton", -1);
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

        return selectedItem.toString();
    }

    @Override
    protected String getTooltipText(final E obj) {
        // TODO Auto-generated method stub
        return super.getTooltipText(obj);
    }

    @Override
    public boolean isEditable(final E obj) {
        return updateModel(dataModel,obj).getSize() > 1;
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
