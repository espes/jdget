//jDownloader - Downloadmanager
//Copyright (C) 2009  JD-Team support@jdownloader.org
//
//This program is free software: you can redistribute it and/or modify
//it under the terms of the GNU General Public License as published by
//the Free Software Foundation, either version 3 of the License, or
//(at your option) any later version.
//
//This program is distributed in the hope that it will be useful,
//but WITHOUT ANY WARRANTY; without even the implied warranty of
//MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
//GNU General Public License for more details.
//
//You should have received a copy of the GNU General Public License
//along with this program.  If not, see <http://www.gnu.org/licenses/>.

package jd.plugins.hoster;

import java.io.IOException;
import java.util.HashMap;

import jd.PluginWrapper;
import jd.http.Browser;
import jd.http.URLConnectionAdapter;
import jd.nutils.encoding.Encoding;
import jd.plugins.DownloadLink;
import jd.plugins.DownloadLink.AvailableStatus;
import jd.plugins.HostPlugin;
import jd.plugins.LinkStatus;
import jd.plugins.PluginException;
import jd.plugins.PluginForHost;
import jd.plugins.download.DownloadInterface;

@HostPlugin(revision = "$Revision$", interfaceVersion = 2, names = { "deredactie.be", "sporza.be" }, urls = { "http://(www\\.)?deredactie\\.be/(permalink/\\d\\.\\d+|cm/vrtnieuws([^/]+)?/(mediatheek|videozone).+)", "http://(www\\.)?sporza\\.be/(permalink/\\d\\.\\d+|cm/(vrtnieuws|sporza)([^/]+)?/(mediatheek|videozone).+)" }, flags = { 0, 0 })
public class DeredactieBe extends PluginForHost {

    public DeredactieBe(PluginWrapper wrapper) {
        super(wrapper);
    }

    private String DLLINK = null;

    @Override
    public String getAGBLink() {
        return "http://deredactie.be/";
    }

    @Override
    public void correctDownloadLink(DownloadLink link) {
        link.setUrlDownload(link.getDownloadURL().replaceAll("/cm/vrtnieuws/mediatheek/[^/]+/[^/]+/[^/]+/([0-9\\.]+)(.+)?", "/permalink/$1"));
        link.setUrlDownload(link.getDownloadURL().replaceAll("/cm/vrtnieuws([^/]+)?/mediatheek(\\w+)?/([0-9\\.]+)(.+)?", "/permalink/$3"));
    }

    @Override
    public AvailableStatus requestFileInformation(DownloadLink downloadLink) throws IOException, PluginException {
        this.setBrowserExclusive();
        br.setFollowRedirects(true);
        br.getPage(downloadLink.getDownloadURL());
        // Link offline
        if (br.containsHTML("(>Pagina \\- niet gevonden<|>De pagina die u zoekt kan niet gevonden worden)")) throw new PluginException(LinkStatus.ERROR_FILE_NOT_FOUND);

        HashMap<String, String> mediaValue = new HashMap<String, String>();
        for (String[] s : br.getRegex("data\\-video\\-([^=]+)=\"([^\"]+)\"").getMatches()) {
            mediaValue.put(s[0], s[1]);
        }
        // Nothing to download
        if (mediaValue == null || mediaValue.size() == 0) throw new PluginException(LinkStatus.ERROR_FILE_NOT_FOUND);

        DLLINK = mediaValue.get("src");
        final String filename = mediaValue.get("title");
        if (DLLINK == null || filename == null) throw new PluginException(LinkStatus.ERROR_PLUGIN_DEFECT);

        String ext = DLLINK.substring(DLLINK.lastIndexOf("."));
        if (ext == null || ext.length() > 5) ext = ".flv";
        downloadLink.setFinalFileName(Encoding.htmlDecode(filename.trim()).replaceAll("\"", "") + ext);
        Browser br2 = br.cloneBrowser();
        // In case the link redirects to the finallink
        br2.setFollowRedirects(true);
        URLConnectionAdapter con = null;
        try {
            con = br2.openGetConnection(DLLINK);
            if (!con.getContentType().contains("html")) {
                downloadLink.setDownloadSize(con.getLongContentLength());
            } else {
                DLLINK = mediaValue.get("rtmp-server");
                DLLINK = DLLINK != null && mediaValue.get("rtmp-path") != null ? DLLINK + "@" + mediaValue.get("rtmp-path") : null;
                if (DLLINK == null) throw new PluginException(LinkStatus.ERROR_FILE_NOT_FOUND);
            }
            return AvailableStatus.TRUE;
        } finally {
            try {
                con.disconnect();
            } catch (Throwable e) {
            }
        }
    }

    @Override
    public void handleFree(DownloadLink downloadLink) throws Exception {
        requestFileInformation(downloadLink);
        if (DLLINK.startsWith("rtmp")) {
            dl = new RTMPDownload(this, downloadLink, DLLINK);
            setupRTMPConnection(dl);
            ((RTMPDownload) dl).startDownload();
        } else {
            dl = jd.plugins.BrowserAdapter.openDownload(br, downloadLink, DLLINK, false, 1);
            if (dl.getConnection().getContentType().contains("html")) {
                br.followConnection();
                throw new PluginException(LinkStatus.ERROR_PLUGIN_DEFECT);
            }
            dl.startDownload();
        }
    }

    private void setupRTMPConnection(final DownloadInterface dl) {
        final jd.network.rtmp.url.RtmpUrlConnection rtmp = ((RTMPDownload) dl).getRtmpConnection();
        rtmp.setPlayPath(DLLINK.split("@")[1]);
        rtmp.setUrl(DLLINK.split("@")[0]);
        rtmp.setSwfVfy("http://www.deredactie.be/html/flash/common/streamsense_jwp-v1.swf");
        rtmp.setResume(true);
        rtmp.setRealTime();
    }

    @Override
    public int getMaxSimultanFreeDownloadNum() {
        return -1;
    }

    @Override
    public void reset() {
    }

    @Override
    public void resetPluginGlobals() {
    }

    @Override
    public void resetDownloadlink(DownloadLink link) {
    }

}