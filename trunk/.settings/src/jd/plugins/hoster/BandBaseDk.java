//jDownloader - Downloadmanager
//Copyright (C) 2010  JD-Team support@jdownloader.org
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

@HostPlugin(revision = "$Revision: 15419 $", interfaceVersion = 2, names = { "bandbase.dk" }, urls = { "http://(www\\.)?bandbase.dk/[^/<>\"\\']+/Track/\\d+/" }, flags = { 0 })
public class BandBaseDk extends PluginForHost {

    public BandBaseDk(PluginWrapper wrapper) {
        super(wrapper);
    }

    @Override
    public String getAGBLink() {
        return "http://www.bandbase.com/About/Contact/";
    }

    @Override
    public int getMaxSimultanFreeDownloadNum() {
        return -1;
    }

    @Override
    public void handleFree(DownloadLink downloadLink) throws Exception, PluginException {
        requestFileInformation(downloadLink);
        br.setFollowRedirects(false);
        final String trackID = new Regex(downloadLink.getDownloadURL(), "bandbase\\.dk/[^/]+/Track/(\\d+)/").getMatch(0);
        br.getPage("http://www.bandbase.dk/Start-Download/" + trackID);
        String dllink = br.getRedirectLocation();
        if (dllink == null || br.containsHTML(">Track not found<")) {
            br.postPage("http://www.bandbase.dk/Webservice/api.asmx/Player_ListItem_Get", "SiteId=1&TrackID=" + trackID + "&UserID=0&VideoID=0&LanguageId=1");
            dllink = br.getRegex("ItemFileName=\"(http://.*?)\"").getMatch(0);
            if (dllink == null) dllink = br.getRegex("\"(http://node\\d+\\.files\\.bandbase\\.com/Audio/Download/.*?)\"").getMatch(0);
        }
        if (dllink == null) throw new PluginException(LinkStatus.ERROR_PLUGIN_DEFECT);
        dl = jd.plugins.BrowserAdapter.openDownload(br, downloadLink, dllink, true, 0);
        if (dl.getConnection().getContentType().contains("html")) {
            br.followConnection();
            throw new PluginException(LinkStatus.ERROR_PLUGIN_DEFECT);
        }
        dl.startDownload();
    }

    @Override
    public AvailableStatus requestFileInformation(DownloadLink link) throws IOException, PluginException {
        this.setBrowserExclusive();
        br.setFollowRedirects(true);
        br.getPage(link.getDownloadURL());
        if (br.containsHTML("(>Der er desværre ingen tracks her\\.|id=\"MainContentPlaceHolder_Tracksl1_LabelNoTracks\")") || br.getURL().contains("bandbase.dk/Bands/Search/")) throw new PluginException(LinkStatus.ERROR_FILE_NOT_FOUND);
        String filename = br.getRegex("onmouseout=\"HidePlayerMenu\\(\\);\" name=\"\\d+\\|(.*?)\\|").getMatch(0);
        if (filename == null) filename = br.getRegex("<title>(.*?) af Backslash på BandBase[\t\n\r ]+</title>").getMatch(0);
        if (filename == null) throw new PluginException(LinkStatus.ERROR_PLUGIN_DEFECT);
        String author = br.getRegex("name=\"author\" content=\"(.*?)\"").getMatch(0);
        if (author != null)
            link.setFinalFileName(Encoding.htmlDecode(author.trim()) + " - " + Encoding.htmlDecode(filename.trim()) + ".mp3");
        else
            link.setName(Encoding.htmlDecode(filename.trim()) + ".mp3");
        return AvailableStatus.TRUE;
    }

    @Override
    public void reset() {
    }

    @Override
    public void resetDownloadlink(DownloadLink link) {
    }

}