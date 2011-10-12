package org.appwork.remotecall;

public class ResponseAlreadySentException extends RuntimeException {

    /**
     * 
     */
    public ResponseAlreadySentException() {
        super();
        
    }

    /**
     * @param message
     * @param cause
     */
    public ResponseAlreadySentException(String message, Throwable cause) {
        super(message, cause);
        
    }

    /**
     * @param message
     */
    public ResponseAlreadySentException(String message) {
        super(message);
        
    }

    /**
     * @param cause
     */
    public ResponseAlreadySentException(Throwable cause) {
        super(cause);
        
    }

}
