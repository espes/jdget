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
import jd.nutils.encoding.Encoding;
import jd.plugins.CryptedLink;
import jd.plugins.DecrypterException;
import jd.plugins.DecrypterPlugin;
import jd.plugins.DownloadLink;
import jd.plugins.FilePackage;
import jd.plugins.PluginForDecrypt;
import jd.utils.locale.JDL;

@DecrypterPlugin(revision = "$Revision: 14596 $", interfaceVersion = 2, names = { "speedlounge.in" }, urls = { "http://(www\\.)?speedlounge\\.in/detail\\.php\\?cat=.*?\\&id=[0-9]+" }, flags = { 0 })
public class SpeedLngN extends PluginForDecrypt {

    public SpeedLngN(PluginWrapper wrapper) {
        super(wrapper);
    }

    public ArrayList<DownloadLink> decryptIt(CryptedLink param, ProgressController progress) throws Exception {
        ArrayList<DownloadLink> decryptedLinks = new ArrayList<DownloadLink>();
        String parameter = param.toString();
        br.getPage(parameter);
        if (br.containsHTML("Entry NOT found\\!")) throw new DecrypterException(JDL.L("plugins.decrypt.errormsg.unavailable", "Perhaps wrong URL or the download is not available anymore."));
        String pass = br.getRegex("<b>Password:</b>.*?<td>(.*?)</td>").getMatch(0);
        ArrayList<String> passwords = new ArrayList<String>();
        passwords.add("speedlounge.in");
        if (pass != null) {
            passwords.add(pass.trim());
        }
        String fpname = br.getRegex("<title>(.*?)</title>").getMatch(0);
        if (br.containsHTML("Entry NOT found")) throw new DecrypterException(JDL.L("plugins.decrypt.errormsg.unavailable", "Perhaps wrong URL or the download is not available anymore."));
        String[] links = br.getRegex("name=\"go\" value=\"Go\\.swf\\?ID=(.*?)\\&amp;").getColumn(0);
        if (links == null || links.length == 0) links = br.getRegex("<embed src=\"Go\\.swf\\?ID=(.*?)\\&amp;").getColumn(0);
        if (links == null || links.length == 0) return null;
        for (String cryptedlink : links) {
            if (cryptedlink.contains("=")) {
                String finallink = "http://linksave.in/" + Encoding.Base64Decode(cryptedlink);
                DownloadLink dl = createDownloadlink(finallink);
                dl.setSourcePluginPasswordList(passwords);
                decryptedLinks.add(dl);
            }
        }
        if (fpname != null) {
            FilePackage fp = FilePackage.getInstance();
            fp.setName(fpname);
            fp.addLinks(decryptedLinks);
        }
        return decryptedLinks;
    }

}
