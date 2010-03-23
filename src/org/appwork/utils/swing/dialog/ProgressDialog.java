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

import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JTextPane;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;

import net.miginfocom.swing.MigLayout;

import org.appwork.utils.BinaryLogic;
import org.appwork.utils.os.CrossSystem;
import org.appwork.utils.swing.EDTHelper;

/**
 * @author thomas
 * 
 */
public class ProgressDialog extends AbstractDialog {
    private static final long serialVersionUID = -7420852517889843489L;
    private JTextPane textField;
    private String message;

    private ProgressGetter getter;

    /**
     * @param progressGetter
     * @param s
     * @param s2
     */
    public ProgressDialog(ProgressGetter progressGetter, String title, String message) {
        super(Dialog.BUTTONS_HIDE_OK, title, null, null, null);
        this.message = message;

        getter = progressGetter;
        init();
    }

    public interface ProgressGetter {
        public int getProgress();

        public String getString();
    }

    private JComponent getTextfield() {
        textField = new JTextPane();
        if (BinaryLogic.containsAll(this.flagMask, Dialog.STYLE_HTML)) {
            textField.setContentType("text/html");
            textField.addHyperlinkListener(new HyperlinkListener() {

                public void hyperlinkUpdate(HyperlinkEvent e) {
                    if (e.getEventType() == HyperlinkEvent.EventType.ACTIVATED) {
                        CrossSystem.openURL(e.getURL());
                    }
                }

            });
        } else {
            textField.setContentType("text");
            textField.setMaximumSize(new Dimension(450, 600));
        }

        textField.setText(this.message);
        textField.setEditable(false);
        textField.setBackground(null);
        textField.setOpaque(false);
        textField.putClientProperty("Synthetica.opaque", Boolean.FALSE);

        if (BinaryLogic.containsAll(this.flagMask, Dialog.STYLE_LARGE)) {
            JScrollPane sp = new JScrollPane(textField);
            sp.setMaximumSize(new Dimension(450, 600));
            return sp;
        } else {
            return textField;
        }
    }

    public JComponent layoutDialogContent() {

        JPanel p = new JPanel(new MigLayout("ins 0"));
        p.add(getTextfield(), "growx,pushx");
        final JProgressBar bar;
        p.add(bar = new JProgressBar(0, 100), "growx,pushx,newline");
        bar.setStringPainted(true);
        final Thread th = new Thread() {
            public void run() {
                while (true) {
                    if (getter != null) {
                        final int prg = getter.getProgress();
                        final String text = getter.getString();
                        new EDTHelper<Object>() {

                            @Override
                            public Object edtRun() {
                                bar.setValue(prg);
                                if (text == null) {
                                    bar.setStringPainted(false);
                                } else {
                                    bar.setStringPainted(true);
                                    bar.setString(text);
                                }
                                return null;
                            }

                        }.start();
                        if (prg >= 100) {
                            dispose();
                            return;
                        }
                    }
                    try {
                        Thread.sleep(500);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        };
        th.start();
        return p;
    }

}
