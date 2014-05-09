package jd.http;

import java.util.ArrayList;
import java.util.List;

import org.appwork.utils.net.httpconnection.HTTPProxy;

import com.sun.java.browser.net.ProxyInfo;

public class StaticProxySelector implements ProxySelectorInterface {

    private HTTPProxy       proxy;
    private List<HTTPProxy> lst;

    public StaticProxySelector(final HTTPProxy proxy) {
        this.proxy = proxy;
        lst = new ArrayList<HTTPProxy>();
        lst.add(proxy);
    }

    public HTTPProxy getProxy() {
        return proxy;
    }

    @Override
    public List<HTTPProxy> getProxiesByUrl(final String url) {
        return lst;
    }

}
