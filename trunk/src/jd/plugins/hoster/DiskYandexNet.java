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

@HostPlugin(revision = "$Revision$", interfaceVersion = 2, names = { "disk.yandex.net" }, urls = { "https?://(www\\.)?(disk\\.yandex\\.net/disk/public/\\?hash=[A-Za-z0-9%/\\+=]+|mail\\.yandex\\.ru/disk/public/#[A-Za-z0-9%\\/+=]+|yadi\\.sk/d/[A-Za-z0-9\\-_]+|yadisk\\.cc/d/[A-Za-z0-9\\-_]+)" }, flags = { 0 })
public class DiskYandexNet extends PluginForHost {

    public DiskYandexNet(PluginWrapper wrapper) {
        super(wrapper);
    }

    @Override
    public String getAGBLink() {
        return "https://disk.yandex.net/";
    }

    public void correctDownloadLink(DownloadLink link) {
        link.setUrlDownload(link.getDownloadURL().replace("mail.yandex.ru/", "disk.yandex.net/").replace("#", "?hash="));
    }

    private String getHashID(DownloadLink link) throws PluginException {
        String hashID = new Regex(link.getDownloadURL(), "hash=(.+)$").getMatch(0);
        if (hashID == null) throw new PluginException(LinkStatus.ERROR_PLUGIN_DEFECT);
        hashID = Encoding.urlDecode(hashID, false);
        hashID = hashID.replaceAll(" ", "+");
        return hashID;
    }

    @Override
    public AvailableStatus requestFileInformation(final DownloadLink link) throws IOException, PluginException {
        this.setBrowserExclusive();
        br.setFollowRedirects(true);
        // redirect links
        if (link.getDownloadURL().matches("https?://(www\\.)?yadi\\.sk/d/[A-Za-z0-9\\-_]+") || link.getDownloadURL().matches("https?://(www\\.)?yadisk\\.cc/d/[A-Za-z0-9\\-_]+")) {
            br.getPage(link.getDownloadURL());
            if (link.getDownloadURL().matches("https?://(www\\.)?yadisk\\.cc/d/[A-Za-z0-9\\-_]+")) {
                if (br.containsHTML("This link was removed or not found")) throw new PluginException(LinkStatus.ERROR_FILE_NOT_FOUND);
            }
            if (!br.getURL().matches("https?://(www\\.)?disk\\.yandex\\.net/disk/public/\\?hash=[A-Za-z0-9%/\\+=]+(\\&.+)?")) throw new PluginException(LinkStatus.ERROR_PLUGIN_DEFECT);
            link.setUrlDownload(new Regex(br.getURL(), "(https?://(www\\.)?disk\\.yandex\\.net/disk/public/\\?hash=[A-Za-z0-9%/\\+=]+)").getMatch(0));
        }
        br.getPage(link.getDownloadURL());
        if (br.getURL().contains("&final=true")) {
            final String xml = br.getRegex("<script id=\"xml\\-data\">(.*?)</script>").getMatch(0);
            if (xml == null) throw new PluginException(LinkStatus.ERROR_PLUGIN_DEFECT);
            br.getRequest().setHtmlCode(Encoding.htmlDecode(xml));
            if (br.containsHTML(">resource not found<")) throw new PluginException(LinkStatus.ERROR_FILE_NOT_FOUND);
        } else {
            br.getHeaders().put("X-Requested-With", "XMLHttpRequest");
            br.postPage("https://disk.yandex.net/neo2/handlers/handlers.jsx", "_handlers=disk-file-info&_locale=en&_page=disk-share&_service=disk&hash=" + Encoding.urlEncode(getHashID(link)));
            if (br.containsHTML(">resource not found<|>handler is undefined<")) throw new PluginException(LinkStatus.ERROR_FILE_NOT_FOUND);
        }
        String filename = parse("name");
        String filesize = parse("size");
        if (filename == null || filesize == null) throw new PluginException(LinkStatus.ERROR_PLUGIN_DEFECT);
        link.setName(Encoding.htmlDecode(filename.trim()));
        link.setDownloadSize(SizeFormatter.getSize(filesize + "b"));
        return AvailableStatus.TRUE;
    }

    @Override
    public void handleFree(DownloadLink downloadLink) throws Exception, PluginException {
        requestFileInformation(downloadLink);
        br.getHeaders().put("X-Requested-With", "XMLHttpRequest");
        if (br.getURL().contains("&final=true")) {
            br.postPage("https://disk.yandex.net/my/handlers/handlers.jsx", "&public_url=1&_handlers=disk-file-info&_locale=en&_page=disk-share&_service=disk&hash=" + Encoding.urlEncode(getHashID(downloadLink)));
        } else {
            br.postPage("https://disk.yandex.net/neo2/handlers/handlers.jsx", "_handlers=disk-file-info&public_url=1&_locale=en&_page=disk-share&_service=disk&hash=" + Encoding.urlEncode(getHashID(downloadLink)));
        }
        String dllink = parse("file");
        if (dllink == null) throw new PluginException(LinkStatus.ERROR_PLUGIN_DEFECT);
        if (dllink.startsWith("//")) dllink = "https:" + dllink;
        // Don't do htmldecode as the link will be invalid then
        dllink = dllink.replace("amp;", "");
        dl = jd.plugins.BrowserAdapter.openDownload(br, downloadLink, dllink, false, 1);
        if (dl.getConnection().getContentType().contains("html")) {
            br.followConnection();
            throw new PluginException(LinkStatus.ERROR_PLUGIN_DEFECT);
        }
        dl.startDownload();
    }

    private String parse(String var) {
        return br.getRegex("<" + var + ">([^<>\"]*?)</" + var + ">").getMatch(0);
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