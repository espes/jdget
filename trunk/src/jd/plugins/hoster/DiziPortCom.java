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

@HostPlugin(revision = "$Revision$", interfaceVersion = 2, names = { "diziport.com" }, urls = { "http://(www\\.)?diziportdecrypted\\.com/.*?/.*?/(\\d+)?" }, flags = { 0 })
public class DiziPortCom extends PluginForHost {

    private String DLLINK = null;

    public DiziPortCom(PluginWrapper wrapper) {
        super(wrapper);
    }

    @Override
    public String getAGBLink() {
        return "http://diziport.com/index.php?bolum=yazi&obje=oku&seoAdi=kullanim_sartlari";
    }

    @Override
    public int getMaxSimultanFreeDownloadNum() {
        return -1;
    }

    public void correctDownloadLink(DownloadLink link) {
        link.setUrlDownload(link.getDownloadURL().replace("diziportdecrypted.com/", "diziport.com/"));
    }

    @Override
    public AvailableStatus requestFileInformation(DownloadLink downloadLink) throws IOException, PluginException {
        this.setBrowserExclusive();
        br.setFollowRedirects(true);
        br.getPage(downloadLink.getDownloadURL());
        if (br.containsHTML(">sayfa bulunamadý") || !br.containsHTML("\\'/js/swfobject\\.js\\'")) throw new PluginException(LinkStatus.ERROR_FILE_NOT_FOUND);
        String filename = br.getRegex("<meta property=\"og:title\" content=\"(.*?) izle \\- Diziport\"").getMatch(0);
        if (filename == null) filename = br.getRegex("<title>(.*?) izle - Diziport</title>").getMatch(0);
        DLLINK = br.getRegex("\\&sid=(.*?)\"").getMatch(0);
        if (filename == null || DLLINK == null) throw new PluginException(LinkStatus.ERROR_PLUGIN_DEFECT);
        br.getHeaders().put("X-Requested-With", "XMLHttpRequest");
        br.getPage("http://diziport.com/nesne-uye.php?olay=sayac&sid=" + DLLINK);
        DLLINK = br.getRegex("\\&strSource=(http.*?)(\\&publisherLogo=|\\')").getMatch(0);
        if (DLLINK == null) throw new PluginException(LinkStatus.ERROR_PLUGIN_DEFECT);
        DLLINK = Encoding.htmlDecode(DLLINK);
        String ext = DLLINK.substring(DLLINK.lastIndexOf("."));
        if (ext == null || ext.length() > 5) ext = ".mp4";
        filename = filename.trim();
        String id = new Regex(downloadLink.getDownloadURL(), "http://(www\\.)?diziport\\.com/.*?/.*?/(\\d+)?").getMatch(1);
        if (id == null) {
            id = "";
        } else {
            id = "_" + id;
        }
        downloadLink.setFinalFileName(Encoding.htmlDecode(filename) + id + ext);
        Browser br2 = br.cloneBrowser();
        // In case the link redirects to the finallink
        br2.setFollowRedirects(true);
        URLConnectionAdapter con = null;
        try {
            con = br2.openGetConnection(DLLINK);
            if (!con.getContentType().contains("html"))
                downloadLink.setDownloadSize(con.getLongContentLength());
            else
                throw new PluginException(LinkStatus.ERROR_FILE_NOT_FOUND);
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
    public void resetDownloadlink(DownloadLink link) {
    }

    @Override
    public void resetPluginGlobals() {
    }
}