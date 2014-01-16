package org.jdownloader.myjdownloader.client.bindings.interfaces;

import java.util.ArrayList;

import org.jdownloader.myjdownloader.client.bindings.events.json.MyJDEvent;
import org.jdownloader.myjdownloader.client.bindings.events.json.PublisherResponse;
import org.jdownloader.myjdownloader.client.bindings.events.json.SubscriptionResponse;
import org.jdownloader.myjdownloader.client.bindings.events.json.SubscriptionStatusResponse;

import com.google.gwt.user.client.rpc.AsyncCallback;

public interface EventsInterfaceAsync {

	void addsubscription(long subscriptionid, String[] subscriptions,
			String[] exclusions, AsyncCallback<SubscriptionResponse> callback);

	void changesubscriptiontimeouts(long subscriptionid, long polltimeout,
			long maxkeepalive, AsyncCallback<SubscriptionResponse> callback);

	void getsubscription(long subscriptionid,
			AsyncCallback<SubscriptionResponse> callback);

	void getsubscriptionstatus(long subscriptionid,
			AsyncCallback<SubscriptionStatusResponse> callback);

	void listen(long subscriptionid, AsyncCallback<MyJDEvent[]> callback);

	void listpublisher(AsyncCallback<ArrayList<PublisherResponse>> callback);

	void removesubscription(long subscriptionid, String[] subscriptions,
			String[] exclusions, AsyncCallback<SubscriptionResponse> callback);

	void setsubscription(long subscriptionid, String[] subscriptions,
			String[] exclusions, AsyncCallback<SubscriptionResponse> callback);

	void subscribe(String[] subscriptions, String[] exclusions,
			AsyncCallback<SubscriptionResponse> callback);

	void unsubscribe(long subscriptionid,
			AsyncCallback<SubscriptionResponse> callback);

}
