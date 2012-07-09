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
import java.util.regex.Pattern;

import jd.PluginWrapper;
import jd.nutils.encoding.Encoding;
import jd.parser.html.Form;
import jd.plugins.Account;
import jd.plugins.AccountInfo;
import jd.plugins.DownloadLink;
import jd.plugins.DownloadLink.AvailableStatus;
import jd.plugins.HostPlugin;
import jd.plugins.LinkStatus;
import jd.plugins.PluginException;
import jd.plugins.PluginForHost;
import jd.utils.locale.JDL;

import org.appwork.utils.formatter.SizeFormatter;
import org.appwork.utils.formatter.TimeFormatter;

@HostPlugin(revision = "$Revision$", interfaceVersion = 2, names = { "vip-file.com" }, urls = { "http://[\\w\\.]*?vip-file\\.com/download(lib)?/.*?/.*?\\.html" }, flags = { 2 })
public class Vipfilecom extends PluginForHost {

    public static final String  FREELINKREGEX = "\"(http://vip-file.com/download([0-9]+)/.*?)\"";

    private static final Object LOCK          = new Object();

    public Vipfilecom(PluginWrapper wrapper) {
        super(wrapper);
        this.setAccountwithoutUsername(true);
        enablePremium("http://vip-file.com/tmpl/premium_en.php");
    }

    public AccountInfo fetchAccountInfo(Account account) throws Exception {
        synchronized (LOCK) {
            AccountInfo ai = new AccountInfo();
            ai.setStatus("Status can only be checked while downloading!");
            account.setValid(true);
            return ai;
        }
    }

    @Override
    public String getAGBLink() {
        return "http://vip-file.com/page/terms.php";
    }

    @Override
    public int getMaxSimultanFreeDownloadNum() {
        return -1;
    }

    @Override
    public void handleFree(DownloadLink downloadLink) throws Exception {
        requestFileInformation(downloadLink);
        /* DownloadLink holen, 2x der Location folgen */
        /* we have to wait little because server too buggy */
        sleep(2000, downloadLink);
        String link = Encoding.htmlDecode(br.getRegex(Pattern.compile(FREELINKREGEX, Pattern.CASE_INSENSITIVE)).getMatch(0));
        if (link == null) {
            try {
                throw new PluginException(LinkStatus.ERROR_PREMIUM, PluginException.VALUE_ID_PREMIUM_ONLY);
            } catch (final Throwable e) {
                if (e instanceof PluginException) throw (PluginException) e;
            }
            throw new PluginException(LinkStatus.ERROR_FATAL, JDL.L("plugins.hoster.vipfilecom.errors.nofreedownloadlink", "No free download link for this file"));
        }
        br.setDebug(true);
        /* SpeedHack */
        br.setFollowRedirects(false);
        br.getPage(link);
        link = br.getRedirectLocation();
        if (link == null) throw new PluginException(LinkStatus.ERROR_PLUGIN_DEFECT);
        if (!link.contains("vip-file.com")) throw new PluginException(LinkStatus.ERROR_FATAL, JDL.L("plugins.hoster.vipfilecom.errors.nofreedownloadlink", "No free download link for this file"));
        // link = link.replaceAll("file.com.*?/", "file.com:8080/");
        br.setFollowRedirects(true);
        jd.plugins.BrowserAdapter.openDownload(br, downloadLink, link, true, 1).startDownload();
    }

    @Override
    public void handlePremium(DownloadLink downloadLink, Account account) throws Exception {
        requestFileInformation(downloadLink);
        br.setCookie("http://vip-file.com/", "lang", "en");
        Form[] allForms = br.getForms();
        if (allForms == null || allForms.length == 0) throw new PluginException(LinkStatus.ERROR_PLUGIN_DEFECT);
        Form premiumform = null;
        for (Form singleForm : allForms) {
            if (singleForm.containsHTML("pass") && singleForm.containsHTML("sms/check2.php")) {
                premiumform = singleForm;
                break;
            }
        }
        if (premiumform == null) throw new PluginException(LinkStatus.ERROR_PLUGIN_DEFECT);
        premiumform.put("pass", Encoding.urlEncode(account.getPass()));
        br.submitForm(premiumform);
        // Try to find the remaining traffic, 1 Point = 1 GB
        String trafficLeft = br.getRegex("\">Points:</acronym> ([0-9\\.]+)</li>").getMatch(0);
        if (trafficLeft != null && !trafficLeft.equals("")) {
            AccountInfo ai = account.getAccountInfo();
            if (ai == null) ai = new AccountInfo();
            ai.setTrafficLeft(SizeFormatter.getSize(trafficLeft + "GB"));
            ai.setStatus("Premium User");
            account.setAccountInfo(ai);
        }
        String expireDate = br.getRegex(">Period of validity:</acronym> (.*?) \\[<acronym").getMatch(0);
        if (expireDate != null) {
            AccountInfo ai = account.getAccountInfo();
            if (ai == null) ai = new AccountInfo();
            ai.setValidUntil(TimeFormatter.getMilliSeconds(expireDate, "yyyy-MM-dd", null));
            ai.setStatus("Premium User");
            account.setAccountInfo(ai);
            if (ai.isExpired()) { throw new PluginException(LinkStatus.ERROR_PREMIUM, PluginException.VALUE_ID_PREMIUM_DISABLE); }
        }
        String urls[] = br.getRegex(Pattern.compile("title=\"Link to the file download\" href=\"(http://[^<>\"\\']+)\"", Pattern.CASE_INSENSITIVE)).getColumn(0);
        if (urls == null) {
            urls = br.getRegex("\"(http://\\d+\\.\\d+\\.\\d+\\.\\d+/f/[a-z0-9]+/[^<>\"\\'/]+)\"").getColumn(0);
        }
        if (urls == null && br.containsHTML("(Wrong password|>This password expired<)")) {
            logger.info("Downloadpassword seems to be wrong, disabeling account now!");
            throw new PluginException(LinkStatus.ERROR_PREMIUM, PluginException.VALUE_ID_PREMIUM_DISABLE);
        }
        if (urls == null) {
            if (br.containsHTML("(Your premium access is about to be over|Amount of Your points is close to zero\\.)")) {
                logger.info("Password is wrong!");
                throw new PluginException(LinkStatus.ERROR_PREMIUM, PluginException.VALUE_ID_PREMIUM_DISABLE);
            }
            throw new PluginException(LinkStatus.ERROR_PLUGIN_DEFECT);
        }
        /* we have to wait little because server too buggy */
        int index = 0;
        for (String url : urls) {
            index++;
            sleep(2000, downloadLink);
            dl = jd.plugins.BrowserAdapter.openDownload(br.cloneBrowser(), downloadLink, url, true, 0);
            if (dl.getConnection().getContentType().contains("html")) {
                if (dl.getConnection().getResponseCode() == 404) {
                    dl.getConnection().disconnect();
                    continue;
                }
                if (index == urls.length) {
                    br.followConnection();
                    if (br.containsHTML("Error")) throw new PluginException(LinkStatus.ERROR_TEMPORARILY_UNAVAILABLE, "ServerError", 2 * 1000l);
                    throw new PluginException(LinkStatus.ERROR_PLUGIN_DEFECT);
                } else {
                    continue;
                }
            }
            dl.startDownload();
            return;
        }
        logger.info("no working link found");
        throw new PluginException(LinkStatus.ERROR_PLUGIN_DEFECT);
    }

    @Override
    public AvailableStatus requestFileInformation(DownloadLink downloadLink) throws PluginException, IOException {
        String downloadURL = downloadLink.getDownloadURL();
        this.setBrowserExclusive();
        br.getHeaders().put("User-Agent", "Mozilla/5.0 (X11; Ubuntu; Linux x86_64; rv:13.0) Gecko/20100101 Firefox/13.0.1");
        br.setReadTimeout(2 * 60 * 1000);
        br.setCookie("http://vip-file.com/", "lang", "en");
        br.getPage(downloadURL);
        if (br.containsHTML("(This file not found|\">File not found)")) throw new PluginException(LinkStatus.ERROR_FILE_NOT_FOUND);
        String fileSize = br.getRegex("name=\"sssize\" value=\"(.*?)\"").getMatch(0);
        if (fileSize == null) fileSize = br.getRegex("<p>Size of file: <span>(.*?)</span>").getMatch(0);
        String fileName = br.getRegex("<input type=\"hidden\" name=\"realname\" value=\"(.*?)\" />").getMatch(0);
        if (fileSize == null || fileName == null) throw new PluginException(LinkStatus.ERROR_PLUGIN_DEFECT);
        downloadLink.setDownloadSize(SizeFormatter.getSize(fileSize));
        downloadLink.setName(fileName);
        String link = Encoding.htmlDecode(br.getRegex(Pattern.compile(FREELINKREGEX, Pattern.CASE_INSENSITIVE)).getMatch(0));
        if (link == null) {
            downloadLink.getLinkStatus().setStatusText(JDL.L("plugins.hoster.vipfilecom.errors.nofreedownloadlink", "No free download link for this file"));
            return AvailableStatus.TRUE;
        }
        return AvailableStatus.TRUE;
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