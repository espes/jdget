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

import java.util.ArrayList;

import jd.PluginWrapper;
import jd.controlling.ProgressController;
import jd.http.RandomUserAgent;
import jd.parser.Regex;
import jd.parser.html.HTMLParser;
import jd.plugins.CryptedLink;
import jd.plugins.DecrypterException;
import jd.plugins.DecrypterPlugin;
import jd.plugins.DownloadLink;
import jd.plugins.PluginForDecrypt;
import jd.utils.locale.JDL;

@DecrypterPlugin(revision = "$Revision: 15843 $", interfaceVersion = 2, names = { "paste2.org" }, urls = { "http://[\\w\\.]*?paste2\\.org/(p|followup)/[0-9]+" }, flags = { 0 })
public class Paste2Org extends PluginForDecrypt {

    public Paste2Org(PluginWrapper wrapper) {
        super(wrapper);
    }

    public ArrayList<DownloadLink> decryptIt(CryptedLink param, ProgressController progress) throws Exception {
        ArrayList<DownloadLink> decryptedLinks = new ArrayList<DownloadLink>();
        String parameter = param.toString();
        br.setFollowRedirects(false);
        // Workaround for "followup" links
        if (parameter.contains("followup")) {
            String id = new Regex(parameter, "followup/(\\d+)").getMatch(0);
            parameter = "http://paste2.org/p/" + id;
        }
        br.getHeaders().put("User-Agent", RandomUserAgent.generate());
        br.getPage(parameter);
        /* Error handling */
        if (br.containsHTML("Page Not Found")) throw new DecrypterException(JDL.L("plugins.decrypt.errormsg.nolinks", "Perhaps wrong URL or there are no links to add."));
        String plaintxt = br.getRegex("main-container(.*?)footer-contents").getMatch(0);
        if (plaintxt == null) plaintxt = br.getRegex("<div class=\"code\\-wrap\">(.*?)</div>").getMatch(0);
        if (plaintxt == null) {
            logger.info("Paste2 Decrypter: Could not find textfield: " + parameter);
            logger.info("Paste2 Decrypter: Please report this to JDownloader' Development Team.");
            return null;
        }
        String[] links = HTMLParser.getHttpLinks(plaintxt, "");
        if (links == null || links.length == 0) {
            logger.info("Found no hosterlinks in plaintext from link " + parameter);
            throw new DecrypterException(JDL.L("plugins.decrypt.errormsg.nolinks", "Perhaps wrong URL or there are no links to add."));
        }
        /* avoid recursion */
        for (int i = 0; i < links.length; i++) {
            String dlLink = links[i];
            if (!this.canHandle(dlLink)) {
                decryptedLinks.add(createDownloadlink(dlLink));
            }
        }
        return decryptedLinks;
    }
}
