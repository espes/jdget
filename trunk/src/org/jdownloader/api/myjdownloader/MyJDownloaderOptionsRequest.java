package org.jdownloader.api.myjdownloader;

import java.io.IOException;
import java.util.LinkedList;

import org.appwork.utils.net.httpserver.requests.OptionsRequest;

public class MyJDownloaderOptionsRequest extends OptionsRequest implements MyJDownloaderRequestInterface {
    private String jqueryCallback;
    private String signature;
    private long   requestID = -1;

    @Override
    public String getSignature() {
        return signature;
    }

    @Override
    public String getJqueryCallback() {
        return jqueryCallback;
    }

    public MyJDownloaderOptionsRequest(MyJDownloaderHttpConnection connection) {
        super(connection);

    }

    @Override
    public void setRequestedURLParameters(LinkedList<String[]> requestedURLParameters) {
        super.setRequestedURLParameters(requestedURLParameters);
        if (requestedURLParameters != null) {
            for (final String[] param : requestedURLParameters) {
                if (param[1] != null) {
                    /* key=value(parameter) */
                    if ("callback".equalsIgnoreCase(param[0])) {
                        /* filter jquery callback */
                        jqueryCallback = param[1];
                        continue;
                    } else if ("signature".equalsIgnoreCase(param[0])) {
                        /* filter url signature */
                        signature = param[1];
                        continue;
                    } else if ("rid".equalsIgnoreCase(param[0])) {
                        requestID = Long.parseLong(param[1]);
                        continue;
                    }

                }
            }
        }
    }

    public String getRequestConnectToken() {
        return getConnection().getRequestConnectToken();
    }

    @Override
    public MyJDownloaderHttpConnection getConnection() {
        return (MyJDownloaderHttpConnection) super.getConnection();
    }

    @Override
    public long getRid() throws IOException {
        return requestID;
    }

}