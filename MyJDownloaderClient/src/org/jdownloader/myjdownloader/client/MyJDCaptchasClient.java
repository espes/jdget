package org.jdownloader.myjdownloader.client;

import java.util.List;

import org.jdownloader.myjdownloader.client.exceptions.MyJDownloaderException;
import org.jdownloader.myjdownloader.client.json.JSonRequest;
import org.jdownloader.myjdownloader.client.json.MyCaptchaChallenge;
import org.jdownloader.myjdownloader.client.json.MyCaptchaSolution;
import org.jdownloader.myjdownloader.client.json.MyCaptchaSolutionsListResponse;

public class MyJDCaptchasClient<GenericType> {
    
    private final AbstractMyJDClient<GenericType> api;
    
    public MyJDCaptchasClient(final AbstractMyJDClient<GenericType> abstractMyJDClient) {
        this.api = abstractMyJDClient;
    }
    
    public MyCaptchaSolution solve(final MyCaptchaChallenge myCaptchaChallenge) throws MyJDownloaderException {
        final SessionInfo sessionInfo = this.api.getSessionInfo();
        final String url = "/my/captchas/solve?sessiontoken=" + this.api.urlencode(sessionInfo.getSessionToken());
        final JSonRequest re = new JSonRequest();
        re.setParams(new Object[] { myCaptchaChallenge });
        re.setUrl(url);
        final List<MyCaptchaSolution> list = this.api.callServer(url, re, sessionInfo, MyCaptchaSolutionsListResponse.class).getList();
        if (list != null && list.size() == 1) { return list.get(0); }
        return null;
    }
    
}
