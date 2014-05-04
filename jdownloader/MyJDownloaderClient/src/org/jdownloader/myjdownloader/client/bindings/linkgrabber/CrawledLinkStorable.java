package org.jdownloader.myjdownloader.client.bindings.linkgrabber;

import org.jdownloader.myjdownloader.client.bindings.AbstractLinkStorable;
import org.jdownloader.myjdownloader.client.json.AvailableLinkState;

public class CrawledLinkStorable extends AbstractLinkStorable {

    private AvailableLinkState availability = null;

    private boolean            variants     = false;

    public CrawledLinkStorable(/* Storable */) {

    }

    public AvailableLinkState getAvailability() {
        return availability;
    }

    public boolean isVariants() {
        return variants;
    }

    public void setAvailability(final AvailableLinkState availability) {
        this.availability = availability;
    }

    public void setVariants(final boolean variants) {
        this.variants = variants;
    }

}
