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
import jd.nutils.encoding.Encoding;
import jd.parser.Regex;
import jd.plugins.DownloadLink;
import jd.plugins.DownloadLink.AvailableStatus;
import jd.plugins.HostPlugin;
import jd.plugins.LinkStatus;
import jd.plugins.PluginException;
import jd.plugins.PluginForHost;

@HostPlugin(revision = "$Revision: 16362 $", interfaceVersion = 2, names = { "altervideo.net" }, urls = { "http://(www\\.)?altervideo\\.net/video/[a-z0-9]+" }, flags = { 0 })
public class AlterVideoNet extends PluginForHost {

    private static final String NORESUME = null;
    private static final String NOCHUNKS = null;

    public AlterVideoNet(PluginWrapper wrapper) {
        super(wrapper);
    }

    private String DLLINK = null;

    @Override
    public String getAGBLink() {
        return "http://www.altervideo.net/contact-us.php";
    }

    @Override
    public AvailableStatus requestFileInformation(DownloadLink downloadLink) throws IOException, PluginException {
        this.setBrowserExclusive();
        br.setFollowRedirects(true);
        br.getPage(downloadLink.getDownloadURL());
        if (br.containsHTML("Video not found<")) throw new PluginException(LinkStatus.ERROR_FILE_NOT_FOUND);
        downloadLink.setFinalFileName(Encoding.htmlDecode(new Regex(downloadLink.getDownloadURL(), "([a-z0-9]+)$").getMatch(0)) + ".flv");
        return AvailableStatus.TRUE;
    }

    @Override
    public void handleFree(DownloadLink downloadLink) throws Exception {
        requestFileInformation(downloadLink);
        final String c = br.getRegex("type=\"hidden\" value=\"([a-z0-9]+)\" name=\"c\"").getMatch(0);
        if (c == null) throw new PluginException(LinkStatus.ERROR_PLUGIN_DEFECT);
        br.postPage(downloadLink.getDownloadURL(), "c=" + c + "&choice=Download");
        DLLINK = br.getRegex("\\'(http://av\\d+\\.altervideo\\.net/files/[a-z0-9]+/[a-z0-9]+/altervideo/[a-z0-9]+\\.flv)\\'").getMatch(0);
        if (DLLINK == null) DLLINK = br.getRegex("document\\.location\\.href=\\'(http://[^<>\"]*?)\\'").getMatch(0);
        if (DLLINK == null) throw new PluginException(LinkStatus.ERROR_PLUGIN_DEFECT);
        dl = jd.plugins.BrowserAdapter.openDownload(br, downloadLink, DLLINK, true, 0);
        if (dl.getConnection().getContentType().contains("html")) {
            br.followConnection();
            throw new PluginException(LinkStatus.ERROR_PLUGIN_DEFECT);
        }
        jd.plugins.BrowserAdapter.openDownload(br, downloadLink, DLLINK, false, 1);
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
