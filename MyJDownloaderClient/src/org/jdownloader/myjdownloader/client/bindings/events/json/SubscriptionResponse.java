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
public class SubscriptionResponse {

    protected long     subscriptionid = -1;

    protected boolean  subscribed     = false;

    protected String[] subscriptions  = null;
    protected String[] exclusions     = null;

    protected long     maxPolltimeout = -1;
    protected long     maxKeepalive   = -1;

    public SubscriptionResponse(/* Storable */) {
    }

    public String[] getExclusions() {
        return this.exclusions;
    }

    public long getMaxKeepalive() {
        return this.maxKeepalive;
    }

    /**
     * @return the maxPolltimeout
     */
    public long getMaxPolltimeout() {
        return this.maxPolltimeout;
    }

    /**
     * @return the subscriptionid
     */
    public long getSubscriptionid() {
        return this.subscriptionid;
    }

    /**
     * @return the subscriptions
     */
    public String[] getSubscriptions() {
        return this.subscriptions;
    }

    /**
     * @return the subscribed
     */
    public boolean isSubscribed() {
        return this.subscribed;
    }

    public void setExclusions(final String[] exclusions) {
        this.exclusions = exclusions;
    }

    public void setMaxKeepalive(final long maxKeepalive) {
        this.maxKeepalive = maxKeepalive;
    }

    /**
     * @param maxPolltimeout
     *            the maxPolltimeout to set
     */
    public void setMaxPolltimeout(final long maxPolltimeout) {
        this.maxPolltimeout = maxPolltimeout;
    }

    /**
     * @param subscribed
     *            the subscribed to set
     */
    public void setSubscribed(final boolean subscribed) {
        this.subscribed = subscribed;
    }

    /**
     * @param subscriptionid
     *            the subscriptionid to set
     */
    public void setSubscriptionid(final long subscriptionid) {
        this.subscriptionid = subscriptionid;
    }

    /**
     * @param subscriptions
     *            the subscriptions to set
     */
    public void setSubscriptions(final String[] subscriptions) {
        this.subscriptions = subscriptions;
    }
}
