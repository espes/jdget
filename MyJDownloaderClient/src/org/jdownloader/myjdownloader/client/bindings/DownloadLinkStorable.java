package org.jdownloader.myjdownloader.client.bindings;

import org.jdownloader.myjdownloader.client.json.AbstractJsonData;

public class DownloadLinkStorable extends AbstractJsonData {

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

    public void setUuid(final long uuid) {
        this.uuid = uuid;
    }

    private long uuid = -1;

    public DownloadLinkStorable(/* Storable */) {

    }

    private String  host             = null;

    private long    packageUUID      = -1;

    private String  extractionStatus = null;
    private boolean enabled          = false;

    private String  url              = null;
    private boolean skipped          = false;
    private boolean running          = false;

    private boolean finished         = false;

    private long    eta              = -1;

    private long    speed            = -1;

    private long    bytesLoaded      = -1;
    private long    bytesTotal       = -1;

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

    public String getExtractionStatus() {
        return extractionStatus;
    }

    public void setExtractionStatus(final String extractionStatus) {
        this.extractionStatus = extractionStatus;
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

    public boolean isSkipped() {
        return skipped;
    }

    public void setSkipped(final boolean skipped) {
        this.skipped = skipped;
    }

    public boolean isRunning() {
        return running;
    }

    public void setRunning(final boolean running) {
        this.running = running;
    }

    public boolean isFinished() {
        return finished;
    }

    public void setFinished(final boolean finished) {
        this.finished = finished;
    }

    public long getEta() {
        return eta;
    }

    public void setEta(final long eta) {
        this.eta = eta;
    }

    public long getSpeed() {
        return speed;
    }

    public void setSpeed(final long speed) {
        this.speed = speed;
    }

    public long getBytesLoaded() {
        return bytesLoaded;
    }

    public void setBytesLoaded(final long bytesLoaded) {
        this.bytesLoaded = bytesLoaded;
    }

    public long getBytesTotal() {
        return bytesTotal;
    }

    public void setBytesTotal(final long bytesTotal) {
        this.bytesTotal = bytesTotal;
    }

}
