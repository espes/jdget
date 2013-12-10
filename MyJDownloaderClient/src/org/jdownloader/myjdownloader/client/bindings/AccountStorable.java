package org.jdownloader.myjdownloader.client.bindings;

import org.jdownloader.myjdownloader.client.json.AbstractJsonData;

public class AccountStorable extends AbstractJsonData {

    private long    UUID;
    private boolean enabled;

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(final boolean enabled) {
        this.enabled = enabled;
    }

    public boolean isValid() {
        return valid;
    }

    public void setValid(final boolean valid) {
        this.valid = valid;
    }

    private boolean valid;
    private String  hostname;
    private String  username;
    private long    validUntil  = 1;
    private long    trafficLeft = 1;

    public long getTrafficLeft() {
        return trafficLeft;
    }

    public void setTrafficLeft(final long trafficLeft) {
        this.trafficLeft = trafficLeft;
    }

    public long getTrafficMax() {
        return trafficMax;
    }

    public void setTrafficMax(final long trafficMax) {
        this.trafficMax = trafficMax;
    }

    private long trafficMax = 1;

    public long getValidUntil() {
        return validUntil;
    }

    public void setValidUntil(final long validUntil) {
        this.validUntil = validUntil;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(final String username) {
        this.username = username;
    }

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

    @SuppressWarnings("unused")
    protected AccountStorable(/* Storable */) {
    }

}
