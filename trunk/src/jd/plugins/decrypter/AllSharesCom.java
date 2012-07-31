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
import jd.plugins.CryptedLink;
import jd.plugins.DecrypterException;
import jd.plugins.DecrypterPlugin;
import jd.plugins.DownloadLink;
import jd.plugins.FilePackage;
import jd.plugins.PluginForDecrypt;

@DecrypterPlugin(revision = "$Revision$", interfaceVersion = 2, names = { "all-shares.com" }, urls = { "http://(www\\.)?all\\-shares\\.com/download/.*?\\.html" }, flags = { 0 })
public class AllSharesCom extends PluginForDecrypt {

    public AllSharesCom(PluginWrapper wrapper) {
        super(wrapper);
    }

    private static final String CAPTCHASTRING = "captcha\\.php\\?";

    public ArrayList<DownloadLink> decryptIt(CryptedLink param, ProgressController progress) throws Exception {
        ArrayList<DownloadLink> decryptedLinks = new ArrayList<DownloadLink>();
        String parameter = param.toString();
        br.getPage(parameter);
        if (br.containsHTML(CAPTCHASTRING)) {
            for (int i = 0; i <= 3; i++) {
                String captchaLink = br.getRegex(">Enter code to get links\\!</span>[\t\n\r ]+<br/><br/>[\t\n\r ]+<img src=\"(/.*?)\"").getMatch(0);
                if (captchaLink == null) captchaLink = br.getRegex("\"(/captcha/captcha\\.php\\?.*?)\"").getMatch(0);
                if (captchaLink == null) {
                    logger.warning("Decrypter broken for link: " + parameter);
                    return null;
                }
                captchaLink = "http://all-shares.com" + captchaLink.replace("amp;", "");
                String code = getCaptchaCode(captchaLink, param);
                br.postPage(parameter, "code=" + Encoding.urlEncode(code));
                if (br.containsHTML(CAPTCHASTRING)) continue;
                break;
            }
            if (br.containsHTML(CAPTCHASTRING)) throw new DecrypterException(DecrypterException.CAPTCHA);
        }
        final String fpName = br.getRegex("\">Download <b>(.*?)</b></a>").getMatch(0);
        String[] links = br.getRegex("\\?url=([^<>\"\\']*?)\\&").getColumn(0);
        if (links == null || links.length == 0) {
            logger.warning("Decrypter broken for link: " + parameter);
            return null;
        }
        /** Links are base 64 encoded */
        for (String singleLink : links)
            decryptedLinks.add(createDownloadlink(Encoding.Base64Decode(Encoding.htmlDecode(singleLink))));
        if (fpName != null) {
            FilePackage fp = FilePackage.getInstance();
            fp.setName(Encoding.htmlDecode(fpName.trim()));
            fp.addLinks(decryptedLinks);
        }
        return decryptedLinks;
    }

}
