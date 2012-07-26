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
import java.text.DecimalFormat;
import java.util.Random;

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

//All links come from a decrypter
@HostPlugin(revision = "$Revision$", interfaceVersion = 2, names = { "pan.baidu.com" }, urls = { "http://(www\\.)?pan\\.baidudecrypted\\.com/\\d+" }, flags = { 0 })
public class PanBaiduCom extends PluginForHost {

    public PanBaiduCom(PluginWrapper wrapper) {
        super(wrapper);
    }

    @Override
    public String getAGBLink() {
        return "http://pan.baidu.com/";
    }

    private String DLLINK = null;

    @Override
    public AvailableStatus requestFileInformation(DownloadLink link) throws IOException, PluginException {
        this.setBrowserExclusive();
        br.setFollowRedirects(true);
        final String dirName = link.getStringProperty("dirname");
        br.getPage(link.getStringProperty("mainlink"));
        final DecimalFormat df = new DecimalFormat("0000");
        if (dirName != null) {
            final String uk = br.getRegex("type=\"text/javascript\">FileUtils\\.sysUK=\"(\\d+)\";</script>").getMatch(0);
            if (uk == null) throw new PluginException(LinkStatus.ERROR_PLUGIN_DEFECT);
            br.getHeaders().put("X-Requested-With", "XMLHttpRequest");
            br.getPage("http://pan.baidu.com/netdisk/weblist?channel=chunlei&clienttype=0&dir=" + dirName + "&t=0." + df.format(new Random().nextInt(100000)) + "&type=1&uk=" + uk);
        }
        if (br.containsHTML("<title>[\t\n\r ]+的完全公开目录_百度网盘")) throw new PluginException(LinkStatus.ERROR_FILE_NOT_FOUND);
        final String correctedBR = br.toString().replace("\\", "");
        final Regex fileInfo = new Regex(correctedBR, "\"server_filename\":\"" + link.getStringProperty("plainfilename") + "\",\"s3_handle\":\"(http://[^<>\"]*?)\",\"size\":(\\d+)");
        if (fileInfo.getMatches().length < 1) throw new PluginException(LinkStatus.ERROR_PLUGIN_DEFECT);
        final String filesize = fileInfo.getMatch(1) + "b";
        link.setFinalFileName(Encoding.htmlDecode(link.getStringProperty("plainfilename").trim()));
        link.setDownloadSize(SizeFormatter.getSize(filesize));
        DLLINK = fileInfo.getMatch(0);
        return AvailableStatus.TRUE;
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