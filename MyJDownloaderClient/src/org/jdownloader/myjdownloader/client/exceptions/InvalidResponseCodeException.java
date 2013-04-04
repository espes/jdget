package org.jdownloader.myjdownloader.client.exceptions;

public class InvalidResponseCodeException extends MyJDownloaderException {

    private int    responseCode;
    private String content;

    public String getContent() {
        return content;
    }

    public int getResponseCode() {
        return responseCode;
    }

    public InvalidResponseCodeException(String errorString, int responseCode) {
        super();
        this.responseCode = responseCode;
        this.content = errorString;
    }
}
