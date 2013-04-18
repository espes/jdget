package org.jdownloader.myjdownloader.client.json;

public class ErrorResponse {
    public static enum Source {
        MYJD,
        DEVICE
    }

    public static enum Type {
        MAINTENANCE(503),
        OVERLOAD(503),
        TOO_MANY_REQUESTS(429),
        ERROR_EMAIL_NOT_CONFIRMED(401),
        TOKEN_INVALID(403),
        OFFLINE(504),
        UNKNOWN(500),
        AUTH_FAILED(403),
        EMAIL_INVALID,
        CHALLENGE_FAILED,
        EMAIL_FORBIDDEN,
        FAILED;

        private int code;

        private Type() {
            this.code = 500;
        }

        private Type(final int code) {
            this.code = code;
        }

        public int getCode() {
            return this.code;
        }
    }

    private Source src;
    private Type   type;

    private Object data;

    public ErrorResponse(/* Storable */) {

    }

    public ErrorResponse(final Source string, final Type error, final Object data) {
        this.src = string;
        this.type = error;
        this.data = data;
    }

    public Object getData() {
        return this.data;
    }

    public Source getSrc() {
        return this.src;
    }

    public Type getType() {
        return this.type;
    }

    public void setData(final Object data) {
        this.data = data;
    }

    public void setSrc(final Source src) {
        this.src = src;
    }

    public void setType(final Type type) {
        this.type = type;
    }

}
