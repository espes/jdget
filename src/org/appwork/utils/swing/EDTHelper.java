/**
 * Copyright (c) 2009 - 2010 AppWork UG(haftungsbeschränkt) <e-mail@appwork.org>
 * 
 * This file is part of org.appwork.utils.swing
 * 
 * This software is licensed under the Artistic License 2.0,
 * see the LICENSE file or http://www.opensource.org/licenses/artistic-license-2.0.php
 * for details
 */
package org.appwork.utils.swing;

import javax.swing.SwingUtilities;

import org.appwork.utils.event.queue.QueueActionRunnable;

/**
 * This class should be used to run gui code in the edt and return the generic
 * datatype to the parent thread.
 * 
 * Implement edtRun to asure Thread safe executio of this gui code.
 * 
 * @author $Author: Thomas$
 * 
 * @param <T>
 */

public abstract class EDTHelper<T> extends QueueActionRunnable {
    /**
     * flag. If Runnable has terminated yet
     */
    private boolean done = false;

    /**
     * flag, has runnable already started, invoked in edt
     */
    private boolean started = false;
    /**
     * lock used for EDT waiting
     */
    private Object lock = new Object();

    /**
     * Stores The returnvalue. This Value if of the Generic Datatype T
     */
    private T returnValue;

    /**
     * Implement this method. Gui code should be used ONLY in this Method.
     * 
     * @return
     */
    public abstract T edtRun();

    /**
     * Call this method if you want to wait for the EDT to finish the runnable.
     * It is assured that the returnvalue is available after this methjod has
     * returned.
     * 
     * @return
     */
    public T getReturnValue() {
        waitForEDT();
        return returnValue;
    }

    /**
     * Run the runnable
     */
    public void run() {
        synchronized (lock) {
            started = true;
        }
        try {
            this.returnValue = this.edtRun();
        } catch (Exception e) {
            org.appwork.utils.logging.Log.exception(e);
        }
        synchronized (lock) {
            lock.notify();
        }
        done = true;
    }

    /**
     * starts the runnable
     * 
     * returns true in case we are in EDT or false if it got invoked later
     */
    public boolean start(boolean invokeLater) {
        synchronized (lock) {
            started = true;
        }
        if (!invokeLater && SwingUtilities.isEventDispatchThread()) {
            run();
            return true;
        } else {
            SwingUtilities.invokeLater(this);
            return false;
        }
    }

    public boolean start() {
        return start(false);
    }

    /**
     * Wait until the runnable has been finished by the EDT. If the Runnable has
     * not started yet, it gets started.
     */
    public void waitForEDT() {
        if (done) return;
        boolean waitForFinish = true;
        synchronized (lock) {
            if (started == false) {
                waitForFinish = !start(false);
            }
        }
        if (waitForFinish) {
            while (!done) {
                synchronized (lock) {
                    try {
                        lock.wait(1000);
                    } catch (InterruptedException e) {
                    }
                }
            }
        }
    }

}
