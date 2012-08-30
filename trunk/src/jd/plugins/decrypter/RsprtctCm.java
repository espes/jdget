//    jDownloader - Downloadmanager
//    Copyright (C) 2008  JD-Team support@jdownloader.org
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
import jd.nutils.encoding.Encoding;
import jd.plugins.CryptedLink;
import jd.plugins.DecrypterPlugin;
import jd.plugins.DownloadLink;
import jd.plugins.PluginForDecrypt;

@DecrypterPlugin(revision = "$Revision$", interfaceVersion = 2, names = { "rsprotect.com" }, urls = { "http://(www\\.)?rsprotect\\.com/rc\\-[A-Za-z0-9]+/[^<>\"/]+" }, flags = { 0 })
public class RsprtctCm extends PluginForDecrypt {

    public RsprtctCm(PluginWrapper wrapper) {
        super(wrapper);
    }

    public ArrayList<DownloadLink> decryptIt(CryptedLink param, ProgressController progress) throws Exception {
        ArrayList<DownloadLink> decryptedLinks = new ArrayList<DownloadLink>();
        String parameter = param.toString();
        if (!parameter.contains("www.")) parameter = parameter.replace("http://", "http://www.");
        br.setFollowRedirects(false);
        br.getPage(parameter);
        if (br.containsHTML("No htmlCode read")) {
            logger.info("Link offline: " + parameter);
            return decryptedLinks;
        }
        String finallink = br.getRegex("onsubmit=\"return checkStatus\\(\\)\" action=\"(https?://[^<>\"]*?)\"").getMatch(0);
        if (finallink == null) {
            logger.warning("Decrypter broken for link: " + parameter);
            return null;
        }
        finallink = Encoding.htmlDecode(finallink);
        decryptedLinks.add(createDownloadlink(Encoding.htmlDecode(finallink)));
        return decryptedLinks;
    }

}
