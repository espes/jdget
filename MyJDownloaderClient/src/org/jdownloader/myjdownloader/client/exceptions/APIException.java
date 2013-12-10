package org.jdownloader.myjdownloader.client.exceptions;

import java.util.HashSet;

public class APIException extends Exception {
    public static   <T extends Throwable> T getInstanceof(Throwable e, final Class<T> class1) {
        Throwable cause;
        final HashSet<Throwable> dupe = new HashSet<Throwable>();
        while (true) {
           
            if (class1.isAssignableFrom(e.getClass())) {
                return (T)e;
            }
            cause = e.getCause();
            if (cause == null || !dupe.add(cause)) {
                return null;
            }
            e = cause;
        }
    }
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
