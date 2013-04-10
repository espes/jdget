package org.jdownloader.myjdownloader;

public class RequestLineParser {

    public static RequestLineParser parse(final byte[] array) {
        try {
            for (int i = 0; i < array.length; i++) {
                if (array[i] == ' ') {
                    /* /t_sessiontoken(40)_deviceid(32) */
                    if (array[i + 2] != 't') { return null; }
                    if (array[i + 3] != '_') { return null; }
                    if (array[i + 44] != '_') { return null; }
                    if (array[i + 77] != '/') { return null; }
                    final String sessionToken = new String(array, i + 4, 40, "ISO-8859-1");
                    final String deviceID = new String(array, i + 45, 32, "ISO-8859-1");
                    return new RequestLineParser(deviceID, sessionToken);
                }
            }
        } catch (final Exception e) {
            e.printStackTrace();
        }
        return null;

    }

    private final String deviceID;

    private final String sessionToken;

    private RequestLineParser(final String deviceID, final String sessionToken) {
        this.deviceID = deviceID;
        this.sessionToken = sessionToken;
    }

    public String getDeviceID() {
        return this.deviceID;
    }

    public String getSessionToken() {
        return this.sessionToken;
    }
}
