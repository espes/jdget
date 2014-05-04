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
public class SubscriptionStatusResponse {
    protected long    subscriptionid = -1;

    protected boolean subscribed     = false;

    protected int     queueSize      = 0;

    public SubscriptionStatusResponse(/* Storable */) {
    }

    public int getQueueSize() {
        return this.queueSize;
    }

    public long getSubscriptionid() {
        return this.subscriptionid;
    }

    public boolean isSubscribed() {
        return this.subscribed;
    }

    public void setQueueSize(final int queueSize) {
        this.queueSize = queueSize;
    }

    public void setSubscribed(final boolean subscribed) {
        this.subscribed = subscribed;
    }

    public void setSubscriptionid(final long subscriptionid) {
        this.subscriptionid = subscriptionid;
    }
}
