package org.jdownloader.myjdownloader.client.bindings;


public abstract class AbstractPackageQuery extends  AbstractQuery  {

    private boolean childCount = false;

    private boolean hosts      = false;

    private boolean saveTo     = false;

 
    public boolean isChildCount() {
        return childCount;
    }

    public boolean isHosts() {
        return hosts;
    }

    public boolean isSaveTo() {
        return saveTo;
    }

    public void setChildCount(final boolean childCount) {
        this.childCount = childCount;
    }

    public void setHosts(final boolean hosts) {
        this.hosts = hosts;
    }


    public void setSaveTo(final boolean saveTo) {
        this.saveTo = saveTo;
    }

   
}
