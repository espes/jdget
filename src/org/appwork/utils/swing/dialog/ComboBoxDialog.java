/**
 * Copyright (c) 2009 - 2010 AppWork UG(haftungsbeschränkt) <e-mail@appwork.org>
 * 
 * This file is part of org.appwork.utils.swing.dialog
 * 
 * This software is licensed under the Artistic License 2.0,
 * see the LICENSE file or http://www.opensource.org/licenses/artistic-license-2.0.php
 * for details
 */
package org.appwork.utils.swing.dialog;

import java.util.Arrays;

import javax.swing.ImageIcon;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JTextPane;
import javax.swing.ListCellRenderer;

import org.appwork.swing.MigPanel;
import org.appwork.utils.logging.Log;

public class ComboBoxDialog extends AbstractDialog<Integer> implements ComboBoxDialogInterface {
    /**
     * The Comboxbox to display the options
     */
    private JComboBox              box;
    /**
     * Stores an additional message
     */
    private final String           message;
    /**
     * Textpane to display th {@link #message}
     */
    private JTextPane              textpane;
    /**
     * Defaultanswer. Answers are given as optionindex
     */
    private final int              defaultAnswer;
    /**
     * Available options
     */
    private final Object[]         options;
    /**
     * Listrenderer to render the optionobjects
     */
    private final ListCellRenderer renderer;

    /**
     * 
     * @see Dialog#showComboDialog(int, String, String, Object[], int,
     *      ImageIcon, String, String, ListCellRenderer)
     */
    public ComboBoxDialog(final int flag, final String title, final String question, final Object[] options, final int defaultSelection, final ImageIcon icon, final String okText, final String cancelText, final ListCellRenderer renderer) {
        super(flag, title, icon, okText, cancelText);
        Log.L.fine("Dialog    [" + okText + "][" + cancelText + "]\r\nflag:  " + Integer.toBinaryString(flag) + "\r\ntitle: " + title + "\r\nmsg:   \r\n" + question + "\r\noptions:   \r\n" + Arrays.toString(options) + "\r\ndef:" + defaultSelection);

        message = question;
        this.renderer = renderer;
        defaultAnswer = defaultSelection < 0 ? 0 : defaultSelection;
        this.options = options;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.appwork.utils.swing.dialog.AbstractDialog#getRetValue()
     */
    @Override
    protected Integer createReturnValue() {
        return getReturnIndex();
    }

    /**
     * @param options2
     * @return
     */
    protected JComboBox getComboBox(final Object[] options2) {

        final JComboBox ret = new JComboBox(options2);
        final ListCellRenderer rend = getRenderer(ret.getRenderer());
        if (rend != null) {
            ret.setRenderer(rend);
        }

        try {
            if (defaultAnswer < options.length && defaultAnswer >= 0) {
                ret.setSelectedIndex(defaultAnswer);
            }
        } catch (final Exception e) {
            Log.exception(e);
        }
        return ret;
    }

    /**
     * @param renderer2
     * @return
     */
    protected ListCellRenderer getRenderer(final ListCellRenderer orgRenderer) {
        // TODO Auto-generated method stub
        return renderer;
    }

    public Integer getReturnIndex() {
        if ((getReturnmask() & Dialog.RETURN_OK) == 0) { return Integer.valueOf(-1); }
        return Integer.valueOf(box.getSelectedIndex());
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.appwork.utils.swing.dialog.AbstractDialog#layoutDialogContent()
     */
    @Override
    public JComponent layoutDialogContent() {
        final JPanel contentpane = new MigPanel("ins 0,wrap 1", "[fill,grow]", "[][]");
        textpane = new JTextPane();
        textpane.setBorder(null);
        textpane.setBackground(null);
        textpane.setOpaque(false);
        textpane.putClientProperty("Synthetica.opaque", Boolean.FALSE);
        textpane.setText(message);
        textpane.setEditable(false);

        contentpane.add(textpane);

        box = getComboBox(options);

        // no idea what this has been good for
        // if (this.getDesiredSize() != null) {
        // this.box.setBounds(0, 0, (int) this.getDesiredSize().getWidth(),
        // (int) this.getDesiredSize().getHeight());
        // this.box.setMaximumSize(this.getDesiredSize());
        // } else {
        // this.box.setBounds(0, 0, 450, 600);
        // this.box.setMaximumSize(new Dimension(450, 600));
        // }
        contentpane.add(box, "pushy,growy,height 24!");

        return contentpane;
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.appwork.utils.swing.dialog.ComboBoxDialogInterface#getSelectedIndex()
     */
    @Override
    public int getSelectedIndex() {
        return getReturnIndex();
    }

}
