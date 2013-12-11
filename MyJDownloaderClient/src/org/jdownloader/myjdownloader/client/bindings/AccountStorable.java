package org.jdownloader.myjdownloader.client.bindings;

import org.jdownloader.myjdownloader.client.json.AbstractJsonData;

public class AccountStorable extends AbstractJsonData {

    private long    UUID;
    private boolean enabled;

    private boolean valid;

    private String  hostname;

    private String  username;

    private long    validUntil  = -1l;

    private long    trafficLeft = -1l;
    private long    trafficMax  = -1l;

    @SuppressWarnings("unused")
    protected AccountStorable(/* Storable */) {
    }

    public String getHostname() {
        return this.hostname;
    }

    public long getTrafficLeft() {
        return this.trafficLeft;
    }

    public long getTrafficMax() {
        return this.trafficMax;
    }

    public String getUsername() {
        return this.username;
    }

    public long getUUID() {
        return this.UUID;
    }

    public long getValidUntil() {
        return this.validUntil;
    }

    public boolean isEnabled() {
        return this.enabled;
    }

    public boolean isValid() {
        return this.valid;
    }

    public void setEnabled(final boolean enabled) {
        this.enabled = enabled;
    }

    public void setHostname(final String hostname) {
        this.hostname = hostname;
    }

    public void setTrafficLeft(final long trafficLeft) {
        this.trafficLeft = trafficLeft;
    }

    public void setTrafficMax(final long trafficMax) {
        this.trafficMax = trafficMax;
    }

    public void setUsername(final String username) {
        this.username = username;
    }

    public void setUUID(final long uUID) {
        this.UUID = uUID;
    }

    public void setValid(final boolean valid) {
        this.valid = valid;
    }

    public void setValidUntil(final long validUntil) {
        this.validUntil = validUntil;
    }

}
