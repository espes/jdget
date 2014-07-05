package org.jdownloader.myjdownloader.client.bindings;

public abstract class AbstractLinkStorable extends AbstractNodeStorable implements LinkNode {

    private String   host        = null;
    private long     packageUUID = -1;
    private PriorityStorable priority    = PriorityStorable.DEFAULT;

    public PriorityStorable getPriority() {
        return priority;
    }

    public void setPriority(PriorityStorable priority) {
        this.priority = priority;
    }

    private String url = null;

    public String getHost() {
        return host;
    }

    public long getPackageUUID() {
        return packageUUID;
    }

    public String getUrl() {
        return url;
    }

    public void setHost(final String host) {
        this.host = host;
    }

    public void setPackageUUID(final long packageUUID) {
        this.packageUUID = packageUUID;
    }

    public void setUrl(final String url) {
        this.url = url;
    }

}
