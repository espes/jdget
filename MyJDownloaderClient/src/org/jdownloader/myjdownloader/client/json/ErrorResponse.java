package org.jdownloader.myjdownloader.client.json;

public class ErrorResponse {
    public static enum Source {
        MYJD,
        DEVICE
    }

    private Source src;
    private String type;

    public ErrorResponse(/* Storable */) {

    }

    public ErrorResponse(final Source string, final String error) {
        this.src = string;
        this.type = error;
    }

    public Source getSrc() {
        return this.src;
    }

    public String getType() {
        return this.type;
    }

    public void setSrc(final Source src) {
        this.src = src;
    }

    public void setType(final String type) {
        this.type = type;
    }

}
