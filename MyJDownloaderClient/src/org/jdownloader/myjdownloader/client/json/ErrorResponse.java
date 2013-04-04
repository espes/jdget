package org.jdownloader.myjdownloader.client.json;

public class ErrorResponse {
    public static enum Source {
        MYJD,
        DEVICE
    }

    public static enum Type {
        OVERLOAD(503),
        ERROR_EMAIL_NOT_CONFIRMED(401),
        TOKEN_INVALID(407),
        OFFLINE,
        UNKNOWN,
        AUTH_FAILED(403);

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
