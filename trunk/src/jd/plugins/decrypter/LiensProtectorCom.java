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

import java.io.File;
import java.util.ArrayList;
import java.util.Random;

import jd.PluginWrapper;
import jd.controlling.ProgressController;
import jd.nutils.encoding.Encoding;
import jd.parser.Regex;
import jd.plugins.CryptedLink;
import jd.plugins.DecrypterException;
import jd.plugins.DecrypterPlugin;
import jd.plugins.DownloadLink;
import jd.plugins.PluginForDecrypt;
import jd.plugins.PluginForHost;
import jd.utils.JDUtilities;

@DecrypterPlugin(revision = "$Revision$", interfaceVersion = 2, names = { "liens-protector.com" }, urls = { "http://(www\\.)?liens\\-protector\\.com/[A-Za-z0-9\\-_]+\\.html" }, flags = { 0 })
public class LiensProtectorCom extends PluginForDecrypt {

    public LiensProtectorCom(PluginWrapper wrapper) {
        super(wrapper);
    }

    // All similar: IleProtectCom, ExtremeProtectCom, TopProtectNet AND MORE
    /* DecrypterScript_linkid=_linkcheck.php */
    private static final String DOMAIN         = "ileprotect.com";
    private static final String RECAPTCHATEXT  = "api\\.recaptcha\\.net";
    private static final String RECAPTCHATEXT2 = "google\\.com/recaptcha/api/challenge";
    private boolean             skipcaptcha    = true;

    public ArrayList<DownloadLink> decryptIt(CryptedLink param, ProgressController progress) throws Exception {
        ArrayList<DownloadLink> decryptedLinks = new ArrayList<DownloadLink>();
        String parameter = param.toString();
        br.setFollowRedirects(true);
        br.getPage(parameter);
        if (!skipcaptcha) {
            boolean failed = true;
            for (int i = 0; i <= 5; i++) {
                if (!br.containsHTML(RECAPTCHATEXT) && !br.containsHTML(RECAPTCHATEXT2)) {
                    return null;
                }
                PluginForHost recplug = JDUtilities.getPluginForHost("DirectHTTP");
                jd.plugins.hoster.DirectHTTP.Recaptcha rc = ((jd.plugins.hoster.DirectHTTP) recplug).getReCaptcha(br);
                rc.parse();
                rc.load();
                File cf = rc.downloadCaptcha(getLocalCaptchaFile());
                String c;
                if (skipcaptcha) {
                    c = "";
                } else {
                    c = getCaptchaCode(cf, param);
                }
                br.postPage("http://" + DOMAIN + "/showlinks.php", "recaptcha_challenge_field=" + rc.getChallenge() + "&recaptcha_response_field=" + Encoding.urlEncode(c) + "&linkid=" + new Regex(parameter, "([A-Za-z0-9]+)$").getMatch(0) + "&x=" + Integer.toString(new Random().nextInt(100)) + "&y=" + Integer.toString(new Random().nextInt(100)));
                if (br.containsHTML("(The security code is <font color=\\'red\\'>incorrect</font>|The CAPTCHA wasn\\'t entered correctly)")) {
                    br.getPage(parameter);
                    skipcaptcha = false;
                    continue;
                }
                failed = false;
                break;
            }
            if (failed) {
                throw new DecrypterException(DecrypterException.CAPTCHA);
            }
        }
        String[] links = br.getRegex("target=_blank>(https?://[^<>\"\\']+)").getColumn(0);
        if (links == null || links.length == 0) {
            if (br.containsHTML("href= target=_blank></a><br></br><a")) {
                logger.info("Link offline: " + parameter);
                return decryptedLinks;
            }
            logger.warning("Decrypter broken for link: " + parameter);
            return null;
        }
        for (String singleLink : links) {
            if (!singleLink.contains(DOMAIN + "/")) {
                decryptedLinks.add(createDownloadlink(singleLink));
            }
        }
        return decryptedLinks;
    }

    /* NO OVERRIDE!! */
    public boolean hasCaptcha(CryptedLink link, jd.plugins.Account acc) {
        return false;
    }

}