package org.jdownloader.myjdownloader.client.bindings;

import java.util.HashMap;

public class AccountStorable {

    private org.jdownloader.myjdownloader.client.json.JsonMap infoMap = null;
    private long                    UUID;
    private String                  hostname;

    public void setUUID(final long uUID) {
        UUID = uUID;
    }

    public void setHostname(final String hostname) {
        this.hostname = hostname;
    }

    public long getUUID() {
        return UUID;
    }

    public String getHostname() {
        return hostname;
    }

    public org.jdownloader.myjdownloader.client.json.JsonMap getInfoMap() {
        return infoMap;
    }

    public void setInfoMap(final org.jdownloader.myjdownloader.client.json.JsonMap infoMap) {
        this.infoMap = infoMap;
    }

    @SuppressWarnings("unused")
    protected AccountStorable(/* Storable */) {
    }

}
