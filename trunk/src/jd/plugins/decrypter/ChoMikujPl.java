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

import jd.PluginWrapper;
import jd.controlling.ProgressController;
import jd.http.Browser;
import jd.nutils.encoding.Encoding;
import jd.parser.Regex;
import jd.plugins.CryptedLink;
import jd.plugins.DecrypterException;
import jd.plugins.DecrypterPlugin;
import jd.plugins.DownloadLink;
import jd.plugins.FilePackage;
import jd.plugins.PluginForDecrypt;

import org.appwork.utils.formatter.SizeFormatter;

@DecrypterPlugin(revision = "$Revision$", interfaceVersion = 2, names = { "chomikuj.pl" }, urls = { "http://(www\\.)?chomikuj\\.pl/.+" }, flags = { 0 })
public class ChoMikujPl extends PluginForDecrypt {

    public ChoMikujPl(PluginWrapper wrapper) {
        super(wrapper);
    }

    private static final String PASSWORDWRONG = ">Nieprawidłowe hasło<";
    private static final String PASSWORDTEXT  = "Ten folder jest <b>zabezpieczony oddzielnym hasłem";
    private static final String VIDEOTEXT     = "GalPage";

    public ArrayList<DownloadLink> decryptIt(CryptedLink param, ProgressController progress) throws Exception {
        ArrayList<DownloadLink> decryptedLinks = new ArrayList<DownloadLink>();
        String parameter = param.toString();
        String problem = null;
        try {
            problem = parameter.substring(parameter.lastIndexOf(","));
        } catch (Exception e) {
        }
        if (problem != null && problem.endsWith(".avi")) parameter = parameter.replace(problem, "");
        parameter = parameter.replace("www.", "");
        // The message used on errors in this plugin
        String error = "Error while decrypting link: " + parameter;
        // If a link is password protected we have to save and use those data in
        // the hosterplugin
        String savePost = null;
        String saveLink = null;
        String password = null;
        br.setFollowRedirects(false);
        br.getPage(parameter);
        // If we have a new link we have to use it or we'll have big problems
        // later when POSTing things to the server
        if (br.getRedirectLocation() != null) {
            parameter = br.getRedirectLocation();
            br.getPage(br.getRedirectLocation());
        }
        // // Check if the link directly wants to access a specified page of the
        // gallery, if so, remove it to avoid problems
        String checkPage = new Regex(parameter, "chomikuj\\.pl/.*?(,\\d+)$").getMatch(0);
        if (checkPage != null) {
            br.getPage(parameter.replace(checkPage, ""));
            if (br.getRedirectLocation() == null) {
                parameter = parameter.replace(checkPage, "");
            } else {
                br.getPage(parameter);
            }
        }
        String fpName = br.getRegex("<title>(.*?) \\- .*? \\- Chomikuj\\.pl.*?</title>").getMatch(0);
        if (fpName == null) {
            fpName = br.getRegex("class=\"T_selected\">(.*?)</span>").getMatch(0);
            if (fpName == null) {
                fpName = br.getRegex("<span id=\"ctl00_CT_FW_SelectedFolderLabel\" style=\"font\\-weight:bold;\">(.*?)</span>").getMatch(0);
            }
        }
        String viewState = br.getRegex("id=\"__VIEWSTATE\" value=\"(.*?)\"").getMatch(0);
        String chomikId = br.getRegex("id=\"ctl00_CT_ChomikID\" value=\"(.*?)\"").getMatch(0);
        String subFolderID = br.getRegex("id=\"ctl00_CT_FW_SubfolderID\" value=\"(.*?)\"").getMatch(0);
        String treeExpandLog = br.getRegex("RefreshTreeAfterOptionsChange\\',\\'\\'\\)\" style=\"display: none\"></a>[\t\n\r ]+<input type=\"hidden\" value=\"(.*?)\" id=\"treeExpandLog\"").getMatch(0);
        if (subFolderID == null) subFolderID = br.getRegex("name=\"ChomikSubfolderId\" type=\"hidden\" value=\"(.*?)\"").getMatch(0);
        if (subFolderID == null || fpName == null || chomikId == null || viewState == null || treeExpandLog == null) {
            logger.warning(error);
            return null;
        }
        fpName = fpName.trim();
        subFolderID = subFolderID.trim();
        // Alle Haupt-POSTdaten
        String postdata = "ctl00%24SM=ctl00%24CT%24FW%24FoldersUp%7Cctl00%24CT%24FW%24RefreshButton&__EVENTTARGET=ctl00%24CT%24FW%24RefreshButton&__EVENTARGUMENT=&__VIEWSTATE=" + Encoding.urlEncode(viewState) + "&PageCmd=&PageArg=undefined&ctl00%24LoginTop%24LoginChomikName=&ctl00%24LoginTop%24LoginChomikPassword=&ctl00%24SearchInputBox=nazwa%20lub%20e-mail&ctl00%24SearchFileBox=nazwa%20pliku&ctl00%24SearchType=all&SType=0&ctl00%24CT%24ChomikID=" + chomikId + "&ctl00%24CT%24PermW%24LoginCtrl%24PF=&treeExpandLog=&" + Encoding.urlEncode(treeExpandLog) + "&ChomikSubfolderId=" + subFolderID + "&tl00%24CT%24TW%24TreeExpandLog=" + "&ctl00%24CT%24FW%24SubfolderID=" + subFolderID + "&FVSortType=1&FVSortDir=1&FVSortChange=&FVPage=%jdownloaderpage%&ctl00%24CT%24FW%24inpFolderAddress=" + Encoding.urlEncode(parameter) + "&ctl00%24CT%24FrW%24FrPage=0&FrGroupId=0&__ASYNCPOST=true&";
        // Needed for page-change for videolinks
        if (br.containsHTML(VIDEOTEXT)) postdata = postdata.replace("&FVPage=", "&GalPage=");
        // Passwort Handling
        if (br.containsHTML(PASSWORDTEXT)) {
            prepareBrowser(parameter, br);
            for (int i = 0; i <= 3; i++) {
                password = getUserInput(null, param);
                savePost = postdata + "&ctl00%24SM=ctl00%24CT%24FW%24FilesUpdatePanel%7Cctl00%24CT%24FW%24FolderLoginButton&ctl00%24CT%24FW%24FolderLoginButton=Wejd%C5%BA&ctl00%24CT%24FW%24FolderPass=" + Encoding.urlEncode(password);
                br.postPage(parameter, savePost);
                if (br.containsHTML(PASSWORDWRONG)) continue;
                break;
            }
            if (br.containsHTML(PASSWORDWRONG)) {
                logger.warning("Wrong password!");
                throw new DecrypterException(DecrypterException.PASSWORD);
            }
            saveLink = parameter;
        }
        logger.info("Looking how many pages we got here for link " + parameter + " ...");
        // Herausfinden wie viele Seiten der Link hat
        int pageCount = getPageCount(postdata, parameter);
        if (pageCount == -1) {
            logger.warning("Error, couldn't successfully find the number of pages for link: " + parameter);
            return null;
        }
        logger.info("Found " + pageCount + " pages. Starting to decrypt them now.");
        progress.setRange(pageCount);
        final String linkPart = new Regex(parameter, "chomikuj\\.pl(/.+)").getMatch(0);
        // Alle Seiten decrypten
        for (int i = 0; i <= pageCount; ++i) {
            logger.info("Decrypting page " + i + " of link: " + parameter);
            String postThatData = postdata.replace("%jdownloaderpage%", Integer.toString(i));
            prepareBrowser(parameter, br);
            br.postPage(parameter, postThatData);
            // Every full page has 30 links (pictures)
            boolean filenameIncluded = true;
            String[][] fileIds = br.getRegex("class=\"FileName\" onclick=\"return ch\\.Download\\.dnFile\\((.*?)\\);\"><b>(.{1,300})</b>(.{1,300})</a>[\t\n\r ]+</td>[\t\n\r ]+<td>[\t\n\r ]+<table cellpadding=\"0\" cellspacing=\"3\" class=\"fInfoTable\">[\t\n\r ]+<tr>[\t\n\r ]+<td><div class=\"fInfoDiv\">(.{1,20})</div></td>").getMatches();
            if (fileIds == null || fileIds.length == 0) {
                filenameIncluded = false;
                fileIds = br.getRegex("class=\"FileName\" onclick=\"return ch\\.Download\\.dnFile\\((.*?)\\);\"").getMatches();
                if (fileIds == null || fileIds.length == 0) {
                    fileIds = br.getRegex("class=\"fileItemProp getFile\" onclick=\"return ch\\.Download\\.dnFile\\((\\d+)\\);\"").getMatches();
                }
            }
            String[][] allFolders = br.getRegex("<td><a href=\"(/[^<>\"/]+/[^<>\"]+)\" onclick=\"return Ts\\(\\'\\d+\\'\\)\">([^<>\"]+)</span></td></tr>").getMatches();
            /**
             * Old regex to get video IDs (IDs only): videoIDs =
             * br.getRegex("ShowVideo\\.aspx\\?id=(\\d+)\\'").getMatches();
             */
            if ((fileIds == null || fileIds.length == 0) && (allFolders == null || allFolders.length == 0)) {
                // If the last page only contains a file or fileS the regexes
                // don't work but the decrypter isn't broken so the user should
                // get the links!
                if (br.containsHTML("\\.zip")) {
                    logger.info("Stopping at page " + i + " because there were no pictures found on the page but i did find a .zip file!");
                    return decryptedLinks;
                }
                logger.warning(error);
                return null;
            }
            if (fileIds != null && fileIds.length != 0) {
                for (String[] id : fileIds) {
                    String finalLink = String.format("&id=%s&gallerylink=%s&", id[0], param.toString().replace("chomikuj.pl", "60423fhrzisweguikipo9re"));
                    DownloadLink dl = createDownloadlink(finalLink);
                    if (filenameIncluded) {
                        dl.setName(id[1].trim() + id[2].trim());
                        dl.setDownloadSize(SizeFormatter.getSize(id[3].replace(",", ".")));
                        dl.setAvailable(true);
                        /**
                         * If the link is a video it needs other
                         * downloadhandling
                         */
                        if (id[2].trim().matches("\\.(avi|flv|mp4|mpg|rmvb|divx|wmv|mkv)")) dl.setProperty("video", "true");
                    } else {
                        dl.setName(String.valueOf(new Random().nextInt(1000000)));
                    }
                    if (saveLink != null && savePost != null && password != null) {
                        dl.setProperty("savedlink", saveLink);
                        dl.setProperty("savedpost", savePost);
                        // Not needed yet but might me useful in the future
                        dl.setProperty("password", password);
                    }
                    decryptedLinks.add(dl);
                }
            }
            if (allFolders != null && allFolders.length != 0) {
                for (String[] folder : allFolders) {
                    String folderLink = folder[0];
                    folderLink = "http://chomikuj.pl" + folderLink;
                    if (folderLink.contains(linkPart) && !folderLink.equals(parameter)) {
                        decryptedLinks.add(createDownloadlink(folderLink));
                    }
                }
            }
            progress.increase(1);
        }
        FilePackage fp = FilePackage.getInstance();
        fp.setName(fpName);
        fp.addLinks(decryptedLinks);
        return decryptedLinks;
    }

    public int getPageCount(String postdata, String theParameter) throws NumberFormatException, DecrypterException, IOException {
        Browser br2 = br.cloneBrowser();
        prepareBrowser(theParameter, br2);
        br2.getPage(theParameter);
        int pageCount = 0;
        int tempint = 0;
        // Loop limited to 20 in case something goes seriously wrong
        for (int i = 0; i <= 20; i++) {
            // Find number of pages
            String pagePiece = br2.getRegex("class=\"navigation\"><table id=\"ctl00_CT_FW_FV_NavTop_NavTable\" border=\"0\">(.*?)</table></div>").getMatch(0);
            if (pagePiece == null) pagePiece = br2.getRegex("id=\"ctl00_CT_FW_FGNavBottom_NavTable\" border=\"0\">[\t\n\r ]+<tr>[\t\n\r ]+<td><span class=\"na\">\\&laquo; <b>poprzednia</b> strona</span></td><td style=\"width:\\d+%;\"></td><td><span>1</span></td><td><a (onclick=.*?</tr>[\t\n\r ]+</table></div>)").getMatch(0);
            if (pagePiece == null) {
                logger.info("pagePiece is null so we should only have one page for this link...");
                pageCount = 0;
                break;
            }
            String[] lolpages = null;
            if (pagePiece != null) lolpages = new Regex(pagePiece, "(ch\\.changeFilesPage|changeGalPageBottom)\\((\\d+)\\)").getColumn(1);
            if (lolpages == null || lolpages.length == 0) return -1;
            // Find highest number of page
            for (String page : lolpages) {
                if (Integer.parseInt(page) > tempint) tempint = Integer.parseInt(page);
            }
            if (tempint == pageCount) break;
            // If we find less than 6-7 pages there are no more so we have to
            // stop this here before getting an error!
            if (tempint < 7) {
                pageCount = tempint;
                break;
            }
            String postThoseData = postdata + tempint;
            br2.postPage(theParameter, postThoseData);
            pageCount = tempint;
        }
        return pageCount;
    }

    private void prepareBrowser(String parameter, Browser bro) {
        // Not needed but has been implemented so lets use it
        bro.getHeaders().put("Referer", parameter);
        bro.getHeaders().put("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8");
        bro.getHeaders().put("Accept-Language", "de-de,de;q=0.8,en-us;q=0.5,en;q=0.3");
        bro.getHeaders().put("Accept-Encoding", "gzip,deflate");
        bro.getHeaders().put("Accept-Charset", "ISO-8859-1,utf-8;q=0.7,*;q=0.7");
        bro.getHeaders().put("Cache-Control", "no-cache, no-cache");
        bro.getHeaders().put("Content-Type", "application/x-www-form-urlencoded; charset=utf-8r ");
        bro.getHeaders().put("X-MicrosoftAjax", "Delta=true");
        bro.getHeaders().put("Pragma", "no-cache");
    }
}
