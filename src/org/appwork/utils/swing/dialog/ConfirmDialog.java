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

import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.JScrollPane;
import javax.swing.JTextPane;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;

import org.appwork.swing.MigPanel;
import org.appwork.uio.ConfirmDialogInterface;
import org.appwork.uio.UIOManager;
import org.appwork.utils.BinaryLogic;
import org.appwork.utils.logging.Log;
import org.appwork.utils.os.CrossSystem;

public class ConfirmDialog extends AbstractDialog<Integer> implements ConfirmDialogInterface {

    private String message;

    public void setMessage(final String message) {
        this.message = message;
    }

    private JTextPane textField;

    public ConfirmDialog(final int flag, final String title, final String message, final ImageIcon icon, final String okOption, final String cancelOption) {
        super(flag, title, icon, okOption, cancelOption);
        Log.L.fine("Dialog    [" + okOption + "][" + cancelOption + "]\r\nflag:  " + Integer.toBinaryString(flag) + "\r\ntitle: " + title + "\r\nmsg:   \r\n" + message);

        this.message = message;
    }

    /**
     * @param i
     * @param name
     * @param gui_settings_extensions_show_now
     */
    public ConfirmDialog(final int flag, final String title, final String message) {
        this(flag, title, message, null, null, null);
    }

    public String getMessage() {
        return message;
    }

    public ConfirmDialogInterface show() {

        return UIOManager.I().show(ConfirmDialogInterface.class, this);
    }

    @Override
    protected Integer createReturnValue() {
        // TODO Auto-generated method stub
        return getReturnmask();
    }

    protected boolean isResizable() {
        // TODO Auto-generated method stub
        return true;
    }

    @Override
    public JComponent layoutDialogContent() {
        final MigPanel p = new MigPanel("ins 0", "[]", "[]");
        addMessageComponent(p);
        return p;
    }

    protected void addMessageComponent(final MigPanel p) {
        textField = new JTextPane() {
            private static final long serialVersionUID = 1L;

            @Override
            public boolean getScrollableTracksViewportWidth() {

                return !BinaryLogic.containsAll(ConfirmDialog.this.flagMask, Dialog.STYLE_LARGE);
            }
        };
        if (BinaryLogic.containsAll(flagMask, Dialog.STYLE_HTML)) {
            textField.setContentType("text/html");
            textField.addHyperlinkListener(new HyperlinkListener() {

                public void hyperlinkUpdate(final HyperlinkEvent e) {
                    if (e.getEventType() == HyperlinkEvent.EventType.ACTIVATED) {
                        CrossSystem.openURL(e.getURL());
                    }
                }

            });
        } else {
            textField.setContentType("text");
            // this.textField.setMaximumSize(new Dimension(450, 600));
        }

        textField.setText(message);
        textField.setEditable(false);
        textField.setBackground(null);
        textField.setOpaque(false);
        textField.setFocusable(false);
        textField.putClientProperty("Synthetica.opaque", Boolean.FALSE);
        textField.setCaretPosition(0);

        if (BinaryLogic.containsAll(flagMask, Dialog.STYLE_LARGE)) {

            p.add(new JScrollPane(textField), "pushx,growx");

        } else {

            p.add(textField);

        }
 
    }

    @Override
    public String toString() {
        if (BinaryLogic.containsAll(flagMask, Dialog.LOGIC_DONOTSHOW_BASED_ON_TITLE_ONLY)) {
            return ("dialog-" + getTitle()).replaceAll("\\W", "_");
        } else {
            return ("dialog-" + getTitle() + "_" + message).replaceAll("\\W", "_");
        }

    }

}
