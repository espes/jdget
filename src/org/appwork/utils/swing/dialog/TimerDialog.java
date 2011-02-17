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

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.Window;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.IOException;

import javax.swing.JDialog;
import javax.swing.JLabel;

import org.appwork.utils.ImageProvider.ImageProvider;
import org.appwork.utils.formatter.TimeFormatter;
import org.appwork.utils.locale.APPWORKUTILS;
import org.appwork.utils.logging.Log;
import org.appwork.utils.swing.EDTHelper;

public abstract class TimerDialog {

    protected class InternDialog extends JDialog {

        /**
         * 
         */
        private static final long serialVersionUID = 1L;

        public InternDialog() {
            super(TimerDialog.this.parentFrame, ModalityType.TOOLKIT_MODAL);

        }

        @Override
        public void dispose() {
            TimerDialog.this.dispose();
            super.dispose();
        }

        @Override
        public Dimension getPreferredSize() {
            return TimerDialog.this.getPreferredSize();

        }

        public Dimension getRealPreferredSize() {
            return super.getPreferredSize();

        }

        /**
         * 
         */
        public void realDispose() {
            super.dispose();

        }
    }

    private static final long serialVersionUID = -7551772010164684078L;
    /**
     * Timer Thread to count down the {@link #counter}
     */
    protected Thread          timer;
    /**
     * Current timer value
     */
    protected int             counter;
    /**
     * Label to display the timervalue
     */
    protected JLabel          timerLbl;
    protected Window          parentFrame;

    private InternDialog      dialog;

    private Dimension         preferredSize;

    public TimerDialog(final Window parentframe) {
        // super(parentframe, ModalityType.TOOLKIT_MODAL);
        this.parentFrame = parentframe;

    }

    /**
     * interrupts the timer countdown
     */
    public void cancel() {
        if (this.timer != null) {
            this.timer.interrupt();
            this.timer = null;
            this.timerLbl.setEnabled(false);
        }
    }

    /**
     * 
     */
    protected void dispose() {
        this.getDialog().realDispose();

    }

    /**
     * @return
     */
    protected Color getBackground() {
        // TODO Auto-generated method stub
        return this.getDialog().getBackground();
    }

    protected InternDialog getDialog() {
        if (this.dialog == null) { throw new NullPointerException("Call #org.appwork.utils.swing.dialog.AbstractDialog.displayDialog() first"); }
        return this.dialog;
    }

    /**
     * override this if you want to set a special height
     * 
     * @return
     */
    protected int getPreferredHeight() {
        // TODO Auto-generated method stub
        return -1;
    }

    /**
     * @return
     */
    public Dimension getPreferredSize() {
        final Dimension pref = this.getDialog().getRealPreferredSize();
        int w = this.getPreferredWidth();
        int h = this.getPreferredHeight();
        if (w <= 0) {
            w = pref.width;
        }
        if (h <= 0) {
            h = pref.height;
        }
        try {
            if (Dialog.getInstance().getParentOwner() != null && Dialog.getInstance().getParentOwner().isVisible()) {
                return new Dimension(Math.min(Dialog.getInstance().getParentOwner().getWidth(), w), Math.min(Dialog.getInstance().getParentOwner().getHeight(), h));
            } else {
                return new Dimension(Math.min((int) (Toolkit.getDefaultToolkit().getScreenSize().width * 0.75), w), Math.min((int) (Toolkit.getDefaultToolkit().getScreenSize().height * 0.75), h));

            }
        } catch (final Throwable e) {
            return pref;
        }
    }

    /**
     * overwride this to set a special width
     * 
     * @return
     */
    protected int getPreferredWidth() {
        // TODO Auto-generated method stub
        return -1;
    }

    protected void initTimer(final int time) {
        this.counter = time;
        this.timer = new Thread() {

            @Override
            public void run() {
                try {
                    // sleep while dialog is invisible
                    while (!TimerDialog.this.isVisible()) {
                        try {
                            Thread.sleep(200);
                        } catch (final InterruptedException e) {
                            break;
                        }
                    }
                    int count = TimerDialog.this.counter;
                    while (--count >= 0) {
                        if (!TimerDialog.this.isVisible()) { return; }
                        if (TimerDialog.this.timer == null) { return; }
                        final String left = TimeFormatter.formatSeconds(count, 0);

                        new EDTHelper<Object>() {

                            @Override
                            public Object edtRun() {
                                TimerDialog.this.timerLbl.setText(left);
                                return null;
                            }

                        }.start();

                        Thread.sleep(1000);

                        if (TimerDialog.this.counter < 0) { return; }
                        if (!TimerDialog.this.isVisible()) { return; }

                    }
                    if (TimerDialog.this.counter < 0) { return; }
                    if (!this.isInterrupted()) {
                        TimerDialog.this.onTimeout();
                    }
                } catch (final InterruptedException e) {
                    return;
                }
            }

        };

        this.timer.start();
    }

    /**
     * @return
     */
    protected boolean isVisible() {
        // TODO Auto-generated method stub
        return this.getDialog().isVisible();
    }

    protected void layoutDialog() {
        this.dialog = new InternDialog();
        if (this.preferredSize != null) {
            this.dialog.setPreferredSize(this.preferredSize);
        }
        this.timerLbl = new JLabel(TimeFormatter.formatSeconds(Dialog.getInstance().getCountdownTime(), 0));

        this.timerLbl.addMouseListener(new MouseAdapter() {

            @Override
            public void mouseClicked(final MouseEvent e) {
                TimerDialog.this.cancel();
                TimerDialog.this.timerLbl.removeMouseListener(this);
            }

        });
        this.timerLbl.setToolTipText(APPWORKUTILS.TIMERDIALOG_TOOLTIP_TIMERLABEL.s());

        try {
            this.timerLbl.setIcon(ImageProvider.getImageIcon("cancel", 16, 16, true));
        } catch (final IOException e1) {
            Log.exception(e1);
        }

    }

    protected abstract void onTimeout();

    public void pack() {
        this.getDialog().pack();
    }

    public void requestFocus() {
        this.getDialog().requestFocus();
    }

    protected void setAlwaysOnTop(final boolean b) {
        this.getDialog().setAlwaysOnTop(b);
    }

    protected void setDefaultCloseOperation(final int doNothingOnClose) {
        this.getDialog().setDefaultCloseOperation(doNothingOnClose);
    }

    protected void setMinimumSize(final Dimension dimension) {
        this.getDialog().setMinimumSize(dimension);
    }

    /**
     * @param dimension
     */
    public void setPreferredSize(final Dimension dimension) {
        try {
            this.getDialog().setPreferredSize(dimension);
        } catch (final NullPointerException e) {
            this.preferredSize = dimension;
        }
    }

    protected void setResizable(final boolean b) {
        this.getDialog().setResizable(b);
    }

    /**
     * @param b
     */
    public void setVisible(final boolean b) {
        this.getDialog().setVisible(b);
    }

}
