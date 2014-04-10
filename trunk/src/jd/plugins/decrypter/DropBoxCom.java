//jDownloader - Downloadmanager
//Copyright (C) 2013  JD-Team support@jdownloader.org
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
import java.util.HashMap;

import jd.PluginWrapper;
import jd.controlling.ProgressController;
import jd.http.URLConnectionAdapter;
import jd.nutils.encoding.Encoding;
import jd.parser.Regex;
import jd.plugins.CryptedLink;
import jd.plugins.DecrypterPlugin;
import jd.plugins.DownloadLink;
import jd.plugins.FilePackage;
import jd.plugins.PluginForDecrypt;
import jd.plugins.PluginForHost;
import jd.utils.JDUtilities;

import org.appwork.utils.formatter.SizeFormatter;

@DecrypterPlugin(revision = "$Revision$", interfaceVersion = 2, names = { "dropbox.com" }, urls = { "https?://(www\\.)?dropbox\\.com/(sh/.+|l/[A-Za-z0-9]+)" }, flags = { 0 })
public class DropBoxCom extends PluginForDecrypt {

    private boolean pluginloaded;

    public DropBoxCom(PluginWrapper wrapper) {
        super(wrapper);
    }

    private final String NORMALLINK   = "https?://(www\\.)?dropbox\\.com/sh/.+";
    private final String REDIRECTLINK = "https?://(www\\.)?dropbox\\.com/l/[A-Za-z0-9]+";

    public ArrayList<DownloadLink> decryptIt(CryptedLink param, ProgressController progress) throws Exception {
        ArrayList<DownloadLink> decryptedLinks = new ArrayList<DownloadLink>();
        String parameter = param.toString().replace("?dl=1", "");
        br.setFollowRedirects(false);
        br.setCookie("http://dropbox.com", "locale", "en");
        URLConnectionAdapter con = null;
        try {
            con = br.openGetConnection(parameter);
            if (con.getResponseCode() == 460) {
                logger.info("Restricted Content: This file is no longer available. For additional information contact Dropbox Support. " + parameter);
                return decryptedLinks;
            }
            // Temporarily unavailable links
            if (con.getResponseCode() == 509) {
                final DownloadLink dl = createDownloadlink(parameter.replace("dropbox.com/", "dropboxdecrypted.com/"));
                dl.setProperty("decrypted", true);
                decryptedLinks.add(dl);
                return decryptedLinks;
            }
            if (con.getResponseCode() == 302 && parameter.matches(REDIRECTLINK)) {
                parameter = br.getRedirectLocation();
            }
            br.followConnection();
        } finally {
            try {
                con.disconnect();
            } catch (Throwable e) {
            }
        }
        br.getPage(parameter);
        // Handling for single links
        if (br.containsHTML(new Regex(parameter, ".*?(\\.com/sh/[a-z0-9]+).+").getMatch(0) + "[^<>\"]+" + "dl=1([^<>\"]*?)\"")) {
            final DownloadLink dl = createDownloadlink(parameter.replace("dropbox.com/", "dropboxdecrypted.com/"));
            dl.setProperty("decrypted", true);
            decryptedLinks.add(dl);
            return decryptedLinks;
        }
        // Decrypt "Download as zip" link
        final String zipLink = br.getRegex("data\\-dl\\-link=\"(https?://dl\\.[^<>\"]*?)\" onclick=\"FreshDropdown\\.hide_all\\(\\)\"><img src=\"[^<>\"]*?\" class=\"[a-z0-9\\-_ ]+\" />Download as \\.zip</a>").getMatch(0);
        if (zipLink != null) {
            final DownloadLink dl = createDownloadlink(parameter.replace("dropbox.com/", "dropboxdecrypted.com/"));
            dl.setProperty("decrypted", true);
            dl.setProperty("type", "zip");
            dl.setProperty("directlink", Encoding.htmlDecode(zipLink));
            decryptedLinks.add(dl);
        }
        // Decrypt file- and folderlinks
        String fpName = br.getRegex("content=\"([^<>/]*?)\" property=\"og:title\"").getMatch(0);
        String emSnippets[][] = br.getRegex("\\('emsnippet-(.*?)'\\).*?=\\s*'(.*?)'").getMatches();
        HashMap<String, String> fileNameMap = new HashMap<String, String>();
        if (emSnippets != null && emSnippets.length > 0) {
            for (String[] fileName : emSnippets) {
                fileNameMap.put(fileName[0], unescape(fileName[1]));
            }
        }
        final String listHTML = br.getRegex("id=\"list\\-view\\-container\" class=\"gallery\\-view\\-section\">(.*?)id=\"modal\\-progress\\-content\">").getMatch(0);
        if (listHTML != null) {
            final String[] entries = new Regex(listHTML, "(class=\"filename\\-col\"><a.*?</span></div>)").getColumn(0);
            if (entries != null && entries.length != 0) {
                for (final String entry : entries) {
                    final String link = new Regex(entry, "<a href=\"(https?://(www\\.)?dropbox\\.com/[^<>\"]*?)\"").getMatch(0);
                    if (entry.contains("class=\"s_web_folder_")) {
                        /* Folder */
                        decryptedLinks.add(createDownloadlink(link));
                    } else {
                        /* File */
                        final String size = new Regex(entry, "class=\"size\">([^<>\"]*?)</span>").getMatch(0);
                        if (link == null || size == null) {
                            continue;
                        }
                        final String filename = new Regex(link, "/([^<>\"/]*?)$").getMatch(0);
                        final DownloadLink dl = createDownloadlink(link.replace("dropbox.com/", "dropboxdecrypted.com/"));
                        if (filename != null) dl.setName(filename);
                        dl.setDownloadSize(SizeFormatter.getSize(size.replace(",", ".")));
                        dl.setProperty("decrypted", true);
                        dl.setAvailable(true);
                        decryptedLinks.add(dl);
                    }
                }
            }
        }
        String sharingModel = br.getRegex("SharingModel\\.init_folder\\(.*?'(.*?)'").getMatch(0);
        sharingModel = unescape(sharingModel);
        if (sharingModel != null && decryptedLinks.size() == 0) {
            /* new js links */
            String links[][] = new Regex(sharingModel, "orig_url\":\\s*\"(http.*?)\".*?\"filename\":\\s*\"(.*?)\"").getMatches();
            for (String fileInfo[] : links) {
                final DownloadLink dl = createDownloadlink(fileInfo[0].replace("dropbox.com/", "dropboxdecrypted.com/"));
                dl.setName(fileInfo[1]);
                dl.setProperty("decrypted", true);
                dl.setAvailable(true);
                decryptedLinks.add(dl);
            }
        }
        if (decryptedLinks.size() == 0) {
            logger.info("Found nothing to download: " + parameter);
            final DownloadLink dl = createDownloadlink(parameter.replace("dropbox.com/", "dropboxdecrypted.com/"));
            dl.setProperty("decrypted", true);
            dl.setProperty("offline", true);
            decryptedLinks.add(dl);
            return decryptedLinks;
        }
        if (fpName != null) {
            if (fpName.contains("\\")) fpName = unescape(fpName);
            FilePackage fp = FilePackage.getInstance();
            fp.setName(Encoding.htmlDecode(fpName.trim()));
            fp.addLinks(decryptedLinks);
        }
        return decryptedLinks;
    }

    private synchronized String unescape(final String s) {
        /* we have to make sure the youtube plugin is loaded */
        if (pluginloaded == false) {
            final PluginForHost plugin = JDUtilities.getPluginForHost("youtube.com");
            if (plugin == null) throw new IllegalStateException("youtube plugin not found!");
            pluginloaded = true;
        }
        return jd.plugins.hoster.Youtube.unescape(s);
    }

    /* NO OVERRIDE!! */
    public boolean hasCaptcha(CryptedLink link, jd.plugins.Account acc) {
        return false;
    }

}