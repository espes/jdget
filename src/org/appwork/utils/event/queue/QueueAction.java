/**
 * Copyright (c) 2009 - 2010 AppWork UG(haftungsbeschränkt) <e-mail@appwork.org>
 * 
 * This file is part of org.appwork.utils.event.queue
 * 
 * This software is licensed under the Artistic License 2.0,
 * see the LICENSE file or http://www.opensource.org/licenses/artistic-license-2.0.php
 * for details
 */
package org.appwork.utils.event.queue;

import org.appwork.utils.event.queue.Queue.QueuePriority;

/**
 * @author daniel
 * 
 */
public abstract class QueueAction<T, E extends Throwable> {

    private T result = null;

    private volatile boolean finished = false;
    private volatile boolean killed = false;
    private volatile boolean started = false;
    private Queue queue = null;    
    private QueuePriority prio = QueuePriority.NORM;

    private Throwable exeption;

    private Thread thread = null;

    public QueueAction() {
    }

    public QueueAction(QueuePriority prio) {
        this.prio = prio;
    }

    protected Queue getQueue() {
        return queue;
    }

    public QueuePriority getQueuePrio() {
        return prio;
    }

    public void setQueuePrio(QueuePriority prio) {
        this.prio = prio;
    }

    @SuppressWarnings("unchecked")
    final public void start(Queue queue) throws E {
        this.queue = queue;
        this.started = true;
        try {
            result = run();
        } catch (Throwable th) {
            exeption = th;
            if (th instanceof RuntimeException) {
                throw (RuntimeException) th;
            } else {
                throw (E) th;
            }
        } finally {
            finished = true;
        }
    }

    /**
     * @return the exeption
     */
    public Throwable getExeption() {
        return exeption;
    }

    public boolean gotStarted() {
        return started;
    }

    public void kill() {
        if (finished == true) return;
        killed = true;
        finished = true;
    }

    public boolean gotKilled() {
        return killed;
    }

    public boolean isFinished() {
        return finished;
    }

    public T getResult() {
        return result;
    }

    protected abstract T run() throws E;

    /**
     * Callback for asynchron queuecalls if exceptions occured. has to return
     * true if exception got handled
     * 
     * @param e
     * @return
     */
    public boolean exceptionHandler(Throwable e) {
        return false;
    }

    protected Thread getCallerThread() {
        return thread;
    }

    protected void setCallerThread(Thread thread) {
        this.thread = thread;
    }
}
