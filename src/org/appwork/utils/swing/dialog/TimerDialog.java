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
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Toolkit;

import javax.swing.JDialog;
import javax.swing.JLabel;

import net.miginfocom.swing.MigLayout;

import org.appwork.utils.formatter.TimeFormatter;
import org.appwork.utils.swing.EDTHelper;
import org.appwork.utils.swing.SwingUtils;

public abstract class TimerDialog {

    protected class InternDialog extends JDialog {

        /**
         * 
         */
        private static final long serialVersionUID = 1L;

        public InternDialog() {
            super(SwingUtils.getWindowForComponent(Dialog.getInstance().getParentOwner()), ModalityType.TOOLKIT_MODAL);

            this.setLayout(new MigLayout("ins 5", "[]", "[fill,grow][]"));
            System.out.println("Dialog parent: " + this.getParent());
            if (Dialog.getInstance().getIconList() != null) {
                this.setIconImages(Dialog.getInstance().getIconList());
            }

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

        // @Override
        // public void setLayout(final LayoutManager manager) {
        // super.setLayout(manager);
        // }
    }

    private static final long serialVersionUID = -7551772010164684078L;
    /**
     * Timer Thread to count down the {@link #counter}
     */
    protected Thread          timer;
    /**
     * Current timer value
     */
    protected long            counter;
    /**
     * Label to display the timervalue
     */
    protected JLabel          timerLbl;

    protected InternDialog    dialog;

    private Dimension         preferredSize;
    private int               countdownTime    = 0;

    public TimerDialog() {
        // super(parentframe, ModalityType.TOOLKIT_MODAL);

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

    /**
     * @return
     */
    protected long getCountdown() {
        return this.getCountdownTime() > 0 ? this.getCountdownTime() : Dialog.getInstance().getCountdownTime();
    }

    public int getCountdownTime() {
        return this.countdownTime;
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
        // w = Math.min(max.width, w);
        // w = Math.max(min.width, w);
        // h = Math.min(max.height, h);
        // h = Math.max(min.height, h);

        try {
            if (this.isIgnoreSizeLimitations()) {
                return new Dimension(Math.min(Toolkit.getDefaultToolkit().getScreenSize().width, w), Math.min(Toolkit.getDefaultToolkit().getScreenSize().height, h));

            } else {
                if (this.getDialog().getParent().isVisible()) {
                    return new Dimension(Math.min(this.getRoot().getWidth(), w), Math.min(this.getRoot().getHeight(), h));
                } else {
                    return new Dimension(Math.min((int) (Toolkit.getDefaultToolkit().getScreenSize().width * 0.75), w), Math.min((int) (Toolkit.getDefaultToolkit().getScreenSize().height * 0.75), h));

                }
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

    /**
     * @return
     */
    private Container getRoot() {
        Container ret = this.getDialog().getParent();
        Container p;
        while ((p = ret.getParent()) != null) {
            ret = p;
        }
        return ret;
    }

    protected void initTimer(final long time) {
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
                    long count = TimerDialog.this.counter;
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
     * Override this method to allow dialogs beeing larger than their parents
     * 
     * @return
     */
    protected boolean isIgnoreSizeLimitations() {
        return false;
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

        this.timerLbl = new JLabel(TimeFormatter.formatSeconds(this.getCountdown(), 0));

    }

    protected abstract void onTimeout();

    public void pack() {
        this.getDialog().pack();

        this.getDialog().setMinimumSize(this.getDialog().getPreferredSize());

    }

    public void requestFocus() {
        this.getDialog().requestFocus();
    }

    protected void setAlwaysOnTop(final boolean b) {
        this.getDialog().setAlwaysOnTop(b);
    }

    public void setCountdownTime(final int countdownTime) {
        this.countdownTime = countdownTime;
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
