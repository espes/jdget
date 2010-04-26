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
    private boolean done = false;
    /**
     * lock used for EDT waiting
     */
    private Object lock = new Object();
    /**
     * Stores The returnvalue. This Value if of the Generic Datatype T
     */
    private T returnValue;

    /**
     * flag. If Runnable has started yet
     */
    private boolean started = false;

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
     * @return the {@link EDTHelper#started}
     * @see EDTHelper#started
     */
    public boolean isStarted() {
        return started;
    }

    /**
     * Run the runnable
     */
    public void run() {
        try {
            this.returnValue = this.edtRun();
        } catch (Exception e) {
            e.printStackTrace();
        }
        synchronized (lock) {
            lock.notify();
            lock = null;
        }
    }

    /**
     * @param started
     *            the {@link EDTHelper#started} to set
     * @see EDTHelper#started
     */
    public void setStarted(boolean started) {
        this.started = started;
    }

    /**
     * Startes the Runnable and enqueues it to the EDT.
     */
    public void start() {
        setStarted(true);
        if (SwingUtilities.isEventDispatchThread()) {
            run();
        } else {
            SwingUtilities.invokeLater(this);
        }
    }

    public void invokeLater() {
        setStarted(true);
        SwingUtilities.invokeLater(this);
    }

    /**
     * Wait until the runnable has been finished by the EDT. If the Runnable has
     * not started yet, it gets started.
     */
    public void waitForEDT() {
        if (done) return;
        if (!isStarted()) start();
        if (!SwingUtilities.isEventDispatchThread()) {
            if (lock != null) {
                synchronized (lock) {
                    try {
                        if (lock != null) {
                            lock.wait();
                        }
                    } catch (InterruptedException e) {
                    }
                }
            }
        }
        done = true;
    }

}
