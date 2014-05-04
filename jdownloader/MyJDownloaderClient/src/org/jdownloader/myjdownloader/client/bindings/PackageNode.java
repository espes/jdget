package org.jdownloader.myjdownloader.client.bindings;

public interface PackageNode extends Node {



    public abstract void setHosts(final String[] hosts);

    public abstract String[] getHosts();

    public abstract void setChildCount(final int childCount);

    public abstract int getChildCount();


    public abstract void setSaveTo(final String saveTo);

    public abstract String getSaveTo();

    


}
