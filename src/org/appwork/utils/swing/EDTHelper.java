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
    private volatile boolean done    = false;

    /**
     * flag, has runnable already started, invoked in edt
     */
    private volatile boolean started = false;
    /**
     * lock used for EDT waiting
     */
    private final Object     lock    = new Object();

    /**
     * Stores The returnvalue. This Value if of the Generic Datatype T
     */
    private T                returnValue;

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
        this.started = true;
        try {
            this.returnValue = this.edtRun();
        } catch (final Throwable e) {
            org.appwork.utils.logging.Log.exception(e);
        } finally {
            synchronized (this.lock) {
                this.done = true;
                this.lock.notify();
            }
        }

    }

    public void start() {
        this.start(false);
    }

    /**
     * starts the runnable
     * 
     * returns true in case we are in EDT or false if it got invoked later
     */
    public void start(final boolean invokeLater) {
        if (this.started) { return; }
        this.started = true;
        if (!invokeLater && SwingUtilities.isEventDispatchThread()) {
            this.run();
        } else {
            SwingUtilities.invokeLater(this);
        }
    }

    /**
     * Wait until the runnable has been finished by the EDT. If the Runnable has
     * not started yet, it gets started.
     */
    public void waitForEDT() {
        if (this.done) { return; }
        this.start(false);
        if (this.done) { return; }
        synchronized (this.lock) {
            if (this.done) { return; }
            try {
                this.lock.wait();
            } catch (final InterruptedException e) {
                org.appwork.utils.logging.Log.exception(e);
            }
        }
    }

}
