package jd.plugins.hoster;

import java.io.IOException;
import java.net.URL;

import jd.network.rtmp.RtmpDump;
import jd.network.rtmp.url.CustomUrlStreamHandlerFactory;
import jd.network.rtmp.url.RtmpUrlConnection;
import jd.plugins.DownloadLink;
import jd.plugins.PluginException;
import jd.plugins.PluginForHost;
import jd.plugins.download.RAFDownload;

//Old librtmp handling in revision < 13938

/**
 * This is a wrapper for RTMP
 * 
 * @author thomas
 * @author bismarck
 * 
 */
public class RTMPDownload extends RAFDownload {
    static {
        URL.setURLStreamHandlerFactory(new CustomUrlStreamHandlerFactory());
    }

    private final URL                 url;
    protected final RtmpUrlConnection rtmpConnection;

    public RTMPDownload(final PluginForHost plugin, final DownloadLink downloadLink, final String rtmpURL) throws IOException, PluginException {
        super(plugin, downloadLink, null);
        // TODO Auto-generated constructor stub
        url = new URL(rtmpURL);
        rtmpConnection = (RtmpUrlConnection) url.openConnection();
        // setResume(false);
        downloadLink.setDownloadInstance(this);
    }

    public RtmpUrlConnection getRtmpConnection() {
        return rtmpConnection;
    }

    public boolean startDownload() throws Exception {
        final RtmpDump rtmpdump = new RtmpDump(plugin, downloadLink, String.valueOf(url));
        return rtmpdump.start(rtmpConnection);
    }
}