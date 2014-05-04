package org.jdownloader.myjdownloader.client.bindings.interfaces;

import java.util.ArrayList;

import org.jdownloader.myjdownloader.client.bindings.ClientApiNameSpace;
import org.jdownloader.myjdownloader.client.bindings.events.json.MyJDEvent;
import org.jdownloader.myjdownloader.client.bindings.events.json.PublisherResponse;
import org.jdownloader.myjdownloader.client.bindings.events.json.SubscriptionResponse;
import org.jdownloader.myjdownloader.client.bindings.events.json.SubscriptionStatusResponse;
import org.jdownloader.myjdownloader.client.exceptions.device.ApiFileNotFoundException;
import org.jdownloader.myjdownloader.client.exceptions.device.InternalServerErrorException;

/**
 * See org.appwork.remoteapi.events.EventsAPIInterface
 * 
 * @author $Author: unknown$
 * 
 */
@ClientApiNameSpace("events")
public interface EventsInterface extends Linkable {

    public SubscriptionResponse addsubscription(long subscriptionid, String[] subscriptions, String[] exclusions);

    public SubscriptionResponse changesubscriptiontimeouts(long subscriptionid, long polltimeout, long maxkeepalive);

    public SubscriptionResponse getsubscription(long subscriptionid);

    public SubscriptionStatusResponse getsubscriptionstatus(long subscriptionid);

    public MyJDEvent[] listen(long subscriptionid) throws ApiFileNotFoundException, InternalServerErrorException;

    public ArrayList<PublisherResponse> listpublisher();

    public SubscriptionResponse removesubscription(long subscriptionid, String[] subscriptions, String[] exclusions);

    public SubscriptionResponse setsubscription(long subscriptionid, String[] subscriptions, String[] exclusions);

    public SubscriptionResponse subscribe(String[] subscriptions, String[] exclusions);

    public SubscriptionResponse unsubscribe(long subscriptionid);
}
