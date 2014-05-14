package jd.controlling.proxy;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import jd.http.Request;

import org.appwork.utils.net.httpconnection.HTTPProxy;
import org.appwork.utils.net.httpconnection.HTTPProxy.TYPE;
import org.jdownloader.updatev2.ProxyData;

public class NoProxySelector extends AbstractProxySelectorImpl {

    private ExtProxy        proxy;
    private List<HTTPProxy> list;

    public NoProxySelector() {

        proxy = new ExtProxy(this, new HTTPProxy(TYPE.NONE));
        // setFilter(proxyData.getFilter());
        list = new ArrayList<HTTPProxy>();
        list.add(proxy);

    }

    public NoProxySelector(ProxyData proxyData) {

        setEnabled(proxyData.isEnabled());

        setFilter(proxyData.getFilter());
        proxy = new ExtProxy(this, new HTTPProxy(TYPE.NONE));
        // setFilter(proxyData.getFilter());
        list = new ArrayList<HTTPProxy>();
        list.add(proxy);

    }

    public ProxyData toProxyData() {
        ProxyData ret = super.toProxyData();
        ret.setProxy(HTTPProxy.getStorable(proxy));
        return ret;
    }

    @Override
    public List<HTTPProxy> getProxiesByUrl(String url) {
        if (banList == null || banList.size() == 0)
            return list;

        try {
            if (!isBanned(new URL(url).getHost(), proxy)) {
                return list;
            } else {
                return new ArrayList<HTTPProxy>();
            }
        } catch (Exception e) {
            return new ArrayList<HTTPProxy>();
        }

    }

    public ExtProxy getProxy() {
        return proxy;
    }

    @Override
    public String toString() {
        return proxy.toString();
    }

    public void setType(Type value) {
        throw new IllegalStateException("This operation is not allowed on this Factory Type");
    }

    @Override
    public Type getType() {
        switch (getProxy().getType()) {

        case NONE:
            return Type.NONE;

        default:
            throw new IllegalStateException();
        }
    }

    @Override
    public String toExportString() {
        return null;
    }

    @Override
    public boolean isPreferNativeImplementation() {
        return proxy.isPreferNativeImplementation();
    }

    @Override
    public void setPreferNativeImplementation(boolean preferNativeImplementation) {

        proxy.setPreferNativeImplementation(preferNativeImplementation);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null || obj.getClass() != NoProxySelector.class)
            return false;
        return proxy.equals(((NoProxySelector) obj).getProxy());
    }

    @Override
    public int hashCode() {
        return proxy.hashCode();
    }

    @Override
    protected boolean isLocal() {
        return true;
    }

    @Override
    public boolean updateProxy(Request request, int retryCounter) {
        return false;
    }

    @Override
    protected void onBanListUpdate() {
        // if (isBanned(proxy)) {
        // // empty
        // list = new ArrayList<HTTPProxy>();
        // } else {
        // ArrayList<HTTPProxy> tmp = new ArrayList<HTTPProxy>();
        // tmp.add(proxy);
        // list = tmp;
        // }
    }

}
