/**
 * Copyright (c) 2009 - 2013 AppWork UG(haftungsbeschränkt) <e-mail@appwork.org>
 * 
 * This file is part of org.appwork.remoteapi.events.json
 * 
 * This software is licensed under the Artistic License 2.0,
 * see the LICENSE file or http://www.opensource.org/licenses/artistic-license-2.0.php
 * for details
 */
package org.appwork.remoteapi.events.json;

import org.appwork.remoteapi.events.EventPublisher;
import org.appwork.storage.Storable;

/**
 * @author daniel
 * 
 */
public class EventObject implements Storable {
    protected String publisher   = null;
    protected String eventid     = null;
    protected Object eventdata   = null;
    protected long   eventnumber = -1;

    public EventObject(/* Storable */) {
    }

    public EventObject(final EventPublisher publisher, final String eventid, final Object eventdata) {
        this.publisher = publisher.getPublisherName();
        this.eventid = eventid;
        this.eventdata = eventdata;
    }

    @Override
    public EventObject clone() {
        final EventObject ret = new EventObject();
        ret.publisher = this.publisher;
        ret.eventid = this.eventid;
        ret.eventdata = this.eventdata;
        ret.eventnumber = this.eventnumber;
        return ret;
    }

    public Object getEventdata() {
        return this.eventdata;
    }

    public String getEventid() {
        return this.eventid;
    }

    public long getEventnumber() {
        return this.eventnumber;
    }

    public String getPublisher() {
        return this.publisher;
    }

    public void setEventdata(final Object eventdata) {
        this.eventdata = eventdata;
    }

    public void setEventid(final String eventid) {
        this.eventid = eventid;
    }

    public void setEventnumber(final long eventnumber) {
        this.eventnumber = eventnumber;
    }

    public void setPublisher(final String publisher) {
        this.publisher = publisher;
    }
}
