package org.jdownloader.api.downloads.v2;

import jd.plugins.DownloadLink;

import org.appwork.storage.Storable;
import org.jdownloader.myjdownloader.client.bindings.DownloadLinkStorable;

public class DownloadLinkAPIStorableV2 extends DownloadLinkStorable implements Storable {

    public DownloadLinkAPIStorableV2(/* Storable */) {

    }

    public DownloadLinkAPIStorableV2(DownloadLink link) {
        setName(link.getName());
        setUuid(link.getUniqueID().getID());
    }

}
