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

/* Old librtmp handling in revision < 13938 */

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
    
    protected final RtmpUrlConnection rtmpConnection;
    
    private final URL                 url;
    // don't name it plugin!
    protected PluginForHost           plg;
    // don't name it downloadLink
    protected DownloadLink            dLink;
    
    public RTMPDownload(final PluginForHost plugin, final DownloadLink downloadLink, final String rtmpURL) throws IOException, PluginException {
        super(plugin, downloadLink, null);
        this.plg = plugin;
        this.dLink = downloadLink;
        url = new URL(rtmpURL);
        rtmpConnection = (RtmpUrlConnection) url.openConnection();
    }
    
    public RtmpUrlConnection getRtmpConnection() {
        return rtmpConnection;
    }
    
    public boolean startDownload() throws Exception {
        return rtmpDump().start(rtmpConnection);
    }
    
    public String getRtmpDumpChecksum() throws Exception {
        return rtmpDump().getRtmpDumpChecksum();
    }
    
    public String getRtmpDumpVersion() throws Exception {
        return rtmpDump().getRtmpDumpVersion();
    }
    
    private RtmpDump rtmpDump() throws Exception {
        return new RtmpDump(plg, dLink, String.valueOf(url));
    }
    
}