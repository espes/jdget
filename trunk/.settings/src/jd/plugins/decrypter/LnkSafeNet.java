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
import jd.plugins.PluginForDecrypt;
import jd.utils.locale.JDL;

@DecrypterPlugin(revision = "$Revision: 14377 $", interfaceVersion = 2, names = { "link-safe.net" }, urls = { "http://(www\\.)?link\\-safe\\.net/folder/[a-z0-9]+\\-[a-z0-9]+" }, flags = { 0 })
public class LnkSafeNet extends PluginForDecrypt {

    public LnkSafeNet(PluginWrapper wrapper) {
        super(wrapper);
    }

    public ArrayList<DownloadLink> decryptIt(CryptedLink param, ProgressController progress) throws Exception {
        ArrayList<DownloadLink> decryptedLinks = new ArrayList<DownloadLink>();
        String parameter = param.toString();
        br.setCustomCharset("windows-1251");
        br.getPage(parameter);
        if (br.containsHTML(">ID ссылки является недействительным\\. Пожалуйста, проверьте вашу ссылку")) throw new DecrypterException(JDL.L("plugins.decrypt.errormsg.unavailable", "Perhaps wrong URL or the download is not available anymore."));
        String[] links = br.getRegex(":center;\"><a href=\"(http://.*?)\"").getColumn(0);
        if (links == null || links.length == 0) links = br.getRegex("\"(http://link\\-safe\\.net/out/.*?)\"").getColumn(0);
        if (links == null || links.length == 0) return null;
        progress.setRange(links.length);
        for (String aLink : links) {
            br.getPage(aLink);
            String finallink = br.getRegex("src=\"(.*?)\"").getMatch(0);
            if (finallink == null) return null;
            finallink = Encoding.htmlDecode(finallink);
            decryptedLinks.add(createDownloadlink(finallink));
            progress.increase(1);
        }
        return decryptedLinks;
    }

}
