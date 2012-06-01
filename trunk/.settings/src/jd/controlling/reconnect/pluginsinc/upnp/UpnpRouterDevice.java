package jd.controlling.reconnect.pluginsinc.upnp;

import java.util.HashMap;

public class UpnpRouterDevice extends HashMap<String, String> {
    /**
     * These constancs are not only storage keys, but are used by the Parser in
     * UPNPRouterPlugin. So <b>DO NEVER CHANGE THEM WITHOUT KNOWING 100% what
     * you are doing</b>
     */
    public static final String LOCATION         = "location";
    public static final String URLBASE          = "urlbase";
    public static final String FRIENDLYNAME     = "friendlyname";
    public static final String HOST             = "host";
    public static final String CONTROLURL       = "controlurl";
    public static final String WANSERVICE       = "wanservice";
    public static final String SERVICETYPE      = "servicetype";
    /**
     * 
     */
    private static final long  serialVersionUID = 1L;
    public static final String MODELNAME        = "modelname";
    public static final String MANUFACTOR       = "manufacturer";

    public String getControlURL() {
        return this.get(UpnpRouterDevice.CONTROLURL);
    }

    public String getFriendlyname() {
        return this.get(UpnpRouterDevice.FRIENDLYNAME);
    }

    public String getHost() {
        return this.get(UpnpRouterDevice.HOST);
    }

    public String getLocation() {
        return this.get(UpnpRouterDevice.LOCATION);
    }

    public String getManufactor() {
        return this.get(UpnpRouterDevice.MANUFACTOR);
    }

    public String getModelname() {
        return this.get(UpnpRouterDevice.MODELNAME);
    }

    public String getServiceType() {
        return this.get(UpnpRouterDevice.SERVICETYPE);
    }

    public String getUrlBase() {
        return this.get(UpnpRouterDevice.URLBASE);
    }

    public String getWanservice() {
        return this.get(UpnpRouterDevice.WANSERVICE);
    }

    public void setControlURL(final String controlURL) {
        this.put(UpnpRouterDevice.CONTROLURL, controlURL);
    }

    public void setFriendlyname(final String friendlyname) {
        this.put(UpnpRouterDevice.FRIENDLYNAME, friendlyname);
    }

    public void setHost(final String host) {
        this.put(UpnpRouterDevice.HOST, host);
    }

    public void setLocation(final String location) {
        this.put(UpnpRouterDevice.LOCATION, location);
    }

    public void setManufactor(final String manufactor2) {
        this.put(UpnpRouterDevice.MANUFACTOR, manufactor2);
    }

    public void setModelname(final String modelname2) {
        this.put(UpnpRouterDevice.MODELNAME, modelname2);
    }

    public void setServiceType(final String servicyType) {
        this.put(UpnpRouterDevice.SERVICETYPE, servicyType);
    }

    public void setUrlBase(final String urlBase) {
        this.put(UpnpRouterDevice.URLBASE, urlBase);
    }

    public void setWanservice(final String wanservice) {
        this.put(UpnpRouterDevice.WANSERVICE, wanservice);
    }

}
