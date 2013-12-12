package org.jdownloader.myjdownloader.client.bindings;



import org.jdownloader.myjdownloader.client.json.AbstractJsonData;

public class FilePackageStorable extends AbstractJsonData   {

    public FilePackageStorable(/* Storable */) {
    }



    private String name = null;

    public String getName() {
        return name;
    }

    public long getUuid() {
        return uuid;
    }

    public void setUuid(final long uuid) {
        this.uuid = uuid;
    }

    public void setName(final String name) {
        this.name = name;
    }

    private long   uuid   = -1;
    private String saveTo = null;

    public String getSaveTo() {
        return saveTo;
    }

    public void setSaveTo(final String saveTo) {
        this.saveTo = saveTo;
    }

    private long bytesTotal = -1;

    public long getBytesTotal() {
        return bytesTotal;
    }

    public void setBytesTotal(final long size) {
        bytesTotal = size;
    }

    private int childCount = -1;

    public int getChildCount() {
        return childCount;
    }

    public void setChildCount(final int childCount) {
        this.childCount = childCount;
    }

    private String[] hosts = null;

    public String[] getHosts() {
        return hosts;
    }

    public void setHosts(final String[] hosts) {
        this.hosts = hosts;
    }

    private String activeTask = null;

    public String getActiveTask() {
        return activeTask;
    }

    public void setActiveTask(final String activeTask) {
        this.activeTask = activeTask;
    }

    private long speed = -1;

    public long getSpeed() {
        return speed;
    }

    public void setSpeed(final long speed) {
        this.speed = speed;
    }

    private boolean finished = false;

    public boolean isFinished() {
        return finished;
    }

    public void setFinished(final boolean finished) {
        this.finished = finished;
    }

    private long eta = -1l;

    public long getEta() {
        return eta;
    }

    public void setEta(final long eta) {
        this.eta = eta;
    }

    private long bytesLoaded = -1;

    public long getBytesLoaded() {
        return bytesLoaded;
    }

    public void setBytesLoaded(final long done) {
        bytesLoaded = done;
    }

    private String comment = null;

    public String getComment() {
        return comment;
    }

    public void setComment(final String comment) {
        this.comment = comment;
    }

    private boolean enabled = false;

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(final boolean enabled) {
        this.enabled = enabled;
    }

    private boolean running = false;

    public boolean isRunning() {
        return running;
    }

    public void setRunning(final boolean running) {
        this.running = running;
    }

}