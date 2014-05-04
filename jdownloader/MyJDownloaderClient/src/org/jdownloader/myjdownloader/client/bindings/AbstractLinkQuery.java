package org.jdownloader.myjdownloader.client.bindings;

public abstract class AbstractLinkQuery extends AbstractQuery {

    private boolean host = false;

    public boolean isHost() {
        return host;
    }

    public void setHost(final boolean host) {
        this.host = host;
    }

    public boolean isUrl() {
        return url;
    }

    public void setUrl(final boolean url) {
        this.url = url;
    }

    private boolean url = false;

}
