/**
 * Copyright (c) 2009 - 2011 AppWork UG(haftungsbeschränkt) <e-mail@appwork.org>
 * 
 * This file is part of org.appwork.remoteapi.test
 * 
 * This software is licensed under the Artistic License 2.0,
 * see the LICENSE file or http://www.opensource.org/licenses/artistic-license-2.0.php
 * for details
 */
package org.appwork.remoteapi.test;

import org.appwork.remoteapi.EventsAPIEvent;
import org.appwork.remoteapi.RemoteAPIProcess;
import org.appwork.utils.net.httpserver.session.HttpSession;
import org.appwork.utils.net.httpserver.test.serverTest;

/**
 * @author daniel
 * 
 */
public class CounterProcess extends RemoteAPIProcess<Boolean> implements CounterProcessInterface {

    private int         counter = 0;
    private boolean     stopped = false;
    private HttpSession session;

    public int getCounterValue() {
        return this.counter;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.appwork.remoteapi.RemoteAPIProcess#getResponse()
     */
    @Override
    protected Boolean getResponse() {
        // TODO Auto-generated method stub
        return null;
    }

    public int getValue() {
        return this.counter;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.appwork.remoteapi.RemoteAPIProcess#process()
     */
    @Override
    public void process() {
        while (!this.stopped) {
            try {
                Thread.sleep(1000);
            } catch (final InterruptedException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            final EventsAPIEvent event = new EventsAPIEvent();
            event.setProcessID(this.getPID());
            event.setData(this.counter);
            serverTest.events.publishEvent(event, this.session);
            this.counter++;
            if (this.counter > 400) {
                break;
            }
        }
    }

    public void setSession(final HttpSession session) {
        this.session = session;
    }

    public boolean stopCounter() {
        this.stopped = true;
        return true;
    }

}
