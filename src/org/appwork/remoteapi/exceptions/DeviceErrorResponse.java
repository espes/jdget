package org.appwork.remoteapi.exceptions;


public class DeviceErrorResponse {


    private String               type;

    private Object               data;

    public DeviceErrorResponse(/* Storable */) {

    }

    public DeviceErrorResponse( final String error, final Object data) {
 
        type = error;
        this.data = data;
    }

    public Object getData() {
        return data;
    }

    public String getSrc() {
        return "DEVICE";
    }

    public String getType() {
        return type;
    }

    public void setData(final Object data) {
        this.data = data;
    }


    public void setType(final String type) {
        this.type = type;
    }

}
