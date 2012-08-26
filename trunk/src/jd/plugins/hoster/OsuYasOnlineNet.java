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

import org.appwork.utils.formatter.SizeFormatter;

@HostPlugin(revision = "$Revision$", interfaceVersion = 2, names = { "osu.yas-online.net" }, urls = { "https?://(www\\.)?(osu\\.yas\\-online\\.net|osupacks\\.ppy\\.sh)/((p|m)#\\d+|t#[^\"\\'<>]+\\-\\d+)" }, flags = { 0 })
public class OsuYasOnlineNet extends PluginForHost {

    private static final String OFFLINE = "\"Theme or pack does not exist";

    public OsuYasOnlineNet(PluginWrapper wrapper) {
        super(wrapper);
    }

    public void correctDownloadLink(DownloadLink link) {
        link.setUrlDownload(link.getDownloadURL().replace("osu.yas-online.net/", "osupacks.ppy.sh/").replace("https://", "http://"));
    }

    @Override
    public String getAGBLink() {
        // No direct toslink found
        return "http://osu.ppy.sh/p/terms";
    }

    @Override
    public int getMaxSimultanFreeDownloadNum() {
        return -1;
    }

    @Override
    public void handleFree(DownloadLink downloadLink) throws Exception, PluginException {
        requestFileInformation(downloadLink);
        String dllink = new Regex(br.toString().replace("\\", ""), "\"downloadLink\":\"(/fetch/.*?)\"").getMatch(0);
        if (dllink == null) throw new PluginException(LinkStatus.ERROR_PLUGIN_DEFECT);
        dllink = "http://osupacks.ppy.sh" + dllink;
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
        br.setFollowRedirects(false);
        String size = null;
        String filename = null;
        br.setFollowRedirects(true);
        br.getHeaders().put("X-Requested-With", "XMLHttpRequest");
        if (link.getDownloadURL().contains("osupacks.ppy.sh/p#")) {
            final String id = new Regex(link.getDownloadURL(), "osupacks\\.ppy\\.sh/p#(\\d+)").getMatch(0);
            br.getPage("http://osupacks.ppy.sh/json.packdata.php?themeId=1&packNum=" + id);
            if (br.containsHTML(OFFLINE)) throw new PluginException(LinkStatus.ERROR_FILE_NOT_FOUND);
            filename = "Beatmap Paket #" + id + ".rar";
        } else if (link.getDownloadURL().contains("osupacks.ppy.sh/m#")) {
            br.getPage("http://osupacks.ppy.sh/json.mapdata.php?mapId=" + new Regex(link.getDownloadURL(), "osupacks\\.ppy\\.sh/m#(\\d+)").getMatch(0));
            if (br.containsHTML("could not be found in the database")) throw new PluginException(LinkStatus.ERROR_FILE_NOT_FOUND);
            filename = br.getRegex("\"filename\":\"(.*?)\"").getMatch(0);
        } else {
            final Regex infoRegex = new Regex(link.getDownloadURL(), "osupacks\\.ppy\\.sh/t#(.*?)\\-(\\d+)");
            br.getPage("http://osupacks.ppy.sh/json.packdata.php?themeId=" + infoRegex.getMatch(0) + "&packNum=" + infoRegex.getMatch(1));
            if (br.containsHTML(OFFLINE)) throw new PluginException(LinkStatus.ERROR_FILE_NOT_FOUND);
            filename = infoRegex.getMatch(0) + " #" + infoRegex.getMatch(1) + ".rar";
        }
        size = br.getRegex("size\":\"(\\d+)\"").getMatch(0);
        if (filename == null || size == null) throw new PluginException(LinkStatus.ERROR_PLUGIN_DEFECT);
        link.setFinalFileName(Encoding.htmlDecode(filename.trim()));
        link.setDownloadSize(SizeFormatter.getSize(size));
        return AvailableStatus.TRUE;
    }

    @Override
    public void reset() {
    }

    @Override
    public void resetDownloadlink(DownloadLink link) {
    }

}