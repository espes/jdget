package jd.http;

import java.util.logging.Logger;

public interface BrowserSettings {

    public ProxySelectorInterface getProxySelector();

    public Logger getLogger();

    public boolean isDebug();

    public boolean isVerbose();

    public void setProxySelector(ProxySelectorInterface proxy);

    public void setDebug(boolean b);

    public void setLogger(Logger logger);

    public void setVerbose(boolean b);

}
