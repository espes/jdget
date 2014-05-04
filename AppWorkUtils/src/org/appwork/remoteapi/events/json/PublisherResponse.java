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
public class PublisherResponse implements Storable {

    protected String   publisher = null;
    protected String[] eventids  = null;

    public PublisherResponse(/* Storable */) {
    }

    public PublisherResponse(final EventPublisher publisher) {
        this.publisher = publisher.getPublisherName();
        this.eventids = publisher.getPublisherEventIDs();
    }

    /**
     * @return the eventids
     */
    public String[] getEventids() {
        return this.eventids;
    }

    /**
     * @return the publisher
     */
    public String getPublisher() {
        return this.publisher;
    }

    /**
     * @param eventids
     *            the eventids to set
     */
    public void setEventids(final String[] eventids) {
        this.eventids = eventids;
    }

    /**
     * @param publisher
     *            the publisher to set
     */
    public void setPublisher(final String publisher) {
        this.publisher = publisher;
    }
}
