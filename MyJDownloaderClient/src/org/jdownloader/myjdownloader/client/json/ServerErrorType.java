package org.jdownloader.myjdownloader.client.json;

public enum ServerErrorType{
    MAINTENANCE(503),
    OVERLOAD(503),
    TOO_MANY_REQUESTS(429),
    ERROR_EMAIL_NOT_CONFIRMED(401),
    TOKEN_INVALID(403),
    OFFLINE(504),
    UNKNOWN(500),
    AUTH_FAILED(403),
    EMAIL_INVALID,
    CHALLENGE_FAILED,
    EMAIL_FORBIDDEN,
    FAILED;

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