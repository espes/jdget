package org.jdownloader.myjdownloader.client;

import org.jdownloader.myjdownloader.client.exceptions.MyJDownloaderException;

public class BadResponseException extends MyJDownloaderException {

    public BadResponseException(final String string, final Exception e) {
        super(string,e);
 
    }

    public BadResponseException(final String string) {
    super(string);
    }

}
