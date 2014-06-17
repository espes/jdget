//    jDownloader - Downloadmanager
//    Copyright (C) 2013  JD-Team support@jdownloader.org
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

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.regex.Pattern;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

import jd.PluginWrapper;
import jd.config.ConfigContainer;
import jd.config.ConfigEntry;
import jd.config.Property;
import jd.config.SubConfiguration;
import jd.controlling.AccountController;
import jd.http.Browser;
import jd.http.Cookie;
import jd.http.Cookies;
import jd.http.URLConnectionAdapter;
import jd.nutils.encoding.Encoding;
import jd.parser.Regex;
import jd.parser.html.Form;
import jd.parser.html.HTMLParser;
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
import org.appwork.utils.formatter.TimeFormatter;
import org.appwork.utils.os.CrossSystem;

@HostPlugin(revision = "$Revision$", interfaceVersion = 2, names = { "rapidgator.net" }, urls = { "http://(www\\.)?(rapidgator\\.net|rg\\.to)/file/([a-z0-9]{32}|\\d+(/[^/<>]+\\.html)?)" }, flags = { 2 })
public class RapidGatorNet extends PluginForHost {

    public RapidGatorNet(final PluginWrapper wrapper) {
        super(wrapper);
        this.enablePremium("http://rapidgator.net/article/premium");
        this.setConfigElements();
    }

    public static class StringContainer {
        public String string = null;
    }

    private static final String    MAINPAGE             = "http://rapidgator.net/";
    private static Object          LOCK                 = new Object();
    private static StringContainer agent                = new StringContainer();
    private static final String    PREMIUMONLYTEXT      = "This file can be downloaded by premium only</div>";
    private static final String    PREMIUMONLYUSERTEXT  = JDL.L("plugins.hoster.rapidgatornet.only4premium", "Only downloadable for premium users!");
    private static AtomicBoolean   hasDled              = new AtomicBoolean(false);
    private static AtomicLong      timeBefore           = new AtomicLong(0);
    private final String           LASTIP               = "LASTIP";
    private static StringContainer lastIP               = new StringContainer();
    private final Pattern          IPREGEX              = Pattern.compile("(([1-2])?([0-9])?([0-9])\\.([1-2])?([0-9])?([0-9])\\.([1-2])?([0-9])?([0-9])\\.([1-2])?([0-9])?([0-9]))", Pattern.CASE_INSENSITIVE);
    private final String           EXPERIMENTALHANDLING = "EXPERIMENTALHANDLING";
    private final String           DISABLE_API_PREMIUM  = "DISABLE_API_PREMIUM";
    // Used to switch to web if there are problems with the API - has no effect since the setting to prefer web method has been added
    private static AtomicBoolean   useAPI               = new AtomicBoolean(true);
    private final String           apiURL               = "https://rapidgator.net/api/";

    private final String[]         IPCHECK              = new String[] { "http://ipcheck0.jdownloader.org", "http://ipcheck1.jdownloader.org", "http://ipcheck2.jdownloader.org", "http://ipcheck3.jdownloader.org" };

    @Override
    public String getAGBLink() {
        return "http://rapidgator.net/article/terms";
    }

    @Override
    public void correctDownloadLink(final DownloadLink link) throws Exception {
        if (link.getDownloadURL().contains("rg.to/")) {
            String url = link.getDownloadURL();
            url = url.replaceFirst("rg.to/", "rapidgator.net/");
            link.setUrlDownload(url);
        }
    }

    /* NO OVERRIDE!! We need to stay 0.9*compatible */
    @Override
    public boolean hasCaptcha(final DownloadLink link, final jd.plugins.Account acc) {
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

    @Override
    public boolean hasAutoCaptcha() {
        return false;
    }

    public static Browser prepareBrowser(final Browser prepBr) {
        if (prepBr == null) {
            return prepBr;
        }
        if (RapidGatorNet.agent.string == null) {
            /* we first have to load the plugin, before we can reference it */
            JDUtilities.getPluginForHost("mediafire.com");
            RapidGatorNet.agent.string = jd.plugins.hoster.MediafireCom.stringUserAgent();
        }
        prepBr.setRequestIntervalLimit("http://rapidgator.net/", 319 * (int) Math.round(Math.random() * 3 + Math.random() * 3));
        prepBr.getHeaders().put("User-Agent", RapidGatorNet.agent.string);
        prepBr.getHeaders().put("Accept-Charset", "ISO-8859-1,utf-8;q=0.7,*;q=0.3");
        prepBr.getHeaders().put("Accept-Language", "en-US,en;q=0.8");
        prepBr.getHeaders().put("Cache-Control", null);
        prepBr.getHeaders().put("Pragma", null);
        prepBr.setCookie("http://rapidgator.net/", "lang", "en");
        prepBr.setCustomCharset("UTF-8");
        prepBr.setReadTimeout(3 * 60 * 1000);
        prepBr.setConnectTimeout(3 * 60 * 1000);
        return prepBr;
    }

    private String handleJavaScriptRedirect() {
        /* check for js redirect */
        final int c = this.br.getRegex("\n").count();
        final boolean isJsRedirect = this.br.getRegex("<html><head><meta http-equiv=\"Content-Type\" content=\"[\\w\\-/;=]{20,50}\"></head>").matches();
        final String[] jsRedirectScripts = this.br.getRegex("<script language=\"JavaScript\">(.*?)</script>").getColumn(0);
        if (jsRedirectScripts != null && jsRedirectScripts.length == 1) {
            if (c == 0 && isJsRedirect) {
                /* final jsredirectcheck */
                String jsRedirectScript = jsRedirectScripts[0];
                final int scriptLen = jsRedirectScript.length();
                final int jsFactor = Math.round((float) scriptLen / (float) this.br.toString().length() * 100);
                /* min 75% of html contains js */
                if (jsFactor > 75) {
                    final String returnValue = new Regex(jsRedirectScript, ";(\\w+)=\'\';$").getMatch(0);
                    jsRedirectScript = jsRedirectScript.substring(0, jsRedirectScript.lastIndexOf("window.location.href"));
                    if (scriptLen > jsRedirectScript.length() && returnValue != null) {
                        return this.executeJavaScriptRedirect(returnValue, jsRedirectScript);
                    }
                }
            }
        }
        return null;
    }

    private String executeJavaScriptRedirect(final String retVal, final String script) {
        Object result = new Object();
        final ScriptEngineManager manager = jd.plugins.hoster.DummyScriptEnginePlugin.getScriptEngineManager(this);
        final ScriptEngine engine = manager.getEngineByName("javascript");
        try {
            engine.eval(script);
            result = engine.get(retVal);
        } catch (final Throwable e) {
            return null;
        }
        return result != null ? result.toString() : null;
    }

    @Override
    public AvailableStatus requestFileInformation(final DownloadLink link) throws Exception {
        this.correctDownloadLink(link);
        this.setBrowserExclusive();
        RapidGatorNet.prepareBrowser(this.br);
        this.br.setFollowRedirects(true);
        this.br.getPage(link.getDownloadURL());

        /* jsRedirect */
        final String reDirHash = this.handleJavaScriptRedirect();
        if (reDirHash != null) {
            this.logger.info("JSRedirect in requestFileInformation");
            this.br.getPage(link.getDownloadURL() + "?" + reDirHash);
        }

        if (this.br.containsHTML("400 Bad Request") && link.getDownloadURL().contains("%")) {
            link.setUrlDownload(link.getDownloadURL().replace("%", ""));
            this.br.getPage(link.getDownloadURL());
        }
        if (this.br.containsHTML("File not found")) {
            final String filenameFromURL = new Regex(link.getDownloadURL(), ".+/(.+)\\.html").getMatch(0);
            if (filenameFromURL != null) {
                link.setName(filenameFromURL);
            }
            throw new PluginException(LinkStatus.ERROR_FILE_NOT_FOUND);
        }
        final String freedlsizelimit = this.br.getRegex("'You can download files up to ([\\d\\.]+ ?(MB|GB)) in free mode<").getMatch(0);
        if (freedlsizelimit != null) {
            link.getLinkStatus().setStatusText(JDL.L("plugins.hoster.rapidgatornet.only4premium", "This file is restricted to Premium users only"));
        }
        String filename = this.br.getRegex("Downloading:[\t\n\r ]+</strong>([^<>\"]+)</p>").getMatch(0);
        if (filename == null) {
            filename = this.br.getRegex("<title>Download file ([^<>\"]+)</title>").getMatch(0);
        }
        final String filesize = this.br.getRegex("File size:[\t\n\r ]+<strong>([^<>\"]+)</strong>").getMatch(0);
        if (filename == null) {
            throw new PluginException(LinkStatus.ERROR_PLUGIN_DEFECT);
        }
        if (filename.startsWith(".")) {
            /* Temp workaround for mac user */
            filename = filename.substring(1);
        }
        link.setName(Encoding.htmlDecode(filename.trim()));
        if (filesize != null) {
            link.setDownloadSize(SizeFormatter.getSize(filesize));
        }
        this.br.setFollowRedirects(false);
        // Only show message if user has no active premium account
        if (this.br.containsHTML(RapidGatorNet.PREMIUMONLYTEXT) && AccountController.getInstance().getValidAccount(this) == null) {
            link.getLinkStatus().setStatusText(RapidGatorNet.PREMIUMONLYUSERTEXT);
        }
        return AvailableStatus.TRUE;
    }

    /**
     * JD2 CODE. DO NOT USE OVERRIDE FOR JD=) COMPATIBILITY REASONS!
     */
    public boolean isProxyRotationEnabledForLinkChecker() {
        return false;
    }

    @Override
    protected void showFreeDialog(final String domain) {
        if (System.getProperty("org.jdownloader.revision") != null) { /* JD2 ONLY! */
            super.showFreeDialog(domain);
            return;
        }
        try {
            SwingUtilities.invokeAndWait(new Runnable() {

                @Override
                public void run() {
                    try {
                        final String lng = System.getProperty("user.language");
                        String message = null;
                        String title = null;
                        final String tab = "                        ";
                        if ("de".equalsIgnoreCase(lng)) {
                            title = domain + " Free Download";
                            message = "Du lädst im kostenlosen Modus von " + domain + ".\r\n";
                            message += "Wie bei allen anderen Hostern holt JDownloader auch hier das Beste für dich heraus!\r\n\r\n";
                            message += tab + "  Falls du allerdings mehrere Dateien\r\n" + "          - und das möglichst mit Fullspeed und ohne Unterbrechungen - \r\n" + "             laden willst, solltest du dir den Premium Modus anschauen.\r\n\r\nUnserer Erfahrung nach lohnt sich das - Aber entscheide am besten selbst. Jetzt ausprobieren?  ";
                        } else {
                            title = domain + " Free Download";
                            message = "You are using the " + domain + " Free Mode.\r\n";
                            message += "JDownloader always tries to get the best out of each hoster's free mode!\r\n\r\n";
                            message += tab + "   However, if you want to download multiple files\r\n" + tab + "- possibly at fullspeed and without any wait times - \r\n" + tab + "you really should have a look at the Premium Mode.\r\n\r\nIn our experience, Premium is well worth the money. Decide for yourself, though. Let's give it a try?   ";
                        }
                        if (CrossSystem.isOpenBrowserSupported()) {
                            final int result = JOptionPane.showConfirmDialog(jd.gui.swing.jdgui.JDGui.getInstance().getMainFrame(), message, title, JOptionPane.YES_NO_OPTION, JOptionPane.INFORMATION_MESSAGE, null);
                            if (JOptionPane.OK_OPTION == result) {
                                CrossSystem.openURL(new URL("http://update3.jdownloader.org/jdserv/BuyPremiumInterface/redirect?" + domain + "&freedialog"));
                            }
                        }
                    } catch (final Throwable e) {
                    }
                }
            });
        } catch (final Throwable e) {
        }
    }

    private void checkShowFreeDialog() {
        SubConfiguration config = null;
        try {
            config = this.getPluginConfig();
            if (config.getBooleanProperty("premAdShown", Boolean.FALSE) == false) {
                if (config.getProperty("premAdShown2") == null) {
                    final File checkFile = JDUtilities.getResourceFile("tmp/rgtmp");
                    if (!checkFile.exists()) {
                        checkFile.mkdirs();
                        this.showFreeDialog("rapidgator.net");
                    }
                } else {
                    config = null;
                }
            } else {
                config = null;
            }
        } catch (final Throwable e) {
        } finally {
            if (config != null) {
                config.setProperty("premAdShown", Boolean.TRUE);
                config.setProperty("premAdShown2", "shown");
                config.save();
            }
        }
    }

    @Override
    public void handleFree(final DownloadLink downloadLink) throws Exception, PluginException {
        this.requestFileInformation(downloadLink);
        this.doFree(downloadLink);
    }

    private void doFree(final DownloadLink downloadLink) throws Exception {
        // experimental code - raz
        // so called 15mins between your last download, ends up with your IP blocked for the day..
        // Trail and error until we find the sweet spot.
        this.checkShowFreeDialog();
        final boolean useExperimentalHandling = this.getPluginConfig().getBooleanProperty(this.EXPERIMENTALHANDLING, false);
        final String currentIP = this.getIP();
        if (useExperimentalHandling) {
            this.logger.info("New Download: currentIP = " + currentIP);
            if (RapidGatorNet.hasDled.get() == true && this.ipChanged(currentIP, downloadLink) == false) {
                final long result = System.currentTimeMillis() - RapidGatorNet.timeBefore.get();
                // 35 minute wait less time since last download.
                this.logger.info("Wait time between downloads to prevent your IP from been blocked for 1 Day!");
                if (result > 0) {
                    throw new PluginException(LinkStatus.ERROR_IP_BLOCKED, "Wait time between download session", 2100000 - result);
                }
            }
        }
        if (this.br.containsHTML(RapidGatorNet.PREMIUMONLYTEXT)) {
            throw new PluginException(LinkStatus.ERROR_FATAL, RapidGatorNet.PREMIUMONLYUSERTEXT);
        }
        try {
            // end of experiment
            if (this.br.containsHTML("You have reached your daily downloads limit. Please try")) {
                throw new PluginException(LinkStatus.ERROR_IP_BLOCKED, "You have reached your daily downloads limit", 60 * 60 * 1000l);
            }
            if (this.br.containsHTML("(You can`t download not more than 1 file at a time in free mode\\.<|>Wish to remove the restrictions\\?)")) {
                throw new PluginException(LinkStatus.ERROR_IP_BLOCKED, "You can't download more than one file within a certain time period in free mode", 60 * 60 * 1000l);
            }
            final String freedlsizelimit = this.br.getRegex("'You can download files up to ([\\d\\.]+ ?(MB|GB)) in free mode<").getMatch(0);
            if (freedlsizelimit != null) {
                try {
                    throw new PluginException(LinkStatus.ERROR_PREMIUM, PluginException.VALUE_ID_PREMIUM_ONLY);
                } catch (final Throwable e) {
                    if (e instanceof PluginException) {
                        throw (PluginException) e;
                    }
                }
                throw new PluginException(LinkStatus.ERROR_FATAL, JDL.L("plugins.hoster.rapidgatornet.only4premium", "No free download link for this file"));
            }
            final String reconnectWait = this.br.getRegex("Delay between downloads must be not less than (\\d+) min\\.<br>Don`t want to wait\\? <a style=\"").getMatch(0);
            if (reconnectWait != null) {
                throw new PluginException(LinkStatus.ERROR_IP_BLOCKED, (Integer.parseInt(reconnectWait) + 1) * 60 * 1000l);
            }
            final String fid = this.br.getRegex("var fid = (\\d+);").getMatch(0);
            if (fid == null) {
                throw new PluginException(LinkStatus.ERROR_PLUGIN_DEFECT);
            }
            // far as I can tell it's not needed.
            final String[] sitelinks = HTMLParser.getHttpLinks(this.br.toString(), null);
            for (final String link : sitelinks) {
                if (link.matches("(.+\\.(js|css))")) {
                    final Browser br2 = this.br.cloneBrowser();
                    this.simulateBrowser(br2, link);
                }
            }
            int wait = 30;
            final String waittime = this.br.getRegex("var secs = (\\d+);").getMatch(0);
            if (waittime != null) {
                wait = Integer.parseInt(waittime);
            }
            Browser br2 = this.br.cloneBrowser();
            RapidGatorNet.prepareBrowser(br2);
            br2.getHeaders().put("X-Requested-With", "XMLHttpRequest");
            br2.getHeaders().put("Accept", "application/json, text/javascript, */*; q=0.01");
            br2.getPage("http://rapidgator.net/download/AjaxStartTimer?fid=" + fid);
            final String sid = br2.getRegex("sid\":\"([a-zA-Z0-9]{32})").getMatch(0);
            String state = br2.getRegex("state\":\"([^\"]+)").getMatch(0);
            if (!"started".equalsIgnoreCase(state)) {
                this.logger.info(br2.toString());
                throw new PluginException(LinkStatus.ERROR_PLUGIN_DEFECT);
            }
            if (sid == null) {
                this.logger.info(br2.toString());
                throw new PluginException(LinkStatus.ERROR_PLUGIN_DEFECT);
            }
            this.sleep((wait + 5) * 1001l, downloadLink);
            /* needed so we have correct referrer ;) (back to original br) */
            br2 = this.br.cloneBrowser();
            RapidGatorNet.prepareBrowser(br2);
            br2.getHeaders().put("X-Requested-With", "XMLHttpRequest");
            br2.getPage("http://rapidgator.net/download/AjaxGetDownloadLink?sid=" + sid);
            state = br2.getRegex("state\":\"(.*?)\"").getMatch(0);
            if (!"done".equalsIgnoreCase(state)) {
                if (br2.containsHTML("wait specified time")) {
                    throw new PluginException(LinkStatus.ERROR_TEMPORARILY_UNAVAILABLE, "ServerIssue", 5 * 60 * 1000l);
                }
                this.logger.info(br2.toString());
                throw new PluginException(LinkStatus.ERROR_PLUGIN_DEFECT);
            }
            final URLConnectionAdapter con1 = this.br.openGetConnection("http://rapidgator.net/download/captcha");
            if (con1.getResponseCode() == 302) {
                try {
                    con1.disconnect();
                } catch (final Throwable e) {
                }
                throw new PluginException(LinkStatus.ERROR_TEMPORARILY_UNAVAILABLE, "ServerIssue", 5 * 60 * 1000l);
            } else if (con1.getResponseCode() == 403) {
                try {
                    con1.disconnect();
                } catch (final Throwable e) {
                }
                throw new PluginException(LinkStatus.ERROR_TEMPORARILY_UNAVAILABLE, "Server error 403", 5 * 60 * 1000l);
            } else if (con1.getResponseCode() == 500) {
                try {
                    con1.disconnect();
                } catch (final Throwable e) {
                }
                throw new PluginException(LinkStatus.ERROR_IP_BLOCKED, "Downloading is not possible at the moment", 10 * 60 * 1000l);
            }
            // wasn't needed for raz, but psp said something about a redirect)
            this.br.followConnection();
            if (this.br.containsHTML("(api\\.recaptcha\\.net/|google\\.com/recaptcha/api/)")) {
                final PluginForHost recplug = JDUtilities.getPluginForHost("DirectHTTP");
                final jd.plugins.hoster.DirectHTTP.Recaptcha rc = ((DirectHTTP) recplug).getReCaptcha(this.br);
                for (int i = 0; i <= 5; i++) {
                    rc.parse();
                    rc.load();
                    final File cf = rc.downloadCaptcha(this.getLocalCaptchaFile());
                    final String c = this.getCaptchaCode(cf, downloadLink);
                    rc.getForm().put("DownloadCaptchaForm%5Bcaptcha%5D", "");
                    rc.setCode(c);
                    if (this.br.containsHTML("(>Please fix the following input errors|>The verification code is incorrect|api\\.recaptcha\\.net/|google\\.com/recaptcha/api/)")) {
                        continue;
                    }
                    break;
                }
            } else {
                if (this.br.containsHTML("//api\\.solvemedia\\.com/papi|//api\\.adscapchta\\.com/")) {
                    final Form captcha = this.br.getFormbyProperty("id", "captchaform");
                    if (captcha == null) {
                        this.logger.info(this.br.toString());
                        throw new PluginException(LinkStatus.ERROR_PLUGIN_DEFECT);
                    }

                    captcha.put("DownloadCaptchaForm[captcha]", "");
                    String code = null, challenge = null;
                    final Browser capt = this.br.cloneBrowser();

                    if (this.br.containsHTML("//api\\.solvemedia\\.com/papi")) {
                        final PluginForDecrypt solveplug = JDUtilities.getPluginForDecrypt("linkcrypt.ws");
                        final jd.plugins.decrypter.LnkCrptWs.SolveMedia sm = ((jd.plugins.decrypter.LnkCrptWs) solveplug).getSolveMedia(this.br);
                        final File cf = sm.downloadCaptcha(this.getLocalCaptchaFile());
                        code = this.getCaptchaCode(cf, downloadLink);
                        final String chid = sm.getChallenge(code);

                        // if (chid == null) throw new PluginException(LinkStatus.ERROR_CAPTCHA);
                        captcha.put("adcopy_challenge", chid);
                        captcha.put("adcopy_response", Encoding.urlEncode(code));

                    } else if (this.br.containsHTML("//api\\.adscapchta\\.com/")) {
                        final String captchaAdress = captcha.getRegex("<iframe src=\'(http://api\\.adscaptcha\\.com/NoScript\\.aspx\\?CaptchaId=\\d+&PublicKey=[^\'<>]+)").getMatch(0);
                        final String captchaType = new Regex(captchaAdress, "CaptchaId=(\\d+)&").getMatch(0);
                        if (captchaAdress == null || captchaType == null) {
                            throw new PluginException(LinkStatus.ERROR_PLUGIN_DEFECT);
                        }

                        if (!"3017".equals(captchaType)) {
                            this.logger.warning("ADSCaptcha: Captcha type not supported!");
                            throw new PluginException(LinkStatus.ERROR_PLUGIN_DEFECT);
                        }
                        capt.getPage(captchaAdress);
                        challenge = capt.getRegex("<img src=\"(http://api\\.adscaptcha\\.com//Challenge\\.aspx\\?cid=[^\"]+)").getMatch(0);
                        code = capt.getRegex("class=\"code\">([0-9a-f\\-]+)<").getMatch(0);

                        if (challenge == null || code == null) {
                            throw new PluginException(LinkStatus.ERROR_PLUGIN_DEFECT);
                        }
                        challenge = this.getCaptchaCode(challenge, downloadLink);
                        captcha.put("adscaptcha_response_field", challenge);
                        captcha.put("adscaptcha_challenge_field", Encoding.urlEncode(code));
                    }
                    this.br.submitForm(captcha);
                }
            }
            // Set-Cookie: failed_on_captcha=1; path=/ response if the captcha expired.
            if ("1".equals(this.br.getCookie("http://rapidgator.net", "failed_on_captcha")) || this.br.containsHTML("(>Please fix the following input errors|>The verification code is incorrect|api\\.recaptcha\\.net/|google\\.com/recaptcha/api/|//api\\.solvemedia\\.com/papi|//api\\.adscaptcha\\.com)")) {
                try {
                    this.invalidateLastChallengeResponse();
                } catch (final Throwable e) {
                }
                throw new PluginException(LinkStatus.ERROR_CAPTCHA);
            } else {
                try {
                    this.validateLastChallengeResponse();
                } catch (final Throwable e) {
                }
            }

            String dllink = this.br.getRegex("'(http://[A-Za-z0-9\\-_]+\\.rapidgator\\.net//\\?r=download/index&session_id=[A-Za-z0-9]+)'").getMatch(0);
            if (dllink == null) {
                dllink = this.br.getRegex("'(http://[A-Za-z0-9\\-_]+\\.rapidgator\\.net//\\?r=download/index&session_id=[A-Za-z0-9]+)'").getMatch(0);
            }
            // Old regex
            if (dllink == null) {
                dllink = this.br.getRegex("location\\.href = '(http://.*?)'").getMatch(0);
            }
            if (dllink == null) {
                this.logger.info(this.br.toString());
                if (this.br.getRegex("location\\.href = '/\\?r=download/index&session_id=[A-Za-z0-9]+'").matches()) {
                    throw new PluginException(LinkStatus.ERROR_FILE_NOT_FOUND);
                }
                throw new PluginException(LinkStatus.ERROR_PLUGIN_DEFECT);
            }
            this.dl = jd.plugins.BrowserAdapter.openDownload(this.br, downloadLink, dllink, true, 1);
            if (this.dl.getConnection().getContentType().contains("html")) {
                if (this.dl.getConnection().getResponseCode() == 416) {
                    throw new PluginException(LinkStatus.ERROR_TEMPORARILY_UNAVAILABLE, "Server error 416", 10 * 60 * 1000l);
                }
                this.br.followConnection();
                if (this.br.containsHTML("<div class=\"error\">[\r\n ]+Error\\. Link expired. You have reached your daily limit of downloads\\.")) {
                    throw new PluginException(LinkStatus.ERROR_IP_BLOCKED, "Link expired, or You've reached your daily limit ", 60 * 60 * 1000l);
                } else if (this.br.containsHTML("<div class=\"error\">[\r\n ]+File is already downloading</div>")) {
                    throw new PluginException(LinkStatus.ERROR_IP_BLOCKED, "Download session in progress", 20 * 60 * 1000l);
                } else {
                    throw new PluginException(LinkStatus.ERROR_PLUGIN_DEFECT);
                }
            }
            this.dl.startDownload();
            RapidGatorNet.hasDled.set(true);
        } catch (final Exception e) {
            RapidGatorNet.hasDled.set(false);
            throw e;
        } finally {
            try {
                RapidGatorNet.timeBefore.set(System.currentTimeMillis());
                this.setIP(currentIP, downloadLink);
            } catch (final Throwable e) {
            }
        }

    }

    @Override
    public int getMaxSimultanFreeDownloadNum() {
        return 1;
    }

    private static AtomicInteger maxPrem = new AtomicInteger(1);

    @Override
    public AccountInfo fetchAccountInfo(final Account account) throws Exception {
        final AccountInfo ai = new AccountInfo();
        synchronized (RapidGatorNet.LOCK) {
            if (this.getPluginConfig().getBooleanProperty(this.DISABLE_API_PREMIUM, false)) {
                return this.fetchAccountInfo_web(account, ai);
            } else {
                return this.fetchAccountInfo_api(account, ai);
            }
        }
    }

    public AccountInfo fetchAccountInfo_api(final Account account, final AccountInfo ai) throws Exception {
        synchronized (RapidGatorNet.LOCK) {
            try {
                RapidGatorNet.maxPrem.set(1);
                final String sid = this.login_api(account);
                if (sid != null) {
                    account.setValid(true);
                    /* premium account */
                    final String expire_date = this.getJSonValueByKey("expire_date");
                    final String traffic_left = this.getJSonValueByKey("traffic_left");
                    final String reset_in = this.getJSonValueByKey("reset_in");
                    if (expire_date != null && traffic_left != null) {
                        /*
                         * expire date and traffic left are available, so its a premium account, add one day extra to prevent it from
                         * expiring too early
                         */
                        ai.setValidUntil(Long.parseLong(expire_date) * 1000 + 24 * 60 * 60 * 1000l);
                        ai.setTrafficLeft(Long.parseLong(traffic_left));
                        if (!ai.isExpired()) {
                            account.setProperty("session_type", "premium");
                            /* account still valid */
                            if (reset_in != null) {
                                ai.setStatus("Traffic exceeded " + reset_in);
                                account.setTempDisabled(true);
                            } else {
                                ai.setStatus("Premium account");
                            }
                            try {
                                RapidGatorNet.maxPrem.set(-1);
                                account.setMaxSimultanDownloads(-1);
                                account.setConcurrentUsePossible(true);
                            } catch (final Throwable e) {
                                // not available in old Stable 0.9.581
                            }
                            account.setProperty("free", false);
                            return ai;
                        }
                    }
                    account.setProperty("free", true);
                    ai.setStatus("Free account");
                    account.setProperty("session_type", Property.NULL);
                    try {
                        RapidGatorNet.maxPrem.set(1);
                        account.setMaxSimultanDownloads(1);
                        account.setConcurrentUsePossible(false);
                    } catch (final Throwable e) {
                        // not available in old Stable 0.9.581
                    }
                    return ai;
                }
                account.setValid(false);
                account.setProperty("session_type", Property.NULL);
                return ai;
            } catch (final PluginException e) {
                account.setProperty("free", Property.NULL);
                account.setProperty("session_type", Property.NULL);
                account.setValid(false);
                throw e;
            }
        }
    }

    public AccountInfo fetchAccountInfo_web(final Account account, final AccountInfo ai) throws Exception {
        RapidGatorNet.maxPrem.set(1);
        try {
            this.login_web(account, true);
        } catch (final PluginException e) {
            account.setValid(false);
            throw e;
        }
        if (account.getBooleanProperty("free")) {
            ai.setStatus("Registered (free) User");
            try {
                RapidGatorNet.maxPrem.set(1);
                // free accounts still have captcha.
                account.setMaxSimultanDownloads(RapidGatorNet.maxPrem.get());
                account.setConcurrentUsePossible(false);
            } catch (final Throwable e) {
                // not available in old Stable 0.9.581
            }
            ai.setUnlimitedTraffic();
        } else {
            this.br.getPage("http://rapidgator.net/profile/index");
            String availableTraffic = this.br.getRegex(">Bandwith available</td>\\s+<td>\\s+([^<>\"]*?) of").getMatch(0);
            logger.info("availableTraffic = " + availableTraffic);
            if (availableTraffic != null) {
                Long avtr = SizeFormatter.getSize(availableTraffic.trim());
                if (avtr == 0) {
                    availableTraffic = "1024 GB"; // SizeFormatter can't handle TB (Temporary workaround)
                }
                ai.setTrafficLeft(SizeFormatter.getSize(availableTraffic.trim()));
            } else {
                /* Probably not true but our errorhandling for empty traffic should work well */
                ai.setUnlimitedTraffic();
            }
            this.br.getPage("http://rapidgator.net/Payment/Payment");
            final String expire = this.br.getRegex("style=\"width:100px;\">\\d+</td><td>([^<>\"]*?)</td>").getMatch(0);
            if (expire == null) {
                this.logger.warning("Could not find expire date!");
                account.setValid(false);
                return ai;
            } else {
                ai.setValidUntil(TimeFormatter.getMilliSeconds(expire, "yyyy-MM-dd", Locale.ENGLISH) + 24 * 60 * 60 * 1000);
            }
            ai.setStatus("Premium User");
            try {
                RapidGatorNet.maxPrem.set(-1);
                account.setMaxSimultanDownloads(RapidGatorNet.maxPrem.get());
                account.setConcurrentUsePossible(true);
            } catch (final Throwable e) {
                // not available in old Stable 0.9.581
            }
        }
        account.setValid(true);
        return ai;
    }

    @SuppressWarnings("unchecked")
    private Map<String, String> login_web(final Account account, final boolean force) throws Exception {
        synchronized (RapidGatorNet.LOCK) {
            try {
                // Load cookies
                this.br.setCookiesExclusive(true);
                RapidGatorNet.prepareBrowser(this.br);
                final Object ret = account.getProperty("cookies", null);
                boolean acmatch = Encoding.urlEncode(account.getUser()).equals(account.getStringProperty("name", Encoding.urlEncode(account.getUser())));
                if (acmatch) {
                    acmatch = Encoding.urlEncode(account.getPass()).equals(account.getStringProperty("pass", Encoding.urlEncode(account.getPass())));
                }
                if (acmatch && ret != null && ret instanceof Map<?, ?> && !force) {
                    final Map<String, String> cookies = (Map<String, String>) ret;
                    if (account.isValid()) {
                        for (final Map.Entry<String, String> cookieEntry : cookies.entrySet()) {
                            final String key = cookieEntry.getKey();
                            final String value = cookieEntry.getValue();
                            this.br.setCookie(RapidGatorNet.MAINPAGE, key, value);
                        }
                        return cookies;
                    }
                }

                this.br.setFollowRedirects(true);

                this.br.getPage(RapidGatorNet.MAINPAGE);
                final Form loginForm = this.br.getFormbyProperty("id", "login");
                String loginPostData = "LoginForm%5Bemail%5D=" + Encoding.urlEncode(account.getUser()) + "&LoginForm%5Bpassword%5D=" + Encoding.urlEncode(account.getPass());
                if (loginForm != null) {
                    String user = loginForm.getBestVariable("email");
                    String pass = loginForm.getBestVariable("password");
                    if (user == null) {
                        user = "LoginForm%5Bemail%5D";
                    }
                    if (pass == null) {
                        pass = "LoginForm%5Bpassword%5D";
                    }
                    loginForm.put(user, Encoding.urlEncode(account.getUser()));
                    loginForm.put(pass, Encoding.urlEncode(account.getPass()));
                    this.br.submitForm(loginForm);
                    loginPostData = loginForm.getPropertyString();
                } else {
                    this.br.postPage("https://rapidgator.net/auth/login", loginPostData);
                }

                /* jsRedirect */
                final String reDirHash = this.handleJavaScriptRedirect();
                if (reDirHash != null) {
                    this.logger.info("JSRedirect in login");
                    // prob should be https also!!
                    this.br.postPage("https://rapidgator.net/auth/login", loginPostData + "&" + reDirHash);
                }

                if (this.br.getCookie(RapidGatorNet.MAINPAGE, "user__") == null) {
                    this.logger.info("disabled because of" + this.br.toString());
                    final String lang = System.getProperty("user.language");
                    if ("de".equalsIgnoreCase(lang)) {
                        throw new PluginException(LinkStatus.ERROR_PREMIUM, "\r\nUngültiger Benutzername oder ungültiges Passwort!\r\nDu bist dir sicher, dass dein eingegebener Benutzername und Passwort stimmen? Versuche folgendes:\r\n1. Falls dein Passwort Sonderzeichen enthält, ändere es (entferne diese) und versuche es erneut!\r\n2. Gib deine Zugangsdaten per Hand (ohne kopieren/einfügen) ein.", PluginException.VALUE_ID_PREMIUM_DISABLE);
                    } else {
                        throw new PluginException(LinkStatus.ERROR_PREMIUM, "\r\nInvalid username/password!\r\nYou're sure that the username and password you entered are correct? Some hints:\r\n1. If your password contains special characters, change it (remove them) and try again!\r\n2. Type in your username/password by hand without copy & paste.", PluginException.VALUE_ID_PREMIUM_DISABLE);
                    }
                }
                if (this.br.containsHTML("Account:&nbsp;<a href=\"/article/premium\">Free</a>")) {
                    account.setProperty("free", true);
                } else {
                    account.setProperty("free", false);
                }
                // Save cookies
                final HashMap<String, String> cookies = new HashMap<String, String>();
                final Cookies add = this.br.getCookies(RapidGatorNet.MAINPAGE);
                for (final Cookie c : add.getCookies()) {
                    cookies.put(c.getKey(), c.getValue());
                }
                account.setProperty("name", Encoding.urlEncode(account.getUser()));
                account.setProperty("pass", Encoding.urlEncode(account.getPass()));
                account.setProperty("cookies", cookies);
                return cookies;
            } catch (final PluginException e) {
                account.setProperty("free", Property.NULL);
                account.setProperty("cookies", Property.NULL);
                throw e;
            }
        }
    }

    private String login_api(final Account account) throws Exception {
        String session_id = null;
        URLConnectionAdapter con = null;
        synchronized (RapidGatorNet.LOCK) {
            try {
                this.prepareBrowser_api(this.br);
                con = this.br.openGetConnection(this.apiURL + "user/login?username=" + Encoding.urlEncode(account.getUser()) + "&password=" + Encoding.urlEncode(account.getPass()));
                this.handleErrors_api(null, null, account, con);
                if (con.getResponseCode() == 200) {
                    this.br.followConnection();
                    session_id = this.getJSonValueByKey("session_id");
                    boolean isPremium = false;
                    if (session_id != null) {
                        final String expire_date = this.getJSonValueByKey("expire_date");
                        final String traffic_left = this.getJSonValueByKey("traffic_left");
                        if (expire_date != null && traffic_left != null) {
                            /*
                             * expire date and traffic left are available, so its a premium account, add one day extra to prevent it from
                             * expiring too early
                             */
                            final AccountInfo ai = new AccountInfo();
                            ai.setValidUntil(Long.parseLong(expire_date) * 1000 + 24 * 60 * 60 * 1000l);
                            isPremium = !ai.isExpired();
                        }
                    }
                    if (isPremium) {
                        account.setProperty("session_type", "premium");
                    } else {
                        account.setProperty("session_type", Property.NULL);
                    }
                    return session_id;
                }
                throw new PluginException(LinkStatus.ERROR_PLUGIN_DEFECT);
            } finally {
                try {
                    con.disconnect();
                } catch (final Throwable ignore) {
                }
                account.setProperty("session_type", Property.NULL);
                account.setProperty("session_id", session_id);
            }
        }
    }

    private String getJSonValueByKey(final String key) {
        String result = this.br.getRegex("\"" + key + "\":\"([^\"]+)\"").getMatch(0);
        if (result == null) {
            result = this.br.getRegex("\"" + key + "\":(\\d+)").getMatch(0);
        }
        return result;
    }

    @Override
    public void handlePremium(final DownloadLink link, final Account account) throws Exception {
        this.correctDownloadLink(link);
        if (this.getPluginConfig().getBooleanProperty(this.DISABLE_API_PREMIUM, false)) {
            this.requestFileInformation(link);
            this.handlePremium_web(link, account);
        } else {
            this.handlePremium_api(link, account);
        }
    }

    public static String readErrorStream(final URLConnectionAdapter con) throws UnsupportedEncodingException, IOException {
        BufferedReader f = null;
        try {
            try {
                con.setAllowedResponseCodes(new int[] { con.getResponseCode() });
            } catch (final Throwable not09581) {
            }
            final InputStream es = con.getErrorStream();
            if (es == null) {
                throw new IOException("No errorstream!");
            }
            f = new BufferedReader(new InputStreamReader(es, "UTF8"));
            String line;
            final StringBuilder ret = new StringBuilder();
            final String sep = System.getProperty("line.separator");
            while ((line = f.readLine()) != null) {
                if (ret.length() > 0) {
                    ret.append(sep);
                }
                ret.append(line);
            }
            return ret.toString();
        } finally {
            try {
                f.close();
            } catch (final Throwable e) {
            }

        }
    }

    private void handleErrors_api(final String session_id, final DownloadLink link, final Account account, final URLConnectionAdapter con) throws PluginException, UnsupportedEncodingException, IOException {
        if (link != null && con.getResponseCode() == 404) {
            throw new PluginException(LinkStatus.ERROR_FILE_NOT_FOUND);
        }
        if (link != null && con.getResponseCode() == 416) {
            throw new PluginException(LinkStatus.ERROR_TEMPORARILY_UNAVAILABLE, "Server error 416", 5 * 60 * 1000l);
        }
        if (link != null && con.getResponseCode() == 500) {
            throw new PluginException(LinkStatus.ERROR_TEMPORARILY_UNAVAILABLE, "Server error 500", 60 * 60 * 1000l);
        }
        if (con.getResponseCode() != 200) {
            synchronized (RapidGatorNet.LOCK) {
                final String lang = System.getProperty("user.language");
                final String errorMessage = RapidGatorNet.readErrorStream(con);
                this.logger.info("ErrorMessage: " + errorMessage);
                if (link != null && errorMessage.contains("Exceeded traffic")) {
                    final AccountInfo ac = new AccountInfo();
                    ac.setTrafficLeft(0);
                    account.setAccountInfo(ac);
                    throw new PluginException(LinkStatus.ERROR_PREMIUM, PluginException.VALUE_ID_PREMIUM_TEMP_DISABLE);
                }
                boolean sessionReset = false;
                if (session_id != null && session_id.equals(account.getStringProperty("session_id", null))) {
                    sessionReset = true;
                }
                if (errorMessage.contains("Please wait")) {
                    if (link == null) {
                        /* we are inside fetchAccountInfo */
                        throw new PluginException(LinkStatus.ERROR_PREMIUM, PluginException.VALUE_ID_PREMIUM_TEMP_DISABLE);
                    } else {
                        /* we are inside handlePremium */
                        throw new PluginException(LinkStatus.ERROR_HOSTER_TEMPORARILY_UNAVAILABLE, 10 * 60 * 1000l);
                    }
                }
                if (errorMessage.contains("User is not PREMIUM") || errorMessage.contains("This file can be downloaded by premium only") || errorMessage.contains("You can download files up to")) {
                    if (sessionReset) {
                        account.setProperty("session_id", Property.NULL);
                        account.setProperty("session_type", Property.NULL);
                    }
                    throw new PluginException(LinkStatus.ERROR_RETRY);
                }
                if (errorMessage.contains("Login or password is wrong")) {
                    if ("de".equalsIgnoreCase(lang)) {
                        throw new PluginException(LinkStatus.ERROR_PREMIUM, "\r\nUngültiger Benutzername oder ungültiges Passwort!\r\nDu bist dir sicher, dass dein eingegebener Benutzername und Passwort stimmen? Versuche folgendes:\r\n1. Falls dein Passwort Sonderzeichen enthält, ändere es (entferne diese) und versuche es erneut!\r\n2. Gib deine Zugangsdaten per Hand (ohne kopieren/einfügen) ein.", PluginException.VALUE_ID_PREMIUM_DISABLE);
                    } else {
                        throw new PluginException(LinkStatus.ERROR_PREMIUM, "\r\nInvalid username/password!\r\nYou're sure that the username and password you entered are correct? Some hints:\r\n1. If your password contains special characters, change it (remove them) and try again!\r\n2. Type in your username/password by hand without copy & paste.", PluginException.VALUE_ID_PREMIUM_DISABLE);
                    }
                }
                if (errorMessage.contains("User is FROZEN")) {
                    if ("de".equalsIgnoreCase(lang)) {
                        throw new PluginException(LinkStatus.ERROR_PREMIUM, "\r\nAccount ist gesperrt!", PluginException.VALUE_ID_PREMIUM_DISABLE);
                    } else {
                        throw new PluginException(LinkStatus.ERROR_PREMIUM, "\r\nAccount is banned!", PluginException.VALUE_ID_PREMIUM_DISABLE);
                    }
                }
                if (errorMessage.contains("Parameter login or password is missing")) {
                    throw new PluginException(LinkStatus.ERROR_PLUGIN_DEFECT);
                }
                if (errorMessage.contains("Session not exist")) {
                    if (sessionReset) {
                        account.setProperty("session_id", Property.NULL);
                        account.setProperty("session_type", Property.NULL);
                    }
                    throw new PluginException(LinkStatus.ERROR_RETRY);
                }
                if (con.getResponseCode() == 503 || errorMessage.contains("Service Temporarily Unavailable")) {
                    throw new PluginException(LinkStatus.ERROR_TEMPORARILY_UNAVAILABLE, "Service Temporarily Unavailable", 5 * 60 * 1000l);
                }
                if (link != null) {
                    RapidGatorNet.useAPI.set(false);
                }
                throw new PluginException(LinkStatus.ERROR_PLUGIN_DEFECT);
            }
        }
    }

    private void prepareBrowser_api(final Browser br) {
        RapidGatorNet.prepareBrowser(br);
        try {
            /* not available in old stable */
            if (br != null) {
                br.setAllowedResponseCodes(new int[] { 401, 402, 501, 423 });
            }
        } catch (final Throwable not09581) {
        }
    }

    public void handlePremium_api(final DownloadLink link, final Account account) throws Exception {
        this.prepareBrowser_api(this.br);
        String session_id = null;
        boolean isPremium = false;
        synchronized (RapidGatorNet.LOCK) {
            session_id = account.getStringProperty("session_id", null);
            if (session_id == null) {
                session_id = this.login_api(account);
            }
            if ("premium".equals(account.getStringProperty("session_type", null))) {
                isPremium = true;
            }
        }
        if (isPremium == false) {
            this.handleFree(link);
            return;
        }
        if (session_id == null) {
            RapidGatorNet.useAPI.set(false);
            throw new PluginException(LinkStatus.ERROR_PLUGIN_DEFECT);
        }
        URLConnectionAdapter con = null;
        String fileName = link.getFinalFileName();
        if (fileName == null) {
            /* no final filename yet, do linkcheck */
            try {
                con = this.br.openGetConnection(this.apiURL + "file/info?sid=" + session_id + "&url=" + Encoding.urlEncode(link.getDownloadURL()));
                this.handleErrors_api(session_id, link, account, con);
                if (con.getResponseCode() == 200) {
                    this.br.followConnection();
                    fileName = this.getJSonValueByKey("filename");
                    final String fileSize = this.getJSonValueByKey("size");
                    final String fileHash = this.getJSonValueByKey("hash");
                    if (fileName != null) {
                        link.setFinalFileName(fileName);
                    }
                    if (fileSize != null) {
                        final long size = Long.parseLong(fileSize);
                        try {
                            link.setVerifiedFileSize(size);
                        } catch (final Throwable not09581) {
                            link.setDownloadSize(size);
                        }
                    }
                    if (fileHash != null) {
                        link.setMD5Hash(fileHash);
                    }
                }
            } finally {
                try {
                    con.disconnect();
                } catch (final Throwable ignore) {
                }
            }
        }
        if (fileName == null) {
            RapidGatorNet.useAPI.set(false);
            return;
            // throw new PluginException(LinkStatus.ERROR_PLUGIN_DEFECT);
        }
        String url = null;
        try {
            con = this.br.openGetConnection(this.apiURL + "file/download?sid=" + session_id + "&url=" + Encoding.urlEncode(link.getDownloadURL()));
            this.handleErrors_api(session_id, link, account, con);
            if (con.getResponseCode() == 200) {
                this.br.followConnection();
                url = this.getJSonValueByKey("url");
                if (url != null) {
                    url = url.replace("\\", "");
                }
            }
        } finally {
            try {
                con.disconnect();
            } catch (final Throwable ignore) {
            }
        }
        if (url == null) {
            RapidGatorNet.useAPI.set(false);
            throw new PluginException(LinkStatus.ERROR_RETRY);
        }
        this.dl = jd.plugins.BrowserAdapter.openDownload(this.br, link, url, true, 0);
        if (this.dl.getConnection().getContentType().contains("html")) {
            this.logger.warning("The final dllink seems not to be a file!");
            this.handleErrors_api(session_id, link, account, this.dl.getConnection());
            throw new PluginException(LinkStatus.ERROR_PLUGIN_DEFECT);
        }
        this.dl.startDownload();
    }

    public void handlePremium_web(final DownloadLink link, final Account account) throws Exception {
        this.logger.info("Performing cached login sequence!!");
        Map<String, String> cookies = this.login_web(account, false);
        final int repeat = 2;
        for (int i = 0; i <= repeat; i++) {
            this.br.setFollowRedirects(false);
            this.br.getPage(link.getDownloadURL());
            if (this.br.getCookie(RapidGatorNet.MAINPAGE, "user__") == null && i + 1 != repeat) {
                // lets login fully again, as hoster as removed premium cookie for some unknown reason...
                this.logger.info("Performing full login sequence!!");
                this.br = new Browser();
                cookies = this.login_web(account, true);
                continue;
            } else if (this.br.getCookie(RapidGatorNet.MAINPAGE, "user__") == null && i + 1 == repeat) {
                // failure
                this.logger.warning("handlePremium Failed! Please report to JDownloader Development Team.");
                synchronized (RapidGatorNet.LOCK) {
                    if (cookies == account.getProperty("cookies", null)) {
                        account.setProperty("cookies", Property.NULL);
                    }
                }
                throw new PluginException(LinkStatus.ERROR_PLUGIN_DEFECT);
            } else {
                break;
            }
        }
        if (account.getBooleanProperty("free")) {
            this.doFree(link);
        } else {
            String dllink = this.br.getRedirectLocation();
            if (dllink == null) {
                /* jsRedirect */
                final String reDirHash = this.handleJavaScriptRedirect();
                if (reDirHash != null) {
                    this.logger.info("JSRedirect in premium");
                    this.br.getPage(link.getDownloadURL() + "?" + reDirHash);
                }
                dllink = this.br.getRegex("var premium_download_link = '(http://[^<>\"']+)';").getMatch(0);
                if (dllink == null) {
                    dllink = this.br.getRegex("'(http://pr_srv\\.rapidgator\\.net//\\?r=download/index&session_id=[A-Za-z0-9]+)'").getMatch(0);
                    if (dllink == null) {
                        dllink = this.br.getRegex("'(http://pr\\d+\\.rapidgator\\.net//\\?r=download/index&session_id=[A-Za-z0-9]+)'").getMatch(0);
                        if (dllink == null) {
                            if (this.br.containsHTML("You have reached quota|You have reached daily quota of downloaded information for premium accounts")) {
                                this.logger.info("You've reached daily download quota for " + account.getUser() + " account");
                                final AccountInfo ac = new AccountInfo();
                                ac.setTrafficLeft(0);
                                account.setAccountInfo(ac);
                                throw new PluginException(LinkStatus.ERROR_RETRY);
                            }
                            if (this.br.getCookie(RapidGatorNet.MAINPAGE, "user__") == null) {
                                this.logger.info("Account seems to be invalid!");
                                // throw new PluginException(LinkStatus.ERROR_PREMIUM, PluginException.VALUE_ID_PREMIUM_DISABLE);
                                account.setProperty("cookies", Property.NULL);
                                throw new PluginException(LinkStatus.ERROR_PLUGIN_DEFECT);
                            }
                            this.logger.warning("Could not find 'dllink'. Please report to JDownloader Development Team");
                            throw new PluginException(LinkStatus.ERROR_PLUGIN_DEFECT);
                        }
                    }
                }
            }
            this.dl = jd.plugins.BrowserAdapter.openDownload(this.br, link, Encoding.htmlDecode(dllink), true, 0);
            if (this.dl.getConnection().getContentType().contains("html")) {
                this.logger.warning("The final dllink seems not to be a file!");
                this.handleErrors_api(null, link, account, this.dl.getConnection());
                throw new PluginException(LinkStatus.ERROR_PLUGIN_DEFECT);
            }
            this.dl.startDownload();
        }
    }

    private void simulateBrowser(final Browser rb, final String url) {
        if (rb == null || url == null) {
            return;
        }
        URLConnectionAdapter con = null;
        try {
            con = rb.openGetConnection(url);
        } catch (final Throwable e) {
        } finally {
            try {
                con.disconnect();
            } catch (final Exception e) {
            }
        }
    }

    private String getIP() throws PluginException {
        final Browser ip = new Browser();
        String currentIP = null;
        final ArrayList<String> checkIP = new ArrayList<String>(Arrays.asList(this.IPCHECK));
        Collections.shuffle(checkIP);
        for (final String ipServer : checkIP) {
            if (currentIP == null) {
                try {
                    ip.getPage(ipServer);
                    currentIP = ip.getRegex(this.IPREGEX).getMatch(0);
                    if (currentIP != null) {
                        break;
                    }
                } catch (final Throwable e) {
                }
            }
        }
        if (currentIP == null) {
            this.logger.warning("firewall/antivirus/malware/peerblock software is most likely is restricting accesss to JDownloader IP checking services");
            throw new PluginException(LinkStatus.ERROR_PLUGIN_DEFECT);
        }
        return currentIP;
    }

    private boolean ipChanged(final String IP, final DownloadLink link) throws PluginException {
        String currentIP = null;
        if (IP != null && new Regex(IP, this.IPREGEX).matches()) {
            currentIP = IP;
        } else {
            currentIP = this.getIP();
        }
        if (currentIP == null) {
            return false;
        }
        String lastIP = link.getStringProperty(this.LASTIP, null);
        if (lastIP == null) {
            lastIP = RapidGatorNet.lastIP.string;
        }
        return !currentIP.equals(lastIP);
    }

    private boolean setIP(final String IP, final DownloadLink link) throws PluginException {
        synchronized (this.IPCHECK) {
            if (IP != null && !new Regex(IP, this.IPREGEX).matches()) {
                throw new PluginException(LinkStatus.ERROR_PLUGIN_DEFECT);
            }
            if (this.ipChanged(IP, link) == false) {
                // Static IP or failure to reconnect! We don't change lastIP
                this.logger.warning("Your IP hasn't changed since last download");
                return false;
            } else {
                final String lastIP = IP;
                link.setProperty(this.LASTIP, lastIP);
                RapidGatorNet.lastIP.string = lastIP;
                this.logger.info("LastIP = " + lastIP);
                return true;
            }
        }
    }

    private void setConfigElements() {
        this.getConfig().addEntry(new ConfigEntry(ConfigContainer.TYPE_CHECKBOX, this.getPluginConfig(), this.EXPERIMENTALHANDLING, JDL.L("plugins.hoster.rapidgatornet.useExperimentalWaittimeHandling", "Activate experimental waittime handling?")).setDefaultValue(false));
        // Some users always get server error 500 via API
        this.getConfig().addEntry(new ConfigEntry(ConfigContainer.TYPE_CHECKBOX, this.getPluginConfig(), this.DISABLE_API_PREMIUM, JDL.L("plugins.hoster.rapidgatornet.disableAPIPremium", "Disable API for premium downloads (use web download)?")).setDefaultValue(false));
    }

    @Override
    public int getMaxSimultanPremiumDownloadNum() {
        return RapidGatorNet.maxPrem.get();
    }

    @Override
    public void reset() {
    }

    @Override
    public void resetDownloadlink(final DownloadLink link) {
    }

    private static AtomicBoolean stableSucks = new AtomicBoolean(false);

    public static void showSSLWarning(final String domain) {
        try {
            SwingUtilities.invokeAndWait(new Runnable() {

                @Override
                public void run() {
                    try {
                        final String lng = System.getProperty("user.language");
                        String message = null;
                        String title = null;
                        final boolean xSystem = CrossSystem.isOpenBrowserSupported();
                        if ("de".equalsIgnoreCase(lng)) {
                            title = domain + " :: Java 7+ && HTTPS Post Requests.";
                            message = "Wegen einem Bug in in Java 7+ in dieser JDownloader version koennen wir keine HTTPS Post Requests ausfuehren.\r\n";
                            message += "Wir haben eine Notloesung ergaenzt durch die man weiterhin diese JDownloader Version nutzen kann.\r\n";
                            message += "Bitte bedenke, dass HTTPS Post Requests als HTTP gesendet werden. Nutzung auf eigene Gefahr!\r\n";
                            message += "Falls du keine unverschluesselten Daten versenden willst, update bitte auf JDownloader 2!\r\n";
                            if (xSystem) {
                                message += "JDownloader 2 Installationsanleitung und Downloadlink: Klicke -OK- (per Browser oeffnen)\r\n ";
                            } else {
                                message += "JDownloader 2 Installationsanleitung und Downloadlink:\r\n" + new URL("http://board.jdownloader.org/showthread.php?t=37365") + "\r\n";
                            }
                        } else if ("es".equalsIgnoreCase(lng)) {
                            title = domain + " :: Java 7+ && HTTPS Solicitudes Post.";
                            message = "Debido a un bug en Java 7+, al utilizar esta versión de JDownloader, no se puede enviar correctamente las solicitudes Post en HTTPS\r\n";
                            message += "Por ello, hemos añadido una solución alternativa para que pueda seguir utilizando esta versión de JDownloader...\r\n";
                            message += "Tenga en cuenta que las peticiones Post de HTTPS se envían como HTTP. Utilice esto a su propia discreción.\r\n";
                            message += "Si usted no desea enviar información o datos desencriptados, por favor utilice JDownloader 2!\r\n";
                            if (xSystem) {
                                message += " Las instrucciones para descargar e instalar Jdownloader 2 se muestran a continuación: Hacer Click en -Aceptar- (El navegador de internet se abrirá)\r\n ";
                            } else {
                                message += " Las instrucciones para descargar e instalar Jdownloader 2 se muestran a continuación, enlace :\r\n" + new URL("http://board.jdownloader.org/showthread.php?t=37365") + "\r\n";
                            }
                        } else {
                            title = domain + " :: Java 7+ && HTTPS Post Requests.";
                            message = "Due to a bug in Java 7+ when using this version of JDownloader, we can not successfully send HTTPS Post Requests.\r\n";
                            message += "We have added a work around so you can continue to use this version of JDownloader...\r\n";
                            message += "Please be aware that HTTPS Post Requests are sent as HTTP. Use at your own discretion.\r\n";
                            message += "If you do not want to send unecrypted data, please upgrade to JDownloader 2!\r\n";
                            if (xSystem) {
                                message += "Jdownloader 2 install instructions and download link: Click -OK- (open in browser)\r\n ";
                            } else {
                                message += "JDownloader 2 install instructions and download link:\r\n" + new URL("http://board.jdownloader.org/showthread.php?t=37365") + "\r\n";
                            }
                        }
                        final int result = JOptionPane.showConfirmDialog(jd.gui.swing.jdgui.JDGui.getInstance().getMainFrame(), message, title, JOptionPane.CLOSED_OPTION, JOptionPane.CLOSED_OPTION);
                        if (xSystem && JOptionPane.OK_OPTION == result) {
                            CrossSystem.openURL(new URL("http://board.jdownloader.org/showthread.php?t=37365"));
                        }
                        RapidGatorNet.stableSucks.set(true);
                    } catch (final Throwable e) {
                    }
                }
            });
        } catch (final Throwable e) {
        }
    }

}