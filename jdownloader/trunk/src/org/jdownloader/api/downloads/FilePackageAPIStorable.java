package org.jdownloader.api.downloads;

import jd.plugins.FilePackage;

import org.appwork.storage.Storable;

public class FilePackageAPIStorable implements Storable {
    private FilePackage                                       pkg;
    private org.jdownloader.myjdownloader.client.json.JsonMap infoMap = null;

    public FilePackageAPIStorable(/* Storable */) {
    }

    public FilePackageAPIStorable(FilePackage pkg) {
        this.pkg = pkg;
    }

    public String getName() {
        FilePackage lpkg = pkg;
        if (lpkg != null) return lpkg.getName();
        return null;
    }

    public long getUUID() {
        FilePackage lpkg = pkg;
        if (lpkg != null) return lpkg.getUniqueID().getID();
        return 0;
    }

    public org.jdownloader.myjdownloader.client.json.JsonMap getInfoMap() {
        return infoMap;
    }

    public void setInfoMap(org.jdownloader.myjdownloader.client.json.JsonMap infoMap) {
        this.infoMap = infoMap;
    }
}