package org.jdownloader.myjdownloader.client.exceptions;

import org.jdownloader.myjdownloader.client.json.RegisterResponse;

public class RegisterException extends MyJDownloaderException {

    private RegisterResponse response;

    public RegisterException(RegisterResponse ret) {
        super(ret.getStatus().toString());
        this.response = ret;
    }

    public RegisterResponse getResponse() {
        return response;
    }

}
