//jDownloader - Downloadmanager
//Copyright (C) 2011  JD-Team support@jdownloader.org
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
import jd.plugins.DecrypterPlugin;
import jd.plugins.DownloadLink;
import jd.plugins.FilePackage;
import jd.plugins.PluginForDecrypt;

@DecrypterPlugin(revision = "$Revision: 14951 $", interfaceVersion = 2, names = { "thekollection.com" }, urls = { "http://((www|dubstep|electro|indie)\\.)?thekollection\\.com/[\\w\\-]+/" }, flags = { 0 })
public class TKlnCm extends PluginForDecrypt {

    // https not currently available

    public TKlnCm(PluginWrapper wrapper) {
        super(wrapper);
    }

    public ArrayList<DownloadLink> decryptIt(CryptedLink param, ProgressController progress) throws Exception {
        ArrayList<DownloadLink> decryptedLinks = new ArrayList<DownloadLink>();
        String parameter = param.toString().replace("http://www.", "http://");
        if (parameter.contains(".com/artist") || parameter.contains(".com/category")) return null;
        br.setCookie("http://thekollection.com", "lang", "en");
        br.setCookiesExclusive(true);
        br.setFollowRedirects(true);
        br.getPage(parameter);
        if (br.containsHTML("<p>404 File Not Found\\.</p>")) {
            logger.warning("Invalid URL or the contents no longer exists: " + parameter);
            return decryptedLinks;
        }
        String fpName = br.getRegex("h1 id=\"post\\-\\d+\" class=\"entry\\-title\">(.*?)</h1>").getMatch(0);
        if (fpName == null) br.getRegex("<title>The Kollection  \\&raquo\\; (.*?)</title>").getMatch(0);
        String[] links = br.getRegex("<div class=\"so\\-player\">[\r\n\t]+<a href=\"(.*?)\" class=\"wpaudio\">").getColumn(0);
        if (links == null || links.length == 0) return null;
        if (links != null && links.length != 0) {
            for (String dl : links)
                decryptedLinks.add(createDownloadlink(dl));
        }
        if (fpName != null) {
            FilePackage fp = FilePackage.getInstance();
            fp.setName(Encoding.htmlDecode(fpName.trim()));
            fp.addLinks(decryptedLinks);
        }
        return decryptedLinks;
    }
}