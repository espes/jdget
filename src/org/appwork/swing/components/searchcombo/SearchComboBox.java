package org.appwork.swing.components.searchcombo;

import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Rectangle;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.util.ArrayList;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

import javax.swing.ComboBoxEditor;
import javax.swing.ComboBoxModel;
import javax.swing.DefaultComboBoxModel;
import javax.swing.Icon;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.ListCellRenderer;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;
import javax.swing.plaf.basic.ComboPopup;

import org.appwork.app.gui.MigPanel;
import org.appwork.scheduler.DelayedRunnable;
import org.appwork.utils.logging.Log;
import org.appwork.utils.swing.EDTRunner;

/**
 * this component extends a normal combobox and implements a editable
 * filter/autocompletion feature. <b> make sure that you model is sorted</b>
 * 
 * @author thomas
 * 
 * @param <T>
 */
public abstract class SearchComboBox<T> extends JComboBox {
    class Editor implements ComboBoxEditor {
        private final JTextField      tf;
        private final MigPanel        panel;

        private final JLabel          icon;
        private T                     value;
        private final DelayedRunnable sheduler;
        private final Color           defaultForeground;
        private boolean               setting;

        public Editor(final ScheduledExecutorService scheduler) {
            this.tf = new JTextField();
            this.icon = new JLabel();
            // editor panel
            this.panel = new MigPanel("ins 0", "[][grow,fill]", "[grow,fill]") {

                @Override
                public void requestFocus() {
                    Editor.this.tf.requestFocus();
                }

            };

            this.panel.add(this.icon);
            this.panel.setOpaque(true);
            this.panel.setBackground(this.tf.getBackground());

            this.tf.setBackground(null);
            this.tf.setOpaque(false);
            this.tf.putClientProperty("Synthetica.opaque", Boolean.FALSE);
            this.defaultForeground = this.tf.getForeground();
            this.panel.add(this.tf);
            this.sheduler = new DelayedRunnable(scheduler, 150l) {

                @Override
                public void delayedrun() {
                    Editor.this.autoComplete(true);
                }

            };
            // this.panel.setBorder(this.tf.getBorder());
            this.tf.setBorder(null);
            this.tf.addFocusListener(new FocusListener() {

                @Override
                public void focusGained(final FocusEvent e) {
                    Editor.this.tf.selectAll();
                }

                @Override
                public void focusLost(final FocusEvent e) {
                    if (!Editor.this.autoComplete(false)) {
                        try {
                            Editor.this.tf.setText(SearchComboBox.this.getText(Editor.this.value));
                        } catch (final NullPointerException e2) {
                            Editor.this.tf.setText("");
                        }
                        Editor.this.autoComplete(false);
                    }

                }
            });
            this.tf.getDocument().addDocumentListener(new DocumentListener() {

                @Override
                public void changedUpdate(final DocumentEvent e) {

                    Editor.this.auto();
                }

                @Override
                public void insertUpdate(final DocumentEvent e) {
                    Editor.this.auto();
                }

                @Override
                public void removeUpdate(final DocumentEvent e) {
                    // this would avoid usuage of backspace
                    // Editor.this.auto();

                }
            });
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
            if (this.value != null && SearchComboBox.this.getText(this.value).equals(txt)) { return true; }
            String text = null;
            final ArrayList<T> found = new ArrayList<T>();

            for (int i = 0; i < SearchComboBox.this.getModel().getSize(); i++) {
                text = SearchComboBox.this.getText((T) SearchComboBox.this.getModel().getElementAt(i));
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
            this.tf.setText(SearchComboBox.this.getText((T) anObject));
            this.icon.setIcon(SearchComboBox.this.getIcon((T) anObject));
            this.value = (T) anObject;
            this.setting = false;

        }

    }

    private Color foregroundBad = Color.red;

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
                    final JLabel ret = (JLabel) org.getListCellRendererComponent(list, SearchComboBox.this.getText((T) value), index, isSelected, cellHasFocus);

                    ret.setIcon(SearchComboBox.this.getIcon((T) value));

                    // ret.setOpaque(false);
                    return ret;
                } catch (final Throwable e) {
                    // org might not be a JLabel (depending on the LAF)
                    // fallback here

                    return org.getListCellRendererComponent(list, SearchComboBox.this.getText((T) value), index, isSelected, cellHasFocus);

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
    abstract protected Icon getIcon(T value);

    /**
     * @param value
     * @return
     */
    abstract protected String getText(T value);

    public JTextField getTextField() {
        return ((Editor) this.getEditor()).getTf();
    }

    public void setForegroundBad(final Color forgroundGood) {
        this.foregroundBad = forgroundGood;

    }

    /**
     * Sets the Model for this combobox
     * 
     * @param listModel
     */
    public void setList(final ArrayList<T> listModel) {
        super.setModel(new DefaultComboBoxModel(listModel.toArray(new Object[] {})));
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
}
