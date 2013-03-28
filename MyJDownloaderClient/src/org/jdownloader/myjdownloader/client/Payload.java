package org.jdownloader.myjdownloader.client;


public class Payload {

    private long timestamp;

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(final long timestamp) {
        this.timestamp = timestamp;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(final String url) {
        this.url = url;
    }

    public String[]  getParams() {
        return params;
    }

    public void setParams(final String[]  params) {
        this.params = params;
    }

    private String            url;
    private String[] params;

    public Payload(/* keep empty constructor fpor json parser */) {

    }

    public Payload(final String url, final long id, final String[] args) {
        this.url = url;
        params = args;
        timestamp = id;
    
    }

}
