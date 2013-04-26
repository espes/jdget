/**
 * Copyright (c) 2009 - 2011 AppWork UG(haftungsbeschränkt) <e-mail@appwork.org>
 * 
 * This file is part of org.appwork.remoteapi
 * 
 * This software is licensed under the Artistic License 2.0,
 * see the LICENSE file or http://www.opensource.org/licenses/artistic-license-2.0.php
 * for details
 */
package org.appwork.remoteapi.events;

import java.util.ArrayList;

import org.appwork.remoteapi.ApiNamespace;
import org.appwork.remoteapi.RemoteAPIInterface;
import org.appwork.remoteapi.RemoteAPIRequest;
import org.appwork.remoteapi.RemoteAPIResponse;
import org.appwork.remoteapi.ResponseWrapper;
import org.appwork.remoteapi.events.json.PublisherResponse;
import org.appwork.remoteapi.events.json.SubscriptionResponse;
import org.appwork.remoteapi.responsewrapper.DataWrapper;

/**
 * @author daniel
 * 
 */
@ApiNamespace("events")
public interface EventsAPIInterface extends RemoteAPIInterface {

    public SubscriptionResponse addsubscription(long subscriptionid, String[] subscriptions, String[] exclusions);

    public SubscriptionResponse changesubscriptiontimeouts(long subscriptionid, long polltimeout, long maxkeepalive);

    public SubscriptionResponse getsubscription(long subscriptionid);

    @ResponseWrapper(DataWrapper.class)
    public void listen(RemoteAPIRequest request, RemoteAPIResponse response, long subscriptionid);

    @ResponseWrapper(DataWrapper.class)
    public void listen(RemoteAPIRequest request, RemoteAPIResponse response, long subscriptionid, long lasteventnumber);

    public ArrayList<PublisherResponse> listpublisher();

    public SubscriptionResponse removesubscription(long subscriptionid, String[] subscriptions, String[] exclusions);

    public SubscriptionResponse setsubscription(long subscriptionid, String[] subscriptions, String[] exclusions);

    public SubscriptionResponse subscribe(String[] subscriptions, String[] exclusions);

    public SubscriptionResponse unsubscribe(long subscriptionid);

}
