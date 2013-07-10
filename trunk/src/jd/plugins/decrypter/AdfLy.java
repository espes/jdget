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

package jd.plugins.decrypter;

import java.util.ArrayList;

import jd.PluginWrapper;
import jd.config.SubConfiguration;
import jd.controlling.ProgressController;
import jd.gui.UserIO;
import jd.http.Browser;
import jd.nutils.encoding.Encoding;
import jd.parser.Regex;
import jd.parser.html.HTMLParser;
import jd.plugins.CryptedLink;
import jd.plugins.DecrypterPlugin;
import jd.plugins.DownloadLink;
import jd.plugins.PluginForDecrypt;
import jd.utils.locale.JDL;

@DecrypterPlugin(revision = "$Revision$", interfaceVersion = 2, names = { "adf.ly" }, urls = { "https?://(www\\.)?(adf\\.ly|9\\.bb|j\\.gs|q\\.gs|urlm\\.in)/(?!link\\-deleted\\.php|index|login)[^<>\r\n\t]+" }, flags = { 0 })
@SuppressWarnings("deprecation")
public class AdfLy extends PluginForDecrypt {

    public AdfLy(PluginWrapper wrapper) {
        super(wrapper);
    }

    private boolean       supportsHTTPS = true;
    private String        protocol      = null;
    private final String  HOSTS         = "https?://(www\\.)?(adf\\.ly|9\\.bb|j\\.gs|q\\.gs|urlm\\.in)";
    private static Object LOCK          = new Object();

    public ArrayList<DownloadLink> decryptIt(CryptedLink param, ProgressController progress) throws Exception {
        ArrayList<DownloadLink> decryptedLinks = new ArrayList<DownloadLink>();
        final String parameter = param.toString().replace("www.", "");
        // imported protocol choice
        protocol = new Regex(parameter, "(https?://)").getMatch(0);
        // poll plugin setting for default protocol, if not set ask the user.
        protocol = getDefaultProtocol() + "://";

        br.setFollowRedirects(false);
        br.setReadTimeout(3 * 60 * 1000);

        if (parameter.matches(HOSTS + "/\\d+/(http|ftp).+")) {
            String linkInsideLink = new Regex(parameter, HOSTS + "/\\d+/(.+)").getMatch(2);
            linkInsideLink = protocol + linkInsideLink;
            if (!linkInsideLink.matches(HOSTS + "/.+")) {
                decryptedLinks.add(createDownloadlink(linkInsideLink));
                return decryptedLinks;
            }
        }
        br.getHeaders().put("User-Agent", "Mozilla/5.0 (Windows NT 6.1; WOW64; rv:17.0) Gecko/20100101 Firefox/17.0");
        boolean skipWait = true;
        String finallink = null;
        for (int i = 0; i <= 2; i++) {
            synchronized (LOCK) {
                br.getPage(parameter.replaceFirst("https?://", protocol));
                if (parameter.contains("9.bb/") && br.getRedirectLocation() != null) {
                    br.getPage(br.getRedirectLocation());
                }
            }
            if (br.containsHTML("(<b style=\"font\\-size: 20px;\">Sorry the page you are looking for does not exist|>404 Not Found<|>Not Found<|>Sorry, but the requested page was not found)") || (br.getRedirectLocation() != null && (br.getRedirectLocation().contains("ink-deleted.php") || br.getRedirectLocation().contains("/suspended")))) {
                logger.info("adf.ly link offline: " + parameter);
                return decryptedLinks;
            }
            if (br.containsHTML("Sorry, there has been a problem\\.")) {
                logger.info("adf.ly link offline: " + parameter);
                return decryptedLinks;
            }
            if (br.getRedirectLocation() != null && br.getRedirectLocation().matches("https?://adf\\.ly/?")) {
                logger.info("adf.ly link offline: " + parameter);
                return decryptedLinks;
            }
            /* javascript vars 20130328 */
            String countdown = getWaittime();
            // they also have secondary zzz variable within 'function adf_counter()', but it's the same.
            String zzz = br.getRegex("var zzz\\s?+=\\s?+'?([\\d,\\.]+|http[^'\"]+)'?;").getMatch(0);
            if (zzz != null && zzz.matches("(https?|ftp)://.+")) finallink = zzz;
            String easyUrl = br.getRegex("var easyUrl\\s?+=\\s?+'?(true|false)'?;").getMatch(0);
            String url = br.getRegex("var url\\s?+=\\s?+'?([^\';]+)'?;").getMatch(0);

            if (finallink == null) {
                finallink = br.getRedirectLocation();
            }
            if (finallink == null) {
                finallink = br.getRegex("\\.attr\\((\"|\\')href(\"|\\'), \\'(.*?)\\'\\)").getMatch(2);
            }
            if (finallink == null) {
                finallink = br.getRegex("window\\.location = ('|\")(.*?)('|\");").getMatch(1);
                if (finallink != null && finallink.contains("/noscript.php")) finallink = null;
            }
            if (finallink == null) {
                finallink = br.getRegex("close_bar.*?self\\.location = \\'(.*?)\\';").getMatch(0);
            }
            /* 20130130 */
            if (finallink == null && (countdown != null && zzz != null && easyUrl != null)) {
                if (countdown.equalsIgnoreCase("7") && easyUrl.equalsIgnoreCase("false")) {
                    br.postPage("/shortener/go", "zzz=" + Encoding.urlEncode(zzz));
                    finallink = br.getRegex("(http[^\"']+)").getMatch(0);
                }
            }
            /* old stuff still exists! tested and working as of 20130328 */
            if (finallink != null && (!finallink.startsWith("/") && finallink.matches(HOSTS + ".+"))) {
                String extendedProtectionPage = br.getRegex("(" + HOSTS + "/go(/|\\.php\\?)[^<>\"\\']+)").getMatch(0);
                if (extendedProtectionPage == null) {
                    extendedProtectionPage = br.getRegex("(/go(/|\\.php\\?)[^<>\"\\']+)").getMatch(0);
                    if (extendedProtectionPage != null) {
                        extendedProtectionPage = protocol + "adf.ly" + extendedProtectionPage;
                    }
                }
                if (extendedProtectionPage == null)
                    break;
                else {
                    int wait = 7;
                    String waittime = getWaittime();
                    // because of possible page action via ajax request, we use old wait time.
                    if (waittime == null) waittime = countdown;
                    if (waittime != null && Integer.parseInt(waittime) <= 20) wait = Integer.parseInt(waittime);
                    if (skipWait) {
                        skipWait();
                        // Wait a seconds. Not waiting can cause the skipWait feature to fail
                        sleep(1 * 10001l, param);
                    } else {
                        sleep(wait * 1000l, param);
                    }
                    br.getPage(extendedProtectionPage);
                    String tempLink = br.getRedirectLocation();
                    if (tempLink != null) {
                        tempLink = tempLink.replace("www.", "");
                        // Redirected to the same link or blocked...try again
                        if (tempLink.replaceAll("https?://", "").equals(parameter.replaceAll("https?://", "")) || tempLink.contains("adf.ly/locked/") || tempLink.contains("adf.ly/blocked")) {
                            logger.info("Blocked, re-trying with waittime...");
                            skipWait = false;
                            try {
                                br.clearCookies("http://adf.ly/");
                            } catch (final Exception e) {

                            }
                            continue;
                        } else {
                            // We found a link to continue with
                            finallink = tempLink;
                            break;
                        }
                    } else {
                        // Everything should have worked correctly, try to get final link
                        finallink = br.getRegex("<META HTTP\\-EQUIV=\"Refresh\" CONTENT=\"\\d+; URL=(https?://[^<>\"\\']+)\"").getMatch(0);
                        break;
                    }
                }
            } else {
                break;
            }
        }
        if (finallink != null && finallink.contains("/link-deleted.php")) {
            logger.info(parameter + " has been removed from adf.ly service provider.");
        } else if (finallink != null) {
            decryptedLinks.add(createDownloadlink(finallink.replace("\\", "")));
        } else {
            logger.warning("adf.ly single regex broken for link: " + parameter);
            logger.info("Adding all available links on page");
            // Use this because they often change the page
            final String[] lol = HTMLParser.getHttpLinks(br.toString(), "");
            for (final String aLink : lol) {
                if (!new Regex(aLink, HOSTS + "/.+").matches() && !aLink.contains("/javascript/")) {
                    decryptedLinks.add(createDownloadlink(aLink));
                }
            }
        }

        return decryptedLinks;
    }

    private String getWaittime() {
        return br.getRegex("var countdown\\s?+=\\s?+'?(\\d+)'?;").getMatch(0);
    }

    private void skipWait() {
        final Browser brAds = br.cloneBrowser();
        brAds.setConnectTimeout(5 * 1000);
        brAds.setReadTimeout(5 * 1000);
        final String[] skpWaitLinks = { "cdn.adf.ly/css/adfly_1.css", "cdn.adf.ly/js/adfly.js", "cdn.adf.ly/images/logo_fb.png", "cdn.adf.ly/images/skip_ad/en.png", "adf.ly/favicon.ico", "cdn.adf.ly/images/ad_top_bg.png", "adf.ly/omnigy7425325410.swf", "adf.ly/holder.php" };
        for (final String skWaitLink : skpWaitLinks) {
            try {
                brAds.openGetConnection(protocol + skWaitLink);
            } catch (final Exception e) {
            }
        }
    }

    /**
     * Issue the user with a dialog prompt and asks them to select a default request protocol. Saves users preference for future
     * communication requests!<br/>
     * <br/>
     * Decrypter Template: Default Request Protocol.
     * 
     * @return default request protocol
     * @author raztoki
     * */
    private String getDefaultProtocol() {
        String defaultProtocol = null;
        if (!supportsHTTPS) {
            defaultProtocol = "http";
        } else {
            SubConfiguration config = null;
            try {
                config = getPluginConfig();
                defaultProtocol = config.getStringProperty("defaultProtocol", null);
                if (defaultProtocol == null) {
                    String lng = System.getProperty("user.language");
                    String message = null;
                    String title = null;
                    if ("de".equalsIgnoreCase(lng)) {
                        title = "Wähle bitte Dein Standard Request Protokoll aus.";
                        message = "Dies ist eine einmalige Auswahl. Einmal gespeichert, nutzt der JDownloader Dein\r\ngewähltes Standard Protokoll auch für alle zukünftigen Verbindungen zu " + this.getHost() + ".";
                    } else {
                        title = "Please select your default request Protocol.";
                        message = "This is a once off choice. Once saved, JDownloader will reuse\r\n your default Protocol for all future requests to " + this.getHost() + ".";
                    }
                    String[] select = new String[] { "http (insecure)", "https (secure)" };
                    int userSelect = UserIO.getInstance().requestComboDialog(0, JDL.L("plugins.decrypter.adfly.SelectDefaultProtocolTitle", title), JDL.L("plugins.decrypter.adfly.SelectDefaultProtocolMessage", message), select, 0, null, null, null, null);
                    if (userSelect != -1) {
                        defaultProtocol = userSelect == 0 ? "http" : "https";
                    } else {
                        // user cancelled! or dialog timed out! respect imported protocol, as fail over!
                        defaultProtocol = protocol;
                        config = null;
                    }
                } else {
                    // no need to save again, and again..
                    config = null;
                }
            } catch (final Throwable e) {
            } finally {
                if (config != null) {
                    config.setProperty("defaultProtocol", defaultProtocol);
                    config.save();
                }
            }
        }
        return defaultProtocol;
    }

    /* NO OVERRIDE!! */
    public boolean hasCaptcha(CryptedLink link, jd.plugins.Account acc) {
        return false;
    }

}