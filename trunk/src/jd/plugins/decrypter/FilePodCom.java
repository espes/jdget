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
import jd.parser.Regex;
import jd.plugins.CryptedLink;
import jd.plugins.DecrypterException;
import jd.plugins.DecrypterPlugin;
import jd.plugins.DownloadLink;
import jd.plugins.PluginForDecrypt;

@DecrypterPlugin(revision = "$Revision$", interfaceVersion = 2, names = { "filepod.com" }, urls = { "http://(www\\.)?filepod\\.com/download/download\\.php\\?f=\\d+" }, flags = { 0 })
public class FilePodCom extends PluginForDecrypt {

    public FilePodCom(PluginWrapper wrapper) {
        super(wrapper);
    }

    public static Object LOCK = new Object();

    public ArrayList<DownloadLink> decryptIt(CryptedLink param, ProgressController progress) throws Exception {
        ArrayList<DownloadLink> decryptedLinks = new ArrayList<DownloadLink>();
        String parameter = param.toString();
        br.setCookiesExclusive(false);
        br.setFollowRedirects(true);
        // Try to skip cookie
        br.setCookie("http://filepod.com", "validuser", "1");
        String fileID = new Regex(parameter, "(\\d+)$").getMatch(0);
        if (fileID == null) return null;
        // This host doesn't like simultan captcha requests
        synchronized (LOCK) {
            br.getPage(parameter);
            if (br.containsHTML(">404 Not Found<")) {
                logger.info("Link offline: " + parameter);
                return decryptedLinks;
            }
            if (br.containsHTML("captcha\\.php")) {
                boolean failed = true;
                for (int i = 0; i <= 3; i++) {
                    String code = getCaptchaCode("http://filepod.com/download/captcha.php", param);
                    br.postPage("http://filepod.com/download/captchacontrol.php", "captcha=" + code + "&f=" + fileID + "&submit_com=Submit" + code);
                    if (br.containsHTML("captcha\\.php")) continue;
                    failed = false;
                    break;
                }
                if (failed) throw new DecrypterException(DecrypterException.CAPTCHA);
            }
        }
        final String finallink = br.getRegex(">Download <a href=\"([^<>\"]*?)\"").getMatch(0);
        if (finallink == null) return null;
        decryptedLinks.add(createDownloadlink(finallink));

        return decryptedLinks;
    }

}
