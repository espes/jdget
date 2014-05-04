package org.jdownloader.myjdownloader.client.exceptions;


public class ExceptionResponse extends MyJDownloaderException {

    private int    responseCode;
    private String content;

    public String getContent() {
        return content;
    }

    public int getResponseCode() {
        return responseCode;
    }

    public ExceptionResponse(final String errorString, final int responseCode) {
        super(errorString);
        this.responseCode = responseCode;
        content = errorString;
    }

    public ExceptionResponse(final Exception e) {
        super(e);
    }
}
