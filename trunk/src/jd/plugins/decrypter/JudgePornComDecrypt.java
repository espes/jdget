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
import jd.plugins.DecrypterPlugin;
import jd.plugins.DownloadLink;
import jd.plugins.PluginForDecrypt;

@DecrypterPlugin(revision = "$Revision$", interfaceVersion = 2, names = { "judgeporn.com" }, urls = { "http://(www\\.)?judgeporn\\.com/videos/[a-z0-9\\-_]+" }, flags = { 0 })
public class JudgePornComDecrypt extends PluginForDecrypt {

    public JudgePornComDecrypt(PluginWrapper wrapper) {
        super(wrapper);
    }

    public ArrayList<DownloadLink> decryptIt(CryptedLink param, ProgressController progress) throws Exception {
        ArrayList<DownloadLink> decryptedLinks = new ArrayList<DownloadLink>();
        final String parameter = param.toString();
        br.setFollowRedirects(false);
        br.getPage(parameter);
        final String finallink = br.getRedirectLocation();
        if (finallink != null && !finallink.contains("judgeporn.com/")) {
            decryptedLinks.add(createDownloadlink(finallink));
        } else {
            final DownloadLink mainlink = createDownloadlink(parameter.replace("judgeporn.com/", "judgeporndecrypted.com/"));
            if (br.getURL().equals("http://www.judgeporn.com/categories/") || br.containsHTML("window\\.location = \"http://(www\\.)?judgeporn\\.com/categories/\"") || br.containsHTML("<title>Free Hardcore Porn Videos and Porn Fucking Movies</title>")) {
                mainlink.setAvailable(false);
            } else {
                String filename = br.getRegex("<title>([^<>\"]*?) at Judge Porn</title>").getMatch(0);
                if (filename == null) filename = br.getRegex("<h1>([^<>\"]*?)</h1>").getMatch(0);
                if (filename != null) {
                    mainlink.setName(Encoding.htmlDecode(filename.trim()) + ".flv");
                }
            }
            decryptedLinks.add(mainlink);
        }

        return decryptedLinks;
    }

}
