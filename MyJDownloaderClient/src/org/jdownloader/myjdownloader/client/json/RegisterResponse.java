package org.jdownloader.myjdownloader.client.json;

public class RegisterResponse {

    public static enum Status {
        INVALID_EMAIL,
        INVALID_CAPTCHA,
        EMAIL_EXISTS,
        WAIT_FOR_CONFIRMATION,
        UNKNOWN
    }

  
    private Status  status  = Status.UNKNOWN;

    public RegisterResponse(/* storable */) {
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(final Status message) {
        status = message;
    }

}
