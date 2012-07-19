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
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import jd.PluginWrapper;
import jd.controlling.ProgressController;
import jd.nutils.encoding.Encoding;
import jd.parser.Regex;
import jd.plugins.CryptedLink;
import jd.plugins.DecrypterPlugin;
import jd.plugins.DownloadLink;
import jd.plugins.FilePackage;
import jd.plugins.PluginForDecrypt;

import org.appwork.utils.formatter.SizeFormatter;

@DecrypterPlugin(revision = "$Revision$", interfaceVersion = 2, names = { "minus.com" }, urls = { "http://((www|dev)\\.)?(minus\\.com|min\\.us)/[A-Za-z0-9]+" }, flags = { 0 })
public class MinUsComDecrypter extends PluginForDecrypt {

    public MinUsComDecrypter(PluginWrapper wrapper) {
        super(wrapper);
    }

    public ArrayList<DownloadLink> decryptIt(CryptedLink param, ProgressController progress) throws Exception {
        ArrayList<DownloadLink> decryptedLinks = new ArrayList<DownloadLink>();
        String parameter = param.toString().replace("dev.min", "min").replace("min.us/", "minus.com/");
        br.getPage(parameter);
        final String mainid = new Regex(parameter, "minus\\.com/(.+)").getMatch(0);
        if (br.containsHTML("(<h2>Not found\\.</h2>|<p>Our records indicate that the gallery/image you are referencing has been deleted or does not exist|The page you requested does not exist)")) {
            DownloadLink dl = createDownloadlink("http://i.minusdecrypted.com/340609783585/VTjbgttT_QsH/" + mainid + ".offline");
            dl.setAvailable(false);
            decryptedLinks.add(dl);
            return decryptedLinks;
        }
        // Get album name for package name
        String fpName = br.getRegex("var gallerydata = \\{.+ \"name\": \"([^\"]+)").getMatch(0);
        // do not catch first "name", only items within array
        final String singleLink = br.getRegex("<a class=\"btn\\-action btn\\-download\"[\t\n\r ]+href=\"(http://i\\.minus\\.com/\\d+/[A-Za-z0-9\\-_]+/[A-Za-z0-9\\-_]+/[^<>\"]*?)\"").getMatch(0);
        String[] items = br.getRegex("\\{[\r\n\t]+(\"name\":[^\\}]+)\\} ").getColumn(0);
        // fail over for single items ?. Either that or they changed website yet
        // again and do not display the full gallery array.
        if (items == null || items.length == 0) items = br.getRegex("var gallerydata = \\{(.*?)\\};").getColumn(0);
        if ((singleLink == null) && (items == null || items.length == 0)) {
            logger.warning("Decrypter broken for link: " + parameter);
            return null;
        }
        if (items != null && items.length != 0) {
            for (String singleitems : items) {
                String filename = new Regex(singleitems, "\"name\": ?\"([^<>\"/]+\\.[A-Za-z0-9]{1,5})\"").getMatch(0);
                final String filesize = new Regex(singleitems, "\"filesize_bytes\": ?(\\d+)").getMatch(0);
                final String secureprefix = new Regex(singleitems, "\"secure_prefix\": ?\"(/\\d+/[A-Za-z0-9\\-_]+)\"").getMatch(0);
                final String linkid = new Regex(singleitems, "\"id\": ?\"([A-Za-z0-9\\-_]+)\"").getMatch(0);
                if (filename == null || filesize == null || secureprefix == null || linkid == null) {
                    logger.warning("Decrypter broken for link: " + parameter);
                    return null;
                }
                filename = decodeUnicode(Encoding.htmlDecode(filename.trim()));
                final String filelink = "http://i.minusdecrypted.com" + secureprefix + "/d" + linkid + filename.substring(filename.lastIndexOf("."));
                final DownloadLink dl = createDownloadlink(filelink);
                dl.setFinalFileName(filename);
                dl.setDownloadSize(Long.parseLong(filesize));
                dl.setAvailable(true);
                dl.setProperty("mainid", mainid);
                decryptedLinks.add(dl);
            }
        }
        // Only one link available, add it!
        if (singleLink != null) {
            final String filesize = br.getRegex("<span class=\"text\">Download \\(([^<>\"]*?)\\)</span>").getMatch(0);
            final String filelink = singleLink.replace("minus.com/", "minusdecrypted.com/");
            final DownloadLink dl = createDownloadlink(filelink);
            dl.setFinalFileName(new Regex(singleLink, "minus\\.com/\\d+/[A-Za-z0-9]+/[A-Za-z0-9]+/([^<>\"]*?)\"").getMatch(0));
            if (filesize != null) dl.setDownloadSize(SizeFormatter.getSize(filesize));
            dl.setAvailable(true);
            dl.setProperty("mainid", mainid);
            decryptedLinks.add(dl);
        }
        if (fpName != null) {
            FilePackage fp = FilePackage.getInstance();
            fp.setName(decodeUnicode(Encoding.htmlDecode(fpName.trim())));
            fp.addLinks(decryptedLinks);
        }
        return decryptedLinks;
    }

    private String decodeUnicode(final String s) {
        final Pattern p = Pattern.compile("\\\\u([0-9a-fA-F]{4})");
        String res = s;
        final Matcher m = p.matcher(res);
        while (m.find()) {
            res = res.replaceAll("\\" + m.group(0), Character.toString((char) Integer.parseInt(m.group(1), 16)));
        }
        return res;
    }
}
