package org.jdownloader.myjdownloader.client.bindings.events;

import java.util.ArrayList;

import org.jdownloader.myjdownloader.client.bindings.ApiNamespace;
import org.jdownloader.myjdownloader.client.bindings.events.json.EventObjectStorable;
import org.jdownloader.myjdownloader.client.bindings.events.json.PublisherResponse;
import org.jdownloader.myjdownloader.client.bindings.events.json.SubscriptionResponse;
import org.jdownloader.myjdownloader.client.bindings.events.json.SubscriptionStatusResponse;

/**
 * See org.appwork.remoteapi.events.EventsAPIInterface
 * 
 * @author $Author: unknown$
 * 
 */
@ApiNamespace("events")
public interface EventsAPIInterface {

    public SubscriptionResponse addsubscription(long subscriptionid, String[] subscriptions, String[] exclusions);

    public SubscriptionResponse changesubscriptiontimeouts(long subscriptionid, long polltimeout, long maxkeepalive);

    public SubscriptionResponse getsubscription(long subscriptionid);

    public SubscriptionStatusResponse getsubscriptionstatus(long subscriptionid);

    public EventObjectStorable[] listen(long subscriptionid);

    public ArrayList<PublisherResponse> listpublisher();

    public SubscriptionResponse removesubscription(long subscriptionid, String[] subscriptions, String[] exclusions);

    public SubscriptionResponse setsubscription(long subscriptionid, String[] subscriptions, String[] exclusions);

    public SubscriptionResponse subscribe(String[] subscriptions, String[] exclusions);

    public SubscriptionResponse unsubscribe(long subscriptionid);
}
