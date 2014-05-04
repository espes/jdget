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
import jd.plugins.DownloadLink;
import jd.plugins.DownloadLink.AvailableStatus;
import jd.plugins.HostPlugin;
import jd.plugins.LinkStatus;
import jd.plugins.PluginException;
import jd.plugins.PluginForHost;

import org.appwork.utils.formatter.SizeFormatter;

@HostPlugin(revision = "$Revision$", interfaceVersion = 2, names = { "dwnshare.pl" }, urls = { "http://(www\\.)?dwnshare\\.pl/(show\\-file/[a-z0-9]+/\\d+/.*?\\.html|download\\-file\\-directly/[a-z0-9]+/\\d+/.+)" }, flags = { 0 })
public class DwnSharePl extends PluginForHost {

    private static final String MAINPAGE = "http://dwnshare.pl/";

    public DwnSharePl(PluginWrapper wrapper) {
        super(wrapper);
    }

    public void correctDownloadLink(DownloadLink link) {
        if (!link.getDownloadURL().contains("/show-file/")) link.setUrlDownload(link.getDownloadURL().replace("/download-file-directly/", "/show-file/") + ".html");
    }

    @Override
    public String getAGBLink() {
        return "http://dwnshare.pl/rules.html";
    }

    @Override
    public int getMaxSimultanFreeDownloadNum() {
        return -1;
    }

    @Override
    public void handleFree(DownloadLink downloadLink) throws Exception, PluginException {
        requestFileInformation(downloadLink);
        String dllink = br.getRegex("<div class=\"link\"><a href=\"(http://.*?)\"").getMatch(0);
        if (dllink == null) dllink = br.getRegex("\"(http://(s\\d+\\.)?dwnshare\\.com/download\\-file\\-directly/.*?)\"").getMatch(0);
        if (dllink == null) throw new PluginException(LinkStatus.ERROR_PLUGIN_DEFECT);
        dl = jd.plugins.BrowserAdapter.openDownload(br, downloadLink, dllink, false, 1);
        if (dl.getConnection().getContentType().contains("html")) {
            br.followConnection();
            throw new PluginException(LinkStatus.ERROR_PLUGIN_DEFECT);
        }
        dl.startDownload();
    }

    @Override
    public AvailableStatus requestFileInformation(DownloadLink link) throws IOException, PluginException {
        this.setBrowserExclusive();
        br.setCustomCharset("utf-8");
        br.setFollowRedirects(true);
        br.setCookie(MAINPAGE, "lang", "pl");
        br.getPage(link.getDownloadURL());
        if (br.getURL().contains("dwnshare.pl/?error=not_found") || br.containsHTML("(<h1>Not Found</h1>|<title>404 Not Found</title>|\">Zaznacz plik który chcesz wysłać, maksymalnie|<title>Upload Files \\- DwnShare</title>)")) throw new PluginException(LinkStatus.ERROR_FILE_NOT_FOUND);
        String filename = br.getRegex("<title>(.*?) \\- Pobieranie Pliku \\- DwnShare</title>").getMatch(0);
        String ext = br.getRegex("dwnshare\\.pl/extensions/([A-Za-z0-9]+)\\.png\"").getMatch(0);
        String filesize = br.getRegex("class=\"result\">Rozmiar (.*?), wgrane").getMatch(0);
        if (filename == null || filesize == null) throw new PluginException(LinkStatus.ERROR_PLUGIN_DEFECT);
        filename = filename.trim();
        if (ext != null && !filename.substring(filename.length() - ext.length(), filename.length()).equals(ext.trim()))
            link.setName(filename + "." + ext.trim());
        else
            link.setName(filename);
        link.setDownloadSize(SizeFormatter.getSize(filesize));
        return AvailableStatus.TRUE;
    }

    @Override
    public void reset() {
    }

    @Override
    public void resetDownloadlink(DownloadLink link) {
    }

}