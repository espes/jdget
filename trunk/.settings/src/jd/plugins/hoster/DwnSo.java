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
import jd.plugins.DownloadLink;
import jd.plugins.DownloadLink.AvailableStatus;
import jd.plugins.HostPlugin;
import jd.plugins.LinkStatus;
import jd.plugins.PluginException;
import jd.plugins.PluginForHost;

import org.appwork.utils.formatter.SizeFormatter;

@HostPlugin(revision = "$Revision: 16352 $", interfaceVersion = 2, names = { "dwn.so" }, urls = { "http://(www\\.)?dwn\\.so/show\\-file/[a-z0-9]+/\\d+/[^<>\"/]+\\.html" }, flags = { 0 })
public class DwnSo extends PluginForHost {

    public DwnSo(PluginWrapper wrapper) {
        super(wrapper);
    }

    @Override
    public String getAGBLink() {
        return "http://dwn.so/rules.html";
    }

    @Override
    public AvailableStatus requestFileInformation(DownloadLink link) throws IOException, PluginException {
        this.setBrowserExclusive();
        br.setFollowRedirects(true);
        br.setCookie("http://dwn.so/", "lang", "en");
        br.getPage(link.getDownloadURL());
        if (br.getURL().contains("dwn.so/?error=not_found") || br.containsHTML("<title>Upload Files \\- DwnShare</title>")) throw new PluginException(LinkStatus.ERROR_FILE_NOT_FOUND);
        String filename = br.getRegex("<title>([^<>\"]*?) \\- Download File \\- DwnShare</title>").getMatch(0);
        if (filename == null) filename = br.getRegex("class=\"link_download\" href=\"#\"><b>([^<>\"]*?)</b></a></div>").getMatch(0);
        String filesize = br.getRegex("class=\"result\">File Size ([^<>\"]*?), uploaded").getMatch(0);
        if (filename == null || filesize == null) throw new PluginException(LinkStatus.ERROR_PLUGIN_DEFECT);
        link.setName(Encoding.htmlDecode(filename.trim()));
        link.setDownloadSize(SizeFormatter.getSize(filesize));
        return AvailableStatus.TRUE;
    }

    @Override
    public void handleFree(DownloadLink downloadLink) throws Exception, PluginException {
        requestFileInformation(downloadLink);
        String dllink = br.getRegex("\\{\\$\\(\\'\\.link_download\\'\\)\\.attr\\(\\'href\\',\\'(http://[^<>\"]*?)\\'\\)").getMatch(0);
        if (dllink == null) dllink = br.getRegex("\\'(http://s\\d+\\.dwnshare\\.com/download\\-file\\-directly/[a-z0-9]+/[a-z0-9]+/[a-z0-9]+/\\d+/[^<>\"]*?)\\'").getMatch(0);
        if (dllink == null) throw new PluginException(LinkStatus.ERROR_PLUGIN_DEFECT);
        dl = jd.plugins.BrowserAdapter.openDownload(br, downloadLink, dllink, false, 1);
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