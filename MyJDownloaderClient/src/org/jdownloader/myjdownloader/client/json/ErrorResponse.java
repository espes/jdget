package org.jdownloader.myjdownloader.client.json;

public class ErrorResponse {
    public static enum Source {
        MYJD,
        DEVICE
    }

    public static enum Type {
        ERROR_EMAIL_NOT_CONFIRMED(401),
        TOKEN_INVALID(407),
        OFFLINE,
        UNKNOWN,
        AUTH_FAILED(403);

        private int code;

        public int getCode() {
            return code;
        }

        private Type() {
            code = 500;
        }

        private Type(final int code) {
            this.code = code;
        }
    }

    private Source src;
    private Type   type;

    public Source getSrc() {
        return src;
    }

    public void setSrc(final Source src) {
        this.src = src;
    }

    public Type getType() {
        return type;
    }

    public void setType(final Type type) {
        this.type = type;
    }

    public Object getData() {
        return data;
    }

    public void setData(final Object data) {
        this.data = data;
    }

    private Object data;

    public ErrorResponse(/* Storable */) {

    }

    public ErrorResponse(final Source string, final Type error, final Object data) {
        src = string;
        type = error;
        this.data = data;
    }

}
