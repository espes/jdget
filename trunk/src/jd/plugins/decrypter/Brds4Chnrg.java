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
import java.util.Date;

import jd.PluginWrapper;
import jd.controlling.ProgressController;
import jd.parser.Regex;
import jd.plugins.CryptedLink;
import jd.plugins.DecrypterPlugin;
import jd.plugins.DownloadLink;
import jd.plugins.DownloadLink.AvailableStatus;
import jd.plugins.FilePackage;
import jd.plugins.PluginForDecrypt;

@DecrypterPlugin(revision = "$Revision$", interfaceVersion = 2, names = { "boards.4chan.org" }, urls = { "https?://[\\w\\.]*?boards\\.4chan\\.org/[0-9a-z]{1,3}/(res/[0-9]+)?" }, flags = { 0 })
public class Brds4Chnrg extends PluginForDecrypt {

    public Brds4Chnrg(PluginWrapper wrapper) {
        super(wrapper);
    }

    // @Override
    public ArrayList<DownloadLink> decryptIt(CryptedLink param, ProgressController progress) throws Exception {
        ArrayList<DownloadLink> decryptedLinks = new ArrayList<DownloadLink>();
        FilePackage fp = FilePackage.getInstance();
        String parameter = param.toString();
        String prot = new Regex(parameter, "(https?)://").getMatch(0);
        br.getPage(parameter);
        if (parameter.matches("https?://[\\w\\.]*?boards\\.4chan\\.org/[0-9a-z]{1,3}/[0-9]*")) {
            String[] threads = br.getRegex("<span id=\"nothread([0-9]+)\"><a href=\"res/").getColumn(0);
            for (String thread : threads) {
                decryptedLinks.add(createDownloadlink(parameter + "res/" + thread));
            }
        } else {
            String[] images = br.getRegex("(?i)(https?://[\\w\\.]*?images.4chan\\.org/[0-9a-z]{1,3}/src/\\d+\\.(gif|jpg|png))").getColumn(0);
            if (images == null || images.length == 0) images = br.getRegex("(?i)File: <a href=\"(//images.4chan\\.org/[0-9a-z]{1,3}/src/\\d+\\.(gif|jpg|png))\"").getColumn(0);

            if (br.containsHTML("404 - Not Found")) {
                fp.setName("4chan - 404 - Not Found");
                br.getPage(prot + "://sys.4chan.org/error/404/rid.php");
                String image404 = br.getRegex("(https?://.+)").getMatch(0);
                DownloadLink dl = createDownloadlink(image404);
                dl.setAvailableStatus(AvailableStatus.TRUE);
                fp.add(dl);
                decryptedLinks.add(dl);
            } else if (images.length == 0) {
                return decryptedLinks;
            } else {
                String domain = "4chan.org";
                String cat = br.getRegex("<div class=\"boardTitle\">/.{1,3}/ - (.*?)</div>").getMatch(0);
                if (cat == null) cat = br.getRegex("<title>/b/ - (.*?)</title>").getMatch(0);
                if (cat != null) {
                    cat = cat.replace("&amp;", "&");
                } else {
                    cat = "Unknown Cat";
                }
                String date = new Date().toString();
                fp.setName(domain + " - " + cat + " - " + date);
                for (String image : images) {
                    if (image.startsWith("/") && !image.startsWith("h")) image = image.replace("//", prot + "://");
                    DownloadLink dl = createDownloadlink(image);
                    dl.setAvailableStatus(AvailableStatus.TRUE);
                    fp.add(dl);
                    decryptedLinks.add(dl);
                }
            }
        }
        return decryptedLinks;
    }
}
