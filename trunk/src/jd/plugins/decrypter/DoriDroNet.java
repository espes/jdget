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
import jd.http.Browser.BrowserException;
import jd.plugins.CryptedLink;
import jd.plugins.DecrypterPlugin;
import jd.plugins.DownloadLink;
import jd.plugins.FilePackage;
import jd.plugins.PluginForDecrypt;

@DecrypterPlugin(revision = "$Revision$", interfaceVersion = 2, names = { "doridro.net" }, urls = { "http://(www\\.)?doridro\\.net/download/.+" }, flags = { 0 })
public class DoriDroNet extends PluginForDecrypt {

    public DoriDroNet(PluginWrapper wrapper) {
        super(wrapper);
    }

    public ArrayList<DownloadLink> decryptIt(CryptedLink param, ProgressController progress) throws Exception {
        ArrayList<DownloadLink> decryptedLinks = new ArrayList<DownloadLink>();
        String parameter = param.toString();
        if (parameter.substring(parameter.length() - 5, parameter.length()).equals(".html")) {
            DownloadLink dl = createDownloadlink(parameter.replace("doridro.net/", "doridrodecrypted.net/"));
            decryptedLinks.add(dl);
        } else {
            try {
                br.getPage(parameter);
            } catch (final BrowserException e) {
                logger.info("Failed to decrypt link because of server error: " + parameter);
                return decryptedLinks;
            }
            String fpName = br.getRegex("<title>(.*?) Album Download</title>").getMatch(0);
            String[] links = br.getRegex("<td bgcolor=\"#666666\"><a href=\"(http://.*?)\"").getColumn(0);
            if (links == null || links.length == 0) {
                links = br.getRegex("\"(http://doridro\\.net/download/.+/.+/.*?)\"").getColumn(0);
                if (links == null || links.length == 0) links = br.getRegex("<td bgcolor=\"#666666\"><a href=\"(http://doridro\\.net/download/.+/.*?)\"").getColumn(0);
            }
            if (links == null || links.length == 0) return null;
            progress.setRange(links.length);
            for (String singleLink : links) {
                if (!singleLink.substring(singleLink.length() - 5, singleLink.length()).equals(".html")) {
                    decryptedLinks.add(createDownloadlink(singleLink));
                } else {
                    decryptedLinks.add(createDownloadlink(singleLink.replace("doridro.net/", "doridrodecrypted.net/")));
                }
                progress.increase(1);
            }
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