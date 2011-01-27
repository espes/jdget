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

import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JTextPane;
import javax.swing.Timer;
import javax.swing.WindowConstants;
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
public class ProgressDialog extends AbstractDialog<Integer> {
    public interface ProgressGetter {

        public int getProgress();

        public String getString();

        public void run() throws Exception;
    }

    private static final long    serialVersionUID   = -7420852517889843489L;
    private boolean              disposed;

    private Thread               executer;
    private final ProgressGetter getter;
    private final String         message;

    private JTextPane            textField;
    private Timer                updater;
    private long                 waitForTermination = 20000;

    /**
     * @param progressGetter
     * @param flags
     *            TODO
     * @param icon
     *            TODO
     * @param s
     * @param s2
     */
    public ProgressDialog(final ProgressGetter progressGetter, final int flags, final String title, final String message, final ImageIcon icon) {
        this(progressGetter, flags, title, message, icon, null, null);
    }

    public ProgressDialog(final ProgressGetter progressGetter, final int flags, final String title, final String message, final ImageIcon icon, final String ok, final String cancel) {
        super(flags | Dialog.BUTTONS_HIDE_OK, title, icon, ok, cancel);
        this.message = message;

        getter = progressGetter;

        setReturnmask(true);

    }

    /*
     * (non-Javadoc)
     * 
     * @see org.appwork.utils.swing.dialog.AbstractDialog#getRetValue()
     */
    @Override
    protected Integer createReturnValue() {
        // TODO Auto-generated method stub
        return getReturnmask();
    }

    @Override
    public void dispose() {
        if (disposed) { return; }
        System.out.println("Dispose Progressdialog");
        disposed = true;
        executer.interrupt();

        try {
            executer.join(waitForTermination);

        } catch (final InterruptedException e) {

        }
        super.dispose();

    }

    private JComponent getTextfield() {
        textField = new JTextPane();
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
            textField.setMaximumSize(new Dimension(450, 600));
        }

        textField.setText(message);
        textField.setEditable(false);
        textField.setBackground(null);
        textField.setOpaque(false);
        textField.putClientProperty("Synthetica.opaque", Boolean.FALSE);

        if (BinaryLogic.containsAll(flagMask, Dialog.STYLE_LARGE)) {
            final JScrollPane sp = new JScrollPane(textField);
            sp.setMaximumSize(new Dimension(450, 600));
            return sp;
        } else {
            return textField;
        }
    }

    public long getWaitForTermination() {
        return waitForTermination;
    }

    @Override
    public JComponent layoutDialogContent() {
        getDialog().setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
        final JPanel p = new JPanel(new MigLayout("ins 0"));

        p.add(getTextfield(), "growx,pushx");
        final JProgressBar bar;
        p.add(bar = new JProgressBar(0, 100), "growx,pushx,newline");
        bar.setStringPainted(true);

        updater = new Timer(50, new ActionListener() {

            public void actionPerformed(final ActionEvent e) {
                if (getter != null) {
                    final int prg = getter.getProgress();
                    final String text = getter.getString();

                    if (prg < 0) {
                        bar.setIndeterminate(true);

                    } else {
                        bar.setIndeterminate(false);
                        bar.setValue(prg);

                    }
                    if (text == null) {
                        bar.setStringPainted(false);
                    } else {
                        bar.setStringPainted(true);
                        bar.setString(text);
                    }

                    if (prg >= 100) {
                        updater.stop();
                        ProgressDialog.this.dispose();
                        return;
                    }
                }
            }
        });
        updater.setRepeats(true);
        updater.setInitialDelay(50);
        updater.start();
        executer = new Thread("ProgressDialogExecuter") {
            @Override
            public void run() {
                try {
                    getter.run();
                } catch (final Exception e) {

                    ProgressDialog.this.setReturnmask(false);
                } finally {
                    new EDTHelper<Object>() {

                        @Override
                        public Object edtRun() {
                            ProgressDialog.this.dispose();
                            return null;
                        }

                    }.start();

                    updater.stop();
                }

            }
        };
        executer.start();

        return p;
    }

    public void setWaitForTermination(final long waitForTermination) {
        this.waitForTermination = waitForTermination;
    }

}
