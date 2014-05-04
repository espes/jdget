/**
 * Copyright (c) 2009 - 2013 AppWork UG(haftungsbeschränkt) <e-mail@appwork.org>
 * 
 * This file is part of org.appwork.remoteapi.events.json
 * 
 * This software is licensed under the Artistic License 2.0,
 * see the LICENSE file or http://www.opensource.org/licenses/artistic-license-2.0.php
 * for details
 */
package org.appwork.remoteapi.events;


/**
 * @author daniel
 * 
 */
public class SimpleEventObject implements EventObject {
    protected final EventPublisher publisher;
    protected final String         eventid;
    protected final Object         eventdata;
    protected final String         collapseKey;
    protected long                 eventnumber = -1;

    public SimpleEventObject(final EventPublisher publisher, final String eventid) {
        this(publisher, eventid, null, null);
    }

    public SimpleEventObject(final EventPublisher publisher, final String eventid, final Object eventdata) {
        this(publisher, eventid, eventdata, null);
    }

    public SimpleEventObject(final EventPublisher publisher, final String eventid, final Object eventdata, final String collapseKey) {
        this.publisher = publisher;
        this.eventid = eventid;
        this.eventdata = eventdata;
        this.collapseKey = collapseKey;
    }

    public SimpleEventObject(final EventPublisher publisher, final String eventid, final String collapseKey) {
        this(publisher, eventid, null, collapseKey);
    }

    public String getCollapseKey() {
        return this.collapseKey;
    }

    public Object getEventdata() {
        return this.eventdata;
    }

    public String getEventid() {
        return this.eventid;
    }

    public EventPublisher getPublisher() {
        return this.publisher;
    }

}
