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

@DecrypterPlugin(revision = "$Revision: 16457 $", interfaceVersion = 2, names = { "xmirror.eu" }, urls = { "http://(www\\.)?xmirror\\.eu/[a-z0-9]{3,}" }, flags = { 0 })
public class XMirrorEu extends PluginForDecrypt {

    public XMirrorEu(PluginWrapper wrapper) {
        super(wrapper);
    }

    public ArrayList<DownloadLink> decryptIt(CryptedLink param, ProgressController progress) throws Exception {
        ArrayList<DownloadLink> decryptedLinks = new ArrayList<DownloadLink>();
        String parameter = param.toString();
        br.getPage(parameter);
        String[] links = br.getRegex("id=\\'stat\\d+_\\d+\\'><a href=\\'(http://.*?)\\'").getColumn(0);
        if (links == null || links.length == 0) {
            logger.warning("Decrypter broken for link: " + parameter);
            return null;
        }
        for (String singleLink : links) {
            /** Needed to get the finallink */
            br.getHeaders().put("Referer", "http://xmirror.eu/?q=r_counter");
            br.getPage(singleLink);
            final String finallink = br.getRegex("ads\\'>[\t\n\r ]+<frame src=\"([^<>\"]*?)\"").getMatch(0);
            if (finallink == null) {
                logger.warning("Decrypter broken for link: " + parameter + "\n");
                logger.warning("At link: " + singleLink);
                return null;
            }
            decryptedLinks.add(createDownloadlink(singleLink));
        }
        return decryptedLinks;
    }
}
