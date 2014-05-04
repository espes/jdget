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

import java.util.List;

import org.appwork.remoteapi.RemoteAPIInterface;
import org.appwork.remoteapi.RemoteAPIRequest;
import org.appwork.remoteapi.RemoteAPIResponse;
import org.appwork.remoteapi.ResponseWrapper;
import org.appwork.remoteapi.annotations.ApiNamespace;
import org.appwork.remoteapi.events.json.PublisherResponse;
import org.appwork.remoteapi.events.json.SubscriptionResponse;
import org.appwork.remoteapi.events.json.SubscriptionStatusResponse;
import org.appwork.remoteapi.exceptions.APIFileNotFoundException;
import org.appwork.remoteapi.exceptions.InternalApiException;
import org.appwork.remoteapi.responsewrapper.RawJSonWrapper;

/**
 * @author daniel
 * 
 */
@ApiNamespace("events")
public interface EventsAPIInterface extends RemoteAPIInterface {
    @ResponseWrapper(RawJSonWrapper.class)
    public SubscriptionResponse addsubscription(long subscriptionid, String[] subscriptions, String[] exclusions);

    @ResponseWrapper(RawJSonWrapper.class)
    public SubscriptionResponse changesubscriptiontimeouts(long subscriptionid, long polltimeout, long maxkeepalive);

    @ResponseWrapper(RawJSonWrapper.class)
    public SubscriptionResponse getsubscription(long subscriptionid);

    public SubscriptionStatusResponse getsubscriptionstatus(long subscriptionid);

    public void listen(RemoteAPIRequest request, RemoteAPIResponse response, long subscriptionid) throws APIFileNotFoundException, InternalApiException;

    @ResponseWrapper(RawJSonWrapper.class)
    public List<PublisherResponse> listpublisher();

    @ResponseWrapper(RawJSonWrapper.class)
    public SubscriptionResponse removesubscription(long subscriptionid, String[] subscriptions, String[] exclusions);

    @ResponseWrapper(RawJSonWrapper.class)
    public SubscriptionResponse setsubscription(long subscriptionid, String[] subscriptions, String[] exclusions);

    @ResponseWrapper(RawJSonWrapper.class)
    public SubscriptionResponse subscribe(String[] subscriptions, String[] exclusions);

    @ResponseWrapper(RawJSonWrapper.class)
    public SubscriptionResponse unsubscribe(long subscriptionid);

}
