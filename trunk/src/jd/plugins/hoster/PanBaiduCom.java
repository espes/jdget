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

import java.util.HashMap;

import jd.PluginWrapper;
import jd.http.Browser;
import jd.http.URLConnectionAdapter;
import jd.nutils.encoding.Encoding;
import jd.parser.Regex;
import jd.plugins.DownloadLink;
import jd.plugins.DownloadLink.AvailableStatus;
import jd.plugins.HostPlugin;
import jd.plugins.LinkStatus;
import jd.plugins.Plugin;
import jd.plugins.PluginException;
import jd.plugins.PluginForHost;

//All links come from a decrypter
@HostPlugin(revision = "$Revision$", interfaceVersion = 2, names = { "pan.baidu.com" }, urls = { "http://(www\\.)?(pan\\.baidudecrypted\\.com/\\d+|pan\\.baidu\\.com/share/init\\?shareid=\\d+\\&uk=\\d+)" }, flags = { 0 })
public class PanBaiduCom extends PluginForHost {

    public PanBaiduCom(PluginWrapper wrapper) {
        super(wrapper);
    }

    @Override
    public String getAGBLink() {
        return "http://pan.baidu.com/";
    }

    private String              DLLINK          = null;
    private static final String PWPROTECTEDLINK = "http://(www\\.)?pan\\.baidu\\.com/share/init\\?shareid=\\d+\\&uk=\\d+";

    @Override
    public AvailableStatus requestFileInformation(final DownloadLink downloadLink) throws Exception {
        if (downloadLink.getDownloadURL().matches(PWPROTECTEDLINK)) {
            downloadLink.getLinkStatus().setStatusText("Password protected link");
            br.getPage(downloadLink.getDownloadURL());
            if (br.containsHTML("alt=\"小刀刀刀刀仔\"")) throw new PluginException(LinkStatus.ERROR_FILE_NOT_FOUND);
            downloadLink.setName(new Regex(downloadLink.getDownloadURL(), "(\\d+)$").getMatch(0));
        } else {
            DLLINK = downloadLink.getStringProperty("dlink");
            if (DLLINK == null) throw new PluginException(LinkStatus.ERROR_PLUGIN_DEFECT);
            DLLINK = DLLINK.replace("\\", "");
            Browser br2 = br.cloneBrowser();
            br2.setFollowRedirects(true);
            URLConnectionAdapter con = null;
            try {
                con = br2.openGetConnection(DLLINK);
                if (!con.getContentType().contains("html")) {
                    downloadLink.setDownloadSize(con.getLongContentLength());
                } else {
                    DLLINK = refreshFinalLink(downloadLink);
                    if (DLLINK == null) throw new PluginException(LinkStatus.ERROR_FILE_NOT_FOUND);
                    DLLINK = DLLINK.replace("\\", "");
                    try {
                        br2.openGetConnection(DLLINK);
                        if (!con.getContentType().contains("html")) {
                            downloadLink.setDownloadSize(con.getLongContentLength());
                        } else {
                            throw new PluginException(LinkStatus.ERROR_FILE_NOT_FOUND);
                        }
                    } finally {
                        try {
                            con.disconnect();
                        } catch (Throwable e) {
                        }
                    }
                }
            } finally {
                try {
                    con.disconnect();
                } catch (Throwable e) {
                }
            }
        }
        return AvailableStatus.TRUE;
    }

    @Override
    public void handleFree(final DownloadLink downloadLink) throws Exception, PluginException {
        requestFileInformation(downloadLink);
        String passCode = downloadLink.getStringProperty("pass");
        if (downloadLink.getDownloadURL().matches(PWPROTECTEDLINK)) {
            final String linkData = new Regex(downloadLink.getDownloadURL(), "(shareid=\\d+\\&uk=\\d+)").getMatch(0);
            if (passCode == null) passCode = Plugin.getUserInput("Password?", downloadLink);
            br.getHeaders().put("X-Requested-With", "XMLHttpRequest");
            br.postPage("http://pan.baidu.com/share/verify?" + linkData + "&t=" + System.currentTimeMillis(), "vcode=&pwd=" + Encoding.urlEncode(passCode));
            if (br.containsHTML("\\{\"errno\":\\-12,")) throw new PluginException(LinkStatus.ERROR_RETRY, "Wrong password entered");
            br.getPage("http://pan.baidu.com/share/link?" + linkData);
            DLLINK = br.getRegex("class=\"new\\-dbtn\" href=\"(http://[^<>\"]*?)\"").getMatch(0);
            if (DLLINK == null) throw new PluginException(LinkStatus.ERROR_PLUGIN_DEFECT);
            DLLINK = Encoding.htmlDecode(DLLINK);
        }
        dl = jd.plugins.BrowserAdapter.openDownload(br, downloadLink, DLLINK, true, 0);
        if (dl.getConnection().getContentType().contains("html")) {
            br.followConnection();
            throw new PluginException(LinkStatus.ERROR_PLUGIN_DEFECT);
        }
        downloadLink.setFinalFileName(Encoding.htmlDecode(getFileNameFromHeader(dl.getConnection())));
        if (passCode != null) downloadLink.setProperty("pass", passCode);
        dl.startDownload();
    }

    private String refreshFinalLink(final DownloadLink downloadLink) throws Exception {
        String dir = downloadLink.getStringProperty("dirName");
        String parameter = downloadLink.getStringProperty("mainLink");
        String hash = downloadLink.getStringProperty("md5");
        if (dir == null || parameter == null || hash == null) return null;

        br.getHeaders().put("X-Requested-With", "XMLHttpRequest");
        br.getPage("http://pan.baidu.com/share/list?channel=chunlei&clienttype=0&web=1&num=100&t=" + System.currentTimeMillis() + "&page=1&dir=" + dir + "&t=0." + +System.currentTimeMillis() + "&uk=" + new Regex(parameter, "uk=(\\d+)").getMatch(0) + "&shareid=" + new Regex(parameter, "shareid=(\\d+)").getMatch(0) + "&_=" + System.currentTimeMillis());

        HashMap<String, String> ret = new HashMap<String, String>();
        String list = br.getRegex("\"list\":\\[(.*?)\\]").getMatch(0);
        for (String[] links : new Regex((list == null ? "" : list), "\\{(.*?)\\}").getMatches()) {
            for (String[] link : new Regex(links[0] + ",", "\"(.*?)\":\"?(.*?)\"?,").getMatches()) {
                ret.put(link[0], link[1]);
            }
            if (ret.containsKey("md5") && hash.equalsIgnoreCase(ret.get("md5"))) return ret.get("dlink");
        }
        return null;
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