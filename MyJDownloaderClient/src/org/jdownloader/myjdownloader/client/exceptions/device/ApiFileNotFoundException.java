package org.jdownloader.myjdownloader.client.exceptions.device;

import org.jdownloader.myjdownloader.client.exceptions.MyJDownloaderException;

public class ApiFileNotFoundException extends MyJDownloaderException {
    private Object data;

    public ApiFileNotFoundException(Object data) {
        this.data = data;
    }

}
