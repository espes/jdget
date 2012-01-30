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
import jd.parser.Regex;
import jd.plugins.CryptedLink;
import jd.plugins.DecrypterPlugin;
import jd.plugins.DownloadLink;
import jd.plugins.PluginForDecrypt;

@DecrypterPlugin(revision = "$Revision$", interfaceVersion = 2, names = { "myref.de" }, urls = { "http://[\\w\\.]*?myref\\.de(/){0,1}\\?\\d{0,10}" }, flags = { 0 })
public class MRf extends PluginForDecrypt {

    public MRf(final PluginWrapper wrapper) {
        super(wrapper);
    }

    @Override
    public ArrayList<DownloadLink> decryptIt(final CryptedLink param, final ProgressController progress) throws Exception {
        final ArrayList<DownloadLink> decryptedLinks = new ArrayList<DownloadLink>();
        final String parameter = param.toString();
        br.setFollowRedirects(true);
        br.getPage(parameter);
        String dllink = br.getRegex("<a class=\"text\" href=\"(http://.*?)\"").getMatch(0);
        if (dllink == null) {
            br.setFollowRedirects(false);
            final String downloadid = new Regex(parameter, "\\?([\\d].*)").getMatch(0);
            br.getPage("http://www.myref.de/go_counter.php?id=" + downloadid);
            dllink = br.getRedirectLocation();
        }
        decryptedLinks.add(createDownloadlink(dllink));
        return decryptedLinks;
    }

}