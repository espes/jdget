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

import org.appwork.remoteapi.events.EventObject;
import org.appwork.storage.Storable;

/**
 * @author daniel
 * 
 */
public class EventObjectStorable implements Storable {

    private final EventObject eventObject;

    public EventObjectStorable(/* Storable */) {
        this(null);
    }

    public EventObjectStorable(final EventObject eventObject) {
        this.eventObject = eventObject;
    }

    public Object getEventdata() {
        if (this.eventObject == null) { return null; }
        return this.eventObject.getEventdata();
    }

    public String getEventid() {
        if (this.eventObject == null) { return null; }
        return this.eventObject.getEventid();
    }

    public String getPublisher() {
        if (this.eventObject == null) { return null; }
        return this.eventObject.getPublisher().getPublisherName();
    }
}
