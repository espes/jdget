package org.jdownloader.myjdownloader.client.bindings.events.json;

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
public class PublisherResponse {

    protected String   publisher = null;
    protected String[] eventids  = null;

    public PublisherResponse(/* Storable */) {
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
