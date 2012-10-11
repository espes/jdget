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

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;

import jd.PluginWrapper;
import jd.config.ConfigContainer;
import jd.config.ConfigEntry;
import jd.gui.UserIO;
import jd.http.Browser;
import jd.http.Cookie;
import jd.http.Cookies;
import jd.http.RandomUserAgent;
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

@HostPlugin(revision = "$Revision$", interfaceVersion = 2, names = { "shareflare.net" }, urls = { "http://(www\\.)?shareflare\\.net/download/.*?/.*?\\.html" }, flags = { 2 })
public class ShareFlareNet extends PluginForHost {

    private static final String  FREEDOWNLOADPOSSIBLE              = "download4";
    private static Object        LOCK                              = new Object();
    private static final String  FREELIMIT                         = ">Your limit for free downloads is over for today<";
    private static final String  COOKIE_HOST                       = "http://shareflare.net";
    private static AtomicInteger maxFree                           = new AtomicInteger(1);
    private static final String  ENABLEUNLIMITEDSIMULTANMAXFREEDLS = "ENABLEUNLIMITEDSIMULTANMAXFREEDLS";

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

    @Override
    public AvailableStatus requestFileInformation(DownloadLink link) throws IOException, PluginException {
        this.setBrowserExclusive();
        br.setCustomCharset("utf-8");
        br.getHeaders().put("User-Agent", RandomUserAgent.generate());
        br.setCookie("http://shareflare.net", "lang", "en");
        br.getPage(link.getDownloadURL());
        if (br.containsHTML("No htmlCode read")) throw new PluginException(LinkStatus.ERROR_TEMPORARILY_UNAVAILABLE, "Server error");
        if (br.containsHTML("(File not found|deleted for abuse or something like this|\"http://up\\-file\\.com/find/)")) throw new PluginException(LinkStatus.ERROR_FILE_NOT_FOUND);
        String filename = br.getRegex("id=\"file-info\">(.*?)<small").getMatch(0);
        if (filename == null) {
            filename = br.getRegex("name=\"name\" value=\"(.*?)\"").getMatch(0);
            if (filename == null) {
                filename = br.getRegex("name=\"realname\" value=\"(.*?)\"").getMatch(0);
            }
        }
        String filesize = br.getRegex("name=\"sssize\" value=\"(.*?)\"").getMatch(0);
        if (filename == null) throw new PluginException(LinkStatus.ERROR_PLUGIN_DEFECT);
        link.setName(filename.trim());
        if (filesize != null) link.setDownloadSize(SizeFormatter.getSize(filesize));
        return AvailableStatus.TRUE;
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
                br.setCustomCharset("UTF-8");
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
                 * we must save the cookies, because shareflare maybe only
                 * allows 100 logins per 24hours
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

    private String getDllink(final Browser br) {
        String dllink = br.getRegex("\"(http:[^<>\"]*?)\"").getMatch(0);
        if (dllink == null) {
            dllink = br.toString().trim();
            if (!dllink.startsWith("http") || dllink.length() > 500) return null;
        }
        return dllink;
    }

    @Override
    public int getMaxSimultanFreeDownloadNum() {
        return maxFree.get();
    }

    private boolean validateEmail(String email) {
        return new Regex(email, ".+@.+\\.[a-z]+").matches();
    }

    private String getLinkViaSkymonkDownloadMethod(String s) throws IOException {
        String appId = getPluginConfig().getStringProperty("APPID", null);
        boolean validate = getPluginConfig().getBooleanProperty("APPIDVALIDATE", false);

        if (!validate || !getPluginConfig().getBooleanProperty("STATUS", false)) return null;
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
        skymonk.postPage("http://api.letitbit.net/internal/index4.php", "action=LINK_GET_DIRECT&link=" + s + "&free_link=1&sh=" + JDHash.getMD5(String.valueOf(Math.random())) + rd + "&sp=" + (49 + rd) + "&appid=" + appId + "&version=2.0");
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
    public void handleFree(DownloadLink downloadLink) throws Exception, PluginException {
        requestFileInformation(downloadLink);
        maxFree.set(1);
        if (getPluginConfig().getBooleanProperty(ENABLEUNLIMITEDSIMULTANMAXFREEDLS, false)) maxFree.set(-1);
        String dllink = getLinkViaSkymonkDownloadMethod(downloadLink.getDownloadURL());
        if (dllink == null) {
            dllink = handleFreeFallback(downloadLink);
        } else {
            // Enable unlimited simultan downloads for skymonk users
            maxFree.set(-1);
        }

        dl = jd.plugins.BrowserAdapter.openDownload(br, downloadLink, dllink, true, 1);
        URLConnectionAdapter con = dl.getConnection();
        if (con.getContentType().contains("html") && con.getLongContentLength() < (downloadLink.getDownloadSize() / 2)) {
            logger.warning("the dllink doesn't seem to be a file, following the connection...");
            br.followConnection();
            if (br.containsHTML(">404 Not Found<")) throw new PluginException(LinkStatus.ERROR_TEMPORARILY_UNAVAILABLE, "Server error", 20 * 60 * 1000l);
            if (br.containsHTML("title>Error</title>")) throw new PluginException(LinkStatus.ERROR_TEMPORARILY_UNAVAILABLE, "Server error", 30 * 60 * 1000l);
            throw new PluginException(LinkStatus.ERROR_PLUGIN_DEFECT);
        }
        dl.startDownload();
    }

    private String handleFreeFallback(final DownloadLink downloadLink) throws Exception {
        final Browser ajaxBR = br.cloneBrowser();
        ajaxBR.getHeaders().put("X-Requested-With", "XMLHttpRequest");
        String reconnectWaittime = br.getRegex("You can wait download for ([\t\n\r0-9]+) minutes or upgrade to premium").getMatch(0);
        if (reconnectWaittime != null) throw new PluginException(LinkStatus.ERROR_IP_BLOCKED, Integer.parseInt(reconnectWaittime.trim()) * 60 * 1001l);
        if (br.containsHTML("You reached your hourly traffic limit\\.")) throw new PluginException(LinkStatus.ERROR_IP_BLOCKED, 30 * 60 * 1001l);
        if (br.containsHTML(FREELIMIT)) throw new PluginException(LinkStatus.ERROR_IP_BLOCKED, 60 * 60 * 1000l);
        if (br.containsHTML("(В бесплатном режиме вы можете скачивать только один файл|You are currently downloading|Free users are allowed to only one parallel download\\.\\.)")) throw new PluginException(LinkStatus.ERROR_IP_BLOCKED);
        br.setFollowRedirects(false);
        boolean passed = submitFreeForm();
        if (passed) logger.info("Sent free form #1");
        passed = submitFreeForm();
        if (passed) logger.info("Sent free form #2");
        passed = submitFreeForm();
        if (passed) logger.info("Sent free form #3");

        final String dlFunction = br.getRegex("function getLink\\(\\)(.*?)</script>").getMatch(0);
        if (dlFunction == null) throw new PluginException(LinkStatus.ERROR_PLUGIN_DEFECT);
        String ajaxPostpage = new Regex(dlFunction, "\\$\\.post\\(\"(/ajax/[^<>\"]*?)\"").getMatch(0);
        if (ajaxPostpage == null) throw new PluginException(LinkStatus.ERROR_PLUGIN_DEFECT);
        ajaxPostpage = "http://shareflare.net" + ajaxPostpage;
        int wait = 60;
        String waittime = br.getRegex("id=\"seconds\" style=\"font\\-size:18px\">(\\d+)</span>").getMatch(0);
        if (waittime == null) waittime = br.getRegex("seconds = (\\d+)").getMatch(0);
        if (waittime != null) {
            logger.info("Waittime found, waittime is " + waittime + " seconds .");
            wait = Integer.parseInt(waittime);
        } else {
            logger.info("No waittime found, continuing...");
        }
        sleep((wait + 1) * 1001l, downloadLink);
        /*
         * this causes issues in 09580 stable, no workaround known, please
         * update to latest jd version
         */
        ajaxBR.getHeaders().put("Content-Length", "0");
        ajaxBR.postPage("http://shareflare.net/ajax/download3.php", "");
        ajaxBR.getHeaders().remove("Content-Length");
        /* we need to remove the newline in old browser */
        final String resp = ajaxBR.toString().replaceAll("%0D%0A", "").trim();
        if (!"1".equals(resp)) {
            if (ajaxBR.containsHTML("No htmlCode read")) throw new PluginException(LinkStatus.ERROR_IP_BLOCKED, "Daily limit reached", 60 * 60 * 1000l);
            throw new PluginException(LinkStatus.ERROR_PLUGIN_DEFECT);
        }

        if (ajaxPostpage.contains("recaptcha")) {
            final String rcID = br.getRegex("challenge\\?k=([^<>\"]*?)\"").getMatch(0);
            if (rcID == null) throw new PluginException(LinkStatus.ERROR_PLUGIN_DEFECT);
            final PluginForHost recplug = JDUtilities.getPluginForHost("DirectHTTP");
            final jd.plugins.hoster.DirectHTTP.Recaptcha rc = ((DirectHTTP) recplug).getReCaptcha(br);
            rc.setId(rcID);
            rc.load();
            for (int i = 0; i <= 3; i++) {
                File cf = rc.downloadCaptcha(getLocalCaptchaFile());
                final String c = getCaptchaCode(cf, downloadLink);
                ajaxBR.postPage("http://shareflare.net/ajax/check_recaptcha.php", "recaptcha_challenge_field=" + rc.getChallenge() + "&recaptcha_response_field=" + c);
                if (ajaxBR.containsHTML("error_wrong_captcha")) {
                    rc.reload();
                    continue;
                }
                break;
            }
            if (ajaxBR.containsHTML("error_wrong_captcha")) throw new PluginException(LinkStatus.ERROR_CAPTCHA);
        } else {
            // Normal captcha handling, UNTESTED!
            final DecimalFormat df = new DecimalFormat("0000");
            for (int i = 0; i <= 5; i++) {
                String code = getCaptchaCode("http://shareflare.net/captcha_new.php?rand=" + df.format(new Random().nextInt(1000)), downloadLink);
                sleep(2000, downloadLink);
                ajaxBR.postPage(ajaxPostpage, "code=" + Encoding.urlEncode(code));
                if (ajaxBR.toString().contains("error_wrong_captcha")) continue;
                break;
            }
            if (ajaxBR.toString().contains("error_wrong_captcha")) throw new PluginException(LinkStatus.ERROR_CAPTCHA);
        }
        String dllink = getDllink(ajaxBR);
        if (ajaxBR.containsHTML("error_free_download_blocked")) throw new PluginException(LinkStatus.ERROR_IP_BLOCKED);
        if (dllink == null) return null;
        return dllink.replace("\\", "");
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
        String url = null;
        if (account.getUser() != null && account.getUser().length() > 0) {
            login(account, false);
            br.setFollowRedirects(true);
            br.getPage(downloadLink.getDownloadURL());
            url = getPremiumDllink();
        } else {
            requestFileInformation(downloadLink);
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
            url = getPremiumDllink();
            if (url == null) {
                if (br.containsHTML("The premium key you provided does not exist")) {
                    logger.info("The premium key you provided does not exist");
                    throw new PluginException(LinkStatus.ERROR_PREMIUM, PluginException.VALUE_ID_PREMIUM_DISABLE);
                }
                throw new PluginException(LinkStatus.ERROR_PLUGIN_DEFECT);
            }
        }
        dl = jd.plugins.BrowserAdapter.openDownload(br, downloadLink, url, true, 1);
        if (dl.getConnection().getContentType().contains("html")) {
            br.followConnection();
            throw new PluginException(LinkStatus.ERROR_PLUGIN_DEFECT);
        }
        dl.startDownload();
    }

    private String getPremiumDllink() {
        String url = br.getRegex("class=\"btn\\-corner\\-tl\"><a style=\\'font\\-size: 16px\\' href=\\'(http://[^<>\"\\']*?)\\'").getMatch(0);
        if (url == null) url = br.getRegex("Link to the file download\" href=\"(http://[^<>\"\\']*?)\"").getMatch(0);
        return url;
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
        getConfig().addEntry(new ConfigEntry(ConfigContainer.TYPE_LABEL, "The SkyMonk method without waittime and captcha needs an activation!"));
        getConfig().addEntry(new ConfigEntry(ConfigContainer.TYPE_SEPARATOR));
        getConfig().addEntry(new ConfigEntry(ConfigContainer.TYPE_LABEL, "IMPORTANT note!"));
        getConfig().addEntry(new ConfigEntry(ConfigContainer.TYPE_LABEL, "JDownloader only uses the download technique which skymonk uses, the programm \"skymonk\" is NOT required for JDownloader to use this method!"));
        getConfig().addEntry(new ConfigEntry(ConfigContainer.TYPE_LABEL, "Just enter a mailadress, click on activate and wait for the confirmation window, that's all!"));
        getConfig().addEntry(new ConfigEntry(ConfigContainer.TYPE_SEPARATOR));
        getConfig().addEntry(new ConfigEntry(ConfigContainer.TYPE_CHECKBOX, getPluginConfig(), "STATUS", JDL.L("plugins.hoster.shareflare.status", "Use SkyMonk?")).setDefaultValue(false));
        getConfig().addEntry(new ConfigEntry(ConfigContainer.TYPE_SEPARATOR));
        final ConfigEntry configEntry;
        getConfig().addEntry(configEntry = new ConfigEntry(ConfigContainer.TYPE_TEXTFIELD, getPluginConfig(), "SKYMONKEMAIL", JDL.L("plugins.hoster.shareflare.email", "E-Mail:")));
        getConfig().addEntry(new ConfigEntry(ConfigContainer.TYPE_SEPARATOR));
        getConfig().addEntry(new ConfigEntry(ConfigContainer.TYPE_BUTTON, new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                new Thread() {
                    public void run() {
                        String email = getPluginConfig().getStringProperty("SKYMONKEMAIL", null);
                        try {
                            jd.config.GuiConfigListener listener = configEntry.getGuiListener();
                            if (listener != null) {
                                email = (String) listener.getText();
                            }
                        } catch (Throwable e2) {
                            /* does not exist in 09581 */
                        }
                        String emailChanged = getPluginConfig().getStringProperty("SKYMONKEMAILCHANGED", null);
                        if (!email.equalsIgnoreCase(emailChanged)) {
                            getPluginConfig().setProperty("APPID", null);
                            getPluginConfig().setProperty("SKYMONKVALIDATE", null);
                            getPluginConfig().setProperty("APPIDVALIDATE", false);
                        }
                        String appId = getPluginConfig().getStringProperty("APPID", null);
                        appId = appId == null ? JDHash.getMD5(String.valueOf(Math.random())) : appId;
                        boolean validate = getPluginConfig().getBooleanProperty("SKYMONKVALIDATE", false);

                        if (email == null || email.length() == 0) {
                            UserIO.getInstance().requestMessageDialog("E-Mail is empty!");
                            return;
                        }
                        if (!validateEmail(email)) {
                            logger.warning("E-Mail is no valid --> " + email);
                            UserIO.getInstance().requestMessageDialog("E-Mail is not valid!");
                            return;
                        }
                        if (!validate) {
                            Browser skymonk = new Browser();
                            skymonk.setCookie("http://shareflare.net/", "lang", "en");
                            skymonk.setCustomCharset("UTF-8");
                            try {
                                skymonk.postPage("http://skymonk.net/?page=activate", "act=get_activation_key&phone=+49" + String.valueOf((int) (Math.random() * (999999999 - 1111111111) + 1111111111)) + "&email=" + email + "&app_id=" + appId + "&app_version=2");
                            } catch (Throwable e1) {
                            }
                            String msg = skymonk.getRegex("content:\'(.*?)\'").getMatch(0);
                            if (skymonk.containsHTML("status:\'error\'")) {
                                msg = msg == null ? "Error occured!" : msg;
                                if ("Пользователь с таким email адресом уже существует. Используйте другой email".equals(msg)) msg = "E-Mail already in use. Please use another E-Mail address and try again!";
                                UserIO.getInstance().requestMessageDialog("Error occured", msg);
                                return;
                            } else if (skymonk.containsHTML("status:\'ok\'")) {
                                if (skymonk.containsHTML("(activation code has been sent to your e\\-mail|Код активации SkyMonk выслан на Ваш мобильный телефон)")) {
                                    getPluginConfig().setProperty("APPID", appId);
                                    getPluginConfig().setProperty("APPIDVALIDATE", true);
                                    getPluginConfig().setProperty("SKYMONKEMAIL", email);
                                    getPluginConfig().setProperty("SKYMONKEMAILCHANGED", email);
                                    getPluginConfig().setProperty("SKYMONKVALIDATE", true);
                                    UserIO.getInstance().requestMessageDialog("Activation succesfully!");
                                } else {
                                    msg = msg == null ? "OK!" : msg;
                                    UserIO.getInstance().requestMessageDialog("SkyMonk server answer", msg);
                                }
                            } else {
                                logger.warning("SkyMonk debug output: " + skymonk.toString());
                                UserIO.getInstance().requestMessageDialog("SkyMonk: Unknown error occured", "Please upload now a logfile, contact our support and add this loglink to your bugreport!");
                            }
                            getPluginConfig().save();
                        } else {
                            UserIO.getInstance().requestMessageDialog("SkyMonk is already activated!");
                        }
                    };
                }.start();

            }
        }, "Activation", null, null));
        getConfig().addEntry(new ConfigEntry(ConfigContainer.TYPE_SEPARATOR));
        getConfig().addEntry(new ConfigEntry(ConfigContainer.TYPE_CHECKBOX, getPluginConfig(), ShareFlareNet.ENABLEUNLIMITEDSIMULTANMAXFREEDLS, JDL.L("plugins.hoster.shareflarenet.enableunlimitedsimultanfreedls", "Enable unlimited (20) max simultanious free downloads (can cause problems, use at your own risc)")).setDefaultValue(false));
    }
}