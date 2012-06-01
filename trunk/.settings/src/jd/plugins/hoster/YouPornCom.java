//    jDownloader - Downloadmanager
//    Copyright (C) 2008  JD-Team support@jdownloader.org
//
//    This program is free software: you can redistribute it and/or modify
//    it under the terms of the GNU General Public License as published by
//    the Free Software Foundation, either version 3 of the License, or
//    (at your option) any later version.
//
//    This program is distributed in the hope that it will be useful,
//    but WITHOUT ANY WARRANTY; without even the implied warranty of
//    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
//    GNU General Public License for more details.
//
//    You should have received a copy of the GNU General Public License
//    along with this program.  If not, see <http://www.gnu.org/licenses/>.

package jd.plugins.hoster;

import java.io.IOException;

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

@HostPlugin(revision = "$Revision: 15978 $", interfaceVersion = 2, names = { "youporn.com" }, urls = { "http://[\\w\\.]*?youporn\\.com/watch/\\d+/?.+/?" }, flags = { 0 })
public class YouPornCom extends PluginForHost {

    String DLLINK = null;

    public YouPornCom(PluginWrapper wrapper) {
        super(wrapper);
    }

    public String getAGBLink() {
        return "http://youporn.com/terms";
    }

    public int getMaxSimultanFreeDownloadNum() {
        return -1;
    }

    public void handleFree(DownloadLink link) throws Exception {
        requestFileInformation(link);
        dl = jd.plugins.BrowserAdapter.openDownload(br, link, DLLINK, true, 0);
        if (dl.getConnection().getContentType().contains("html")) {
            br.followConnection();
            throw new PluginException(LinkStatus.ERROR_PLUGIN_DEFECT);
        }
        dl.startDownload();
    }

    public AvailableStatus requestFileInformation(DownloadLink parameter) throws IOException, PluginException {
        this.setBrowserExclusive();
        br.setFollowRedirects(true);
        br.setCookie("http://youporn.com/", "age_verified", "1");
        br.setCookie("http://youporn.com/", "is_pc", "1");
        br.getPage(parameter.getDownloadURL());
        if (br.getRedirectLocation() != null) br.getPage(br.getRedirectLocation());
        if (br.containsHTML("invalid video_id")) throw new PluginException(LinkStatus.ERROR_FILE_NOT_FOUND);
        String filename = br.getRegex("<title>(.*?) - Free Porn Videos - YouPorn</title>").getMatch(0);
        if (filename == null) filename = br.getRegex("addthis:title=\"YouPorn - (.*?)\"></a>").getMatch(0);
        DLLINK = br.getRegex("\"(http://[^<>\"\\']+)\">MP4").getMatch(0);
        if (DLLINK == null) DLLINK = br.getRegex("\"(http://videos\\-\\d+\\.youporn\\.com/[^<>\"\\'/]+/save/scene_h264[^<>\"\\']+)\"").getMatch(0);
        if (DLLINK == null || filename == null) throw new PluginException(LinkStatus.ERROR_PLUGIN_DEFECT);
        DLLINK = Encoding.htmlDecode(DLLINK);
        parameter.setFinalFileName(Encoding.htmlDecode(filename).trim().replaceAll("   ", "-") + ".mp4");
        Browser br2 = br.cloneBrowser();
        // In case the link redirects to the finallink
        br2.setFollowRedirects(true);
        URLConnectionAdapter con = null;
        try {
            con = br2.openGetConnection(DLLINK);
            if (!con.getContentType().contains("html"))
                parameter.setDownloadSize(con.getLongContentLength());
            else
                throw new PluginException(LinkStatus.ERROR_FILE_NOT_FOUND);
            return AvailableStatus.TRUE;
        } finally {
            try {
                con.disconnect();
            } catch (Throwable e) {
            }
        }
    }

    public void reset() {
    }

    public void resetDownloadlink(DownloadLink link) {
    }
}