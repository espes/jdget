package org.appwork.exceptions;

/**
 * For all "What the F$%&" Situations. We all know that the shouldn't exist, but
 * we all know: They do!
 * 
 * @author Thomas
 * 
 */
public class WTFException extends RuntimeException {

    /**
     * 
     */
    private static final long serialVersionUID = -6107320171996331828L;

    /**
     * 
     */
    public WTFException() {
        super();
        // TODO Auto-generated constructor stub
    }

    /**
     * @param message
     */
    public WTFException(final String message) {
        super(message);
        // TODO Auto-generated constructor stub
    }

    /**
     * @param message
     * @param cause
     */
    public WTFException(final String message, final Throwable cause) {
        super(message, cause);
        // TODO Auto-generated constructor stub
    }

    /**
     * @param cause
     */
    public WTFException(final Throwable cause) {
        super(cause);
        // TODO Auto-generated constructor stub
    }

}
