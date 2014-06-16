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

@HostPlugin(revision = "$Revision$", interfaceVersion = 2, names = { "5sing.com" }, urls = { "http://(www\\.)?[a-z0-9]+\\.5sing\\.com/(\\d+\\.html|down/\\d+)" }, flags = { 0 })
public class FiveSingCom extends PluginForHost {

    public FiveSingCom(PluginWrapper wrapper) {
        super(wrapper);
    }

    @Override
    public String getAGBLink() {
        return "http://5sing.com/";
    }

    private static final String CRIPPLEDLINK = "http://(www\\.)?[a-z0-9]+\\.5sing\\.com/down/\\d+";

    public void correctDownloadLink(final DownloadLink link) {
        if (link.getDownloadURL().matches(CRIPPLEDLINK)) {
            link.setUrlDownload("http://fc.5sing.com/" + new Regex(link.getDownloadURL(), "(\\d+)$").getMatch(0) + ".html");
        }
    }

    @Override
    public AvailableStatus requestFileInformation(final DownloadLink link) throws IOException, PluginException {
        this.setBrowserExclusive();
        br.setFollowRedirects(true);
        br.getPage(link.getDownloadURL());
        if (br.getURL().contains("FileNotFind")) {
            throw new PluginException(LinkStatus.ERROR_FILE_NOT_FOUND);
        }
        String extension = br.getRegex("<em>格式：</em>([^<>\"]*?)<br").getMatch(0);
        if (extension == null && br.containsHTML("<em>演唱：</em>")) {
            extension = "mp3";
        }
        final String filename = br.getRegex("var SongName   = \"([^<>\"]*?)\"").getMatch(0);
        final String fileid = br.getRegex("var SongID     = ([^<>\"]*?);").getMatch(0);
        final String filesize = br.getRegex("<em>大小：</em>([^<>\"]*?)<br").getMatch(0);
        if (filename == null || extension == null) {
            throw new PluginException(LinkStatus.ERROR_PLUGIN_DEFECT);
        }
        link.setFinalFileName(Encoding.htmlDecode(filename.trim()) + "-" + fileid + "." + Encoding.htmlDecode(extension.trim()));
        if (filesize != null) {
            link.setDownloadSize(SizeFormatter.getSize(filesize + "b"));
        }
        return AvailableStatus.TRUE;
    }

    @Override
    public void handleFree(final DownloadLink downloadLink) throws Exception, PluginException {
        requestFileInformation(downloadLink);
        // file: "http://data9.5sing.com/T1zbhLB4xT1R47IVrK.mp3"
        String dllink = br.getRegex("file: \"(http://[^<>\"]*?)\"").getMatch(0);
        if (dllink == null) {
            dllink = br.getRegex("\"(http://data\\d+\\.5sing\\.com/[^<>\"]*?)\"").getMatch(0);
        }
        if (dllink == null) {
            throw new PluginException(LinkStatus.ERROR_PLUGIN_DEFECT);
        }
        dl = jd.plugins.BrowserAdapter.openDownload(br, downloadLink, dllink, true, 0);
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
    public void resetDownloadlink(final DownloadLink link) {
    }

}