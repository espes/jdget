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

import java.io.File;

import jd.PluginWrapper;
import jd.nutils.encoding.Encoding;
import jd.parser.html.HTMLParser;
import jd.plugins.DownloadLink;
import jd.plugins.DownloadLink.AvailableStatus;
import jd.plugins.HostPlugin;
import jd.plugins.LinkStatus;
import jd.plugins.PluginException;
import jd.plugins.PluginForHost;
import jd.utils.JDUtilities;

import org.appwork.utils.formatter.SizeFormatter;

@HostPlugin(revision = "$Revision$", interfaceVersion = 2, names = { "shareupload.net" }, urls = { "http://(www\\.)?shareupload\\.net/((\\?d|download\\.php\\?id)=[A-Z0-9]+|((en|ru|fr|es|de)/)?file/[0-9]+/)" }, flags = { 2 })
public class ShareUploadNet extends PluginForHost {

    public ShareUploadNet(PluginWrapper wrapper) {
        super(wrapper);
        this.enablePremium(COOKIE_HOST + "/register.php?g=3");
    }

    // MhfScriptBasic 1.6
    @Override
    public String getAGBLink() {
        return COOKIE_HOST + "/rules.php";
    }

    private static final String COOKIE_HOST = "http://shareupload.net";

    public void correctDownloadLink(DownloadLink link) {
        link.setUrlDownload(link.getDownloadURL().replaceAll("(en|ru|fr|es|de)/file/", "file/"));
    }

    @Override
    public AvailableStatus requestFileInformation(DownloadLink parameter) throws Exception {
        this.setBrowserExclusive();
        br.setFollowRedirects(true);
        br.setCookie(COOKIE_HOST, "mfh_mylang", "en");
        br.setCookie(COOKIE_HOST, "yab_mylang", "en");
        br.getPage(parameter.getDownloadURL());
        if (br.getURL().contains("&code=DL_FileNotFound") || br.containsHTML("(Your requested file is not found|No file found)")) throw new PluginException(LinkStatus.ERROR_FILE_NOT_FOUND);
        String filename = br.getRegex("<h2 class=\"float\\-left\">([^<>\"]*?)</h2>").getMatch(0);
        String filesize = getData("File size");
        if (filename == null || filename.matches("")) throw new PluginException(LinkStatus.ERROR_FILE_NOT_FOUND);
        parameter.setFinalFileName(filename.trim());
        if (filesize != null) parameter.setDownloadSize(SizeFormatter.getSize(filesize));
        return AvailableStatus.TRUE;
    }

    @Override
    public void handleFree(DownloadLink downloadLink) throws Exception {
        this.setBrowserExclusive();
        requestFileInformation(downloadLink);
        if (br.containsHTML("value=\"Free Users\""))
            br.postPage(downloadLink.getDownloadURL(), "Free=Free+Users");
        else if (br.getFormbyProperty("name", "entryform1") != null) br.submitForm(br.getFormbyProperty("name", "entryform1"));

        final String rcID = br.getRegex("challenge\\?k=([^<>\"]*?)\"").getMatch(0);
        if (rcID == null) throw new PluginException(LinkStatus.ERROR_PLUGIN_DEFECT);

        PluginForHost recplug = JDUtilities.getPluginForHost("DirectHTTP");
        jd.plugins.hoster.DirectHTTP.Recaptcha rc = ((DirectHTTP) recplug).getReCaptcha(br);
        rc.setId(rcID);
        rc.load();
        File cf = rc.downloadCaptcha(getLocalCaptchaFile());
        String c = getCaptchaCode(cf, downloadLink);

        br.getHeaders().put("X-Requested-With", "XMLHttpRequest");
        br.postPage(downloadLink.getDownloadURL(), "downloadverify=1&d=1&recaptcha_response_field=" + c + "&recaptcha_challenge_field=" + rc.getChallenge());
        if (br.containsHTML("incorrect\\-captcha\\-sol")) throw new PluginException(LinkStatus.ERROR_CAPTCHA);
        final String reconnectWaittime = br.getRegex("You must wait (\\d+) mins\\. for next download.").getMatch(0);
        if (reconnectWaittime != null) throw new PluginException(LinkStatus.ERROR_IP_BLOCKED, Integer.parseInt(reconnectWaittime) * 60 * 1001l);
        // Note that there are links which always show this error, must be a
        // bug!
        if (br.containsHTML("The allowed bandwidth assigned to your IP is used up")) throw new PluginException(LinkStatus.ERROR_IP_BLOCKED);
        if (br.containsHTML("The allowed download sessions assigned to your IP is used up")) throw new PluginException(LinkStatus.ERROR_IP_BLOCKED, "Too many simultan dls", 5 * 60 * 1000l);
        final String finalLink = findLink();
        if (finalLink == null) throw new PluginException(LinkStatus.ERROR_PLUGIN_DEFECT);
        int wait = 60;
        final String waittime = br.getRegex("countdown\\((\\d+)\\);").getMatch(0);
        if (waittime != null) wait = Integer.parseInt(waittime);
        sleep(wait * 1001l, downloadLink);
        dl = jd.plugins.BrowserAdapter.openDownload(br, downloadLink, finalLink, false, 1);
        if (dl.getConnection().getContentType().contains("html")) {
            br.followConnection();

            if (br.containsHTML(">AccessKey is expired, please request")) throw new PluginException(LinkStatus.ERROR_FATAL, "FATAL server error, waittime skipped?");
            throw new PluginException(LinkStatus.ERROR_PLUGIN_DEFECT);
        }
        dl.startDownload();
    }

    private String findLink() throws Exception {
        String finalLink = br.getRegex("(http://.{5,30}getfile\\.php\\?id=\\d+[^<>\"\\']*?)(\"|\\')").getMatch(0);
        if (finalLink == null) {
            String[] sitelinks = HTMLParser.getHttpLinks(br.toString(), null);
            if (sitelinks == null || sitelinks.length == 0) throw new PluginException(LinkStatus.ERROR_PLUGIN_DEFECT);
            for (String alink : sitelinks) {
                alink = Encoding.htmlDecode(alink);
                if (alink.contains("access_key=") || alink.contains("getfile.php?")) {
                    finalLink = alink;
                    break;
                }
            }
        }
        return finalLink;
    }

    private String getData(final String data) {
        return br.getRegex(">" + data + "</strong></li>[\t\n\r ]+<li class=\"col\\-w50\">([^<>\"]*?)</li>").getMatch(0);
    }

    @Override
    public void reset() {
    }

    @Override
    public void resetDownloadlink(DownloadLink link) {
    }

    @Override
    public int getMaxSimultanFreeDownloadNum() {
        return -1;
    }

}
