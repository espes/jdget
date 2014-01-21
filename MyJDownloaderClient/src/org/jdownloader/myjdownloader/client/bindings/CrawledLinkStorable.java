package org.jdownloader.myjdownloader.client.bindings;

import org.jdownloader.myjdownloader.client.json.AbstractJsonData;
import org.jdownloader.myjdownloader.client.json.AvailableLinkState;

public class CrawledLinkStorable extends AbstractJsonData {

    private String name = null;

    public String getName() {
        return name;
    }

    public void setName(final String name) {
        this.name = name;
    }

    public long getUuid() {
        return uuid;
    }

    private AvailableLinkState availability = null;

    public AvailableLinkState getAvailability() {
        return availability;
    }

    public void setAvailability(final AvailableLinkState availability) {
        this.availability = availability;
    }

    public void setUuid(final long uuid) {
        this.uuid = uuid;
    }

    private long uuid = -1;

    public CrawledLinkStorable(/* Storable */) {

    }

    private String  host        = null;

    private long    packageUUID = -1;

    private boolean variants    = false;

    public boolean isVariants() {
        return variants;
    }

    public void setVariants(final boolean variants) {
        this.variants = variants;
    }

    private boolean enabled    = false;

    private String  url        = null;

    private long    bytesTotal = -1;

    public String getHost() {
        return host;
    }

    public void setHost(final String host) {
        this.host = host;
    }

    public long getPackageUUID() {
        return packageUUID;
    }

    public void setPackageUUID(final long packageUUID) {
        this.packageUUID = packageUUID;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(final boolean enabled) {
        this.enabled = enabled;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(final String url) {
        this.url = url;
    }

    public long getBytesTotal() {
        return bytesTotal;
    }

    public void setBytesTotal(final long bytesTotal) {
        this.bytesTotal = bytesTotal;
    }

}
