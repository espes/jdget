package jd.http;

import org.appwork.utils.net.httpconnection.HTTPProxy;

public class StaticProxy {

    private HTTPProxy orgReference;

    public HTTPProxy getOrgReference() {
        return orgReference;
    }

    public HTTPProxy getLocalClone() {
        return localClone;
    }

    HTTPProxy localClone;

    public StaticProxy(HTTPProxy proxy) {
        this.orgReference = proxy;
        this.localClone = proxy == null ? null : proxy.clone();

    }

}