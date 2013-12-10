package org.jdownloader.myjdownloader.client.bindings.events;

import java.util.EventListener;

public interface EventsDistributorListener extends EventListener {

    void onNewMyJDEvent(String publisher, String eventid, Object eventData);

}