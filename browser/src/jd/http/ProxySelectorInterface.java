package jd.http;

import java.util.List;

import org.appwork.utils.net.httpconnection.HTTPProxy;

public interface ProxySelectorInterface {

    List<HTTPProxy> getProxiesByUrl(String url);

}
