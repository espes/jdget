package org.jdownloader.myjdownloader.client.json;

public enum DeviceErrorType {
    UNKNOWN(500)


;

    private int code;

    private DeviceErrorType() {
        code = 500;
    }

    private DeviceErrorType(final int code) {
        this.code = code;
    }

    public int getCode() {
        return code;
    }
}