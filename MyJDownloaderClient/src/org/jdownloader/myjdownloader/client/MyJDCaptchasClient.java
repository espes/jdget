package org.jdownloader.myjdownloader.client;

import org.jdownloader.myjdownloader.client.exceptions.MyJDownloaderException;
import org.jdownloader.myjdownloader.client.json.CaptchaResponse;
import org.jdownloader.myjdownloader.client.json.DataURL;
import org.jdownloader.myjdownloader.client.json.JSonRequest;

public class MyJDCaptchasClient<GenericType> {
    
    private final AbstractMyJDClient<GenericType> api;
    
    public MyJDCaptchasClient(final AbstractMyJDClient<GenericType> abstractMyJDClient) {
        this.api = abstractMyJDClient;
    }
    
    public CaptchaResponse solve(final DataURL dataURL) throws MyJDownloaderException {
        final SessionInfo sessionInfo = this.api.getSessionInfo();
        final String url = "/my/captchas/solve?sessiontoken=" + this.api.urlencode(sessionInfo.getSessionToken());
        final JSonRequest re = new JSonRequest();
        re.setParams(new Object[] { dataURL });
        re.setUrl(url);
        return this.api.callServer(url, re, sessionInfo, CaptchaResponse.class);
    }
}
