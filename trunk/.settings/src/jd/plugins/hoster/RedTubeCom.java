package jd.plugins.hoster;

import jd.PluginWrapper;
import jd.http.RandomUserAgent;
import jd.nutils.encoding.Encoding;
import jd.plugins.DownloadLink;
import jd.plugins.DownloadLink.AvailableStatus;
import jd.plugins.HostPlugin;
import jd.plugins.LinkStatus;
import jd.plugins.PluginException;
import jd.plugins.PluginForHost;

@HostPlugin(revision = "$Revision: 15539 $", interfaceVersion = 2, names = { "redtube.com" }, urls = { "http://(www\\.)?redtube\\.com/\\d+" }, flags = { 0 })
public class RedTubeCom extends PluginForHost {
    private String dlink = null;

    public RedTubeCom(PluginWrapper wrapper) {
        super(wrapper);
    }

    @Override
    public String getAGBLink() {
        return "http://www.redtube.com/terms";
    }

    @Override
    public int getMaxSimultanFreeDownloadNum() {
        return -1;
    }

    @Override
    public void handleFree(DownloadLink link) throws Exception {
        this.setBrowserExclusive();
        requestFileInformation(link);
        if (dlink == null) throw new PluginException(LinkStatus.ERROR_PLUGIN_DEFECT);
        dl = jd.plugins.BrowserAdapter.openDownload(br, link, dlink, true, 0);
        if (dl.getConnection().getContentType().contains("html")) {
            dl.getConnection().disconnect();
            throw new PluginException(LinkStatus.ERROR_FILE_NOT_FOUND);
        }
        dl.startDownload();
    }

    @Override
    public AvailableStatus requestFileInformation(DownloadLink link) throws Exception {
        this.setBrowserExclusive();
        br.getHeaders().put("User-Agent", RandomUserAgent.generate());
        br.setCookie("http://www.redtube.com", "language", "en");
        br.getPage(link.getDownloadURL());
        String fileName = br.getRegex("<h1 class=\"videoTitle\">(.*?)</h1>").getMatch(0);
        if (fileName == null) fileName = br.getRegex("<title>(.*?)- RedTube - Free Porn Videos</title>").getMatch(0);
        if (fileName != null) link.setName(fileName.trim() + ".flv");
        br.setFollowRedirects(true);
        dlink = br.getRegex("html5_vid.*?source src=\"(http.*?)(\"|%3D%22)").getMatch(0);
        if (dlink == null) dlink = br.getRegex("flv_h264_url=(http.*?)(\"|%3D%22)").getMatch(0);
        if (dlink == null) throw new PluginException(LinkStatus.ERROR_PLUGIN_DEFECT);
        dlink = Encoding.urlDecode(dlink, true);
        try {
            if (!br.openGetConnection(dlink).getContentType().contains("html")) {
                link.setDownloadSize(br.getHttpConnection().getLongContentLength());
                return AvailableStatus.TRUE;
            }
        } finally {
            try {
                br.getHttpConnection().disconnect();
            } catch (final Throwable e) {
            }
        }

        throw new PluginException(LinkStatus.ERROR_FILE_NOT_FOUND);
    }

    @Override
    public void reset() {

    }

    @Override
    public void resetDownloadlink(DownloadLink link) {

    }

}