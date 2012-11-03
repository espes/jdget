//    jDownloader - Downloadmanager
//    Copyright (C) 2011  JD-Team support@jdownloader.org
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
//
package jd.plugins.decrypter;

import java.util.ArrayList;
import java.util.List;

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

@DecrypterPlugin(revision = "$Revision$", interfaceVersion = 2, names = { "israbox.com" }, urls = { "http://[\\w\\.]*israbox\\.com/[0-9]+-.*?\\.html" }, flags = { 0 })
public class SrBoxCom extends PluginForDecrypt {

    public SrBoxCom(PluginWrapper wrapper) {
        super(wrapper);
    }

    public ArrayList<DownloadLink> decryptIt(CryptedLink param, ProgressController progress) throws Exception {
        // Logger logDebug = JDLogger.getLogger();
        ArrayList<DownloadLink> decryptedLinks = new ArrayList<DownloadLink>();
        String parameter = param.toString();
        br.setFollowRedirects(false);
        br.getPage(parameter);
        if (br.containsHTML("(An error has occurred|The article cannot be found)")) throw new DecrypterException(JDL.L("plugins.decrypt.errormsg.unavailable", "Perhaps wrong URL or the download is not available anymore."));
        String fpName = br.getRegex("<h1><a href.*>(.*?)</a></h1>").getMatch(0);
        if (fpName == null) {
            fpName = br.getRegex("<title>(.*?)</title>").getMatch(0);
            if (fpName == null) {
                fpName = br.getRegex("<b>Download Fast:(.*?)</b>").getMatch(0);
                if (fpName == null) {
                    fpName = br.getRegex("color=\"#ffffff\" size=\"1\">(.*?)free from rapidshare").getMatch(0);
                }
            }
        }
        // On this site, when an accent is set, the result is as extended ASCII
        // character which is bad in the name of the package
        fpName = Encoding.htmlDecode(fpName);
        fpName = RemoveCharacter(fpName);
        fpName = CapitalLetterForEachWords(fpName);

        // Array of image to download the cover (It can be usable if the user
        // want to create a subfolder with the name of the package because
        // the folder is immediately created because it will download the cover
        // in it
        String[] TabImage = br.getRegex("<img src=\"http://[\\w\\.]*?israbox\\.com/uploads(.*?)\"").getColumn(0);

        // Creation of the array of link that is supported by all plug-in
        String[] links = br.getRegex("<a href=\"(.*?)\"").getColumn(0);
        if (links == null || links.length == 0) return null;

        // Number of picture
        int iImage = TabImage == null ? 0 : TabImage.length;

        // Some link can be crypted in this site, see if it is the case
        String[] linksCrypted = br.getRegex("\"(http://www\\.israbox\\.com/engine/go\\.php\\?url=.*?)\"").getColumn(0);

        progress.setRange(links.length + iImage + linksCrypted.length);
        // Added links
        for (String redirectlink : links) {
            decryptedLinks.add(createDownloadlink(redirectlink));
            progress.increase(1);
        }

        // Added crypted links
        for (String redirectlink : linksCrypted) {
            br.getPage(redirectlink);
            String finallink = br.getRedirectLocation();
            if (finallink != null) {
                decryptedLinks.add(createDownloadlink(finallink));
                progress.increase(1);
            }
        }

        // Added Image
        if (TabImage != null) {
            for (String strImageLink : TabImage) {
                if (!strImageLink.toLowerCase().contains("foto")) {
                    strImageLink = "http://www.israbox.com/uploads" + strImageLink;

                    DownloadLink DLLink = createDownloadlink(strImageLink, false);
                    String strExtension = "";
                    int iIndex = strImageLink.lastIndexOf('.');
                    if (iIndex > -1) {
                        strExtension = strImageLink.substring(iIndex);
                    }
                    if (strExtension != "") {
                        if (fpName != null) {
                            iIndex = fpName.lastIndexOf(')');
                            String strName = fpName;
                            if (iIndex == fpName.length() - 1) {
                                iIndex = fpName.lastIndexOf(" (");
                                strName = fpName.substring(0, iIndex);
                            }
                            DLLink.setFinalFileName(strName + strExtension);
                        }
                    }
                    decryptedLinks.add(DLLink);
                }
                progress.increase(1);
            }
        }

        for (int i = decryptedLinks.size() - 1; i >= 0; i--) {
            if (decryptedLinks.get(i) == null) {
                decryptedLinks.remove(i);
            }
        }

        // Add all link in a package
        if (fpName != null) {
            FilePackage fp = FilePackage.getInstance();
            fp.setName(fpName.trim());
            fp.addLinks(decryptedLinks);
        }
        return decryptedLinks;
    }

    @Override
    protected DownloadLink createDownloadlink(String link) {
        return createDownloadlink(link, true);
    }

    protected DownloadLink createDownloadlink(String link, Boolean bVerify) {
        if (!link.startsWith("http")) return null;
        if (bVerify && link.startsWith("http://www.israbox.com")) return null;

        return super.createDownloadlink(link);
    }

    /**
     * Allows to remove some character to have a nice name
     * 
     * @param strName
     *            The name of the package
     * @return the name of the package normalized.
     */
    private String RemoveCharacter(String strName) {
        strName = strName.replace("", "-");
        strName = strName.replace("", "'");
        strName = strName.replace(":", ",");

        strName = strName.replace("VA - ", "");
        strName = strName.replace("FLAC", "");
        strName = strName.replace("flac", "");
        strName = strName.replace("APE", "");
        strName = strName.replace("ape", "");

        strName = strName.replace("MP3", "");
        return strName;
    }

    /**
     * Allows to put a capital letter on each words of the title
     * 
     * @param strName
     *            The name of the package
     * @return the name of the package with a capital letter on each words.
     */
    private String CapitalLetterForEachWords(String strName) {
        String strResult = "";
        List<String> FirstCaracException = new ArrayList<String>();
        FirstCaracException.add("(");
        FirstCaracException.add("-");

        String[] AllWord = strName.split(" ");
        for (String strWord : AllWord) {
            strWord = strWord.toLowerCase();
            if (strWord.length() > 0) {
                String strFirstCarac = strWord.substring(0, 1);

                if (FirstCaracException.contains(strFirstCarac)) {
                    if (strWord.length() > 1) {
                        strFirstCarac += strWord.substring(1, 2);
                    }
                }

                try {
                    strFirstCarac = strFirstCarac.toUpperCase();
                } catch (Exception e) {
                }
                strResult += strFirstCarac + strWord.substring(strFirstCarac.length(), strWord.length()) + " ";
            }
        }
        if (strResult != "")
            // Remove the last space introduces in the loop
            strResult = strResult.substring(0, strResult.length() - 1);
        else
            // If no result, we return the name pass to the function
            strResult = strName;
        return strResult;
    }
}
