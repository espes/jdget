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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import jd.PluginWrapper;
import jd.http.Cookie;
import jd.http.Cookies;
import jd.http.RandomUserAgent;
import jd.nutils.encoding.Encoding;
import jd.parser.Regex;
import jd.plugins.Account;
import jd.plugins.AccountInfo;
import jd.plugins.DownloadLink;
import jd.plugins.DownloadLink.AvailableStatus;
import jd.plugins.HostPlugin;
import jd.plugins.LinkStatus;
import jd.plugins.Plugin;
import jd.plugins.PluginException;
import jd.plugins.PluginForHost;
import jd.utils.locale.JDL;

import org.appwork.utils.formatter.SizeFormatter;
import org.appwork.utils.formatter.TimeFormatter;

@HostPlugin(revision = "$Revision$", interfaceVersion = 2, names = { "uploading.com" }, urls = { "http://[\\w\\.]*?uploading\\.com/files/(get/)?\\w+" }, flags = { 2 })
public class UploadingCom extends PluginForHost {
    private static int          simultanpremium = 1;
    private static final Object PREMLOCK        = new Object();
    // private String otherUseragent =
    // "Mozilla/5.0 (Windows; U; Windows NT 6.0; chrome://global/locale/intl.properties; rv:1.8.1.12) Gecko/2008102920  Firefox/3.0.0";
    private String              userAgent       = RandomUserAgent.generate();
    private boolean             free            = false;
    private static final String FILEIDREGEX     = "name=\"file_id\" value=\"(.*?)\"";
    private static final String CODEREGEX       = "uploading\\.com/files/get/(\\w+)";
    private static final Object LOCK            = new Object();
    private static final String MAINPAGE        = "http://uploading.com/";
    private boolean             loginCaptcha    = false;
    private static final String PASSWORDTEXT    = "Please Enter Password:<";

    public UploadingCom(PluginWrapper wrapper) {
        super(wrapper);
        this.setStartIntervall(1000l);
        this.enablePremium("http://uploading.com/premium/");
    }

    public void checkErrors() throws PluginException {
        logger.info("Checking errors");
        if (br.containsHTML("Sorry, but file you are trying to download is larger then allowed for free download")) throw new PluginException(LinkStatus.ERROR_FATAL, "Only downloadable via account");
        if (br.containsHTML("YOU REACHED YOUR COUNTRY DAY LIMIT")) throw new PluginException(LinkStatus.ERROR_IP_BLOCKED, JDL.L("plugins.hoster.uploadingcom.errors.countrylimitreached", "You reached your country daily limit"), 60 * 60 * 1000l);
        if (br.containsHTML("you have reached your daily download limi")) throw new PluginException(LinkStatus.ERROR_IP_BLOCKED, 1 * 60 * 60 * 1000l);
        if (br.containsHTML("Your IP address is currently downloading a file")) throw new PluginException(LinkStatus.ERROR_IP_BLOCKED, 5 * 60 * 1000l);
        if (br.containsHTML("Only Premium users can download files larger than")) throw new PluginException(LinkStatus.ERROR_FATAL, "Only downloadable via premium");
        if (br.containsHTML("You have reached the daily downloads limit")) throw new PluginException(LinkStatus.ERROR_IP_BLOCKED, 1 * 60 * 60 * 1000l);
        if (br.containsHTML("you can download only one file per")) {
            int wait = 15;
            String time = br.getRegex("you can download only one file per (\\d+) minutes").getMatch(0);
            if (time != null) wait = Integer.parseInt(time.trim());
            throw new PluginException(LinkStatus.ERROR_IP_BLOCKED, wait * 60 * 1000l);
        }
        if (br.containsHTML("(>Server stopped<|Sorry, the server storing the file is currently unavailable|/> Please try again later\\.)")) throw new PluginException(LinkStatus.ERROR_TEMPORARILY_UNAVAILABLE, JDL.L("plugins.hoster.uploadingcom.errors.tempunavailable", "This file is temporary unavailable"), 60 * 60 * 1000l);
    }

    public boolean checkLinks(DownloadLink[] urls) {
        if (urls == null || urls.length == 0) { return false; }
        try {
            ArrayList<DownloadLink> links = new ArrayList<DownloadLink>();
            int index = 0;
            StringBuilder sb = new StringBuilder();
            while (true) {
                sb.delete(0, sb.capacity());
                sb.append("urls=");
                links.clear();
                while (true) {
                    /* we test 100 links at once */
                    if (index == urls.length || links.size() > 100) break;
                    links.add(urls[index]);
                    index++;
                }
                int c = 0;
                for (DownloadLink dl : links) {
                    /*
                     * append fake filename , because api will not report
                     * anything else
                     */
                    if (c > 0) sb.append("%0D%0A");
                    fixLink(dl);
                    sb.append(Encoding.urlEncode(dl.getDownloadURL()));
                    c++;
                }
                /* parser works on english language */
                br.setCookie("http://uploading.com/", "lang", "1");
                br.setCookie("http://uploading.com/", "language", "1");
                br.setCookie("http://uploading.com/", "setlang", "en");
                br.setCookie("http://uploading.com/", "_lang", "en");
                br.setDebug(true);
                br.postPage("http://uploading.com/files/checker/?ajax", sb.toString());
                String correctedHTML = br.toString().replace("\\", "");
                for (DownloadLink dl : links) {
                    String fileid = new Regex(dl.getDownloadURL(), "uploading\\.com/files/(get/)?(.+)").getMatch(1);
                    if (fileid == null) {
                        logger.warning("Uploading.com availablecheck is broken!");
                        return false;
                    }
                    String regexForThisLink = "(\">http://uploading\\.com/files/" + fileid + ".*?/</a></td>ntttt<td>(Aktiv|active|Gelöscht|Deleted)</td>ntttt<td>.*?</td>)";
                    String theData = new Regex(correctedHTML, regexForThisLink).getMatch(0);
                    if (theData == null) {
                        dl.setAvailable(false);
                        continue;
                    }
                    Regex allMatches = new Regex(theData, "\">http://uploading\\.com/files/" + fileid + "/(.*?)/</a></td>ntttt<td>(Aktiv|active|Gelöscht|Deleted)</td>ntttt<td>(.*?)</td>");
                    String status = allMatches.getMatch(1);
                    String filename = allMatches.getMatch(0);
                    String filesize = allMatches.getMatch(2);
                    if (filename == null || filesize == null) {
                        logger.warning("Uploading.com availablecheck is broken!");
                        dl.setAvailable(false);
                    } else if (!status.matches("(Aktiv|active)")) {
                        dl.setAvailable(false);
                    } else {
                        dl.setAvailable(true);
                    }
                    filename = Encoding.htmlDecode(filename.trim());
                    filename = Encoding.urlDecode(filename, false);
                    dl.setName(filename);
                    dl.setDownloadSize(SizeFormatter.getSize(filesize));
                }
                if (index == urls.length) break;
            }
        } catch (Exception e) {
            return false;
        }
        return true;
    }

    public void correctDownloadLink(DownloadLink link) {
        if (!link.getDownloadURL().contains("/get")) link.setUrlDownload(link.getDownloadURL().replace("/files", "/files/get").replace("www.", ""));
        fixLink(link);
    }

    @Override
    public AccountInfo fetchAccountInfo(Account account) throws Exception {
        AccountInfo ai = new AccountInfo();
        this.setBrowserExclusive();
        synchronized (LOCK) {
            try {
                loginCaptcha = true;
                login(account, true);
            } catch (PluginException e) {
                account.setValid(false);
                if (account.getAccountInfo() != null) {
                    ai = account.getAccountInfo();
                }
                return ai;
            } finally {
                loginCaptcha = false;
            }
            if (!isPremium(account, true)) {
                account.setValid(true);
                ai.setStatus("Free Membership");
                return ai;
            }
        }
        account.setValid(true);
        ai.setStatus("Premium Membership");
        String validUntil = br.getRegex("<dt>Valid Until:</dt>[\t\n\r ]+<dd>(.*?) \\(<a").getMatch(0);
        if (validUntil != null) {
            ai.setValidUntil(TimeFormatter.getMilliSeconds(validUntil.trim(), "MMM dd, yyyy", null));
        } else {
            if (br.containsHTML("<dd>Lifetime membership</")) {
                /* lifetime accounts */
                ai.setValidUntil(-1);
            } else {
                /* fallback */
                ai.setValidUntil(br.getCookies(MAINPAGE).get("remembered_user").getExpireDate());
            }
        }
        String balance = br.getRegex("Balance: <b>\\$([0-9\\.]+)+<").getMatch(0);
        if (balance != null) {
            ai.setAccountBalance(balance);
        }
        return ai;
    }

    public AvailableStatus fileCheck(DownloadLink downloadLink) throws PluginException, IOException {
        fixLink(downloadLink);
        br.getPage(downloadLink.getDownloadURL());
        if (br.containsHTML("but due to abuse or through deletion by")) throw new PluginException(LinkStatus.ERROR_FILE_NOT_FOUND);
        if (br.containsHTML("file was removed")) throw new PluginException(LinkStatus.ERROR_FILE_NOT_FOUND);

        String filesize = br.getRegex("File size: <b>(.*?)</b>").getMatch(0);
        String filename = br.getRegex(">Download(.*?)for free on uploading.com").getMatch(0);
        if (filename == null) {
            filename = br.getRegex(">File download</h2><br/>.*?<h2>(.*?)</h2>").getMatch(0);
            if (filename == null) {
                // Last try to get the filename, if this
                String fname = new Regex(downloadLink.getDownloadURL(), "uploading\\.com/files/\\w+/([a-zA-Z0-9 ._]+)").getMatch(0);
                fname = fname.replace(" ", "_");
                if (br.containsHTML(fname)) {
                    filename = fname;
                }

            }
        }
        if (filename == null) throw new PluginException(LinkStatus.ERROR_FILE_NOT_FOUND);
        downloadLink.setName(filename.trim());
        if (filesize != null) {
            downloadLink.setDownloadSize(SizeFormatter.getSize(filesize.trim()));
        }
        return AvailableStatus.TRUE;
    }

    private void fixLink(DownloadLink link) {
        link.setUrlDownload(link.getDownloadURL().replaceFirst("http://www\\.", "http://"));
    }

    @Override
    public String getAGBLink() {
        return "http://uploading.com/terms/";
    }

    public String getDownloadUrl(DownloadLink downloadLink, String fileID, String code, String passCode) throws Exception {
        br.setDebug(true);
        String varLink = br.getRegex("var file_link = \\'(http://.*?)\\'").getMatch(0);
        /* captcha may occur here */
        String captcha = "";
        if (br.containsHTML("var captcha_src = \\'http://uploading")) {
            String captchaUrl = "http://uploading.com/general/captcha/download" + fileID + "/?ts=" + System.currentTimeMillis();
            String captchaCode = getCaptchaCode(captchaUrl, downloadLink);
            captcha = "&captcha_code=" + Encoding.urlEncode(captchaCode);
        } else if (passCode != null) captcha = passCode;
        if (varLink != null) {
            sleep(2000, downloadLink);
            return varLink;
        }
        br.setFollowRedirects(false);
        String starttimer = br.getRegex("start_timer\\((\\d+)\\)").getMatch(0);
        String redirect = null;
        if (starttimer != null) {
            sleep((Long.parseLong(starttimer) + 2) * 1000l, downloadLink);
        }
        br.getHeaders().put("X-Requested-With", "XMLHttpRequest");
        br.postPage("http://uploading.com/files/get/?ajax", "action=get_link&code=" + code + "&pass=" + captcha);
        redirect = br.getRegex("link\":( )?\"(http.*?)\"").getMatch(1);
        if (redirect != null) {
            redirect = redirect.replaceAll("\\\\/", "/");
        } else {
            if (br.containsHTML("Please wait")) throw new PluginException(LinkStatus.ERROR_TEMPORARILY_UNAVAILABLE, 10 * 1000l);
            if (br.containsHTML("Your download was not found or")) throw new PluginException(LinkStatus.ERROR_TEMPORARILY_UNAVAILABLE, "Your download was not found or has expired. Please try again later", 15 * 60 * 1000l);
            if (br.containsHTML("Your download has expired")) throw new PluginException(LinkStatus.ERROR_TEMPORARILY_UNAVAILABLE, "Your download was not found or has expired. Please try again later", 15 * 60 * 1000l);
            // Second Password-Errorhandling
            if (br.containsHTML("\"The entered password is incorrect\"")) throw new PluginException(LinkStatus.ERROR_RETRY, "Invalid password");
            if (captcha.length() > 0) throw new PluginException(LinkStatus.ERROR_CAPTCHA);
            throw new PluginException(LinkStatus.ERROR_PLUGIN_DEFECT);
        }
        return redirect;
    }

    @Override
    public int getMaxSimultanFreeDownloadNum() {
        return 1;
    }

    @Override
    public int getMaxSimultanPremiumDownloadNum() {
        synchronized (PREMLOCK) {
            return simultanpremium;
        }
    }

    @Override
    public int getTimegapBetweenConnections() {
        return 100;
    }

    private void handleDownloadErrors() throws IOException, PluginException {
        logger.info("Handling errors");
        if (dl.getConnection().getResponseCode() == 416) {
            dl.getConnection().disconnect();
            throw new PluginException(LinkStatus.ERROR_TEMPORARILY_UNAVAILABLE, "ServerError", 1000l * 60 * 30);
        }
        if (!dl.getConnection().isContentDisposition()) {
            br.followConnection();
            Cookie cookie = dl.getConnection().getRequest().getCookies().get("error");
            String error = null;
            if (cookie != null) error = cookie.getValue();
            if (error == null) error = br.getCookie("http://uploading.com/", "error");
            if (error != null && error.contains("wait")) throw new PluginException(LinkStatus.ERROR_TEMPORARILY_UNAVAILABLE, 1000l * 15);
            if (error != null && error.contains("reached")) throw new PluginException(LinkStatus.ERROR_IP_BLOCKED, 1 * 60 * 1000l);
            if (br.containsHTML("The page you requested was not found")) throw new PluginException(LinkStatus.ERROR_FILE_NOT_FOUND);
            logger.warning("dl isn't ContentDisposition, plugin must be broken!");
            throw new PluginException(LinkStatus.ERROR_PLUGIN_DEFECT);
        }
        if (dl.getConnection().getLongContentLength() == 0) {
            dl.getConnection().disconnect();
            throw new PluginException(LinkStatus.ERROR_TEMPORARILY_UNAVAILABLE, "ServerError", 1000l * 60 * 30);
        }
    }

    @Override
    public void handleFree(DownloadLink downloadLink) throws Exception {
        requestFileInformation(downloadLink);
        if (!downloadLink.isAvailable()) throw new PluginException(LinkStatus.ERROR_FILE_NOT_FOUND);
        br.setFollowRedirects(true);
        br.getPage(downloadLink.getDownloadURL());
        handleFree0(downloadLink);
    }

    public void handleFree0(DownloadLink link) throws Exception {
        checkErrors();
        String passCode = link.getStringProperty("pass", null);
        String fileID = br.getRegex(FILEIDREGEX).getMatch(0);
        String code = new Regex(link.getDownloadURL(), CODEREGEX).getMatch(0);
        String purse = br.getRegex("type=\"hidden\" name=\"LMI_PAYEE_PURSE\" value=\"([^<>\"\\']+)\"").getMatch(0);
        if (br.containsHTML("that only premium members are")) { throw new PluginException(LinkStatus.ERROR_FATAL, "Only for premium members"); }
        if (fileID == null || code == null || purse == null) {
            logger.warning("The first form equals null");
            throw new PluginException(LinkStatus.ERROR_PLUGIN_DEFECT);
        }
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            return;
        }
        logger.info("Submitting form");
        try {
            // POSTing to the wrong link causes MAJOR issues...
            String postData = "action=second_page&file_id=" + fileID + "&code=" + code + "&LMI_PAYMENT_AMOUNT=%23amount%23&LMI_PAYMENT_DESC=Uploading.com+Premuim+Membership&LMI_PAYEE_PURSE=" + purse + "&LMI_SIM_MODE=0&user_id=%23user_id%23&proceed_without_registration=on";
            if (br.containsHTML(PASSWORDTEXT)) {
                if (passCode == null) passCode = Plugin.getUserInput("Password?", link);
                passCode = Encoding.urlEncode(passCode);
                postData += "&pass=" + passCode;
            }
            br.postPage(link.getDownloadURL().replace("www.", "") + "/", postData);
        } catch (Exception e) {
            // This is the "disconnected" error...
            logger.warning("FATAL error happened with link: " + link.getDownloadURL());
            throw new PluginException(LinkStatus.ERROR_PLUGIN_DEFECT);
        }
        // First Password-Errorhandling
        if (passCode != null && (br.containsHTML(PASSWORDTEXT) || "The%20entered%20password%20is%20incorrect".equals(br.getCookie(MAINPAGE, "error")))) throw new PluginException(LinkStatus.ERROR_RETRY, "Invalid password");
        checkErrors();
        String redirect = getDownloadUrl(link, fileID, code, passCode);
        br.setFollowRedirects(false);
        br.setDebug(true);
        dl = jd.plugins.BrowserAdapter.openDownload(br, link, redirect, true, 1);
        handleDownloadErrors();
        dl.setFilenameFix(true);
        if (passCode != null) link.setProperty("pass", passCode);
        dl.startDownload();
    }

    @Override
    public void handlePremium(DownloadLink link, Account account) throws Exception {
        free = false;
        br.setDebug(true);
        requestFileInformation(link);
        String passCode = link.getStringProperty("pass");
        synchronized (PREMLOCK) {
            synchronized (LOCK) {
                login(account, false);
                if (!isPremium(account, false)) {
                    simultanpremium = 1;
                    free = true;
                } else {
                    if (simultanpremium + 1 > 20) {
                        simultanpremium = 20;
                    } else {
                        simultanpremium++;
                    }
                }
            }
        }
        String redirect = null;
        br.setFollowRedirects(false);
        br.getPage(link.getDownloadURL());
        if (br.getRedirectLocation() != null) {
            String red = br.getRedirectLocation();
            if (red.contains("get_file/")) {
                redirect = red;
            } else {
                br.getPage(red);
            }
        }
        if (redirect == null) {
            if (free) {
                handleFree0(link);
                return;
            }
            String code = new Regex(link.getDownloadURL(), CODEREGEX).getMatch(0);
            if (code == null) {
                logger.warning("The first form equals null");
                throw new PluginException(LinkStatus.ERROR_PLUGIN_DEFECT);
            }
            if (br.containsHTML(PASSWORDTEXT)) {
                if (passCode == null) passCode = Plugin.getUserInput("Password?", link);
                passCode = Encoding.urlEncode(passCode);
            }
            redirect = getDownloadUrl(link, null, code, passCode);
        }
        br.setFollowRedirects(false);
        dl = jd.plugins.BrowserAdapter.openDownload(br, link, redirect, true, 0);
        handleDownloadErrors();
        dl.setFilenameFix(true);
        if (passCode != null) link.setProperty("pass", passCode);
        dl.startDownload();
    }

    // do not add @Override here to keep 0.* compatibility
    public boolean hasCaptcha() {
        return true;
    }

    public boolean isPremium(Account account, boolean force) throws IOException {
        boolean isPremium = false;
        synchronized (LOCK) {
            boolean follow = br.isFollowingRedirects();
            br.setFollowRedirects(true);
            br.getPage("http://uploading.com/profile/");
            br.setFollowRedirects(follow);
            if (br.containsHTML("UPGRADE TO PREMIUM")) {
                isPremium = false;
            } else if (br.containsHTML("<dt>Membership:</dt>[\t\n\r ]+<dd>Premium</dd")) {
                isPremium = true;
            } else {
                isPremium = false;
            }
        }
        return isPremium;
    }

    /**
     * TODO: remove with next major update, DownloadWatchDog/AccountController
     * handle blocked accounts now
     */
    @SuppressWarnings("deprecation")
    @Override
    public boolean isPremiumDownload() {
        /* free user accounts are no premium accounts */
        boolean ret = super.isPremiumDownload();
        if (ret && free) ret = false;
        return ret;
    }

    public void login(Account account, boolean forceLogin) throws IOException, PluginException {
        boolean valid = false;
        try {
            synchronized (LOCK) {
                this.setBrowserExclusive();
                br.getHeaders().put("User-Agent", userAgent);
                br.setCookie(MAINPAGE, "lang", "1");
                br.setCookie(MAINPAGE, "language", "1");
                br.setCookie(MAINPAGE, "setlang", "en");
                br.setCookie(MAINPAGE, "_lang", "en");
                if (!forceLogin) {
                    Object cookiesRet = account.getProperty("cookies");
                    Map<String, String> cookies = null;
                    if (cookiesRet != null && cookiesRet instanceof Map) {
                        cookies = (Map<String, String>) cookiesRet;
                    }
                    if (cookies != null) {
                        if (cookies.containsKey("remembered_user") && account.isValid()) {
                            for (final String key : cookies.keySet()) {
                                this.br.setCookie("http://uploading.com/", key, cookies.get(key));
                            }
                            valid = true;
                            return;
                        }
                    }
                }
                br.getPage(MAINPAGE);
                final String damnCookie = br.getCookie(MAINPAGE, "SID");
                if (damnCookie == null) throw new PluginException(LinkStatus.ERROR_PREMIUM, PluginException.VALUE_ID_PREMIUM_DISABLE);
                br.postPage("http://uploading.com/general/login_form/?SID=" + damnCookie + "&JsHttpRequest=" + System.currentTimeMillis() + "-xml", "email=" + Encoding.urlEncode(account.getUser()) + "&password=" + Encoding.urlEncode(account.getPass()) + "&remember=on");
                if (br.containsHTML("captcha\":\"") && loginCaptcha) {
                    String captchaID = br.getRegex("captcha\":\"(.*?)\"").getMatch(0);
                    String cf = "http://uploading.com/general/captcha/" + captchaID + "/?ts=" + System.currentTimeMillis();
                    DownloadLink dummyLink = new DownloadLink(null, "Account", "uploading.com", "http://uploading.com", true);
                    String c = getCaptchaCode(cf, dummyLink);
                    br.postPage("http://uploading.com/general/login_form/?JsHttpRequest=" + System.currentTimeMillis() + "-xml", "email=" + Encoding.urlEncode(account.getUser()) + "&password=" + Encoding.urlEncode(account.getPass()) + "&remember=on&captcha=" + Encoding.urlEncode(c));
                }
                if (br.containsHTML("captcha\":\"")) {
                    AccountInfo ai = account.getAccountInfo();
                    if (ai == null) {
                        ai = new AccountInfo();
                        account.setAccountInfo(ai);
                    }
                    ai.setStatus("Logout/Login in Browser please!");
                    throw new PluginException(LinkStatus.ERROR_PREMIUM, PluginException.VALUE_ID_PREMIUM_DISABLE);
                }
                if (br.containsHTML("Please enter correct")) {
                    AccountInfo ai = account.getAccountInfo();
                    if (ai == null) {
                        ai = new AccountInfo();
                        account.setAccountInfo(ai);
                    }
                    ai.setStatus("Please enter correct e-mail and password!");
                    throw new PluginException(LinkStatus.ERROR_PREMIUM, PluginException.VALUE_ID_PREMIUM_DISABLE);
                }
                if (br.getCookie(MAINPAGE, "remembered_user") != null) {
                    /* change language to english */
                    br.postPage("http://uploading.com/general/select_language/?JsHttpRequest=" + System.currentTimeMillis() + "-xml", "language=1");
                    final HashMap<String, String> cookies = new HashMap<String, String>();
                    final Cookies add = this.br.getCookies(MAINPAGE);
                    for (final Cookie c : add.getCookies()) {
                        cookies.put(c.getKey(), c.getValue());
                    }
                    account.setProperty("cookies", cookies);
                    valid = true;
                } else {
                    valid = false;
                }
            }
        } finally {
            if (valid == false) {
                account.setProperty("cookies", null);
                throw new PluginException(LinkStatus.ERROR_PREMIUM, PluginException.VALUE_ID_PREMIUM_DISABLE);
            }
        }
    }

    @Override
    public AvailableStatus requestFileInformation(DownloadLink downloadLink) throws Exception {
        setBrowserExclusive();
        br.getHeaders().put("User-Agent", userAgent);
        br.setFollowRedirects(true);
        br.setCookie(MAINPAGE, "lang", "1");
        br.setCookie(MAINPAGE, "language", "1");
        br.setCookie(MAINPAGE, "setlang", "en");
        br.setCookie(MAINPAGE, "_lang", "en");
        return fileCheck(downloadLink);
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