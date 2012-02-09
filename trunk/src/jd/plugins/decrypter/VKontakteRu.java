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

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import jd.PluginWrapper;
import jd.config.Property;
import jd.controlling.ProgressController;
import jd.gui.UserIO;
import jd.http.Cookie;
import jd.http.Cookies;
import jd.nutils.encoding.Encoding;
import jd.parser.Regex;
import jd.parser.html.Form;
import jd.plugins.CryptedLink;
import jd.plugins.DecrypterException;
import jd.plugins.DecrypterPlugin;
import jd.plugins.DownloadLink;
import jd.plugins.FilePackage;
import jd.plugins.PluginForDecrypt;

@DecrypterPlugin(revision = "$Revision$", interfaceVersion = 2, names = { "vkontakte.ru" }, urls = { "http://(www\\.)?(vkontakte\\.ru|vk\\.com)/(audio(\\.php)?(\\?album_id=\\d+\\&id=|\\?id=)\\d+|(video(\\-)?\\d+_\\d+|videos\\d+|video\\?section=tagged\\&id=\\d+)|(photos|id)\\d+|albums\\-?\\d+|([A-Za-z0-9_\\-]+#/)?album\\d+_\\d+|photo(\\-)?\\d+_\\d+)" }, flags = { 0 })
public class VKontakteRu extends PluginForDecrypt {

    /* must be static so all plugins share same lock */
    private static final Object LOCK = new Object();

    public VKontakteRu(PluginWrapper wrapper) {
        super(wrapper);
    }

    private static final String  FILEOFFLINE  = "(id=\"msg_back_button\">Wr\\&#243;\\&#263;</button|B\\&#322;\\&#261;d dost\\&#281;pu)";
    private static final String  DOMAIN       = "vk.com";
    private static final Integer MAXCOOKIEUSE = 50;

    @Override
    public ArrayList<DownloadLink> decryptIt(CryptedLink param, ProgressController progress) throws Exception {
        ArrayList<DownloadLink> decryptedLinks = new ArrayList<DownloadLink>();
        br.setFollowRedirects(false);
        String parameter = param.toString().replace("vkontakte.ru/", "vk.com/");
        br.setCookiesExclusive(false);
        synchronized (LOCK) {
            /** Login process */
            if (!getUserLogin()) return null;
            /* this call can refesh the login */
            br.getPage(parameter);
            /** Retry if login failed */
            if (br.getRedirectLocation() != null && br.getRedirectLocation().contains("login.vk.com") || br.containsHTML("(method=\"post\" name=\"login\" id=\"login\"|name=\"login\" id=\"quick_login_form\")")) {
                logger.info("Retrying vk.com login...");
                this.getPluginConfig().setProperty("logincounter", "-1");
                this.getPluginConfig().save();
                br.clearCookies(DOMAIN);
                br.clearCookies("login.vk.com");
                if (!getUserLogin()) return null;
            }
            final HashMap<String, String> cookies = new HashMap<String, String>();
            final Cookies add = this.br.getCookies(DOMAIN);
            for (final Cookie c : add.getCookies()) {
                cookies.put(c.getKey(), c.getValue());
            }
            this.getPluginConfig().setProperty("cookies", cookies);
            this.getPluginConfig().save();
            /** Decryption process START */
            br.setFollowRedirects(false);
            if (parameter.matches(".*?vk\\.com/audio.*?")) {
                /** Audio playlists */
                decryptedLinks = decryptAudioAlbum(decryptedLinks, parameter);
            } else if (parameter.matches(".*?vk\\.com/video(\\-)?\\d+_\\d+")) {
                /** Single video */
                decryptedLinks = decryptSingleVideo(decryptedLinks, parameter);
            } else if (parameter.matches(".*?(album\\d+_|photos|id)\\d+")) {
                /**
                 * Photo albums Examples: http://vk.com/photos575934598
                 * http://vk.com/id28426816 http://vk.com/album87171972_0
                 */
                decryptedLinks = decryptPhotoAlbum(decryptedLinks, parameter, progress);
            } else if (parameter.matches(".*?vk\\.com/photo(\\-)?\\d+_\\d+")) {
                /**
                 * Single photo links, those are just passed to the
                 * hosterplugin! Example:http://vk.com/photo125005168_269986868
                 */
                decryptedLinks = decryptSinglePhoto(decryptedLinks, parameter);
            } else if (parameter.matches(".*?vk\\.com/albums(\\-)?\\d+")) {
                /**
                 * Photo Album lists/overviews Example:
                 * http://vk.com/albums46486585
                 */
                decryptedLinks = decryptPhotoAlbums(decryptedLinks, parameter, progress);
            } else {
                /**
                 * Video-Albums Example: http://vk.com/videos575934598 Example2:
                 * http://vk.com/video?section=tagged&id=46468795637
                 */
                decryptedLinks = decryptVideoAlbum(decryptedLinks, parameter, progress);
            }
            return decryptedLinks;
        }

    }

    private ArrayList<DownloadLink> decryptAudioAlbum(ArrayList<DownloadLink> decryptedLinks, String parameter) throws IOException {
        br.getPage(parameter);
        br.getHeaders().put("X-Requested-With", "XMLHttpRequest");
        br.postPage("http://vk.com/audio", "act=load_audios_silent&al=1&edit=0&gid=0&id=" + new Regex(parameter, "id=(\\d+)$").getMatch(0));
        String[][] audioLinks = br.getRegex("\\'(http://cs\\d+\\.vk\\.com/u\\d+/audio/[a-z0-9]+\\.mp3)\\',\\'\\d+\\',\\'\\d+:\\d+\\',\\'(.*?)\\',\\'(.*?)\\'").getMatches();
        if (audioLinks == null || audioLinks.length == 0) return null;
        for (String audioInfo[] : audioLinks) {
            String finallink = audioInfo[0];
            if (finallink == null) return null;
            DownloadLink dl = createDownloadlink("directhttp://" + finallink);
            // Set filename so we have nice filenames here ;)
            dl.setFinalFileName(Encoding.htmlDecode(audioInfo[1].trim()) + " - " + Encoding.htmlDecode(audioInfo[2].trim()) + ".mp3");
            dl.setAvailable(true);
            decryptedLinks.add(dl);
        }
        return decryptedLinks;
    }

    private ArrayList<DownloadLink> decryptSingleVideo(ArrayList<DownloadLink> decryptedLinks, String parameter) throws IOException {
        br.getPage(parameter);
        if (br.containsHTML("class=\"button_blue\"><button id=\"msg_back_button\">Wr\\&#243;\\&#263;</button>")) return decryptedLinks;
        DownloadLink finallink = findVideolink(parameter);
        if (finallink == null) {
            logger.warning("Decrypter broken for link: " + parameter + "\n");
            return null;
        }
        decryptedLinks.add(finallink);
        return decryptedLinks;
    }

    private ArrayList<DownloadLink> decryptPhotoAlbum(ArrayList<DownloadLink> decryptedLinks, String parameter, ProgressController progress) throws IOException {
        if (parameter.contains("#/album"))
            parameter = "http://vk.com/album" + new Regex(parameter, "#/album(\\d+_\\d+)").getMatch(0);
        else if (parameter.matches(".*?vk\\.com/(photos|id)\\d+")) parameter = parameter.replaceAll("vk\\.com/(photos|id)", "vk.com/album") + "_0";
        br.getPage(parameter);
        if (br.containsHTML(FILEOFFLINE) || br.containsHTML("(В альбоме нет фотографий|<title>DELETED</title>)")) {
            logger.info("Link offline: " + parameter);
            return decryptedLinks;
        }
        String numberOfEntrys = getNumberOfEntrys();
        if (numberOfEntrys == null) {
            logger.warning("Decrypter broken for link: " + parameter);
            return null;
        }
        /**
         * Find out how many times we have to reload images. Take the number of
         * pictures - 80 (because without any request we already got 80) and
         * divide it by 40 (every reload we get 40)
         */
        int maxLoops = (int) StrictMath.ceil((Double.parseDouble(numberOfEntrys) - 80) / 40);
        if (maxLoops < 0) maxLoops = 0;
        int offset = 80;
        br.getHeaders().put("X-Requested-With", "XMLHttpRequest");
        progress.setRange(maxLoops);
        for (int i = 0; i <= maxLoops; i++) {
            if (i > 0) {
                br.postPage(parameter, "al=1&offset=" + offset + "&part=1");
                offset += 40;
            }
            String correctedBR = br.toString().replace("\\", "");
            String[] photoIDs = new Regex(correctedBR, "class=\"photo_row\" id=\"photo_row(\\d+_\\d+)\"").getColumn(0);
            if (photoIDs == null || photoIDs.length == 0) {
                photoIDs = new Regex(correctedBR, "><a href=\"/photo(\\d+_\\d+)\"").getColumn(0);
                if (photoIDs == null || photoIDs.length == 0) {
                    photoIDs = new Regex(correctedBR, "showPhoto\\(\\'(\\d+_\\d+)\\'").getColumn(0);
                }
            }
            if (photoIDs == null || photoIDs.length == 0) {
                logger.warning("Decrypter broken for link: " + parameter + "\n");
                logger.warning("Decrypter couldn't find photoIDs!");
                return null;
            }
            String albumID = new Regex(parameter, "/album(.+)").getMatch(0);
            for (String photoID : photoIDs) {
                /** Pass those goodies over to the hosterplugin */
                DownloadLink dl = createDownloadlink("http://vkontaktedecrypted.ru/picturelink/" + photoID);
                dl.setAvailable(true);
                dl.setProperty("albumid", albumID);
                decryptedLinks.add(dl);
            }
            progress.increase(1);
        }
        FilePackage fp = FilePackage.getInstance();
        fp.setName(new Regex(parameter, "/album(.+)").getMatch(0));
        fp.addLinks(decryptedLinks);
        return decryptedLinks;
    }

    private ArrayList<DownloadLink> decryptSinglePhoto(ArrayList<DownloadLink> decryptedLinks, String parameter) throws IOException {
        String albumID = br.getRegex("class=\"active_link\">[\t\n\r ]+<a href=\"/(.*?)\"").getMatch(0);
        if (albumID == null) {
            logger.warning("Decrypter broken for link: " + parameter + "\n");
            return null;
        }
        DownloadLink dl = createDownloadlink("http://vkontaktedecrypted.ru/picturelink/" + new Regex(parameter, ".*?vk\\.com/photo" + "(.+)").getMatch(0));
        dl.setProperty("albumid", albumID);
        decryptedLinks.add(dl);
        return decryptedLinks;
    }

    private ArrayList<DownloadLink> decryptPhotoAlbums(ArrayList<DownloadLink> decryptedLinks, String parameter, ProgressController progress) throws IOException {
        br.getPage(parameter);
        final String[] regexes = { "class=\\\\\"ge_photos_album\\\\\" href=\\\\\"\\\\(/album(\\-)?\\d+_\\d+)\\\\\"", "<div class=\"fl_l info_wrap\">[\t\n\r ]+<div class=\"name\"><a href=\"(/album(\\-)?\\d+_\\d+)\\?" };
        for (String regex : regexes) {
            String[] photoAlbums = br.getRegex(regex).getColumn(0);
            if (photoAlbums == null || photoAlbums.length == 0) continue;
            for (String photoAlbum : photoAlbums) {
                decryptedLinks.add(createDownloadlink("http://vk.com" + photoAlbum));
            }
        }
        String numberOfEntrys = getNumberOfEntrys();
        if (numberOfEntrys == null && (decryptedLinks == null || decryptedLinks.size() == 0)) {
            logger.warning("Decrypter broken for link: " + parameter);
            return null;
        }
        if (numberOfEntrys != null) {
            /**
             * Find out how many times we have to reload images. Take the number
             * of albums - 40 (because without any request we already got 20)
             * and divide it by 20 (every reload we get 20)
             */
            int maxLoops = (int) StrictMath.ceil((Double.parseDouble(numberOfEntrys) - 40) / 20);
            if (maxLoops < 0) maxLoops = 0;
            int offset = 20;
            br.getHeaders().put("X-Requested-With", "XMLHttpRequest");
            progress.setRange(maxLoops);
            /** Photos are placed in different locations, find them all */
            // Dev note: similar process as above with the offset:
            // http://vk.com/albums3793387, al=1&offset=40&part=1
            for (int i = 0; i <= maxLoops; i++) {
                if (i > 0) {
                    offset += 20;
                    br.postPage(parameter, "al=1&offset=" + offset + "&part=1");
                }
                String[] photoAlbums = br.getRegex("class=\"ge_photos_album\" href=\"(/album(\\-)?\\d+_\\d+)\"").getColumn(0);
                if (photoAlbums == null || photoAlbums.length == 0) continue;
                for (String photoAlbum : photoAlbums) {
                    decryptedLinks.add(createDownloadlink("http://vk.com" + photoAlbum));
                }
            }
        }
        if (decryptedLinks == null || decryptedLinks.size() == 0) {
            logger.warning("Decrypter broken for link: " + parameter);
            return null;
        }
        return decryptedLinks;
    }

    private ArrayList<DownloadLink> decryptVideoAlbum(ArrayList<DownloadLink> decryptedLinks, String parameter, ProgressController progress) throws IOException {
        br.getPage(parameter);
        String[] allVideos = br.getRegex("<td class=\"video_thumb\"><a href=\"/video(\\d+_\\d+)\"").getColumn(0);
        if (allVideos == null || allVideos.length == 0) allVideos = br.getRegex("<div class=\"video_info_cont\">[\t\n\r ]+<a href=\"/video(\\d+_\\d+)\"").getColumn(0);
        if (allVideos == null || allVideos.length == 0) {
            logger.warning("Couldn't find any videos for link: " + parameter);
            return null;
        }
        progress.setRange(allVideos.length);
        int counter = 1;
        logger.info("Found " + allVideos.length + " videos, decrypting...");
        for (String singleVideo : allVideos) {
            logger.info("Decrypting video " + counter + " / " + allVideos.length);
            String completeVideolink = "http://vk.com/video" + singleVideo;
            br.getPage(completeVideolink);
            // Invalid link
            if (br.containsHTML(FILEOFFLINE)) {
                logger.info("Link offline: " + completeVideolink);
                continue;
            }
            DownloadLink finallink = findVideolink(completeVideolink);
            if (finallink == null) {
                logger.warning("Decrypter broken for link: " + parameter + "\n");
                logger.warning("stopped at: " + completeVideolink);
                return null;
            }
            decryptedLinks.add(finallink);
            progress.increase(1);
            counter++;
        }
        return decryptedLinks;
    }

    private String getNumberOfEntrys() {
        String numberOfEntrys = br.getRegex("\\| (\\d+) zdj&#281").getMatch(0);
        if (numberOfEntrys == null) {
            numberOfEntrys = br.getRegex("count: (\\d+),").getMatch(0);
            if (numberOfEntrys == null) {
                numberOfEntrys = br.getRegex("</a>(\\d+) zdj\\&#281;\\&#263;<span").getMatch(0);
            }
        }
        return numberOfEntrys;
    }

    private DownloadLink findVideolink(String parameter) throws IOException {
        String correctedBR = br.toString().replace("\\", "");
        // Find youtube.com link if it exists
        String embeddedVideo = new Regex(correctedBR, "youtube\\.com/embed/(.*?)\\?autoplay=").getMatch(0);
        if (embeddedVideo != null) { return createDownloadlink("http://www.youtube.com/watch?v=" + embeddedVideo); }
        // Find rutube.ru link if it exists
        embeddedVideo = new Regex(correctedBR, "video\\.rutube\\.ru/(.*?)\\'").getMatch(0);
        if (embeddedVideo != null) {
            br.getPage("http://rutube.ru/trackinfo/" + embeddedVideo + ".html");
            String finalID = br.getRegex("<track_id>(\\d+)</track_id>").getMatch(0);
            if (finalID == null) {
                logger.warning("Decrypter broken for link: " + parameter);
                return null;
            }
            return createDownloadlink("http://rutube.ru/tracks/" + finalID + ".html");
        }
        // Find vimeo.com link if it exists
        embeddedVideo = new Regex(correctedBR, "player\\.vimeo\\.com/video/(\\d+)").getMatch(0);
        if (embeddedVideo != null) { return createDownloadlink("http://vimeo.com/" + embeddedVideo); }
        // No external video found, try finding a hosted video
        String additionalStuff = "video/";
        String urlPart = new Regex(correctedBR, "\"thumb\":\"(http:.{10,100})/video").getMatch(0);
        if (urlPart == null) urlPart = new Regex(correctedBR, "\"host\":\"(.*?)\"").getMatch(0);
        String vtag = new Regex(correctedBR, "\"vtag\":\"(.*?)\"").getMatch(0);
        String videoID = new Regex(correctedBR, "\"vkid\":\"(.*?)\"").getMatch(0);
        if (videoID == null) videoID = new Regex(parameter, ".*?vk\\.com/video(\\-)?\\d+_(\\d+)").getMatch(1);
        if (videoID == null || urlPart == null || vtag == null) return null;
        /**
         * Find the highest possible quality, also every video is only available
         * in 1-2 formats so we HAVE to use the highest one, if we don't do that
         * we get wrong links
         */
        String quality = ".vk.flv";
        if (correctedBR.contains("\"hd\":1")) {
            quality = ".360.mov";
            videoID = "";
        } else if (correctedBR.contains("\"hd\":2")) {
            quality = ".480.mov";
            videoID = "";
        } else if (correctedBR.contains("\"hd\":3")) {
            quality = ".720.mov";
            videoID = "";
        } else if (correctedBR.contains("\"no_flv\":1")) {
            quality = ".240.mov";
            videoID = "";
        }
        if (correctedBR.contains("\"hd\":3") && correctedBR.contains("\"no_flv\":0")) {
            quality = ".720.mp4";
            videoID = "";
        } else if (correctedBR.contains("\"no_flv\":0")) {
            /** Last big change done here on 07.11.2011 */
            if (urlPart.contains("vkadre.ru")) {
                additionalStuff = "assets/videos/";
            } else {
                quality = ".flv";
                videoID = "";
            }
        }
        String videoName = new Regex(correctedBR, "\"md_title\":\"(.*?)\"").getMatch(0);
        if (videoName == null) {
            videoName = new Regex(correctedBR, "\\{\"title\":\"(.*?)\"").getMatch(0);
        }
        String completeLink = "directhttp://http://" + urlPart.replace("http://", "") + "/" + additionalStuff + vtag + videoID + quality;
        DownloadLink dl = createDownloadlink(completeLink);
        // Set filename so we have nice filenames here ;)
        if (videoName != null) {
            if (videoName.length() > 100) {
                videoName = videoName.substring(0, 100);
            }
            dl.setFinalFileName(Encoding.htmlDecode(videoName).replaceAll("(»|\")", "").trim() + quality.substring(quality.length() - 4, quality.length()));
        }
        return dl;
    }

    @SuppressWarnings("unchecked")
    private boolean getUserLogin() throws Exception {
        br.setFollowRedirects(true);
        String username = null;
        String password = null;
        synchronized (LOCK) {
            int loginCounter = this.getPluginConfig().getIntegerProperty("logincounter");
            // Only login every x th time to prevent getting banned | else just
            // set cookies (re-use them)
            if (loginCounter > MAXCOOKIEUSE || loginCounter == -1) {
                username = this.getPluginConfig().getStringProperty("user", null);
                password = this.getPluginConfig().getStringProperty("pass", null);
                for (int i = 0; i < 3; i++) {
                    if (username == null || password == null) {
                        username = UserIO.getInstance().requestInputDialog("Enter Loginname for " + DOMAIN + " :");
                        if (username == null) return false;
                        password = UserIO.getInstance().requestInputDialog("Enter password for " + DOMAIN + " :");
                        if (password == null) return false;
                    }
                    if (!loginSite(username, password)) {
                        break;
                    } else {
                        if (loginCounter > MAXCOOKIEUSE) {
                            loginCounter = 0;
                        } else {
                            loginCounter++;
                        }
                        this.getPluginConfig().setProperty("user", username);
                        this.getPluginConfig().setProperty("pass", password);
                        this.getPluginConfig().setProperty("logincounter", loginCounter);
                        final HashMap<String, String> cookies = new HashMap<String, String>();
                        final Cookies add = this.br.getCookies(DOMAIN);
                        for (final Cookie c : add.getCookies()) {
                            cookies.put(c.getKey(), c.getValue());
                        }
                        this.getPluginConfig().setProperty("cookies", cookies);
                        this.getPluginConfig().save();
                        return true;
                    }

                }
            } else {
                final Object ret = this.getPluginConfig().getProperty("cookies", null);
                if (ret != null && ret instanceof HashMap<?, ?>) {
                    final HashMap<String, String> cookies = (HashMap<String, String>) ret;
                    for (Map.Entry<String, String> entry : cookies.entrySet()) {
                        this.br.setCookie(DOMAIN, entry.getKey(), entry.getValue());
                    }
                    loginCounter++;
                    this.getPluginConfig().setProperty("logincounter", loginCounter);
                    this.getPluginConfig().save();
                    return true;
                }
            }
        }
        this.getPluginConfig().setProperty("user", Property.NULL);
        this.getPluginConfig().setProperty("pass", Property.NULL);
        this.getPluginConfig().setProperty("logincounter", "-1");
        this.getPluginConfig().setProperty("cookies", Property.NULL);
        this.getPluginConfig().save();
        throw new DecrypterException("Login or/and password for " + DOMAIN + " is wrong!");
    }

    private boolean loginSite(String username, String password) throws Exception {
        br.getPage("http://vk.com/login.php");
        String damnIPH = br.getRegex("name=\"ip_h\" value=\"(.*?)\"").getMatch(0);
        if (damnIPH == null) damnIPH = br.getRegex("\\{loginscheme: \\'https\\', ip_h: \\'(.*?)\\'\\}").getMatch(0);
        if (damnIPH == null) damnIPH = br.getRegex("loginscheme: 'https'.*?ip_h: '(.*?)'").getMatch(0);
        if (damnIPH == null) return false;
        br.postPage("https://login.vk.com/", "act=login&success_url=&fail_url=&try_to_login=1&to=&vk=&al_test=3&from_host=vkontakte.ru&ip_h=" + damnIPH + "&email=" + Encoding.urlEncode(username) + "&pass=" + Encoding.urlEncode(password) + "&expire=1");
        String hash = br.getRegex("type=\"hidden\" name=\"hash\" value=\"(.*?)\"").getMatch(0);
        // If this variable is null the login is probably wrong
        if (hash == null) return false;
        br.getPage("http://vk.com/login.php?act=slogin&fast=1&hash=" + hash + "&redirect=1&s=0");
        // Finish login
        Form lol = br.getFormbyProperty("name", "login");
        if (lol != null) {
            lol.put("email", Encoding.urlEncode(username));
            lol.put("pass", Encoding.urlEncode(password));
            lol.put("expire", "0");
            br.submitForm(lol);
        }
        return true;
    }

}
