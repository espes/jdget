//    jDownloader - Downloadmanager
//    Copyright (C) 2008  JD-Team support@jdownloader.org
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

import java.io.IOException;
import java.util.Random;
import java.util.regex.Pattern;

import jd.PluginWrapper;
import jd.parser.Regex;
import jd.parser.html.Form;
import jd.plugins.DownloadLink;
import jd.plugins.DownloadLink.AvailableStatus;
import jd.plugins.HostPlugin;
import jd.plugins.LinkStatus;
import jd.plugins.PluginException;
import jd.plugins.PluginForHost;

import org.appwork.utils.formatter.SizeFormatter;

@HostPlugin(revision = "$Revision$", interfaceVersion = 2, names = { "supershare.net", "hotshare.net" }, urls = { "http://(www\\.)?(hotshare|supershare)\\.net/(.+/)?(file|audio|video)/.+", "dugbh59jhgvrohtgedfwhbDELETEMEsbv7549gpujnren" }, flags = { 0, 0 })
public class HotShareNet extends PluginForHost {

    public HotShareNet(PluginWrapper wrapper) {
        super(wrapper);

    }

    @Override
    public void correctDownloadLink(DownloadLink link) throws Exception {
        String part = new Regex(link.getDownloadURL(), "(file/.+|audio/.+|video/.+)").getMatch(0);
        link.setUrlDownload("http://www.supershare.net/en/" + part);
    }

    @Override
    public String getAGBLink() {
        return "http://www.supershare.net/pages/tos.html";
    }

    @Override
    public int getMaxSimultanFreeDownloadNum() {
        return -1;
    }

    @Override
    public AvailableStatus requestFileInformation(DownloadLink downloadLink) throws IOException, InterruptedException, PluginException {
        br.setFollowRedirects(true);
        this.setBrowserExclusive();
        br.setCookie("http://www.supershare.net/", "language", "english");
        br.getPage(downloadLink.getDownloadURL().replaceAll("video", "file").replaceAll("audio", "file"));
        if (br.containsHTML("(<h1>File not Found</h1>|<b>The file you requested cannot be found\\.|> Owner deleted this file\\.</p>|404\\.html\")")) throw new PluginException(LinkStatus.ERROR_FILE_NOT_FOUND);
        String filename = br.getRegex(Pattern.compile("<title>HotShare \\- (.*?)</title>")).getMatch(0);
        if (filename == null) filename = br.getRegex(Pattern.compile("class=\"top_title_downloading\"><strong>.*?</strong> \\((.*?)\\)")).getMatch(0);
        String filesize = br.getRegex(Pattern.compile("<span class=\"arrow1\">Size: <b>(.*?)</b></span> ")).getMatch(0);
        if (filesize == null || filename == null) throw new PluginException(LinkStatus.ERROR_PLUGIN_DEFECT);
        downloadLink.setName(filename.trim());
        downloadLink.setDownloadSize(SizeFormatter.getSize(filesize));
        return AvailableStatus.TRUE;
    }

    @Override
    public void handleFree(DownloadLink downloadLink) throws Exception {
        requestFileInformation(downloadLink);
        Form downloadForm = br.getFormbyProperty("name", "form1");
        downloadForm.put("download", "1");
        downloadForm.put("imageField.x", Integer.toString(new Random().nextInt(100)));
        downloadForm.put("imageField.y", Integer.toString(new Random().nextInt(100)));
        br.submitForm(downloadForm);
        final String downloadUrl = br.getRegex(Pattern.compile("<a class=\"link_v3\" href=\"(.*?)\">")).getMatch(0);
        if (downloadUrl == null) throw new PluginException(LinkStatus.ERROR_PLUGIN_DEFECT);
        dl = jd.plugins.BrowserAdapter.openDownload(br, downloadLink, downloadUrl, false, 1);
        if (dl.getConnection().getContentType().contains("html")) {
            br.followConnection();
            if (br.containsHTML("File not")) throw new PluginException(LinkStatus.ERROR_FILE_NOT_FOUND);
            if (br.getURL().matches("http://www.supershare.net/en/(file/.+|audio/.+|video/.+)")) throw new PluginException(LinkStatus.ERROR_FATAL, "FATAL server error");
            throw new PluginException(LinkStatus.ERROR_PLUGIN_DEFECT);
        }
        dl.startDownload();
        if (downloadLink.getDownloadCurrent() == 15) throw new PluginException(LinkStatus.ERROR_FILE_NOT_FOUND);
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