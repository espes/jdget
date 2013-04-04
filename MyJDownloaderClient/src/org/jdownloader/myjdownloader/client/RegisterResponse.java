package org.jdownloader.myjdownloader.client;

public class RegisterResponse {
    public RegisterResponse(/* storable */) {

    }

    public Status getMessage() {
        return message;
    }

    public void setMessage(Status message) {
        this.message = message;
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public static enum Status {
        WAIT_FOR_CONFIRMATION,
        EMAIL_EXISTS
    }

    private Status  message;
    private boolean success;
}
