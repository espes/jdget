package org.appwork.exceptions;

/**
 * For all "What the F$%&" Situations. We all know that the shouldn't exist, but we all know: They do! 
 * @author Thomas
 *
 */
public class WTFException extends RuntimeException {

    /**
     * 
     */
    public WTFException() {
        super();
        // TODO Auto-generated constructor stub
    }

    /**
     * @param message
     * @param cause
     * @param enableSuppression
     * @param writableStackTrace
     */
    public WTFException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
        // TODO Auto-generated constructor stub
    }

    /**
     * @param message
     * @param cause
     */
    public WTFException(String message, Throwable cause) {
        super(message, cause);
        // TODO Auto-generated constructor stub
    }

    /**
     * @param message
     */
    public WTFException(String message) {
        super(message);
        // TODO Auto-generated constructor stub
    }

    /**
     * @param cause
     */
    public WTFException(Throwable cause) {
        super(cause);
        // TODO Auto-generated constructor stub
    }

}
