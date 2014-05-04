package org.jdownloader.myjdownloader.client.json;

public enum DeviceErrorType {
    SESSION(403),
    API_COMMAND_NOT_FOUND(404),
    AUTH_FAILED(403),
    FILE_NOT_FOUND(404),
    INTERNAL_SERVER_ERROR(500),
    API_INTERFACE_NOT_FOUND(404),
    BAD_PARAMETERS(400);
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