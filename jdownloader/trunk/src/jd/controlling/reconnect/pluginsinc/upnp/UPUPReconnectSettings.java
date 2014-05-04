package jd.controlling.reconnect.pluginsinc.upnp;

import org.appwork.storage.config.ConfigInterface;
import org.appwork.storage.config.annotations.AboutConfig;

public interface UPUPReconnectSettings extends ConfigInterface {

    @AboutConfig
    String getControlURL();

    void setControlURL(String str);

    @AboutConfig
    String getModelName();

    void setModelName(String name);

    @AboutConfig
    String getServiceType();

    void setServiceType(String text);

    @AboutConfig
    String getWANService();

    void setWANService(String wan);

    @AboutConfig
    boolean isIPCheckEnabled();

    void setIPCheckEnabled(boolean b);
}
