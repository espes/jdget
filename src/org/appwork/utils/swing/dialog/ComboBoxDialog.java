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

import javax.swing.ImageIcon;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JTextPane;
import javax.swing.ListCellRenderer;

import net.miginfocom.swing.MigLayout;

public class ComboBoxDialog extends AbstractDialog {
    /**
     * 
     */
    private static final long serialVersionUID = 3779238515088665521L;
    /**
     * The Comboxbox to display the options
     */
    private JComboBox box;
    /**
     * Stores an additional message
     */
    private String message;
    /**
     * Textpane to display th {@link #message}
     */
    private JTextPane textpane;
    /**
     * Defaultanswer. Answers are given as optionindex
     */
    private int defaultAnswer;
    /**
     * Available options
     */
    private Object[] options;
    /**
     * Listrenderer to render the optionobjects
     */
    private ListCellRenderer renderer;

    /**
     * 
     *@see Dialog#showComboDialog(int, String, String, Object[], int,
     *      ImageIcon, String, String, ListCellRenderer)
     */
    public ComboBoxDialog(int flag, String title, String question, Object[] options, int defaultSelection, ImageIcon icon, String okText, String cancelText, ListCellRenderer renderer) {
        super(flag, title, icon, okText, cancelText);
        message = question;
        this.renderer = renderer;
        this.defaultAnswer = defaultSelection;
        this.options = options;
        init();
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * com.rapidshare.utils.swing.dialog.AbstractDialog#layoutDialogContent()
     */
    @Override
    public JComponent layoutDialogContent() {
        JPanel contentpane = new JPanel(new MigLayout("ins 0,wrap 1", "[fill,grow]"));
        textpane = new JTextPane();
        textpane.setBorder(null);
        textpane.setBackground(null);
        textpane.setOpaque(false);
        textpane.setText(this.message);
        textpane.setEditable(false);

        contentpane.add(textpane);

        box = new JComboBox(options);
        if (renderer != null) box.setRenderer(renderer);
        box.setSelectedIndex(this.defaultAnswer);

        if (getDesiredSize() != null) {
            box.setBounds(0, 0, (int) getDesiredSize().getWidth(), (int) getDesiredSize().getHeight());
            box.setMaximumSize(getDesiredSize());
        } else {
            box.setBounds(0, 0, 450, 600);
            box.setMaximumSize(new Dimension(450, 600));
        }
        contentpane.add(box, "pushy,growy, width n:n:450");

        return contentpane;
    }

    public Integer getReturnIndex() {
        if ((this.getReturnmask() & Dialog.RETURN_OK) == 0) return -1;
        return box.getSelectedIndex();
    }

}
