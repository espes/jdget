package org.jdownloader.myjdownloader.client.json;

public enum DeviceConnectionStatus {
    /* Session is not bound to a device */
    UNBOUND(5),
    /* normal keepAlive=close */
    KEEPALIVE(4),
    /* OK, connection is established, but current syncMark will be send too */
    OK_SYNC(2),
    /* old OK, connection is established */
    OK(1),
    /* sessionToken is invalid */
    TOKEN(0);

    public static DeviceConnectionStatus parse(final int code) {
        for (final DeviceConnectionStatus status : DeviceConnectionStatus.values()) {
            if (status.getCode() == code) { return status; }
        }
        return null;
    }

    private int code;

    private DeviceConnectionStatus(final int code) {
        this.code = code;
    }

    public int getCode() {
        return this.code;
    }
}
