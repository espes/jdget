package org.jdownloader.myjdownloader.client.bindings;

import org.jdownloader.myjdownloader.client.json.AbstractJsonData;

public class CrawledLinkQuery extends AbstractJsonData {
    private long[]  packageUUIDs;
    private int     startAt=0;
    private int     maxResults=-1;
    private boolean host = false;
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

    public boolean isHost() {
        return host;
    }

    public void setHost(final boolean host) {
        this.host = host;
    }

    public boolean isBytesTotal() {
        return bytesTotal;
    }

    public void setBytesTotal(final boolean size) {
        bytesTotal = size;
    }



    public boolean isUrl() {
        return url;
    }

    public void setUrl(final boolean url) {
        this.url = url;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(final boolean enabled) {
        this.enabled = enabled;
    }


    private boolean bytesTotal       = false;
  

  


   
    private boolean url              = false;
    private boolean enabled          = false;


    public long[] getPackageUUIDs() {
        return packageUUIDs;
    }

    public void setPackageUUIDs(final long[] packageUUIDs) {
        this.packageUUIDs = packageUUIDs;
    }

    public int getStartAt() {
        return startAt;
    }

    public void setStartAt(final int startAt) {
        this.startAt = startAt;
    }

    public int getMaxResults() {
        return maxResults;
    }

    public void setMaxResults(final int maxResults) {
        this.maxResults = maxResults;
    }

}