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

    private boolean              disposed;

    private Thread               executer;
    private final ProgressGetter getter;
    private final String         message;

    private JTextPane            textField;
    private Timer                updater;
    private long                 waitForTermination = 20000;
    protected Throwable          throwable          = null;

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
        if (progressGetter == null && this instanceof ProgressGetter) {
            this.getter = (ProgressGetter) this;
        } else {
            this.getter = progressGetter;
        }
        this.setReturnmask(true);

    }

    /*
     * (non-Javadoc)
     * 
     * @see org.appwork.utils.swing.dialog.AbstractDialog#getRetValue()
     */
    @Override
    protected Integer createReturnValue() {
        // TODO Auto-generated method stub
        return this.getReturnmask();
    }

    @Override
    public void dispose() {
        if (this.disposed) { return; }
        System.out.println("Dispose Progressdialog");
        this.disposed = true;
        this.executer.interrupt();

        try {
            this.executer.join(this.waitForTermination);

        } catch (final InterruptedException e) {

        }
        super.dispose();

    }

    private JComponent getTextfield() {
        this.textField = new JTextPane();
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
            this.textField.setMaximumSize(new Dimension(450, 600));
        }

        this.textField.setText(this.message);
        this.textField.setEditable(false);
        this.textField.setBackground(null);
        this.textField.setOpaque(false);
        this.textField.putClientProperty("Synthetica.opaque", Boolean.FALSE);

        if (BinaryLogic.containsAll(this.flagMask, Dialog.STYLE_LARGE)) {
            final JScrollPane sp = new JScrollPane(this.textField);
            sp.setMaximumSize(new Dimension(450, 600));
            return sp;
        } else {
            return this.textField;
        }
    }

    /**
     * @return the throwable
     */
    public Throwable getThrowable() {
        return this.throwable;
    }

    public long getWaitForTermination() {
        return this.waitForTermination;
    }

    @Override
    public JComponent layoutDialogContent() {
        this.getDialog().setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
        final JPanel p = new JPanel(new MigLayout("ins 0"));

        p.add(this.getTextfield(), "growx,pushx");
        final JProgressBar bar;
        p.add(bar = new JProgressBar(0, 100), "growx,pushx,newline");
        bar.setStringPainted(true);

        this.updater = new Timer(50, new ActionListener() {

            public void actionPerformed(final ActionEvent e) {
                if (ProgressDialog.this.getter != null) {
                    final int prg = ProgressDialog.this.getter.getProgress();
                    final String text = ProgressDialog.this.getter.getString();

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
                        ProgressDialog.this.updater.stop();
                        ProgressDialog.this.dispose();
                        return;
                    }
                }
            }
        });
        this.updater.setRepeats(true);
        this.updater.setInitialDelay(50);
        this.updater.start();
        this.executer = new Thread("ProgressDialogExecuter") {

            @Override
            public void run() {
                try {
                    ProgressDialog.this.getter.run();
                } catch (final Throwable e) {
                    ProgressDialog.this.throwable = e;
                    e.printStackTrace();
                    ProgressDialog.this.setReturnmask(false);
                } finally {
                    new EDTHelper<Object>() {

                        @Override
                        public Object edtRun() {
                            ProgressDialog.this.dispose();
                            return null;
                        }

                    }.start();

                    ProgressDialog.this.updater.stop();
                }

            }
        };
        this.executer.start();

        return p;
    }

    public void setWaitForTermination(final long waitForTermination) {
        this.waitForTermination = waitForTermination;
    }

}
