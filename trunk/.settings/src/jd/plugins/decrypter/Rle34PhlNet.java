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
import jd.parser.Regex;
import jd.plugins.CryptedLink;
import jd.plugins.DecrypterPlugin;
import jd.plugins.DownloadLink;
import jd.plugins.FilePackage;
import jd.plugins.PluginForDecrypt;

@DecrypterPlugin(revision = "$Revision: 16183 $", interfaceVersion = 2, names = { "rule34.paheal.net" }, urls = { "http://(www\\.)?rule34\\.paheal\\.net/post/list/[A-Za-z0-9_\\-\\.]+/\\d+" }, flags = { 0 })
public class Rle34PhlNet extends PluginForDecrypt {

    public Rle34PhlNet(PluginWrapper wrapper) {
        super(wrapper);
    }

    public ArrayList<DownloadLink> decryptIt(CryptedLink param, ProgressController progress) throws Exception {
        ArrayList<DownloadLink> decryptedLinks = new ArrayList<DownloadLink>();
        String parameter = param.toString();
        br.getPage(parameter);
        String[] links = br.getRegex("<br><a href=\\'(http://.*?)\\'>").getColumn(0);
        if (links == null || links.length == 0) links = br.getRegex("\\'(http://rule34\\-images\\.paheal\\.net/_images/[a-z0-9]+/.*?)\\'").getColumn(0);
        if (links == null || links.length == 0) links = br.getRegex("('|\")(http://rule34\\-[a-zA-Z0-9\\-]*?\\.paheal\\.net/_images/[a-z0-9]+/.*?)('|\")").getColumn(1);
        if (links == null || links.length == 0) return null;
        for (String dl : links)
            decryptedLinks.add(createDownloadlink("directhttp://" + dl));
        FilePackage fp = FilePackage.getInstance();
        fp.setName(new Regex(parameter, "rule34\\.paheal\\.net/post/list/(.*?)/\\d+").getMatch(0));
        fp.addLinks(decryptedLinks);
        return decryptedLinks;
    }

}
