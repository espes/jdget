package org.jdownloader.myjdownloader.client.exceptions;


public class InvalidResponseCodeException extends MyJDownloaderException {

    private int responseCode;

    public int getResponseCode() {
        return responseCode;
    }

    public InvalidResponseCodeException(int responseCode) {
        super();
        this.responseCode = responseCode;
    }

}
