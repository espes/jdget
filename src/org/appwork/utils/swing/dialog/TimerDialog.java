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

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.IOException;

import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;

import org.appwork.utils.ImageProvider.ImageProvider;
import org.appwork.utils.formatter.TimeFormatter;
import org.appwork.utils.locale.APPWORKUTILS;
import org.appwork.utils.logging.Log;
import org.appwork.utils.swing.EDTHelper;

public abstract class TimerDialog extends JDialog {

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

    public TimerDialog(JFrame parentframe) {
        super(parentframe, ModalityType.TOOLKIT_MODAL);
        // avoids always On Top BUg
        if (parentframe != null) {
            parentframe.setAlwaysOnTop(true);
            parentframe.setAlwaysOnTop(false);
        }
        layoutDialog();
    }

    protected void layoutDialog() {
        this.timerLbl = new JLabel(TimeFormatter.formatSeconds(Dialog.getInstance().getCoundownTime(), 0));

        this.timerLbl.addMouseListener(new MouseAdapter() {

            @Override
            public void mouseClicked(MouseEvent e) {
                cancel();
                timerLbl.removeMouseListener(this);
            }

        });
        this.timerLbl.setToolTipText(APPWORKUTILS.TIMERDIALOG_TOOLTIP_TIMERLABEL.s());

        try {
            this.timerLbl.setIcon(ImageProvider.getImageIcon("cancel", 16, 16, true));
        } catch (IOException e1) {
            Log.exception(e1);
        }

    }

    /**
     * interrupts the timer countdown
     */
    public void cancel() {
        if (timer != null) {
            timer.interrupt();
            timer = null;
            timerLbl.setEnabled(false);
        }
    }

    protected abstract void onTimeout();

    protected void initTimer(int time) {
        this.counter = time;
        timer = new Thread() {

            public void run() {
                try {
                    // sleep while dialog is invisible
                    while (!isVisible()) {
                        try {
                            Thread.sleep(200);
                        } catch (InterruptedException e) {
                            break;
                        }
                    }
                    int count = counter;
                    while (--count >= 0) {
                        if (!isVisible()) return;
                        if (timer == null) return;
                        final String left = TimeFormatter.formatSeconds(count, 0);

                        new EDTHelper<Object>() {

                            public Object edtRun() {
                                timerLbl.setText(left);
                                return null;
                            }

                        }.start();

                        Thread.sleep(1000);

                        if (counter < 0) return;
                        if (!isVisible()) return;

                    }
                    if (counter < 0) return;
                    if (!this.isInterrupted()) onTimeout();
                } catch (InterruptedException e) {
                    return;
                }
            }

        };

        timer.start();
    }

}
