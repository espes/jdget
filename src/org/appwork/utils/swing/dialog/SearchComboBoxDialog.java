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

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.Arrays;

import javax.imageio.ImageIO;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JTextPane;
import javax.swing.ListCellRenderer;

import org.appwork.storage.JSonStorage;
import org.appwork.swing.MigPanel;
import org.appwork.swing.components.searchcombo.SearchComboBox;
import org.appwork.utils.images.IconIO;
import org.appwork.utils.logging.Log;
import org.appwork.utils.net.Base64OutputStream;

public class SearchComboBoxDialog<Type> extends AbstractDialog<Type> implements IconComboBoxDialogInterface {

    /**
     * Stores an additional message
     */
    private final String         message;
    /**
     * Textpane to display th {@link #message}
     */
    private JTextPane            textpane;
    /**
     * Defaultanswer. Answers are given as optionindex
     */
    private final Type           defaultAnswer;
    /**
     * Available options
     */
    private final Type[]         options;
    private SearchComboBox<Type> box;

    @Override
    public boolean isRemoteAPIEnabled() {
        return true;
    }

    /**
     * 
     * @see Dialog#showComboDialog(int, String, String, Object[], int,
     *      ImageIcon, String, String, ListCellRenderer)
     */
    public SearchComboBoxDialog(final int flag, final String title, final String question, final Type[] options, final Type defaultSelection, final ImageIcon icon, final String okText, final String cancelText) {
        super(flag, title, icon, okText, cancelText);
        Log.L.fine("Dialog    [" + okText + "][" + cancelText + "]\r\nflag:  " + Integer.toBinaryString(flag) + "\r\ntitle: " + title + "\r\nmsg:   \r\n" + question + "\r\noptions:   \r\n" + Arrays.toString(options) + "\r\ndef:" + defaultSelection);

        message = question;

        defaultAnswer = defaultSelection;
        this.options = options;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.appwork.utils.swing.dialog.AbstractDialog#getRetValue()
     */
    @Override
    protected Type createReturnValue() {
        return getSelectedItem();
    }

    /**
     * @param options2
     * @return
     */
    protected SearchComboBox<Type> getComboBox(final Type[] options2) {
        final ArrayList<Type> ret = new ArrayList<Type>();
        for (final Type t : options2) {
            ret.add(t);
        }
        final SearchComboBox<Type> box = new SearchComboBox<Type>(ret) {

            @Override
            protected Icon getIconForValue(final Type value) {
                if (value == null) { return null; }
                return SearchComboBoxDialog.this.getIconByValue(value);

            }

            @Override
            public void onChanged() {
                super.onChanged();
                SearchComboBoxDialog.this.onChanged();

            }

            @Override
            protected String getTextForValue(final Type value) {
                if (value == null) { return ""; }
                return SearchComboBoxDialog.this.getStringByValue(value);
            }

        };
        if (defaultAnswer != null) {
            box.setSelectedItem(defaultAnswer);
        }

        return box;
    }

    /**
     * @param value
     * @return
     */
    protected String getStringByValue(final Type value) {

        return value + "";
    }

    /**
     * 
     */
    protected void onChanged() {
        // TODO Auto-generated method stub

    }

    /**
     * @param value
     * @return
     */
    protected Icon getIconByValue(final Type value) {

        return null;
    }

    public Type getSelectedItem() {
        if ((getReturnmask() & Dialog.RETURN_OK) == 0) { return null; }
        return box == null ? defaultAnswer : box.getSelectedItem();
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
     * @see org.appwork.uio.ComboBoxDialogInterface#getSelectedIndex()
     */
    @Override
    public int getSelectedIndex() {
        if ((getReturnmask() & Dialog.RETURN_OK) == 0) { return -1; }
        if (box != null) { return box.getSelectedIndex(); }
        if (defaultAnswer == null) { return -1; }
        return Arrays.binarySearch(options, defaultAnswer);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.appwork.uio.ConfirmDialogInterface#getMessage()
     */
    @Override
    public String getMessage() {

        return message;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.appwork.uio.ComboBoxDialogInterface#getLabels()
     */
    @Override
    public String[] getLabels() {
        String[] ret = new String[options.length];
        for (int i = 0; i < ret.length; i++) {
            ret[i] = JSonStorage.toString(getStringByValue(options[i]));

        }
        return ret;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.appwork.uio.ComboBoxDialogInterface#getPreSelectedIndex()
     */
    @Override
    public int getPreSelectedIndex() {
        for (int i = 0; i < options.length; i++) {

            if (options[i].equals(defaultAnswer)) { return i; }
        }
        return -1;

    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.appwork.utils.swing.dialog.IconComboBoxDialogInterface#getIconDataUrls
     * ()
     */
    @Override
    public String[] getIconDataUrls() {
        String[] ret = new String[options.length];
        for (int i = 0; i < ret.length; i++) {
            Icon ic = getIconByValue(options[i]);
            if (ic == null) {
                ret[i] = null;
            } else {
                Base64OutputStream b64os = null;
                ByteArrayOutputStream bos = null;

                try {
                    bos = new ByteArrayOutputStream();
                    b64os = new Base64OutputStream(bos);
                    ImageIO.write(IconIO.convertIconToBufferedImage(ic), "png", b64os);
                    b64os.flush(true);
                    ret[i] = "png;base64," + bos.toString("UTF-8");

                } catch (final Exception e) {
                    e.printStackTrace();
                    return null;
                } finally {
                    try {
                        b64os.close();
                    } catch (final Throwable e) {
                    }
                    try {
                        bos.close();
                    } catch (final Throwable e) {
                    }

                }
            }

        }
        return ret;

    }

}
