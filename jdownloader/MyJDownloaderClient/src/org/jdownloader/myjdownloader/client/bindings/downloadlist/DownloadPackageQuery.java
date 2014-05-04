package org.jdownloader.myjdownloader.client.bindings.downloadlist;

import org.jdownloader.myjdownloader.client.bindings.AbstractPackageQuery;

public class DownloadPackageQuery extends AbstractPackageQuery {
    public boolean isSpeed() {
        return speed;
    }

    public void setSpeed(final boolean speed) {
        this.speed = speed;
    }

    public boolean isFinished() {
        return finished;
    }

    public void setFinished(final boolean finished) {
        this.finished = finished;
    }

    public boolean isEta() {
        return eta;
    }

    public void setEta(final boolean eta) {
        this.eta = eta;
    }

    public boolean isBytesLoaded() {
        return bytesLoaded;
    }

    public void setBytesLoaded(final boolean done) {
        bytesLoaded = done;
    }

    public boolean isRunning() {
        return running;
    }

    public void setRunning(final boolean running) {
        this.running = running;
    }

    private boolean speed       = false;
    private boolean finished    = false;
    private boolean eta         = false;
    private boolean bytesLoaded = false;

    private boolean running     = false;
}