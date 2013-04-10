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
import jd.plugins.DecrypterPlugin;
import jd.plugins.DownloadLink;
import jd.plugins.FilePackage;
import jd.plugins.PluginForDecrypt;

@DecrypterPlugin(revision = "$Revision$", interfaceVersion = 2, names = { "putlocker.com" }, urls = { "http://(www\\.)?putlocker\\.com/public/[A-Za-z0-9]+" }, flags = { 0 })
public class PutLockerComFolder extends PluginForDecrypt {

    public PutLockerComFolder(PluginWrapper wrapper) {
        super(wrapper);
    }

    public ArrayList<DownloadLink> decryptIt(CryptedLink param, ProgressController progress) throws Exception {
        ArrayList<DownloadLink> decryptedLinks = new ArrayList<DownloadLink>();
        ArrayList<String> pages = new ArrayList<String>();
        pages.add("1");
        final String parameter = param.toString();
        br.getPage(parameter);
        if (br.containsHTML(">No files to display<")) {
            logger.info("Link offline: " + parameter);
            return decryptedLinks;
        }
        final String fpName = br.getRegex("<title>([^<>\"]*?) on PutLocker</title>").getMatch(0);
        final String[] addPages = br.getRegex("\\&page=\\d+\">(\\d+)</a>").getColumn(0);
        if (addPages != null && addPages.length != 0) {
            for (final String aPage : addPages)
                pages.add(aPage);
        }
        for (final String currentPage : pages) {
            if (!currentPage.equals("1")) br.getPage(parameter + "?folder_pub=" + new Regex(parameter, "([A-Za-z0-9]+)$").getMatch(0) + "&page=" + currentPage);
            final String[] links = br.getRegex("\"(http://(www\\.)?putlocker\\.com/file/[A-Za-z0-9]+)\"").getColumn(0);
            if (links == null || links.length == 0) {
                logger.warning("Decrypter broken for link: " + parameter);
                return null;
            }
            for (String singleLink : links)
                decryptedLinks.add(createDownloadlink(singleLink));
        }
        if (fpName != null) {
            final FilePackage fp = FilePackage.getInstance();
            fp.setName(Encoding.htmlDecode(fpName.trim()));
            fp.addLinks(decryptedLinks);
        }
        return decryptedLinks;
    }

    /* NO OVERRIDE!! */
    public boolean hasCaptcha(CryptedLink link, jd.plugins.Account acc) {
        return false;
    }

}