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

@HostPlugin(revision = "$Revision$", interfaceVersion = 2, names = { "justin.tv" }, urls = { "http://(www\\.)?justindecrypted\\.tv/[a-z0-9\\-_]+/./\\d+" }, flags = { 0 })
public class JustinTv extends PluginForHost {

    private String  DLLINK  = null;

    private boolean blocked = false;

    public JustinTv(PluginWrapper wrapper) {
        super(wrapper);
    }

    public void correctDownloadLink(DownloadLink link) {
        link.setUrlDownload(link.getDownloadURL().replace("justindecrypted.tv", "justin.tv"));
    }

    @Override
    public String getAGBLink() {
        return "http://www.justin.tv/user/terms_of_service";
    }

    @Override
    public int getMaxSimultanFreeDownloadNum() {
        return -1;
    }

    @Override
    public void handleFree(DownloadLink downloadLink) throws Exception {
        requestFileInformation(downloadLink);
        if (blocked) throw new PluginException(LinkStatus.ERROR_HOSTER_TEMPORARILY_UNAVAILABLE, "Too many simultan downloads!", 10 * 60 * 1000l);
        dl = jd.plugins.BrowserAdapter.openDownload(br, downloadLink, DLLINK, true, -2);
        if (dl.getConnection().getContentType().contains("html")) {
            br.followConnection();
            throw new PluginException(LinkStatus.ERROR_PLUGIN_DEFECT);
        }
        dl.startDownload();
    }

    @Override
    public AvailableStatus requestFileInformation(DownloadLink downloadLink) throws IOException, PluginException {
        this.setBrowserExclusive();
        br.setFollowRedirects(true);

        String type = getType(downloadLink);
        URLConnectionAdapter con = null;
        // System.out.println(type);
        if ("c".equals(type)) {
            con = br.openGetConnection("http://api.justin.tv/api/broadcast/by_chapter/" + new Regex(downloadLink.getDownloadURL(), "(\\d+)$").getMatch(0) + ".xml");

        } else {
            con = br.openGetConnection("http://api.justin.tv/api/broadcast/show/" + new Regex(downloadLink.getDownloadURL(), "(\\d+)$").getMatch(0) + ".xml");

        }
        if (con.getResponseCode() == 400) {
            con.disconnect();
            blocked = true;
            return AvailableStatus.TRUE;
        }
        br.followConnection();
        if (br.containsHTML("<error>broadcast not found</error>")) throw new PluginException(LinkStatus.ERROR_FILE_NOT_FOUND);
        String filename = br.getRegex("<title>(.*?)</title>").getMatch(0);
        DLLINK = br.getRegex("<video_file_url>(http://.*?)</video_file_url>").getMatch(0);
        if (filename == null) throw new PluginException(LinkStatus.ERROR_PLUGIN_DEFECT);
        DLLINK = Encoding.htmlDecode(DLLINK);
        filename = filename.trim();
        if (downloadLink.getFinalFileName() == null) downloadLink.setFinalFileName(Encoding.htmlDecode(filename) + ".flv");
        if (DLLINK != null) {
            Browser br2 = br.cloneBrowser();
            // In case the link redirects to the finallink
            br2.setFollowRedirects(true);
            try {
                con = br2.openGetConnection(DLLINK);
                if (!con.getContentType().contains("html"))
                    downloadLink.setDownloadSize(con.getLongContentLength());
                else
                    throw new PluginException(LinkStatus.ERROR_FILE_NOT_FOUND);
            } finally {
                try {
                    con.disconnect();
                } catch (Throwable e) {
                }
            }
        }
        return AvailableStatus.TRUE;
    }

    private String getType(DownloadLink downloadLink) {

        return new Regex(downloadLink.getDownloadURL(), "\\.tv/[a-z0-9\\-_]+/(.)/\\d+").getMatch(0);
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