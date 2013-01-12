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

import jd.PluginWrapper;
import jd.nutils.encoding.Encoding;
import jd.plugins.DownloadLink;
import jd.plugins.DownloadLink.AvailableStatus;
import jd.plugins.HostPlugin;
import jd.plugins.LinkStatus;
import jd.plugins.PluginException;
import jd.plugins.PluginForHost;

@HostPlugin(revision = "$Revision$", interfaceVersion = 2, names = { "subscene.com" }, urls = { "http://(www\\.)?subscene\\.com/(subtitles/[a-z0-9\\-]+/[a-z0-9]+/\\d+|[a-z0-9]+/[a-z0-9\\-]+/subtitle\\-\\d+\\.aspx)" }, flags = { 0 })
public class SubSceneCom extends PluginForHost {

    public SubSceneCom(PluginWrapper wrapper) {
        super(wrapper);
    }

    @Override
    public String getAGBLink() {
        return "http://subscene.com/";
    }

    @Override
    public AvailableStatus requestFileInformation(DownloadLink link) throws IOException, PluginException {
        this.setBrowserExclusive();
        br.setFollowRedirects(true);
        br.getPage(link.getDownloadURL());
        if (br.containsHTML("(>An error occurred while processing your request|>Server Error)")) throw new PluginException(LinkStatus.ERROR_FILE_NOT_FOUND);
        if ((br.containsHTML("<li class=\"deleted\">")) && (!br.containsHTML("mac"))) throw new PluginException(LinkStatus.ERROR_FILE_NOT_FOUND);
        String filename = br.getRegex("<title>([^<>\"]*?)\\- Subscene</title>").getMatch(0);
        if (filename == null) throw new PluginException(LinkStatus.ERROR_PLUGIN_DEFECT);
        link.setFinalFileName(Encoding.htmlDecode(filename.trim()) + ".zip");
        return AvailableStatus.TRUE;
    }

    @Override
    public void handleFree(DownloadLink downloadLink) throws Exception, PluginException {
        requestFileInformation(downloadLink);
        final String dllink = br.getRegex("\"(/subtitle/download\\?mac=[^<>\"]*?)\"").getMatch(0);
        if (dllink == null) throw new PluginException(LinkStatus.ERROR_PLUGIN_DEFECT);
        // Resume and chunks disabled, not needed for such small files & can't
        // test
        dl = jd.plugins.BrowserAdapter.openDownload(br, downloadLink, "http://subscene.com" + dllink, false, 1);
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