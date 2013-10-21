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
import jd.parser.Regex;
import jd.plugins.DownloadLink;
import jd.plugins.DownloadLink.AvailableStatus;
import jd.plugins.HostPlugin;
import jd.plugins.LinkStatus;
import jd.plugins.PluginException;
import jd.plugins.PluginForHost;

@HostPlugin(revision = "$Revision$", interfaceVersion = 2, names = { "5ilthy.com" }, urls = { "http://(www\\.)?5ilthy\\.com/(videos/\\d+/[a-z0-9\\-]+\\.html|playerConfig\\.php\\?[A-Za-z0-9]+\\.(flv|mp4))" }, flags = { 0 })
public class FiveIlthyCom extends PluginForHost {

    private String DLLINK = null;

    public FiveIlthyCom(PluginWrapper wrapper) {
        super(wrapper);
    }

    @Override
    public String getAGBLink() {
        return "http://5ilthy.com/terms.php";
    }

    @Override
    public int getMaxSimultanFreeDownloadNum() {
        return -1;
    }

    private static final String EMBEDLINK = "http://(www\\.)?5ilthy\\.com/playerConfig\\.php\\?[A-Za-z0-9]+\\.(flv|mp4)";

    @Override
    public AvailableStatus requestFileInformation(final DownloadLink downloadLink) throws IOException, PluginException {
        this.setBrowserExclusive();
        br.setFollowRedirects(true);
        br.getPage(downloadLink.getDownloadURL());
        String filename = null;
        if (downloadLink.getDownloadURL().matches(EMBEDLINK)) {
            filename = downloadLink.getStringProperty("5ilthydirectfilename", null);
            if (filename == null) filename = new Regex(downloadLink.getDownloadURL(), "([A-Za-z0-9]+)\\.(flv|mp4)$").getMatch(0);
            DLLINK = br.getRegex("flvMask:(http://[^<>\"]*?);").getMatch(0);
        } else {
            if (br.getURL().contains("?=index")) throw new PluginException(LinkStatus.ERROR_FILE_NOT_FOUND);
            filename = br.getRegex("<div class=\"vtitle\"><h2>([^<>\"]*?)</h2>").getMatch(0);
            if (filename == null) filename = br.getRegex("<title>([^<>\"]*?) at 5ilthy</title>").getMatch(0);
            DLLINK = br.getRegex("<param name=\"flashvars\" value=\"settings=(http://.*?)\"/>").getMatch(0);
            if (DLLINK == null) DLLINK = br.getRegex("settings=(http://(www\\.)?5ilthy\\.com/playerConfig\\.php\\?.*?)\"").getMatch(0);
            if (filename == null || DLLINK == null) throw new PluginException(LinkStatus.ERROR_PLUGIN_DEFECT);
            br.getPage(Encoding.htmlDecode(DLLINK));
            DLLINK = br.getRegex("defaultVideo:(http://.*?);").getMatch(0);
            if (DLLINK == null) DLLINK = br.getRegex("flvMask:(http://.*?);").getMatch(0);
        }
        if (DLLINK == null) throw new PluginException(LinkStatus.ERROR_PLUGIN_DEFECT);
        filename = filename.trim();
        String ext = DLLINK.substring(DLLINK.lastIndexOf("."));
        if (ext == null || ext.length() > 5) ext = ".flv";
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