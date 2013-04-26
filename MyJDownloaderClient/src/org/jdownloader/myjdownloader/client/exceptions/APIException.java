package org.jdownloader.myjdownloader.client.exceptions;

public class APIException extends Exception {

    private Object data;

    public Object getData() {
        return data;
    }

    public APIException(final Exception e) {
        super(e);
    }

    public APIException() {
    }

    public APIException(final String string, final Object data) {
        super(string);
        this.data=data;
    }

    public static APIException get(final Exception e) {
        if (e instanceof APIException) {
            return (APIException) e;
        }
        return new APIException(e);
    }

}
