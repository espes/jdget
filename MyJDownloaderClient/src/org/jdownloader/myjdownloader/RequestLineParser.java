package org.jdownloader.myjdownloader;

public class RequestLineParser {

    private String deviceID;

    private String token;

    public RequestLineParser() {

    }

    public String getDeviceID() {
        return this.deviceID;
    }

    public String getToken() {
        return this.token;
    }

    public RequestLineParser parse(final byte[] array) {

        try {
            byte b;
            for (int i = 0; i < array.length; i++) {
                b = array[i];
                if (b == ' ') {
                    // you could write method here
                    i += 2;

                    if (array[i] != 't') { return this; }

                    i += 2;
                    // token is sha1
                    this.token = new String(array, i, 40, "ISO-8859-1");

                    i += 40;

                    if (array[i] != '_') { return this; }
                    i++;
                    final int start = i;
                    int end = i;
                    while (i < array.length) {

                        i++;
                        if (array[i] == '/') {
                            end = i;
                            break;
                        }

                    }

                    this.deviceID = new String(array, start, end - start, "ISO-8859-1");

                    return this;
                }

            }
            return this;
        } catch (final Exception e) {
            e.printStackTrace();
            this.deviceID = null;
            this.token = null;
        }
        return this;

    }

}
