package org.jdownloader.myjdownloader.client.exceptions.device;

import org.jdownloader.myjdownloader.client.exceptions.MyJDownloaderException;

public class InternalServerErrorException extends MyJDownloaderException {
    private Object data;

    public InternalServerErrorException(Object data) {
        this.data = data;
    }

}
