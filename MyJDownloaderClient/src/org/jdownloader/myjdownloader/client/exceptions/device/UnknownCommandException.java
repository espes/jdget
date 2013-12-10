package org.jdownloader.myjdownloader.client.exceptions.device;

import org.jdownloader.myjdownloader.client.exceptions.MyJDownloaderException;

public class UnknownCommandException extends MyJDownloaderException {
    private Object data;

    public UnknownCommandException(Object data) {
        this.data = data;
    }

}
