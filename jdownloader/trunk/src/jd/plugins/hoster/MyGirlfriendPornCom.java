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

@HostPlugin(revision = "$Revision$", interfaceVersion = 2, names = { "mygirlfriendporn.com" }, urls = { "http://(www\\.)?mygirlfriendporn\\.com/videos/[A-Za-z0-9\\-_]+\\d+\\.html" }, flags = { 0 })
public class MyGirlfriendPornCom extends PluginForHost {

    public MyGirlfriendPornCom(PluginWrapper wrapper) {
        super(wrapper);
    }

    private String DLLINK = null;

    @Override
    public String getAGBLink() {
        return "http://www.mygirlfriendporn.com/";
    }

    @Override
    public AvailableStatus requestFileInformation(final DownloadLink downloadLink) throws IOException, PluginException {
        this.setBrowserExclusive();
        br.setFollowRedirects(true);
        br.getPage(downloadLink.getDownloadURL());
        if (br.getURL().equals("http://www.mygirlfriendporn.com/404.php") || br.getURL().contains("main.htm") || !br.getURL().contains("mygirlfriendporn.com/") || br.containsHTML(">404: File Not Found<|The file you have requested was not found on this server")) throw new PluginException(LinkStatus.ERROR_FILE_NOT_FOUND);
        String filename = br.getRegex("22px; margin\\-top: 0px; padding\\-top: 0px; \">([^<>\"]*?)</h1>").getMatch(0);
        if (filename == null) filename = br.getRegex("<title>([^<>\"]*?)</title>").getMatch(0);
        if (filename == null) throw new PluginException(LinkStatus.ERROR_PLUGIN_DEFECT);
        DLLINK = br.getRegex("(http://(www\\.)?mygirlfriendporn\\.com/playerConfig\\.php\\?[^<>\"/\\&]*?)\"").getMatch(0);
        if (DLLINK == null) throw new PluginException(LinkStatus.ERROR_PLUGIN_DEFECT);
        br.getPage(Encoding.htmlDecode(DLLINK));
        DLLINK = br.getRegex("flvMask:(http://[^<>\"]*?);").getMatch(0);
        if (DLLINK == null) throw new PluginException(LinkStatus.ERROR_PLUGIN_DEFECT);
        DLLINK = Encoding.htmlDecode(DLLINK);
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
    public void handleFree(final DownloadLink downloadLink) throws Exception {
        requestFileInformation(downloadLink);
        dl = jd.plugins.BrowserAdapter.openDownload(br, downloadLink, DLLINK, true, 0);
        if (dl.getConnection().getContentType().contains("html")) {
            br.followConnection();
            throw new PluginException(LinkStatus.ERROR_PLUGIN_DEFECT);
        }
        dl.startDownload();
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
