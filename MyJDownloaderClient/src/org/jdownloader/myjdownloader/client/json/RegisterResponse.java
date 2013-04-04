package org.jdownloader.myjdownloader.client.json;

public class RegisterResponse {

    public static enum MSG {
        INVALID_EMAIL,
        INVALID_CAPTCHA,
        EMAIL_EXISTS,
        WAIT_FOR_CONFIRMATION,
        UNKNOWN
    }

    private boolean success = false;
    private MSG     message = MSG.UNKNOWN;

    public RegisterResponse(/* storable */) {
    }

    public MSG getMessage() {
        return message;
    }

    public boolean isSuccess() {
        return success;
    }

    public void setMessage(final MSG message) {
        this.message = message;
    }

    public void setSuccess(final boolean success) {
        this.success = success;
    }

}
