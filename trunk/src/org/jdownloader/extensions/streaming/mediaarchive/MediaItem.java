package org.jdownloader.extensions.streaming.mediaarchive;

import javax.swing.ImageIcon;

import jd.plugins.DownloadLink;

import org.jdownloader.images.NewTheme;

public abstract class MediaItem implements MediaNode {
    private DownloadLink downloadLink;

    public MediaItem(DownloadLink dl) {
        this.downloadLink = dl;
    }

    public DownloadLink getDownloadLink() {
        return downloadLink;
    }

    public void setDownloadLink(DownloadLink downloadLink) {
        this.downloadLink = downloadLink;
    }

    @Override
    public String getName() {
        return downloadLink.getName();
    }

    @Override
    public ImageIcon getIcon() {
        return NewTheme.I().getIcon("video", 20);
    }

    private String containerFormat;
    private long   size = -1;

    // video container type
    public void setContainerFormat(String majorBrand) {
        this.containerFormat = majorBrand;

    }

    public String getContainerFormat() {
        return containerFormat;
    }

    public void setSize(long l) {
        this.size = l;
    }

    public long getSize() {

        return size <= 0 ? downloadLink.getDownloadSize() : size;
    }

}
