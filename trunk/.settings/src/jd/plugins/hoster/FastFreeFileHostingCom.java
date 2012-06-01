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

import org.appwork.utils.formatter.SizeFormatter;

@HostPlugin(revision = "$Revision: 15422 $", interfaceVersion = 2, names = { "fastfreefilehosting.com" }, urls = { "http://[\\w\\.]*?fastfreefilehosting\\.com/((\\?d|download\\.php\\?id)=[A-Z0-9]+|((en|ru|fr|es)/)?file/[0-9]+/)" }, flags = { 0 })
public class FastFreeFileHostingCom extends PluginForHost {

    private static final String COOKIE_HOST = "http://fastfreefilehosting.com";

    private static final String IPBLOCKED   = "(You have got max allowed bandwidth size per hour|You have got max allowed download sessions from the same IP)";

    public FastFreeFileHostingCom(PluginWrapper wrapper) {
        super(wrapper);
        // this.enablePremium("http://Only4Devs2Test.com/register.php?g=3");
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

    // MhfScriptBasic 1.1
    @Override
    public String getAGBLink() {
        return COOKIE_HOST + "/rules.php";
    }

    @Override
    public int getMaxSimultanFreeDownloadNum() {
        return -1;
    }

    @Override
    public void handleFree(DownloadLink link) throws Exception {
        this.setBrowserExclusive();
        requestFileInformation(link);
        if (br.containsHTML("value=\"Free Users\"")) br.postPage(link.getDownloadURL(), "Free=Free+Users");
        String passCode = null;
        Form captchaform = br.getFormbyProperty("name", "myform");
        if (captchaform == null) {
            captchaform = br.getFormbyProperty("name", "validateform");
            if (captchaform == null) {
                captchaform = br.getFormbyProperty("name", "valideform");
            }
        }
        if (br.containsHTML("(captcha.php|class=textinput name=downloadpw)")) {
            if (captchaform == null) throw new PluginException(LinkStatus.ERROR_PLUGIN_DEFECT);
            for (int i = 0; i <= 3; i++) {
                if (br.containsHTML("captcha.php")) {
                    String captchaurl = COOKIE_HOST + "/captcha.php";
                    String code = getCaptchaCode(captchaurl, link);
                    captchaform.put("captchacode", code);
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
                if (br.containsHTML("Captcha number error") || br.containsHTML("captcha.php") && !br.containsHTML(IPBLOCKED)) {
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
        if (br.containsHTML("Captcha number error") || br.containsHTML("captcha.php") && !br.containsHTML(IPBLOCKED)) {
            logger.warning("Wrong captcha or wrong password!");
            link.setProperty("pass", null);
            throw new PluginException(LinkStatus.ERROR_CAPTCHA);
        }
        if (passCode != null) {
            link.setProperty("pass", passCode);
        }
        if (br.containsHTML(IPBLOCKED)) throw new PluginException(LinkStatus.ERROR_IP_BLOCKED, null, 10 * 60 * 1001l);
        String finalLink = findLink();
        if (finalLink == null) throw new PluginException(LinkStatus.ERROR_PLUGIN_DEFECT);
        dl = jd.plugins.BrowserAdapter.openDownload(br, link, finalLink, true, 1);
        if (dl.getConnection().getContentType().contains("html")) {
            br.followConnection();
            throw new PluginException(LinkStatus.ERROR_PLUGIN_DEFECT);
        }
        dl.startDownload();
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
                filename = br.getRegex("content=\"(.*?), The best file hosting service").getMatch(0);
                if (filename == null) {
                    filename = br.getRegex("<title>(.*?)</title>").getMatch(0);
                }
            }
        }
        String filesize = br.getRegex("<b>(File size|Filesize):</b></td>[\r\t\n ]+<td align=([\r\t\n ]+|left)>(.*?)</td>").getMatch(2);
        if (filesize == null) filesize = br.getRegex("<b>\\&#4324;\\&#4304;\\&#4312;\\&#4314;\\&#4312;\\&#4321; \\&#4310;\\&#4317;\\&#4315;\\&#4304;:</b></td>[\t\r\n ]+<td align=left>(.*?)</td>").getMatch(0);
        if (filename == null || filename.matches("")) throw new PluginException(LinkStatus.ERROR_FILE_NOT_FOUND);
        parameter.setFinalFileName(Encoding.htmlDecode(filename.trim()));
        if (filesize != null) parameter.setDownloadSize(SizeFormatter.getSize(filesize));
        return AvailableStatus.TRUE;
    }

    @Override
    public void reset() {
    }

    @Override
    public void resetDownloadlink(DownloadLink link) {
    }

}