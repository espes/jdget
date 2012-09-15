//jDownloader - Downloadmanager
//Copyright (C) 2012  JD-Team support@jdownloader.org
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
import java.text.SimpleDateFormat;
import java.util.Date;

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

import org.appwork.utils.Hash;

@HostPlugin(revision = "$Revision$", interfaceVersion = 2, names = { "judgeporn.com" }, urls = { "http://(www\\.)?judgeporn.com/videos/[a-z0-9\\-_]+" }, flags = { 0 })
public class JudgePornCom extends PluginForHost {

    private String DLLINK = null;
    private String AHV    = "ZmFlZWViODhhMzkwZDk3NzAyNWRhMjlhNTEyNTQ2MDA=";

    public JudgePornCom(PluginWrapper wrapper) {
        super(wrapper);
    }

    private String checkMD(final String videoUrl, final String time) {
        return Hash.getMD5(videoUrl + time + Encoding.Base64Decode(AHV));
    }

    private String checkMD2(final String time) {
        return Hash.getMD5(time + Encoding.Base64Decode(AHV));
    }

    private String checkTM() {
        final Date dt = new Date();
        final SimpleDateFormat df = new SimpleDateFormat("yyyyMMddHHmmss");
        return df.format(dt);
    }

    @Override
    public String getAGBLink() {
        return "http://www.judgeporn.com/terms.php";
    }

    @Override
    public AvailableStatus requestFileInformation(DownloadLink link) throws IOException, PluginException {
        this.setBrowserExclusive();
        br.setFollowRedirects(true);
        br.getPage(link.getDownloadURL());
        if (br.getURL().equals("http://www.judgeporn.com/categories/") || br.containsHTML("<title>Free Hardcore Porn Videos and Porn Fucking Movies</title>")) throw new PluginException(LinkStatus.ERROR_FILE_NOT_FOUND);
        String filename = br.getRegex("<h1 class=\"block_header\">([^<>\"]*?)</h1>").getMatch(0);
        if (filename == null) filename = br.getRegex("<title>([^<>\"]*?) \\- JudgePorn\\.com</title>").getMatch(0);
        if (filename == null) throw new PluginException(LinkStatus.ERROR_PLUGIN_DEFECT);
        DLLINK = br.getRegex("video_url=(http://.*?)\\&amp;preview_url").getMatch(0);
        if (DLLINK == null) {
            DLLINK = br.getRegex("video_url:\\s?\'(http://[^\',]+)").getMatch(0);
        }
        if (filename == null || DLLINK == null) { throw new PluginException(LinkStatus.ERROR_PLUGIN_DEFECT); }
        DLLINK = Encoding.htmlDecode(DLLINK);
        filename = filename.trim();
        String ext = DLLINK.substring(DLLINK.lastIndexOf(".")).replaceAll("\\W", "");
        if (ext == null || ext.length() > 5) {
            ext = "flv";
        }
        link.setFinalFileName(Encoding.htmlDecode(filename) + "." + ext);

        final String time = checkTM();
        final String ahv = checkMD(DLLINK, time);
        DLLINK = DLLINK + "?time=" + time + "&ahv=" + ahv + "&cv=" + checkMD2(time);

        final Browser br2 = br.cloneBrowser();
        // In case the link redirects to the finallink
        br2.setFollowRedirects(true);
        URLConnectionAdapter con = null;
        try {
            con = br2.openGetConnection(DLLINK);
            if (!con.getContentType().contains("html")) {
                link.setDownloadSize(con.getLongContentLength());
            } else {
                throw new PluginException(LinkStatus.ERROR_FILE_NOT_FOUND);
            }
            return AvailableStatus.TRUE;
        } finally {
            try {
                con.disconnect();
            } catch (final Throwable e) {
            }
        }
    }

    @Override
    public void handleFree(DownloadLink downloadLink) throws Exception, PluginException {
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
    public int getMaxSimultanFreeDownloadNum() {
        return -1;
    }

    @Override
    public void resetDownloadlink(DownloadLink link) {
    }

}