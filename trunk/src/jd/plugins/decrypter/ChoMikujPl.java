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

import java.io.IOException;
import java.util.ArrayList;
import java.util.Random;
import java.util.regex.Pattern;

import jd.PluginWrapper;
import jd.config.Property;
import jd.controlling.ProgressController;
import jd.http.Browser;
import jd.nutils.encoding.Encoding;
import jd.parser.Regex;
import jd.parser.html.Form;
import jd.plugins.CryptedLink;
import jd.plugins.DecrypterException;
import jd.plugins.DecrypterPlugin;
import jd.plugins.DownloadLink;
import jd.plugins.FilePackage;
import jd.plugins.PluginForDecrypt;
import jd.plugins.PluginForHost;
import jd.utils.JDUtilities;

import org.appwork.utils.formatter.SizeFormatter;

@DecrypterPlugin(revision = "$Revision$", interfaceVersion = 2, names = { "chomikuj.pl" }, urls = { "http://((www\\.)?chomikuj\\.pl//?(?!action/)[^<>\"/]+/.+|/chomikujpagedecrypt\\.pl/.*?,\\d+$)" }, flags = { 0 })
public class ChoMikujPl extends PluginForDecrypt {

    public ChoMikujPl(PluginWrapper wrapper) {
        super(wrapper);
    }

    private static final String PASSWORDTEXT             = "Ten folder jest (<b>)?zabezpieczony oddzielnym hasłem";
    private String              FOLDERPASSWORD           = null;
    private ArrayList<Integer>  REGEXSORT                = new ArrayList<Integer>();
    private String              ERROR                    = "Decrypter broken for link: ";
    private String              REQUESTVERIFICATIONTOKEN = null;
    private static final String PAGEDECRYPTLINK          = "http://chomikujpagedecrypt\\.pl/.*?\\d+";
    private static final String ENDINGS                  = "\\.(3gp|7zip|7z|abr|ac3|aiff|aifc|aif|ai|au|avi|bin|bat|bz2|cbr|cbz|ccf|chm|cso|cue|cvd|dta|deb|divx|djvu|dlc|dmg|doc|docx|dot|eps|epub|exe|ff|flv|flac|f4v|gsd|gif|gz|iwd|idx|iso|ipa|ipsw|java|jar|jpg|jpeg|load|m2ts|mws|mv|m4v|m4a|mkv|mp2|mp3|mp4|mobi|mov|movie|mpeg|mpe|mpg|mpq|msi|msu|msp|nfo|npk|oga|ogg|ogv|otrkey|par2|pkg|png|pdf|pptx|ppt|pps|ppz|pot|psd|qt|rmvb|rm|rar|ram|ra|rev|rnd|[r-z]\\d{2}|r\\d+|rpm|run|rsdf|reg|rtf|shnf|sh(?!tml)|ssa|smi|sub|srt|snd|sfv|swf|tar\\.gz|tar\\.bz2|tar\\.xz|tar|tgz|tiff|tif|ts|txt|viv|vivo|vob|webm|wav|wmv|wma|xla|xls|xpi|zeno|zip)";
    private static final String VIDEOENDINGS             = "\\.(avi|flv|mp4|mpg|rmvb|divx|wmv|mkv)";

    public ArrayList<DownloadLink> decryptIt(CryptedLink param, ProgressController progress) throws Exception {
        ArrayList<DownloadLink> decryptedLinks = new ArrayList<DownloadLink>();
        String parameter = param.toString().replace("chomikujpagedecrypt.pl/", "chomikuj.pl/").replace("chomikuj.pl//", "chomikuj.pl/");
        String linkending = null;
        if (parameter.contains(",")) linkending = parameter.substring(parameter.lastIndexOf(","));
        if (linkending == null) linkending = parameter.substring(parameter.lastIndexOf("/") + 1);
        /** Correct added link */
        parameter = parameter.replace("www.", "");
        br.setFollowRedirects(false);

        /** Handle single links */
        if (linkending != null) {
            String tempExt = null;
            if (linkending.contains(".")) tempExt = linkending.substring(linkending.lastIndexOf("."));
            final boolean isLinkendingWithoutID = (!linkending.contains(",") && tempExt != null && new Regex(tempExt, Pattern.compile(ENDINGS, Pattern.CASE_INSENSITIVE)).matches());
            if (linkending.matches(",\\d+\\.[A-Za-z0-9]{1,5}") || isLinkendingWithoutID) {
                /**
                 * If the ID is missing but it's a single link we have to access
                 * the link to get it's read link and it's download ID.
                 */
                if (isLinkendingWithoutID) {
                    br.getPage(parameter);
                    final String orgLink = br.getRegex("property=\"og:url\" content=\"(http://(www\\.)?chomikuj\\.pl/[^<>\"]*?)\"").getMatch(0);
                    if (orgLink != null && orgLink.contains(",")) {
                        linkending = orgLink.substring(orgLink.lastIndexOf(","));
                        if (!linkending.matches(",\\d+\\.[A-Za-z0-9]{1,5}")) {
                            logger.warning("SingleLink handling failed for link: " + parameter);
                            return null;
                        }
                        parameter = orgLink;
                    } else {
                        logger.warning("SingleLink handling failed for link: " + parameter);
                        return null;
                    }
                }
                final DownloadLink dl = createDownloadlink(parameter.replace("chomikuj.pl/", "chomikujdecrypted.pl/") + "," + System.currentTimeMillis() + new Random().nextInt(100000));
                final Regex info = new Regex(parameter, "/([^<>\"/]*?),(\\d+)(\\..+)$");
                final String filename = Encoding.htmlDecode(info.getMatch(0)) + info.getMatch(2);
                final String fileid = info.getMatch(1);
                String ext = null;
                if (filename.contains(".")) ext = filename.substring(filename.lastIndexOf("."));

                dl.setProperty("fileid", fileid);
                dl.setName(filename);
                if ((ext != null && ext.length() <= 5) && ext.matches(VIDEOENDINGS)) dl.setProperty("video", true);
                try {
                    distribute(dl);
                } catch (final Throwable e) {
                    /* does not exist in 09581 */
                }
                decryptedLinks.add(dl);
                return decryptedLinks;
            } else {
                // Or it's just a specified page of a folder, we remove that to
                // prevent problems!
                parameter = parameter.replace(linkending, "");
            }
        }

        br.getPage(parameter);

        // Check for redirect and apply new link
        String redirect = br.getRedirectLocation();
        if (redirect != null) {
            parameter = redirect;
            br.getPage(parameter);
        }

        /** Handle single links 2 */
        String ext = parameter.substring(parameter.lastIndexOf("."));
        if (ext != null && ext.length() <= 5 && ext.matches(ENDINGS)) {
            br.getPage(parameter);
            redirect = br.getRedirectLocation();
            if (redirect != null) {
                // Maybe direct link is no direct link anymore?!
                ext = redirect.substring(redirect.lastIndexOf("."));
                if (ext == null || ext.length() > 5 || !ext.matches(ENDINGS)) {
                    logger.info("Link offline: " + parameter);
                    return decryptedLinks;
                }
                br.getPage(redirect);
            }

            // Check if link can be decrypted
            final String cantDecrypt = getError();
            if (cantDecrypt != null) {
                logger.info(String.format(cantDecrypt, parameter));
                return decryptedLinks;
            }

            final String filename = br.getRegex("Download: <b>([^<>\"]*?)</b>").getMatch(0);
            final String filesize = br.getRegex("<p class=\"fileSize\">([^<>\"]*?)</p>").getMatch(0);
            final String fid = br.getRegex("id=\"fileDetails_(\\d+)\"").getMatch(0);
            if (filename == null || filesize == null || fid == null) {
                logger.warning("Decrypter broken for link: " + parameter);
                return null;
            }
            final DownloadLink dl = createDownloadlink(parameter.replace("chomikuj.pl/", "chomikujdecrypted.pl/") + "," + System.currentTimeMillis() + new Random().nextInt(100000));
            dl.setProperty("fileid", fid);
            dl.setName(correctFilename(Encoding.htmlDecode(filename)));
            dl.setDownloadSize(SizeFormatter.getSize(Encoding.htmlDecode(filesize.trim().replace(",", "."))));
            dl.setAvailable(true);
            dl.setProperty("requestverificationtoken", REQUESTVERIFICATIONTOKEN);
            try {
                distribute(dl);
            } catch (final Throwable e) {
                /* does not exist in 09581 */
            }
            decryptedLinks.add(dl);
            return decryptedLinks;
        }

        // Check if link can be decrypted
        final String cantDecrypt = getError();
        if (cantDecrypt != null) {
            logger.info(String.format(cantDecrypt, parameter));
            return decryptedLinks;
        }

        // If we have a new link we have to use it or we'll have big problems
        // later when POSTing things to the server
        if (br.getRedirectLocation() != null) {
            parameter = br.getRedirectLocation();
            br.getPage(br.getRedirectLocation());
        }
        /** Get needed values */
        String fpName = br.getRegex("<title>(.*?) \\- .*? \\- Chomikuj\\.pl.*?</title>").getMatch(0);
        if (fpName == null) {
            fpName = br.getRegex("class=\"T_selected\">(.*?)</span>").getMatch(0);
            if (fpName == null) {
                fpName = br.getRegex("<span id=\"ctl00_CT_FW_SelectedFolderLabel\" style=\"font\\-weight:bold;\">(.*?)</span>").getMatch(0);
            }
        }
        String chomikID = br.getRegex("name=\"chomikId\" type=\"hidden\" value=\"(\\d+)\"").getMatch(0);
        if (chomikID == null) {
            chomikID = br.getRegex("id=\"__accno\" name=\"__accno\" type=\"hidden\" value=\"(\\d+)\"").getMatch(0);
            if (chomikID == null) {
                chomikID = br.getRegex("name=\"friendId\" type=\"hidden\" value=\"(\\d+)\"").getMatch(0);
                if (chomikID == null) {
                    chomikID = br.getRegex("\\&amp;chomikId=(\\d+)\"").getMatch(0);
                }
            }
        }
        String folderID = br.getRegex("type=\"hidden\" name=\"FolderId\" value=\"(\\d+)\"").getMatch(0);
        if (folderID == null) folderID = br.getRegex("name=\"folderId\" type=\"hidden\" value=\"(\\d+)\"").getMatch(0);
        REQUESTVERIFICATIONTOKEN = br.getRegex("<input name=\"__RequestVerificationToken\" type=\"hidden\" value=\"([^<>\"\\']+)\"").getMatch(0);
        if (REQUESTVERIFICATIONTOKEN == null) {
            logger.warning(ERROR + parameter);
            return null;
        }
        if (folderID == null || fpName == null) {
            logger.warning(ERROR + parameter);
            return null;
        }

        fpName = fpName.trim();
        // All Main-POSTdata
        String postdata = "chomikId=" + chomikID + "&folderId=" + folderID + "&__RequestVerificationToken=" + Encoding.urlEncode(REQUESTVERIFICATIONTOKEN);
        final FilePackage fp = FilePackage.getInstance();
        // Make only one package
        fp.setProperty("ALLOW_MERGE", true);
        fp.setName(fpName);
        decryptedLinks = decryptAll(parameter, postdata, param, fp);
        return decryptedLinks;
    }

    private String getError() {
        String error = null;
        if (br.containsHTML("label for=\"Password\">Hasło</label><input id=\"Password\"")) {
            error = "Password protected links can't be decrypted: %s";
        } else if (br.containsHTML("Konto czasowo zablokowane")) {
            error = "Can't decrypt link, the account of the owner is banned: %s";
        } else if (br.containsHTML("Chomik o takiej nazwie nie istnieje<|Nie znaleziono - błąd 404")) {
            error = "This link is offline (received error 404): %s";
        } else if (br.containsHTML("\"/action/UserAccess/ShowAdultContentConfirmation\"")) {
            error = "This link is only available via account: %s";
        }
        return error;
    }

    private ArrayList<DownloadLink> decryptAll(final String parameter, final String postdata, final CryptedLink param, final FilePackage fp) throws Exception {
        br.setFollowRedirects(true);
        ArrayList<DownloadLink> decryptedLinks = new ArrayList<DownloadLink>();
        String savePost = postdata;
        String saveLink = null;
        final PluginForHost chomikujpl = JDUtilities.getPluginForHost("chomikuj.pl");
        final boolean decryptFolders = chomikujpl.getPluginConfig().getBooleanProperty(jd.plugins.hoster.ChoMikujPl.DECRYPTFOLDERS, false);
        // Password handling
        if (br.containsHTML(PASSWORDTEXT)) {
            prepareBrowser(parameter, br);
            final Form pass = br.getFormbyProperty("id", "LoginToFolder");
            if (pass == null) {
                logger.warning(ERROR + " :: Can't find Password Form!");
                return null;
            }
            for (int i = 0; i <= 3; i++) {
                FOLDERPASSWORD = param.getStringProperty("password");
                if (FOLDERPASSWORD == null) {
                    FOLDERPASSWORD = getUserInput(null, param);
                }
                pass.put("Password", FOLDERPASSWORD);
                br.submitForm(pass);
                if (br.containsHTML("\\{\"IsSuccess\":true")) {
                    break;
                } else {
                    // Maybe password was saved before but has changed in the
                    // meantime!
                    param.setProperty("password", Property.NULL);
                    continue;
                }
            }
            if (!br.containsHTML("\\{\"IsSuccess\":true")) {
                logger.warning("Wrong password!");
                throw new DecrypterException(DecrypterException.PASSWORD);
            }
            saveLink = parameter;
        }
        logger.info("Looking how many pages we got here for link " + parameter + " ...");

        // Herausfinden wie viele Seiten der Link hat
        int pageCount = 1;
        if (param.toString().matches(PAGEDECRYPTLINK)) {
            pageCount = Integer.parseInt(new Regex(param.toString(), ",(\\d+)$").getMatch(0));
        } else {
            pageCount = getPageCount(parameter);
        }
        if (pageCount == -1) {
            logger.warning("Error, couldn't successfully find the number of pages for link: " + parameter);
            return null;
        } else if (pageCount == 0) pageCount = 1;

        // More than one page? Every page goes back into the decrypter as a
        // single link!
        if (pageCount > 1 && !param.toString().matches(PAGEDECRYPTLINK)) {
            logger.info("Found " + pageCount + " pages. Adding those for the decryption now.");
            for (int i = 1; i <= pageCount; i++) {
                final DownloadLink dl = createDownloadlink(parameter.replace("chomikuj.pl/", "chomikujpagedecrypt.pl/") + "," + i);
                dl.setProperty("reallink", parameter);
                fp.add(dl);
                try {
                    distribute(dl);
                } catch (final Throwable e) {
                    /* does not exist in 09581 */
                }
                decryptedLinks.add(dl);
            }
        } else {
            final String linkPart = new Regex(parameter, "chomikuj\\.pl(/.+)").getMatch(0);
            /** Decrypt all pages, start with 1 (not 0 as it was before) */
            logger.info("Decrypting page " + pageCount + " of link: " + parameter);
            final Browser tempBr = br.cloneBrowser();
            prepareBrowser(parameter, tempBr);
            /** Only request further pages is folder isn't password protected */
            if (FOLDERPASSWORD != null) {
                tempBr.getPage(parameter);
            } else {
                accessPage(postdata, tempBr, pageCount);
            }
            String[][] allFolders = null;
            final String folderTable = tempBr.getRegex("<div id=\"foldersList\">[\t\n\r ]+<table>(.*?)</table>[\t\n\r ]+</div>").getMatch(0);
            if (folderTable != null) {
                allFolders = new Regex(folderTable, "<a href=\"(/[^<>\"]*?)\" rel=\"\\d+\" title=\"([^<>\"]*?)\"").getMatches();
            }
            // Every full page has 30 links
            /** For photos */
            String[][] fileIds = tempBr.getRegex("<div class=\"left\">[\t\n\r ]+<p class=\"filename\">[\t\n\r ]+<a class=\"downloadAction\" href=\"[^<>\"\\']+\"> +<span class=\"bold\">(.{1,300})</span>(\\..{1,20})</a>[\t\n\r ]+</p>[\t\n\r ]+<div class=\"thumbnail\">.*?title=\"([^<>\"]*?)\".*?</div>[\t\n\r ]+<div class=\"smallTab\">[\t\n\r ]+<ul class=\"tabGradientBg borderRadius\">[\t\n\r ]+<li>([^<>\"\\'/]+)</li>.*?class=\"galeryActionButtons visibleOpt fileIdContainer\" rel=\"(\\d+)\"").getMatches();
            addRegexInt(0, 1, 3, 4, 2);
            if (fileIds == null || fileIds.length == 0) {
                /**
                 * Specified for videos (also works for mp3s, maybe also for
                 * other types)
                 */
                fileIds = tempBr.getRegex("<ul class=\"borderRadius tabGradientBg\">[\t\n\r ]+<li><span>([^<>\"\\']+)</span></li>[\t\n\r ]+<li><span class=\"date\">[^<>\"\\']+</span></li>[\t\n\r ]+</ul>[\t\n\r ]+</div>[\t\n\r ]+<div class=\"fileActionsButtons clear visibleButtons  fileIdContainer\" rel=\"(\\d+)\" style=\"visibility: hidden;\">.*?class=\"expanderHeader downloadAction\" href=\"[^<>\"\\']+\" title=\"[^<>\"\\']+\">[\t\n\r ]+<span class=\"bold\">([^<>\"\\']*?(<span class=\"e\"> </span>[^<>\"\\']*?)?)</span>([^<>\"\\']+)</a>[\t\n\r ]+<img alt=\"pobierz\" class=\"downloadArrow visibleArrow\" src=\"").getMatches();
                addRegexInt(2, 4, 0, 1, 0);
                /**
                 * Last attempt, only get IDs (no pre-available-check possible)
                 */
                if (fileIds == null || fileIds.length == 0) {
                    fileIds = tempBr.getRegex("fileIdContainer\" rel=\"(\\d+)\"").getMatches();
                }
            }
            if ((fileIds == null || fileIds.length == 0) && (allFolders == null || allFolders.length == 0)) {
                if (tempBr.containsHTML("class=\"noFile\">Nie ma plik\\&#243;w w tym folderze</p>")) {
                    logger.info("The following link is offline: " + parameter);
                    return decryptedLinks;
                }
                logger.warning(ERROR);
                return null;
            }
            if (fileIds != null && fileIds.length != 0) {
                for (String[] id : fileIds) {
                    final DownloadLink dl = createDownloadlink(parameter.replace("chomikuj.pl/", "chomikujdecrypted.pl/") + "," + System.currentTimeMillis() + new Random().nextInt(100000));
                    dl.setProperty("fileid", id[REGEXSORT.get(3)]);
                    if (id.length > 1) {
                        if (id.length == 6) {
                            dl.setName(correctFilename(Encoding.htmlDecode(id[REGEXSORT.get(4)].trim())));
                        } else {
                            dl.setName(correctFilename(Encoding.htmlDecode(id[REGEXSORT.get(0)].trim()) + id[REGEXSORT.get(1)].trim()));

                        }
                        dl.setDownloadSize(SizeFormatter.getSize(id[REGEXSORT.get(2)].replace(",", ".")));
                        dl.setAvailable(true);
                        /**
                         * If the link is a video it needs other download
                         * handling
                         */
                        if (id[REGEXSORT.get(1)].trim().matches(VIDEOENDINGS)) dl.setProperty("video", true);
                    } else {
                        dl.setName(String.valueOf(new Random().nextInt(1000000)));
                    }
                    if (saveLink != null && savePost != null && FOLDERPASSWORD != null) {
                        dl.setProperty("savedlink", saveLink);
                        dl.setProperty("savedpost", savePost);
                        // Not needed yet but might me useful in the future
                        dl.setProperty("password", FOLDERPASSWORD);
                    }
                    dl.setProperty("requestverificationtoken", REQUESTVERIFICATIONTOKEN);
                    fp.add(dl);
                    try {
                        distribute(dl);
                    } catch (final Throwable e) {
                        /* does not exist in 09581 */
                    }
                    decryptedLinks.add(dl);
                }
            }
            if (decryptFolders && allFolders != null && allFolders.length != 0) {
                for (String[] folder : allFolders) {
                    String folderLink = folder[0];
                    folderLink = "http://chomikuj.pl" + folderLink;
                    if (folderLink.contains(linkPart) && !folderLink.equals(parameter)) {
                        final DownloadLink dl = createDownloadlink(folderLink);
                        fp.add(dl);
                        try {
                            distribute(dl);
                        } catch (final Throwable e) {
                            /* does not exist in 09581 */
                        }
                        decryptedLinks.add(dl);
                    }
                }
            }
        }
        return decryptedLinks;
    }

    private void addRegexInt(int filename, int filenameExt, int filesize, int fileid, int fullfilename) {
        REGEXSORT.clear();
        REGEXSORT.add(filename);
        REGEXSORT.add(filenameExt);
        REGEXSORT.add(filesize);
        REGEXSORT.add(fileid);
        REGEXSORT.add(fullfilename);
    }

    public int getPageCount(final String theParameter) throws NumberFormatException, DecrypterException, IOException {
        final Browser br2 = br.cloneBrowser();
        prepareBrowser(theParameter, br2);
        br2.setFollowRedirects(false);
        br2.getPage(theParameter + ",20000");
        final String result = br2.getRedirectLocation();
        if (result == null) {
            logger.info("Couldn't find any pages, returning 1");
            return 1;
        }
        final String pageCount = new Regex(result, ",(\\d+)$").getMatch(0);
        // Only 1 page
        if (pageCount == null) return 1;
        return Integer.parseInt(pageCount);
    }

    private void accessPage(String postData, Browser pageBR, int pageNum) throws IOException {
        pageBR.postPage("http://chomikuj.pl/action/Files/FilesList", postData + "&pageNr=" + pageNum);
    }

    private void prepareBrowser(String parameter, Browser bro) {
        // Not needed but has been implemented so lets use it
        bro.getHeaders().put("Referer", parameter);
        bro.getHeaders().put("Accept", "*/*");
        bro.getHeaders().put("Accept-Language", "de-de,de;q=0.8,en-us;q=0.5,en;q=0.3");
        bro.getHeaders().put("Accept-Encoding", "gzip,deflate");
        bro.getHeaders().put("Accept-Charset", "ISO-8859-1,utf-8;q=0.7,*;q=0.7");
        bro.getHeaders().put("Cache-Control", "no-cache");
        bro.getHeaders().put("Content-Type", "application/x-www-form-urlencoded; charset=UTF-8");
        bro.getHeaders().put("X-Requested-With", "XMLHttpRequest");
        bro.getHeaders().put("Pragma", "no-cache");
    }

    private String correctFilename(final String filename) {
        return filename.replace("<span class=\"e\"> </span>", "");
    }
}
