package org.jdownloader.myjdownloader.client.exceptions;

import org.jdownloader.myjdownloader.client.json.ErrorResponse.Source;

public class MyJDownloaderException extends Exception {

    private Source source;

    public Source getSource() {
        return source;
    }

    public MyJDownloaderException(String string) {
        super(string);
    }

    public MyJDownloaderException() {
    }

    public MyJDownloaderException(Exception e) {
        super(e);
    }

    public static MyJDownloaderException get(Exception e) {
        if (e instanceof MyJDownloaderException) return (MyJDownloaderException) e;
        return new MyJDownloaderException(e);
    }

    public String toString() {
        return super.toString() + "(SRC: " + getSource() + ")";
    }

    public void setSource(Source src) {
        this.source = src;
    }

}
