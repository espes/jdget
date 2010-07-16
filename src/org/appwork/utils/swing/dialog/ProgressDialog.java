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
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JTextPane;
import javax.swing.Timer;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;

import net.miginfocom.swing.MigLayout;

import org.appwork.utils.BinaryLogic;
import org.appwork.utils.os.CrossSystem;

/**
 * @author thomas
 * 
 */
public class ProgressDialog extends AbstractDialog {
    private static final long serialVersionUID = -7420852517889843489L;
    private JTextPane textField;
    private String message;

    private ProgressGetter getter;
    private Timer updater;
    private Thread executer;

    /**
     * @param progressGetter
     * @param flags
     *            TODO
     * @param s
     * @param s2
     */
    public ProgressDialog(ProgressGetter progressGetter, int flags, String title, String message) {
        super(flags | Dialog.BUTTONS_HIDE_OK, title, null, null, null);
        this.message = message;
        this.setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
        getter = progressGetter;
        setReturnmask(true);
        init();
    }

    public interface ProgressGetter {
        public void run() throws Exception;

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

    public void dispose() {
        super.dispose();

        executer.interrupt();

        try {
            executer.join(20000);
        } catch (InterruptedException e) {

        }

    }

    public JComponent layoutDialogContent() {

        JPanel p = new JPanel(new MigLayout("ins 0"));
        p.add(getTextfield(), "growx,pushx");
        final JProgressBar bar;
        p.add(bar = new JProgressBar(0, 100), "growx,pushx,newline");
        bar.setStringPainted(true);

        updater = new Timer(50, new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                if (getter != null) {
                    final int prg = getter.getProgress();
                    final String text = getter.getString();

                    bar.setValue(prg);
                    if (text == null) {
                        bar.setStringPainted(false);
                    } else {
                        bar.setStringPainted(true);
                        bar.setString(text);
                    }

                    if (prg >= 100) {
                        updater.stop();
                        dispose();
                        return;
                    }
                }
            }
        });
        updater.setRepeats(true);
        updater.setInitialDelay(50);
        updater.start();
        executer = new Thread("ProgressDialogExecuter") {
            public void run() {
                try {
                    getter.run();
                } catch (Exception e) {

                    setReturnmask(false);
                } finally {
                    dispose();
                    updater.stop();
                }

            }
        };
        executer.start();

        return p;
    }

}
