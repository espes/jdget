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
import jd.controlling.ProgressController;
import jd.http.RandomUserAgent;
import jd.parser.Regex;
import jd.parser.html.HTMLParser;
import jd.plugins.CryptedLink;
import jd.plugins.DecrypterPlugin;
import jd.plugins.DownloadLink;
import jd.plugins.PluginForDecrypt;

@DecrypterPlugin(revision = "$Revision$", interfaceVersion = 2, names = { "adf.ly" }, urls = { "http://(www\\.)?(adf\\.ly|9\\.bb|j\\.gs|q\\.gs|urlm\\.in)/.+" }, flags = { 0 })
public class AdfLy extends PluginForDecrypt {

    public AdfLy(PluginWrapper wrapper) {
        super(wrapper);
    }

    private static final Object LOCK = new Object();
    private static String       ua   = RandomUserAgent.generate();

    public ArrayList<DownloadLink> decryptIt(CryptedLink param, ProgressController progress) throws Exception {
        ArrayList<DownloadLink> decryptedLinks = new ArrayList<DownloadLink>();
        final String parameter = param.toString().replace("www.", "");
        br.setFollowRedirects(false);
        br.setReadTimeout(3 * 60 * 1000);
        br.getHeaders().put("User-Agent", ua);
        synchronized (LOCK) {
            br.getPage(parameter);
            if (parameter.contains("9.bb/") && br.getRedirectLocation() != null) {
                br.getPage(br.getRedirectLocation());
            }
        }
        if (br.containsHTML("(<b style=\"font\\-size: 20px;\">Sorry the page you are looking for does not exist|>404 Not Found<)")) {
            logger.info("adf.ly link offline: " + parameter);
            return decryptedLinks;
        }
        String finallink = br.getRedirectLocation();
        if (finallink == null) {
            finallink = br.getRegex("\\.attr\\((\"|\\')href(\"|\\'), \\'(.*?)\\'\\)").getMatch(2);
        }
        if (finallink == null) {
            finallink = br.getRegex("window\\.location = ('|\")(.*?)('|\");").getMatch(1);
        }
        if (finallink == null) {
            finallink = br.getRegex("close_bar.*?self\\.location = \\'(.*?)\\';").getMatch(0);
        }
        // Use this because they often change the page
        final String[] lol = HTMLParser.getHttpLinks(br.toString(), "");
        for (final String aLink : lol) {
            if (!new Regex(aLink, "http://(www\\.)?(adf\\.ly|9\\.bb|j\\.gs|q\\.gs|urlm\\.in)/.+").matches() && !aLink.contains("/javascript/")) {
                decryptedLinks.add(createDownloadlink(aLink));
            }
        }
        for (int i = 0; i <= 2; i++) {
            String extendedProtectionPage = br.getRegex("\\'(https?://adf\\.ly/go/[^<>\"\\']*?)\\'").getMatch(0);
            if (extendedProtectionPage == null) {
                extendedProtectionPage = br.getRegex("var url = '(/go/[^<>\"\\']*?)\\'").getMatch(0);
                if (extendedProtectionPage != null) {
                    extendedProtectionPage = "http://adf.ly" + extendedProtectionPage;
                }
            }
            if (extendedProtectionPage != null) {
                int wait = 7;
                String waittime = br.getRegex("var countdown = (\\d+);").getMatch(0);
                if (waittime != null && Integer.parseInt(waittime) <= 20) wait = Integer.parseInt(waittime);
                sleep(wait * 1000l, param);
                br.getPage(extendedProtectionPage);
                String tempLink = br.getRedirectLocation();
                if (tempLink != null) {
                    tempLink = tempLink.replace("www.", "");
                    // Redirected to the same link...try again
                    if (tempLink.equals(parameter)) {
                        continue;
                        // They blocked us for a short time, wait and try again
                    } else if (tempLink.contains("adf.ly/locked/")) {
                        wait = 8;
                        waittime = br.getRegex("var countdown = (\\d+);").getMatch(0);
                        if (waittime != null && Integer.parseInt(waittime) <= 20) wait = Integer.parseInt(waittime);
                        sleep(wait * 1000l, param);
                        br.getPage(parameter);
                        continue;
                    } else {
                        // We found a link to continue with
                        finallink = tempLink;
                    }
                } else {
                    // Everything should have worked correctly, try to get final
                    // link
                    finallink = br.getRegex("<META HTTP\\-EQUIV=\"Refresh\" CONTENT=\"\\d+; URL=(http://[^<>\"\\']+)\"").getMatch(0);
                }
            }
            break;
        }
        if (finallink != null) {
            /* we found the wanted link, so lets clear results of htmlparser */
            decryptedLinks.clear();
            decryptedLinks.add(createDownloadlink(finallink));
        } else {
            logger.warning("adf.ly single regex broken for link: " + parameter);
        }

        return decryptedLinks;
    }

}
