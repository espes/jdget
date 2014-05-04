package org.appwork.remotecall;

public class ResponseAlreadySentException extends RuntimeException {

    /**
     * 
     */
    private static final long serialVersionUID = 1536214454339514720L;

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
