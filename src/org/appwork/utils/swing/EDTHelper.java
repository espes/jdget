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

public abstract class EDTHelper<T> implements Runnable {
    /**
     * flag. If Runnable has terminated yet
     */
    private boolean      done    = false;

    /**
     * flag, has runnable already started, invoked in edt
     */
    private boolean      started = false;
    /**
     * lock used for EDT waiting
     */
    private final Object lock    = new Object();

    /**
     * Stores The returnvalue. This Value if of the Generic Datatype T
     */
    private T            returnValue;

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
        this.waitForEDT();
        return this.returnValue;
    }

    /**
     * Run the runnable
     */
    public void run() {
        synchronized (this.lock) {
            this.started = true;
        }
        try {
            this.returnValue = this.edtRun();
        } catch (final Exception e) {
            org.appwork.utils.logging.Log.exception(e);
        }
        synchronized (this.lock) {
            this.lock.notify();
        }
        this.done = true;
    }

    public boolean start() {
        return this.start(false);
    }

    /**
     * starts the runnable
     * 
     * returns true in case we are in EDT or false if it got invoked later
     */
    public boolean start(final boolean invokeLater) {
        synchronized (this.lock) {
            this.started = true;
        }
        if (!invokeLater && SwingUtilities.isEventDispatchThread()) {
            this.run();
            return true;
        } else {
            SwingUtilities.invokeLater(this);
            return false;
        }
    }

    /**
     * Wait until the runnable has been finished by the EDT. If the Runnable has
     * not started yet, it gets started.
     */
    public void waitForEDT() {
        if (this.done) { return; }
        boolean waitForFinish = true;
        synchronized (this.lock) {
            if (this.started == false) {
                waitForFinish = !this.start(false);
            }
        }
        if (waitForFinish) {
            while (!this.done) {
                synchronized (this.lock) {
                    try {
                        this.lock.wait(1000);
                    } catch (final InterruptedException e) {
                    }
                }
            }
        }
    }

}
