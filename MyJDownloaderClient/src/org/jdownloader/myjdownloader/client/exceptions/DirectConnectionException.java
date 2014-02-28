package org.jdownloader.myjdownloader.client.exceptions;

import org.jdownloader.myjdownloader.client.json.ErrorResponse.Source;

public class DirectConnectionException extends MyJDownloaderException {
    
    public DirectConnectionException() {
        super();
    }
    
    public DirectConnectionException(final Exception e) {
        super(e);
    }
    
    @Override
    public Source getSource() {
        return Source.DEVICE;
    }
    
}
