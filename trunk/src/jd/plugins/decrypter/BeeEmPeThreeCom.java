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
import jd.nutils.encoding.Encoding;
import jd.parser.Regex;
import jd.plugins.CryptedLink;
import jd.plugins.DecrypterException;
import jd.plugins.DecrypterPlugin;
import jd.plugins.DownloadLink;
import jd.plugins.PluginForDecrypt;

@DecrypterPlugin(revision = "$Revision$", interfaceVersion = 2, names = { "beemp3.com" }, urls = { "http://(www\\.)?beemp3s\\.org/download\\.php\\?file=\\d+\\&song=.+" }, flags = { 0 })
public class BeeEmPeThreeCom extends PluginForDecrypt {

    public BeeEmPeThreeCom(PluginWrapper wrapper) {
        super(wrapper);
    }

    public static Object        LOCK         = new Object();
    private static final String CAPTCHAREGEX = "\"(code\\.php\\?par=\\d+)\"";
    private static final String DONE         = "Done#\\|#";

    public ArrayList<DownloadLink> decryptIt(CryptedLink param, ProgressController progress) throws Exception {
        ArrayList<DownloadLink> decryptedLinks = new ArrayList<DownloadLink>();
        String parameter = param.toString();
        br.setFollowRedirects(true);
        br.getPage(parameter);
        if (br.containsHTML(">Error: This file has been removed|>Page Not Found<")) {
            logger.info("Link offline: " + parameter);
            return decryptedLinks;
        }

        /* Check for external hulkshare.com link */
        final String hulkshare = br.getRegex("show_url\\(\\'(http://(www\\.)?hulkshare\\.com/ap\\-[a-z0-9]+)\\'\\)").getMatch(0);
        if (hulkshare != null) {
            final DownloadLink fina = createDownloadlink(hulkshare.replace("/ap-", "/"));
            decryptedLinks.add(fina);
            return decryptedLinks;
        }

        final String finalFilename = br.getRegex("monetized_ad_client_song = \"([^<>\"]*?)\"").getMatch(0);
        String captchaUrl = null;
        boolean failed = true;
        String fileID = new Regex(parameter, "beemp3s\\.org/download\\.php\\?file=(\\d+)").getMatch(0);
        for (int i = 0; i <= 5; i++) {
            captchaUrl = br.getRegex(CAPTCHAREGEX).getMatch(0);
            if (captchaUrl == null) {
                return null;
            }
            captchaUrl = "http://beemp3s.org/" + captchaUrl;
            String code = getCaptchaCode(captchaUrl, param);
            br.getPage("http://beemp3s.org/chk_cd.php?id=" + fileID + "&code=" + code);
            if (!br.containsHTML(DONE)) {
                br.clearCookies("http://beemp3s.com");
                br.getPage(parameter);
                continue;
            }
            failed = false;
            break;
        }
        if (failed) {
            throw new DecrypterException(DecrypterException.CAPTCHA);
        }
        if (br.containsHTML("Error#\\|# File not found")) {
            final DownloadLink offline = createDownloadlink("directhttp://" + parameter);
            offline.setFinalFileName(new Regex(parameter, "\\&song=(.+)").getMatch(0) + ".mp3");
            offline.setAvailable(false);
            decryptedLinks.add(offline);
            return decryptedLinks;
        }
        final String finallink = br.getRegex("Done#\\|#(http://.+)").getMatch(0);
        if (finallink == null) {
            logger.warning("Decrypter broken for link: " + parameter);
            return null;
        }
        /**
         * Set filename if possible as filenames may be cut or broken if not set here
         */
        final DownloadLink dl = createDownloadlink("directhttp://" + finallink.trim());
        if (finalFilename != null) {
            dl.setFinalFileName(Encoding.htmlDecode(finalFilename) + ".mp3");
        }
        decryptedLinks.add(dl);

        return decryptedLinks;
    }

    /* NO OVERRIDE!! */
    public boolean hasCaptcha(CryptedLink link, jd.plugins.Account acc) {
        return true;
    }

}