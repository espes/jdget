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

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import jd.PluginWrapper;
import jd.config.ConfigContainer;
import jd.config.ConfigEntry;
import jd.config.Property;
import jd.http.Browser;
import jd.http.Cookie;
import jd.http.Cookies;
import jd.http.URLConnectionAdapter;
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
import jd.utils.JDUtilities;
import jd.utils.locale.JDL;

import org.appwork.utils.formatter.SizeFormatter;
import org.appwork.utils.formatter.TimeFormatter;

@HostPlugin(revision = "$Revision$", interfaceVersion = 2, names = { "shareflare.net" }, urls = { "http://(www\\.|u\\d+\\.)?shareflare\\.net/download/[^<>\"/]+/[^<>\"/]+\\.html" }, flags = { 2 })
public class ShareFlareNet extends PluginForHost {

    private static Object        LOCK                              = new Object();
    private static final String  COOKIE_HOST                       = "http://shareflare.net";
    private static AtomicInteger maxFree                           = new AtomicInteger(1);
    private static final String  ENABLEUNLIMITEDSIMULTANMAXFREEDLS = "ENABLEUNLIMITEDSIMULTANMAXFREEDLS";
    private static final String  APIKEY                            = "VjR1U3JGUkNx";
    private static final String  APIPAGE                           = "http://api.letitbit.net/";
    private final String         TEMPDISABLED                      = "(api\\.recaptcha\\.net|google\\.com/recaptcha/api/|>File not found<|id=\"videocaptcha_)";
    private static String        agent                             = null;
    private String               AJAXMAINURL                       = null;
    // Static reCaptcha ID (API key)
    private static final String  RCKEY                             = "NkxjOXpkTVNBQUFBQUYtN3Myd3VRLTAzNnBMUmJNMHA4ZERhUWRBTQ==";
    // If enabled, JD will (try to) force reCaptcha, even if the site requests a video captcha
    private static final boolean FORCERECAPTCHA                    = true;
    // If enabled, JD will retry up to 5 times to get a reCaptcha instead of a video captcha
    private static final boolean TRYTOGETRECAPTCHA                 = true;

    public ShareFlareNet(PluginWrapper wrapper) {
        super(wrapper);
        setConfigElements();
        this.setAccountwithoutUsername(true);
        enablePremium("http://shareflare.net/page/premium.php");
    }

    @Override
    public String getAGBLink() {
        return "http://shareflare.net/page/terms.php";
    }

    @Override
    public void correctDownloadLink(DownloadLink link) throws Exception {
        link.setUrlDownload(link.getDownloadURL().replaceAll("\\?", "%3F"));
    }

    /**
     * Important: Always sync this code with the vip-file.com, shareflare.net and letitbit.net plugins Limits: 20 * 50 = 1000 links per
     * minute
     * */
    @Override
    public boolean checkLinks(final DownloadLink[] urls) {
        if (urls == null || urls.length == 0) { return false; }
        try {
            final Browser br = new Browser();
            prepBrowser(br);
            br.setCookiesExclusive(true);
            final StringBuilder sb = new StringBuilder();
            final ArrayList<DownloadLink> links = new ArrayList<DownloadLink>();
            int index = 0;
            while (true) {
                links.clear();
                while (true) {
                    /*
                     * we test 50 links at once (probably we could check even more)
                     */
                    if (index == urls.length || links.size() > 50) {
                        break;
                    }
                    links.add(urls[index]);
                    index++;
                }
                sb.delete(0, sb.capacity());
                sb.append("r=" + Encoding.urlEncode("[\"" + Encoding.Base64Decode(APIKEY) + "\""));
                for (final DownloadLink dl : links) {
                    sb.append(",[\"download/info\",{\"link\":\"" + dl.getDownloadURL() + "\"}]");
                }
                sb.append("]");
                br.setReadTimeout(2 * 60 * 60);
                br.setConnectTimeout(2 * 60 * 60);
                br.postPage(APIPAGE, sb.toString());
                for (final DownloadLink dllink : links) {
                    final String fid = getFID(dllink);
                    final Regex fInfo = br.getRegex("\"name\":\"([^<>\"]*?)\",\"size\":(\")?(\\d+)(\")?,\"uid\":\"" + fid + "\",\"project\":\"(letitbit\\.net|shareflare\\.net|vip\\-file\\.com)\",\"md5\":\"([a-z0-9]{32}|0)\"");
                    if (br.containsHTML("\"data\":\\[\\[\\]\\]")) {
                        dllink.setAvailable(false);
                    } else {
                        final String md5 = fInfo.getMatch(4);
                        dllink.setFinalFileName(Encoding.htmlDecode(fInfo.getMatch(0)));
                        dllink.setDownloadSize(Long.parseLong(fInfo.getMatch(2)));
                        dllink.setAvailable(true);
                        if (!md5.equals("0")) dllink.setMD5Hash(md5);
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
    public AvailableStatus requestFileInformation(final DownloadLink downloadLink) throws Exception {
        checkLinks(new DownloadLink[] { downloadLink });
        if (!downloadLink.isAvailabilityStatusChecked()) { return AvailableStatus.UNCHECKED; }
        if (downloadLink.isAvailabilityStatusChecked() && !downloadLink.isAvailable()) { throw new PluginException(LinkStatus.ERROR_FILE_NOT_FOUND); }
        return AvailableStatus.TRUE;
    }

    private String getFID(final DownloadLink dl) {
        return new Regex(dl.getDownloadURL(), "/(\\d+\\-)?([^<>\"/]*?)/[^<>\"/]*?\\.html").getMatch(1);
    }

    // private AvailableStatus oldAvailableCheck(final DownloadLink
    // downloadLink) throws IOException, PluginException {
    // br.setFollowRedirects(true);
    // br.getPage(downloadLink.getDownloadURL());
    // br.setFollowRedirects(false);
    // if (br.containsHTML("No htmlCode read")) throw new
    // PluginException(LinkStatus.ERROR_TEMPORARILY_UNAVAILABLE,
    // "Server error");
    // if
    // (br.containsHTML("(File not found|deleted for abuse or something like this|\"http://up\\-file\\.com/find/)"))
    // throw new PluginException(LinkStatus.ERROR_FILE_NOT_FOUND);
    // String filename =
    // br.getRegex("id=\"file-info\">(.*?)<small").getMatch(0);
    // if (filename == null) {
    // filename = br.getRegex("name=\"name\" value=\"(.*?)\"").getMatch(0);
    // if (filename == null) {
    // filename = br.getRegex("name=\"realname\" value=\"(.*?)\"").getMatch(0);
    // }
    // }
    // String filesize =
    // br.getRegex("name=\"sssize\" value=\"(.*?)\"").getMatch(0);
    // if (filename == null) throw new
    // PluginException(LinkStatus.ERROR_PLUGIN_DEFECT);
    // downloadLink.setName(filename.trim());
    // if (filesize != null)
    // downloadLink.setDownloadSize(SizeFormatter.getSize(filesize));
    // return AvailableStatus.TRUE;
    // }

    private String getJson(final String parameter) {
        return br.getRegex("\"" + parameter + "\":\"([^<>\"]*?)\"").getMatch(0);
    }

    public AccountInfo fetchAccountInfo(final Account account) throws Exception {
        synchronized (LOCK) {
            AccountInfo ai = new AccountInfo();
            if (account.getUser() != null && account.getUser().length() > 0) {
                login(account, true);
                br.getHeaders().put("X-Requested-With", "XMLHttpRequest");
                br.postPage("http://shareflare.net/ajax/get_attached_passwords.php", "act=get_attached_passwords");
                final String availableTraffic = br.getRegex("<td>(\\d+\\.\\d+)</td>").getMatch(0);
                final String expire = br.getRegex("<td>(\\d{4}\\-\\d{2}\\-\\d{2})</td>").getMatch(0);
                if (availableTraffic == null || expire == null) {
                    ai.setStatus("This is no premium account!");
                    account.setValid(false);
                    return ai;
                }
                ai.setTrafficLeft(SizeFormatter.getSize(availableTraffic + "GB"));
                ai.setValidUntil(TimeFormatter.getMilliSeconds(expire, "yyyy-MM-dd", null));
                ai.setStatus("Premium User");
            } else {
                // Reset stuff because it can only be checked while downloading
                ai.setValidUntil(-1);
                ai.setTrafficLeft(-1);
                ai.setStatus("Status can only be checked while downloading!");
                account.setValid(true);
            }
            return ai;
        }
    }

    private boolean login(final Account account, final boolean force) throws Exception {
        synchronized (LOCK) {
            // Load cookies
            try {
                this.setBrowserExclusive();
                prepBrowser(br);
                br.setCookie(COOKIE_HOST, "lang", "en");
                final Object ret = account.getProperty("cookies", null);
                boolean acmatch = Encoding.urlEncode(account.getUser()).matches(account.getStringProperty("name", Encoding.urlEncode(account.getUser())));
                if (acmatch) acmatch = Encoding.urlEncode(account.getPass()).matches(account.getStringProperty("pass", Encoding.urlEncode(account.getPass())));
                if (acmatch && ret != null && ret instanceof Map<?, ?> && !force) {
                    final Map<String, String> cookies = (Map<String, String>) ret;
                    if (account.isValid()) {
                        for (final Map.Entry<String, String> cookieEntry : cookies.entrySet()) {
                            final String key = cookieEntry.getKey();
                            final String value = cookieEntry.getValue();
                            this.br.setCookie(COOKIE_HOST, key, value);
                        }
                        return false;
                    }
                }
                /*
                 * we must save the cookies, because shareflare maybe only allows 100 logins per 24hours
                 */
                br.postPage(COOKIE_HOST, "login=" + Encoding.urlEncode(account.getUser()) + "&password=" + Encoding.urlEncode(account.getPass()) + "&act=login");
                String check = br.getCookie(COOKIE_HOST, "log");
                if (check == null) check = br.getCookie(COOKIE_HOST, "pas");
                if (check == null) throw new PluginException(LinkStatus.ERROR_PREMIUM, PluginException.VALUE_ID_PREMIUM_DISABLE);
                final HashMap<String, String> cookies = new HashMap<String, String>();
                final Cookies add = this.br.getCookies(COOKIE_HOST);
                for (final Cookie c : add.getCookies()) {
                    cookies.put(c.getKey(), c.getValue());
                }
                account.setProperty("name", Encoding.urlEncode(account.getUser()));
                account.setProperty("pass", Encoding.urlEncode(account.getPass()));
                account.setProperty("cookies", cookies);
                return true;
            } catch (final PluginException e) {
                account.setProperty("cookies", null);
                throw e;
            }
        }
    }

    @Override
    public int getMaxSimultanFreeDownloadNum() {
        return maxFree.get();
    }

    private String getLinkViaSkymonkDownloadMethod(String s) throws IOException {
        Browser skymonk = new Browser();
        skymonk.setCustomCharset("UTF-8");
        skymonk.getHeaders().put("Pragma", null);
        skymonk.getHeaders().put("Cache-Control", null);
        skymonk.getHeaders().put("Accept-Charset", null);
        skymonk.getHeaders().put("Accept-Encoding", null);
        skymonk.getHeaders().put("Accept", null);
        skymonk.getHeaders().put("Accept-Language", null);
        skymonk.getHeaders().put("User-Agent", null);
        skymonk.getHeaders().put("Referer", null);
        skymonk.getHeaders().put("Content-Type", "application/x-www-form-urlencoded");

        int rd = (int) Math.random() * 6 + 1;
        skymonk.postPage("http://api.letitbit.net/internal/index4.php", "action=LINK_GET_DIRECT&link=" + s + "&free_link=1&sh=" + JDHash.getMD5(String.valueOf(Math.random())) + rd + "&sp=" + (49 + rd) + "&appid=" + JDHash.getMD5(String.valueOf(Math.random())) + "&version=2.12");
        String[] result = skymonk.getRegex("([^\r\n]+)").getColumn(0);
        if (result == null || result.length == 0) return null;

        if ("NO".equals(result[0].trim())) {
            if (result.length > 1) {
                if ("activation".equals(result[1].trim())) {
                    logger.warning("SkyMonk activation not completed!");
                }
            }
        }

        ArrayList<String> res = new ArrayList<String>();
        for (String r : result) {
            if (r.startsWith("http")) {
                res.add(r);
            }
        }
        if (res.size() > 1) return res.get(1);
        return res.size() == 1 ? res.get(0) : null;
    }

    @Override
    public void handleFree(final DownloadLink downloadLink) throws Exception, PluginException {
        requestFileInformation(downloadLink);
        maxFree.set(1);
        if (getPluginConfig().getBooleanProperty(ENABLEUNLIMITEDSIMULTANMAXFREEDLS, false)) maxFree.set(-1);
        String dllink = checkDirectLink(downloadLink, "directlink");
        if (dllink == null) {
            dllink = getLinkViaSkymonkDownloadMethod(downloadLink.getDownloadURL());
            if (dllink == null) {
                dllink = handleFreeFallback(downloadLink);
            } else {
                // Enable unlimited simultan downloads for skymonk users
                maxFree.set(-1);
            }
        }

        dl = jd.plugins.BrowserAdapter.openDownload(br, downloadLink, dllink, true, 1);
        URLConnectionAdapter con = dl.getConnection();
        if (con.getContentType().contains("html") && con.getLongContentLength() < (downloadLink.getDownloadSize() / 2)) {
            downloadLink.setProperty("directlink", Property.NULL);
            logger.warning("the dllink doesn't seem to be a file, following the connection...");
            br.followConnection();
            if (br.containsHTML(">404 Not Found<")) throw new PluginException(LinkStatus.ERROR_TEMPORARILY_UNAVAILABLE, "Server error", 20 * 60 * 1000l);
            if (br.containsHTML("title>Error</title>")) throw new PluginException(LinkStatus.ERROR_TEMPORARILY_UNAVAILABLE, "Server error", 30 * 60 * 1000l);
            throw new PluginException(LinkStatus.ERROR_PLUGIN_DEFECT);
        }
        downloadLink.setProperty("directlink", dllink);
        dl.startDownload();
    }

    private String handleFreeFallback(final DownloadLink downloadLink) throws Exception {
        prepBrowser(br);
        br.setFollowRedirects(true);
        br.getPage(downloadLink.getDownloadURL());
        br.setFollowRedirects(false);
        handleNonApiErrors(downloadLink);
        boolean passed = submitFreeForm();
        if (passed) logger.info("Sent free form #1");
        passed = submitFreeForm();
        if (passed) logger.info("Sent free form #2");
        passed = submitFreeForm();
        if (passed) logger.info("Sent free form #3");

        String urlPrefix = new Regex(br.getURL(), "http://(www\\.)?([a-z0-9]+\\.)shareflare\\.net/.+").getMatch(1);
        if (urlPrefix == null) urlPrefix = "";
        AJAXMAINURL = "http://" + urlPrefix + "shareflare.net";

        /** Captcha START */
        final Browser ajaxBR = br.cloneBrowser();
        prepareBrowser(ajaxBR);
        String dlFunction = null;
        String ajaxPostpage = null;
        final long timeBefore = System.currentTimeMillis();
        // TODO: Maybe find a way to force the old captchas
        // if (true) {
        // ajaxPostpage = "http://shareflare.net//ajax/check_captcha.php";
        // for (int i = 0; i <= 5; i++) {
        // final String c = getCaptchaCode("http://shareflare.net/captcha_new.php?rand=" + new Random().nextInt(100000), downloadLink);
        // if (i == 0) {
        // waitTime(ajaxBR, timeBefore, downloadLink);
        // }
        // ajaxBR.postPage(ajaxPostpage, "code=" + c);
        // if (ajaxBR.toString().length() < 2 || ajaxBR.toString().contains("error_wrong_captcha")) {
        // continue;
        // }
        // break;
        // }
        // if (ajaxBR.toString().contains("error_wrong_captcha")) throw new PluginException(LinkStatus.ERROR_CAPTCHA);
        // }
        if (br.containsHTML("(api\\.recaptcha\\.net|google\\.com/recaptcha/api/)") || FORCERECAPTCHA) {
            dlFunction = br.getRegex("function getLink\\(\\)(.*?)(function|</script>)").getMatch(0);
            String rcID = br.getRegex("challenge\\?k=([^<>\"]*?)\"").getMatch(0);
            if (rcID == null) rcID = Encoding.Base64Decode(ShareFlareNet.RCKEY);
            if (dlFunction == null || rcID == null) throw new PluginException(LinkStatus.ERROR_PLUGIN_DEFECT);
            final String rcControl = new Regex(dlFunction, "var recaptcha_control_field = \\'([^<>\"]*?)\\'").getMatch(0);
            ajaxPostpage = new Regex(dlFunction, "\\$\\.post\\(\"(/ajax/[^<>\"]*?)\"").getMatch(0);
            if (ajaxPostpage == null || rcControl == null) throw new PluginException(LinkStatus.ERROR_PLUGIN_DEFECT);
            ajaxPostpage = AJAXMAINURL + ajaxPostpage;
            final PluginForHost recplug = JDUtilities.getPluginForHost("DirectHTTP");
            jd.plugins.hoster.DirectHTTP.Recaptcha rc = ((DirectHTTP) recplug).getReCaptcha(br);
            rc.setId(rcID);
            rc.load();
            for (int i = 0; i <= 5; i++) {
                final File cf = rc.downloadCaptcha(getLocalCaptchaFile());
                final String c = getCaptchaCode(cf, downloadLink);
                if (i == 0) {
                    waitTime(ajaxBR, timeBefore, downloadLink);
                }
                ajaxBR.postPage(ajaxPostpage, "recaptcha_challenge_field=" + rc.getChallenge() + "&recaptcha_response_field=" + c + "&recaptcha_control_field=" + Encoding.urlEncode(rcControl));
                if (ajaxBR.toString().length() < 2 || ajaxBR.toString().contains("error_wrong_captcha")) {
                    rc.reload();
                    continue;
                }
                break;
            }
            if (ajaxBR.toString().length() < 2 || ajaxBR.toString().contains("error_wrong_captcha")) throw new PluginException(LinkStatus.ERROR_CAPTCHA);

        } else if (br.containsHTML("vc = new videoCaptcha\\(\\$\\(\\'#captchav\\'\\)")) {
            if (ShareFlareNet.TRYTOGETRECAPTCHA) {
                // Video captcha isn't supported yet
                int timesFailed = downloadLink.getIntegerProperty("timesfailedshareflare", 0);
                if (timesFailed <= 4) {
                    timesFailed++;
                    downloadLink.setProperty("timesfailedshareflare", timesFailed);
                    throw new PluginException(LinkStatus.ERROR_RETRY, "Trying to get reCaptcha");
                } else {
                    downloadLink.setProperty("timesfailedshareflare", Property.NULL);
                    throw new PluginException(LinkStatus.ERROR_HOSTER_TEMPORARILY_UNAVAILABLE, "Unsupported captcha-type", 30 * 60 * 1000l);
                }
            } else {
                throw new PluginException(LinkStatus.ERROR_HOSTER_TEMPORARILY_UNAVAILABLE, "Unsupported captcha-type", 30 * 60 * 1000l);
            }
            // dlFunction = br.getRegex("function getLinkV\\(\\)(.*?)(function|</script>)").getMatch(0);
            // if (dlFunction == null) throw new PluginException(LinkStatus.ERROR_PLUGIN_DEFECT);
            // ajaxPostpage = new Regex(dlFunction, "\\$\\.post\\(\"(/ajax/[^<>\"]*?)\"").getMatch(0);
            // final String vcControl = new Regex(dlFunction, "var vcaptcha_control_field = \\'([^<>\"]*?)\\'").getMatch(0);
            // if (ajaxPostpage == null || vcControl == null) throw new PluginException(LinkStatus.ERROR_PLUGIN_DEFECT);
            // ajaxPostpage = AJAXMAINURL + ajaxPostpage;
            // final String r = br.getRegex("castom_verificator:getLinkV,key:\\'([a-z0-9]{32})\\'\\}").getMatch(0);
            // if (r == null) throw new PluginException(LinkStatus.ERROR_PLUGIN_DEFECT);
            // ajaxBR.getPage("http://videocaptcha.letitbit.net/stat?callback=jQuery" + System.currentTimeMillis() + "_" +
            // System.currentTimeMillis() + "&r=%5B%22" + r + "%22%2C%5B%5B%22captchaCreated%22%2C%7B%22key%22%3A%22%22%7D%5D%5D%5D&_=" +
            // System.currentTimeMillis());
            // if (!ajaxBR.containsHTML("\"status\":\"ok\"")) throw new PluginException(LinkStatus.ERROR_PLUGIN_DEFECT);
            // for (int i = 0; i <= 5; i++) {
            // ajaxBR.getPage("http://videocaptcha.letitbit.net/data?callback=jQuery" + System.currentTimeMillis() + "_" +
            // System.currentTimeMillis() + "&r=%5B%22" + r + "%22%2C%5B%5B%22get_code%22%5D%5D%5D&_=" + System.currentTimeMillis());
            // // We have different formats here
            // String videoURL = ajaxBR.getRegex("\"video\\\\/webm\":\"(http:[^<>\"]*?)\"").getMatch(0);
            // if (videoURL == null) throw new PluginException(LinkStatus.ERROR_PLUGIN_DEFECT);
            // videoURL = videoURL.replace("\\", "");
            // ajaxBR.postPage(ajaxPostpage, "videocapcha_token=&videocapcha_val=" + "RESULT_FROM_USER" + "&vcaptcha_control_field=" +
            // Encoding.urlEncode(vcControl) + "&videocapcha_skey=");
            // if (ajaxBR.toString().contains("error_wrong_captcha")) continue;
            // break;
            // }
            // if (ajaxBR.toString().contains("error_wrong_captcha")) throw new PluginException(LinkStatus.ERROR_CAPTCHA);
        } else {
            throw new PluginException(LinkStatus.ERROR_PLUGIN_DEFECT, "Unsupported captchatype");
        }
        /** Captcha END */

        // Downloadlimit is per day so let's just wait 3 hours
        if (ajaxBR.containsHTML("error_free_download_blocked")) throw new PluginException(LinkStatus.ERROR_IP_BLOCKED, 3 * 60 * 60 * 1000l);
        if (ajaxBR.containsHTML("callback_file_unavailable")) throw new PluginException(LinkStatus.ERROR_TEMPORARILY_UNAVAILABLE, "ServerError", 30 * 60 * 1000l);
        LinkedList<String> finallinksx = new LinkedList<String>();
        String[] finallinks = ajaxBR.getRegex("\"(http:[^<>\"]*?)\"").getColumn(0);
        // More comon for shareflare.net
        if ((finallinks == null || finallinks.length == 0) && ajaxBR.toString().length() < 500) finallinks = ajaxBR.getRegex("(http:[^<>\"].+)").getColumn(0);
        if (finallinks == null || finallinks.length == 0) { throw new PluginException(LinkStatus.ERROR_PLUGIN_DEFECT); }
        for (final String finallink : finallinks) {
            if (!finallinksx.contains(finallink) && finallink.startsWith("http")) finallinksx.add(finallink);
        }
        // Grab last links, this might changes and has to be fixed if users get
        // "server error" in JD while it's working via browser. If this is
        // changed often we should consider trying the whole list of finallinks.
        final String url = finallinksx.peekLast();
        if (url == null || url.length() > 1000 || !url.startsWith("http")) {
            if (ajaxBR.containsHTML("error_free_download_blocked")) { throw new PluginException(LinkStatus.ERROR_IP_BLOCKED, "Free download blocked", 2 * 60 * 60 * 1000l); }
            logger.warning("url couldn't be found!");
            logger.severe(url);
            logger.severe(ajaxBR.toString());
            throw new PluginException(LinkStatus.ERROR_PLUGIN_DEFECT);
        }
        /* we have to wait little because server too buggy */
        sleep(2000, downloadLink);
        /* remove newline */
        return url.replaceAll("%0D%0A", "").trim().replace("\\", "");
    }

    private boolean submitFreeForm() throws Exception {
        // this finds the form to "click" on the next "free download" button
        Form[] allforms = br.getForms();
        if (allforms == null || allforms.length == 0) return false;
        Form down = null;
        for (Form singleform : allforms) {
            if (singleform.containsHTML("md5crypt") && singleform.getAction() != null && !singleform.containsHTML("/sms/check")) {
                down = singleform;
                break;
            }
        }
        if (down == null) return false;
        br.submitForm(down);
        return true;
    }

    @Override
    public void handlePremium(final DownloadLink downloadLink, final Account account) throws Exception {
        String dlUrl = null;
        if (account.getUser() == null || account.getUser().trim().length() == 0) {
            prepBrowser(br);
            br.postPage(APIPAGE, "r=[\"" + Encoding.Base64Decode(APIKEY) + "\",[\"download/direct_links\",{\"link\":\"" + downloadLink.getDownloadURL() + "\",\"pass\":\"" + account.getPass() + "\"}]]");
            if (br.containsHTML("data\":\"bad password\"")) {
                logger.info("Wrong password, disabling the account!");
                throw new PluginException(LinkStatus.ERROR_PREMIUM, PluginException.VALUE_ID_PREMIUM_DISABLE);
            }
            if (br.containsHTML("\"data\":\"no mirrors\"")) throw new PluginException(LinkStatus.ERROR_FILE_NOT_FOUND);
            if (br.containsHTML("\"data\":\"file is not found\"")) throw new PluginException(LinkStatus.ERROR_FILE_NOT_FOUND);
            dlUrl = br.getRegex("\"(http:[^<>\"]*?)\"").getMatch(0);
            if (dlUrl != null)
                dlUrl = dlUrl.replace("\\", "");
            else
                dlUrl = handleOldPremiumPassWay(account, downloadLink);

        } else {
            login(account, false);
            br.setFollowRedirects(true);
            br.getPage(downloadLink.getDownloadURL());
            handleNonApiErrors(downloadLink);
            dlUrl = getPremiumDllink();
        }
        dl = jd.plugins.BrowserAdapter.openDownload(br, downloadLink, dlUrl, true, 1);
        if (dl.getConnection().getContentType().contains("html")) {
            br.followConnection();
            throw new PluginException(LinkStatus.ERROR_PLUGIN_DEFECT);
        }
        dl.startDownload();
    }

    // NOTE: Old, tested 15.11.12, works!
    private String handleOldPremiumPassWay(final Account account, final DownloadLink downloadLink) throws Exception {
        br.setFollowRedirects(true);
        prepBrowser(br);
        br.getPage(downloadLink.getDownloadURL());
        br.setFollowRedirects(false);
        handleNonApiErrors(downloadLink);
        Form premForm = null;
        Form allForms[] = br.getForms();
        if (allForms == null || allForms.length == 0) throw new PluginException(LinkStatus.ERROR_PLUGIN_DEFECT);
        for (Form aForm : allForms) {
            if (aForm.containsHTML("\"pass\"")) {
                premForm = aForm;
                break;
            }
        }
        if (premForm == null) throw new PluginException(LinkStatus.ERROR_PLUGIN_DEFECT);
        premForm.put("pass", Encoding.urlEncode(account.getPass()));
        br.submitForm(premForm);
        if (br.containsHTML("<b>Given password does not exist")) {
            logger.info("Downloadpassword seems to be wrong, disabeling account now!");
            throw new PluginException(LinkStatus.ERROR_PREMIUM, PluginException.VALUE_ID_PREMIUM_DISABLE);
        }
        /** 1 point = 1 GB */
        String points = br.getRegex(">Points:</span>([0-9\\.]+)\\&nbsp;").getMatch(0);
        if (points == null) points = br.getRegex("<p>You have: ([0-9\\.]+) Points</p>").getMatch(0);
        if (points != null) {
            AccountInfo ai = account.getAccountInfo();
            if (ai == null) {
                ai = new AccountInfo();
                account.setAccountInfo(ai);
            }
            ai.setTrafficLeft(SizeFormatter.getSize(points + "GB"));
        }
        if (br.containsHTML("(>The file is temporarily unavailable for download|Please try a little bit later\\.<)")) throw new PluginException(LinkStatus.ERROR_TEMPORARILY_UNAVAILABLE, "Servererror", 60 * 60 * 1000l);
        String dlUrl = getPremiumDllink();
        if (dlUrl == null) {
            if (br.containsHTML("The premium key you provided does not exist")) {
                logger.info("The premium key you provided does not exist");
                throw new PluginException(LinkStatus.ERROR_PREMIUM, PluginException.VALUE_ID_PREMIUM_DISABLE);
            }
            throw new PluginException(LinkStatus.ERROR_PLUGIN_DEFECT);
        }
        return dlUrl;
    }

    private String getPremiumDllink() {
        String finallink = null;
        final String allLinks = br.getRegex("var direct_links = \\{(.*?)\\};").getMatch(0);
        if (allLinks != null) {
            final String[] theLinks = new Regex(allLinks, "\"(http[^<>\"]*?)\"").getColumn(0);
            if (theLinks != null && theLinks.length != 0) finallink = theLinks[theLinks.length - 1];
        }
        return finallink;
    }

    private void handleNonApiErrors(final DownloadLink dl) throws IOException, PluginException {
        if (br.containsHTML(TEMPDISABLED) && !br.containsHTML(">For logging in, your E-mail will be used as your username\\. The password will be sent to your E\\-mail\\.</p>[\t\n\r ]+<script type=\"text/javascript\" src=\"http://www\\.google\\.com/recaptcha")) {
            br.postPage(APIPAGE, "r=" + Encoding.urlEncode("[\"" + Encoding.Base64Decode(APIKEY) + "\",[\"download/check_link\",{\"link\":\"" + dl.getDownloadURL() + "\"}]]"));
            final String availableMirrors = br.getRegex("\"data\":\\[(\\d+)\\]").getMatch(0);
            if (availableMirrors != null) {
                if (availableMirrors.equals("0")) {
                    throw new PluginException(LinkStatus.ERROR_FILE_NOT_FOUND);
                } else {
                    throw new PluginException(LinkStatus.ERROR_TEMPORARILY_UNAVAILABLE, "Temporarily not downloadable, " + availableMirrors + " mirrors remaining!");
                }
            }
        }
    }

    private void waitTime(final Browser br, long timeBefore, final DownloadLink downloadLink) throws PluginException, IOException {
        int passedTime = (int) ((System.currentTimeMillis() - timeBefore) / 1000) - 1;
        /** Ticket Time */
        int wait = 60;
        String waittime = br.getRegex("id=\"seconds\" style=\"font\\-size:18px\">(\\d+)</span>").getMatch(0);
        if (waittime == null) waittime = br.getRegex("seconds = (\\d+)").getMatch(0);
        if (waittime != null) {
            logger.info("Waittime found, waittime is " + waittime + " seconds .");
            wait = Integer.parseInt(waittime);
        } else {
            logger.info("No waittime found, continuing with default waittime...");
        }
        wait -= passedTime;
        logger.info("Waittime detected, waiting " + passedTime + " - " + passedTime + " seconds from now on...");
        sleep(wait * 1001l, downloadLink);
        /*
         * this causes issues in 09580 stable, no workaround known, please update to latest jd version
         */
        br.getHeaders().put("Content-Length", "0");
        br.postPage(AJAXMAINURL + "/ajax/download3.php", "");
        br.getHeaders().remove("Content-Length");
        /* we need to remove the newline in old browser */
        final String resp = br.toString().replaceAll("%0D%0A", "").trim();
        if (!"1".equals(resp)) {
            if (br.containsHTML("No htmlCode read")) throw new PluginException(LinkStatus.ERROR_IP_BLOCKED, "Daily limit reached", 60 * 60 * 1000l);
            throw new PluginException(LinkStatus.ERROR_PLUGIN_DEFECT);
        }
    }

    private String checkDirectLink(final DownloadLink downloadLink, final String property) {
        String dllink = downloadLink.getStringProperty(property);
        if (dllink != null) {
            try {
                final Browser br2 = br.cloneBrowser();
                URLConnectionAdapter con = br2.openGetConnection(dllink);
                if (con.getContentType().contains("html") || con.getLongContentLength() == -1) {
                    downloadLink.setProperty(property, Property.NULL);
                    dllink = null;
                }
                con.disconnect();
            } catch (Exception e) {
                downloadLink.setProperty(property, Property.NULL);
                dllink = null;
            }
        }
        return dllink;
    }

    private void prepareBrowser(final Browser br) {
        /*
         * last time they did not block the useragent, we just need this stuff below ;)
         */
        if (br == null) { return; }
        br.getHeaders().put("Accept", "*/*");
        br.getHeaders().put("Pragma", "no-cache");
        br.getHeaders().put("Cache-Control", "no-cache");
        br.getHeaders().put("X-Requested-With", "XMLHttpRequest");
        br.getHeaders().put("Content-Length", "0");
    }

    private Browser prepBrowser(Browser prepBr) {
        // define custom browser headers and language settings.
        if (prepBr == null) prepBr = new Browser();
        if (agent == null) {
            /* we first have to load the plugin, before we can reference it */
            JDUtilities.getPluginForHost("mediafire.com");
            agent = jd.plugins.hoster.MediafireCom.stringUserAgent();
        }
        prepBr.getHeaders().put("User-Agent", agent);
        prepBr.getHeaders().put("Accept-Language", "en-gb, en;q=0.9");
        prepBr.setCustomCharset("UTF-8");
        prepBr.setCookie(COOKIE_HOST, "lang", "en");
        return prepBr;
    }

    // do not add @Override here to keep 0.* compatibility
    public boolean hasCaptcha() {
        return true;
    }

    @Override
    public void reset() {
    }

    @Override
    public void resetDownloadlink(DownloadLink link) {
    }

    private void setConfigElements() {
        getConfig().addEntry(new ConfigEntry(ConfigContainer.TYPE_CHECKBOX, getPluginConfig(), ShareFlareNet.ENABLEUNLIMITEDSIMULTANMAXFREEDLS, JDL.L("plugins.hoster.shareflarenet.enableunlimitedsimultanfreedls", "Enable unlimited (20) max simultanious free downloads for non-skymonk mode (can cause problems, use at your own risc)")).setDefaultValue(false));
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