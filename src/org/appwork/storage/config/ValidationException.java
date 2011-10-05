package org.appwork.storage.config;

public class ValidationException extends RuntimeException {

    private Object value;
    public Object getValue() {
        return value;
    }

    public ValidationException() {
        super();
        // TODO Auto-generated constructor stub
    }

    public ValidationException(final String message) {
        super(message);
        // TODO Auto-generated constructor stub
    }

    public ValidationException(final String message, final Throwable cause) {
        super(message, cause);
        // TODO Auto-generated constructor stub
    }
public void setValue(Object value){
    this.value=value;
}
    public ValidationException(final Throwable cause) {
        super(cause);
        // TODO Auto-generated constructor stub
    }

}
