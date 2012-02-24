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

import java.text.DecimalFormat;
import java.util.ArrayList;

import jd.PluginWrapper;
import jd.controlling.ProgressController;
import jd.nutils.encoding.Encoding;
import jd.parser.Regex;
import jd.plugins.CryptedLink;
import jd.plugins.DecrypterPlugin;
import jd.plugins.DownloadLink;
import jd.plugins.FilePackage;
import jd.plugins.PluginForDecrypt;

@DecrypterPlugin(revision = "$Revision$", interfaceVersion = 2, names = { "imagefap.com" }, urls = { "http://[\\w\\.]*?imagefap\\.com/(gallery\\.php\\?p?gid=.+|gallery/.+|pictures/\\d+/.{1})" }, flags = { 0 })
public class MgfpCm extends PluginForDecrypt {

    public MgfpCm(PluginWrapper wrapper) {
        super(wrapper);
    }

    public ArrayList<DownloadLink> decryptIt(CryptedLink param, ProgressController progress) throws Exception {
        ArrayList<DownloadLink> decryptedLinks = new ArrayList<DownloadLink>();
        br.setFollowRedirects(false);
        String parameter = param.toString();
        parameter = parameter.replaceAll("view\\=[0-9]+", "view=2");
        if (!parameter.contains("view=2") && !new Regex(parameter, "imagefap\\.com/gallery\\.php\\?pgid=").matches()) parameter += "?view=2";
        br.getPage(parameter);
        if (br.getRedirectLocation() != null) {
            if (br.getRedirectLocation().contains("/pictures/")) {
                parameter = br.getRedirectLocation();
                logger.info("New parameter is set: " + parameter);
                br.getPage(parameter);
            } else {
                logger.warning("Getting unknown redirect page");
                br.getPage(br.getRedirectLocation());
            }
        }
        // First find all the information we need (name of the gallery, name of
        // the galleries author)
        String galleryName = br.getRegex("<title>Porn pics of (.*?) \\(Page 1\\)</title>").getMatch(0);
        if (galleryName == null) {
            galleryName = br.getRegex("<font face=\"verdana\" color=\"white\" size=\"4\"><b>(.*?)</b></font>").getMatch(0);
            if (galleryName == null) galleryName = br.getRegex("<meta name=\"description\" content=\"Airplanes porn pics - Imagefap\\.com\\. The ultimate social porn pics site\" />").getMatch(0);
        }
        String authorsName = br.getRegex("<b><font size=\"3\" color=\"#CC0000\">Uploaded by ([^<>\"]+)</font></b>").getMatch(0);
        if (authorsName == null) authorsName = br.getRegex("<td class=\"mnu0\"><a href=\"http://(www\\.)?imagefap\\.com/profile\\.php\\?user=([^<>\"]+)\"").getMatch(0);
        if (galleryName == null) {
            logger.warning("Gallery name could not be found!");
            return null;
        }
        if (authorsName == null) authorsName = "Anonymous";
        galleryName = Encoding.htmlDecode(galleryName);
        authorsName = Encoding.htmlDecode(authorsName);
        int counter = 1;
        DecimalFormat df = new DecimalFormat("0000");
        String links[] = br.getRegex("<a name=\"\\d+\" href=\"/image\\.php\\?id=(\\d+)\\&").getColumn(0);
        if (links == null || links.length == 0) return null;
        for (String element : links) {
            final String orderID = df.format(counter);
            final DownloadLink link = createDownloadlink("http://imagefap.com/image.php?id=" + element);
            link.setProperty("orderid", orderID);
            link.setProperty("galleryname", galleryName);
            link.setProperty("authorsname", authorsName);
            link.setName(orderID);
            decryptedLinks.add(link);
            counter++;
        }
        // Finally set the packagename even if its set again in the linkgrabber
        // available check of the imagefap hosterplugin
        FilePackage fp = FilePackage.getInstance();
        fp.setName(authorsName.trim() + " - " + galleryName.trim());
        fp.addLinks(decryptedLinks);
        return decryptedLinks;
    }

}
