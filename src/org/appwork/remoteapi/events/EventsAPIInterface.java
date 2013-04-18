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
import org.appwork.remoteapi.ApiRawJsonResponse;
import org.appwork.remoteapi.RemoteAPIInterface;
import org.appwork.remoteapi.RemoteAPIRequest;
import org.appwork.remoteapi.RemoteAPIResponse;
import org.appwork.remoteapi.events.json.PublisherResponse;
import org.appwork.remoteapi.events.json.SubscriptionResponse;

/**
 * @author daniel
 * 
 */
@ApiNamespace("events")
public interface EventsAPIInterface extends RemoteAPIInterface {

    @ApiRawJsonResponse
    public SubscriptionResponse addsubscription(long subscriptionid, String[] subscriptions, String[] exclusions);

    @ApiRawJsonResponse
    public SubscriptionResponse changesubscriptiontimeouts(long subscriptionid, long polltimeout, long maxkeepalive);

    @ApiRawJsonResponse
    public SubscriptionResponse getsubscription(long subscriptionid);

    public void listen(RemoteAPIRequest request, RemoteAPIResponse response, long subscriptionid);

    public void listen(RemoteAPIRequest request, RemoteAPIResponse response, long subscriptionid, long lasteventnumber);

    @ApiRawJsonResponse
    public ArrayList<PublisherResponse> listpublisher();

    @ApiRawJsonResponse
    public SubscriptionResponse removesubscription(long subscriptionid, String[] subscriptions, String[] exclusions);

    @ApiRawJsonResponse
    public SubscriptionResponse setsubscription(long subscriptionid, String[] subscriptions, String[] exclusions);

    @ApiRawJsonResponse
    public SubscriptionResponse subscribe(String[] subscriptions, String[] exclusions);

    @ApiRawJsonResponse
    public SubscriptionResponse unsubscribe(long subscriptionid);

}
