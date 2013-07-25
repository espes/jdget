package org.appwork.swing.exttable.columns;

import java.awt.Color;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.util.regex.Pattern;

import javax.swing.BorderFactory;
import javax.swing.Icon;
import javax.swing.JComponent;
import javax.swing.JTextField;
import javax.swing.border.Border;

import org.appwork.swing.MigPanel;
import org.appwork.swing.exttable.ExtColumn;
import org.appwork.swing.exttable.ExtDefaultRowSorter;
import org.appwork.swing.exttable.ExtTableModel;
import org.appwork.utils.swing.SwingUtils;
import org.appwork.utils.swing.renderer.RenderLabel;
import org.appwork.utils.swing.renderer.RendererMigPanel;

import sun.swing.SwingUtilities2;

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

    protected RenderLabel     rendererIcon;

    protected MigPanel        renderer;
    protected RenderLabel     editorIconLabel;
    protected boolean         noset            = false;

    /**
     * @param string
     */
    public ExtTextColumn(final String name) {
        this(name, null);

    }

    public boolean onRenameClick(final MouseEvent e, final E obj) {
        if (isEditable(obj)) {
            startEditing(obj);
            System.out.println("Start");
            return true;
        } else {
            return super.onRenameClick(e, obj);
        }

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

                setVisible(icon != null);
                if (icon != this.getIcon()) {
                    super.setIcon(icon);
                }
            }
        };
        this.editorField.addKeyListener(new KeyListener() {

            public void keyPressed(final KeyEvent e) {
            }

            public void keyReleased(final KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ESCAPE) {
                    ExtTextColumn.this.noset = true;
                    try {
                        ExtTextColumn.this.stopCellEditing();
                    } finally {
                        ExtTextColumn.this.noset = false;
                    }
                }
            }

            public void keyTyped(final KeyEvent e) {
            }
        });

        this.editorIconLabel = new RenderLabel() {
            /**
             * 
             */
            private static final long serialVersionUID = 1L;

            @Override
            public void setIcon(final Icon icon) {

                setVisible(icon != null);
                super.setIcon(icon);
            }
        };

        this.rendererField = new RenderLabel() {

            /**
             * 
             */
            private static final long serialVersionUID = 1L;

            @Override
            public void setIcon(final Icon icon) {
                ExtTextColumn.this.rendererIcon.setIcon(icon);
            }

            @Override
            public void setText(final String text) {
                if (text != null && text.equals(getText())) { return; }
                if (text == null && getText() == null) { return; }
                super.setText(text);
            }

        };

        this.rendererForeground = this.rendererField.getForeground();
        this.editorForeground = this.editorField.getForeground();
        this.rendererFont = this.rendererField.getFont();
        this.editorFont = this.editorField.getFont();
        this.editor = new MigPanel("ins 0", "[]5[grow,fill]", "[grow,fill]");

        this.renderer = new RendererMigPanel("ins 0", "[]0[grow,fill]", "[grow,fill]");

        this.layoutEditor(this.editor, this.editorIconLabel, this.editorField);
        this.layoutRenderer(this.renderer, this.rendererIcon, this.rendererField);
        setRowSorter(new ExtDefaultRowSorter<E>() {

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
                if (getSortOrderIdentifier() != ExtColumn.SORT_ASC) {
                    return o1s.compareToIgnoreCase(o2s);
                } else {
                    return o2s.compareToIgnoreCase(o1s);
                }

            }

        });

    }

    public void actionPerformed(final ActionEvent e) {
        this.editorField.removeActionListener(this);
        fireEditingStopped();
    }

    @Override
    protected void configureCurrentlyEditingComponent(final E value, final boolean isSelected, final int row, final int column) {
        this.editorIconLabel.setIcon(this.getIcon(value));

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
        this.editorField.addActionListener(this);

        this.editorIconLabel.setIcon(this.getIcon(value));

    }

    @Override
    public void configureRendererComponent(final E value, final boolean isSelected, final boolean hasFocus, final int row, final int column) {

        this.rendererIcon.setIcon(this.getIcon(value));
        String str = this.getStringValue(value);
       if (str == null) {
            // under substance, setting setText(null) somehow sets the label
            // opaque.
            str = "";
        }

        if (getTableColumn() != null) {
            this.rendererField.setText(SwingUtilities2.clipStringIfNecessary(this.rendererField, this.rendererField.getFontMetrics(this.rendererField.getFont()), str, getTableColumn().getWidth() - this.rendererIcon.getPreferredSize().width - 5));
        } else {
            this.rendererField.setText(str);
        }

    }

    @Override
    public void focusGained(final FocusEvent e) {
        this.editorField.selectAll();
    }

    @Override
    public void focusLost(final FocusEvent e) {
        if (!e.isTemporary() || e.getOppositeComponent() == null) {
            /*
             * we check for temporary , because a rightclick menu will cause
             * focus lost but editing should not stop
             * 
             * we also check for oppositeComponent to stopEditing when we click
             * outside the window
             */
            ExtTextColumn.this.noset = true;
            try {
                ExtTextColumn.this.stopCellEditing();
            } finally {
                ExtTextColumn.this.noset = false;
            }
        }
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

    public JTextField getEditorField() {
        return this.editorField;
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

    /**
     * @param editor2
     * @param editorIconLabel2
     * @param editorField2
     */
    protected void layoutEditor(final MigPanel editor, final RenderLabel editorIconLabel, final JTextField editorField) {
        editor.add(editorIconLabel, "hidemode 2");
        editor.add(editorField);
    }

    /**
     * @param rendererField
     * @param rendererIco
     * @param renderer2
     */
    protected void layoutRenderer(final MigPanel renderer, final RenderLabel rendererIcon, final RenderLabel rendererField) {
        renderer.add(rendererIcon, "hidemode 2");
        renderer.add(rendererField);

    }

    @Override
    public boolean matchSearch(final E object, final Pattern pattern) {
        final String stringValue = this.getStringValue(object);
        if (stringValue == null) { return false; }
        return pattern.matcher(stringValue).matches();
    }

    @Override
    public void resetEditor() {
        this.editor.setEnabled(true);
        this.editorField.setFont(this.editorFont);
        this.editorField.setForeground(this.editorForeground);
        this.editorField.setBackground(null);
        this.editorIconLabel.setIcon(null);

        SwingUtils.setOpaque(editorIconLabel, false);
        SwingUtils.setOpaque(editorField, false);

    }

    @Override
    public void resetRenderer() {
        this.renderer.setEnabled(true);

        this.rendererField.setBorder(this.defaultBorder);
        this.rendererField.setOpaque(false);
        this.rendererField.setBackground(null);
        this.rendererField.setFont(this.rendererFont);
        this.rendererField.setForeground(this.rendererForeground);
        this.renderer.setOpaque(false);
        this.rendererIcon.setIcon(null);

        SwingUtils.setOpaque(rendererIcon, false);
        SwingUtils.setOpaque(rendererField, false);

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
        if (!this.noset) {
            this.setStringValue((String) value, object);
        }
    }

}
