//    jDownloader - Downloadmanager
//    Copyright (C) 2012  JD-Team support@jdownloader.org
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

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;

import jd.PluginWrapper;
import jd.config.ConfigContainer;
import jd.config.ConfigEntry;
import jd.http.Browser;
import jd.http.Cookie;
import jd.http.Cookies;
import jd.http.RandomUserAgent;
import jd.nutils.JDHash;
import jd.nutils.encoding.Encoding;
import jd.parser.Regex;
import jd.parser.html.Form;
import jd.plugins.Account;
import jd.plugins.AccountInfo;
import jd.plugins.DownloadLink;
import jd.plugins.DownloadLink.AvailableStatus;
import jd.plugins.HostPlugin;
import jd.plugins.LinkStatus;
import jd.plugins.PluginException;
import jd.plugins.PluginForHost;
import jd.utils.JDHexUtils;
import jd.utils.JDUtilities;
import jd.utils.locale.JDL;

import org.appwork.utils.formatter.SizeFormatter;
import org.appwork.utils.formatter.TimeFormatter;

//When adding new domains here also add them to the turbobit.net decrypter (TurboBitNetFolder)
@HostPlugin(revision = "$Revision$", interfaceVersion = 2, names = { "turbobit.net" }, urls = { "http://(www\\.)?(maxisoc\\.ru|turo\\-bit\\.net|depositfiles\\.com\\.ua|dlbit\\.net|sharephile\\.com|filesmail\\.ru|hotshare\\.biz|bluetooths\\.pp\\.ru|speed-file\\.ru|sharezoid\\.com|turbobit\\.pl|dz-files\\.ru|file\\.alexforum\\.ws|file\\.grad\\.by|file\\.krut-warez\\.ru|filebit\\.org|files\\.best-trainings\\.org\\.ua|files\\.wzor\\.ws|gdefile\\.ru|letitshare\\.ru|mnogofiles\\.com|share\\.uz|sibit\\.net|turbo-bit\\.ru|turbobit\\.net|upload\\.mskvn\\.by|vipbit\\.ru|files\\.prime-speed\\.ru|filestore\\.net\\.ru|turbobit\\.ru|upload\\.dwmedia\\.ru|upload\\.uz|xrfiles\\.ru|unextfiles\\.com|e-flash\\.com\\.ua|turbobax\\.net|zharabit\\.net|download\\.uzhgorod\\.name|trium-club\\.ru|alfa-files\\.com|turbabit\\.net|filedeluxe\\.com|turbobit\\.name|files\\.uz\\-translations\\.uz|turboblt\\.ru|fo\\.letitbook\\.ru|freefo\\.ru|bayrakweb\\.com)/(.*?\\.html|download/free/[a-z0-9]+)" }, flags = { 2 })
public class TurboBitNet extends PluginForHost {

    private final static String UA            = RandomUserAgent.generate();
    private static final String RECAPTCHATEXT = "api\\.recaptcha\\.net";
    private static final String CAPTCHAREGEX  = "\"(http://turbobit\\.net/captcha/.*?)\"";
    private static String       MAINPAGE      = "http://turbobit.net";
    private static final Object LOCK          = new Object();
    private static final String BLOCKED       = "Turbobit.net is blocking JDownloader: Please contact the turbobit.net support and complain!";

    public TurboBitNet(final PluginWrapper wrapper) {
        super(wrapper);
        setConfigElements();
        enablePremium("http://turbobit.net/turbo");
    }

    @Override
    public boolean checkLinks(final DownloadLink[] urls) {
        if (urls == null || urls.length == 0) { return false; }
        try {
            final Browser br = new Browser();
            br.setCookie(MAINPAGE, "user_lang", "en");
            br.getHeaders().put("X-Requested-With", "XMLHttpRequest");
            br.setCookiesExclusive(true);
            final StringBuilder sb = new StringBuilder();
            final ArrayList<DownloadLink> links = new ArrayList<DownloadLink>();
            int index = 0;
            while (true) {
                links.clear();
                while (true) {
                    /* we test 50 links at once */
                    if (index == urls.length || links.size() > 49) {
                        break;
                    }
                    links.add(urls[index]);
                    index++;
                }
                sb.delete(0, sb.capacity());
                sb.append("links_to_check=");
                for (final DownloadLink dl : links) {
                    sb.append(dl.getDownloadURL());
                    sb.append("%0A");
                }
                br.postPage("http://" + getHost() + "/linkchecker/check", sb.toString());
                for (final DownloadLink dllink : links) {
                    final String linkID = getID(dllink.getDownloadURL());
                    final Regex fileInfo = br.getRegex("<td>" + linkID + "</td>[\t\n\r ]+<td>([^<>/\"]*?)</td>[\t\n\r ]+<td style=\"text\\-align:center;\"><img src=\"/img/icon/(done|error)\\.png\"");
                    if (fileInfo.getMatches() == null || fileInfo.getMatches().length == 0) {
                        dllink.setAvailable(false);
                        logger.warning("Linkchecker broken for " + getHost() + " Example link: " + dllink.getDownloadURL());
                    } else {
                        if (fileInfo.getMatch(1).equals("error")) {
                            dllink.setAvailable(false);
                        } else {
                            final String name = fileInfo.getMatch(0);
                            dllink.setAvailable(true);
                            dllink.setFinalFileName(Encoding.htmlDecode(name.trim()));
                        }
                    }
                }
                if (index == urls.length) {
                    break;
                }
            }
        } catch (final Exception e) {
            return false;
        }
        return true;
    }

    @Override
    public void correctDownloadLink(final DownloadLink link) {
        final String freeId = new Regex(link.getDownloadURL(), "download/free/([a-z0-9]+)").getMatch(0);
        if (freeId != null) {
            link.setUrlDownload(MAINPAGE + "/" + freeId + ".html");
        } else {
            link.setUrlDownload(link.getDownloadURL().replaceAll("://[^/]+", "://turbobit.net"));
        }
    }

    private String escape(final String s) {
        final byte[] org = s.getBytes();
        final StringBuilder sb = new StringBuilder();
        String code;
        for (final byte element : org) {
            sb.append('%');
            code = Integer.toHexString(element);
            code = code.length() % 2 > 0 ? "0" + code : code;
            sb.append(code.substring(code.length() - 2));
        }
        return sb + "";
    }

    @Override
    public AccountInfo fetchAccountInfo(final Account account) throws Exception {
        final AccountInfo ai = new AccountInfo();
        try {
            login(account, true);
        } catch (final PluginException e) {
            if (br.containsHTML("Our service is currently unavailable in your country.")) {
                ai.setStatus("Our service is currently unavailable in your country.");
            }
            account.setValid(false);
            return ai;
        }
        ai.setUnlimitedTraffic();
        String expire = br.getRegex("<u>(Turbo Access|Turbo Zugang)</u> to(.*?)(<a|</di)").getMatch(1);
        if (expire == null) {
            expire = br.getRegex("<u>Турбо доступ</u> до(.*?)(<a|</di)").getMatch(0);
            if (expire == null) {
                expire = br.getRegex("<img src=\\'/img/icon/yesturbo\\.png\\'> <u>.{5,20}</u> .{1,5} (.*?) <a href=\\'/turbo\\'>").getMatch(0);
            }
        }
        if (expire == null) {
            ai.setExpired(true);
            account.setValid(false);
            return ai;
        } else {
            ai.setValidUntil(TimeFormatter.getMilliSeconds(expire.trim(), "dd.MM.yyyy", null));
        }
        ai.setStatus("Premium User");
        return ai;
    }

    @Override
    public String getAGBLink() {
        return "http://turbobit.net/rules";
    }

    private String getID(final String downloadlink) {
        return new Regex(downloadlink, "http://.*?/([a-zA-Z0-9]+)").getMatch(0);
    }

    @Override
    public int getMaxSimultanFreeDownloadNum() {
        return 1;
    }

    @Override
    public int getMaxSimultanPremiumDownloadNum() {
        return -1;
    }

    @Override
    public void handleFree(final DownloadLink downloadLink) throws Exception {
        /*
         * we have to load the plugin first! we must not reference a plugin
         * class without loading it before
         */
        JDUtilities.getPluginForDecrypt("linkcrypt.ws");
        requestFileInformation(downloadLink);
        prepareBrowser(UA);
        String dllink = downloadLink.getDownloadURL();
        br.getPage(dllink);
        if (br.containsHTML("(>Please wait, searching file|\\'File not found\\. Probably it was deleted)")) { throw new PluginException(LinkStatus.ERROR_FILE_NOT_FOUND); }
        String fileSize = br.getRegex("File size:</b>(.*?)</div>").getMatch(0);
        if (fileSize == null) {
            fileSize = br.getRegex("<span class=\\'file\\-icon.*?\\'>.*?</span>.*?\\((.*?)\\)").getMatch(0);
        }
        if (fileSize != null) {
            fileSize = fileSize.replace("М", "M");
            fileSize = fileSize.replace("к", "k");
            fileSize = fileSize.replace("Г", "g");
            fileSize = fileSize.replace("б", "");
            if (!fileSize.endsWith("b")) {
                fileSize = fileSize + "b";
            }
            downloadLink.setDownloadSize(SizeFormatter.getSize(fileSize.trim().replace(",", ".").replace(" ", "")));
        }
        String downloadUrl = null, waittime = null;
        String id = new Regex(dllink, "turbobit\\.net/(.*?)/.*?\\.html").getMatch(0);
        if (id == null) {
            id = new Regex(dllink, "turbobit\\.net/(.*?)\\.html").getMatch(0);
        }
        if (id == null) { throw new PluginException(LinkStatus.ERROR_PLUGIN_DEFECT); }
        br.getPage("/download/free/" + id);

        Form captchaform = null;
        final Form[] allForms = br.getForms();
        if (allForms != null && allForms.length != 0) {
            for (final Form aForm : allForms) {
                if (aForm.containsHTML("captcha")) {
                    captchaform = aForm;
                    break;
                }
            }
        }

        if (captchaform == null) {
            if (br.containsHTML(tb(0))) {
                waittime = br.getRegex(tb(1)).getMatch(0);
                final int wait = waittime != null ? Integer.parseInt(waittime) : -1;

                if (wait > 31) {
                    throw new PluginException(LinkStatus.ERROR_IP_BLOCKED, wait * 1001l);
                } else if (wait < 0) {
                } else {
                    sleep(wait * 1000l, downloadLink);
                }
            }
            waittime = br.getRegex(tb(1)).getMatch(0);
            if (waittime != null) { throw new PluginException(LinkStatus.ERROR_IP_BLOCKED, Integer.parseInt(waittime) * 1001l); }
        }

        if (captchaform == null) {
            if (br.containsHTML("Our service is currently unavailable in your country.")) { throw new PluginException(LinkStatus.ERROR_FATAL, "Our service is currently unavailable in your country."); }
            logger.warning("captchaform equals null!");
            throw new PluginException(LinkStatus.ERROR_PLUGIN_DEFECT);
        }
        if (br.containsHTML(RECAPTCHATEXT)) {
            logger.info("Handling Re Captcha");
            final String theId = new Regex(br.toString(), "challenge\\?k=(.*?)\"").getMatch(0);
            if (theId == null) {
                logger.warning("the id for Re Captcha equals null!");
                throw new PluginException(LinkStatus.ERROR_PLUGIN_DEFECT);
            }
            final PluginForHost recplug = JDUtilities.getPluginForHost("DirectHTTP");
            final jd.plugins.hoster.DirectHTTP.Recaptcha rc = ((DirectHTTP) recplug).getReCaptcha(br);
            rc.setId(theId);
            rc.setForm(captchaform);
            rc.load();
            final File cf = rc.downloadCaptcha(getLocalCaptchaFile());
            final String c = getCaptchaCode("recaptcha", cf, downloadLink);
            rc.getForm().setAction(MAINPAGE + "/download/free/" + id + "#");
            rc.setCode(c);
            if (br.containsHTML(RECAPTCHATEXT) || br.containsHTML("Incorrect, try again")) { throw new PluginException(LinkStatus.ERROR_CAPTCHA); }
        } else {
            logger.info("Handling normal captchas");
            final String captchaUrl = br.getRegex(CAPTCHAREGEX).getMatch(0);
            if (captchaUrl == null) { throw new PluginException(LinkStatus.ERROR_PLUGIN_DEFECT); }
            for (int i = 1; i <= 2; i++) {
                String captchaCode;
                if (!getPluginConfig().getBooleanProperty("JAC", false) || i == 2) {
                    captchaCode = getCaptchaCode(null, captchaUrl, downloadLink);
                } else if (captchaUrl.contains("/basic/")) {
                    logger.info("Handling basic captchas");
                    captchaCode = getCaptchaCode("turbobit.net.basic", captchaUrl, downloadLink);
                } else {
                    captchaCode = getCaptchaCode(captchaUrl, downloadLink);
                }
                captchaform.put("captcha_response", captchaCode);
                br.submitForm(captchaform);
                if (br.getRegex(CAPTCHAREGEX).getMatch(0) == null) {
                    break;
                }
            }
            if (br.getRegex(CAPTCHAREGEX).getMatch(0) != null || br.containsHTML(RECAPTCHATEXT)) { throw new PluginException(LinkStatus.ERROR_CAPTCHA); }
        }
        // Ticket Time
        String ttt = parseImageUrl(br.getRegex(jd.plugins.decrypter.LnkCrptWs.IMAGEREGEX(null)).getMatch(0), true);
        int maxWait = 9999, realWait = 0;
        for (String s : br.getRegex(tb(11)).getColumn(0)) {
            realWait = Integer.parseInt(s);
            if (realWait == 0) continue;
            if (realWait < maxWait) maxWait = realWait;
        }
        int tt = 60;
        if (ttt != null) {
            tt = Integer.parseInt(ttt);
            tt = tt < realWait ? tt : realWait;
            if (tt < 30 || tt > 600) {
                ttt = parseImageUrl(tb(2) + tt + "};" + br.getRegex(tb(3)).getMatch(0), false);
                if (ttt == null) { throw new PluginException(LinkStatus.ERROR_HOSTER_TEMPORARILY_UNAVAILABLE, BLOCKED, 10 * 60 * 60 * 1001l); }
                tt = Integer.parseInt(ttt);
            }
            logger.info(" Waittime detected, waiting " + String.valueOf(tt) + " seconds from now on...");
            if (tt > 250) { throw new PluginException(LinkStatus.ERROR_IP_BLOCKED, "Limit reached or IP already loading", tt * 1001l); }
        }

        final Browser tOut = br.cloneBrowser();
        final String to = br.getRegex("(?i)(/\\w+/timeout\\.js\\?\\w+=[^\"\'<>]+)").getMatch(0);
        tOut.getPage(to == null ? "/files/timeout.js?ver=" + JDHash.getMD5(String.valueOf(Math.random())).toUpperCase(Locale.ENGLISH) : to);
        final String fun = escape(tOut.toString());
        br.getHeaders().put("X-Requested-With", "XMLHttpRequest");

        // realtime update
        String rtUpdate = getPluginConfig().getStringProperty("rtupdate", null);
        final boolean isUpdateNeeded = getPluginConfig().getBooleanProperty("isUpdateNeeded", false);
        int attemps = getPluginConfig().getIntegerProperty("attemps", 1);

        if (isUpdateNeeded || rtUpdate == null) {
            final Browser rt = new Browser();
            rtUpdate = rt.getPage("http://update0.jdownloader.org/pluginstuff/tbupdate.js");
            getPluginConfig().setProperty("rtupdate", rtUpdate);
            getPluginConfig().setProperty("isUpdateNeeded", false);
            getPluginConfig().setProperty("attemps", attemps++);
            getPluginConfig().save();
        }

        String res = rhino("var id = \'" + id + "\';@" + fun + "@" + rtUpdate, 666);
        if (res == null || res != null && !res.matches(tb(10))) {
            res = rhino("var id = \'" + id + "\';@" + fun + "@" + rtUpdate, 100);
            if (new Regex(res, "/~ID~/").matches()) {
                res = res.replaceAll("/~ID~/", id);
            }
        }

        if (res != null && res.matches(tb(10))) {
            sleep(tt * 1001, downloadLink);
            // Wed Jun 13 12:29:47 UTC 0200 2012
            SimpleDateFormat df = new SimpleDateFormat("EEE MMM dd HH:mm:ss zZ yyyy");
            Date date = new Date();
            br.setCookie(br.getHost(), "turbobit1", Encoding.urlEncode_light(df.format(date)).replace(":", "%3A"));

            br.getPage(res);
            downloadUrl = rhino(escape(br.toString()) + "@" + rtUpdate, 999);
            if (downloadUrl != null) {
                downloadUrl = downloadUrl.replaceAll(MAINPAGE, "");
                if (downloadUrl.equals("/download/free/" + id)) {
                    downloadUrl = null;
                }
            }
            if (downloadUrl == null) {
                downloadUrl = br.getRegex("(/download/redirect/[0-9A-F]{32}/" + dllink.replaceAll(MAINPAGE, "") + ")").getMatch(0);
                if (downloadUrl == null) {
                    downloadUrl = br.getRegex("<a href=\'([^\']+)").getMatch(0);
                }
            }
        }
        if (downloadUrl == null) {
            getPluginConfig().setProperty("isUpdateNeeded", true);
            if (br.containsHTML("The file is not avaliable now because of technical problems")) { throw new PluginException(LinkStatus.ERROR_TEMPORARILY_UNAVAILABLE, 15 * 60 * 1000l); }

            if (attemps > 1) {
                getPluginConfig().setProperty("isUpdateNeeded", false);
                getPluginConfig().setProperty("attemps", 1);
                getPluginConfig().save();
                throw new PluginException(LinkStatus.ERROR_HOSTER_TEMPORARILY_UNAVAILABLE, BLOCKED, 10 * 60 * 60 * 1000l);
            } else {
                throw new PluginException(LinkStatus.ERROR_RETRY);
            }
        }

        dl = jd.plugins.BrowserAdapter.openDownload(br, downloadLink, downloadUrl, true, 1);
        if (dl.getConnection().getContentType().contains("html")) {
            if (dl.getConnection().getResponseCode() == 404) {
                try {
                    dl.getConnection().disconnect();
                } catch (final Throwable e) {
                }
                throw new PluginException(LinkStatus.ERROR_FILE_NOT_FOUND);
            }
            br.followConnection();
            if (br.containsHTML("Try to download it once again after")) { throw new PluginException(LinkStatus.ERROR_TEMPORARILY_UNAVAILABLE, "Server error", 20 * 60 * 1000l); }
            throw new PluginException(LinkStatus.ERROR_HOSTER_TEMPORARILY_UNAVAILABLE, BLOCKED, 10 * 60 * 1000l);
        }
        dl.startDownload();
    }

    @Override
    public void handlePremium(final DownloadLink link, final Account account) throws Exception {
        requestFileInformation(link);
        login(account, false);
        sleep(2000, link);
        br.getPage(link.getDownloadURL());
        String dllink = br.getRegex("<h1><a href=\\'(.*?)\\'>").getMatch(0);
        if (dllink == null) {
            dllink = br.getRegex("(\\'|\")(http://(www\\.)?turbobit\\.net//download/redirect/.*?)(\\'|\")").getMatch(1);
        }
        if (dllink == null) {
            if (br.containsHTML("Our service is currently unavailable in your country.")) { throw new PluginException(LinkStatus.ERROR_FATAL, "Our service is currently unavailable in your country."); }
            logger.warning("dllink equals null, plugin seems to be broken!");
            if (br.getCookie("http://turbobit.net", "user_isloggedin") == null || "deleted".equalsIgnoreCase(br.getCookie("http://turbobit.net", "user_isloggedin"))) {
                synchronized (LOCK) {
                    account.setProperty("UA", null);
                    account.setProperty("cookies", null);
                }
                throw new PluginException(LinkStatus.ERROR_RETRY);
            }
            throw new PluginException(LinkStatus.ERROR_PLUGIN_DEFECT);
        }
        if (!dllink.contains("turbobit.net")) {
            dllink = MAINPAGE + dllink;
        }
        br.setFollowRedirects(true);
        dl = jd.plugins.BrowserAdapter.openDownload(br, link, dllink, true, 0);
        if (dl.getConnection().getContentType().contains("html")) {
            if (dl.getConnection().getResponseCode() == 403) {
                try {
                    dl.getConnection().disconnect();
                } catch (final Throwable e) {
                }
                logger.info("No traffic available");
                throw new PluginException(LinkStatus.ERROR_PREMIUM, PluginException.VALUE_ID_PREMIUM_TEMP_DISABLE);
            }
            if (dl.getConnection().getResponseCode() == 404) {
                try {
                    dl.getConnection().disconnect();
                } catch (final Throwable e) {
                }
                logger.info("File is offline");
                throw new PluginException(LinkStatus.ERROR_FILE_NOT_FOUND);
            }
            br.followConnection();
            logger.warning("dllink doesn't seem to be a file...");
            if (br.containsHTML("<h1>404 Not Found</h1>")) { throw new PluginException(LinkStatus.ERROR_TEMPORARILY_UNAVAILABLE, "Server error"); }
            if (br.containsHTML("Try to download it once again after")) { throw new PluginException(LinkStatus.ERROR_TEMPORARILY_UNAVAILABLE, "Server error", 20 * 60 * 1000l); }
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

    private void login(final Account account, final boolean force) throws Exception {
        synchronized (LOCK) {
            // Load cookies
            try {
                setBrowserExclusive();
                String ua = null;
                if (force == false) {
                    /*
                     * we have to reuse old UA, else the cookie will become
                     * invalid
                     */
                    ua = account.getStringProperty("UA", null);
                }
                if (ua == null) {
                    ua = UA;
                }
                br.getHeaders().put("User-Agent", ua);
                br.setCookie(MAINPAGE, "set_user_lang_change", "en");
                br.setCustomCharset("UTF-8");
                br.setFollowRedirects(true);
                final Object ret = account.getProperty("cookies", null);
                boolean acmatch = Encoding.urlEncode(account.getUser()).equals(account.getStringProperty("name", Encoding.urlEncode(account.getUser())));
                if (acmatch) {
                    acmatch = Encoding.urlEncode(account.getPass()).equals(account.getStringProperty("pass", Encoding.urlEncode(account.getPass())));
                }
                if (acmatch && ret != null && ret instanceof HashMap<?, ?> && !force) {
                    final HashMap<String, String> cookies = (HashMap<String, String>) ret;
                    if (account.isValid()) {
                        for (final Map.Entry<String, String> cookieEntry : cookies.entrySet()) {
                            final String key = cookieEntry.getKey();
                            final String value = cookieEntry.getValue();
                            br.setCookie(MAINPAGE, key, value);
                        }
                        return;
                    }
                }
                br.getPage(MAINPAGE);
                br.postPage(MAINPAGE + "/user/login", "user%5Blogin%5D=" + Encoding.urlEncode(account.getUser()) + "&user%5Bpass%5D=" + Encoding.urlEncode(account.getPass()) + "&user%5Bmemory%5D=on&user%5Bsubmit%5D=Login");
                if (!br.containsHTML("yesturbo")) { throw new PluginException(LinkStatus.ERROR_PREMIUM, PluginException.VALUE_ID_PREMIUM_DISABLE); }
                if (br.getCookie(MAINPAGE + "/", "sid") == null) { throw new PluginException(LinkStatus.ERROR_PREMIUM, PluginException.VALUE_ID_PREMIUM_DISABLE); }
                // cookies
                final HashMap<String, String> cookies = new HashMap<String, String>();
                final Cookies add = br.getCookies(br.getHost());
                for (final Cookie c : add.getCookies()) {
                    cookies.put(c.getKey(), c.getValue());
                }
                account.setProperty("name", Encoding.urlEncode(account.getUser()));
                account.setProperty("pass", Encoding.urlEncode(account.getPass()));
                account.setProperty("cookies", cookies);
                account.setProperty("UA", UA);
            } catch (final PluginException e) {
                account.setProperty("UA", null);
                account.setProperty("cookies", null);
                throw e;
            }
        }
    }

    private String parseImageUrl(String fun, final boolean NULL) {
        if (fun == null) { return null; }
        if (!NULL) {
            final String[] next = fun.split(tb(9));
            if (next == null || next.length != 2) {
                fun = rhino(fun, 0);
                if (fun == null) { return null; }
                fun = new Regex(fun, tb(4)).getMatch(0);
                return fun == null ? new Regex(fun, tb(5)).getMatch(0) : rhino(fun, 2);
            }
            return rhino(next[1], 1);
        }
        return new Regex(fun, tb(1)).getMatch(0);
    }

    private void prepareBrowser(final String userAgent) {
        br.getHeaders().put("Pragma", null);
        br.getHeaders().put("Cache-Control", null);
        br.getHeaders().put("Accept-Charset", null);
        br.getHeaders().put("Accept", "text/html, application/xhtml+xml, */*");
        br.getHeaders().put("Accept-Language", "en-EN");
        br.getHeaders().put("User-Agent", userAgent);
        br.getHeaders().put("Referer", null);
    }

    // Also check HitFileNet plugin if this one is broken
    @Override
    public AvailableStatus requestFileInformation(final DownloadLink downloadLink) throws Exception {
        /** Old linkcheck code can be found in rev 16195 */
        correctDownloadLink(downloadLink);
        checkLinks(new DownloadLink[] { downloadLink });
        if (!downloadLink.isAvailabilityStatusChecked()) { return AvailableStatus.UNCHECKED; }
        if (downloadLink.isAvailabilityStatusChecked() && !downloadLink.isAvailable()) { throw new PluginException(LinkStatus.ERROR_FILE_NOT_FOUND); }
        return AvailableStatus.TRUE;
    }

    @Override
    public void reset() {
    }

    @Override
    public void resetDownloadlink(final DownloadLink link) {
    }

    private String rhino(final String s, final int b) {
        Object result = new Object();
        final ScriptEngineManager manager = new ScriptEngineManager();
        final ScriptEngine engine = manager.getEngineByName("javascript");
        try {
            switch (b) {
            case 0:
                engine.eval(s + tb(6));
                result = engine.get(tb(7));
                break;
            case 1:
                result = ((Double) engine.eval(tb(8))).longValue();
                break;
            case 2:
                engine.eval("var out=\"" + s + "\";");
                result = engine.get("out");
                break;
            case 100:
                String[] code = s.split("@");
                engine.eval(code[0] + "var b = 3;var inn = \'" + code[1] + "\';" + code[2]);
                result = engine.get("out");
                break;
            case 666:
                code = s.split("@");
                engine.eval(code[0] + "var b = 1;var inn = \'" + code[1] + "\';" + code[2]);
                result = engine.get("out");
                break;
            case 999:
                code = s.split("@");
                engine.eval("var b = 2;var inn = \'" + code[0] + "\';" + code[1]);
                result = engine.get("out");
                break;
            }
        } catch (final Throwable e) {
            return null;
        }
        return result != null ? result.toString() : null;
    }

    private void setConfigElements() {
        getConfig().addEntry(new ConfigEntry(ConfigContainer.TYPE_CHECKBOX, getPluginConfig(), "JAC", JDL.L("plugins.hoster.turbobit.jac", "Enable JAC?")).setDefaultValue(true));
    }

    private String tb(final int i) {
        final String[] s = new String[12];
        s[0] = "fe8cfbfafa57cde31bc2b798df5141ab2dc171ec0852d89a1a135e3f116c83d15d8bf93a";
        s[1] = "fddbfbfafa57cdef1a90b5cedf5647ae2cc572ec0958dd981e125c68156882d65d82f869";
        s[2] = "fdd9fbf2fb05cde71a97b69edf5742f1289470bb0a5bd9c81a1b5e39116c85805982fc6e880ce26a201651b8ea211874e4232d90c59b6462ac28d2b26f0537385fa6";
        s[3] = "f980f8f7fa0acdb21b91b6cbdf5043fc2ac775ea080fd8c71a4f5d68156586d05982fd3e8b5ae33f244555e8eb201d77e12128cbc1c7";
        s[4] = "f980ffa5fa07cdb01a93b6c8de0642ae299571bb0c0ddb9c1a1b5b6f143d84855ddfff6b8b5de66e254553eeea751d72e17e2d98c19a6760af75d6b46b05";
        s[5] = "f980ffa5f951ceb31ec7b3c8da5246fa2ac770bc0b0fdc9c1e13";
        s[6] = "fc8efbf2fb01c9e61bc2b798df5146f82cc075bf0b5fd8c71a4e5f3e153a8781588ff86f890de26a221050eaee701824e4742d9cc1c66238a973";
        s[7] = "fddefaf6fb07";
        s[8] = "fe8cfbfafa57cde31bc2b798df5146ad29c071b6080edbca1a135f6f156984d75982fc6e8800e338";
        s[9] = "ff88";
        s[10] = "f9def8a1fa02c9b21ac5b5c9da0746ae2ac671be0c0fd99f194e5b69113a85d65c8bf86e8d00e23d254751eded741d72e7262ecdc19c6267af72d2e26b5e326a59a5ce295d28f89e21ae29ea523acfb545fd8adb";
        s[11] = "f980fea5fa0ac9ef1bc7b694de0142f1289075bd0d0ddb9d1b195a6d103d82865cddff69890ae76a251b53efef711d74e07e299bc098";
        /*
         * we have to load the plugin first! we must not reference a plugin
         * class without loading it before
         */
        JDUtilities.getPluginForDecrypt("linkcrypt.ws");
        return JDHexUtils.toString(jd.plugins.decrypter.LnkCrptWs.IMAGEREGEX(s[i]));
    }

}
