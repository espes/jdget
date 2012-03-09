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
import java.util.Random;

import jd.PluginWrapper;
import jd.controlling.ProgressController;
import jd.nutils.encoding.Encoding;
import jd.parser.Regex;
import jd.plugins.CryptedLink;
import jd.plugins.DecrypterPlugin;
import jd.plugins.DownloadLink;
import jd.plugins.FilePackage;
import jd.plugins.PluginForDecrypt;

@DecrypterPlugin(revision = "$Revision$", interfaceVersion = 2, names = { "top-protect.net" }, urls = { "http://(www\\.)?top\\-protect\\.net/linkidwoc\\.php\\?linkid=[a-z]+" }, flags = { 0 })
public class TopProtectNet extends PluginForDecrypt {

    public TopProtectNet(PluginWrapper wrapper) {
        super(wrapper);
    }

    public ArrayList<DownloadLink> decryptIt(CryptedLink param, ProgressController progress) throws Exception {
        ArrayList<DownloadLink> decryptedLinks = new ArrayList<DownloadLink>();
        String parameter = param.toString();
        br.setFollowRedirects(true);
        br.postPage("http://top-protect.net/linkid.php", "linkid=" + new Regex(parameter, "top\\-protect\\.net/linkidwoc\\.php\\?linkid=([a-z]+)").getMatch(0) + "&x=" + Integer.toString(new Random().nextInt(100)) + "&y=" + Integer.toString(new Random().nextInt(100)));
        final String fpName = br.getRegex("Titre:[\t\n\r ]+</td>[\t\n\r ]+<td style=\\'border:1px\\'>([^<>\"/]+)</td>").getMatch(0);
        String[] links = br.getRegex("target=_blank>(http://[^<>\"\\']+)").getColumn(0);
        if (links == null || links.length == 0) {
            logger.warning("Decrypter broken for link: " + parameter);
            return null;
        }
        for (String singleLink : links)
            if (!singleLink.contains("top-protect.net/")) decryptedLinks.add(createDownloadlink(singleLink));
        if (fpName != null) {
            FilePackage fp = FilePackage.getInstance();
            fp.setName(Encoding.htmlDecode(fpName.trim()));
            fp.addLinks(decryptedLinks);
        }
        return decryptedLinks;
    }

}
