package org.jdownloader.myjdownloader.client.exceptions.device;

import org.jdownloader.myjdownloader.client.exceptions.MyJDownloaderException;

public class UnknownInterfaceException extends MyJDownloaderException {
    private Object data;

    public UnknownInterfaceException(Object data) {
        this.data = data;
    }

}
