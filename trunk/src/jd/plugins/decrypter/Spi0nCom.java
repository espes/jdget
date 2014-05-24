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
import jd.plugins.CryptedLink;
import jd.plugins.DecrypterPlugin;
import jd.plugins.DownloadLink;
import jd.plugins.PluginForDecrypt;

@DecrypterPlugin(revision = "$Revision$", interfaceVersion = 2, names = { "spi0n.com" }, urls = { "http://www\\.spi0n\\.com/[a-z0-9\\-_]+" }, flags = { 0 })
public class Spi0nCom extends PluginForDecrypt {

    public Spi0nCom(PluginWrapper wrapper) {
        super(wrapper);
    }

    private static final String INVALIDLINKS = "http://www\\.spi0n\\.com/favicon";

    public ArrayList<DownloadLink> decryptIt(CryptedLink param, ProgressController progress) throws Exception {
        ArrayList<DownloadLink> decryptedLinks = new ArrayList<DownloadLink>();
        String parameter = param.toString();
        if (parameter.matches(INVALIDLINKS)) {
            final DownloadLink offline = createDownloadlink("directhttp://" + parameter);
            offline.setAvailable(false);
            decryptedLinks.add(offline);
            return decryptedLinks;
        }
        br.getPage(parameter);
        if (!br.containsHTML("id=\"container\"")) {
            logger.info("Link offline (no video on this page?!): " + parameter);
            final DownloadLink offline = createDownloadlink("directhttp://" + parameter);
            offline.setAvailable(false);
            decryptedLinks.add(offline);
            return decryptedLinks;
        }
        String finallink = br.getRegex("\"(http://(www\\.)?dailymotion\\.com/video/[A-Za-z0-9\\-_]+)\"").getMatch(0);
        if (finallink == null) {
            finallink = br.getRegex("\"((http:)?//(www\\.)?youtube\\.com/embed/[^<>\"/]+)\"").getMatch(0);
            if (finallink != null && !finallink.startsWith("http:")) {
                finallink = "http:" + finallink;
            }
        }
        /* Sometimes they host videos on their own servers */
        if (finallink == null) {
            finallink = br.getRegex("\"file\":\"(http://(www\\.)?spi0n\\.com/wp\\-content/uploads[^<>\"]*?)\"").getMatch(0);
            if (finallink != null) {
                finallink = "directhttp://" + finallink.replace("http://www.", "http://");
            }
        }
        if (finallink == null) {
            logger.warning("Decrypter broken for link: " + parameter);
            return null;
        }
        decryptedLinks.add(createDownloadlink(finallink));

        return decryptedLinks;
    }

    public boolean hasCaptcha(CryptedLink link, jd.plugins.Account acc) {
        return false;
    }

}