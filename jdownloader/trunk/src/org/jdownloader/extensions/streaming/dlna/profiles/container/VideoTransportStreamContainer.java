package org.jdownloader.extensions.streaming.dlna.profiles.container;

import org.jdownloader.extensions.streaming.dlna.Extensions;

public class VideoTransportStreamContainer extends AbstractAudioVideoContainer {

    protected boolean systemStream;
    protected int     packetsize;
    private TimeStamp timestamp;

    public boolean isSystemStream() {
        return systemStream;
    }

    public static enum TimeStamp {
        WITHOUT,
        ZERO,
        VALID
    }

    public VideoTransportStreamContainer(TimeStamp ts, Extensions... extensions) {
        super(extensions);
        setFormatName("mpegts");
        setName("video/mpegts");
        this.systemStream = true;
        timestamp = ts;
        packetsize = 188;

    }
}
