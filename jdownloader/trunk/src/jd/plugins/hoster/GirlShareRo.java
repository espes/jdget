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

import java.io.IOException;

import jd.PluginWrapper;
import jd.http.RandomUserAgent;
import jd.parser.html.Form;
import jd.plugins.DownloadLink;
import jd.plugins.DownloadLink.AvailableStatus;
import jd.plugins.HostPlugin;
import jd.plugins.LinkStatus;
import jd.plugins.PluginException;
import jd.plugins.PluginForHost;

import org.appwork.utils.formatter.SizeFormatter;

@HostPlugin(revision = "$Revision$", interfaceVersion = 2, names = { "girlshare.ro" }, urls = { "http://[\\w\\.]*?girlshare\\.ro/[0-9\\.]+" }, flags = { 0 })
public class GirlShareRo extends PluginForHost {

    public static class StringContainer {
        public String string = RandomUserAgent.generate();
    }

    private static StringContainer UA = new StringContainer();

    public GirlShareRo(PluginWrapper wrapper) {
        super(wrapper);
    }

    @Override
    public String getAGBLink() {
        return "http://www.girlshare.ro/";
    }

    @Override
    public int getMaxSimultanFreeDownloadNum() {
        return -1;
    }

    @Override
    public void handleFree(DownloadLink downloadLink) throws Exception, PluginException {

        requestFileInformation(downloadLink);

        Form dlform = br.getForm(0);

        if (dlform == null) throw new PluginException(LinkStatus.ERROR_PLUGIN_DEFECT);
        dlform.remove(null);

        dlform.put("x", "" + (int) (100 * Math.random() + 1));
        dlform.put("y", "" + (int) (25 * Math.random() + 1));
        br.setFollowRedirects(true);

        dl = jd.plugins.BrowserAdapter.openDownload(br, downloadLink, dlform, false, 1);
        if (!dl.getConnection().isContentDisposition()) {
            // website often opens ads on the first click.
            dl = jd.plugins.BrowserAdapter.openDownload(br, downloadLink, dlform, false, 1);
        }
        if (dl.getConnection().getContentType().contains("html")) {

            if (!br.getURL().contains("girlshare.ro")) throw new PluginException(LinkStatus.ERROR_TEMPORARILY_UNAVAILABLE, "Server error", 60 * 60 * 1001l);
            br.followConnection();
            throw new PluginException(LinkStatus.ERROR_PLUGIN_DEFECT);
        }
        dl.startDownload();
    }

    @Override
    public AvailableStatus requestFileInformation(DownloadLink link) throws IOException, PluginException {
        this.setBrowserExclusive();
        br.getHeaders().put("User-Agent", UA.string);
        br.getPage(link.getDownloadURL());
        if (br.containsHTML("(<b>Acest fisier nu exista\\.</b>|<title>GirlShare - Acest fisier nu exista\\.</title>)")) throw new PluginException(LinkStatus.ERROR_FILE_NOT_FOUND);
        String filename = br.getRegex("title = \"(.*?)\";").getMatch(0);
        if (filename == null) filename = br.getRegex("<title>GirlShare - Download (.*?)</title>").getMatch(0);
        String filesize = br.getRegex("</H3>[\n\r\t ]+<br>(.*?) , ").getMatch(0);
        if (filename == null || filesize == null) throw new PluginException(LinkStatus.ERROR_PLUGIN_DEFECT);
        link.setName(filename.trim());
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