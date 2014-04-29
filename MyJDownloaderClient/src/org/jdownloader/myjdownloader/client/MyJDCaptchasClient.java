package org.jdownloader.myjdownloader.client;

import java.util.List;

import org.jdownloader.myjdownloader.client.exceptions.MyJDownloaderException;
import org.jdownloader.myjdownloader.client.json.JSonRequest;
import org.jdownloader.myjdownloader.client.json.MyCaptchaChallenge;
import org.jdownloader.myjdownloader.client.json.MyCaptchaSolution;
import org.jdownloader.myjdownloader.client.json.MyCaptchaSolutionsListResponse;
import org.jdownloader.myjdownloader.client.json.SuccessfulResponse;

public class MyJDCaptchasClient<GenericType> {
    
    private final AbstractMyJDClient<GenericType> api;
    
    public MyJDCaptchasClient(final AbstractMyJDClient<GenericType> abstractMyJDClient) {
        this.api = abstractMyJDClient;
    }
    
    public boolean abort(final String ID) throws MyJDownloaderException {
        return this.remove(ID, MyCaptchaSolution.RESULT.ABORT);
    }
    
    public MyCaptchaSolution get(final String ID) throws MyJDownloaderException {
        final List<MyCaptchaSolution> ret = this.get(new String[] { ID });
        if (ret != null && ret.size() == 1) { return ret.get(0); }
        return null;
    }
    
    public List<MyCaptchaSolution> get(final String IDs[]) throws MyJDownloaderException {
        final SessionInfo sessionInfo = this.api.getSessionInfo();
        final String url = "/my/captchas/get?sessiontoken=" + this.api.urlencode(sessionInfo.getSessionToken());
        final JSonRequest re = new JSonRequest();
        re.setParams(new Object[] { IDs });
        re.setUrl(url);
        final List<MyCaptchaSolution> list = this.api.callServer(url, re, sessionInfo, MyCaptchaSolutionsListResponse.class).getList();
        return list;
    }
    
    public boolean invalidate(final String ID) throws MyJDownloaderException {
        return this.remove(ID, MyCaptchaSolution.RESULT.WRONG);
    }
    
    public List<MyCaptchaSolution> list() throws MyJDownloaderException {
        return this.get((String[]) null);
    }
    
    public boolean remove(final String ID, final MyCaptchaSolution.RESULT result) throws MyJDownloaderException {
        final SessionInfo sessionInfo = this.api.getSessionInfo();
        final String url = "/my/captchas/remove?sessiontoken=" + this.api.urlencode(sessionInfo.getSessionToken());
        final JSonRequest re = new JSonRequest();
        re.setParams(new Object[] { ID, result });
        re.setUrl(url);
        return this.api.callServer(url, re, sessionInfo, SuccessfulResponse.class).isSuccessful();
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
    
    public boolean timeout(final String ID) throws MyJDownloaderException {
        return this.remove(ID, MyCaptchaSolution.RESULT.TIMEOUT);
    }
    
    public boolean validate(final String ID) throws MyJDownloaderException {
        return this.remove(ID, MyCaptchaSolution.RESULT.CORRECT);
    }
    
}
