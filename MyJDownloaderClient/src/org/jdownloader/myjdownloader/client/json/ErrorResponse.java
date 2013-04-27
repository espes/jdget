package org.jdownloader.myjdownloader.client.json;

public class ErrorResponse {
    public static enum Source {
        MYJD,
        DEVICE
    }

    private Source src;
    private String   type;

    private Object data;

    public ErrorResponse(/* Storable */) {

    }

    public ErrorResponse(final Source string, final String error, final Object data) {
        src = string;
        type = error;
        this.data = data;
    }

    public Object getData() {
        return data;
    }

    public Source getSrc() {
        return src;
    }

    public String getType() {
        return type;
    }

    public void setData(final Object data) {
        this.data = data;
    }

    public void setSrc(final Source src) {
        this.src = src;
    }

    public void setType(final String type) {
        this.type = type;
    }

}
