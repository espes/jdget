package org.jdownloader.myjdownloader.client.exceptions;

public class MyJDownloaderException extends Exception {

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

}
