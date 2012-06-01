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
import jd.controlling.DistributeData;
import jd.controlling.ProgressController;
import jd.plugins.CryptedLink;
import jd.plugins.DecrypterPlugin;
import jd.plugins.DownloadLink;
import jd.plugins.PluginForDecrypt;

@DecrypterPlugin(revision = "$Revision: 12369 $", interfaceVersion = 2, names = { "xenonlink.net" }, urls = { "http://[\\w\\.]*?xenonlink\\.net/" }, flags = { 0 })
public class XnnLnkNt extends PluginForDecrypt {

    public XnnLnkNt(PluginWrapper wrapper) {
        super(wrapper);
    }

    public ArrayList<DownloadLink> decryptIt(CryptedLink param, ProgressController progress) throws Exception {
        ArrayList<DownloadLink> decryptedLinks = new ArrayList<DownloadLink>();

        String dataCode = getUserInput(null, param);

        br.getPage("http://www.xenonlink.net/index.php?p=2&dg=" + dataCode);

        String jdlist = br.getRegex("<body bgcolor=#3366CC>(.*)").getMatch(0);
        ArrayList<DownloadLink> links = new DistributeData(jdlist).findLinks();
        decryptedLinks.addAll(links);

        return decryptedLinks;
    }

}
