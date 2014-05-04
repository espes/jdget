/**
 * Copyright (c) 2009 - 2010 AppWork UG(haftungsbeschrÃ¤nkt) <e-mail@appwork.org>
 * 
 * This file is part of org.appwork.utils.event.queue
 * 
 * This software is licensed under the Artistic License 2.0,
 * see the LICENSE file or http://www.opensource.org/licenses/artistic-license-2.0.php
 * for details
 */
package org.appwork.utils.event.queue;

import org.appwork.utils.event.queue.Queue.QueuePriority;
import org.appwork.utils.logging.Log;

/**
 * @author daniel
 * 
 */
public abstract class QueueAction<T, E extends Throwable> {

    private Throwable        exeption;

    private volatile boolean finished         = false;
    private volatile boolean killed           = false;
    private QueuePriority    prio             = QueuePriority.NORM;
    private Queue            queue            = null;
    private T                result           = null;
    private String           callerStackTrace = null;

    private volatile boolean started          = false;

    private Thread           thread           = null;

    public QueueAction() {
    }

    public QueueAction(final QueuePriority prio) {
        this.prio = prio;
    }

    protected boolean allowAsync() {
        return false;
    }

    /**
     * @param e
     * @return
     */
    protected synchronized boolean callExceptionHandler() {
        if (this.exeption == null) { return true; }
        if (this.exeption != null && this.handleException(this.exeption)) {
            this.exeption = null;
            return true;
        }
        return false;
    }

    protected String getCallerStackTrace() {
        return this.callerStackTrace;
    }

    protected Thread getCallerThread() {
        return this.thread;
    }

    /**
     * @return the exeption
     */
    public Throwable getExeption() {
        return this.exeption;
    }

    protected Queue getQueue() {
        return this.queue;
    }

    public QueuePriority getQueuePrio() {
        return this.prio;
    }

    public T getResult() {
        return this.result;
    }

    public boolean gotKilled() {
        return this.killed;
    }

    public boolean gotStarted() {
        return this.started;
    }

    /**
     * Callback for asynchron queuecalls if exceptions occured. has to return
     * true if exception got handled
     * 
     * @param e
     * @return
     */
    public boolean handleException(final Throwable e) {
        Log.exception(e);
        return false;
    }

    public boolean isFinished() {
        return this.finished;
    }

    public void kill() {
        synchronized (this) {
            if (this.finished == true) { return; }
            this.killed = true;
            this.finished = true;
            this.notifyAll();
        }
    }

    protected void postRun() {
    }

    public void reset() {
        this.exeption = null;
        this.killed = false;
        this.finished = false;
        this.callerStackTrace = null;
        this.thread = null;
        this.queue = null;
    }

    protected abstract T run() throws E;

    public void setCallerThread(final Queue queue, final Thread thread) {
        this.thread = thread;
        this.queue = queue;
        if (queue != null && queue.isDebug() && thread != null) {
            StringBuilder sb = new StringBuilder();
            for (final StackTraceElement elem : thread.getStackTrace()) {
                sb.append(elem.toString() + "\r\n");
            }
            this.callerStackTrace = sb.toString();
            sb = null;
        }
    }

    /**
     * @param finished
     *            the finished to set
     */
    public void setFinished(final boolean finished) {
        synchronized (this) {
            this.finished = finished;
            this.notifyAll();
        }
    }

    public void setQueuePrio(final QueuePriority prio) {
        this.prio = prio;
    }

    @SuppressWarnings("unchecked")
    final public void start(final Queue queue) throws E {
        this.queue = queue;
        this.started = true;
        try {
            this.result = this.run();
        } catch (final Throwable th) {

            if (queue != null && queue.isDebug()) {
                Log.L.severe("QueueActionCallerStackTrace:\r\n" + this.callerStackTrace);
            }
            this.exeption = th;
            if (th instanceof RuntimeException) {
                throw (RuntimeException) th;
            } else {
                throw (E) th;
            }
        } finally {
            try {
                this.postRun();
            } catch (final Throwable e) {
                Log.exception(e);
            }
        }
    }
}
