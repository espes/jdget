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
import jd.parser.html.HTMLParser;
import jd.plugins.CryptedLink;
import jd.plugins.DecrypterPlugin;
import jd.plugins.DownloadLink;
import jd.plugins.FilePackage;
import jd.plugins.PluginForDecrypt;

@DecrypterPlugin(revision = "$Revision$", interfaceVersion = 2, names = { "filetram.com" }, urls = { "http://(www\\.)?filetram\\.com/download/.+" }, flags = { 0 })
public class FileTramCom extends PluginForDecrypt {

    public FileTramCom(PluginWrapper wrapper) {
        super(wrapper);
    }

    public ArrayList<DownloadLink> decryptIt(CryptedLink param, ProgressController progress) throws Exception {
        ArrayList<DownloadLink> decryptedLinks = new ArrayList<DownloadLink>();
        String parameter = param.toString();
        br.setFollowRedirects(false);
        br.getPage(parameter);
        if (br.getRedirectLocation() != null && !br.getRedirectLocation().matches("http://(www\\.)?filetram\\.com/download/.+")) {
            decryptedLinks.add(createDownloadlink(br.getRedirectLocation()));
        } else {
            if (br.containsHTML("(>Not Found<|The requested URL was not found on this server)")) {
                logger.info("Link offline: " + parameter);
                return decryptedLinks;
            }
            String fpName = br.getRegex("<h1 class=\"title\">(.*?)</h1>").getMatch(0);
            if (fpName == null) fpName = br.getRegex("<title>(.*?) \\- [A-Za-z0-9\\-]+ download</title>").getMatch(0);
            String textArea = br.getRegex("id=\"copy\\-links\" class=\"select\\-content\" wrap=\"off\">(.*?)</textarea>").getMatch(0);
            String singleLink = br.getRegex("globalVar\\.url=\\'(.*?)\\'").getMatch(0);
            if (singleLink == null) singleLink = br.getRegex("<div id=\"urlHolder\" style=\"display:none\">[\t\n\r ]+<a href=\"(.*?)\"").getMatch(0);
            if ((textArea == null || textArea.length() == 0) && singleLink == null) {
                logger.warning("Decrypter broken for link: " + parameter);
                return null;
            }
            if (textArea != null && textArea.length() != 0) {
                String[] links = HTMLParser.getHttpLinks(textArea, "");
                if (links == null || links.length == 0) return null;
                for (String dl : links)
                    decryptedLinks.add(createDownloadlink(dl));
            }
            if (singleLink != null) decryptedLinks.add(createDownloadlink(singleLink));
            if (fpName != null) {
                FilePackage fp = FilePackage.getInstance();
                fp.setName(fpName.trim());
                fp.addLinks(decryptedLinks);
            }
        }

        return decryptedLinks;
    }

    /* NO OVERRIDE!! */
    public boolean hasCaptcha(CryptedLink link, jd.plugins.Account acc) {
        return false;
    }

}