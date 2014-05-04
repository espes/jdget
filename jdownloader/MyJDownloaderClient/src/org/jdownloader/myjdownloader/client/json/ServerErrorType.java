package org.jdownloader.myjdownloader.client.json;

public enum ServerErrorType {
    MAINTENANCE(503),
    OVERLOAD(503),
    TOO_MANY_REQUESTS(429),
    ERROR_EMAIL_NOT_CONFIRMED(401),
    OUTDATED(403),
    TOKEN_INVALID(403),
    OFFLINE(504),
    UNKNOWN(500),
    BAD_REQUEST(400),
    AUTH_FAILED(403),
    EMAIL_INVALID,
    CHALLENGE_FAILED,
    METHOD_FORBIDDEN,
    EMAIL_FORBIDDEN,
    FAILED,
    /* storage errors */
    STORAGE_NOT_FOUND,
    STORAGE_LIMIT_REACHED,
    STORAGE_ALREADY_EXISTS,
    STORAGE_INVALID_KEY,
    STORAGE_KEY_NOT_FOUND,
    STORAGE_INVALID_STORAGEID;
    
    private int code;
    
    private ServerErrorType() {
        this.code = 500;
    }
    
    private ServerErrorType(final int code) {
        this.code = code;
    }
    
    public int getCode() {
        return this.code;
    }
}