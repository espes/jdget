package org.jdownloader.myjdownloader.client.bindings;

import org.jdownloader.myjdownloader.client.json.AbstractJsonData;

public class AccountStorable extends AbstractJsonData {

    private long    UUID;
    private boolean enabled;

    private boolean valid;

    private String  hostname;

    private String  username;
    private String  errorType;

    public String getErrorType() {
        return errorType;
    }

    public void setErrorType(final String errorType) {
        this.errorType = errorType;
    }

    public String getErrorString() {
        return errorString;
    }

    public void setErrorString(final String errorString) {
        this.errorString = errorString;
    }

    private String errorString;
    private long   validUntil  = -1l;

    private long   trafficLeft = -1l;
    private long   trafficMax  = -1l;

    @SuppressWarnings("unused")
    protected AccountStorable(/* Storable */) {
    }

    public String getHostname() {
        return hostname;
    }

    public long getTrafficLeft() {
        return trafficLeft;
    }

    public long getTrafficMax() {
        return trafficMax;
    }

    public String getUsername() {
        return username;
    }

    public long getUUID() {
        return UUID;
    }

    public long getValidUntil() {
        return validUntil;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public boolean isValid() {
        return valid;
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
        UUID = uUID;
    }

    public void setValid(final boolean valid) {
        this.valid = valid;
    }

    public void setValidUntil(final long validUntil) {
        this.validUntil = validUntil;
    }

}
