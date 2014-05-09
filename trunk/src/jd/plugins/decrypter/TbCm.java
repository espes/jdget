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

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.URLEncoder;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map.Entry;
import java.util.Random;
import java.util.Scanner;
import java.util.WeakHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.regex.Pattern;

import jd.PluginWrapper;
import jd.config.SubConfiguration;
import jd.controlling.AccountController;
import jd.controlling.ProgressController;
import jd.gui.UserIO;
import jd.http.Browser;
import jd.http.Request;
import jd.nutils.encoding.Encoding;
import jd.nutils.io.JDIO;
import jd.parser.Regex;
import jd.parser.html.Form;
import jd.parser.html.Form.MethodType;
import jd.plugins.Account;
import jd.plugins.CryptedLink;
import jd.plugins.DecrypterException;
import jd.plugins.DecrypterPlugin;
import jd.plugins.DownloadLink;
import jd.plugins.FilePackage;
import jd.plugins.LinkStatus;
import jd.plugins.PluginException;
import jd.plugins.PluginForDecrypt;
import jd.plugins.PluginForHost;
import jd.utils.JDUtilities;
import jd.utils.locale.JDL;
import de.savemytube.flv.FLV;

@DecrypterPlugin(revision = "$Revision$", interfaceVersion = 2, names = { "youtube.com" }, urls = { "https?://([a-z]+\\.)?youtube\\.com/(embed/|.*?watch.*?v(%3D|=)|view_play_list\\?p=|playlist\\?(p|list)=|.*?g/c/|.*?grid/user/|v/|user/|course\\?list=)[A-Za-z0-9\\-_]+(.*?page=\\d+)?(.*?list=[A-Za-z0-9\\-_]+)?" }, flags = { 0 })
public class TbCm extends PluginForDecrypt {
    private static AtomicBoolean PLUGIN_CHECKED  = new AtomicBoolean(false);
    private static AtomicBoolean PLUGIN_DISABLED = new AtomicBoolean(false);

    public TbCm(PluginWrapper wrapper) {
        super(wrapper);
    };

    private void canHandle() {
        if (PLUGIN_CHECKED.get()) return;
        String installerSource = null;
        try {
            installerSource = JDIO.readFileToString(JDUtilities.getResourceFile("src.dat"));
            PLUGIN_DISABLED.set(installerSource.contains("\"PS\""));
        } catch (Throwable e) {
            e.printStackTrace();
        } finally {
            PLUGIN_CHECKED.set(true);
        }
    }

    public static enum DestinationFormat {
        AUDIO_MP3("Audio (MP3)", new String[] { ".mp3" }),
        VIDEO_FLV("Video (FLV)", new String[] { ".flv" }),
        VIDEO_MP4("Video (MP4)", new String[] { ".mp4" }),
        VIDEO_3D_MP4("3D-Video (MP4)", new String[] { ".mp4" }),
        VIDEO_WEBM("Video (Webm)", new String[] { ".webm" }),
        VIDEO_3D_WEBM("3D-Video (Webm)", new String[] { ".webm" }),
        VIDEO_3GP("Video (3GP)", new String[] { ".3gp" }),
        UNKNOWN("Unknown (unk)", new String[] { ".unk" }),
        VIDEOIPHONE("Video (IPhone)", new String[] { ".mp4" }),
        VIDEO_DASH_MP4("Video (MP4)", new String[] { ".mp4" }),
        AUDIO_DASH_AAC("Audio (AAC)", new String[] { ".aac" });

        private String   text;
        private String[] ext;

        DestinationFormat(final String text, final String[] ext) {
            this.text = text;
            this.ext = ext;
        }

        public String getExtFirst() {
            return this.ext[0];
        }

        public String getText() {
            return this.text;
        }

        @Override
        public String toString() {
            return this.text;
        }
    }

    public static enum ITAG {
        DASH_VIDEO_ORIGINAL_H264(138, "MP4_DASH_ORIGINAL"),
        DASH_VIDEO_1080P_H264(137, "MP4_DASH_1080"),
        DASH_VIDEO_720P_H264(136, "MP4_DASH_720"),
        DASH_VIDEO_480P_H264(135, "MP4_DASH_480"),
        DASH_VIDEO_360P_H264(134, "MP4_DASH_360"),
        DASH_VIDEO_240P_H264(133, "MP4_DASH_240"),
        DASH_VIDEO_144P_H264(160, "MP4_DASH_144"),

        DASH_AUDIO_48K_AAC(139, "AAC_48"),
        DASH_AUDIO_128K_AAC(140, "AAC_128"),
        DASH_AUDIO_128K_WEBM(171, null),
        DASH_AUDIO_256K_AAC(141, "AAC_256"),
        DASH_AUDIO_192K_WEBM(172, null),

        MP4_VIDEO_AUDIO_ORIGINAL(38, "MP4_ORIGINAL"),
        MP4_VIDEO_1080P_H264_AUDIO_AAC(37, "MP4_1080"),
        MP4_VIDEO_720P_H264_AUDIO_AAC(22, "MP4_720"),
        MP4_VIDEO_360P_H264_AUDIO_AAC(18, "MP4_360"),

        FLV_VIDEO_LOW_240P_H263_AUDIO_MP3(5, "FLV_240_LOW"),
        FLV_VIDEO_HIGH_240P_H263_AUDIO_MP3(6, "FLV_240_HIGH"),
        FLV_VIDEO_360P_H264_AUDIO_AAC(34, "FLV_360"),
        FLV_VIDEO_480P_H264_AUDIO_AAC(35, "FLV_480"),

        THREEGP_VIDEO_144P_H264_AUDIO_AAC(17, "THREEGP_144"),
        THREEGP_VIDEO_240P_H263_AUDIO_AAC(13, "THREEGP_240_LOW"),
        THREEGP_VIDEO_240P_H264_AUDIO_AAC(36, "THREEGP_240_HIGH"),

        MP4_VIDEO_240P_H264_AUDIO_AAC_3D(83, "MP4_3D_240"),
        MP4_VIDEO_360P_H264_AUDIO_AAC_3D(82, "MP4_3D_360"),
        MP4_VIDEO_520P_H264_AUDIO_AAC_3D(85, "MP4_3D_520"),
        MP4_VIDEO_720P_H264_AUDIO_AAC_3D(84, "MP4_3D_720"),

        WEBM_VIDEO_360P_VP8_AUDIO_128K_VORBIS_3D(100, "WEBM_3D_360_128"),
        WEBM_VIDEO_360P_VP8_AUDIO_192K_VORBIS_3D(101, "WEBM_3D_360_192"),
        WEBM_VIDEO_720P_VP8_AUDIO_192K_VORBIS_3D(102, "WEBM_3D_720"),

        WEBM_VIDEO_360P_VP8_AUDIO_VORBIS(43, "WEBM_360"),
        WEBM_VIDEO_480P_VP8_AUDIO_VORBIS(44, "WEBM_480"),
        WEBM_VIDEO_720P_VP8_AUDIO_VORBIS(45, "WEBM_720"),
        WEBM_VIDEO_1080P_VP8_AUDIO_VORBIS(46, "WEBM_1080");

        private final int    itag;
        private final String variantID;

        public String getVariantID() {
            return variantID;
        }

        private ITAG(int itag, String variantID) {
            this.itag = itag;
            this.variantID = variantID;
        }

        public int getITAG() {
            return itag;
        }

        public static ITAG get(int itag) {
            for (ITAG tag : values()) {
                if (tag.getITAG() == itag) return tag;
            }
            return null;
        }
    }

    static class Info {
        public String link;
        public long   size;
        public ITAG   itag;
        public String desc;
    }

    static class InfoList extends ArrayList<Info> {

        /**
         * 
         */
        private static final long serialVersionUID = -6005153187820351430L;

        public Info getInfoForITAG(ITAG itag) {
            for (Info info : this) {
                if (info.itag == itag) return info;
            }
            return null;
        }

        public Info getInfoForFirstITAGMatch(ITAG... itags) {
            for (ITAG itag : itags) {
                for (Info info : this) {
                    if (info.itag == itag) return info;
                }
            }
            return null;
        }
    }

    private final Pattern                        StreamingShareLink  = Pattern.compile("\\< streamingshare=\"youtube\\.com\" name=\"(.*?)\" dlurl=\"(.*?)\" brurl=\"(.*?)\" convertto=\"(.*?)\" comment=\"(.*?)\" \\>", Pattern.CASE_INSENSITIVE);
    private final Pattern                        YT_FILENAME_PATTERN = Pattern.compile("<meta name=\"title\" content=\"(.*?)\">", Pattern.CASE_INSENSITIVE);
    private final String                         UNSUPPORTEDRTMP     = "itag%2Crtmpe%2";

    private HashMap<DestinationFormat, InfoList> possibleconverts    = null;

    private static final String                  TEMP_EXT            = ".tmp$";
    private boolean                              pluginloaded        = false;
    private boolean                              verifyAge           = false;

    private HashMap<String, FilePackage>         filepackages        = new HashMap<String, FilePackage>();

    private final String                         NAME_SUBTITLES      = "subtitles";
    private final String                         NAME_THUMBNAILS     = "thumbnails";

    private final String                         CUSTOM_DATE         = "CUSTOM_DATE";
    private final String                         CUSTOM_FILENAME     = "CUSTOM_FILENAME";
    private final String                         VIDEONUMBERFORMAT   = "VIDEONUMBERFORMAT";

    public static boolean ConvertFile(final DownloadLink downloadlink, final DestinationFormat InType, final DestinationFormat OutType) {
        if (InType.equals(OutType)) {
            final File oldone = new File(downloadlink.getFileOutput());
            final File newone = new File(downloadlink.getFileOutput().replaceAll(TbCm.TEMP_EXT, OutType.getExtFirst()));
            downloadlink.setFinalFileName(downloadlink.getName().replaceAll(TbCm.TEMP_EXT, OutType.getExtFirst()));
            oldone.renameTo(newone);
            return true;
        }

        downloadlink.getLinkStatus().setStatusText(JDL.L("convert.progress.convertingto", "convert to") + " " + OutType.toString());

        switch (InType) {
        case VIDEO_FLV:
            // Inputformat FLV
            switch (OutType) {
            case AUDIO_MP3:
                System.out.println("Convert FLV to mp3...");
                new FLV(downloadlink.getFileOutput(), true, true);

                // FLV löschen
                new File(downloadlink.getFileOutput()).delete();
                // AVI löschen
                new File(downloadlink.getFileOutput().replaceAll(TbCm.TEMP_EXT, ".avi")).delete();

                return true;
            default:
                System.out.println("Don't know how to convert " + InType.getText() + " to " + OutType.getText());
                downloadlink.getLinkStatus().setErrorMessage(JDL.L("convert.progress.unknownintype", "Unknown format"));
                return false;
            }
        default:
            System.out.println("Don't know how to convert " + InType.getText() + " to " + OutType.getText());
            downloadlink.getLinkStatus().setErrorMessage(JDL.L("convert.progress.unknownintype", "Unknown format"));
            return false;
        }
    }

    /**
     * Converts the Google Closed Captions subtitles to SRT subtitles. It runs after the completed download.
     * 
     * @param downloadlink
     *            . The finished link to the Google CC subtitle file.
     * @return The success of the conversion.
     */
    public static boolean convertSubtitle(final DownloadLink downloadlink) {
        final File source = new File(downloadlink.getFileOutput());

        BufferedWriter dest = null;
        FileOutputStream fos = null;
        try {
            try {
                File srtFile = new File(source.getAbsolutePath().replace(".xml", ".srt"));
                dest = new BufferedWriter(new OutputStreamWriter(fos = new FileOutputStream(srtFile), "UTF-8"));
            } catch (IOException e1) {
                return false;
            }

            final StringBuilder xml = new StringBuilder();
            int counter = 1;
            final String lineseparator = System.getProperty("line.separator");

            FileInputStream fis = null;
            try {
                Scanner in = new Scanner(new InputStreamReader(fis = new FileInputStream(source), "UTF-8"));
                while (in.hasNext()) {
                    xml.append(in.nextLine() + lineseparator);
                }
            } catch (Exception e) {
                return false;
            } finally {
                try {
                    fis.close();
                } catch (final Throwable e) {
                }
            }

            String[][] matches = new Regex(xml.toString(), "<text start=\"(.*?)\".*?(dur=\"(.*?)\")?>(.*?)</text>").getMatches();

            try {
                String[] prevMatch = null;
                for (String[] match : matches) {
                    if (prevMatch != null) {
                        String[] lastMatch = prevMatch;
                        prevMatch = null;
                        dest.write(counter++ + lineseparator);
                        Double start = Double.valueOf(lastMatch[0]);
                        Double end = Double.valueOf(match[0]);
                        dest.write(convertSubtitleTime(start) + " --> " + convertSubtitleTime(end) + lineseparator);
                        String text = lastMatch[3].trim();
                        text = text.replaceAll(lineseparator, " ");
                        text = text.replaceAll("&amp;", "&");
                        text = text.replaceAll("&quot;", "\"");
                        text = text.replaceAll("&#39;", "'");
                        dest.write(text + lineseparator + lineseparator);
                    }
                    if (match[1] == null) {
                        /* no end timestamp */
                        prevMatch = match;
                        continue;
                    }
                    /* we have start/end timestamps */
                    dest.write(counter++ + lineseparator);
                    Double start = Double.valueOf(match[0]);
                    Double end = start + Double.valueOf(match[2]);
                    dest.write(convertSubtitleTime(start) + " --> " + convertSubtitleTime(end) + lineseparator);
                    String text = match[3].trim();
                    text = text.replaceAll(lineseparator, " ");
                    text = text.replaceAll("&amp;", "&");
                    text = text.replaceAll("&quot;", "\"");
                    text = text.replaceAll("&#39;", "'");
                    dest.write(text + lineseparator + lineseparator);
                }
            } catch (Exception e) {
                return false;
            }
        } finally {
            try {
                dest.close();
            } catch (Throwable e) {
            }
            try {
                fos.close();
            } catch (Throwable e) {
            }
        }
        source.delete();
        return true;
    }

    /**
     * Converts the the time of the Google format to the SRT format.
     * 
     * @param time
     *            . The time from the Google XML.
     * @return The converted time as String.
     */
    private static String convertSubtitleTime(Double time) {
        String hour = "00";
        String minute = "00";
        String second = "00";
        String millisecond = "0";

        Integer itime = Integer.valueOf(time.intValue());

        // Hour
        Integer timeHour = Integer.valueOf(itime.intValue() / 3600);
        if (timeHour < 10) {
            hour = "0" + timeHour.toString();
        } else {
            hour = timeHour.toString();
        }

        // Minute
        Integer timeMinute = Integer.valueOf((itime.intValue() % 3600) / 60);
        if (timeMinute < 10) {
            minute = "0" + timeMinute.toString();
        } else {
            minute = timeMinute.toString();
        }

        // Second
        Integer timeSecond = Integer.valueOf(itime.intValue() % 60);
        if (timeSecond < 10) {
            second = "0" + timeSecond.toString();
        } else {
            second = timeSecond.toString();
        }

        // Millisecond
        millisecond = String.valueOf(time - itime).split("\\.")[1];
        if (millisecond.length() == 1) millisecond = millisecond + "00";
        if (millisecond.length() == 2) millisecond = millisecond + "0";
        if (millisecond.length() > 2) millisecond = millisecond.substring(0, 3);

        // Result
        String result = hour + ":" + minute + ":" + second + "," + millisecond;

        return result;
    }

    private void gsProxy(boolean b) {
        SubConfiguration cfg = SubConfiguration.getConfig("youtube.com");
        if (cfg != null && cfg.getBooleanProperty("PROXY_ACTIVE")) {
            String PROXY_ADDRESS = cfg.getStringProperty("PROXY_ADDRESS");
            int PROXY_PORT = cfg.getIntegerProperty("PROXY_PORT");
            if (isEmpty(PROXY_ADDRESS) || PROXY_PORT < 0) return;
            PROXY_ADDRESS = new Regex(PROXY_ADDRESS, "^[0-9a-zA-Z]+://").matches() ? PROXY_ADDRESS : "http://" + PROXY_ADDRESS;
            org.appwork.utils.net.httpconnection.HTTPProxy proxy = org.appwork.utils.net.httpconnection.HTTPProxy.parseHTTPProxy(PROXY_ADDRESS + ":" + PROXY_PORT);
            if (b && proxy != null && proxy.getHost() != null) {
                br.setProxy(proxy);
                return;
            }
        }
        br.setProxySelector(br.getThreadProxy());
    }

    private boolean isEmpty(String ip) {
        return ip == null || ip.trim().length() == 0;
    }

    private void addtopos(final DestinationFormat mode, final String link, final long size, final String desc, final ITAG itag) {
        InfoList info = this.possibleconverts.get(mode);
        if (info == null) {
            info = new InfoList();
            this.possibleconverts.put(mode, info);
        }
        final Info tmp = new Info();
        tmp.link = link;
        tmp.size = size;
        tmp.desc = desc;
        tmp.itag = itag;
        info.add(tmp);
    }

    public boolean canHandle(final String data) {
        canHandle();
        if (PLUGIN_DISABLED.get() == true) return false;
        return super.canHandle(data);
    }

    /**
     * Contains a List (of any form 'play|course|user') within provided String
     */
    private boolean containsList(String x) {
        return new Regex(x, "(" + playListRegex + "|" + courseListRegex + "|" + userListRegex + ")").matches();
    }

    /**
     * Contains a Video (of any form '(/v/vuid|/watch?.*v=vuid|/embed/vuid)' within provided String
     */
    private boolean containsVideo(String x) {
        return new Regex(x, "((\\?|&)v=|/v/|/embed/)[a-z0-9\\-_]+").matches();
    }

    /**
     * Returns host from provided String.
     */
    private String getHost(String x) {
        return new Regex(x, "(https?://[^/]+)").getMatch(0);
    }

    /**
     * Returns a ListID from provided String.
     */
    private String getListID(String x) {
        String list = null;
        // these are /user/ also but we want the id/tag?
        list = new Regex(parameter, userGridGCRegex).getMatch(1);
        // /user/
        if (list == null) list = new Regex(parameter, "/user/([A-Za-z0-9\\-_]+)").getMatch(0);
        // play && course
        if (list == null) list = new Regex(x, "list=([A-Za-z0-9\\-_]+)").getMatch(0);
        return list;
    }

    private String getVideoID(String URL) {
        String vuid = new Regex(URL, "v=([A-Za-z0-9\\-_]+)").getMatch(0);
        if (vuid == null) {
            vuid = new Regex(URL, "(v|embed)/([A-Za-z0-9\\-_]+)").getMatch(1);
        }
        return vuid;
    }

    private String                      parameter       = null;
    private String                      host            = null;
    private String                      luid            = null;
    private String                      vuid            = null;
    private String                      next            = null;
    private final String                playListRegex   = "(\\?|&)(play)?list=[A-Za-z0-9\\-_]+";
    private final String                courseListRegex = "/course\\?list=[A-Za-z0-9\\-_]+";
    private final String                userListRegex   = "/user/[A-Za-z0-9\\-_]+";
    // child of /user/, within #tags
    private final String                userGridGCRegex = "(g/c/|grid/user/)([A-Za-z0-9\\-_]+)";
    private final LinkedHashSet<String> dupeList        = new LinkedHashSet<String>();

    public ArrayList<DownloadLink> decryptIt(final CryptedLink param, final ProgressController progress) throws Exception {
        final ArrayList<DownloadLink> decryptedLinks = new ArrayList<DownloadLink>();
        canHandle();
        this.possibleconverts = new HashMap<DestinationFormat, InfoList>();
        if (PLUGIN_DISABLED.get() == true) return decryptedLinks;
        long startTime = System.currentTimeMillis();
        br.setFollowRedirects(true);
        br.setCookiesExclusive(true);
        br.clearCookies("youtube.com");
        br.setCookie("http://youtube.com", "PREF", "hl=en-GB");

        // load hoster plugin
        if (pluginloaded == false) {
            final PluginForHost plugin = JDUtilities.getPluginForHost("youtube.com");
            if (plugin == null) throw new IllegalStateException("youtube plugin not found!");
            pluginloaded = true;
        }

        parameter = preferHTTPS(Encoding.urlDecode(param.toString(), false));
        host = getHost(parameter);
        vuid = getVideoID(parameter);

        // cleanup video link, if it's not a list
        if (!containsList(parameter)) {
            parameter = getHost(parameter) + "/watch?v=" + vuid;
        }

        // Some Stable errorhandling

        ArrayList<String[]> linkstodecrypt = new ArrayList<String[]>();

        boolean prem = false;
        if (isJDownloader2()) {
            final ArrayList<Account> accounts = AccountController.getInstance().getAllAccounts("youtube.com");
            if (accounts != null && accounts.size() != 0) {
                Iterator<Account> it = accounts.iterator();
                while (it.hasNext()) {
                    Account n = it.next();
                    if (n.isEnabled() && n.isValid()) {
                        prem = this.login(n);
                        break;
                    }
                }
            }
        }

        boolean multiple_videos = true;

        // Check if link contains a video and a playlist
        int choice = -1;
        if (containsList(parameter) && containsVideo(parameter)) {
            choice = getPluginConfig().getIntegerProperty("ISVIDEOANDPLAYLIST", 2);
            // choice == 0 -> Only video
            // choice == 1 -> Playlist
            // choice == 2 -> Ask

            if (choice == 2) {
                int ret = UserIO.getInstance().requestConfirmDialog(0, parameter, JDL.L("plugins.host.youtube.isvideoandplaylist.question.message", "The Youtube link contains a video and a playlist. What do you want do download?"), null, JDL.L("plugins.host.youtube.isvideoandplaylist.question.onlyvideo", "Only video"), JDL.L("plugins.host.youtube.isvideoandplaylist.question.playlist", "Complete playlist"));
                // Video selected
                if (ret == 2) choice = 0;
                // Playlist selected
                if (ret == 4) choice = 1;
            }

            if (choice == 0) {
                parameter = host + "/watch?v=" + vuid;
            }
        }

        int videoNumberCounter = 1;

        // List support
        if (new Regex(parameter, "(" + playListRegex + "|" + courseListRegex + "|" + userGridGCRegex + ")").matches() || choice == 1) {
            luid = getListID(parameter);
            String page = parameter;
            int segmentCounter = 1;
            do {
                try {
                    if (this.isAbort()) {
                        if (videoNumberCounter > 1) {
                            logger.info("Decryption aborted by user, found " + videoNumberCounter + " links, stopping...");
                            break;
                        } else {
                            logger.info("Decryption aborted by user, stopping...");
                            return decryptedLinks;
                        }
                    }
                } catch (final Throwable e) {
                }
                // first link, make it into playlist link
                if (next == null)
                    page = host + "/playlist?list=" + luid;
                // secondary page results will start with /, thats ok.
                else {
                    page = Encoding.htmlDecode(next);
                    // little sleep per page to prevent ddos
                    Thread.sleep(1000);
                }
                br.getPage(page);
                String[] videos = br.getRegex("href=\"(/watch\\?v=[A-Za-z0-9\\-_]+)\\&amp;list=[A-Z0-9]+").getColumn(0);
                // the (g/c/|grid/user/) doesn't return the same luid within url so will fail.
                if (videos == null || videos.length == 0 && new Regex(parameter, userGridGCRegex).matches()) videos = br.getRegex("href=\"(/watch\\?v=[A-Za-z0-9\\-_]+)\\&amp;list=[A-Z0-9]+").getColumn(0);
                for (String video : videos) {
                    video = Encoding.htmlDecode(video);
                    if (!dupeList.add(getVideoID(video))) continue;
                    video = host + video;
                    final String[] finalInformation = new String[2];
                    finalInformation[0] = video;
                    finalInformation[1] = Integer.toString(videoNumberCounter);
                    linkstodecrypt.add(finalInformation);
                    videoNumberCounter++;
                }
                // not all pages are shown on first page! Grab next and continue loop
                next = br.getRegex("<a href=\"(/playlist\\?list=" + luid + "\\&amp;page=\\d+)\"[^\r\n]+>Next").getMatch(0);
                logger.info("youtube.com/playlist: Found " + videoNumberCounter + " links");
                logger.info("youtube.com/playlist: Decrypted segment " + segmentCounter);
                segmentCounter++;
            } while (next != null);
        }
        // user support
        else if (parameter.contains("/user/")) {
            luid = getListID(parameter);
            br.getPage(host + "/user/" + luid + "/videos?view=0");
            if (br.containsHTML(">404 Not Found<")) {
                logger.info("The following link is offline: " + parameter);
                return decryptedLinks;
            }

            // NOTE: This will stop at 1050 links - probably because youtube doesn't provide more "load more videos" links hmm
            int segmentCounter = 1;
            do {
                try {
                    if (this.isAbort()) {
                        if (videoNumberCounter > 1) {
                            logger.info("Decryption aborted by user, found " + videoNumberCounter + " links, stopping...");
                            break;
                        } else {
                            logger.info("Decryption aborted by user, stopping...");
                            return decryptedLinks;
                        }
                    }
                } catch (final Throwable e) {
                }
                String content = unescape(br.toString());
                if (br.containsHTML("iframe style=\"display:block;border:0;\" src=\"/error")) {
                    logger.warning("An unkown error occured");
                    return null;
                }

                String[] links = new Regex(content, "a href=\"(/watch\\?v=[A-Za-z0-9\\-_]+)\" class=\"ux-thumb-wrap yt-uix-sessionlink yt-uix-contextlink").getColumn(0);

                for (String url : links) {
                    String video = Encoding.htmlDecode(url);
                    if (!dupeList.add(getVideoID(url))) continue;
                    video = host + video;
                    final String[] finalInformation = new String[2];
                    finalInformation[0] = video;
                    finalInformation[1] = Integer.toString(videoNumberCounter);
                    linkstodecrypt.add(finalInformation);
                    videoNumberCounter++;
                }

                next = new Regex(content, "data-uix-load-more-href=\"(/[^<>\"]*?)\"").getMatch(0);
                if (next != null) {
                    Thread.sleep(1000);
                    br.getPage(Encoding.htmlDecode(next));
                    segmentCounter++;
                }
                logger.info("youtube.com/user: Found " + videoNumberCounter + " links");
                logger.info("youtube.com/user: Decrypted segment " + segmentCounter);
            } while (next != null);
        } else {
            // Handle single video
            final String[] finalInformation = new String[2];
            finalInformation[0] = parameter;
            finalInformation[1] = "-1";
            linkstodecrypt.add(finalInformation);
            multiple_videos = false;
        }
        if (containsList(parameter)) logger.info("Found a total number of " + (videoNumberCounter - 1) + " videos for link: " + parameter);

        final SubConfiguration cfg = SubConfiguration.getConfig("youtube.com");
        boolean fast = cfg.getBooleanProperty("FAST_CHECK2", false);
        final boolean best = cfg.getBooleanProperty("ALLOW_BEST2", false);
        final AtomicBoolean mp3 = new AtomicBoolean(cfg.getBooleanProperty("ALLOW_MP3_V2", true));
        final AtomicBoolean flv = new AtomicBoolean(cfg.getBooleanProperty("ALLOW_FLV_V2", true));
        /* http://en.wikipedia.org/wiki/YouTube */
        final HashMap<ITAG, Object[]> allowedItags = new HashMap<ITAG, Object[]>() {
            private static final long serialVersionUID = -3028718522449785181L;

            {
                boolean threeD = cfg.getBooleanProperty("ALLOW_3D_V2", true);
                boolean mp4 = cfg.getBooleanProperty("ALLOW_MP4_V2", true);
                boolean webm = cfg.getBooleanProperty("ALLOW_WEBM_V2", true);
                boolean threegp = cfg.getBooleanProperty("ALLOW_3GP_V2", true);
                if (mp3.get() == false && mp4 == false && webm == false && flv.get() == false && threegp == false) {
                    /* if no container is selected, then everything is enabled */
                    mp3.set(true);
                    mp4 = true;
                    webm = true;
                    flv.set(true);
                    threegp = true;
                }

                /** User selected nothing -> Decrypt everything */
                boolean q144p = cfg.getBooleanProperty("ALLOW_144P_V2", true);
                boolean q240p = cfg.getBooleanProperty("ALLOW_240P_V2", true);
                boolean q360p = cfg.getBooleanProperty("ALLOW_360P_V2", true);
                boolean q480p = cfg.getBooleanProperty("ALLOW_480P_V2", true);
                boolean q520p = cfg.getBooleanProperty("ALLOW_520P_V2", true);
                boolean q720p = cfg.getBooleanProperty("ALLOW_720P_V2", true);
                boolean q1080p = cfg.getBooleanProperty("ALLOW_1080P_V2", true);
                boolean original = cfg.getBooleanProperty("ALLOW_ORIGINAL_V2", true);
                boolean dashSupported = isJDownloader2();
                if (!q144p && !q240p && !q360p && !q480p && !q520p && !q720p && !q1080p && !original && !threeD) {
                    q144p = true;
                    q240p = true;
                    q360p = true;
                    q480p = true;
                    q520p = true;
                    q720p = true;
                    q1080p = true;
                    original = true;
                    threeD = true;
                }

                // **** FLV *****
                if (mp3.get()) {
                    this.put(ITAG.FLV_VIDEO_LOW_240P_H263_AUDIO_MP3, new Object[] { DestinationFormat.AUDIO_MP3, "H.263", "MP3", "Stereo" });
                    this.put(ITAG.FLV_VIDEO_HIGH_240P_H263_AUDIO_MP3, new Object[] { DestinationFormat.AUDIO_MP3, "H.263", "MP3", "Mono" });
                }
                if (flv.get()) {
                    if (q240p) {
                        // video bit rate @ 0.25Mbit/second
                        this.put(ITAG.FLV_VIDEO_LOW_240P_H263_AUDIO_MP3, new Object[] { DestinationFormat.VIDEO_FLV, "H.263", "MP3", "Stereo", "240p" });
                    }
                    if (q240p) {
                        // video bit rate @ 0.8Mbit/second
                        this.put(ITAG.FLV_VIDEO_HIGH_240P_H263_AUDIO_MP3, new Object[] { DestinationFormat.VIDEO_FLV, "H.263", "MP3", "Stereo", "240p" });
                    }
                    if (q360p) {
                        this.put(ITAG.FLV_VIDEO_360P_H264_AUDIO_AAC, new Object[] { DestinationFormat.VIDEO_FLV, "H.264", "AAC", "Stereo", "360p" });
                    }
                    if (q480p) {
                        this.put(ITAG.FLV_VIDEO_480P_H264_AUDIO_AAC, new Object[] { DestinationFormat.VIDEO_FLV, "H.264", "AAC", "Stereo", "480p" });
                    }
                }

                // **** 3GP *****
                if (threegp) {
                    // according to wiki the video format is unknown. we rate this as mono! need to look into 3gp more to confirm.
                    if (q144p) {
                        this.put(ITAG.THREEGP_VIDEO_144P_H264_AUDIO_AAC, new Object[] { DestinationFormat.VIDEO_3GP, "H.264", "AAC", "Stereo", "144p" });
                    }
                    if (q240p) {
                        this.put(ITAG.THREEGP_VIDEO_240P_H263_AUDIO_AAC, new Object[] { DestinationFormat.VIDEO_3GP, "H.263", "AAC", "Mono", "240p" });
                    }
                    if (q240p) {
                        this.put(ITAG.THREEGP_VIDEO_240P_H264_AUDIO_AAC, new Object[] { DestinationFormat.VIDEO_3GP, "H.264", "AAC", "Stereo", "240p" });
                    }
                }

                // **** MP4 *****
                if (mp4) {
                    if (q144p) {
                        if (dashSupported) {
                            this.put(ITAG.DASH_VIDEO_144P_H264, new Object[] { DestinationFormat.VIDEO_DASH_MP4, "H.264", "AAC", "Stereo", "144p" });
                        }
                    }
                    if (q240p) {
                        if (dashSupported) this.put(ITAG.DASH_VIDEO_240P_H264, new Object[] { DestinationFormat.VIDEO_DASH_MP4, "H.264", "AAC", "Stereo", "240p" });
                    }
                    if (q360p) {
                        // 270p / 360p
                        if (dashSupported) this.put(ITAG.DASH_VIDEO_360P_H264, new Object[] { DestinationFormat.VIDEO_DASH_MP4, "H.264", "AAC", "Stereo", "360p" });
                        this.put(ITAG.MP4_VIDEO_360P_H264_AUDIO_AAC, new Object[] { DestinationFormat.VIDEO_MP4, "H.264", "AAC", "Stereo", "360p" });
                    }
                    if (q480p) {
                        if (dashSupported) this.put(ITAG.DASH_VIDEO_480P_H264, new Object[] { DestinationFormat.VIDEO_DASH_MP4, "H.264", "AAC", "Stereo", "480p" });
                    }
                    if (q720p) {
                        if (dashSupported) this.put(ITAG.DASH_VIDEO_720P_H264, new Object[] { DestinationFormat.VIDEO_DASH_MP4, "H.264", "AAC", "Stereo", "720p" });
                        this.put(ITAG.MP4_VIDEO_720P_H264_AUDIO_AAC, new Object[] { DestinationFormat.VIDEO_MP4, "H.264", "AAC", "Stereo", "720p" });
                    }
                    if (q1080p) {
                        if (dashSupported) this.put(ITAG.DASH_VIDEO_1080P_H264, new Object[] { DestinationFormat.VIDEO_DASH_MP4, "H.264", "AAC", "Stereo", "1080p" });
                        this.put(ITAG.MP4_VIDEO_1080P_H264_AUDIO_AAC, new Object[] { DestinationFormat.VIDEO_MP4, "H.264", "AAC", "Stereo", "1080p" });
                    }
                    // maybe this varies?? wiki says 3072p but I've seen less. eg :: 38 2048x1536 9 0 115,
                    if (original) {
                        if (dashSupported) this.put(ITAG.DASH_VIDEO_ORIGINAL_H264, new Object[] { DestinationFormat.VIDEO_DASH_MP4, "H.264", "AAC", "Stereo", "Original" });
                        this.put(ITAG.MP4_VIDEO_AUDIO_ORIGINAL, new Object[] { DestinationFormat.VIDEO_MP4, "H.264", "AAC", "Stereo", "Original" });
                    }
                }

                // **** WebM *****
                if (webm) {
                    if (q360p) {
                        this.put(ITAG.WEBM_VIDEO_360P_VP8_AUDIO_VORBIS, new Object[] { DestinationFormat.VIDEO_WEBM, "VP8", "Vorbis", "Stereo", "360p" });
                    }
                    if (q480p) {
                        this.put(ITAG.WEBM_VIDEO_480P_VP8_AUDIO_VORBIS, new Object[] { DestinationFormat.VIDEO_WEBM, "VP8", "Vorbis", "Stereo", "480p" });
                    }
                    if (q720p) {
                        this.put(ITAG.WEBM_VIDEO_720P_VP8_AUDIO_VORBIS, new Object[] { DestinationFormat.VIDEO_WEBM, "VP8", "Vorbis", "Stereo", "720p" });
                    }
                    if (q1080p) {
                        this.put(ITAG.WEBM_VIDEO_1080P_VP8_AUDIO_VORBIS, new Object[] { DestinationFormat.VIDEO_WEBM, "VP8", "Vorbis", "Stereo", "1080p" });
                    }
                }

                // **** 3D *****/
                if (threeD) {
                    if (webm) {
                        if (q360p) {
                            this.put(ITAG.WEBM_VIDEO_360P_VP8_AUDIO_128K_VORBIS_3D, new Object[] { DestinationFormat.VIDEO_3D_WEBM, "VP8", "Vorbis", "Stereo", "360p" });
                        }
                        if (q360p) {
                            this.put(ITAG.WEBM_VIDEO_360P_VP8_AUDIO_192K_VORBIS_3D, new Object[] { DestinationFormat.VIDEO_3D_WEBM, "VP8", "Vorbis", "Stereo", "360p" });
                        }
                        if (q720p) {
                            this.put(ITAG.WEBM_VIDEO_720P_VP8_AUDIO_192K_VORBIS_3D, new Object[] { DestinationFormat.VIDEO_3D_WEBM, "VP8", "Vorbis", "Stereo", "720p" });
                        }
                    }
                    if (mp4) {
                        if (q240p) {
                            this.put(ITAG.MP4_VIDEO_240P_H264_AUDIO_AAC_3D, new Object[] { DestinationFormat.VIDEO_3D_MP4, "H.264", "AAC", "Stereo", "240p" });
                        }
                        if (q360p) {
                            this.put(ITAG.MP4_VIDEO_360P_H264_AUDIO_AAC_3D, new Object[] { DestinationFormat.VIDEO_3D_MP4, "H.264", "AAC", "Stereo", "360p" });
                        }
                        if (q520p) {
                            this.put(ITAG.MP4_VIDEO_520P_H264_AUDIO_AAC_3D, new Object[] { DestinationFormat.VIDEO_3D_MP4, "H.264", "AAC", "Stereo", "520p" });
                        }
                        if (q720p) {
                            this.put(ITAG.MP4_VIDEO_720P_H264_AUDIO_AAC_3D, new Object[] { DestinationFormat.VIDEO_3D_MP4, "H.264", "AAC", "Stereo", "720p" });
                        }
                    }
                }

                if (dashSupported) {
                    // 48k mp4
                    this.put(ITAG.DASH_AUDIO_48K_AAC, new Object[] { DestinationFormat.AUDIO_DASH_AAC, "AAC_48k", "AAC", "Stereo", "low(48k)" });
                    // 128k mp4
                    this.put(ITAG.DASH_AUDIO_128K_AAC, new Object[] { DestinationFormat.AUDIO_DASH_AAC, "AAC_128k", "AAC", "Stereo", "middle(128k)" });
                    // 256k mp4
                    this.put(ITAG.DASH_AUDIO_256K_AAC, new Object[] { DestinationFormat.AUDIO_DASH_AAC, "AAC_256k", "AAC", "Stereo", "high(256k)" });
                    // 128k webm
                    // this.put(ITAG.DASH_AUDIO_128K_WEBM, new Object[] { DestinationFormat.AUDIO_DASH, "WEBM_128k", "WEBM", "Stereo",
                    // "middle(128k)" });
                    // 192k webm
                    // this.put(ITAG.DASH_AUDIO_192K_WEBM, new Object[] { DestinationFormat.AUDIO_DASH, "WEBM_256k", "WEBM", "Stereo",
                    // "high(256k)" });

                }
            }
        };

        // Force fast linkcheck if there are more then 20 videos in queue.
        if (linkstodecrypt.size() > 20) fast = true;

        for (String urlInformation[] : linkstodecrypt) {
            // Make an little sleep to prevent DDoS
            Thread.sleep(25);
            final String currentVideoUrl = urlInformation[0];
            final String currentPlaylistVideoNumber = urlInformation[1];

            try {
                this.possibleconverts.clear();

                if (this.StreamingShareLink.matcher(currentVideoUrl).matches()) {
                    // StreamingShareLink

                    final String[] info = new Regex(currentVideoUrl, this.StreamingShareLink).getMatches()[0];

                    for (final String debug : info) {
                        logger.info(debug);
                    }
                    // not sure about streaming links and '"youtubeJD" it never had it before... -raz
                    final DownloadLink thislink = this.createDownloadlink(info[1]);
                    thislink.setProperty("ALLOW_DUPE", true);
                    thislink.setBrowserUrl(info[2]);
                    thislink.setFinalFileName(info[0]);
                    thislink.setProperty("convertto", info[3]);

                    decryptedLinks.add(thislink);
                    // return decryptedLinks;
                    continue;
                }
                verifyAge = false;
                HashMap<Integer, String[]> foundLinks = this.getLinks(currentVideoUrl, prem, this.br, 0);
                String error = br.getRegex("<div id=\"unavailable\\-message\" class=\"\">[\t\n\r ]+<span class=\"yt\\-alert\\-vertical\\-trick\"></span>[\t\n\r ]+<div class=\"yt\\-alert\\-message\">([^<>\"]*?)</div>").getMatch(0);
                // Removed due wrong offline detection
                // if (error == null) error = br.getRegex("<div class=\"yt\\-alert\\-message\">(.*?)</div>").getMatch(0);
                if (error == null) error = br.getRegex("reason=([^<>\"/]*?)(\\&|$)").getMatch(0);
                if (br.containsHTML(UNSUPPORTEDRTMP)) error = "RTMP video download isn't supported yet!";
                if ((foundLinks == null || foundLinks.isEmpty()) && error != null) {
                    error = Encoding.urlDecode(error, false);
                    logger.info("Video unavailable: " + currentVideoUrl);
                    if (error != null) logger.info("Reason: " + error.trim());
                    continue;
                }
                final String videoid = getVideoID(currentVideoUrl);
                if (foundLinks == null || foundLinks.isEmpty()) {
                    if (linkstodecrypt.size() == 1) {
                        if (verifyAge || this.br.getURL().toLowerCase().indexOf("youtube.com/get_video_info?") != -1 && !prem) { throw new DecrypterException(DecrypterException.ACCOUNT); }
                        final DownloadLink offline = getOfflineLink();
                        offline.setName(videoid);
                        decryptedLinks.add(offline);
                        logger.info("Video unavailable: " + currentVideoUrl);
                        continue;
                    } else {
                        continue;
                    }
                }

                /** FILENAME PART1 START */
                String YT_FILENAME = "";
                if (foundLinks.containsKey(-1)) {
                    YT_FILENAME = foundLinks.get(-1)[0];
                    foundLinks.remove(-1);
                }
                HashMap<ITAG, String[]> availableItags = new HashMap<TbCm.ITAG, String[]>();
                Iterator<Entry<Integer, String[]>> it = foundLinks.entrySet().iterator();
                while (it.hasNext()) {
                    Entry<Integer, String[]> nextEntry = it.next();
                    ITAG tag = ITAG.get(nextEntry.getKey());
                    if (tag != null) availableItags.put(tag, nextEntry.getValue());
                }
                // replacing default Locate to be compatible with page language
                Locale locale = Locale.ENGLISH;
                SimpleDateFormat formatter = new SimpleDateFormat("dd.MM.yyyy", locale);
                String date = br.getRegex("id=\"eow-date\" class=\"watch-video-date\" >(\\d{2}\\.\\d{2}\\.\\d{4})</span>").getMatch(0);
                if (date == null) {
                    formatter = new SimpleDateFormat("dd MMM yyyy", locale);
                    date = br.getRegex("class=\"watch-video-date\" >([ ]+)?(\\d{1,2} [A-Za-z]{3} \\d{4})</span>").getMatch(1);
                }
                final String channelName = br.getRegex("feature=watch\"[^>]+dir=\"ltr[^>]+>(.*?)</a>(\\s+)?<span class=\"yt-user").getMatch(0);
                // userName != channelName
                final String userName = br.getRegex("temprop=\"url\" href=\"http://(www\\.)?youtube\\.com/user/([^<>\"]*?)\"").getMatch(1);
                final int playlistNumberInt = Integer.parseInt(currentPlaylistVideoNumber);
                String formattedFilename = cfg.getStringProperty(CUSTOM_FILENAME, jd.plugins.hoster.Youtube.defaultCustomFilename);
                if ((!formattedFilename.contains("*videoname*") && !formattedFilename.contains("*videoid*")) || !formattedFilename.contains("*ext*")) formattedFilename = jd.plugins.hoster.Youtube.defaultCustomFilename;
                if (formattedFilename == null || formattedFilename.equals("")) formattedFilename = "*videoname**quality**ext*";
                String partnumberformat = cfg.getStringProperty(VIDEONUMBERFORMAT);
                if (partnumberformat == null || partnumberformat.equals("")) partnumberformat = "0000";

                final DecimalFormat df = new DecimalFormat(partnumberformat);

                String formattedDate = null;
                if (date != null) {
                    final String userDefinedDateFormat = cfg.getStringProperty(CUSTOM_DATE, "dd.MM.yyyy_HH-mm-ss");
                    Date dateStr = formatter.parse(date);
                    formattedDate = formatter.format(dateStr);
                    Date theDate = formatter.parse(formattedDate);
                    formatter = new SimpleDateFormat(userDefinedDateFormat);
                    formattedDate = formatter.format(theDate);
                }

                formattedFilename = formattedFilename.replace("*videonumber*", df.format(playlistNumberInt));
                if (channelName != null) {
                    formattedFilename = Encoding.htmlDecode(formattedFilename.replace("*channelname*", channelName));
                } else {
                    formattedFilename = formattedFilename.replace("*channelname*", "");
                }
                if (userName != null) {
                    formattedFilename = Encoding.htmlDecode(formattedFilename.replace("*username*", userName));
                } else {
                    formattedFilename = formattedFilename.replace("*username*", "");
                }
                if (formattedFilename.contains("*videoid*")) {
                    formattedFilename = formattedFilename.replace("*videoid*", videoid);
                } else {
                    formattedFilename = formattedFilename.replace("*videoid*", "");
                }
                if (formattedDate != null) {
                    formattedFilename = formattedFilename.replace("*date*", formattedDate);
                } else {
                    formattedFilename = formattedFilename.replace("*date*", "");
                }
                /** FILENAME PART1 END */

                // ytid are case sensitive, you can not effectively dupe check 100% reliability with lower case only.
                String ytID = getVideoID(currentVideoUrl);

                /*
                 * We match against users resolution and file encoding type. This allows us to use their upper and lower limits. It will
                 * return multiple results if they are in the same quality rating
                 */
                HashMap<ITAG, String[]> useTags = new HashMap<ITAG, String[]>(availableItags);
                if (best) {
                    final HashMap<ITAG, String[]> bestFound = new HashMap<ITAG, String[]>();
                    List<ITAG[]> bestQualityTags = new ArrayList<ITAG[]>();
                    bestQualityTags.add(new ITAG[] { ITAG.MP4_VIDEO_AUDIO_ORIGINAL, ITAG.DASH_VIDEO_ORIGINAL_H264 });
                    bestQualityTags.add(new ITAG[] { ITAG.MP4_VIDEO_1080P_H264_AUDIO_AAC, ITAG.DASH_VIDEO_1080P_H264, ITAG.WEBM_VIDEO_1080P_VP8_AUDIO_VORBIS });
                    bestQualityTags.add(new ITAG[] { ITAG.MP4_VIDEO_720P_H264_AUDIO_AAC, ITAG.MP4_VIDEO_720P_H264_AUDIO_AAC_3D, ITAG.WEBM_VIDEO_720P_VP8_AUDIO_VORBIS, ITAG.WEBM_VIDEO_720P_VP8_AUDIO_192K_VORBIS_3D, ITAG.DASH_VIDEO_720P_H264 });
                    bestQualityTags.add(new ITAG[] { ITAG.MP4_VIDEO_520P_H264_AUDIO_AAC_3D });
                    bestQualityTags.add(new ITAG[] { ITAG.FLV_VIDEO_480P_H264_AUDIO_AAC, ITAG.WEBM_VIDEO_480P_VP8_AUDIO_VORBIS, ITAG.DASH_VIDEO_480P_H264 });
                    bestQualityTags.add(new ITAG[] { ITAG.MP4_VIDEO_360P_H264_AUDIO_AAC, ITAG.FLV_VIDEO_360P_H264_AUDIO_AAC, ITAG.WEBM_VIDEO_360P_VP8_AUDIO_VORBIS, ITAG.MP4_VIDEO_360P_H264_AUDIO_AAC_3D, ITAG.WEBM_VIDEO_360P_VP8_AUDIO_128K_VORBIS_3D, ITAG.WEBM_VIDEO_360P_VP8_AUDIO_192K_VORBIS_3D });
                    bestQualityTags.add(new ITAG[] { ITAG.FLV_VIDEO_LOW_240P_H263_AUDIO_MP3, ITAG.FLV_VIDEO_HIGH_240P_H263_AUDIO_MP3, ITAG.THREEGP_VIDEO_240P_H263_AUDIO_AAC, ITAG.THREEGP_VIDEO_240P_H264_AUDIO_AAC, ITAG.MP4_VIDEO_240P_H264_AUDIO_AAC_3D });
                    bestQualityTags.add(new ITAG[] { ITAG.THREEGP_VIDEO_144P_H264_AUDIO_AAC, ITAG.DASH_VIDEO_144P_H264 });
                    for (ITAG[] tags : bestQualityTags) {
                        for (ITAG tag : tags) {
                            if (availableItags.containsKey(tag) && allowedItags.containsKey(tag) && bestFound.isEmpty()) bestFound.put(tag, availableItags.get(tag));
                        }
                    }
                    if (bestFound.isEmpty()) {
                        logger.warning("You do not have Resolution or Format selected within range! : " + currentVideoUrl);
                        break;
                    } else {
                        if (bestFound.containsKey(ITAG.MP4_VIDEO_AUDIO_ORIGINAL)) bestFound.remove(ITAG.DASH_VIDEO_ORIGINAL_H264);
                        if (bestFound.containsKey(ITAG.MP4_VIDEO_1080P_H264_AUDIO_AAC)) bestFound.remove(ITAG.DASH_VIDEO_1080P_H264);
                        if (bestFound.containsKey(ITAG.MP4_VIDEO_720P_H264_AUDIO_AAC)) bestFound.remove(ITAG.DASH_VIDEO_720P_H264);
                        if (bestFound.containsKey(ITAG.FLV_VIDEO_480P_H264_AUDIO_AAC)) bestFound.remove(ITAG.DASH_VIDEO_480P_H264);
                        if (bestFound.containsKey(ITAG.MP4_VIDEO_360P_H264_AUDIO_AAC)) bestFound.remove(ITAG.DASH_VIDEO_360P_H264);
                        if (bestFound.containsKey(ITAG.DASH_VIDEO_ORIGINAL_H264) || bestFound.containsKey(ITAG.DASH_VIDEO_1080P_H264) || bestFound.containsKey(ITAG.DASH_VIDEO_720P_H264) || bestFound.containsKey(ITAG.DASH_VIDEO_480P_H264) || bestFound.containsKey(ITAG.DASH_VIDEO_360P_H264)) {
                            ITAG[] dashAudioTags = new ITAG[] { ITAG.DASH_AUDIO_256K_AAC, ITAG.DASH_AUDIO_128K_AAC, ITAG.DASH_AUDIO_48K_AAC, ITAG.DASH_AUDIO_192K_WEBM, ITAG.DASH_AUDIO_128K_WEBM };
                            for (ITAG dashAudio : dashAudioTags) {
                                if (availableItags.containsKey(dashAudio) && allowedItags.containsKey(dashAudio)) {
                                    /* at the moment we only use the first best audio match */
                                    bestFound.put(dashAudio, availableItags.get(dashAudio));
                                    break;
                                }
                            }
                        }
                        useTags = bestFound;
                    }
                } else {
                    Iterator<Entry<ITAG, String[]>> it2 = useTags.entrySet().iterator();
                    while (it2.hasNext()) {
                        Entry<ITAG, String[]> next2 = it2.next();
                        if (!allowedItags.containsKey(next2.getKey())) it2.remove();
                    }
                }
                /* filter out dash/xy duplicates */
                if (useTags.containsKey(ITAG.MP4_VIDEO_AUDIO_ORIGINAL)) useTags.remove(ITAG.DASH_VIDEO_ORIGINAL_H264);
                if (useTags.containsKey(ITAG.MP4_VIDEO_1080P_H264_AUDIO_AAC)) useTags.remove(ITAG.DASH_VIDEO_1080P_H264);
                if (useTags.containsKey(ITAG.MP4_VIDEO_720P_H264_AUDIO_AAC)) useTags.remove(ITAG.DASH_VIDEO_720P_H264);
                if (useTags.containsKey(ITAG.FLV_VIDEO_480P_H264_AUDIO_AAC)) useTags.remove(ITAG.DASH_VIDEO_480P_H264);
                if (useTags.containsKey(ITAG.MP4_VIDEO_360P_H264_AUDIO_AAC)) useTags.remove(ITAG.DASH_VIDEO_360P_H264);
                final boolean allowVariants = isJDownloader2() && cfg.getBooleanProperty("ENABLE_VARIANTS", false);
                String TTSURL = br.getRegex("\"ttsurl\": \"(http.*?)\"").getMatch(0);
                for (final Entry<ITAG, String[]> entry : useTags.entrySet()) {
                    DestinationFormat cMode = (DestinationFormat) allowedItags.get(entry.getKey())[0];
                    String vQuality = "(" + entry.getValue()[1] + "_" + allowedItags.get(entry.getKey())[1] + "-" + allowedItags.get(entry.getKey())[2] + ")";
                    String dlLink = entry.getValue()[0];
                    ITAG itag = entry.getKey();
                    int format = itag.getITAG();
                    // Skip MP3 but handle 240p flv
                    if (!(format == 5 && mp3.get() && !flv.get())) {
                        try {
                            if (fast) {
                                this.addtopos(cMode, dlLink, -1, vQuality, itag);
                            } else if (this.br.openGetConnection(dlLink).getResponseCode() == 200) {
                                Thread.sleep(100);
                                this.addtopos(cMode, dlLink, this.br.getHttpConnection().getLongContentLength(), vQuality, itag);
                            }
                        } catch (final Throwable e) {
                            e.printStackTrace();
                        } finally {
                            try {
                                this.br.getHttpConnection().disconnect();
                            } catch (final Throwable e) {
                            }
                        }
                    } else
                    // Handle MP3
                    if ((format == 0 || format == 5 || format == 6) && mp3.get()) {
                        try {
                            if (fast) {
                                this.addtopos(DestinationFormat.AUDIO_MP3, dlLink, -1, "", itag);
                            } else if (this.br.openGetConnection(dlLink).getResponseCode() == 200) {
                                Thread.sleep(100);
                                this.addtopos(DestinationFormat.AUDIO_MP3, dlLink, this.br.getHttpConnection().getLongContentLength(), "", itag);
                            }
                        } catch (final Throwable e) {
                            e.printStackTrace();
                        } finally {
                            try {
                                this.br.getHttpConnection().disconnect();
                            } catch (final Throwable e) {
                            }
                        }
                    }
                }

                Info bestDashAudio = null;
                if (possibleconverts.get(DestinationFormat.AUDIO_DASH_AAC) != null) {
                    for (Info audio : possibleconverts.get(DestinationFormat.AUDIO_DASH_AAC)) {
                        if (audio.itag.getITAG() > ITAG.DASH_AUDIO_256K_AAC.getITAG()) {
                            /* ignore webm audio at the moment */
                            continue;
                        }
                        if (bestDashAudio == null) {
                            bestDashAudio = audio;
                        } else if (audio.itag.getITAG() > bestDashAudio.itag.getITAG()) {
                            bestDashAudio = audio;
                        }
                    }
                }
                boolean formatInName = cfg.getBooleanProperty("FORMATINNAME", true);
                boolean allowAAC = cfg.getBooleanProperty("ALLOW_AAC", false);
                for (final Entry<DestinationFormat, InfoList> next : this.possibleconverts.entrySet()) {
                    final DestinationFormat convertTo = next.getKey();
                    if (DestinationFormat.AUDIO_DASH_AAC.equals(convertTo) && allowAAC == false) {
                        /* at the moment we do not handle audio only */
                        continue;
                    }
                    if (DestinationFormat.VIDEO_DASH_MP4.equals(convertTo) && bestDashAudio == null) {
                        /* at the moment we do not handle video only */
                        continue;
                    }
                    // create a package, for each quality.
                    FilePackage filePackage = filepackages.get(convertTo.getText());
                    if (filePackage == null) {
                        filePackage = FilePackage.getInstance();
                        filePackage.setProperty("ALLOW_MERGE", true);
                        if (multiple_videos || cfg.getBooleanProperty("GROUP_FORMAT", false))
                            filePackage.setName("YouTube " + convertTo.getText());
                        else
                            // package name should not reference convertTo when user doesn't want to group by type!
                            filePackage.setName(YT_FILENAME);
                        filepackages.put(convertTo.getText(), filePackage);
                    }
                    if (allowVariants) {
                        List<String> variants = new ArrayList<String>();
                        Info bestHit = null;
                        ITAG[] itags = null;
                        /* first find bestInfo for convertTo Format */
                        switch (convertTo) {
                        case VIDEO_WEBM:
                            itags = new ITAG[] { ITAG.WEBM_VIDEO_1080P_VP8_AUDIO_VORBIS, ITAG.WEBM_VIDEO_720P_VP8_AUDIO_VORBIS, ITAG.WEBM_VIDEO_480P_VP8_AUDIO_VORBIS, ITAG.WEBM_VIDEO_360P_VP8_AUDIO_VORBIS };
                            bestHit = next.getValue().getInfoForFirstITAGMatch(itags);
                            break;
                        case VIDEO_MP4:
                            itags = new ITAG[] { ITAG.MP4_VIDEO_AUDIO_ORIGINAL, ITAG.MP4_VIDEO_1080P_H264_AUDIO_AAC, ITAG.MP4_VIDEO_720P_H264_AUDIO_AAC, ITAG.MP4_VIDEO_360P_H264_AUDIO_AAC };
                            bestHit = next.getValue().getInfoForFirstITAGMatch(itags);
                            break;
                        case VIDEO_DASH_MP4:
                            itags = new ITAG[] { ITAG.DASH_VIDEO_ORIGINAL_H264, ITAG.DASH_VIDEO_1080P_H264, ITAG.DASH_VIDEO_720P_H264, ITAG.DASH_VIDEO_480P_H264, ITAG.DASH_VIDEO_360P_H264, ITAG.DASH_VIDEO_240P_H264, ITAG.DASH_VIDEO_144P_H264 };
                            bestHit = next.getValue().getInfoForFirstITAGMatch(itags);
                            break;
                        case VIDEO_3GP:
                            itags = new ITAG[] { ITAG.THREEGP_VIDEO_240P_H264_AUDIO_AAC, ITAG.THREEGP_VIDEO_240P_H263_AUDIO_AAC, ITAG.THREEGP_VIDEO_144P_H264_AUDIO_AAC };
                            bestHit = next.getValue().getInfoForFirstITAGMatch(itags);
                            break;
                        case VIDEO_FLV:
                            itags = new ITAG[] { ITAG.FLV_VIDEO_480P_H264_AUDIO_AAC, ITAG.FLV_VIDEO_360P_H264_AUDIO_AAC, ITAG.FLV_VIDEO_HIGH_240P_H263_AUDIO_MP3, ITAG.FLV_VIDEO_LOW_240P_H263_AUDIO_MP3 };
                            bestHit = next.getValue().getInfoForFirstITAGMatch(itags);
                            break;
                        case VIDEO_3D_MP4:
                            itags = new ITAG[] { ITAG.MP4_VIDEO_720P_H264_AUDIO_AAC_3D, ITAG.MP4_VIDEO_520P_H264_AUDIO_AAC_3D, ITAG.MP4_VIDEO_360P_H264_AUDIO_AAC_3D, ITAG.MP4_VIDEO_240P_H264_AUDIO_AAC_3D };
                            bestHit = next.getValue().getInfoForFirstITAGMatch(itags);
                            break;
                        case VIDEO_3D_WEBM:
                            itags = new ITAG[] { ITAG.WEBM_VIDEO_720P_VP8_AUDIO_192K_VORBIS_3D, ITAG.WEBM_VIDEO_360P_VP8_AUDIO_192K_VORBIS_3D, ITAG.WEBM_VIDEO_360P_VP8_AUDIO_128K_VORBIS_3D };
                            bestHit = next.getValue().getInfoForFirstITAGMatch(itags);
                            break;
                        case AUDIO_DASH_AAC:
                            itags = new ITAG[] { ITAG.DASH_AUDIO_256K_AAC, ITAG.DASH_AUDIO_128K_AAC, ITAG.DASH_AUDIO_48K_AAC };
                            bestHit = next.getValue().getInfoForFirstITAGMatch(itags);
                            break;
                        }
                        if (bestHit != null) {
                            /* find all available variants */
                            switch (convertTo) {
                            case VIDEO_WEBM:
                            case VIDEO_MP4:
                            case VIDEO_DASH_MP4:
                            case VIDEO_3GP:
                            case VIDEO_FLV:
                            case VIDEO_3D_MP4:
                            case VIDEO_3D_WEBM:
                            case AUDIO_DASH_AAC:
                                if (bestHit != null) {
                                    for (ITAG itag : itags) {
                                        if (availableItags.containsKey(itag) && itag.getVariantID() != null) variants.add(itag.getVariantID());
                                    }
                                }
                                break;
                            }
                        }
                        if (bestHit != null) {
                            DownloadLink infoLink = null;
                            switch (convertTo) {
                            case VIDEO_WEBM:
                            case VIDEO_MP4:
                            case VIDEO_3GP:
                            case VIDEO_FLV:
                            case VIDEO_3D_MP4:
                            case VIDEO_3D_WEBM:
                                infoLink = this.createDownloadlink(formattedFilename, currentVideoUrl, ytID, YT_FILENAME, convertTo, formatInName, bestHit, null);
                                break;
                            case VIDEO_DASH_MP4:
                                infoLink = this.createDownloadlink(formattedFilename, currentVideoUrl, ytID, YT_FILENAME, convertTo, formatInName, bestHit, bestDashAudio);
                                break;
                            case AUDIO_DASH_AAC:
                                infoLink = this.createDownloadlink(formattedFilename, currentVideoUrl, ytID, YT_FILENAME, convertTo, formatInName, null, bestHit);
                                break;
                            }
                            if (infoLink != null) {
                                if (variants.size() > 1) {
                                    if (bestHit != null) {
                                        infoLink.setProperty("VARIANT", bestHit.itag.getVariantID());
                                        infoLink.setProperty("VARIANTS", variants);
                                        setVariantsSupport(infoLink, true);
                                    }
                                }
                                infoLink.setProperty("LINKDUPEID", "ytID" + ytID + "." + convertTo.name());
                                if (filePackage != null) filePackage.add(infoLink);
                                decryptedLinks.add(infoLink);
                            } else {
                                logger.info("createDownloadLink failed for " + convertTo.name());
                            }
                        }
                    } else {
                        for (final Info info : next.getValue()) {
                            final DownloadLink infoLink = this.createDownloadlink(formattedFilename, currentVideoUrl, ytID, YT_FILENAME, convertTo, formatInName, info, bestDashAudio);
                            if (infoLink != null) {
                                infoLink.setProperty("LINKDUPEID", "ytID" + ytID + "." + info.itag.getITAG());
                                if (filePackage != null) filePackage.add(infoLink);
                                decryptedLinks.add(infoLink);
                            } else {
                                logger.info("createDownloadLink failed for " + convertTo.name());
                            }
                        }
                    }
                }

                final String VIDEOID = new Regex(currentVideoUrl, "watch\\?v=([\\w_\\-]+)").getMatch(0);
                // Grab Subtitles
                if (cfg.getBooleanProperty("ALLOW_SUBTITLES_V2", true)) {
                    FilePackage filePackage = filepackages.get(NAME_SUBTITLES);
                    if (filePackage == null) {
                        filePackage = FilePackage.getInstance();
                        filePackage.setProperty("ALLOW_MERGE", true);
                        String subtitles = "(Subtitles)";
                        if (multiple_videos)
                            filePackage.setName("Youtube " + subtitles);
                        else if (!cfg.getBooleanProperty("GROUP_FORMAT", false))
                            filePackage.setName(YT_FILENAME);
                        else
                            filePackage.setName(YT_FILENAME + " " + subtitles);
                        filepackages.put(NAME_SUBTITLES, filePackage);
                    }
                    /* new tts */

                    if (TTSURL != null) {
                        TTSURL = unescape(TTSURL.replaceAll("\\\\/", "/"));
                        br.getPage(preferHTTPS(TTSURL + "&asrs=1&fmts=1&tlangs=1&ts=" + System.currentTimeMillis() + "&type=list"));
                        TTSURL = TTSURL.replaceFirst("v=[a-zA-Z0-9\\-_]+", "");
                    } else {
                        /* old tts */
                        br.getPage(preferHTTPS("http://www.youtube.com/api/timedtext?type=list&v=" + VIDEOID));
                    }
                    // br.getRegex("<track id=\"(.*?)\" name=\"(.*?)\" lang_code=\"(.*?)\" lang_original=\"(.*?)\".*?/>")
                    String[] matches = br.getRegex("<track id=\"(.*?)\".*?/>").getColumn(0);
                    HashSet<String> duplicate = new HashSet<String>();
                    for (String trackID : matches) {
                        String lang = br.getRegex("<track id=\"" + trackID + "\".*?lang_code=\"(.*?)\".*?/>").getMatch(0);
                        String name = br.getRegex("<track id=\"" + trackID + "\".*?name=\"(.*?)\".*?/>").getMatch(0);
                        String kind = br.getRegex("<track id=\"" + trackID + "\".*?kind=\"(.*?)\".*?/>").getMatch(0);
                        String langOrg = br.getRegex("<track id=\"" + trackID + "\".*?lang_original=\"(.*?)\".*?/>").getMatch(0);
                        if (name == null) name = "";
                        if (kind == null) kind = "";
                        if (duplicate.add(lang) == false) continue;
                        String link = null;
                        if (TTSURL == null) {
                            link = preferHTTPS("http://www.youtube.com/api/timedtext?type=track&name=" + URLEncoder.encode(name, "UTF-8") + "&lang=" + URLEncoder.encode(lang, "UTF-8") + "&v=" + URLEncoder.encode(VIDEOID, "UTF-8"));
                        } else {
                            link = preferHTTPS(TTSURL + "&kind=" + URLEncoder.encode(kind, "UTF-8") + "&format=1&ts=" + System.currentTimeMillis() + "&type=track&lang=" + URLEncoder.encode(lang, "UTF-8") + "&name=" + URLEncoder.encode(name, "UTF-8") + "&v=" + URLEncoder.encode(VIDEOID, "UTF-8"));
                        }
                        DownloadLink dlink = this.createDownloadlink("youtubeJD" + link);
                        dlink.setProperty("ALLOW_DUPE", true);
                        dlink.setBrowserUrl(currentVideoUrl);

                        /** FILENAME PART3 START */
                        String subtitleName = formattedFilename;
                        if (langOrg != null) {
                            subtitleName = subtitleName.replace("*ext*", " (" + langOrg + ").xml");
                        }
                        subtitleName = subtitleName.replace("*quality*", " " + NAME_SUBTITLES);
                        subtitleName = subtitleName.replace("*videoname*", YT_FILENAME);
                        dlink.setFinalFileName(subtitleName);
                        dlink.setProperty("name", subtitleName);
                        /** FILENAME PART3 END */

                        dlink.setProperty("subtitle", true);
                        dlink.setProperty("videolink", currentVideoUrl);
                        filePackage.add(dlink);
                        decryptedLinks.add(dlink);
                    }
                }

                // Grab thumbnails
                FilePackage filePackage = filepackages.get(NAME_THUMBNAILS);
                if ((cfg.getBooleanProperty("ALLOW_THUMBNAIL_HQ", false) || cfg.getBooleanProperty("ALLOW_THUMBNAIL_MQ", false) || cfg.getBooleanProperty("ALLOW_THUMBNAIL_DEFAULT", false) || cfg.getBooleanProperty("ALLOW_THUMBNAIL_MAX", false)) && filePackage == null) {
                    filePackage = FilePackage.getInstance();
                    filePackage.setProperty("ALLOW_MERGE", true);
                    String thumbnails = "(Thumbnails)";
                    if (multiple_videos)
                        filePackage.setName("Youtube " + thumbnails);
                    else if (!cfg.getBooleanProperty("GROUP_FORMAT", false))
                        filePackage.setName(YT_FILENAME);
                    else
                        filePackage.setName(YT_FILENAME + " " + thumbnails);
                    filepackages.put(NAME_THUMBNAILS, filePackage);
                }

                if (cfg.getBooleanProperty("ALLOW_THUMBNAIL_MAX", false)) {
                    decryptedLinks.add(createThumbnailDownloadLink(YT_FILENAME + " (MAX).jpg", "http://img.youtube.com/vi/" + VIDEOID + "/maxresdefault.jpg", currentVideoUrl, filePackage));
                }

                if (cfg.getBooleanProperty("ALLOW_THUMBNAIL_HQ", false)) {
                    decryptedLinks.add(createThumbnailDownloadLink(YT_FILENAME + " (HQ).jpg", "http://img.youtube.com/vi/" + VIDEOID + "/hqdefault.jpg", currentVideoUrl, filePackage));
                }

                if (cfg.getBooleanProperty("ALLOW_THUMBNAIL_MQ", false)) {
                    decryptedLinks.add(createThumbnailDownloadLink(YT_FILENAME + " (MQ).jpg", "http://img.youtube.com/vi/" + VIDEOID + "/mqdefault.jpg", currentVideoUrl, filePackage));
                }

                if (cfg.getBooleanProperty("ALLOW_THUMBNAIL_DEFAULT", false)) {
                    decryptedLinks.add(createThumbnailDownloadLink(YT_FILENAME + ".jpg", "http://img.youtube.com/vi/" + VIDEOID + "/default.jpg", currentVideoUrl, filePackage));
                }
            } catch (final IOException e) {
                this.br.getHttpConnection().disconnect();
                logger.log(java.util.logging.Level.SEVERE, "Exception occurred", e);
                // return null;
            }
        }

        // No links found -> Link is probably offline
        if (decryptedLinks == null || decryptedLinks.isEmpty()) {
            if (verifyAge || this.br.getURL().toLowerCase().indexOf("youtube.com/get_video_info?") != -1 && !prem) { throw new DecrypterException(DecrypterException.ACCOUNT); }
            final DownloadLink offline = getOfflineLink();
            offline.setName(getVideoID(parameter));
            decryptedLinks.add(offline);
        }

        if (!dupeList.isEmpty()) logger.info("Time to decrypt : " + ((System.currentTimeMillis() - startTime) / 1000) + " seconds. Returning " + dupeList.size() + " Videos from List for " + parameter);

        return decryptedLinks;
    }

    private DownloadLink createDownloadlink(String formattedFilename, String currentVideoURL, String ytID, String YT_FILENAME, DestinationFormat convertTo, boolean formatInName, Info videoInfo, Info audioInfo) {
        DownloadLink thislink = null;
        String desc = null;
        if (videoInfo != null) {
            desc = videoInfo.desc;
            thislink = this.createDownloadlink("youtubeJD" + videoInfo.link);
        } else if (audioInfo != null) {
            desc = audioInfo.desc;
            thislink = this.createDownloadlink("youtubeJD" + audioInfo.link);
        } else {
            return null;
        }
        if (formatInName == false) {
            desc = "";
        }
        if (videoInfo != null) {
            switch (videoInfo.itag.getITAG()) {
            case 82:
            case 83:
            case 84:
            case 85:
            case 100:
            case 101:
            case 102:
                desc = "(3D)" + desc;
                break;
            }
        }
        thislink.setAvailable(true);
        thislink.setBrowserUrl(currentVideoURL);
        /** FILENAME PART2 START */
        String currentFilename = formattedFilename;
        currentFilename = currentFilename.replace("*quality*", desc);
        currentFilename = currentFilename.replace("*ext*", convertTo.getExtFirst());
        currentFilename = currentFilename.replace("*videoname*", YT_FILENAME);
        if (currentFilename.equals(formattedFilename)) {
            /* avoid multiple string instances */
            currentFilename = formattedFilename;
        }
        if (DestinationFormat.AUDIO_DASH_AAC.equals(convertTo)) {
            audioInfo = videoInfo;
            if (audioInfo == null) return null;
            /* special AUDIO-DASH handling */
            thislink.setFinalFileName(currentFilename);
            thislink.setProperty("name", currentFilename);
            thislink.setProperty("DASH", true);
            long completeSize = audioInfo.size;
            if (audioInfo.size > 0) {
                thislink.setProperty("size", completeSize);
                thislink.setDownloadSize(completeSize);
            }
            thislink.setProperty("DASH_AUDIO", audioInfo.itag.getITAG());
            thislink.setProperty("DASH_AUDIO_SIZE", audioInfo.size);
        } else if (DestinationFormat.VIDEO_DASH_MP4.equals(convertTo)) {
            if (audioInfo == null) return null;
            /* special VIDEO-DASH handling */
            thislink.setFinalFileName(currentFilename);
            thislink.setProperty("name", currentFilename);
            thislink.setProperty("DASH", true);
            long completeSize = videoInfo.size + audioInfo.size;
            if (videoInfo.size > 0 && audioInfo.size > 0) {
                thislink.setProperty("size", completeSize);
                thislink.setDownloadSize(completeSize);
            }
            thislink.setProperty("DASH_AUDIO", audioInfo.itag.getITAG());
            thislink.setProperty("DASH_AUDIO_SIZE", audioInfo.size);
            thislink.setProperty("DASH_VIDEO", videoInfo.itag.getITAG());
            thislink.setProperty("DASH_VIDEO_SIZE", videoInfo.size);
        } else {
            /* normal handling */
            thislink.setProperty("fmtNew", videoInfo.itag.getITAG());
            thislink.setFinalFileName(currentFilename);
            if (convertTo != DestinationFormat.AUDIO_MP3) {
                thislink.setProperty("name", currentFilename);
            } else {
                // because demuxer will fail when mp3 file already exists
                String audioName = formattedFilename;
                audioName = audioName.replace("*quality*", desc);
                audioName = audioName.replace("*ext*", ".tmp");
                audioName = audioName.replace("*videoname*", YT_FILENAME);
                if (audioName.equals(formattedFilename)) {
                    /* avoid multiple string instances */
                    audioName = formattedFilename;
                }
                thislink.setProperty("name", audioName);
            }
            thislink.setProperty("size", videoInfo.size);
            if (videoInfo.size > 0) {
                thislink.setDownloadSize(videoInfo.size);
                thislink.setProperty("VERIFIEDFILESIZE", videoInfo.size);
            }
            thislink.setProperty("convertto", convertTo.name());
        }
        /** FILENAME PART2 END */
        thislink.setProperty("videolink", currentVideoURL);
        thislink.setProperty("valid", true);
        thislink.setProperty("ytID", ytID);
        return thislink;
    }

    protected void setVariantsSupport(DownloadLink link, boolean variantsSupported) {
    }

    private DownloadLink createThumbnailDownloadLink(String name, String link, String browserurl, FilePackage filePackage) {
        DownloadLink dlink = this.createDownloadlink("youtubeJD" + link);
        dlink.setProperty("ALLOW_DUPE", true);
        filePackage.add(dlink);
        dlink.setBrowserUrl(browserurl);

        dlink.setFinalFileName(name);
        dlink.setProperty("name", name);
        dlink.setProperty("thumbnail", true);

        return dlink;
    }

    private HashMap<Integer, String[]> parseLinks(Browser br, String html5_fmt_map, boolean allowVideoOnly) throws IOException, PluginException {
        final HashMap<Integer, String[]> links = new HashMap<Integer, String[]>();
        if (html5_fmt_map != null) {
            if (html5_fmt_map.contains(UNSUPPORTEDRTMP)) { return links; }
            String[] html5_hits = new Regex(html5_fmt_map, "(.*?)(,|$)").getColumn(0);
            if (html5_hits != null) {
                for (String hit : html5_hits) {
                    hit = unescape(hit);
                    String hitUrl = new Regex(hit, "url=(http.*?)(\\&|$)").getMatch(0);
                    String sig = new Regex(hit, "url=http.*?(\\&|$)(sig|signature)=(.*?)(\\&|$)").getMatch(2);
                    if (sig == null) sig = new Regex(hit, "(sig|signature)=(.*?)(\\&|$)").getMatch(1);
                    if (sig == null) sig = new Regex(hit, "(sig|signature)%3D(.*?)%26").getMatch(1);
                    if (sig == null) sig = descrambleSignature(new Regex(hit, "s=(.*?)(\\&|$)").getMatch(0), br);
                    String hitFmt = new Regex(hit, "itag=(\\d+)").getMatch(0);
                    String hitQ = new Regex(hit, "quality=(.*?)(\\&|$)").getMatch(0);
                    if (hitQ == null && allowVideoOnly) {
                        if (hitFmt != null) {
                            switch (Integer.parseInt(hitFmt)) {
                            case 38:
                            case 138:
                                hitQ = "2160p";
                                break;
                            case 160:
                                hitQ = "144p";
                                break;
                            case 137:
                                hitQ = "1080p";
                                break;
                            case 136:
                                hitQ = "720p";
                                break;
                            case 135:
                                hitQ = "480p";
                                break;
                            case 134:
                                hitQ = "360p";
                                break;
                            case 133:
                                hitQ = "240p";
                                break;
                            case 139:
                            case 140:
                            case 141:
                                /* aac audio */
                                hitQ = "";
                                break;
                            }
                        }
                        if (hitQ == null) hitQ = "unknown";
                    }
                    if (hitUrl != null && hitFmt != null && hitQ != null) {
                        hitUrl = unescape(hitUrl.replaceAll("\\\\/", "/"));
                        if (hitUrl.startsWith("http%253A")) {
                            hitUrl = Encoding.htmlDecode(hitUrl);
                        }
                        String[] inst = null;
                        if (hitUrl.contains("sig")) {
                            inst = new String[] { Encoding.htmlDecode(Encoding.urlDecode(hitUrl, true)), hitQ };
                        } else {
                            inst = new String[] { Encoding.htmlDecode(Encoding.urlDecode(hitUrl, true) + "&signature=" + sig), hitQ };
                        }
                        links.put(Integer.parseInt(hitFmt), inst);
                    }
                }
            }
        }
        return links;
    }

    private HashMap<Integer, String[]> parseLinks(Browser br, final String videoURL, String YT_FILENAME, boolean ythack, boolean tryGetDetails) throws InterruptedException, IOException, PluginException {
        final HashMap<Integer, String[]> links = new HashMap<Integer, String[]>();
        String html5_fmt_map = br.getRegex("\"html5_fmt_map\": \\[(.*?)\\]").getMatch(0);

        if (html5_fmt_map != null) {
            String[] html5_hits = new Regex(html5_fmt_map, "\\{(.*?)\\}").getColumn(0);
            if (html5_hits != null) {
                for (String hit : html5_hits) {
                    String hitUrl = new Regex(hit, "url\": \"(http:.*?)\"").getMatch(0);
                    String hitFmt = new Regex(hit, "itag\": (\\d+)").getMatch(0);
                    String hitQ = new Regex(hit, "quality\": \"(.*?)\"").getMatch(0);
                    if (hitUrl != null && hitFmt != null && hitQ != null) {
                        hitUrl = unescape(hitUrl.replaceAll("\\\\/", "/"));
                        links.put(Integer.parseInt(hitFmt), new String[] { Encoding.htmlDecode(Encoding.urlDecode(hitUrl, true)), hitQ });
                    }
                }
            }
        } else {
            /* new format since ca. 1.8.2011 */
            html5_fmt_map = br.getRegex("\"url_encoded_fmt_stream_map\": \"(.*?)\"").getMatch(0);
            if (html5_fmt_map == null) {
                html5_fmt_map = br.getRegex("url_encoded_fmt_stream_map=(.*?)(&|$)").getMatch(0);
                if (html5_fmt_map != null) {
                    html5_fmt_map = html5_fmt_map.replaceAll("%2C", ",");
                    if (!html5_fmt_map.contains("url=")) {
                        html5_fmt_map = html5_fmt_map.replaceAll("%3D", "=");
                        html5_fmt_map = html5_fmt_map.replaceAll("%26", "&");
                    }
                }
            }
            if (html5_fmt_map != null && !html5_fmt_map.contains("signature") && !html5_fmt_map.contains("sig") && !html5_fmt_map.contains("s=")) {
                Thread.sleep(5000);
                br.clearCookies(getHost());
                return null;
            }
            if (html5_fmt_map != null) {
                HashMap<Integer, String[]> ret = parseLinks(br, html5_fmt_map, false);
                if (ret.size() == 0) return links;
                links.putAll(ret);
                if (isJDownloader2()) {
                    /* not playable by vlc */
                    /* check for adaptive fmts */
                    String adaptive = br.getRegex("\"adaptive_fmts\": \"(.*?)\"").getMatch(0);
                    ret = parseLinks(br, adaptive, true);
                    links.putAll(ret);
                }
            } else {
                if (br.containsHTML("reason=Unfortunately")) return null;
                if (tryGetDetails == true) {
                    br.getPage("http://www.youtube.com/get_video_info?el=detailpage&video_id=" + getVideoID(videoURL));
                    return parseLinks(br, videoURL, YT_FILENAME, ythack, false);
                } else {
                    return null;
                }
            }
        }

        /* normal links */
        final HashMap<String, String> fmt_list = new HashMap<String, String>();
        String fmt_list_str = "";
        if (ythack) {
            fmt_list_str = (br.getMatch("&fmt_list=(.+?)&") + ",").replaceAll("%2F", "/").replaceAll("%2C", ",");
        } else {
            fmt_list_str = (br.getMatch("\"fmt_list\":\\s+\"(.+?)\",") + ",").replaceAll("\\\\/", "/");
        }
        final String fmt_list_map[][] = new Regex(fmt_list_str, "(\\d+)/(\\d+x\\d+)/\\d+/\\d+/\\d+,").getMatches();
        for (final String[] fmt : fmt_list_map) {
            fmt_list.put(fmt[0], fmt[1]);
        }
        if (links.size() == 0 && ythack) {
            /* try to find fallback links */
            String urls[] = br.getRegex("url%3D(.*?)($|%2C)").getColumn(0);
            int index = 0;
            for (String vurl : urls) {
                String hitUrl = new Regex(vurl, "(.*?)%26").getMatch(0);
                String hitQ = new Regex(vurl, "%26quality%3D(.*?)%").getMatch(0);
                if (hitUrl != null && hitQ != null) {
                    hitUrl = unescape(hitUrl.replaceAll("\\\\/", "/"));
                    if (fmt_list_map.length >= index) {
                        links.put(Integer.parseInt(fmt_list_map[index][0]), new String[] { Encoding.htmlDecode(Encoding.urlDecode(hitUrl, false)), hitQ });
                        index++;
                    }
                }
            }
        }
        for (Integer fmt : links.keySet()) {
            String fmt2 = fmt + "";
            if (fmt_list.containsKey(fmt2)) {
                String Videoq = links.get(fmt)[1];
                final Integer q = Integer.parseInt(fmt_list.get(fmt2).split("x")[1]);
                if (fmt == 17) {
                    Videoq = "144p";
                } else if (fmt == 40) {
                    Videoq = "240p Light";
                } else if (q > 1080) {
                    Videoq = "2160p";
                } else if (q > 720) {
                    Videoq = "1080p";
                } else if (q > 576) {
                    Videoq = "720p";
                } else if (q > 480) {
                    Videoq = "520p";
                } else if (q > 360) {
                    Videoq = "480p";
                } else if (q > 240) {
                    Videoq = "360p";
                } else {
                    Videoq = "240p";
                }
                links.get(fmt)[1] = Videoq;
            }
        }
        if (YT_FILENAME != null && links != null && !links.isEmpty()) {
            links.put(-1, new String[] { YT_FILENAME });
        }
        return links;
    }

    public HashMap<Integer, String[]> getLinks(final String video, final boolean prem, Browser br, int retrycount) throws Exception {
        if (retrycount > 2) {
            // do not retry more often than 2 time
            return null;
        }
        if (br == null) {
            br = this.br;
        }

        try {
            gsProxy(true);
        } catch (Throwable e) {
            /* does not exist in 09581 */
        }
        br.setFollowRedirects(true);
        /* this cookie makes html5 available and skip controversy check */
        br.setCookie("youtube.com", "PREF", "f2=40100000&hl=en-GB");
        br.getHeaders().put("User-Agent", "Wget/1.12");
        br.getPage(video);
        if (br.containsHTML("id=\"unavailable-submessage\" class=\"watch-unavailable-submessage\"")) { return null; }
        final String VIDEOID = new Regex(video, "watch\\?v=([\\w_\\-]+)").getMatch(0);
        boolean fileNameFound = false;
        String YT_FILENAME = VIDEOID;
        if (br.containsHTML("&title=")) {
            YT_FILENAME = Encoding.htmlDecode(br.getRegex("&title=([^&$]+)").getMatch(0).replaceAll("\\+", " ").trim());
            fileNameFound = true;
        }
        final String url = br.getURL();
        boolean ythack = false;
        if (url != null && !url.equals(video)) {
            /* age verify with activated premium? */
            if (url.toLowerCase(Locale.ENGLISH).indexOf("youtube.com/verify_age?next_url=") != -1) {
                verifyAge = true;
            }
            if (url.toLowerCase(Locale.ENGLISH).indexOf("youtube.com/verify_age?next_url=") != -1 && prem) {
                final String session_token = br.getRegex("onLoadFunc.*?gXSRF_token = '(.*?)'").getMatch(0);
                final LinkedHashMap<String, String> p = Request.parseQuery(url);
                final String next = p.get("next_url");
                final Form form = new Form();
                form.setAction(url);
                form.setMethod(MethodType.POST);
                form.put("next_url", "%2F" + next.substring(1));
                form.put("action_confirm", "Confirm+Birth+Date");
                form.put("session_token", Encoding.urlEncode(session_token));
                br.submitForm(form);
                if (br.getCookie("http://www.youtube.com", "is_adult") == null) { return null; }
            } else if (url.toLowerCase(Locale.ENGLISH).indexOf("youtube.com/index?ytsession=") != -1 || url.toLowerCase(Locale.ENGLISH).indexOf("youtube.com/verify_age?next_url=") != -1 && !prem) {
                ythack = true;
                br.getPage("http://www.youtube.com/get_video_info?video_id=" + VIDEOID);
                if (br.containsHTML("&title=") && fileNameFound == false) {
                    YT_FILENAME = Encoding.htmlDecode(br.getRegex("&title=([^&$]+)").getMatch(0).replaceAll("\\+", " ").trim());
                    fileNameFound = true;
                }
            } else if (url.toLowerCase(Locale.ENGLISH).indexOf("google.com/accounts/servicelogin?") != -1) {
                // private videos
                return null;
            }
        }
        Form forms[] = br.getForms();
        if (forms != null) {
            for (Form form : forms) {
                if (form.getAction() != null && form.getAction().contains("verify_age")) {
                    logger.info("Verify Age");
                    br.submitForm(form);
                    break;
                }
            }
        }
        // if (br.containsHTML("This video may be inappropriate for some users")) {
        // /* commented because those are the regex to login/verify */
        // String verifyURL = br.getRegex("(https?://accounts.google.com/ServiceLogin\\?ltmpl=verifyage.*?)\"").getMatch(0);
        // if (verifyURL == null) verifyURL =
        // br.getRegex("(https?://accounts.google.com/ServiceLogin\\?.*?ltmpl=verifyage.*?)\"").getMatch(0);
        // if (verifyURL != null) {
        // verifyURL = Encoding.htmlDecode(verifyURL);
        // br.getPage(verifyURL);
        // }
        // }
        /* html5_fmt_map */
        if (br.getRegex(YT_FILENAME_PATTERN).count() != 0 && fileNameFound == false) {
            YT_FILENAME = Encoding.htmlDecode(br.getRegex(YT_FILENAME_PATTERN).getMatch(0).trim());
            fileNameFound = true;
        }
        HashMap<Integer, String[]> links = parseLinks(br, video, YT_FILENAME, ythack, false);
        return links;
    }

    private static WeakHashMap<String, String> JS_CACHE = new WeakHashMap<String, String>();

    /**
     * *
     * 
     * @param s
     * @return
     * @throws IOException
     * @throws PluginException
     */
    private String descrambleSignature(String sig, Browser br) throws IOException, PluginException {
        if (sig == null) return null;
        String jsUrl = br.getMatch("\"js\"\\: \"(.+?)\"");
        jsUrl = jsUrl.replace("\\/", "/");
        jsUrl = "http:" + jsUrl;
        String js = null;
        synchronized (JS_CACHE) {
            js = JS_CACHE.get(jsUrl);
            if (js == null) {
                js = br.cloneBrowser().getPage(jsUrl);
                JS_CACHE.put(jsUrl, js);
            }
        }
        String descrambler = new Regex(js, "\\w+\\.signature\\=([\\w\\d]+)\\([\\w\\d]+\\)").getMatch(0);

        String func = "function " + descrambler + "\\(([^)]+)\\)\\{(.+?return.*?)\\}";
        String des = new Regex(js, Pattern.compile(func)).getMatch(1);
        String s = sig;
        // Debug code.
        // Context cx = null;
        // Object result = null;
        // try {
        //
        // try {
        // cx = ContextFactory.getGlobal().enterContext();
        //
        // } catch (java.lang.SecurityException e) {
        // /* in case classshutter already set */
        // }
        // Scriptable scope = cx.initStandardObjects();
        // String all = new Regex(js, Pattern.compile("function " + descrambler +
        // "\\(([^)]+)\\)\\{(.+?return.*?)\\}.*?\\{.*?\\}")).getMatch(-1);
        // result = cx.evaluateString(scope, all + " " + descrambler + "(\"" + sig + "\")", "<cmd>", 1, null);
        //
        // } finally {
        // try {
        // Context.exit();
        // } catch (final Throwable e) {
        // }
        // }
        for (String line : new Regex(des, "[^;]+").getColumn(-1)) {
            s = handleRule(s, line);
        }
        logger.info(s);
        // logger.info(result);
        // logger.info(result.equals(s));
        logger.info("Signature Dec:" + s);
        return s;

    }

    private String handleRule(String s, String line) throws PluginException {
        logger.info(line);
        logger.info("FR " + s);
        String method = new Regex(line, "\\.([\\w\\d]+?)\\(\\s*\\)").getMatch(0);
        if ("reverse".equals(method)) {
            //
            s = new StringBuilder(s).reverse().toString();
            logger.info("TO " + s);
            return s;
        }
        // slice
        String i = new Regex(line, "\\.slice\\((\\d+)\\)").getMatch(0);
        if (i != null) {
            //
            s = s.substring(Integer.parseInt(i));
            logger.info("TO " + s);
            return s;
        }

        String idx = new Regex(line, "=..\\([^,]+\\,(\\d+)\\)").getMatch(0);
        if (idx != null) {
            int idxI = Integer.parseInt(idx);
            s = pk(s, idxI);
            logger.info("TO " + s);
            return s;

        }

        if (new Regex(line, "\\.split\\(\"\"\\)").matches()) { return s; }
        if (new Regex(line, "\\.join\\(\"\"\\)").matches()) { return s; }
        throw new PluginException(LinkStatus.ERROR_PLUGIN_DEFECT, "Unknown Signature Rule: " + line);

    }

    protected static String pk(String s, int idxI) {
        char c = s.charAt(0);
        StringBuilder sb = new StringBuilder();
        sb.append(s.charAt(idxI % s.length()));
        sb.append(s.substring(1, idxI));
        sb.append(c);
        sb.append(s.substring(idxI + 1));
        return sb.toString();
    }

    private DownloadLink getOfflineLink() {
        final DownloadLink offline = createDownloadlink("youtubeJD" + preferHTTPS("http") + "://r20---sn-4g57kner.c.youtube.com/videoplayback?sver=3&id=a3a288054be22ded&ipbits=8&itag=43&expire=1376781178&ratebypass=yes&fexp=927826%2C903903%2C920604%2C932237%2C927839%2C916626%2C931005%2C909546%2C906397%2C929117%2C929121%2C929906%2C929907%2C929922%2C929127%2C929129%2C929131%2C929930%2C925720%2C925722%2C925718%2C925714%2C929917%2C929919%2C929933%2C912521%2C932306%2C913428%2C920605%2C904830%2C919373%2C930803%2C904122%2C938701%2C919008%2C911423%2C909549%2C900816%2C912711%2C935802%2C904494%2C906001&ms=au&cp=U0hWS1dTT19MUkNONl9PTVNCOnozWXhMQzNfazJD&ip=87.160.200.202&key=yt1&mt=1376758530&sparams=cp%2Cid%2Cip%2Cipbits%2Citag%2Cratebypass%2Csource%2Cupn%2Cexpire&mv=m&upn=VoejVd34qx8&source=youtube&signature=7D265E3116EA58DFDE26BF4A210B5B33753FCD69." + new Random().nextInt(100000));
        offline.setAvailable(false);
        offline.setProperty("offline", true);
        return offline;
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

    private String preferHTTPS(final String s) {
        if (pluginloaded == false) {
            final PluginForHost plugin = JDUtilities.getPluginForHost("youtube.com");
            if (plugin == null) throw new IllegalStateException("youtube plugin not found!");
            pluginloaded = true;
        }
        boolean prefers = getPluginConfig().getBooleanProperty("PREFER_HTTPS", jd.plugins.hoster.Youtube.defaultCustomPreferHTTPS);
        if (prefers)
            return s.replaceFirst("http://", "https://");
        else
            return s.replaceFirst("https://", "http://");
    }

    @Override
    public void init() {
        Browser.setRequestIntervalLimitGlobal(this.getHost(), 100);
    }

    private boolean login(final Account account) throws Exception {
        this.setBrowserExclusive();
        final PluginForHost plugin = JDUtilities.getPluginForHost("youtube.com");
        try {
            if (plugin != null) {
                ((jd.plugins.hoster.Youtube) plugin).login(account, this.br, false, false);
            } else {
                return false;
            }
        } catch (final PluginException e) {
            account.setValid(false);
            return false;
        }
        return true;
    }

    private boolean isJDownloader2() {
        return System.getProperty("jd.revision.jdownloaderrevision") != null;
    }

    /* NO OVERRIDE!! */
    public boolean hasCaptcha(CryptedLink link, jd.plugins.Account acc) {
        return false;
    }

}