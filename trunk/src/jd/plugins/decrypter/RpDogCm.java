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

package jd.plugins.decrypter;

import java.io.File;
import java.util.ArrayList;

import jd.PluginWrapper;
import jd.controlling.ProgressController;
import jd.http.Browser;
import jd.http.URLConnectionAdapter;
import jd.parser.html.Form;
import jd.plugins.CryptedLink;
import jd.plugins.DecrypterException;
import jd.plugins.DecrypterPlugin;
import jd.plugins.DownloadLink;
import jd.plugins.PluginForDecrypt;
import jd.utils.locale.JDL;

@DecrypterPlugin(revision = "$Revision$", interfaceVersion = 2, names = { "rapidog.com" }, urls = { "http://[\\w\\.]*?rapidog\\.com/search/file\\.php\\?file_id=[0-9]+" }, flags = { 0 })
public class RpDogCm extends PluginForDecrypt {

    public RpDogCm(PluginWrapper wrapper) {
        super(wrapper);
    }

    public ArrayList<DownloadLink> decryptIt(CryptedLink param, ProgressController progress) throws Exception {
        ArrayList<DownloadLink> decryptedLinks = new ArrayList<DownloadLink>();
        String parameter = param.toString();
        br.setFollowRedirects(true);
        br.getPage(parameter);
        /* Error handling */
        if (br.containsHTML("<title> rapidshare</title>")) {
            logger.warning("The requested document was not found on this server.");
            logger.warning(JDL.L("plugins.decrypt.errormsg.unavailable", "Perhaps wrong URL or the download is not available anymore."));
            return null;
        }
        for (int i = 0; i <= 2; i++) {
            Form crptform = br.getFormbyProperty("name", "form");
            if (crptform == null) return null;
            String captchalink = null;
            if (br.containsHTML("/captcha.php")) captchalink = "http://rapidog.com/captcha.php";
            if (captchalink == null) return null;
            Browser brc = br.cloneBrowser();
            URLConnectionAdapter con = brc.openGetConnection(captchalink);
            File file = this.getLocalCaptchaFile();
            Browser.download(file, con);
            String code = getCaptchaCode(file, param);
            crptform.put("captcha", code);
            br.submitForm(crptform);
            if (!br.containsHTML("Please enter the correct security code")) {
                break;
            }
            br.getPage(parameter);
        }
        if (br.containsHTML("Please enter the correct security code")) throw new DecrypterException(DecrypterException.CAPTCHA);
        String decryptedlink = br.getRegex("<h2><a href=\"(.*?)\"").getMatch(0);
        if (decryptedlink == null) {
            decryptedlink = br.getRegex("</h3><a href=\"(.*?)\"").getMatch(0);
        }
        if (decryptedlink == null) return null;
        decryptedLinks.add(createDownloadlink(decryptedlink.trim()));

        return decryptedLinks;
    }

    /* NO OVERRIDE!! */
    public boolean hasCaptcha(CryptedLink link, jd.plugins.Account acc) {
        return true;
    }

}