package org.jdownloader.myjdownloader.client.bindings.events.json;

import org.jdownloader.myjdownloader.client.json.AbstractJsonData;

/**
 * Copyright (c) 2009 - 2013 AppWork UG(haftungsbeschränkt) <e-mail@appwork.org>
 * 
 * This file is part of org.appwork.remoteapi.events.json
 * 
 * This software is licensed under the Artistic License 2.0,
 * see the LICENSE file or http://www.opensource.org/licenses/artistic-license-2.0.php
 * for details
 */

/**
 * @author daniel
 * 
 */
public class MyJDEvent extends AbstractJsonData{

    private Object eventData;

    public Object getEventData() {
        return eventData;
    }

    public void setEventData(final Object eventData) {
        this.eventData = eventData;
    }

    public String getEventid() {
        return eventid;
    }

    public void setEventid(final String eventid) {
        this.eventid = eventid;
    }

    public String getPublisher() {
        return publisher;
    }

    public void setPublisher(final String publisher) {
        this.publisher = publisher;
    }

    private String eventid;
    private String publisher;

    public MyJDEvent(/* Storable */) {

    }

}
