package org.appwork.swing.exttable.columns;

import java.awt.Color;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.util.regex.Pattern;

import javax.swing.BorderFactory;
import javax.swing.Icon;
import javax.swing.JComponent;
import javax.swing.JTextField;
import javax.swing.border.Border;

import org.appwork.app.gui.MigPanel;
import org.appwork.swing.exttable.ExtColumn;
import org.appwork.swing.exttable.ExtDefaultRowSorter;
import org.appwork.swing.exttable.ExtTableModel;
import org.appwork.utils.swing.renderer.RenderLabel;
import org.appwork.utils.swing.renderer.RendererMigPanel;

public abstract class ExtTextColumn<E> extends ExtColumn<E> implements ActionListener, FocusListener {

    private static final long serialVersionUID = 2114805529462086691L;
    protected RenderLabel     rendererField;
    protected JTextField      editorField;
    private final Border      defaultBorder    = BorderFactory.createEmptyBorder(0, 5, 0, 5);
    private Color             rendererForeground;
    private Color             editorForeground;
    private Font              rendererFont;
    private Font              editorFont;
    protected MigPanel        editor;
    private RenderLabel       rendererIcon;
    protected MigPanel        renderer;
    private RenderLabel       editorIconLabel;

    /**
     * @param string
     */
    public ExtTextColumn(final String name) {
        this(name, null);

    }

    public ExtTextColumn(final String name, final ExtTableModel<E> table) {
        super(name, table);
        this.editorField = new JTextField();
        this.editorField.addFocusListener(this);
        this.editorField.setBorder(null);
        this.rendererIcon = new RenderLabel() {
            private static final long serialVersionUID = 1L;

            @Override
            public void setIcon(final Icon icon) {

                this.setVisible(icon != null);
                super.setIcon(icon);
            }
        };

        this.rendererIcon.setOpaque(false);
        this.editorIconLabel = new RenderLabel() {
            /**
             * 
             */
            private static final long serialVersionUID = 1L;

            @Override
            public void setIcon(final Icon icon) {

                this.setVisible(icon != null);
                super.setIcon(icon);
            }
        };
        this.editorIconLabel.setOpaque(false);
        this.rendererField = new RenderLabel() {

            /**
             * 
             */
            private static final long serialVersionUID = 1L;

            @Override
            public void setIcon(final Icon icon) {

                ExtTextColumn.this.rendererIcon.setIcon(icon);
            }

        };
        this.editorField.setOpaque(false);
        this.editorField.putClientProperty("Synthetica.opaque", Boolean.FALSE);
        this.rendererForeground = this.rendererField.getForeground();
        this.editorForeground = this.editorField.getForeground();
        this.rendererFont = this.rendererField.getFont();
        this.editorFont = this.editorField.getFont();
        this.editor = new RendererMigPanel("ins 0", "[]5[grow,fill]", "[grow,fill]");

        this.renderer = new RendererMigPanel("ins 0", "[]0[grow,fill]", "[grow,fill]");

        this.editor.add(this.editorIconLabel, "hidemode 2");
        this.editor.add(this.editorField);
        this.renderer.add(this.rendererIcon, "hidemode 2");
        this.renderer.add(this.rendererField);
        this.setRowSorter(new ExtDefaultRowSorter<E>() {

            @Override
            public int compare(final E o1, final E o2) {
                String o1s = ExtTextColumn.this.getStringValue(o1);
                String o2s = ExtTextColumn.this.getStringValue(o2);
                if (o1s == null) {
                    o1s = "";
                }
                if (o2s == null) {
                    o2s = "";
                }
                if (this.getSortOrderIdentifier() != ExtColumn.SORT_ASC) {
                    return o1s.compareTo(o2s);
                } else {
                    return o2s.compareTo(o1s);
                }

            }

        });

    }

    public void actionPerformed(final ActionEvent e) {
        this.editorField.removeActionListener(this);
        this.fireEditingStopped();
    }

    @Override
    public void configureEditorComponent(final E value, final boolean isSelected, final int row, final int column) {
        this.editorField.removeActionListener(this);

        String str = this.getStringValue(value);
        if (str == null) {
            // under substance, setting setText(null) somehow sets the label
            // opaque.
            str = "";
        }
        this.editorField.setText(str);
        this.editorIconLabel.setIcon(this.getIcon(value));
        this.editorField.addActionListener(this);

    }

    @Override
    public void configureRendererComponent(final E value, final boolean isSelected, final boolean hasFocus, final int row, final int column) {

        String str = this.getStringValue(value);
        if (str == null) {
            // under substance, setting setText(null) somehow sets the label
            // opaque.
            str = "";
        }

        this.rendererField.setText(str);
        this.rendererIcon.setIcon(this.getIcon(value));

    }

    @Override
    public void focusGained(final FocusEvent e) {
        this.editorField.selectAll();
    }

    @Override
    public void focusLost(final FocusEvent e) {
        // TODO Auto-generated method stub

    }

    @Override
    public Object getCellEditorValue() {
        return this.editorField.getText();
    }

    /**
     * @return
     */
    @Override
    public JComponent getEditorComponent(final E value, final boolean isSelected, final int row, final int column) {
        return this.editor;
    }

    /*
     * @param value
     * 
     * @return
     */
    protected Icon getIcon(final E value) {
        return null;
    }

    /**
     * @return
     */
    @Override
    public JComponent getRendererComponent(final E value, final boolean isSelected, final boolean hasFocus, final int row, final int column) {
        return this.renderer;
    }

    public abstract String getStringValue(E value);

    @Override
    protected String getTooltipText(final E obj) {
        return this.getStringValue(obj);

    }

    @Override
    public boolean isEditable(final E obj) {
        return false;
    }

    @Override
    public boolean isEnabled(final E obj) {
        return true;
    }

    @Override
    public boolean isSortable(final E obj) {
        return true;
    }

    @Override
    public boolean matchSearch(final E object, final Pattern pattern) {
        return pattern.matcher(this.getStringValue(object)).matches();
    }

    @Override
    public void resetEditor() {
        this.editorField.setFont(this.editorFont);
        this.editorField.setForeground(this.editorForeground);
        this.editorField.setOpaque(false);
        this.editorField.setBackground(null);
        this.editor.setOpaque(false);
        this.editorIconLabel.setIcon(null);
    }

    @Override
    public void resetRenderer() {
        this.rendererField.setBorder(this.defaultBorder);
        this.rendererField.setOpaque(false);
        this.rendererField.setBackground(null);
        this.rendererField.setFont(this.rendererFont);
        this.rendererField.setForeground(this.rendererForeground);
        this.renderer.setOpaque(false);
        this.rendererIcon.setIcon(null);

    }

    // /**
    // * @param value
    // */
    // protected void prepareLabel(final E value) {
    // }

    // /**
    // * @param label2
    // */
    // protected void prepareLabelForHelpText(final JLabel label) {
    //
    // label.setForeground(Color.lightGray);
    //
    // }

    // /**
    // * Should be overwritten to prepare the component for the TableCellEditor
    // * (e.g. setting tooltips)
    // */
    // protected void prepareTableCellEditorComponent(final JTextField text) {
    // }

    // protected void prepareTextfieldForHelpText(final JTextField tf) {
    //
    // tf.setForeground(Color.lightGray);
    //
    // }

    /**
     * Override to save value after editing
     * 
     * @param value
     * @param object
     */
    protected void setStringValue(final String value, final E object) {

    }

    @Override
    public void setValue(final Object value, final E object) {
        this.setStringValue((String) value, object);
    }

}
