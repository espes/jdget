package org.jdownloader.myjdownloader.client.exceptions;

import org.jdownloader.myjdownloader.client.SessionInfo;

public class TokenException extends MyJDownloaderException {

    private final SessionInfo sessionInfo;

    public SessionInfo getSessionInfo() {
        return sessionInfo;
    }

    public TokenException(SessionInfo sessionInfo) {
        super();
        this.sessionInfo = sessionInfo;
    }

}
