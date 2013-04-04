package org.jdownloader.myjdownloader.client.exceptions;

import org.jdownloader.myjdownloader.client.exceptions.MyJDownloaderException;

public class ExceptionResponse extends MyJDownloaderException {

    private int    responseCode;
    private String content;

    public String getContent() {
        return content;
    }

    public int getResponseCode() {
        return responseCode;
    }

    public ExceptionResponse(String errorString, int responseCode) {
        super();
        this.responseCode = responseCode;
        this.content = errorString;
    }

    public ExceptionResponse(Exception e) {
        super(e);
    }
}
