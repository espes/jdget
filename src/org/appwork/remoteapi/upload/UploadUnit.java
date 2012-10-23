package org.appwork.remoteapi.upload;

import java.io.File;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

import org.appwork.storage.Storable;
import org.appwork.utils.Hash;

public class UploadUnit implements Storable {

    private static AtomicLong   COUNTER     = new AtomicLong(0);
    private final String        eTag;
    private File                fileLocation;
    private long                lastAccess  = -1;
    private final long          expectedFinalSize;
    private final AtomicBoolean isUploading = new AtomicBoolean(false);

    private UploadUnit(/* Storable */) {
        this(-1);
    }

    public UploadUnit(final long expectedFinalSize) {
        this.eTag = UploadUnit.COUNTER.incrementAndGet() + Hash.getSHA1("" + System.currentTimeMillis() + UploadUnit.COUNTER.incrementAndGet());
        this.expectedFinalSize = expectedFinalSize;
    }

    public File _getFile() {
        return this.fileLocation;
    }

    public String _getQuotedETag() {
        return "\"" + this.eTag + "\"";
    }

    public void _setFile(final File file) {
        this.fileLocation = file;
    }

    public String getETag() {
        return this.eTag;
    }

    public long getExpectedFinalSize() {
        return this.expectedFinalSize;
    }

    public long getLastAccess() {
        return this.lastAccess;
    }

    public long getSize() {
        return this._getFile().length();
    }

    public boolean isComplete() {
        return this._getFile().length() == this.getExpectedFinalSize();
    }

    public boolean isUploading() {
        return this.isUploading.get();
    }

    public void setIsUploading(final boolean b) {
        this.isUploading.set(b);
    }

    public void setLastAccess(final long lastAccess) {
        this.lastAccess = lastAccess;
    }

    @Override
    public String toString() {
        return "UploadUnit [eTag=" + this.eTag + ", fileLocation=" + this.fileLocation + ", lastAccess=" + this.lastAccess + ", expectedFinalSize=" + this.expectedFinalSize + ", isUploading=" + this.isUploading + "]";
    }

}
