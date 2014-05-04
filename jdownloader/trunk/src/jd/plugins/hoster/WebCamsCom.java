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

@HostPlugin(revision = "$Revision$", interfaceVersion = 2, names = { "webcams.com" }, urls = { "http://[\\w\\.]*?webcams\\.com/index\\.php\\?action=ModelPage\\&page=movie\\&model_id=\\d+\\&media_id=\\d+(\\&mmcat_id=\\d+)?" }, flags = { 0 })
public class WebCamsCom extends PluginForHost {

    public String dllink = null;

    public WebCamsCom(PluginWrapper wrapper) {
        super(wrapper);
    }

    @Override
    public String getAGBLink() {
        return "http://www.webcams.com/index.php?page=terms";
    }

    @Override
    public int getMaxSimultanFreeDownloadNum() {
        return -1;
    }

    @Override
    public void handleFree(DownloadLink downloadLink) throws Exception {
        requestFileInformation(downloadLink);
        dl = jd.plugins.BrowserAdapter.openDownload(br, downloadLink, dllink, true, 0);
        if (dl.getConnection().getContentType().contains("html")) {
            br.followConnection();
            throw new PluginException(LinkStatus.ERROR_PLUGIN_DEFECT);
        }
        dl.startDownload();
    }

    @Override
    public AvailableStatus requestFileInformation(DownloadLink downloadLink) throws IOException, PluginException {
        this.setBrowserExclusive();
        br.setFollowRedirects(false);
        br.getPage(downloadLink.getDownloadURL());
        if (!br.containsHTML("swfobject.js")) throw new PluginException(LinkStatus.ERROR_FILE_NOT_FOUND);
        String model = new Regex(downloadLink.getDownloadURL(), "model_id=(\\d+)").getMatch(0);
        String mediaid = new Regex(downloadLink.getDownloadURL(), "media_id=(\\d+)").getMatch(0);
        String modelName = br.getRegex("<h3 class=\"status\">(.*?) is <span").getMatch(0);
        if (model == null || mediaid == null || modelName == null) throw new PluginException(LinkStatus.ERROR_PLUGIN_DEFECT);
        dllink = br.getRegex("name=\"flashvars\" value=\"file=(http.*?\\.flv).*?\"").getMatch(0);
        if (dllink == null) dllink = new Regex(Encoding.urlDecode(br.toString(), false), "(http://static\\d+\\.webcams\\.com/images/models/.*?\\.flv)").getMatch(0);
        if (dllink == null) throw new PluginException(LinkStatus.ERROR_FILE_NOT_FOUND);
        dllink = Encoding.urlDecode(dllink, true);
        downloadLink.setFinalFileName(modelName + " - " + model + " movie " + mediaid + ".flv");
        Browser br2 = br.cloneBrowser();
        // In case the link redirects to the finallink
        br2.setFollowRedirects(true);
        URLConnectionAdapter con = br2.openGetConnection(dllink);
        if (!con.getContentType().contains("html"))
            downloadLink.setDownloadSize(con.getLongContentLength());
        else
            throw new PluginException(LinkStatus.ERROR_FILE_NOT_FOUND);
        return AvailableStatus.TRUE;
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