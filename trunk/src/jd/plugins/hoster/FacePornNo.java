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
import jd.utils.locale.JDL;

@HostPlugin(revision = "$Revision$", interfaceVersion = 2, names = { "faceporn.no" }, urls = { "http://(www\\.)?faceporn\\.no/video/[a-z0-9\\-]+" }, flags = { 0 })
public class FacePornNo extends PluginForHost {

    private String DLLINK = null;

    public FacePornNo(PluginWrapper wrapper) {
        super(wrapper);
    }

    @Override
    public String getAGBLink() {
        return "http://www.faceporn.no/legal/docs/terms";
    }

    @Override
    public int getMaxSimultanFreeDownloadNum() {
        return -1;
    }

    private static final String NOTDOWNLOADABLE = JDL.L("hoster.facepornno.notdownloadable", "Not downloadable: You have to pay to download this video");

    @Override
    public AvailableStatus requestFileInformation(DownloadLink downloadLink) throws IOException, PluginException {
        this.setBrowserExclusive();
        br.setFollowRedirects(true);
        br.getPage(downloadLink.getDownloadURL());
        if (br.getURL().equals("http://www.faceporn.no/") || br.containsHTML("(>An error occurred<|>The video you tried to watch has been deleted, and is no longer available<|<title>Faceporn \\| Adult social porn community\\. Free blowjobs for everyone\\!</title>)")) throw new PluginException(LinkStatus.ERROR_FILE_NOT_FOUND);
        if (br.getURL().contains("faceporn.no/items/buy/")) {
            String filename = br.getRegex("<div class=\"label\">Title:</div>[\t\n\r ]+<div class=\"value\">([^<>\"]*?)</div>").getMatch(0);
            if (filename == null) throw new PluginException(LinkStatus.ERROR_PLUGIN_DEFECT);
            filename = Encoding.htmlDecode(filename.trim() + ".mp4");
            downloadLink.setFinalFileName(filename);
            downloadLink.getLinkStatus().setStatusText(NOTDOWNLOADABLE);
            return AvailableStatus.TRUE;
        } else {
        }
        String filename = br.getRegex("class=\"h2\"><div class=\"title\">(.*?)</div></div>").getMatch(0);
        if (filename == null) {
            filename = br.getRegex("<div class=\"content text\">(.*?)</div>").getMatch(0);
            if (filename == null) {
                filename = br.getRegex("<title>Faceporn \\| (.*?)</title>").getMatch(0);
            }
        }
        String linkPart = br.getRegex("createPlayer\\(\"(http://.*?)\"\\);").getMatch(0);
        String token = br.getRegex("token:\"(.*?)\"").getMatch(0);
        if (filename == null || linkPart == null || token == null) throw new PluginException(LinkStatus.ERROR_PLUGIN_DEFECT);
        DLLINK = linkPart + "?start=0&id=player1&client=FLASH%20WIN%2011,0,1,152&version=4.2.95&width=928&token=" + token;
        DLLINK = Encoding.htmlDecode(DLLINK);
        filename = filename.trim();
        String ext = DLLINK.substring(DLLINK.lastIndexOf("."));
        if (ext == null || ext.length() > 5) ext = ".mp4";
        downloadLink.setFinalFileName(Encoding.htmlDecode(filename) + ext);
        Browser br2 = br.cloneBrowser();
        // In case the link redirects to the finallink
        br2.setFollowRedirects(true);
        URLConnectionAdapter con = null;
        try {
            con = br2.openGetConnection(DLLINK);
            if (!con.getContentType().contains("html"))
                downloadLink.setDownloadSize(con.getLongContentLength());
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

    @Override
    public void handleFree(DownloadLink downloadLink) throws Exception {
        requestFileInformation(downloadLink);
        if (br.getURL().contains("faceporn.no/items/buy/")) throw new PluginException(LinkStatus.ERROR_FATAL, NOTDOWNLOADABLE);
        dl = jd.plugins.BrowserAdapter.openDownload(br, downloadLink, DLLINK, true, 0);
        if (dl.getConnection().getContentType().contains("html")) {
            br.followConnection();
            throw new PluginException(LinkStatus.ERROR_PLUGIN_DEFECT);
        }
        dl.startDownload();
    }

    @Override
    public void reset() {
    }

    @Override
    public void resetDownloadlink(DownloadLink link) {
    }

    @Override
    public void resetPluginGlobals() {
    }
}