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

import java.util.logging.Level;

import javax.swing.SwingUtilities;

import org.appwork.utils.logging.Log;

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
    private volatile boolean     done                  = false;

    /**
     * flag, has runnable already started, invoked in edt
     */
    private volatile boolean     started               = false;
    /**
     * lock used for EDT waiting
     */
    private final Object         lock                  = new Object();

    private volatile boolean     callThreadInterrupted = false;
    private Thread               callThread            = null;
    private InterruptedException iException            = null;

    /**
     * Stores The returnvalue. This Value if of the Generic Datatype T
     */
    private T                    returnValue;

    private RuntimeException     exception;

    private Error                error;

    /**
     * Implement this method. Gui code should be used ONLY in this Method.
     * 
     * @return
     */
    public abstract T edtRun();

    public InterruptedException getInterruptException() {
        return this.iException;
    }

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

    public boolean isInterrupted() {
        return this.iException != null;
    }

    /**
     * Run the runnable
     */
    public void run() {
        this.started = true;
        try {
            this.returnValue = this.edtRun();
        } catch (final RuntimeException e) {
            this.exception = e;
            Log.exception(e);
        } catch (final Error e) {
            this.error = e;
            Log.exception(e);
        } finally {
            synchronized (this.lock) {
                final Thread lcallThread = this.callThread;
                if (lcallThread != null) {
                    this.callThreadInterrupted = true;
                }
                this.done = true;
                if (lcallThread != null) {
                    lcallThread.interrupt();
                }
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
        // long c = -1;
        try {
            synchronized (this.lock) {
                if (this.done) { return; }
            }
            this.start(false);
            synchronized (this.lock) {
                if (this.done) { return; }
                if (!SwingUtilities.isEventDispatchThread()) {
                    this.callThread = Thread.currentThread();
                }
            }
            // c = System.currentTimeMillis();
            while (this.done == false) {
                try {
                    Thread.sleep(1000);
                } catch (final InterruptedException e) {
                    if (!this.done && !this.callThreadInterrupted) {
                        this.iException = e;
                        org.appwork.utils.logging.Log.exception(Level.WARNING, e);
                    }
                    return;
                }
            }
        } finally {
            // if (c != -1 && System.currentTimeMillis() - c > 1000) {
            // new
            // WTFException("EDT blocked longer than 1sec!").printStackTrace();
            // }
            /* make sure we remove the interrupted flag */
            synchronized (this.lock) {
                if (this.callThreadInterrupted) {
                    Thread.interrupted();
                } else {
                    // System.out.println("not interrupted");
                }
                this.callThread = null;
            }
            if (this.exception != null) { throw this.exception; }
            if (this.error != null) { throw this.error; }
        }
    }

}
