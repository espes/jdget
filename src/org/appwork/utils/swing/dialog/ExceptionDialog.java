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

import org.appwork.resources.AWUTheme;
import org.appwork.utils.BinaryLogic;
import org.appwork.utils.Exceptions;
import org.appwork.utils.locale._AWU;
import org.appwork.utils.logging.Log;
import org.appwork.utils.os.CrossSystem;

/**
 * @author thomas
 * 
 */
public class ExceptionDialog extends AbstractDialog<Integer> {

    private final String    message;
    private JTextPane       textField;

    private final Throwable exception;

    private JTextArea       logField;

    private JScrollPane     scrollPane;

    private JLabel          logLabel;
    private JButton         more;
    private boolean         expanded = false;
    private String          moreString;

    public boolean isExpanded() {
        return expanded;
    }

    public void setExpanded(boolean expanded) {
        this.expanded = expanded;
    }

    public ExceptionDialog(final int flag, final String title, final String message, final Throwable exception, final String okOption, final String cancelOption) {
        super(flag, title, null, okOption, cancelOption);
        Log.L.fine("Dialog    [" + okOption + "][" + cancelOption + "]\r\nflag:  " + Integer.toBinaryString(flag) + "\r\ntitle: " + title + "\r\nmsg:   \r\n" + message);

        this.message = message;
        this.exception = exception;
    }

    public String getMessage() {
        return message;
    }

    @Override
    protected void addButtons(final JPanel buttonBar) {

        more = new JButton(_AWU.T.ExceptionDialog_layoutDialogContent_more_button());

        more.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        // more.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0,
        // cp.getBackground().darker()));
        more.addActionListener(new ActionListener() {

            public void actionPerformed(final ActionEvent e) {

                expand();
            }
        });
        more.setHorizontalAlignment(SwingConstants.RIGHT);
        buttonBar.add(more, "hidemode 3");
        if (expanded) {
            expand();
        }
    }

    @Override
    protected Integer createReturnValue() {
        // TODO Auto-generated method stub
        return this.getReturnmask();
    }

    @Override
    protected String getDontShowAgainKey() {
        return "ABSTRACTDIALOG_DONT_SHOW_AGAIN_" + this.exception.hashCode() + "_" + this.toString();
    }

    public Throwable getException() {
        return this.exception;
    }

    @Override
    public JComponent layoutDialogContent() {

        final JPanel cp = new JPanel(new MigLayout("ins 0,wrap 1", "[fill]", "[][]"));
        this.textField = new JTextPane() {
            private static final long serialVersionUID = 1L;

            @Override
            public boolean getScrollableTracksViewportWidth() {
                return !BinaryLogic.containsAll(ExceptionDialog.this.flagMask, Dialog.STYLE_LARGE);
            }
        };
        if (BinaryLogic.containsAll(this.flagMask, Dialog.STYLE_HTML)) {
            this.textField.setContentType("text/html");
            this.textField.addHyperlinkListener(new HyperlinkListener() {

                public void hyperlinkUpdate(final HyperlinkEvent e) {
                    if (e.getEventType() == HyperlinkEvent.EventType.ACTIVATED) {
                        CrossSystem.openURL(e.getURL());
                    }
                }

            });
        } else {
            this.textField.setContentType("text");
            // this.textField.setMaximumSize(new Dimension(450, 600));
        }

        this.textField.setText(this.message);
        this.textField.setEditable(false);
        this.textField.setBackground(null);
        this.textField.setOpaque(false);
        this.textField.putClientProperty("Synthetica.opaque", Boolean.FALSE);
        this.textField.setCaretPosition(0);
        cp.add(new JLabel(AWUTheme.I().getIcon(Dialog.ICON_ERROR, 32)), "width 32!,split 2");
        if (BinaryLogic.containsAll(this.flagMask, Dialog.STYLE_LARGE)) {
            cp.add(new JScrollPane(this.textField), "pushx,growx");
        } else {
            cp.add(this.textField, "pushx,growx");
        }

        this.logField = new JTextArea();
        this.logField.setLineWrap(false);

        this.logField.setEditable(true);
        this.logField.setAutoscrolls(true);
        this.scrollPane = new JScrollPane(this.logField);
        this.scrollPane.setVisible(false);
        this.logField.setEditable(true);
        this.logField.setAutoscrolls(true);
        this.logField.setForeground(Color.RED);
        this.logLabel = new JLabel(_AWU.T.ExceptionDialog_layoutDialogContent_logLabel());
        this.logLabel.setVisible(false);
        cp.add(this.logLabel, "hidemode 3,gaptop 5");

        cp.add(this.scrollPane, "hidemode 3,height 100:300:n,width 200:600:n,pushx,growx,pushy,growy");

        return cp;

    }

    @Override
    public String toString() {
        return ("dialog-" + this.getTitle() + "_" + this.message).replaceAll("\\W", "_");
    }

    public void expand() {
        ExceptionDialog.this.scrollPane.setVisible(true);
        if (moreString != null) {
            logField.setText(moreString);
        } else {
            ExceptionDialog.this.logField.setText(Exceptions.getStackTrace(ExceptionDialog.this.exception));
        }
        ExceptionDialog.this.logLabel.setVisible(true);

        more.setVisible(false);
        ExceptionDialog.this.setResizable(true);
        ExceptionDialog.this.pack();
    }

    /**
     * @param string
     */
    public void setMore(String string) {
        moreString = string;

    }

}
