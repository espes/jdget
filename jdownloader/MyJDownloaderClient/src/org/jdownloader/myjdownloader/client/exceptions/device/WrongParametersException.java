package org.jdownloader.myjdownloader.client.exceptions.device;

import org.jdownloader.myjdownloader.client.exceptions.MyJDownloaderException;

public class WrongParametersException extends MyJDownloaderException {
    private Object data;

    public WrongParametersException(final Object data) {
        super(data==null?null:data.toString());
        this.data = data;
    }

}
