package org.jdownloader.myjdownloader.client.exceptions;

public class APIException extends Exception {

    public APIException(Exception e) {
        super(e);
    }

    public APIException() {
    }

    public APIException(String string) {
        super(string);
    }

    public static APIException get(Exception e) {
        if (e instanceof APIException) return (APIException) e;
        return new APIException(e);
    }

}
