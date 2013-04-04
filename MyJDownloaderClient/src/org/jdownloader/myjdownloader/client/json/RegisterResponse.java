package org.jdownloader.myjdownloader.client.json;

public class RegisterResponse implements RequestIDValidator {

    public RegisterResponse(/* storable */) {
    }

    private long rid;

    public long getRid() {
        return rid;
    }

    public void setRid(final long timestamp) {
        this.rid = timestamp;
    }

}
