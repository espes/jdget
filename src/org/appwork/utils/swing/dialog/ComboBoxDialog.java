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

import java.awt.Dimension;
import java.util.Arrays;

import javax.swing.ImageIcon;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JTextPane;
import javax.swing.ListCellRenderer;

import net.miginfocom.swing.MigLayout;

import org.appwork.utils.logging.Log;

public class ComboBoxDialog extends AbstractDialog<Integer> {
    /**
     * 
     */
    private static final long      serialVersionUID = 3779238515088665521L;
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
     *@see Dialog#showComboDialog(int, String, String, Object[], int,
     *      ImageIcon, String, String, ListCellRenderer)
     */
    public ComboBoxDialog(final int flag, final String title, final String question, final Object[] options, final int defaultSelection, final ImageIcon icon, final String okText, final String cancelText, final ListCellRenderer renderer) {
        super(flag, title, icon, okText, cancelText);
        Log.L.fine("Dialog    [" + okText + "][" + cancelText + "]\r\nflag:  " + Integer.toBinaryString(flag) + "\r\ntitle: " + title + "\r\nmsg:   \r\n" + question + "\r\noptions:   \r\n" + Arrays.toString(options) + "\r\ndef:" + defaultSelection);

        this.message = question;
        this.renderer = renderer;
        this.defaultAnswer = defaultSelection;
        this.options = options;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.appwork.utils.swing.dialog.AbstractDialog#getRetValue()
     */
    @Override
    protected Integer createReturnValue() {
        return this.getReturnIndex();
    }

    public Integer getReturnIndex() {
        if ((this.getReturnmask() & Dialog.RETURN_OK) == 0) { return Integer.valueOf(-1); }
        return Integer.valueOf(this.box.getSelectedIndex());
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.appwork.utils.swing.dialog.AbstractDialog#layoutDialogContent()
     */
    @Override
    public JComponent layoutDialogContent() {
        final JPanel contentpane = new JPanel(new MigLayout("ins 0,wrap 1", "[fill,grow]"));
        this.textpane = new JTextPane();
        this.textpane.setBorder(null);
        this.textpane.setBackground(null);
        this.textpane.setOpaque(false);
        this.textpane.putClientProperty("Synthetica.opaque", Boolean.FALSE);
        this.textpane.setText(this.message);
        this.textpane.setEditable(false);

        contentpane.add(this.textpane);

        this.box = new JComboBox(this.options);
        if (this.renderer != null) {
            this.box.setRenderer(this.renderer);
        }
        try {

            this.box.setSelectedIndex(this.defaultAnswer);
        } catch (final Exception e) {
            Log.exception(e);
        }
        if (this.getDesiredSize() != null) {
            this.box.setBounds(0, 0, (int) this.getDesiredSize().getWidth(), (int) this.getDesiredSize().getHeight());
            this.box.setMaximumSize(this.getDesiredSize());
        } else {
            this.box.setBounds(0, 0, 450, 600);
            this.box.setMaximumSize(new Dimension(450, 600));
        }
        contentpane.add(this.box, "pushy,growy, width n:n:450");

        return contentpane;
    }

}
