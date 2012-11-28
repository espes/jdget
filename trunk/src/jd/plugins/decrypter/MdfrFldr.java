//    jDownloader - Downloadmanager
//    Copyright (C) 2012  JD-Team support@jdownloader.org
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

import java.io.IOException;
import java.util.ArrayList;

import jd.PluginWrapper;
import jd.controlling.AccountController;
import jd.controlling.ProgressController;
import jd.http.Browser;
import jd.nutils.JDHash;
import jd.nutils.encoding.Encoding;
import jd.parser.Regex;
import jd.plugins.Account;
import jd.plugins.CryptedLink;
import jd.plugins.DecrypterPlugin;
import jd.plugins.DownloadLink;
import jd.plugins.PluginForDecrypt;
import jd.plugins.PluginForHost;
import jd.utils.JDUtilities;

import org.appwork.utils.formatter.SizeFormatter;

@DecrypterPlugin(revision = "$Revision$", interfaceVersion = 2, names = { "mediafire.com" }, urls = { "http://(?!download|blog)(\\w+\\.)?(mediafire\\.com|mfi\\.re)/(?!select_account_type\\.php|reseller|policies|tell_us_what_you_think\\.php|about\\.php|lost_password\\.php|blank\\.html|js/|common_questions/|software/|error\\.php|favicon|acceptable_use_policy\\.php|privacy_policy\\.php|terms_of_service\\.php)(imageview|i/\\?|\\\\?sharekey=|view/\\?|(?!download|file|\\?JDOWNLOADER|imgbnc\\.php)).{4,}" }, flags = { 0 })
public class MdfrFldr extends PluginForDecrypt {

    public MdfrFldr(PluginWrapper wrapper) {
        super(wrapper);
    }

    private String             SESSIONTOKEN  = null;
    public static final String APIKEY        = "czQ1cDd5NWE3OTl2ZGNsZmpkd3Q1eXZhNHcxdzE4c2Zlbmt2djdudw==";
    public static final String APPLICATIONID = "27112";
    private String             ERRORCODE     = null;

    public ArrayList<DownloadLink> decryptIt(CryptedLink param, ProgressController progress) throws Exception {
        ArrayList<DownloadLink> decryptedLinks = new ArrayList<DownloadLink>();
        String parameter = param.toString().replace("mfi.re/", "mediafire.com/").trim();
        if (parameter.matches("http://(\\w+\\.)?mediafire\\.com/view/\\?.+")) parameter = parameter.replace("/view", "");
        if (parameter.endsWith("mediafire.com") || parameter.endsWith("mediafire.com/")) return decryptedLinks;
        parameter = parameter.replaceAll("(&.+)", "").replaceAll("(#.+)", "");
        String fpName = null;
        this.setBrowserExclusive();
        br.getHeaders().put("User-Agent", "Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/535.7 (KHTML, like Gecko) Chrome/16.0.912.75 Safari/535.7");
        if (parameter.matches("http://download\\d+\\.mediafire.+")) {
            /* direct download */
            String ID = new Regex(parameter, "\\.com/\\?(.+)").getMatch(0);
            if (ID == null) ID = new Regex(parameter, "\\.com/.*?/(.*?)/").getMatch(0);
            if (ID != null) {
                DownloadLink link = createDownloadlink("http://www.mediafire.com/download.php?" + ID);
                decryptedLinks.add(link);
                return decryptedLinks;
            }
        }
        if (parameter.contains("imageview.php")) {
            String ID = new Regex(parameter, "\\.com/.*?quickkey=(.+)").getMatch(0);
            if (ID != null) {
                final DownloadLink link = createDownloadlink("http://www.mediafire.com/download.php?" + ID);
                decryptedLinks.add(link);
                return decryptedLinks;
            }
            return null;
        }
        if (parameter.contains("/i/?")) {
            String ID = new Regex(parameter, "\\.com/i/\\?(.+)").getMatch(0);
            if (ID != null) {
                final DownloadLink link = createDownloadlink("http://www.mediafire.com/download.php?" + ID);
                decryptedLinks.add(link);
                return decryptedLinks;
            }
            logger.warning("Decrypter broken for link: " + parameter);
            return null;
        }
        br.setFollowRedirects(false);
        // Private link? Login needed!
        if (getUserLogin()) {
            logger.info("Decrypting with logindata...");
        } else {
            logger.info("Decrypting without logindata...");
        }
        // Not found, either invalid link or filelink
        // if ("112".equals(ERRORCODE)) {
        // final DownloadLink link = createDownloadlink("http://www.mediafire.com/download.php?" + new Regex(parameter,
        // "([a-z0-9]+)$").getMatch(0));
        // decryptedLinks.add(link);
        // return decryptedLinks;
        // }
        if (!br.containsHTML("<result>Success</result>")) {
            logger.info("Either invalid folder or decrypter failure for link: " + parameter);
            return null;
        }
        // Check if we have a single link or multiple folders/files
        final String folderKey = new Regex(parameter, "([a-z0-9]+)$").getMatch(0);
        apiRequest(this.br, "http://www.mediafire.com/api/file/get_info.php", "?quick_key=" + folderKey);
        if ("110".equals(this.ERRORCODE)) {
            apiRequest(this.br, "http://www.mediafire.com/api/folder/get_content.php?folder_key=", folderKey + "&content_type=folders");
            final String[] subFolders = br.getRegex("<folderkey>([a-z0-9]+)</folderkey>").getColumn(0);
            apiRequest(this.br, "http://www.mediafire.com/api/folder/get_content.php?folder_key=", folderKey + "&content_type=files");
            final String[] files = br.getRegex("<file>(.*?)</file>").getColumn(0);
            if ((subFolders == null || subFolders.length == 0) && (files == null || files.length == 0)) {
                logger.warning("Decrypter broken for link: " + parameter);
                return null;
            }
            if (subFolders != null && subFolders.length != 0) {
                for (final String folderID : subFolders) {
                    final DownloadLink link = createDownloadlink("http://www.mediafire.com/?" + folderID);
                    decryptedLinks.add(link);
                }
            }
            if (files != null && files.length != 0) {
                for (final String fileInfo : files) {
                    final DownloadLink link = createDownloadlink("http://www.mediafire.com/download.php?" + getXML("quickkey", fileInfo));
                    link.setDownloadSize(SizeFormatter.getSize(getXML("size", fileInfo) + "b"));
                    link.setName(getXML("filename", fileInfo));
                    if ("private".equals(getXML("privacy", br.toString()))) {
                        link.setProperty("privatefile", true);
                    }
                    link.setAvailable(true);
                    decryptedLinks.add(link);
                }
            }
            return decryptedLinks;
        } else {
            final DownloadLink link = createDownloadlink("http://www.mediafire.com/download.php?" + folderKey);
            decryptedLinks.add(link);
            return decryptedLinks;
        }
    }

    private boolean getUserLogin() throws Exception {
        /*
         * we have to load the plugins first! we must not reference a plugin class without loading it before
         */
        final PluginForHost hosterPlugin = JDUtilities.getPluginForHost("mediafire.com");
        final Account aa = AccountController.getInstance().getValidAccount(hosterPlugin);
        if (aa != null) {
            // Get token for user account
            apiRequest(this.br, "https://www.mediafire.com/api/user/get_session_token.php", "?email=" + Encoding.urlEncode(aa.getUser()) + "&password=" + Encoding.urlEncode(aa.getPass()) + "&application_id=" + APPLICATIONID + "&signature=" + JDHash.getSHA1(aa.getUser() + aa.getPass() + APPLICATIONID + Encoding.Base64Decode(APIKEY)) + "&version=1");
            SESSIONTOKEN = getXML("session_token", br.toString());
            return true;
        }
        return false;
    }

    private void apiRequest(final Browser br, final String url, final String data) throws IOException {
        if (SESSIONTOKEN == null)
            br.getPage(url + data);
        else
            br.getPage(url + data + "&session_token=" + SESSIONTOKEN);
        ERRORCODE = getXML("error", br.toString());
    }

    private String getXML(final String parameter, final String source) {
        return new Regex(source, "<" + parameter + ">([^<>\"]*?)</" + parameter + ">").getMatch(0);
    }
}
