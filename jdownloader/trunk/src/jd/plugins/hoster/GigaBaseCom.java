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
import jd.http.RandomUserAgent;
import jd.nutils.encoding.Encoding;
import jd.parser.Regex;
import jd.parser.html.Form;
import jd.plugins.DownloadLink;
import jd.plugins.DownloadLink.AvailableStatus;
import jd.plugins.HostPlugin;
import jd.plugins.LinkStatus;
import jd.plugins.PluginException;
import jd.plugins.PluginForHost;

import org.appwork.utils.formatter.SizeFormatter;

@HostPlugin(revision = "$Revision$", interfaceVersion = 2, names = { "gigabase.com" }, urls = { "http://(www\\.)?gigabase\\.com/getfile/[^<>\"\\'/]+/" }, flags = { 0 })
public class GigaBaseCom extends PluginForHost {

    public GigaBaseCom(PluginWrapper wrapper) {
        super(wrapper);
    }

    private static String       AGENT           = RandomUserAgent.generate();
    private static final String SECURITYCAPTCHA = "text from the image and click \"Continue\" to access the website";

    @Override
    public String getAGBLink() {
        return "http://www.gigabase.com/page/terms";
    }

    @Override
    public int getMaxSimultanFreeDownloadNum() {
        return 1;
    }

    @Override
    public AvailableStatus requestFileInformation(DownloadLink link) throws IOException, PluginException {
        this.setBrowserExclusive();
        br.setFollowRedirects(true);
        br.getHeaders().put("User-Agent", AGENT);
        br.getPage(link.getDownloadURL());
        if (br.containsHTML("(>File not found or removed<|<title>Gigabase\\.com</title>)")) throw new PluginException(LinkStatus.ERROR_FILE_NOT_FOUND);
        if (br.containsHTML(SECURITYCAPTCHA)) {
            link.getLinkStatus().setStatusText("Can't check status, security captcha...");
            return AvailableStatus.UNCHECKABLE;
        }
        final Regex fileInfo = br.getRegex("<small>Download file:</small><br/>([^<>\"]*?)<small>\\(([^<>\"]*?)\\)</small>");
        final String filename = fileInfo.getMatch(0);
        final String filesize = fileInfo.getMatch(1);
        if (filename == null || filesize == null) throw new PluginException(LinkStatus.ERROR_PLUGIN_DEFECT);
        link.setName(Encoding.htmlDecode(filename.trim()));
        link.setDownloadSize(SizeFormatter.getSize(filesize));
        return AvailableStatus.TRUE;
    }

    @Override
    public void handleFree(DownloadLink downloadLink) throws Exception, PluginException {
        requestFileInformation(downloadLink);
        if (br.containsHTML(SECURITYCAPTCHA)) {
            final Form captchaForm = br.getForm(0);
            if (captchaForm == null) throw new PluginException(LinkStatus.ERROR_PLUGIN_DEFECT);
            final String code = getCaptchaCode("http://www." + this.getHost() + "/captcha/?rnd=", downloadLink);
            captchaForm.put("captcha", code);
            br.submitForm(captchaForm);
            if (br.containsHTML(SECURITYCAPTCHA)) throw new PluginException(LinkStatus.ERROR_CAPTCHA);
        }
        String dllink = br.getRegex(">Download types</span><br/><span class=\"c3\"><a href=\"(http://.*?)\"").getMatch(0);
        if (dllink == null) dllink = br.getRegex("\"(http://st\\d+\\.gigabase\\.com/down/[^\"<>]+)").getMatch(0);
        if (dllink == null) {
            dllink = br.getRegex("href=\"/getfile/[^\"<>]*?(/free\\?step=[^\"<>]*?)&referer=").getMatch(0);
            dllink = downloadLink.getDownloadURL() + dllink;

            /* workaround for old stable bug */
            dllink = dllink.replaceAll("\\/\\/", "/");
            dllink = dllink.replaceAll("http:\\/", "http://");

            br.getPage(dllink);
            // some coutries (Poland, Germany) are redirected by one more page
            // with possibility of SMS-payment
            dllink = br.getRegex("id=\"noThanxDiv\"><a href=\"(/getfile/[^\"<>]*?/link\\?step=[^\"<>]*?)&referer=\"").getMatch(0);
            if (dllink != null) {
                dllink = downloadLink.getDownloadURL() + dllink;

                /* workaround for old stable bug */
                dllink = dllink.replaceAll("\\/\\/", "/");
                dllink = dllink.replaceAll("http:\\/", "http://");

                br.getPage(dllink);
            }
            dllink = br.getRegex("\"(http://st\\d+\\.gigabase\\.com/dfile/[^\"<>]+)").getMatch(0);
        }
        if (dllink == null) {
            Form dlForm = br.getForm(2);
            if (dlForm != null) {
                br.submitForm(dlForm);
            } else {
                throw new PluginException(LinkStatus.ERROR_PLUGIN_DEFECT);
            }
            dllink = br.getRegex(">Download types</span><br/><span class=\"c3\"><a href=\"(http://.*?)\"").getMatch(0);
            if (dllink == null) dllink = br.getRegex("\"(http://st\\d+\\.gigabase\\.com/down/[^\"<>]+)").getMatch(0);
            if (dllink == null) { throw new PluginException(LinkStatus.ERROR_FATAL, "Cannot download, maybe your country is blocked?!"); }
        }
        dl = jd.plugins.BrowserAdapter.openDownload(br, downloadLink, dllink, false, 1);
        if (dl.getConnection().getContentType().contains("html")) {
            br.followConnection();
            if (br.containsHTML("HTTP Status 404")) throw new PluginException(LinkStatus.ERROR_FILE_NOT_FOUND);
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

}