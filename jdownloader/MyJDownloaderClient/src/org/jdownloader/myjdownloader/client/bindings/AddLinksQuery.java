package org.jdownloader.myjdownloader.client.bindings;

import org.jdownloader.myjdownloader.client.json.AbstractJsonData;

public class AddLinksQuery extends AbstractJsonData {
    public AddLinksQuery(/* storable */) {

    }

    /**
     * @param autostart
     * @param links
     * @param packageName
     * @param extractPassword
     * @param downloadPassword
     * @param destinationFolder
     */
    public AddLinksQuery(final boolean autostart, final String links, final String packageName, final String extractPassword, final String downloadPassword, final String destinationFolder) {
        super();
        this.autostart = autostart;
        this.links = links;
        this.packageName = packageName;
        this.extractPassword = extractPassword;
        this.downloadPassword = downloadPassword;
        this.destinationFolder = destinationFolder;
    }

    private boolean autostart       = false;
    private String  links           = null;
    private String  packageName     = null;
    private String  extractPassword = null;

    public boolean isAutostart() {
        return autostart;
    }

    public void setAutostart(final boolean autostart) {
        this.autostart = autostart;
    }

    public String getLinks() {
        return links;
    }

    public void setLinks(final String links) {
        this.links = links;
    }

    public String getPackageName() {
        return packageName;
    }

    public void setPackageName(final String packageName) {
        this.packageName = packageName;
    }

    public String getExtractPassword() {
        return extractPassword;
    }

    public void setExtractPassword(final String extractPassword) {
        this.extractPassword = extractPassword;
    }

    public String getDownloadPassword() {
        return downloadPassword;
    }

    public void setDownloadPassword(final String downloadPassword) {
        this.downloadPassword = downloadPassword;
    }

    public String getDestinationFolder() {
        return destinationFolder;
    }

    public void setDestinationFolder(final String destinationFolder) {
        this.destinationFolder = destinationFolder;
    }

    private PriorityStorable priority = PriorityStorable.DEFAULT;

    public PriorityStorable getPriority() {
        return priority;
    }

    public void setPriority(PriorityStorable priority) {
        this.priority = priority;
    }

    private String downloadPassword  = null;
    private String destinationFolder = null;

}