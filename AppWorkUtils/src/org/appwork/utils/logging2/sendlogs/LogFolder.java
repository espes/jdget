package org.appwork.utils.logging2.sendlogs;

import java.io.File;

public class LogFolder {

    private long    created;
    private boolean selected;
    private boolean current    = false;

    private long    lastModified;

    private File    folder;

    private boolean needsFlush = false;

    public LogFolder(final File f, final long timestamp) {
        this.created = timestamp;
        this.lastModified = f.lastModified();
        this.folder = f;
    }

    public long getCreated() {
        return this.created;
    }

    public File getFolder() {
        return this.folder;
    }

    public long getLastModified() {
        return this.lastModified;
    }

    /**
     * @return the current
     */
    public boolean isCurrent() {
        return this.current;
    }

    /**
     * @return the needsFlush
     */
    public boolean isNeedsFlush() {
        return this.needsFlush;
    }

    public boolean isSelected() {
        return this.selected;
    }

    public void setCreated(final long created) {
        this.created = created;
    }

    /**
     * @param current
     *            the current to set
     */
    public void setCurrent(final boolean current) {
        this.current = current;
    }

    public void setFolder(final File folder) {
        this.folder = folder;
    }

    public void setLastModified(final long lastModified) {
        this.lastModified = lastModified;
    }

    /**
     * @param needsFlush
     *            the needsFlush to set
     */
    public void setNeedsFlush(final boolean needsFlush) {
        this.needsFlush = needsFlush;
    }

    public void setSelected(final boolean selected) {
        this.selected = selected;
    }

}
