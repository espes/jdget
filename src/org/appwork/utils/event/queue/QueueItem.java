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
import org.appwork.utils.logging.Log;

/**
 * @author daniel
 * 
 */
public abstract class QueueItem {

    private Object result = null;
    private Exception exce = null;
    private volatile boolean finished = false;
    private volatile boolean killed = false;
    private volatile boolean started = false;
    private Queue queue = null;
    private QueueItem source = null;
    private QueuePriority prio = QueuePriority.NORM;

    protected Queue getQueue() {
        return queue;
    }

    public QueuePriority getQueuePrio() {
        return prio;
    }

    public void setQueuePrio(QueuePriority prio) {
        this.prio = prio;
    }

    public void setSourceQueueItem(QueueItem source) {
        this.source = source;
    }

    public QueueItem getSourceQueueItem() {
        return source;
    }

    public void start(Queue queue) throws Exception {
        this.queue = queue;
        this.started = true;
        try {
            result = run();
        } catch (Exception e) {
            Log.exception(e);
            exce = e;
            throw e;
        } finally {
            finished = true;
        }
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

    public Object getResult() {
        return result;
    }

    public Exception getException() {
        return exce;
    }

    protected abstract Object run() throws Exception;

    /**
     * override this if you want customized exceptionhandling
     * 
     * @param e
     */
    public void exceptionHandler(Exception e) {
    }

}
