//    By Highfredo
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

import java.io.IOException;
import java.text.DecimalFormat;
import java.util.ArrayList;

import jd.PluginWrapper;
import jd.controlling.ProgressController;
import jd.nutils.encoding.Encoding;
import jd.parser.Regex;
import jd.plugins.CryptedLink;
import jd.plugins.DecrypterException;
import jd.plugins.DecrypterPlugin;
import jd.plugins.DownloadLink;
import jd.plugins.PluginForDecrypt;

@DecrypterPlugin(revision = "$Revision$", interfaceVersion = 2, names = { "antena3.com" }, urls = { "http://(www\\.)?antena3.com/videos/[\\-/0-9A-Za-c]+\\.html" }, flags = { 0 })
public class Antena3ComSalon extends PluginForDecrypt {

    public Antena3ComSalon(PluginWrapper wrapper) {
        super(wrapper);
    }

    private ArrayList<DownloadLink> decryptedLinks = new ArrayList<DownloadLink>();

    @Override
    public ArrayList<DownloadLink> decryptIt(CryptedLink link, ProgressController progress) throws Exception {
        br.getPage(link.toString());
        if (br.containsHTML("<h1>¡Uy\\! No encontramos la página que buscas\\.</h1>")) {
            logger.info("Link offline: " + link.toString());
            return decryptedLinks;
        }
        if (br.containsHTML("<li class=\"active\"><a title=\"Vídeos de Capítulos Completos de Series de Antena 3\"")) {
            final String[] videoPages = br.getRegex("<ul class=\"page\\d+\">(.*?)</ul>").getColumn(0);
            for (final String vList : videoPages) {
                final String[] episodeList = new Regex(vList, "alt=\"[^<>\"/]+\"[\t\n\r ]+href=\"(/videos/[^<>\"]*?)\"").getColumn(0);
                if (episodeList == null || episodeList.length == 0) {
                    logger.warning("Decrypter broken for link: " + link.toString());
                    return null;
                }
                for (final String video : episodeList) {
                    br.getPage("http://www.antena3.com" + video);
                    try {
                        decryptSingleVideo(link);
                    } catch (final DecrypterException e) {
                        if ("Offline".equals(e.getMessage())) return decryptedLinks;
                        throw e;
                    }
                }
            }
        } else {
            try {
                decryptSingleVideo(link);
            } catch (final DecrypterException e) {
                if ("Offline".equals(e.getMessage())) return decryptedLinks;
                throw e;
            }
        }

        return decryptedLinks;
    }

    private void decryptSingleVideo(final CryptedLink link) throws DecrypterException, IOException {
        String name = br.getRegex("<title>ANTENA 3 TV \\- Vídeos de ([^<>\"]*?)</title>").getMatch(0);
        final String xmlstuff = br.getRegex("player_capitulo\\.xml=\\'(.*?)\\';").getMatch(0);
        if (xmlstuff == null || name == null) {
            logger.warning("Decrypter broken for link: " + link.toString());
            throw new DecrypterException("Decrypter broken");
        }
        name = Encoding.htmlDecode(name);

        br.getPage("http://www.antena3.com" + xmlstuff);
        // Offline1
        if (br.containsHTML(">El contenido al que estás intentando acceder ya no está disponible")) {
            logger.info("Link offline: " + link.toString());
            throw new DecrypterException("Offline");
        }
        // Offline2
        if (br.getURL().equals("http://www.antena3.comnull/")) {
            logger.info("Link offline: " + link.toString());
            throw new DecrypterException("Offline");
        }
        // Offline3
        if (br.containsHTML(">El contenido al que estás intentando acceder no existe<")) {
            logger.info("Link offline: " + link.toString());
            throw new DecrypterException("Offline");
        }
        final String[] links = br.getRegex("<archivo>(.*?)</archivo>").getColumn(0);
        if (links == null || links.length == 0) {
            logger.warning("Decrypter broken for link: " + link.toString());
            throw new DecrypterException("Decrypter broken");
        }
        int counter = 1;
        final DecimalFormat df = new DecimalFormat("00");
        for (String sdl : links) {
            if (sdl.contains(".mp4")) {
                sdl = "http://desprogresiva.antena3.com/" + sdl.replace("<![CDATA[", "").replace("]]>", "");
                final DownloadLink dl = createDownloadlink(sdl);
                dl.setFinalFileName(name + "_" + df.format(counter) + ".mp4");
                decryptedLinks.add(dl);
                counter++;
            }
        }
    }
}
