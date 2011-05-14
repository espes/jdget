package org.appwork.swing.components.searchcombo;

import java.awt.Color;
import java.awt.Component;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;

import javax.swing.ComboBoxEditor;
import javax.swing.Icon;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JTextField;
import javax.swing.ListCellRenderer;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import org.appwork.app.gui.MigPanel;
import org.appwork.scheduler.SingleSchedule;
import org.appwork.utils.swing.EDTRunner;

public abstract class SearchComboBox<T> extends JComboBox {
    class Editor implements ComboBoxEditor {
        private final JTextField     tf;
        private final MigPanel       panel;
        private final JLabel         icon;
        private Object               value;
        private final SingleSchedule sheduler;
        private final Color          defaultForeground;
        private boolean              setting;

        public Editor() {
            this.tf = new JTextField();
            this.icon = new JLabel();
            this.panel = new MigPanel("ins 0", "[][grow,fill]", "[grow,fill]");
            this.panel.add(this.icon);
            this.panel.setOpaque(true);
            this.panel.setBackground(this.tf.getBackground());
            this.tf.setBackground(null);
            this.tf.setOpaque(false);
            this.defaultForeground = this.tf.getForeground();
            this.panel.add(this.tf);
            this.sheduler = new SingleSchedule(50);
            // this.panel.setBorder(this.tf.getBorder());
            this.tf.setBorder(null);
            this.tf.addFocusListener(new FocusListener() {

                @Override
                public void focusGained(final FocusEvent e) {
                    Editor.this.tf.selectAll();
                }

                @Override
                public void focusLost(final FocusEvent e) {
                    if (!Editor.this.autoComplete()) {
                        Editor.this.tf.setText("");
                        Editor.this.autoComplete();
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

            this.sheduler.submit(new Runnable() {

                @Override
                public void run() {
                    Editor.this.autoComplete();
                }
            });

        }

        /**
         * 
         */
        protected boolean autoComplete() {
            final String txt = Editor.this.tf.getText();
            T found = null;
            String text = null;
            for (int i = 0; i < SearchComboBox.this.getModel().getSize(); i++) {
                text = SearchComboBox.this.getText((T) SearchComboBox.this.getModel().getElementAt(i));
                if (text != null && text.startsWith(txt)) {
                    found = (T) SearchComboBox.this.getModel().getElementAt(i);
                    break;
                }
            }
            final T fFound = found;

            new EDTRunner() {

                @Override
                protected void runInEDT() {
                    final int pos = Editor.this.tf.getCaretPosition();
                    if (fFound == null) {
                        Editor.this.tf.setForeground(SearchComboBox.this.getForegroundBad());

                    } else {
                        Editor.this.tf.setForeground(Editor.this.defaultForeground);

                        Editor.this.setItem(fFound);
                        Editor.this.tf.setCaretPosition(pos);
                        Editor.this.tf.select(txt.length(), Editor.this.tf.getText().length());

                    }
                }
            };
            return fFound != null;
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
        @Override
        public void setItem(final Object anObject) {
            // if (this.value == anObject) { return; }
            this.setting = true;
            this.tf.setText(SearchComboBox.this.getText((T) anObject));
            this.icon.setIcon(SearchComboBox.this.getIcon((T) anObject));
            this.value = anObject;
            this.setting = false;

        }

    }

    private Color foregroundBad = Color.red;

    public SearchComboBox() {
        super();

        this.setEditor(new Editor());
        this.setEditable(true);
        final ListCellRenderer org = this.getRenderer();

        this.setRenderer(new ListCellRenderer() {

            @SuppressWarnings("unchecked")
            public Component getListCellRendererComponent(final JList list, final Object value, final int index, final boolean isSelected, final boolean cellHasFocus) {

                final JLabel ret = (JLabel) org.getListCellRendererComponent(list, SearchComboBox.this.getText((T) value), index, isSelected, cellHasFocus);
                ret.setIcon(SearchComboBox.this.getIcon((T) value));
                // ret.setOpaque(false);
                return ret;
            }
        });
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

    public void setForegroundBad(final Color forgroundGood) {
        this.foregroundBad = forgroundGood;
    }
}
