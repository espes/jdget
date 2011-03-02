/**
 * Copyright (c) 2009 - 2011 AppWork UG(haftungsbeschränkt) <e-mail@appwork.org>
 * 
 * This file is part of org.appwork.utils.swing.dialog
 * 
 * This software is licensed under the Artistic License 2.0,
 * see the LICENSE file or http://www.opensource.org/licenses/artistic-license-2.0.php
 * for details
 */
package org.appwork.utils.swing.dialog;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextPane;
import javax.swing.SwingConstants;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;

import net.miginfocom.swing.MigLayout;

import org.appwork.utils.BinaryLogic;
import org.appwork.utils.Exceptions;
import org.appwork.utils.ImageProvider.ImageProvider;
import org.appwork.utils.locale.APPWORKUTILS;
import org.appwork.utils.logging.Log;
import org.appwork.utils.os.CrossSystem;

/**
 * @author thomas
 * 
 */
public class ExceptionDialog extends AbstractDialog<Integer> {

    private static final long serialVersionUID = -7647771640756844691L;

    private final String      message;
    private JTextPane         textField;

    private final Throwable   exception;

    private JTextArea         logField;

    private JScrollPane       scrollPane;

    private JLabel            logLabel;

    public ExceptionDialog(final int flag, final String title, final String message, final Throwable exception, final String okOption, final String cancelOption) {
        super(flag, title, null, okOption, cancelOption);
        Log.L.fine("Dialog    [" + okOption + "][" + cancelOption + "]\r\nflag:  " + Integer.toBinaryString(flag) + "\r\ntitle: " + title + "\r\nmsg:   \r\n" + message);

        this.message = message;
        this.exception = exception;
    }

    @Override
    protected void addButtons(final JPanel buttonBar) {

        final JButton more = new JButton(APPWORKUTILS.T.ExceptionDialog_layoutDialogContent_more_button());

        more.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        // more.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0,
        // cp.getBackground().darker()));
        more.addActionListener(new ActionListener() {

            public void actionPerformed(final ActionEvent e) {

                scrollPane.setVisible(true);
                logField.setText(Exceptions.getStackTrace(exception));
                logLabel.setVisible(true);
                more.setVisible(false);
                setResizable(true);
                pack();
            }
        });
        more.setHorizontalAlignment(SwingConstants.RIGHT);
        buttonBar.add(more, "hidemode 3");
    }

    @Override
    protected Integer createReturnValue() {
        // TODO Auto-generated method stub
        return getReturnmask();
    }

    @Override
    protected String getDontShowAgainKey() {
        return "ABSTRACTDIALOG_DONT_SHOW_AGAIN_" + exception.hashCode() + "_" + toString();
    }

    public Throwable getException() {
        return exception;
    }

    @Override
    public JComponent layoutDialogContent() {

        final JPanel cp = new JPanel(new MigLayout("ins 0,wrap 1", "[fill]", "[][]"));
        textField = new JTextPane() {
            private static final long serialVersionUID = 1L;

            @Override
            public boolean getScrollableTracksViewportWidth() {
                return !BinaryLogic.containsAll(ExceptionDialog.this.flagMask, Dialog.STYLE_LARGE);
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
        textField.putClientProperty("Synthetica.opaque", Boolean.FALSE);
        textField.setCaretPosition(0);
        cp.add(new JLabel(ImageProvider.getImageIcon(Dialog.ICON_ERROR, 32, 32)), "width 32!,split 2");
        if (BinaryLogic.containsAll(flagMask, Dialog.STYLE_LARGE)) {
            cp.add(new JScrollPane(textField), "pushx,growx");
        } else {
            cp.add(textField, "pushx,growx");
        }

        logField = new JTextArea();
        logField.setLineWrap(false);

        logField.setEditable(true);
        logField.setAutoscrolls(true);
        scrollPane = new JScrollPane(logField);
        scrollPane.setVisible(false);
        logField.setEditable(true);
        logField.setAutoscrolls(true);
        logField.setForeground(Color.RED);
        logLabel = new JLabel(APPWORKUTILS.T.ExceptionDialog_layoutDialogContent_logLabel());
        logLabel.setVisible(false);
        cp.add(logLabel, "hidemode 3,gaptop 5");

        cp.add(scrollPane, "hidemode 3,height 100:300:n,width 200:600:n,pushx,growx,pushy,growy");

        return cp;

    }

    @Override
    public String toString() {
        return ("dialog-" + getTitle() + "_" + message).replaceAll("\\W", "_");
    }

}
