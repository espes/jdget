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
import java.util.regex.Pattern;

import jd.PluginWrapper;
import jd.controlling.ProgressController;
import jd.parser.Regex;
import jd.plugins.CryptedLink;
import jd.plugins.DecrypterPlugin;
import jd.plugins.DownloadLink;
import jd.plugins.FilePackage;
import jd.plugins.PluginForDecrypt;

@DecrypterPlugin(revision = "$Revision$", interfaceVersion = 2, names = { "spiegel.de" }, urls = { "(http://(www\\.)?spiegel\\.de/video/[a-z0-9\\-]+\\-\\d+\\.html|http://[\\w\\.]*?spiegel\\.de/fotostrecke/fotostrecke-\\d+(-\\d+)?.html)" }, flags = { 0 })
public class SpglD extends PluginForDecrypt {

    private static final Pattern PATTERN_SUPPORTED_VIDEO      = Pattern.compile("http://[\\w\\.]*?spiegel\\.de/video/[a-z0-9\\-]+-(\\d+).html", Pattern.CASE_INSENSITIVE);

    private static final Pattern PATTERN_SUPPORED_FOTOSTRECKE = Pattern.compile("http://[\\w\\.]*?spiegel\\.de/fotostrecke/fotostrecke-\\d+(-\\d+)?.html", Pattern.CASE_INSENSITIVE);

    // Patterns für Vidos

    private static final Pattern PATTERN_THEMA                = Pattern.compile("<headline>(.+?)</headline>");
    private static final Pattern PATTERN_HEADLINE             = Pattern.compile("<thema>(.+?)</thema>");
    // private static final Pattern PATTERN_TEASER = Pattern.compile("<teaser>(.+?)</teaser>");

    /*
     * Type 1: h263 flv Type 2: flv mid (VP6) Type 3: h263 low Type 4: flv low (VP6) Type 5: flv high (VP6) (680544) Type 6: h263 3gp Type
     * 7: h263 3gp low Type 8: iphone mp4 Type 9: podcast mp4 640480 Type 15 : H264
     */

    private static final Pattern PATTERN_FILENAME             = Pattern.compile("<filename>(.+?)</filename>");
    // private static final Pattern PATTERN_FILENAME_T5 = Pattern.compile("^\\s+<type5>\\s+$\\s+^\\s+" + SpglD.PATTERN_FILENAME.toString(),
    // Pattern.MULTILINE);
    // private static final Pattern PATTERN_FILENAME_T6 = Pattern.compile("^\\s+<type6>\\s+$\\s+^\\s+" + SpglD.PATTERN_FILENAME.toString(),
    // Pattern.MULTILINE);
    // private static final Pattern PATTERN_FILENAME_T8 = Pattern.compile("^\\s+<type8>\\s+$\\s+^\\s+" + SpglD.PATTERN_FILENAME.toString(),
    // Pattern.MULTILINE);
    // private static final Pattern PATTERN_FILENAME_T9 = Pattern.compile("^\\s+<type9>\\s+$\\s+^\\s+" + SpglD.PATTERN_FILENAME.toString(),
    // Pattern.MULTILINE);
    // private static final Pattern PATTERN_FILENAME_T15 = Pattern.compile("^\\s+<type15>\\s+$\\s+^\\s+" +
    // SpglD.PATTERN_FILENAME.toString(), Pattern.MULTILINE);

    // Patterns für Fotostrecken
    private static final Pattern PATTERN_IMG_URL              = Pattern.compile("<a id=\"spFotostreckeControlImg\" href=\"(/fotostrecke/fotostrecke-\\d+-\\d+.html)\"><img src=\"(http://www.spiegel.de/img/.+?(\\.\\w+?))\"");
    private static final Pattern PATTERN_IMG_TITLE            = Pattern.compile("<meta name=\"description\" content=\"(.+?)\" />");

    public SpglD(final PluginWrapper wrapper) {
        super(wrapper);
    }

    public ArrayList<DownloadLink> decryptIt(final CryptedLink cryptedLink, final ProgressController progress) throws Exception {
        final ArrayList<DownloadLink> decryptedLinks = new ArrayList<DownloadLink>();
        if (new Regex(cryptedLink.getCryptedUrl(), SpglD.PATTERN_SUPPORTED_VIDEO).matches()) {
            final String id = new Regex(cryptedLink.getCryptedUrl(), SpglD.PATTERN_SUPPORTED_VIDEO).getMatch(0);
            this.br.getPage("http://video.spiegel.de/flash/" + id + ".xml");
            final String[] types = br.getRegex("(<type\\d+>.*?type\\d+>)").getColumn(0);
            final String xmlInfos = this.br.getPage("http://www1.spiegel.de/active/playlist/fcgi/playlist.fcgi/asset=flashvideo/mode=id/id=" + id);
            final String name = new Regex(xmlInfos, SpglD.PATTERN_THEMA).getMatch(0) + "-" + new Regex(xmlInfos, SpglD.PATTERN_HEADLINE).getMatch(0);
            // final String comment = new Regex(xmlInfos, SpglD.PATTERN_TEASER).getMatch(0);

            if (types == null || types.length == 0) {
                logger.warning("Decrypter broken for link: " + cryptedLink.toString());
                return null;
            }
            for (final String type : types) {
                final String fullName = new Regex(type, "<filename>([^<>\"]*?)</filename>").getMatch(0);
                String quality = new Regex(type, "<filename>\\d+_([^<>\"]*?)</filename>").getMatch(0);
                if (quality == null) quality = fullName;
                if (fullName == null || quality == null) {
                    logger.warning("Decrypter broken for link: " + cryptedLink.toString());
                    return null;
                }
                // Skip .3gp as (most of) those links are offline
                if (quality.contains(".3gp")) continue;
                final DownloadLink dl = createDownloadlink("http://video2.spiegel.de/flash/" + fullName);
                dl.setFinalFileName(name + "_" + quality);
                decryptedLinks.add(dl);
            }
            final FilePackage fp = FilePackage.getInstance();
            fp.setName(name);
            fp.addLinks(decryptedLinks);
        } else if (new Regex(cryptedLink.getCryptedUrl(), SpglD.PATTERN_SUPPORED_FOTOSTRECKE).matches()) {
            final String group3 = new Regex(cryptedLink.getCryptedUrl(), SpglD.PATTERN_SUPPORED_FOTOSTRECKE).getMatch(0);
            if (group3 != null) {
                // Sicherstellen, dass mit dem 1. Bild begonnen wird!
                decryptedLinks.add(this.createDownloadlink(cryptedLink.getCryptedUrl().replaceAll(group3 + "\\.html", ".html")));
                return decryptedLinks;
            }

            String url = cryptedLink.getCryptedUrl();
            final String title = new Regex(this.br.getPage(url), SpglD.PATTERN_IMG_TITLE).getMatch(0);
            int count = 1;
            final FilePackage filePackage = FilePackage.getInstance();
            filePackage.setName(title.trim());
            while (url != null) {
                final String page = this.br.getPage(url);
                final Regex regex = new Regex(page, SpglD.PATTERN_IMG_URL);
                url = regex.getMatch(0);
                url = url != null ? "http://www.spiegel.de" + url : null;
                final String imgLink = regex.getMatch(1);
                final String ending = regex.getMatch(2);
                if (imgLink != null) {
                    final DownloadLink dlLink = this.createDownloadlink(imgLink);
                    filePackage.add(dlLink);
                    dlLink.setFinalFileName(title.trim() + "-" + count + ending);
                    dlLink.setName(dlLink.getFinalFileName());
                    decryptedLinks.add(dlLink);
                    count++;
                }

            }
        }

        return decryptedLinks;
    }

}
