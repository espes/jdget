//    jDownloader - Downloadmanager
//    Copyright (C) 2009  JD-Team support@jdownloader.org
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
import jd.plugins.DownloadLink;
import jd.plugins.DownloadLink.AvailableStatus;
import jd.plugins.HostPlugin;
import jd.plugins.LinkStatus;
import jd.plugins.PluginException;
import jd.plugins.PluginForHost;

@HostPlugin(revision = "$Revision$", interfaceVersion = 2, names = { "imageshack.com", "imageshack.us" }, urls = { "https?://(www\\.)?imageshack\\.(us|com)/(i/[A-Za-z0-9]+|f/\\d+/[^<>\"/]+)", "z690hi09erhj6r0nrheswhrzogjrtehoDELETE_MEfhjtzjzjzthj" }, flags = { 0, 0 })
public class ImagesHackUs extends PluginForHost {

    public ImagesHackUs(PluginWrapper wrapper) {
        super(wrapper);
    }

    @Override
    public String getAGBLink() {
        return "http://reg.imageshack.us/content.php?page=rules";
    }

    // More is possible but 1 is good to prevent errors
    @Override
    public int getMaxSimultanFreeDownloadNum() {
        return 1;
    }

    public void correctDownloadLink(final DownloadLink link) {
        link.setUrlDownload(link.getDownloadURL().replace("imageshack.us/", "imageshack.com/").replace("http://", "https://"));
    }

    private static final String TYPE_DOWNLOAD = "https?://(www\\.)?imageshack\\.(us|com)/f/\\d+/[^<>\"/]+";
    private static final String TYPE_IMAGE    = "https?://(www\\.)?imageshack\\.(us|com)/i/[A-Za-z0-9]+";
    private String              DLLINK        = null;

    @Override
    public AvailableStatus requestFileInformation(final DownloadLink link) throws IOException, PluginException {
        this.setBrowserExclusive();
        if (link.getDownloadURL().matches(TYPE_DOWNLOAD)) {
            br.setFollowRedirects(true);
            br.getPage(link.getDownloadURL());
            if (br.containsHTML("Looks like the image is no longer here"))
                throw new PluginException(LinkStatus.ERROR_FILE_NOT_FOUND);
            DLLINK = br.getRegex("\"(https?://imageshack\\.us/download/[^<>\"]*?)\"").getMatch(0);
        } else {
            br.setFollowRedirects(false);
            br.getPage(link.getDownloadURL());
            if (br.getRedirectLocation() != null)
                throw new PluginException(LinkStatus.ERROR_FILE_NOT_FOUND);
            DLLINK = br.getRegex("data\\-width=\"0\" data\\-height=\"0\" alt=\"\" src=\"(//imagizer\\.imageshack[^<>\"]*?)\"").getMatch(0);
            if (DLLINK == null)
                throw new PluginException(LinkStatus.ERROR_PLUGIN_DEFECT);
            DLLINK = "http" + DLLINK;
        }
        br.setFollowRedirects(true);
        URLConnectionAdapter con = null;
        try {
            con = br.openGetConnection(DLLINK);
            if (con.getContentType().contains("html"))
                throw new PluginException(LinkStatus.ERROR_FILE_NOT_FOUND);
            link.setName(getFileNameFromHeader(con));
            link.setDownloadSize(con.getLongContentLength());
        } catch (final Throwable e) {
            try {
                con.disconnect();
            } catch (final Throwable e2) {
            }
        }
        return AvailableStatus.TRUE;
    }

    @Override
    public void handleFree(final DownloadLink downloadLink) throws Exception, PluginException {
        requestFileInformation(downloadLink);

        if (downloadLink.getDownloadURL().matches(TYPE_IMAGE)) {
            br.setFollowRedirects(true);
            br.getPage(downloadLink.getDownloadURL());
            DLLINK = br.getRegex("/rss\\+xml\" href=\"(.*?)\\.comments\\.xml\"").getMatch(0);
            if (DLLINK == null)
                throw new PluginException(LinkStatus.ERROR_PLUGIN_DEFECT);
        }

        // More is possible but 1 chunk is good to prevent errors
        dl = jd.plugins.BrowserAdapter.openDownload(br, downloadLink, DLLINK, true, 1);
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

}