package org.jdownloader.myjdownloader.client.bindings;

public interface LinkNode extends Node{



    public abstract void setUrl(final String url);

    public abstract String getUrl();



    public abstract void setPackageUUID(final long packageUUID);

    public abstract long getPackageUUID();

    public abstract void setHost(final String host);

    public abstract String getHost();



}
