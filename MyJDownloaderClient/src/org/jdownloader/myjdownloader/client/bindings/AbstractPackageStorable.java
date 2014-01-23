package org.jdownloader.myjdownloader.client.bindings;

public abstract class AbstractPackageStorable extends AbstractNodeStorable implements PackageNode {

    private int      childCount = -1;

    private String[] hosts      = null;

    private String   saveTo     = null;

    public int getChildCount() {
        return childCount;
    }

    public String[] getHosts() {
        return hosts;
    }

    public String getSaveTo() {
        return saveTo;
    }

    public void setChildCount(final int childCount) {
        this.childCount = childCount;
    }

    public void setHosts(final String[] hosts) {
        this.hosts = hosts;
    }

    public void setSaveTo(final String saveTo) {
        this.saveTo = saveTo;
    }

}
