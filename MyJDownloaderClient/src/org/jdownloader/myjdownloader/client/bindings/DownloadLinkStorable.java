package org.jdownloader.myjdownloader.client.bindings;

import org.jdownloader.myjdownloader.client.json.AbstractJsonData;

public class DownloadLinkStorable extends AbstractJsonData {

    private String  name             = null;

    private long    uuid             = -1;

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

    public DownloadLinkStorable(/* Storable */) {

    }

    public long getBytesLoaded() {
        return this.bytesLoaded;
    }

    public long getBytesTotal() {
        return this.bytesTotal;
    }

    public long getEta() {
        return this.eta;
    }

    public String getExtractionStatus() {
        return this.extractionStatus;
    }

    public String getHost() {
        return this.host;
    }

    public String getName() {
        return this.name;
    }

    public long getPackageUUID() {
        return this.packageUUID;
    }

    public long getSpeed() {
        return this.speed;
    }

    public String getUrl() {
        return this.url;
    }

    public long getUuid() {
        return this.uuid;
    }

    public boolean isEnabled() {
        return this.enabled;
    }

    public boolean isFinished() {
        return this.finished;
    }

    public boolean isRunning() {
        return this.running;
    }

    public boolean isSkipped() {
        return this.skipped;
    }

    public void setBytesLoaded(final long bytesLoaded) {
        this.bytesLoaded = bytesLoaded;
    }

    public void setBytesTotal(final long bytesTotal) {
        this.bytesTotal = bytesTotal;
    }

    public void setEnabled(final boolean enabled) {
        this.enabled = enabled;
    }

    public void setEta(final long eta) {
        this.eta = eta;
    }

    public void setExtractionStatus(final String extractionStatus) {
        this.extractionStatus = extractionStatus;
    }

    public void setFinished(final boolean finished) {
        this.finished = finished;
    }

    public void setHost(final String host) {
        this.host = host;
    }

    public void setName(final String name) {
        this.name = name;
    }

    public void setPackageUUID(final long packageUUID) {
        this.packageUUID = packageUUID;
    }

    public void setRunning(final boolean running) {
        this.running = running;
    }

    public void setSkipped(final boolean skipped) {
        this.skipped = skipped;
    }

    public void setSpeed(final long speed) {
        this.speed = speed;
    }

    public void setUrl(final String url) {
        this.url = url;
    }

    public void setUuid(final long uuid) {
        this.uuid = uuid;
    }

}
