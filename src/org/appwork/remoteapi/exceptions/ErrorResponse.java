package org.appwork.remoteapi.exceptions;

import org.appwork.storage.Storable;

public class ErrorResponse implements Storable {

    private String type;

    private Object data;

    public ErrorResponse(/* Storable */) {
    }

    public ErrorResponse(final String error, final Object data) {

        this.type = error;
        this.data = data;
    }

    public Object getData() {
        return this.data;
    }

    public String getType() {
        return this.type;
    }

    public void setData(final Object data) {
        this.data = data;
    }

    public void setType(final String type) {
        this.type = type;
    }

}
