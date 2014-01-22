package org.jdownloader.myjdownloader.client.exceptions.device;

import org.jdownloader.myjdownloader.client.exceptions.MyJDownloaderException;

public class ApiFileNotFoundException extends MyJDownloaderException {
    private Object data;

    public ApiFileNotFoundException(final Object data) {
        super(data==null?null:data.toString());
        this.data = data;
    }

}
