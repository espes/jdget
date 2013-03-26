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
import jd.parser.html.Form;
import jd.parser.html.HTMLParser;
import jd.plugins.DownloadLink;
import jd.plugins.DownloadLink.AvailableStatus;
import jd.plugins.HostPlugin;
import jd.plugins.LinkStatus;
import jd.plugins.Plugin;
import jd.plugins.PluginException;
import jd.plugins.PluginForHost;
import jd.utils.JDUtilities;

import org.appwork.utils.formatter.SizeFormatter;

@HostPlugin(revision = "$Revision$", interfaceVersion = 2, names = { "navihost.us" }, urls = { "http://[\\w\\.]*?navihost\\.us/((\\?d|download\\.php\\?id)=[A-Z0-9]+|((en|ru|fr|es)/)?file/[0-9]+/)" }, flags = { 0 })
public class NaviHostUs extends PluginForHost {

    private static final String COOKIE_HOST      = "http://navihost.us";

    private static final String IPBLOCKED        = "(You have got max allowed bandwidth size per hour|You have got max allowed download sessions from the same IP|>The allowed download sessions assigned to your IP is used up: some files are being downloaded from your IP|Please try later, untill your current downloads are completed or)";

    private static final String RECAPTCHATEXT    = "(api\\.recaptcha\\.net|google\\.com/recaptcha/api/)";

    private static final String CHEAPCAPTCHATEXT = "captcha\\.php";

    public NaviHostUs(PluginWrapper wrapper) {
        super(wrapper);
        // this.enablePremium(COOKIE_HOST + "/register.php?g=3");
    }

    public void correctDownloadLink(DownloadLink link) {
        link.setUrlDownload(link.getDownloadURL() + "&setlang=en");
    }

    private String findLink() throws Exception {
        String finalLink = null;
        String[] sitelinks = HTMLParser.getHttpLinks(br.toString(), null);
        if (sitelinks == null || sitelinks.length == 0) throw new PluginException(LinkStatus.ERROR_PLUGIN_DEFECT);
        for (String alink : sitelinks) {
            alink = Encoding.htmlDecode(alink);
            if (alink.contains("access_key=") || alink.contains("getfile.php?")) {
                finalLink = alink;
                break;
            }
        }
        return finalLink;
    }

    // MhfScriptBasic 1.1, added 1 filesize regex, changed 1 filename regex, new
    // freeform handling, reconnecttexts
    @Override
    public String getAGBLink() {
        return COOKIE_HOST + "/rules.php";
    }

    @Override
    public int getMaxSimultanFreeDownloadNum() {
        return 1;
    }

    @Override
    public void handleFree(DownloadLink link) throws Exception {
        this.setBrowserExclusive();
        requestFileInformation(link);
        Form freeform = br.getFormbyProperty("name", "entryform1");
        if (freeform != null) br.submitForm(freeform);
        String passCode = null;
        Form captchaform = br.getFormbyProperty("name", "myform");
        if (captchaform == null) {
            captchaform = br.getFormbyProperty("name", "validateform");
            if (captchaform == null) {
                captchaform = br.getFormbyProperty("name", "valideform");
                if (captchaform == null) {
                    captchaform = br.getFormbyProperty("name", "verifyform");
                }
            }
        }
        if (br.containsHTML("class=textinput name=downloadpw") || br.containsHTML(RECAPTCHATEXT) || br.containsHTML(CHEAPCAPTCHATEXT)) {
            if (captchaform == null) throw new PluginException(LinkStatus.ERROR_PLUGIN_DEFECT);
            for (int i = 0; i <= 3; i++) {
                if (br.containsHTML(CHEAPCAPTCHATEXT)) {
                    logger.info("Found normal captcha");
                    String captchaurl = COOKIE_HOST + "/captcha.php";
                    String code = getCaptchaCode(captchaurl, link);
                    captchaform.put("captchacode", code);
                } else if (br.containsHTML(RECAPTCHATEXT)) {
                    logger.info("Found reCaptcha");
                    PluginForHost recplug = JDUtilities.getPluginForHost("DirectHTTP");
                    jd.plugins.hoster.DirectHTTP.Recaptcha rc = ((DirectHTTP) recplug).getReCaptcha(br);
                    rc.parse();
                    rc.load();
                    File cf = rc.downloadCaptcha(getLocalCaptchaFile());
                    captchaform.put("recaptcha_challenge_field", rc.getChallenge());
                    captchaform.put("recaptcha_response_field", getCaptchaCode(cf, link));
                }
                if (br.containsHTML("class=textinput name=downloadpw")) {
                    if (link.getStringProperty("pass", null) == null) {
                        passCode = Plugin.getUserInput("Password?", link);

                    } else {
                        /* gespeicherten PassCode holen */
                        passCode = link.getStringProperty("pass", null);
                    }
                    captchaform.put("downloadpw", passCode);
                }
                br.submitForm(captchaform);
                if (br.containsHTML("Password Error")) {
                    logger.warning("Wrong password!");
                    link.setProperty("pass", null);
                    continue;
                }
                if (br.containsHTML(IPBLOCKED)) throw new PluginException(LinkStatus.ERROR_IP_BLOCKED, null, 10 * 60 * 1001l);
                if (br.containsHTML("Captcha number error") || br.containsHTML(RECAPTCHATEXT) || br.containsHTML(CHEAPCAPTCHATEXT)) {
                    logger.warning("Wrong captcha or wrong password!");
                    link.setProperty("pass", null);
                    continue;
                }
                break;
            }
        }
        if (br.containsHTML("Password Error")) {
            logger.warning("Wrong password!");
            link.setProperty("pass", null);
            throw new PluginException(LinkStatus.ERROR_RETRY);
        }
        if (br.containsHTML("Captcha number error") || br.containsHTML(RECAPTCHATEXT) || br.containsHTML(CHEAPCAPTCHATEXT)) {
            logger.warning("Wrong captcha or wrong password!");
            link.setProperty("pass", null);
            throw new PluginException(LinkStatus.ERROR_CAPTCHA);
        }
        if (passCode != null) {
            link.setProperty("pass", passCode);
        }
        String finalLink = findLink();
        if (finalLink == null) throw new PluginException(LinkStatus.ERROR_PLUGIN_DEFECT);
        dl = jd.plugins.BrowserAdapter.openDownload(br, link, finalLink, false, 1);
        if (dl.getConnection().getContentType().contains("html")) {
            br.followConnection();
            throw new PluginException(LinkStatus.ERROR_PLUGIN_DEFECT);
        }
        dl.startDownload();
    }

    // do not add @Override here to keep 0.* compatibility
    public boolean hasAutoCaptcha() {
        return true;
    }

    // do not add @Override here to keep 0.* compatibility
    public boolean hasCaptcha() {
        return true;
    }

    @Override
    public AvailableStatus requestFileInformation(DownloadLink parameter) throws Exception {
        this.setBrowserExclusive();
        br.setFollowRedirects(true);
        br.setCookie(COOKIE_HOST, "mfh_mylang", "en");
        br.setCookie(COOKIE_HOST, "yab_mylang", "en");
        br.getPage(parameter.getDownloadURL());
        String newlink = br.getRegex("<p>The document has moved <a href=\"(.*?)\">here</a>\\.</p>").getMatch(0);
        if (newlink != null) {
            logger.info("This link has moved, trying to find and set the new link...");
            newlink = newlink.replaceAll("(\\&amp;|setlang=en)", "");
            parameter.setUrlDownload(newlink);
            br.getPage(newlink);
        }
        if (br.containsHTML("(Your requested file is not found|No file found)")) throw new PluginException(LinkStatus.ERROR_FILE_NOT_FOUND);
        String filename = br.getRegex("<b>File name:</b></td>[\r\t\n ]+<td align=([\r\t\n ]+|left width=\\d+px)>(.*?)</td>").getMatch(1);
        if (filename == null) {
            filename = br.getRegex("\"Click (this to report for|Here to Report)(.*?)\"").getMatch(1);
            if (filename == null) {
                filename = br.getRegex("class=\"content_header_middle widebox_outer_width\">[\t\n\r ]+<h2 class=\"float-left\">(.*?)</h2>").getMatch(0);
                if (filename == null) {
                    filename = br.getRegex("<title>(.*?)</title>").getMatch(0);
                }
            }
        }
        String filesize = br.getRegex("<b>(File size|Filesize):</b></td>[\r\t\n ]+<td align=([\r\t\n ]+|left)>(.*?)</td>").getMatch(2);
        if (filesize == null) {
            filesize = br.getRegex("<b>\\&#4324;\\&#4304;\\&#4312;\\&#4314;\\&#4312;\\&#4321; \\&#4310;\\&#4317;\\&#4315;\\&#4304;:</b></td>[\t\r\n ]+<td align=left>(.*?)</td>").getMatch(0);
            if (filesize == null) filesize = br.getRegex("<strong>File size</strong></li>[\t\n\r ]+<li class=\"col-w50\">(.*?)</li>").getMatch(0);
        }
        if (filename == null || filename.matches("")) throw new PluginException(LinkStatus.ERROR_FILE_NOT_FOUND);
        parameter.setFinalFileName(filename.trim());
        if (filesize != null) parameter.setDownloadSize(SizeFormatter.getSize(filesize));
        return AvailableStatus.TRUE;
    }

    @Override
    public void reset() {
    }

    @Override
    public void resetDownloadlink(DownloadLink link) {
    }

    /* NO OVERRIDE!! We need to stay 0.9*compatible */
    public boolean hasCaptcha(DownloadLink link, jd.plugins.Account acc) {
        if (acc == null) {
            /* no account, yes we can expect captcha */
            return true;
        }
        if (Boolean.TRUE.equals(acc.getBooleanProperty("free"))) {
            /* free accounts also have captchas */
            return true;
        }
        return false;
    }
}