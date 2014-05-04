package org.jdownloader.myjdownloader.client.bindings.downloadlist;

import org.jdownloader.myjdownloader.client.bindings.AbstractPackageStorable;




public class DownloadPackageStorable extends AbstractPackageStorable {

    private String activeTask = null;

    private long bytesLoaded = -1;

    private long eta = -1l;

    private boolean finished = false;

    private boolean running = false;

    private long speed = -1;

    public DownloadPackageStorable(/* Storable */) {
    }

    public String getActiveTask() {
        return activeTask;
    }

    public long getBytesLoaded() {
        return bytesLoaded;
    }

    public long getEta() {
        return eta;
    }

    public long getSpeed() {
        return speed;
    }

    public boolean isFinished() {
        return finished;
    }

    public boolean isRunning() {
        return running;
    }

    public void setActiveTask(final String activeTask) {
        this.activeTask = activeTask;
    }

    public void setBytesLoaded(final long done) {
        bytesLoaded = done;
    }

    public void setEta(final long eta) {
        this.eta = eta;
    }

    public void setFinished(final boolean finished) {
        this.finished = finished;
    }

    public void setRunning(final boolean running) {
        this.running = running;
    }

    public void setSpeed(final long speed) {
        this.speed = speed;
    }

}