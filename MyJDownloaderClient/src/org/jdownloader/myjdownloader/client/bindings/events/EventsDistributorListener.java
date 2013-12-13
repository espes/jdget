package org.jdownloader.myjdownloader.client.bindings.events;

import java.util.EventListener;

public interface EventsDistributorListener extends EventListener {
    public String getEventPattern();
public String getFilterPattern();
    void onNewMyJDEvent(String publisher, String eventid, Object eventData);

}