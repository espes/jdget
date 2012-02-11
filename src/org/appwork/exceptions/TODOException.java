package org.appwork.exceptions;

public class TODOException extends RuntimeException {

    /**
     * 
     */
    private static final long serialVersionUID = -4277315499969343085L;

    /**
     * 
     */
    public TODOException() {
        super();
        // TODO Auto-generated constructor stub
    }

    /**
     * @param message
     * @param cause
     * @param enableSuppression
     * @param writableStackTrace
     */
    public TODOException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
        // TODO Auto-generated constructor stub
    }

    /**
     * @param message
     * @param cause
     */
    public TODOException(String message, Throwable cause) {
        super(message, cause);
        // TODO Auto-generated constructor stub
    }

    /**
     * @param message
     */
    public TODOException(String message) {
        super(message);
        // TODO Auto-generated constructor stub
    }

    /**
     * @param cause
     */
    public TODOException(Throwable cause) {
        super(cause);
        // TODO Auto-generated constructor stub
    }

}
