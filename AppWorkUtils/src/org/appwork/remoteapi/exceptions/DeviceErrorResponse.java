package org.appwork.remoteapi.exceptions;

import org.appwork.storage.Storable;

public class DeviceErrorResponse extends ErrorResponse implements Storable {

    public DeviceErrorResponse(/* Storable */) {
    }

    public DeviceErrorResponse(final String error, final Object data) {
        super(error, data);
    }

    public String getSrc() {
        return "DEVICE";
    }

}
