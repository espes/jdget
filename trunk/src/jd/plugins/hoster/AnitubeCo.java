//    jDownloader - Downloadmanager
//    Copyright (C) 2011  JD-Team support@jdownloader.org
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
import jd.http.URLConnectionAdapter;
import jd.nutils.encoding.Encoding;
import jd.parser.Regex;
import jd.plugins.DownloadLink;
import jd.plugins.DownloadLink.AvailableStatus;
import jd.plugins.HostPlugin;
import jd.plugins.LinkStatus;
import jd.plugins.PluginException;
import jd.plugins.PluginForHost;

@HostPlugin(revision = "$Revision$", interfaceVersion = 2, names = { "anitube.co" }, urls = { "http://(www\\.)?anitube\\.(co|tv|com\\.br)/video/\\d+" }, flags = { 0 })
public class AnitubeCo extends PluginForHost {

    private String dllink = null;

    public AnitubeCo(final PluginWrapper wrapper) {
        super(wrapper);
    }

    @Override
    public String getAGBLink() {
        return "http://www.anitube.co/terms";
    }

    @Override
    public void correctDownloadLink(final DownloadLink link) {
        link.setUrlDownload(link.getDownloadURL().replaceFirst("\\.(tv|com\\.br)", ".co"));
    }

    @Override
    public void handleFree(final DownloadLink downloadLink) throws Exception {
        requestFileInformation(downloadLink);
        dl = jd.plugins.BrowserAdapter.openDownload(br, downloadLink, dllink, true, 0);
        if (dl.getConnection().getContentType().contains("html")) {
            dl.getConnection().disconnect();
            throw new PluginException(LinkStatus.ERROR_FILE_NOT_FOUND);
        }
        dl.startDownload();
    }

    @Override
    public AvailableStatus requestFileInformation(final DownloadLink downloadLink) throws IOException, PluginException {
        setBrowserExclusive();
        br.setFollowRedirects(true);
        br.getPage(downloadLink.getDownloadURL());
        // provider blocks some subnets on http gateway, unknown what ranges.
        if (br.containsHTML(">403 Forbidden<") && br.containsHTML(">nginx/[\\d+\\.]+<")) throw new PluginException(LinkStatus.ERROR_FATAL, "IP Blocked: Provider prevents access based on IP address.");

        if (br.getURL().contains("error.php?type=video_missing")) throw new PluginException(LinkStatus.ERROR_FILE_NOT_FOUND);
        Regex match = br.getRegex("(http://(?:www\\.)?anitube\\.co/[^/]+)/config\\.php\\?key=([0-9a-f]+)'");
        if (match.count() > 0) {
            br.getPage(match.getMatch(0) + "/playlist.php?key=" + match.getMatch(1));
            dllink = br.getRegex("<file>\\s*(http://[^<]+\\d+\\.flv)\\s*</file>").getMatch(0);
            String filename = br.getRegex("<title>\\s*([^<]+)\\s*</title>").getMatch(0);
            if (filename == null || dllink == null) throw new PluginException(LinkStatus.ERROR_PLUGIN_DEFECT);
            URLConnectionAdapter con = null;
            try {
                con = br.openGetConnection(dllink);
                if (!con.getContentType().contains("html")) {
                    downloadLink.setDownloadSize(con.getLongContentLength());
                    filename = filename.trim();
                    downloadLink.setFinalFileName(Encoding.htmlDecode(filename) + ".mp4");
                } else {
                    throw new PluginException(LinkStatus.ERROR_FILE_NOT_FOUND);
                }
            } finally {
                try {
                    con.disconnect();
                } catch (final Throwable e) {
                }
            }
        } else {
            throw new PluginException(LinkStatus.ERROR_PLUGIN_DEFECT);
        }
        return AvailableStatus.TRUE;
    }

    @Override
    public int getMaxSimultanFreeDownloadNum() {
        return -1;
    }

    @Override
    public void reset() {
    }

    @Override
    public void resetDownloadlink(final DownloadLink link) {
    }

}