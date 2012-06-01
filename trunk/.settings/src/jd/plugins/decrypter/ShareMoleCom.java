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
import jd.plugins.CryptedLink;
import jd.plugins.DecrypterException;
import jd.plugins.DecrypterPlugin;
import jd.plugins.DownloadLink;
import jd.plugins.PluginForDecrypt;
import jd.utils.locale.JDL;

@DecrypterPlugin(revision = "$Revision: 10247 $", interfaceVersion = 2, names = { "sharemole.com" }, urls = { "http://[\\w\\.]*?sharemole\\.com/[a-z0-9]+" }, flags = { 0 })
public class ShareMoleCom extends PluginForDecrypt {

    public ShareMoleCom(PluginWrapper wrapper) {
        super(wrapper);
    }

    public ArrayList<DownloadLink> decryptIt(CryptedLink param, ProgressController progress) throws Exception {
        ArrayList<DownloadLink> decryptedLinks = new ArrayList<DownloadLink>();
        String parameter = param.toString();
        br.setFollowRedirects(true);
        br.getPage(parameter);
        if (br.containsHTML("(does not exist|or it has been removed)")) throw new DecrypterException(JDL.L("plugins.decrypt.errormsg.unavailable", "Perhaps wrong URL or the download is not available anymore."));
        String filename = br.getRegex("<title>(.*?)- Download").getMatch(0);
        if (filename == null) filename = br.getRegex("<br />Name :(.*?)<br").getMatch(0);
        String[] links = br.getRegex("<li><a href=(http.*?)target").getColumn(0);
        if (links.length == 0) return null;
        if (filename != null) {
            for (String finallink : links) {
                DownloadLink dlink = createDownloadlink(finallink.trim());
                dlink.setFinalFileName(filename);
                decryptedLinks.add(dlink);
            }
        } else {
            for (String finallink : links) {
                decryptedLinks.add(createDownloadlink(finallink.trim()));
            }
        }

        return decryptedLinks;
    }
}
