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

@HostPlugin(revision = "$Revision$", interfaceVersion = 2, names = { "fux.com" }, urls = { "http://(www\\.)?fux\\.com/(video|embed)/\\d+" }, flags = { 0 })
public class FuxCom extends PluginForHost {

    public FuxCom(PluginWrapper wrapper) {
        super(wrapper);
    }

    @Override
    public String getAGBLink() {
        return "http://www.fux.com/legal/tos";
    }

    @Override
    public int getMaxSimultanFreeDownloadNum() {
        return -1;
    }

    public void correctDownloadLink(final DownloadLink link) {
        link.setUrlDownload(link.getDownloadURL().replace("fux.com/embed/", "fux.com/video/"));
    }

    @Override
    public AvailableStatus requestFileInformation(final DownloadLink downloadLink) throws IOException, PluginException {
        this.setBrowserExclusive();
        br.setFollowRedirects(true);
        br.getPage(downloadLink.getDownloadURL());
        if (br.containsHTML("(<title>Fux \\- Error \\- Page not found</title>|<h2>Page<br />not found</h2>|We can\\'t find that page you\\'re looking for|<h3>Oops\\!</h3>|class='videoNotAvailable')") || br.getURL().matches(".+/video\\?error=\\d+")) throw new PluginException(LinkStatus.ERROR_FILE_NOT_FOUND);
        String filename = br.getRegex("<h1>(.*?)</h1>").getMatch(0);
        if (filename == null) {
            filename = br.getRegex("<title>(.*?) \\- FUX</title>").getMatch(0);
        }
        final Regex info = br.getRegex("\\$\\.ajax\\(url, opts\\);[\t\n\r ]+\\}[\t\n\r ]+\\}\\)\\((\\d+), \\d+, \\[(.*?)\\]\\);");
        final String mediaID = info.getMatch(0);
        String availablequalities = info.getMatch(1);
        if (availablequalities != null) {
            availablequalities = availablequalities.replace(",", "+");
        } else {
            availablequalities = "1080+720+480+360+240";
        }
        if (mediaID == null || filename == null) throw new PluginException(LinkStatus.ERROR_PLUGIN_DEFECT);
        br.getHeaders().put("Accept", "application/json, text/javascript, */*; q=0.01");
        br.getHeaders().put("Origin", "http://www.fux.com");
        br.postPage("http://tkn.fux.com/" + mediaID + "/desktop/" + availablequalities, "");
        // seems to be listed in order highest quality to lowest. 20130513
        String DLLINK = getDllink();
        if (DLLINK == null) {
            logger.warning("Couldn't find 'DDLINK'");
            throw new PluginException(LinkStatus.ERROR_PLUGIN_DEFECT);
        }
        DLLINK = Encoding.htmlDecode(DLLINK);
        filename = Encoding.htmlDecode(filename.trim());
        if (DLLINK.contains(".m4v"))
            downloadLink.setFinalFileName(filename + ".m4v");
        else if (DLLINK.contains(".mp4")) {
            downloadLink.setFinalFileName(filename + ".mp4");
        } else
            downloadLink.setFinalFileName(filename + ".flv");
        // In case the link redirects to the finallink
        br.setFollowRedirects(true);
        URLConnectionAdapter con = null;
        try {
            con = br.openGetConnection(DLLINK.trim());
            if (!con.getContentType().contains("html")) {
                downloadLink.setDownloadSize(con.getLongContentLength());
                downloadLink.setProperty("DDLink", br.getURL());
            } else {
                throw new PluginException(LinkStatus.ERROR_FILE_NOT_FOUND);
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
        dl = jd.plugins.BrowserAdapter.openDownload(br, downloadLink, downloadLink.getStringProperty("DDLink"), true, 0);
        if (dl.getConnection().getContentType().contains("html")) {
            br.followConnection();
            throw new PluginException(LinkStatus.ERROR_PLUGIN_DEFECT);
        }
        dl.startDownload();
    }

    final String getDllink() {
        String finallink = null;
        final String[] qualities = new String[] { "1080", "720", "480", "360", "240" };
        for (final String quality : qualities) {
            if (br.containsHTML("\"" + quality + "\"")) {
                finallink = br.getRegex("\"" + quality + "\":\\{\"status\":\"success\",\"token\":\"(http[^<>\"]*?)\"").getMatch(0);
                if (finallink != null && checkDirectLink(finallink) != null) break;
            }
        }
        /* Hm probably this is only needed if only one quality exists */
        if (finallink == null) finallink = br.getRegex("\"token\":\"(http://[^<>\"]*?)\"").getMatch(0);
        return finallink;
    }

    private String checkDirectLink(String directlink) {
        if (directlink != null) {
            try {
                final Browser br2 = br.cloneBrowser();
                URLConnectionAdapter con = br2.openGetConnection(directlink);
                if (con.getContentType().contains("html") || con.getLongContentLength() == -1) {
                    directlink = null;
                }
                con.disconnect();
            } catch (final Exception e) {
                directlink = null;
            }
        }
        return directlink;
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