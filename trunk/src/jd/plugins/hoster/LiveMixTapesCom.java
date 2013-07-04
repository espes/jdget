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

import java.io.File;
import java.io.IOException;

import jd.PluginWrapper;
import jd.http.Browser;
import jd.nutils.encoding.Encoding;
import jd.parser.Regex;
import jd.plugins.Account;
import jd.plugins.AccountInfo;
import jd.plugins.DownloadLink;
import jd.plugins.DownloadLink.AvailableStatus;
import jd.plugins.HostPlugin;
import jd.plugins.LinkStatus;
import jd.plugins.PluginException;
import jd.plugins.PluginForDecrypt;
import jd.plugins.PluginForHost;
import jd.utils.JDUtilities;
import jd.utils.locale.JDL;

import org.appwork.utils.formatter.SizeFormatter;

@HostPlugin(revision = "$Revision$", interfaceVersion = 2, names = { "livemixtapes.com" }, urls = { "http://(www\\.)?(livemixtap\\.es/[a-z0-9]+|(indy\\.)?livemixtapes\\.com/(download(/mp3)?|mixtapes)/\\d+/.*?\\.html)" }, flags = { 2 })
public class LiveMixTapesCom extends PluginForHost {

    private static final String CAPTCHATEXT            = "/captcha/captcha\\.gif\\?";
    private static final String MAINPAGE               = "http://www.livemixtapes.com/";
    private static final String MUSTBELOGGEDIN         = ">You must be logged in to access this page";
    private static final String ONLYREGISTEREDUSERTEXT = "Download is only available for registered users";

    public LiveMixTapesCom(PluginWrapper wrapper) {
        super(wrapper);
        // Currently there is only support for free accounts
        this.enablePremium("http://www.livemixtapes.com/signup.html");
    }

    public void correctDownloadLink(DownloadLink link) {
        link.setUrlDownload(link.getDownloadURL().replace("indy.", "").replace("/mixtapes/", "/download/"));
    }

    private void doFree(DownloadLink downloadLink) throws Exception, PluginException {
        br.setFollowRedirects(false);
        String dllink = null;
        if (br.containsHTML(MUSTBELOGGEDIN)) {
            final Browser br2 = br.cloneBrowser();
            try {
                br2.getPage("http://www.livemixtapes.com/play/" + new Regex(downloadLink.getDownloadURL(), "download(/mp3)?/(\\d+)").getMatch(1));
                dllink = br2.getRedirectLocation();
            } catch (final Exception e) {

            }
            if (dllink == null) throw new PluginException(LinkStatus.ERROR_FATAL, JDL.L("plugins.hoster.livemixtapescom.only4registered", ONLYREGISTEREDUSERTEXT));
        } else {
            final String timestamp = br.getRegex("name=\"timestamp\" value=\"(\\d+)\"").getMatch(0);
            final String challengekey = br.getRegex("ACPuzzle\\.create\\(\\'(.*?)\\'").getMatch(0);
            if (timestamp == null || challengekey == null) throw new PluginException(LinkStatus.ERROR_PLUGIN_DEFECT);
            final PluginForDecrypt solveplug = JDUtilities.getPluginForDecrypt("linkcrypt.ws");
            final jd.plugins.decrypter.LnkCrptWs.SolveMedia sm = ((jd.plugins.decrypter.LnkCrptWs) solveplug).getSolveMedia(br);
            sm.setChallengeKey(challengekey);
            final File cf = sm.downloadCaptcha(getLocalCaptchaFile());
            final String code = getCaptchaCode(cf, downloadLink);
            final String chid = sm.getChallenge(code);
            // Usually we have a waittime here but it can be skipped
            // int waittime = 40;
            // String wait =
            // br.getRegex("<span id=\"counter\">(\\d+)</span>").getMatch(0);
            // if (wait == null) wait = br.getRegex("wait: (\\d+)").getMatch(0);
            // if (wait != null) {
            // waittime = Integer.parseInt(wait);
            // if (waittime > 1000) waittime = waittime / 1000;
            // sleep(waittime * 1001, downloadLink);
            // }
            try {
                br.postPage(br.getURL(), "retries=0&timestamp=" + timestamp + "&adcopy_response=manual_challenge&adcopy_challenge=" + chid);
            } catch (Exception e) {
            }
            if (br.containsHTML("solvemedia\\.com/papi/")) throw new PluginException(LinkStatus.ERROR_CAPTCHA);
            dllink = br.getRedirectLocation();
            if (dllink == null) throw new PluginException(LinkStatus.ERROR_PLUGIN_DEFECT);
        }
        dl = jd.plugins.BrowserAdapter.openDownload(br, downloadLink, dllink, false, 1);
        if (dl.getConnection().getContentType().contains("html")) {
            br.followConnection();
            throw new PluginException(LinkStatus.ERROR_PLUGIN_DEFECT);
        }
        downloadLink.setFinalFileName(getFileNameFromHeader(dl.getConnection()));
        dl.startDownload();
    }

    @Override
    public AccountInfo fetchAccountInfo(Account account) throws Exception {
        AccountInfo ai = new AccountInfo();
        try {
            login(account);
        } catch (PluginException e) {
            account.setValid(false);
            return ai;
        }
        ai.setUnlimitedTraffic();
        ai.setStatus("Registered User");
        return ai;
    }

    @Override
    public String getAGBLink() {
        return "http://www.livemixtapes.com/contact.html";
    }

    @Override
    public int getMaxSimultanFreeDownloadNum() {
        return -1;
    }

    @Override
    public int getMaxSimultanPremiumDownloadNum() {
        return -1;
    }

    @Override
    public void handleFree(DownloadLink downloadLink) throws Exception, PluginException {
        requestFileInformation(downloadLink);
        doFree(downloadLink);
    }

    @Override
    public void handlePremium(DownloadLink link, Account account) throws Exception {
        requestFileInformation(link);
        login(account);
        br.setFollowRedirects(true);
        br.getPage(link.getDownloadURL());
        doFree(link);
    }

    // do not add @Override here to keep 0.* compatibility
    public boolean hasCaptcha() {
        return true;
    }

    private void login(Account account) throws Exception {
        this.setBrowserExclusive();
        // br.getPage(MAINPAGE);
        br.postPage("http://www.livemixtapes.com/login.php", "username=" + Encoding.urlEncode(account.getUser()) + "&password=" + Encoding.urlEncode(account.getPass()));
        if (br.getCookie(MAINPAGE, "u") == null || br.getCookie(MAINPAGE, "p") == null) throw new PluginException(LinkStatus.ERROR_PREMIUM, PluginException.VALUE_ID_PREMIUM_DISABLE);
    }

    @Override
    public AvailableStatus requestFileInformation(DownloadLink link) throws IOException, PluginException {
        this.setBrowserExclusive();
        br.getHeaders().put("Accept-Encoding", "gzip,deflate");
        /** If link is a short link correct it */
        if (new Regex(link.getDownloadURL(), "http://(www\\.)?livemixtap\\.es/[a-z0-9]+").matches()) {
            br.setFollowRedirects(false);
            br.getPage(link.getDownloadURL());
            String correctLink = br.getRedirectLocation();
            if (correctLink == null) throw new PluginException(LinkStatus.ERROR_PLUGIN_DEFECT);
            br.getPage(correctLink);
            correctLink = br.getRedirectLocation();
            if (correctLink == null) throw new PluginException(LinkStatus.ERROR_PLUGIN_DEFECT);
            link.setUrlDownload(correctLink);
            correctDownloadLink(link);
        }
        br.setFollowRedirects(true);
        br.getPage(link.getDownloadURL());
        if (br.containsHTML("(>Not Found</|The page you requested could not be found\\.<|>This mixtape is no longer available for download.<)")) throw new PluginException(LinkStatus.ERROR_FILE_NOT_FOUND);
        String filename = null, filesize = null;
        if (br.containsHTML(MUSTBELOGGEDIN)) {
            final Regex fileInfo = br.getRegex("<td height=\"35\"><div style=\"padding\\-left: 8px\">([^<>\"]*?)</div></td>[\t\n\r ]+<td align=\"center\">([^<>\"]*?)</td>");
            filename = fileInfo.getMatch(0);
            filesize = fileInfo.getMatch(1);
        } else {
            final Regex fileInfo = br.getRegex("<td height=\"35\"><div[^>]+>(.*?)</div></td>[\t\n\r ]+<td align=\"center\">((\\d+(\\.\\d+)? ?(KB|MB|GB)))</td>");
            filename = fileInfo.getMatch(0);
            filesize = fileInfo.getMatch(1);
        }
        if (filename == null || filesize == null) throw new PluginException(LinkStatus.ERROR_PLUGIN_DEFECT);
        link.setFinalFileName(Encoding.htmlDecode(filename.trim()));
        link.setDownloadSize(SizeFormatter.getSize(filesize));
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