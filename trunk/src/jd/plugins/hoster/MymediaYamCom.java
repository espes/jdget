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
import jd.utils.locale.JDL;

@HostPlugin(revision = "$Revision$", interfaceVersion = 2, names = { "mymedia.yam.com" }, urls = { "http://(www\\.)?mymediadecrypted\\.yam\\.com/m/\\d+" }, flags = { 0 })
public class MymediaYamCom extends PluginForHost {

    public MymediaYamCom(PluginWrapper wrapper) {
        super(wrapper);
    }

    private String DLLINK = null;

    @Override
    public String getAGBLink() {
        return "http://mymedia.yam.com/";
    }

    public void correctDownloadLink(DownloadLink link) {
        link.setUrlDownload(link.getDownloadURL().replace("mymediadecrypted.yam.com/", "mymedia.yam.com/"));
    }

    private static final String MAINTENANCE         = ">進行系統維護作業，<br";
    private static final String MAINTENANCEUSERTEXT = JDL.L("hoster.mymediayamcom.undermaintenance", "This server is under Maintenance");

    @Override
    public AvailableStatus requestFileInformation(final DownloadLink downloadLink) throws IOException, PluginException {
        this.setBrowserExclusive();
        br.setFollowRedirects(true);
        br.setCustomCharset("utf-8");
        br.getPage(downloadLink.getDownloadURL());
        if (br.containsHTML("使用者影音平台存取發生錯誤<|該使用者影音平台關閉中<")) throw new PluginException(LinkStatus.ERROR_FILE_NOT_FOUND);
        if (br.containsHTML(MAINTENANCE)) {
            downloadLink.getLinkStatus().setStatusText(MAINTENANCEUSERTEXT);
            return AvailableStatus.TRUE;
        }
        String filename = br.getRegex("class=\"heading\"><span style=\\'float:left;\\'>([^<>\"]*?)</span>").getMatch(0);
        if (filename == null) filename = br.getRegex("<title>yam 天空部落-影音分享\\-(.*?)</title>").getMatch(0);
        if (br.containsHTML("type=\"password\" id=\"passwd\" name=\"passwd\"")) {
            if (filename == null) filename = new Regex(downloadLink.getDownloadURL(), "(\\d+)$").getMatch(0);
            downloadLink.getLinkStatus().setStatusText("Password protected links aren't supported yet. Please contact our support!");
            downloadLink.setName(Encoding.htmlDecode(filename.trim()));
            return AvailableStatus.TRUE;
        }
        if (filename == null) throw new PluginException(LinkStatus.ERROR_PLUGIN_DEFECT);
        br.getPage("http://mymedia.yam.com/api/a/?pID=" + new Regex(downloadLink.getDownloadURL(), "(\\d+)$").getMatch(0));
        DLLINK = br.getRegex("mp3file=(http://.+\\.mp3)").getMatch(0);
        if (DLLINK == null || DLLINK.length() > 200) throw new PluginException(LinkStatus.ERROR_PLUGIN_DEFECT);
        DLLINK = Encoding.htmlDecode(DLLINK);
        filename = filename.trim();
        String ext = DLLINK.substring(DLLINK.lastIndexOf("."));
        if (ext == null || ext.length() > 5) ext = ".mp3";
        downloadLink.setFinalFileName(Encoding.htmlDecode(filename) + ext);
        final Browser br2 = br.cloneBrowser();
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
        if (br.containsHTML(MAINTENANCE)) throw new PluginException(LinkStatus.ERROR_TEMPORARILY_UNAVAILABLE, MAINTENANCEUSERTEXT, 2 * 60 * 60 * 1000l);
        if (br.containsHTML("type=\"password\" id=\"passwd\" name=\"passwd\"")) throw new PluginException(LinkStatus.ERROR_FATAL, "Password protected links aren't supported yet. Please contact our support!");
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
