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

/**
 * @author daniel
 * 
 */
public abstract class QueueItem {

    private Object result = null;
    private Exception exce = null;
    private volatile boolean finished = false;
    private volatile boolean killed = false;
    private Queue queue = null;

    protected QueueItem getThis() {
        return this;
    }

    protected Queue getQueue() {
        return queue;
    }

    public void start(Queue queue) throws Exception {
        this.queue = queue;
        try {
            result = run();
        } catch (Exception e) {
            exce = e;
            throw e;
        } finally {
            finished = true;
        }
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
    protected void exceptionHandler(Exception e) {
    }

}
