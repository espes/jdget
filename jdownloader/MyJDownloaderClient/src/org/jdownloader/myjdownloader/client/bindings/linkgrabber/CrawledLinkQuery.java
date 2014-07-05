package org.jdownloader.myjdownloader.client.bindings.linkgrabber;

import org.jdownloader.myjdownloader.client.bindings.AbstractLinkQuery;

public class CrawledLinkQuery extends AbstractLinkQuery {

    private boolean availability = false;

    public boolean isAvailability() {
        return availability;
    }

    public void setAvailability(final boolean availability) {
        this.availability = availability;
    }

    public boolean isVariants() {
        return variants;
    }

    public void setVariants(final boolean variants) {
        this.variants = variants;
    }

    private boolean variants = false;

    private boolean priority = false;

    public boolean isPriority() {
        return priority;
    }

    public void setPriority(boolean priority) {
        this.priority = priority;
    }

}