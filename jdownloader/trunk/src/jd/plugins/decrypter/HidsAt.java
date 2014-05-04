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
import jd.http.Browser;
import jd.http.URLConnectionAdapter;
import jd.parser.Regex;
import jd.parser.html.HTMLParser;
import jd.plugins.CryptedLink;
import jd.plugins.DecrypterException;
import jd.plugins.DecrypterPlugin;
import jd.plugins.DownloadLink;
import jd.plugins.LinkStatus;
import jd.plugins.PluginException;
import jd.plugins.PluginForDecrypt;
import jd.utils.JDUtilities;
import jd.utils.locale.JDL;

@DecrypterPlugin(revision = "$Revision$", interfaceVersion = 2, names = { "hides.at" }, urls = { "https?://(www\\.)?hides\\.at/(link/)?[a-f0-9]{32}" }, flags = { 0 })
public class HidsAt extends PluginForDecrypt {

    public HidsAt(PluginWrapper wrapper) {
        super(wrapper);
    }

    private final String  CAPTCHATEXT = "securimage-1\\.0\\.3\\.1/securimage_show\\.php";
    private static String agent       = null;

    public ArrayList<DownloadLink> decryptIt(CryptedLink param, ProgressController progress) throws Exception {
        ArrayList<DownloadLink> decryptedLinks = new ArrayList<DownloadLink>();
        if (agent == null) {
            JDUtilities.getPluginForHost("mediafire.com");
            agent = jd.plugins.hoster.MediafireCom.stringUserAgent();
        }
        br.getHeaders().put("User-Agent", agent);
        br.setFollowRedirects(true);
        String parameter = param.toString();
        br.getPage(parameter);
        if (br.containsHTML("Error loading list or invalid list")) throw new DecrypterException(JDL.L("plugins.decrypt.errormsg.unavailable", "Perhaps wrong URL or the download is not available anymore."));
        if (!br.containsHTML(CAPTCHATEXT)) return null;
        String linkID = new Regex(parameter, "hides\\.at/(link/)?(.+)").getMatch(1);

        Browser br2 = br.cloneBrowser();
        URLConnectionAdapter con = null;
        try {
            con = br2.openGetConnection("http://hides.at/include/securimage-1.0.3.1/securimage_show.php");
            if (con.getContentType().contains("html")) {
                br2.followConnection();
                if (br2.containsHTML("<b>Fatal error</b>:"))
                    br.getPage(parameter + "?captcha_code=+&hash=" + linkID);
                else
                    throw new PluginException(LinkStatus.ERROR_PLUGIN_DEFECT);
            } else {
                int repeat = 4;
                for (int i = 0; i <= repeat; i++) {
                    String code = getCaptchaCode("http://hides.at/include/securimage-1.0.3.1/securimage_show.php", param);
                    Browser cap = br.cloneBrowser();
                    cap.getPage("http://hides.at/link/" + linkID + "?captcha_code=" + code + "&hash=" + linkID + "&btnSubmit=Submit");
                    if (i + 1 == repeat && cap.containsHTML(CAPTCHATEXT)) {
                        throw new DecrypterException(DecrypterException.CAPTCHA);
                    } else if (cap.containsHTML(CAPTCHATEXT)) {
                        continue;
                    } else {
                        br = cap.cloneBrowser();
                        break;
                    }
                }
            }
        } catch (final Throwable e) {
            if (e instanceof PluginException) throw (PluginException) e;
            if (e instanceof DecrypterException) throw (DecrypterException) e;
        } finally {
            try {
                con.disconnect();
            } catch (final Exception e) {
            }
        }
        String list = br.getRegex("id=\"list2Copy\" style=\"display: none;\">(.*?)</div>").getMatch(0);
        if (list == null) return null;
        String[] links = HTMLParser.getHttpLinks(list, "");
        if (links == null || links.length == 0) return null;
        for (String dl : links)
            decryptedLinks.add(createDownloadlink(dl));
        return decryptedLinks;
    }

    /* NO OVERRIDE!! */
    public boolean hasCaptcha(CryptedLink link, jd.plugins.Account acc) {
        return true;
    }

}