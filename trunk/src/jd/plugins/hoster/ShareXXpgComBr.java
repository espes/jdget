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

import jd.PluginWrapper;
import jd.nutils.encoding.Encoding;
import jd.plugins.DownloadLink;
import jd.plugins.DownloadLink.AvailableStatus;
import jd.plugins.HostPlugin;
import jd.plugins.LinkStatus;
import jd.plugins.PluginException;
import jd.plugins.PluginForHost;

import org.appwork.utils.formatter.SizeFormatter;

@HostPlugin(revision = "$Revision$", interfaceVersion = 2, names = { "sharex.xpg.com.br" }, urls = { "http://(www\\.)?(sharex\\.xpg\\.com\\.br/files/[0-9]+|beta\\.sharex\\.xpg\\.com\\.br/[a-z0-9]+/[^<>\"/]*?\\.html)" }, flags = { 0 })
public class ShareXXpgComBr extends PluginForHost {

    public ShareXXpgComBr(PluginWrapper wrapper) {
        super(wrapper);
    }

    @Override
    public String getAGBLink() {
        return "http://sharex.xpg.com.br/contact.php";
    }

    private static final String BETALINK = "http://(www\\.)?beta\\.sharex\\.xpg\\.com\\.br/[a-z0-9]+/[^<>\"/]*?\\.html";

    @Override
    public AvailableStatus requestFileInformation(DownloadLink downloadLink) throws Exception {
        this.setBrowserExclusive();
        br.setFollowRedirects(true);
        br.getPage(downloadLink.getDownloadURL());
        if (downloadLink.getDownloadURL().matches(BETALINK)) {
            final String filename = br.getRegex("<div class=\"downinfo\">([^<>\"]*?)</div>").getMatch(0);
            if (filename == null) throw new PluginException(LinkStatus.ERROR_PLUGIN_DEFECT);
            downloadLink.setName(Encoding.htmlDecode(filename.trim()));
        } else {
            if (br.containsHTML("Este arquivo foi apagado e esta URL foi desativada")) throw new PluginException(LinkStatus.ERROR_FILE_NOT_FOUND);
            String filename = br.getRegex("alt=\"download\".*?></a>.*?<br><b>(.*?)</b> ").getMatch(0);
            if (filename == null) {
                filename = br.getRegex("/download/.*?/(.*?)\"").getMatch(0);
                if (filename == null) {
                    filename = br.getRegex("/files/.*?/(.*?)\"").getMatch(0);
                }
            }
            String filesize = br.getRegex("\\(([0-9]+ bytes)\\)").getMatch(0);
            if (filename == null) throw new PluginException(LinkStatus.ERROR_FILE_NOT_FOUND);
            downloadLink.setName(filename.trim());
            if (filesize != null) downloadLink.setDownloadSize(SizeFormatter.getSize(filesize));
        }
        return AvailableStatus.TRUE;
    }

    @Override
    public void handleFree(DownloadLink link) throws Exception {
        this.setBrowserExclusive();
        requestFileInformation(link);
        String dllink = null;
        if (link.getDownloadURL().matches(BETALINK)) {
            dllink = link.getDownloadURL().replace("beta.sharex.xpg.com.br/", "beta.sharex.xpg.com.br/download/").replace(".html", "");
        } else {
            dllink = br.getRegex("<br><a href=\"(.*?)\"").getMatch(0);
            if (dllink == null) dllink = br.getRegex("<br><a href=\"(http://sharex\\.xpg\\.com\\.br/download/[0-9]+/.*?)\"").getMatch(0);
            if (dllink == null) throw new PluginException(LinkStatus.ERROR_PLUGIN_DEFECT);
        }
        jd.plugins.BrowserAdapter.openDownload(br, link, dllink, true, 0);
        if ((dl.getConnection().getContentType().contains("html"))) {
            if (dl.getConnection().getResponseCode() == 503) throw new PluginException(LinkStatus.ERROR_TEMPORARILY_UNAVAILABLE, "Server error", 60 * 60 * 1000l);
            br.followConnection();
            if (br.containsHTML(">404 Not Found<")) throw new PluginException(LinkStatus.ERROR_FILE_NOT_FOUND);
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
    public void resetDownloadlink(DownloadLink link) {
    }

}