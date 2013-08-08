/**
 * Copyright (c) 2009 - 2013 AppWork UG(haftungsbeschränkt) <e-mail@appwork.org>
 * 
 * This file is part of org.appwork.utils.swing.dialog
 * 
 * This software is licensed under the Artistic License 2.0,
 * see the LICENSE file or http://www.opensource.org/licenses/artistic-license-2.0.php
 * for details
 */
package org.appwork.utils.swing.dialog;

import java.awt.event.ActionEvent;
import java.util.concurrent.atomic.AtomicLong;

import org.appwork.uio.UIOManager;
import org.appwork.utils.formatter.TimeFormatter;
import org.appwork.utils.swing.EDTHelper;

/**
 * @author daniel
 * 
 */
public class AbstractTimerThread extends Thread {

    public static void main(final String[] args) throws DialogClosedException, DialogCanceledException {
        Dialog.getInstance().setDefaultTimeout(30000);
        Dialog.getInstance().showDialog(new ConfirmDialog(UIOManager.LOGIC_COUNTDOWN, "TimerTest", "TimerTest", null, "close", "reset") {
            /*
             * (non-Javadoc)
             * 
             * @see
             * org.appwork.utils.swing.dialog.AbstractDialog#actionPerformed
             * (java.awt.event.ActionEvent)
             */
            @Override
            public void actionPerformed(final ActionEvent e) {
                if (e.getSource() == this.okButton) {
                    super.actionPerformed(e);
                } else {
                    this.resetTimer();
                }
            }
        });
    }

    private final AbstractDialog<?> dialog;

    private final AtomicLong        counter = new AtomicLong(0);

    public AbstractTimerThread(final AbstractDialog<?> dialog) {
        this.dialog = dialog;
        this.reset();
        this.setDaemon(true);
        this.setName("DialogTimer: " + dialog);
    }

    protected boolean isCurrentTimer() {
        return this.dialog.getTimer().get() == this;
    }

    protected boolean isVisible() {
        return Boolean.TRUE.equals(new EDTHelper<Boolean>() {

            @Override
            public Boolean edtRun() {
                return AbstractTimerThread.this.dialog.isVisible();
            }
        }.getReturnValue());
    }

    public void reset() {
        final long timeout = this.dialog.getCountdown();
        if (timeout <= 0) { throw new IllegalArgumentException("timeout is invalid " + timeout); }
        this.counter.set(timeout);
    }

    @Override
    public void run() {
        try {
            // sleep while dialog is invisible
            while (!this.isVisible() && this.isCurrentTimer()) {
                try {
                    Thread.sleep(200);
                } catch (final InterruptedException e) {
                    return;
                }
            }
            long currentTimeout = this.counter.get();
            while (currentTimeout >= 0 && this.isCurrentTimer() && this.isVisible()) {
                final String left = TimeFormatter.formatMilliSeconds(currentTimeout, 0);
                new EDTHelper<Object>() {

                    @Override
                    public Object edtRun() {
                        AbstractTimerThread.this.dialog.timerLbl.setText(left);
                        return null;
                    }

                }.start();
                Thread.sleep(1000);
                if (this.counter.compareAndSet(currentTimeout, currentTimeout - 1000)) {
                    currentTimeout = currentTimeout - 1000;
                } else {
                    currentTimeout = this.counter.get();
                }
            }
            if (this.isCurrentTimer() && this.isVisible()) {
                if (!this.isInterrupted()) {
                    this.dialog.onTimeout();
                }
            }
        } catch (final InterruptedException e) {
            return;
        } finally {
            this.dialog.getTimer().compareAndSet(AbstractTimerThread.this, null);
        }
    }
}
