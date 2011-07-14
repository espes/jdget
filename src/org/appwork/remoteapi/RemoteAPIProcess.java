/**
 * Copyright (c) 2009 - 2011 AppWork UG(haftungsbeschränkt) <e-mail@appwork.org>
 * 
 * This file is part of org.appwork.remoteapi
 * 
 * This software is licensed under the Artistic License 2.0,
 * see the LICENSE file or http://www.opensource.org/licenses/artistic-license-2.0.php
 * for details
 */
package org.appwork.remoteapi;

import java.util.concurrent.atomic.AtomicLong;

/**
 * @author daniel
 * 
 */
public abstract class RemoteAPIProcess<T> implements Runnable, RemoteApiProcessInterface {

    private boolean           isRunning  = false;
    private boolean           isFinished = false;

    private static AtomicLong pid        = new AtomicLong();

    private final String      pidString  = this.getClass().getSimpleName() + "_" + RemoteAPIProcess.pid.incrementAndGet() + Math.abs((System.currentTimeMillis() + "_" + RemoteAPIProcess.pid.incrementAndGet()).hashCode());
    private RemoteAPI         remoteAPI;

    /**
     * @return the pidString
     */
    public String getPID() {
        return this.pidString;
    }

    protected abstract T getResponse();

    protected synchronized boolean isFinished() {
        return this.isFinished;
    }

    public boolean isRunning() {
        return this.isRunning;
    }

    public abstract void process();

    @Override
    public void run() {
        this.isRunning = true;
        try {
            this.process();
        } finally {
            synchronized (this) {
                this.isRunning = false;
                this.isFinished = true;
                try {
                    this.remoteAPI.unregisterProcess(this);
                } catch (final Throwable e) {
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * @param remoteAPI
     */
    protected synchronized void setRemoteAPI(final RemoteAPI remoteAPI) {
        this.remoteAPI = remoteAPI;
    }

}
