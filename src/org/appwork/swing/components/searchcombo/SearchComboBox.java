package org.appwork.swing.components.searchcombo;

import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Graphics;
import java.awt.Rectangle;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

import javax.swing.ComboBoxEditor;
import javax.swing.ComboBoxModel;
import javax.swing.DefaultComboBoxModel;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.ListCellRenderer;
import javax.swing.UIManager;
import javax.swing.event.CaretEvent;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;
import javax.swing.plaf.basic.BasicComboBoxUI;
import javax.swing.plaf.basic.ComboPopup;

import org.appwork.app.gui.MigPanel;
import org.appwork.scheduler.DelayedRunnable;
import org.appwork.swing.components.BadgePainter;
import org.appwork.swing.components.Badgeable;
import org.appwork.utils.logging.Log;
import org.appwork.utils.swing.EDTRunner;
import org.appwork.utils.swing.SwingUtils;

/**
 * this component extends a normal combobox and implements a editable
 * filter/autocompletion feature. <b> make sure that you model is sorted</b>
 * 
 * @author thomas
 * 
 * @param <T>
 */
public abstract class SearchComboBox<T> extends JComboBox implements Badgeable {

    class Editor implements ComboBoxEditor, FocusListener, DocumentListener {
        private final JTextField      tf;
        private final MigPanel        panel;

        private final JLabel          icon;
        private T                     value;
        private final DelayedRunnable sheduler;
        private final Color           defaultForeground;
        private boolean               setting;

        public Editor(final ScheduledExecutorService scheduler) {
            this.tf = new JTextField() {
                @Override
                public void setForeground(Color fg) {
                    if (SearchComboBox.this.helptext != null) {
                        if (SearchComboBox.this.helptext.equals(this.getText())) {
                            fg = SearchComboBox.this.helpColor;
                        }
                    }
                    super.setForeground(fg);

                }

                @Override
                public void setText(String t) {
                    if (!this.hasFocus() && SearchComboBox.this.helptext != null && (t == null || t.length() == 0)) {
                        t = SearchComboBox.this.helptext;
                    }

                    super.setText(t);
                    if (SearchComboBox.this.helptext != null) {
                        if (SearchComboBox.this.helptext.equals(t)) {
                            this.setForeground(SearchComboBox.this.helpColor);
                        } else {
                            // if (!Editor.this.autoComplete(false)) {
                            // this.setForeground(SearchComboBox.this.foregroundBad);
                            // } else {
                            // this.setForeground(Editor.this.defaultForeground);
                            // }
                            this.setForeground(Editor.this.defaultForeground);
                        }
                    }
                }
            };
            this.tf.getDocument().addDocumentListener(this);
            this.icon = new JLabel();
            // editor panel
            this.panel = new MigPanel("ins 0", "[][grow,fill]", "[grow,fill]") {

                @Override
                public void requestFocus() {
                    Editor.this.tf.requestFocus();
                }

            };

            this.tf.addFocusListener(this);
            this.panel.add(this.icon);
            this.panel.setOpaque(true);
            this.panel.setBackground(this.tf.getBackground());

            this.tf.setBackground(null);
            SwingUtils.setOpaque(this.tf, false);
            this.defaultForeground = this.tf.getForeground();
            this.panel.add(this.tf);
            SwingUtils.setOpaque(this.panel, false);
            this.sheduler = new DelayedRunnable(scheduler, 150l) {

                @Override
                public void delayedrun() {
                    Editor.this.autoComplete(true);
                }

            };
            // this.panel.setBorder(this.tf.getBorder());
            this.tf.setBorder(null);

        }

        /*
         * (non-Javadoc)
         * 
         * @see javax.swing.ComboBoxEditor#addActionListener(java.awt.event.
         * ActionListener)
         */
        @Override
        public void addActionListener(final ActionListener l) {
            this.tf.addActionListener(l);
        }

        private void auto() {
            if (this.setting) { return; }
            // scheduler executes at least 50 ms after this submit.
            this.sheduler.run();

        }

        /**
         * finds all possible matches of the entered text and sets the selected
         * object
         */
        protected boolean autoComplete(final boolean showPopup) {

            final String txt = Editor.this.tf.getText();
            if (this.value != null && SearchComboBox.this.getTextForValue(this.value).equals(txt)) { return true; }
            String text = null;
            final ArrayList<T> found = new ArrayList<T>();

            for (int i = 0; i < SearchComboBox.this.getModel().getSize(); i++) {
                text = SearchComboBox.this.getTextForValue((T) SearchComboBox.this.getModel().getElementAt(i));
                if (text != null && text.startsWith(txt)) {
                    found.add((T) SearchComboBox.this.getModel().getElementAt(i));

                }
            }

            new EDTRunner() {

                @Override
                protected void runInEDT() {
                    final int pos = Editor.this.tf.getCaretPosition();

                    if (found.size() == 0) {
                        Editor.this.tf.setForeground(SearchComboBox.this.getForegroundBad());
                        SearchComboBox.this.hidePopup();
                        // javax.swing.plaf.synth.SynthComboPopup
                    } else {
                        Editor.this.tf.setForeground(Editor.this.defaultForeground);
                        // Editor.this.setItem(found.get(0));
                        SearchComboBox.this.setSelectedItem(found.get(0));
                        Editor.this.setItem(found.get(0));
                        Editor.this.tf.setCaretPosition(pos);
                        Editor.this.tf.select(txt.length(), Editor.this.tf.getText().length());
                        // Show popup, and scroll to correct position

                        if (found.size() > 1 && showPopup) {
                            // limit popup rows
                            SearchComboBox.this.setMaximumRowCount(found.size());
                            SearchComboBox.this.setPopupVisible(true);

                            // Scroll popup list, so that found[0] is the first
                            // entry. This is a bit "dirty", so we put it in a
                            // try catch...just to avoid EDT Exceptions
                            try {
                                final Object popup = SearchComboBox.this.getUI().getAccessibleChild(SearchComboBox.this, 0);
                                if (popup instanceof Container) {
                                    final Component scrollPane = ((Container) popup).getComponent(0);
                                    if (popup instanceof ComboPopup) {
                                        final JList jlist = ((ComboPopup) popup).getList();
                                        if (scrollPane instanceof JScrollPane) {
                                            final Rectangle cellBounds = jlist.getCellBounds(SearchComboBox.this.getSelectedIndex(), SearchComboBox.this.getSelectedIndex() + found.size() - 1);
                                            if (cellBounds != null) {
                                                jlist.scrollRectToVisible(cellBounds);
                                            }

                                        }
                                    }
                                }
                            } catch (final Throwable e) {
                                Log.exception(e);
                            }
                        } else {
                            SearchComboBox.this.hidePopup();
                        }

                    }

                }
            };
            return found.size() > 0;
        }

        public void caretUpdate(final CaretEvent arg0) {

        }

        /*
         * (non-Javadoc)
         * 
         * @see
         * javax.swing.event.DocumentListener#changedUpdate(javax.swing.event
         * .DocumentEvent)
         */
        @Override
        public void changedUpdate(final DocumentEvent e) {
            this.auto();
            SearchComboBox.this.onChanged();
        }

        public void focusGained(final FocusEvent arg0) {

            if (this.tf.getText().equals(SearchComboBox.this.helptext)) {
                this.tf.setText("");
                this.tf.setForeground(SearchComboBox.this.defaultColor);
            } else {
                Editor.this.tf.selectAll();
            }

        }

        public void focusLost(final FocusEvent arg0) {

            if (this.tf.getDocument().getLength() == 0 || this.tf.getText().equals(SearchComboBox.this.helptext)) {
                this.tf.setText(SearchComboBox.this.helptext);
                this.tf.setForeground(SearchComboBox.this.helpColor);
            } else if (!SearchComboBox.this.isUnkownTextInputAllowed() && !Editor.this.autoComplete(false)) {
                // reset text after modifications to a valid value
                try {
                    Editor.this.tf.setText(SearchComboBox.this.helptext != null ? SearchComboBox.this.helptext : SearchComboBox.this.getTextForValue(Editor.this.value));
                } catch (final NullPointerException e2) {
                    Editor.this.tf.setText("");
                }
                Editor.this.autoComplete(false);
            }

        }

        /*
         * (non-Javadoc)
         * 
         * @see javax.swing.ComboBoxEditor#getEditorComponent()
         */
        @Override
        public Component getEditorComponent() {
            // TODO Auto-generated method stub

            return this.panel;
        }

        /*
         * (non-Javadoc)
         * 
         * @see javax.swing.ComboBoxEditor#getItem()
         */
        @Override
        public Object getItem() {
            // TODO Auto-generated method stub
            return this.value;
        }

        public JTextField getTf() {
            return this.tf;
        }

        /*
         * (non-Javadoc)
         * 
         * @see
         * javax.swing.event.DocumentListener#insertUpdate(javax.swing.event
         * .DocumentEvent)
         */
        @Override
        public void insertUpdate(final DocumentEvent e) {
            this.auto();
            SearchComboBox.this.onChanged();

        }

        /*
         * (non-Javadoc)
         * 
         * @see javax.swing.ComboBoxEditor#removeActionListener(java.awt.event.
         * ActionListener)
         */
        @Override
        public void removeActionListener(final ActionListener l) {
            this.tf.removeActionListener(l);
        }

        /*
         * (non-Javadoc)
         * 
         * @see
         * javax.swing.event.DocumentListener#removeUpdate(javax.swing.event
         * .DocumentEvent)
         */
        @Override
        public void removeUpdate(final DocumentEvent e) {
            // auto();
            SearchComboBox.this.onChanged();
        }

        /*
         * (non-Javadoc)
         * 
         * @see javax.swing.ComboBoxEditor#selectAll()
         */
        @Override
        public void selectAll() {
            this.tf.selectAll();
        }

        /*
         * (non-Javadoc)
         * 
         * @see javax.swing.ComboBoxEditor#setItem(java.lang.Object)
         */
        @SuppressWarnings("unchecked")
        @Override
        public void setItem(final Object anObject) {
            // if (this.value == anObject) { return; }
            this.setting = true;
            this.tf.setText(SearchComboBox.this.getTextForValue((T) anObject));
            this.icon.setIcon(SearchComboBox.this.getIconForValue((T) anObject));
            this.value = (T) anObject;
            this.setting = false;

        }

    }

    private Color     defaultColor;

    private Color     helpColor;
    {

        this.defaultColor = this.getForeground();
        this.helpColor = (Color) UIManager.get("TextField.disabledForeground");
        if (this.helpColor == null) {
            this.helpColor = Color.LIGHT_GRAY;
        }
    }

    private Color     foregroundBad          = Color.red;
    private String    helptext;

    private boolean   unkownTextInputAllowed = false;

    private ImageIcon badgeIcon;

    /**
     * @param plugins
     */
    public SearchComboBox() {
        this(null, Executors.newSingleThreadScheduledExecutor());
    }

    public SearchComboBox(final ArrayList<T> plugins, final ScheduledExecutorService scheduler) {
        super((ComboBoxModel) null);
        this.addFocusListener(new FocusListener() {

            @Override
            public void focusGained(final FocusEvent e) {
                SearchComboBox.this.getEditor().getEditorComponent().requestFocus();
            }

            @Override
            public void focusLost(final FocusEvent e) {
                // TODO Auto-generated method stub

            }
        });
        if (plugins != null) {
            this.setList(plugins);
        }

        this.setEditor(new Editor(scheduler));
        this.setEditable(true);

        // we extends the existing renderer. this avoids LAF incompatibilities
        final ListCellRenderer org = this.getRenderer();
        this.addPopupMenuListener(new PopupMenuListener() {

            @Override
            public void popupMenuCanceled(final PopupMenuEvent e) {
                // TODO Auto-generated method stub

            }

            @Override
            public void popupMenuWillBecomeInvisible(final PopupMenuEvent e) {
                SearchComboBox.this.setMaximumRowCount(8);

            }

            @Override
            public void popupMenuWillBecomeVisible(final PopupMenuEvent e) {

            }
        });
        this.setRenderer(new ListCellRenderer() {

            @SuppressWarnings("unchecked")
            public Component getListCellRendererComponent(final JList list, final Object value, final int index, final boolean isSelected, final boolean cellHasFocus) {
                try {
                    final JLabel ret = (JLabel) org.getListCellRendererComponent(list, SearchComboBox.this.getTextForValue((T) value), index, isSelected, cellHasFocus);

                    ret.setIcon(SearchComboBox.this.getIconForValue((T) value));

                    // ret.setOpaque(false);
                    return ret;
                } catch (final Throwable e) {
                    // org might not be a JLabel (depending on the LAF)
                    // fallback here

                    return org.getListCellRendererComponent(list, SearchComboBox.this.getTextForValue((T) value), index, isSelected, cellHasFocus);

                }
            }
        });
    }

    /**
     * @return
     */
    public String getEditorText() {

        return this.getTextField().getText();
    }

    public Color getForegroundBad() {
        return this.foregroundBad;
    }

    /**
     * @param value
     * @return
     */
    abstract protected Icon getIconForValue(T value);

    public String getText() {
        return this.getTextField().getText();
    }

    public JTextField getTextField() {
        return ((Editor) this.getEditor()).getTf();
    }

    /**
     * @param value
     * @return
     */
    abstract protected String getTextForValue(T value);

    public boolean isHelpTextVisible() {
        return this.helptext != null && this.helptext.equals(this.getText());
    }

    /**
     * if unknown values are allowed, the component will not try to find a valid
     * entry on fopcus lost
     * 
     * @return
     */
    public boolean isUnkownTextInputAllowed() {

        return this.unkownTextInputAllowed;
    }

    /**
     * 
     */
    protected void onChanged() {
        // TODO Auto-generated method stub

    }

    public void paintBadge(final BadgePainter migPanel, final Graphics g) {
        if (this.badgeIcon != null) {

            g.drawImage(this.badgeIcon.getImage(), (int) (this.getWidth() - this.badgeIcon.getIconWidth() / 1.5), (int) (this.getHeight() - this.badgeIcon.getIconHeight() / 1.5), null);

        }
    }

    @Override
    protected void paintComponent(final Graphics g) {

        super.paintComponent(g);

    }

    /**
     * @param icon
     */
    public void setBadgeIcon(final ImageIcon icon) {
        this.badgeIcon = icon;
        Container parent = this;
        BadgePainter painter = null;
        while ((parent = parent.getParent()) != null) {
            if (parent instanceof BadgePainter && ((BadgePainter) parent).isBadgesEnabled()) {
                painter = (BadgePainter) parent;
            }
        }
        if (painter != null) {
            painter.repaint();
        }

    }

    public void setForegroundBad(final Color fg) {
        this.foregroundBad = fg == null ? ((Editor) this.getEditor()).defaultForeground : fg;

    }

    /**
     * @param addLinksDialog_layoutDialogContent_packagename_help
     */
    public void setHelpText(final String helptext) {
        this.helptext = helptext;
        this.getTextField().setText(this.getText());

    }

    /**
     * Sets the Model for this combobox
     * 
     * @param listModel
     */
    public void setList(final ArrayList<T> listModel) {
        super.setModel(new DefaultComboBoxModel(listModel.toArray(new Object[] {})));
        try {
            final BasicComboBoxUI udi = (BasicComboBoxUI) this.getUI();
            JComponent arrowButton = null;
            try {
                final Field field = BasicComboBoxUI.class.getDeclaredField("arrowButton");

                final BasicComboBoxUI bla = null;

                if (field != null) {
                    field.setAccessible(true);
                    arrowButton = (JComponent) field.get(udi);

                }
            } catch (final Throwable e) {

            }
            if (listModel.size() > 0) {
                udi.unconfigureArrowButton();
                udi.configureArrowButton();
                if (arrowButton != null) {
                    arrowButton.setEnabled(true);

                }

            } else {
                udi.unconfigureArrowButton();
                if (arrowButton != null) {
                    arrowButton.setEnabled(false);

                }

            }

        } catch (final Throwable e) {
            e.printStackTrace();
            // for lafs not extending BasicComboBoxUI it is possible to open a
            // empty popup
        }
    }

    /**
     * Do not use this method. For Type Safty, please use
     * {@link #setList(ArrayList)} instead
     * 
     * @deprecated use {@link #setList(ArrayList)}
     */
    @Override
    @Deprecated
    public void setModel(final ComboBoxModel aModel) {
        if (aModel == null) {
            super.setModel(new DefaultComboBoxModel());
            return;
        }
        throw new RuntimeException("Use setList()");
    }

    @Override
    public void setRenderer(final ListCellRenderer aRenderer) {
        // TODO Auto-generated method stub
        System.out.println("SET " + aRenderer);
        super.setRenderer(aRenderer);
    }

    /**
     * @param defaultDownloadFolder
     */
    public void setText(final String text) {
        this.getTextField().setText(text);

    }

    /**
     * if unknown values are allowed, the component will not try to find a valid
     * entry on fopcus lost
     * 
     * @param allowUnknownValuesEnabled
     */
    public void setUnkownTextInputAllowed(final boolean allowUnknownValuesEnabled) {
        this.unkownTextInputAllowed = allowUnknownValuesEnabled;
    }

}
